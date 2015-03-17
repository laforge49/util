package org.agilewiki.utils.durable;

import org.agilewiki.utils.maplist.ListAccessor;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An immutable versioning list.
 */
public class DurableListNode {
    public final static char LIST_NODE_ID = 'l';
    public final static char LIST_NIL_ID = '1';

    /**
     * A time after all insertions and deletions.
     */
    public final static long MAX_TIME = Long.MAX_VALUE - 1;

    /**
     * The root node of an empty tree.
     */
    public final static DurableListNode LIST_NIL = new DurableListNode();

    protected final int level;
    protected final DurableListNode leftNode;
    protected final DurableListNode rightNode;
    protected final Object value;
    protected final long created;
    protected final long deleted;
    protected final int size;
    protected final DurableFactory valueFactory;
    protected final int durableLength;

    protected DurableListNode() {
        level = 0;
        leftNode = this;
        rightNode = this;
        value = null;
        created = 0L;
        deleted = 0L;
        size = 0;
        valueFactory = null;
        durableLength = 2;
    }

    protected DurableListNode(int level,
                              DurableListNode leftNode,
                              DurableListNode rightNode,
                              Object value,
                              long created,
                              long deleted) {
        this(
                level,
                leftNode,
                rightNode,
                value,
                created,
                deleted,
                FactoryRegistry.getDurableFactory(value));
    }

    protected DurableListNode(int level,
                              DurableListNode leftNode,
                              DurableListNode rightNode,
                              Object value,
                              long created,
                              long deleted,
                              DurableFactory valueFactory) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.value = value;
        this.created = created;
        this.deleted = deleted;
        size = leftNode.size + rightNode.size + 1;
        this.valueFactory = valueFactory;
        durableLength = 22 +
                leftNode.getDurableLength() +
                valueFactory.getDurableLength(value) +
                rightNode.getDurableLength();
    }

    /**
     * Returns the count of all the values in the list, deleted or not.
     *
     * @return The count of all the values in the list.
     */
    public int totalSize() {
        return size;
    }

    protected boolean exists(long time) {
        return time >= created && time < deleted;
    }

    protected boolean isNil() {
        return this == LIST_NIL;
    }

    /**
     * Returns the count of all the values currently in the list.
     *
     * @param time The time of the query.
     * @return The current size of the list.
     */
    public int size(long time) {
        if (isNil())
            return 0;
        int s = leftNode.size(time) + rightNode.size(time);
        if (exists(time))
            s += 1;
        return s;
    }

    /**
     * Returns a value if it is in range and the value exists for the given time.
     *
     * @param ndx  The index of the selected value.
     * @param time The time of the query.
     * @return A value, or null.
     */
    public Object get(int ndx, long time) {
        DurableListNode n = getListNode(ndx);
        if (n == null || !n.exists(time))
            return null;
        return n.value;
    }

    /**
     * Get the index of an existing value with the same identity (==).
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    public int getIndex(Object value, long time) {
        if (isNil())
            return -1;
        int ndx = leftNode.getIndex(value, time);
        if (ndx > -1)
            return ndx;
        if (this.value == value && exists(time))
            return leftNode.size;
        ndx = rightNode.getIndex(value, time);
        if (ndx == -1)
            return -1;
        return leftNode.size + 1 + ndx;
    }

    /**
     * Get the index of an existing value with the same identity (==).
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    public int getIndexRight(Object value, long time) {
        if (isNil())
            return -1;
        int ndx = rightNode.getIndexRight(value, time);
        if (ndx > -1)
            return leftNode.size + 1 + ndx;
        if (this.value == value && exists(time))
            return leftNode.size;
        ndx = leftNode.getIndexRight(value, time);
        if (ndx == -1)
            return -1;
        return ndx;
    }

    /**
     * Find the index of an equal existing value.
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    public int findIndex(Object value, long time) {
        if (isNil())
            return -1;
        int ndx = leftNode.findIndex(value, time);
        if (ndx > -1)
            return ndx;
        if (exists(time) && this.value.equals(value))
            return leftNode.size;
        ndx = rightNode.findIndex(value, time);
        if (ndx == -1)
            return -1;
        return leftNode.size + 1 + ndx;
    }

    /**
     * Find the index of an equal existing value.
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    public int findIndexRight(Object value, long time) {
        if (isNil())
            return -1;
        int ndx = rightNode.findIndexRight(value, time);
        if (ndx > -1)
            return leftNode.size + 1 + ndx;
        if (exists(time) && this.value.equals(value))
            return leftNode.size;
        ndx = leftNode.findIndexRight(value, time);
        if (ndx == -1)
            return -1;
        return ndx;
    }

    protected DurableListNode getListNode(int ndx) {
        if (ndx < 0 || ndx >= size)
            return null; //out of range
        int leftSize = leftNode.size;
        if (ndx < leftSize)
            return leftNode.getListNode(ndx);
        if (ndx > leftSize)
            return rightNode.getListNode(ndx - leftSize - 1);
        return this;
    }

    /**
     * Returns the index of an existing value higher than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher, or -1.
     */
    public int higherIndex(int ndx, long time) {
        if (ndx >= size - 1 || isNil())
            return -1; //out of range
        int leftSize = leftNode.size;
        if (ndx < leftSize - 1) {
            int h = leftNode.higherIndex(ndx, time);
            if (h > -1)
                return h;
        }
        if (ndx < leftSize) {
            if (exists(time))
                return leftSize;
        }
        int h = rightNode.higherIndex(ndx - leftSize - 1, time);
        return h == -1 ? -1 : h + leftSize + 1;
    }

    /**
     * Returns the index of an existing value higher than or equal to the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher or equal, or -1.
     */
    public int ceilingIndex(int ndx, long time) {
        if (ndx >= size || isNil()) {
            return -1; //out of range
        }
        int leftSize = leftNode.size;
        if (ndx < leftSize) {
            int h = leftNode.ceilingIndex(ndx, time);
            if (h > -1)
                return h;
        }
        if (ndx <= leftSize) {
            if (exists(time))
                return leftSize;
        }
        int h = rightNode.ceilingIndex(ndx - leftSize - 1, time);
        return h <= -1 ? -1 : h + leftSize + 1;
    }

    /**
     * Returns the index of the first existing value in the list.
     *
     * @param time The time of the query.
     * @return The index of the first existing value in the list, or -1.
     */
    public int firstIndex(long time) {
        return ceilingIndex(0, time);
    }

    /**
     * Returns the index of an existing value lower than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is lower, or -1.
     */
    public int lowerIndex(int ndx, long time) {
        if (ndx <= 0 || isNil())
            return -1; //out of range
        int leftSize = leftNode.size;
        if (ndx > leftSize + 1) {
            int l = rightNode.lowerIndex(ndx - leftSize - 1, time);
            if (l > -1)
                return l + leftSize + 1;
        }
        if (ndx > leftSize) {
            if (exists(time))
                return leftSize;
        }
        return leftNode.lowerIndex(ndx, time);
    }

    /**
     * Returns the index of an existing value lower than or equal to the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is lower or equal, or -1.
     */
    public int floorIndex(int ndx, long time) {
        if (ndx < 0 || isNil())
            return -1; //out of range
        int leftSize = leftNode.size;
        if (ndx > leftSize) {
            int l = rightNode.floorIndex(ndx - leftSize - 1, time);
            if (l > -1)
                return l + leftSize + 1;
        }
        if (ndx >= leftSize) {
            if (exists(time))
                return leftSize;
        }
        return leftNode.floorIndex(ndx, time);
    }

    /**
     * Returns the index of the last existing value in the list.
     *
     * @param time The time of the query.
     * @return The index of the last existing value in the list, or -1.
     */
    public int lastIndex(long time) {
        return floorIndex(size, time);
    }

    /**
     * Returns true if there are no values present for the given time.
     *
     * @param time The time of the query.
     * @return Returns true if the list is empty for the given time.
     */
    public boolean isEmpty(long time) {
        if (isNil())
            return true;
        return !(exists(time) && leftNode.isEmpty(time) && rightNode.isEmpty(time));
    }

    protected void flatList(List list, long time) {
        if (isNil())
            return;
        leftNode.flatList(list, time);
        if (exists(time))
            list.add(value);
        rightNode.flatList(list, time);
    }

    /**
     * Returns a list of all the values that are present for a given time.
     *
     * @param time The time of the query.
     * @return A list of all values present for the given time.
     */
    public List flatList(long time) {
        List list = new ArrayList<>();
        flatList(list, time);
        return list;
    }

    /**
     * Returns an iterator over the existing values.
     *
     * @param time The time of the query.
     * @return The iterator.
     */
    public Iterator iterator(long time) {
        return new Iterator() {
            int last = -1;

            @Override
            public boolean hasNext() {
                return higherIndex(last, time) > -1;
            }

            @Override
            public Object next() {
                int next = higherIndex(last, time);
                if (next == -1)
                    throw new NoSuchElementException();
                last = next;
                return get(last, time);
            }
        };
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor() {
        return listAccessor(null, MAX_TIME);
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor(Comparable key) {
        return listAccessor(key, MAX_TIME);
    }

    /**
     * Returns a list accessor for the given time.
     *
     * @param key  The key for the list.
     * @param time The time of the query.
     * @return A list accessor for the given time.
     */
    public ListAccessor listAccessor(Comparable key, long time) {
        return new ListAccessor() {
            @Override
            public Comparable key() {
                return key;
            }

            @Override
            public long time() {
                return time;
            }

            @Override
            public int size() {
                return DurableListNode.this.size(time);
            }

            @Override
            public Object get(int ndx) {
                return DurableListNode.this.get(ndx, time);
            }

            @Override
            public int getIndex(Object value) {
                return DurableListNode.this.getIndex(value, time);
            }

            @Override
            public int getIndexRight(Object value) {
                return DurableListNode.this.getIndexRight(value, time);
            }

            @Override
            public int findIndex(Object value) {
                return DurableListNode.this.findIndex(value, time);
            }

            @Override
            public int findIndexRight(Object value) {
                return DurableListNode.this.findIndexRight(value, time);
            }

            @Override
            public int higherIndex(int ndx) {
                return DurableListNode.this.higherIndex(ndx, time);
            }

            @Override
            public int ceilingIndex(int ndx) {
                return DurableListNode.this.ceilingIndex(ndx, time);
            }

            @Override
            public int firstIndex() {
                return DurableListNode.this.firstIndex(time);
            }

            @Override
            public int lowerIndex(int ndx) {
                return DurableListNode.this.lowerIndex(ndx, time);
            }

            @Override
            public int floorIndex(int ndx) {
                return DurableListNode.this.floorIndex(ndx, time);
            }

            @Override
            public int lastIndex() {
                return DurableListNode.this.lastIndex(time);
            }

            @Override
            public boolean isEmpty() {
                return DurableListNode.this.isEmpty(time);
            }

            @Override
            public List flatList() {
                return DurableListNode.this.flatList(time);
            }

            @Override
            public Iterator iterator() {
                return DurableListNode.this.iterator(time);
            }
        };
    }

    protected DurableListNode skew() {
        if (isNil())
            return this;
        if (leftNode.isNil())
            return this;
        if (leftNode.level == level) {
            DurableListNode t = new DurableListNode(
                    level,
                    leftNode.rightNode,
                    rightNode,
                    value,
                    created,
                    deleted);
            DurableListNode l = new DurableListNode(
                    leftNode.level,
                    leftNode.leftNode,
                    t,
                    leftNode.value,
                    leftNode.created,
                    leftNode.deleted);
            return l;
        } else
            return this;
    }

    protected DurableListNode split() {
        if (isNil())
            return this;
        if (rightNode.isNil() || rightNode.rightNode.isNil())
            return this;
        if (level == rightNode.rightNode.level) {
            DurableListNode t = new DurableListNode(
                    level,
                    leftNode,
                    rightNode.leftNode,
                    value,
                    created,
                    deleted);
            DurableListNode r = new DurableListNode(
                    rightNode.level + 1,
                    t,
                    rightNode.rightNode,
                    rightNode.value,
                    rightNode.created,
                    rightNode.deleted);
            return r;
        }
        return this;
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public DurableListNode add(Object value, long time) {
        return add(-1, value, time);
    }

    /**
     * Add a non-null value to the list.
     *
     * @param ndx   Where to add the value, or -1 to append to the end.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public DurableListNode add(int ndx, Object value, long time) {
        return add(ndx, value, time, Long.MAX_VALUE);
    }

    protected DurableListNode add(int ndx, Object value, long created, long deleted) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            if (ndx != 0 && ndx != -1)
                throw new IllegalArgumentException("index out of range");
            return new DurableListNode(1, LIST_NIL, LIST_NIL, value, created, deleted);
        }
        if (ndx == -1)
            ndx = size;
        int leftSize = leftNode.size;
        DurableListNode t = this;
        if (ndx <= leftSize) {
            t = new DurableListNode(
                    level,
                    leftNode.add(ndx, value, created, deleted),
                    rightNode,
                    this.value,
                    this.created,
                    this.deleted);
        } else {
            t = new DurableListNode(
                    level,
                    leftNode,
                    rightNode.add(ndx - leftSize - 1, value, created, deleted),
                    this.value,
                    this.created,
                    this.deleted);
        }
        return t.skew().split();
    }

    /**
     * Mark a value as deleted.
     *
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public DurableListNode remove(int ndx, long time) {
        if (isNil())
            return this;
        int leftSize = leftNode.size;
        if (ndx == leftSize) {
            if (exists(time))
                return new DurableListNode(
                        level,
                        leftNode,
                        rightNode,
                        value,
                        created,
                        time);
            return this;
        }
        if (ndx < leftSize) {
            DurableListNode n = leftNode.remove(ndx, time);
            if (leftNode == n)
                return this;
            return new DurableListNode(
                    level,
                    n,
                    rightNode,
                    value,
                    created,
                    deleted);
        }
        DurableListNode n = rightNode.remove(ndx - leftSize - 1, time);
        if (rightNode == n)
            return this;
        return new DurableListNode(
                level,
                leftNode,
                n,
                value,
                created,
                deleted);
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public DurableListNode copyList() {
        return copyList(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public DurableListNode copyList(long time) {
        return copyList(LIST_NIL, time);
    }

    protected DurableListNode copyList(DurableListNode n, long time) {
        if (isNil())
            return n;
        n = leftNode.copyList(n, time);
        if (deleted >= time)
            n = n.add(n.size, value, created, deleted);
        return rightNode.copyList(n, time);
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned list.
     */
    public DurableListNode clearList(long time) {
        if (isNil())
            return this;
        DurableListNode ln = leftNode.clearList(time);
        DurableListNode rn = rightNode.clearList(time);
        if (ln == leftNode && rn == rightNode && !exists(time))
            return this;
        return new DurableListNode(level, ln, rn, value, created, time);
    }

    /**
     * Returns the size of a byte array needed to serialize this object,
     * including the space needed for the durable id.
     *
     * @return The size in bytes of the serialized data.
     */
    int getDurableLength() {
        return durableLength;
    }

    /**
     * Write the durable to a byte buffer.
     *
     * @param byteBuffer    The byte buffer.
     */
    public void writeDurable(ByteBuffer byteBuffer) {
        if (isNil()) {
            byteBuffer.putChar(LIST_NIL_ID);
            return;
        }
        byteBuffer.putChar(LIST_NODE_ID);
        serialize(byteBuffer);
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer    Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer) {
        byteBuffer.putInt(level);
        byteBuffer.putLong(created);
        byteBuffer.putLong(deleted);
        leftNode.writeDurable(byteBuffer);
        valueFactory.writeDurable(value, byteBuffer);
        rightNode.writeDurable(byteBuffer);
    }
}

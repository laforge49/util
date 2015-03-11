package org.agilewiki.utils.maplist.immutable;

import org.agilewiki.utils.maplist.ListAccessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An immutable versioning list.
 */
public class ImmutableListNode {
    /**
     * A time after all insertions and deletions.
     */
    public final static long MAX_TIME = Integer.MAX_VALUE - 1;

    /**
     * The root node of an empty tree.
     */
    public final static ImmutableListNode LIST_NIL = new ImmutableListNode();

    protected final int level;
    protected final ImmutableListNode leftNode;
    protected final ImmutableListNode rightNode;
    protected final Object value;
    protected final long created;
    protected final long deleted;
    protected final int size;

    protected ImmutableListNode() {
        level = 0;
        leftNode = this;
        rightNode = this;
        value = null;
        created = 0L;
        deleted = 0L;
        size = 0;
    }

    protected ImmutableListNode(int level,
                                ImmutableListNode leftNode,
                                ImmutableListNode rightNode,
                                Object value,
                                long created,
                                long deleted) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.value = value;
        this.created = created;
        this.deleted = deleted;
        size = leftNode.size + rightNode.size + 1;
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
     * @param time    The time of the query.
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
        ImmutableListNode n = getListNode(ndx);
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

    protected ImmutableListNode getListNode(int ndx) {
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
     * But after calling add, a previously created accessor becomes invalid.
     *
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor() {
        return listAccessor(null, MAX_TIME);
    }

    /**
     * Returns a list accessor for the latest time.
     * But after calling add, a previously created accessor becomes invalid.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor(Comparable key) {
        return listAccessor(key, MAX_TIME);
    }

    /**
     * Returns a list accessor for the given time.
     * But after calling add, a previously created accessor becomes invalid.
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
                return ImmutableListNode.this.size(time);
            }

            @Override
            public Object get(int ndx) {
                return ImmutableListNode.this.get(ndx, time);
            }

            @Override
            public int getIndex(Object value) {
                return ImmutableListNode.this.getIndex(value, time);
            }

            @Override
            public int getIndexRight(Object value) {
                return ImmutableListNode.this.getIndexRight(value, time);
            }

            @Override
            public int findIndex(Object value) {
                return ImmutableListNode.this.findIndex(value, time);
            }

            @Override
            public int findIndexRight(Object value) {
                return ImmutableListNode.this.findIndexRight(value, time);
            }

            @Override
            public int higherIndex(int ndx) {
                return ImmutableListNode.this.higherIndex(ndx, time);
            }

            @Override
            public int ceilingIndex(int ndx) {
                return ImmutableListNode.this.ceilingIndex(ndx, time);
            }

            @Override
            public int firstIndex() {
                return ImmutableListNode.this.firstIndex(time);
            }

            @Override
            public int lowerIndex(int ndx) {
                return ImmutableListNode.this.lowerIndex(ndx, time);
            }

            @Override
            public int floorIndex(int ndx) {
                return ImmutableListNode.this.floorIndex(ndx, time);
            }

            @Override
            public int lastIndex() {
                return ImmutableListNode.this.lastIndex(time);
            }

            @Override
            public boolean isEmpty() {
                return ImmutableListNode.this.isEmpty(time);
            }

            @Override
            public List flatList() {
                return ImmutableListNode.this.flatList(time);
            }

            @Override
            public Iterator iterator() {
                return ImmutableListNode.this.iterator(time);
            }
        };
    }
}

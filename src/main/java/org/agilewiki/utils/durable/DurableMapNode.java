package org.agilewiki.utils.durable;

import org.agilewiki.utils.maplist.ListAccessor;
import org.agilewiki.utils.maplist.MapAccessor;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * An immutable map of versioned lists.
 */
public class DurableMapNode {
    public final static char MAP_NODE_ID = 'm';
    public final static char MAP_NIL_ID = '2';

    /**
     * The root node of an empty tree.
     */
    public final static DurableMapNode MAP_NIL = new DurableMapNode();

    protected final int level;
    protected final DurableMapNode leftNode;
    protected final DurableMapNode rightNode;
    protected final DurableListNode listNode;
    protected final Comparable key;
    protected final DurableFactory keyFactory;
    protected final int durableLength;

    protected DurableMapNode() {
        level = 0;
        leftNode = this;
        rightNode = this;
        listNode = DurableListNode.LIST_NIL;
        key = null;
        keyFactory = null;
        durableLength = 2;
    }

    protected DurableMapNode(int level,
                             DurableMapNode leftNode,
                             DurableMapNode rightNode,
                             DurableListNode listNode,
                             Comparable key) {
        this(
                level,
                leftNode,
                rightNode,
                listNode,
                key,
                FactoryRegistry.getDurableFactory(key));
    }

    protected DurableMapNode(int level,
                             DurableMapNode leftNode,
                             DurableMapNode rightNode,
                             DurableListNode listNode,
                             Comparable key, DurableFactory keyFactory) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.listNode = listNode;
        this.key = key;
        this.keyFactory = keyFactory;
        durableLength = 6 +
                keyFactory.getDurableLength(key) +
                leftNode.getDurableLength() +
                listNode.getDurableLength() +
                rightNode.getDurableLength();
    }

    protected boolean isNil() {
        return this == MAP_NIL;
    }

    protected DurableListNode getList(Comparable key) {
        if (isNil())
            return listNode;
        int c = key.compareTo(this.key);
        if (c < 0)
            return leftNode.getList(key);
        if (c == 0)
            return listNode;
        return rightNode.getList(key);
    }

    /**
     * Returns the count of all the values in the list,
     * including deleted values.
     *
     * @param key The list identifier.
     * @return The count of all the values in the list.
     */
    public int totalSize(Comparable key) {
        return getList(key).totalSize();
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor(Comparable key) {
        return getList(key).listAccessor(key);
    }

    /**
     * Returns a list accessor for the given time.
     *
     * @param key  The key for the list.
     * @param time The time of the query.
     * @return A list accessor for the given time.
     */
    public ListAccessor listAccessor(Comparable key, long time) {
        return getList(key).listAccessor(key, time);
    }

    protected DurableMapNode skew() {
        if (isNil())
            return this;
        if (leftNode.isNil())
            return this;
        if (leftNode.level == level) {
            DurableMapNode t = new DurableMapNode(
                    level,
                    leftNode.rightNode,
                    rightNode,
                    listNode,
                    key);
            DurableMapNode l = new DurableMapNode(
                    leftNode.level,
                    leftNode.leftNode,
                    t,
                    leftNode.listNode,
                    leftNode.key);
            return l;
        } else
            return this;
    }

    protected DurableMapNode split() {
        if (isNil())
            return this;
        if (rightNode.isNil() || rightNode.rightNode.isNil())
            return this;
        if (level == rightNode.rightNode.level) {
            DurableMapNode t = new DurableMapNode(
                    level,
                    leftNode,
                    rightNode.leftNode,
                    listNode,
                    key);
            DurableMapNode r = new DurableMapNode(
                    rightNode.level + 1,
                    t,
                    rightNode.rightNode,
                    rightNode.listNode,
                    rightNode.key);
            return r;
        }
        return this;
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param key   The key of the list.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public DurableMapNode add(Comparable key, Object value, long time) {
        return add(key, -1, value, time);
    }

    /**
     * Add a non-null value to the list.
     *
     * @param key   The key of the list.
     * @param ndx   Where to add the value.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public DurableMapNode add(Comparable key, int ndx, Object value, long time) {
        return add(key, ndx, value, time, Integer.MAX_VALUE);
    }

    protected DurableMapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil()) {
            DurableListNode listNode = DurableListNode.LIST_NIL.add(ndx, value, created, deleted);
            return new DurableMapNode(1, MAP_NIL, MAP_NIL, listNode, key);
        }
        DurableMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new DurableMapNode(
                    level,
                    leftNode.add(key, ndx, value, created, deleted),
                    rightNode,
                    listNode,
                    key);
        } else if (c == 0) {
            return new DurableMapNode(
                    level,
                    leftNode,
                    rightNode,
                    listNode.add(ndx, value, created, deleted),
                    key);
        } else {
            t = new DurableMapNode(
                    level,
                    leftNode,
                    rightNode.add(key, ndx, value, created, deleted),
                    listNode,
                    key);
        }
        return t.skew().split();
    }

    /**
     * Mark a value as deleted.
     *
     * @param key  The key of the list.
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public DurableMapNode remove(Comparable key, int ndx, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return this;
        int c = key.compareTo(this.key);
        if (c < 0) {
            DurableMapNode n = leftNode.remove(key, ndx, time);
            if (n == leftNode)
                return this;
            return new DurableMapNode(level, n, rightNode, listNode, key);
        } else if (c == 0) {
            DurableListNode n = listNode.remove(ndx, time);
            if (n == listNode)
                return this;
            return new DurableMapNode(level, leftNode, rightNode, n, key);
        } else {
            DurableMapNode n = rightNode.remove(key, ndx, time);
            if (n == rightNode)
                return this;
            return new DurableMapNode(level, leftNode, n, listNode, key);
        }
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public DurableMapNode clearList(Comparable key, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return this;
        int c = key.compareTo(this.key);
        if (c < 0) {
            DurableMapNode n = leftNode.clearList(key, time);
            if (n == leftNode)
                return this;
            return new DurableMapNode(level, n, rightNode, listNode, key);
        } else if (c == 0) {
            DurableListNode n = listNode.clearList(time);
            if (n == listNode)
                return this;
            return new DurableMapNode(level, leftNode, rightNode, n, key);
        } else {
            DurableMapNode n = rightNode.clearList(key, time);
            if (n == rightNode)
                return this;
            return new DurableMapNode(level, leftNode, n, listNode, key);
        }
    }

    public DurableMapNode set(Comparable key, Object value, long time) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            DurableListNode listNode = DurableListNode.LIST_NIL.add(value, time);
            return new DurableMapNode(1, MAP_NIL, MAP_NIL, listNode, key);
        }
        int c = key.compareTo(this.key);
        if (c < 0) {
            DurableMapNode n = leftNode.set(key, value, time);
            return new DurableMapNode(level, n, rightNode, listNode, key);
        } else if (c == 0) {
            DurableListNode n = listNode.clearList(time);
            n = n.add(value, time);
            return new DurableMapNode(level, leftNode, rightNode, n, key);
        } else {
            DurableMapNode n = rightNode.set(key, value, time);
            return new DurableMapNode(level, leftNode, n, listNode, key);
        }
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned map.
     */
    public DurableMapNode clearMap(long time) {
        if (isNil())
            return this;
        DurableMapNode ln = leftNode.clearMap(time);
        DurableMapNode rn = rightNode.clearMap(time);
        if (ln == leftNode && rn == rightNode && listNode.isEmpty(time))
            return this;
        return new DurableMapNode(level,
                ln,
                rn,
                listNode.clearList(time),
                key);
    }

    /**
     * Perform a complete list copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public DurableListNode copyList(Comparable key) {
        return getList(key).copyList();
    }

    /**
     * Copy everything in the list except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public DurableListNode copyList(Comparable key, long time) {
        return getList(key).copyList(time);
    }

    protected void flatKeys(NavigableSet<Comparable> keys, long time) {
        if (isNil())
            return;
        leftNode.flatKeys(keys, time);
        if (!listNode.isEmpty(time))
            keys.add(key);
        rightNode.flatKeys(keys, time);
    }

    /**
     * Returns a set of all keys with non-empty lists for the given time.
     *
     * @param time The time of the query.
     * @return A set of the keys with content at the time of the query.
     */
    public NavigableSet<Comparable> flatKeys(long time) {
        NavigableSet keys = new TreeSet<>();
        flatKeys(keys, time);
        return keys;
    }

    protected void flatMap(NavigableMap<Comparable, List> map, long time) {
        if (isNil())
            return;
        leftNode.flatMap(map, time);
        if (!listNode.isEmpty(time))
            map.put(key, listNode.flatList(time));
        rightNode.flatMap(map, time);
    }

    /**
     * Returns a map of all the keys and values present at the given time.
     *
     * @param time The time of the query.
     * @return A map of lists.
     */
    public NavigableMap<Comparable, List> flatMap(long time) {
        NavigableMap<Comparable, List> map = new TreeMap<Comparable, List>();
        flatMap(map, time);
        return map;
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public DurableMapNode copyMap() {
        return copyMap(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the lists are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the map without some historical values.
     */
    public DurableMapNode copyMap(long time) {
        return copyMap(MAP_NIL, time);
    }

    protected DurableMapNode copyMap(DurableMapNode n, long time) {
        if (isNil())
            return n;
        n = leftNode.copyMap(n, time);
        n = n.addList(key, listNode.copyList(time));
        return leftNode.copyMap(n, time);
    }

    protected DurableMapNode addList(Comparable key, DurableListNode listNode) {
        if (listNode.isNil())
            return this;
        if (isNil()) {
            return new DurableMapNode(1, MAP_NIL, MAP_NIL, listNode, key);
        }
        DurableMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new DurableMapNode(
                    level,
                    leftNode.addList(key, listNode),
                    rightNode,
                    listNode,
                    key);
        } else if (c == 0) {
            throw new IllegalArgumentException("duplicate key not supported");
        } else {
            t = new DurableMapNode(
                    level,
                    leftNode,
                    rightNode.addList(key, listNode),
                    listNode,
                    key);
        }
        return t.skew().split();
    }

    /**
     * Returns the count of all the keys in the map, empty or not.
     *
     * @return The count of all the keys in the map.
     */
    public int totalSize() {
        if (isNil())
            return 0;
        return leftNode.totalSize() + 1 + rightNode.totalSize();
    }

    /**
     * Returns the count of all the keys with a non-empty list.
     *
     * @param time The time of the query.
     * @return The current size of the map.
     */
    public int size(long time) {
        if (isNil())
            return 0;
        int s = leftNode.size(time) + rightNode.size(time);
        if (!listNode.isEmpty(time))
            s += 1;
        return s;
    }

    /**
     * Returns the smallest key of the non-empty lists for the given time.
     *
     * @param time The time of the query.
     * @return The smallest key, or null.
     */
    public Comparable firstKey(long time) {
        if (isNil())
            return null;
        Comparable k = leftNode.firstKey(time);
        if (k != null)
            return k;
        if (!listNode.isEmpty(time))
            return key;
        return rightNode.firstKey(time);
    }

    /**
     * Returns the largest key of the non-empty lists for the given time.
     *
     * @param time The time of the query.
     * @return The largest key, or null.
     */
    public Comparable lastKey(long time) {
        if (isNil())
            return null;
        Comparable k = rightNode.lastKey(time);
        if (k != null)
            return k;
        if (!listNode.isEmpty(time))
            return key;
        return leftNode.lastKey(time);
    }

    /**
     * Returns the next greater key.
     *
     * @param key  The given key.
     * @param time The time of the query.
     * @return The next greater key with content at the time of the query, or null.
     */
    public Comparable higherKey(Comparable key, long time) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c <= 0) {
            Comparable k = leftNode.higherKey(key, time);
            if (k != null)
                return k;
        }
        if (c < 0 && !listNode.isEmpty(time))
            return this.key;
        return rightNode.higherKey(key, time);
    }

    /**
     * Returns the key with content that is greater than or equal to the given key.
     *
     * @param key  The given key.
     * @param time The time of the query.
     * @return The key greater than or equal to the given key, or null.
     */
    public Comparable ceilingKey(Comparable key, long time) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c < 0) {
            Comparable k = leftNode.ceilingKey(key, time);
            if (k != null)
                return k;
        }
        if (c <= 0 && !listNode.isEmpty(time))
            return this.key;
        return rightNode.ceilingKey(key, time);
    }

    /**
     * Returns the next smaller key.
     *
     * @param key  The given key.
     * @param time The time of the query.
     * @return The next smaller key with content at the time of the query, or null.
     */
    public Comparable lowerKey(Comparable key, long time) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c >= 0) {
            Comparable k = rightNode.lowerKey(key, time);
            if (k != null)
                return k;
        }
        if (c > 0 && !listNode.isEmpty(time))
            return this.key;
        return leftNode.lowerKey(key, time);
    }

    /**
     * Returns the key with content that is smaller than or equal to the given key.
     *
     * @param key  The given key.
     * @param time The time of the query.
     * @return The key smaller than or equal to the given key, or null.
     */
    public Comparable floorKey(Comparable key, long time) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c > 0) {
            Comparable k = rightNode.floorKey(key, time);
            if (k != null)
                return k;
        }
        if (c >= 0 && !listNode.isEmpty(time))
            return this.key;
        return leftNode.floorKey(key, time);
    }

    /**
     * Returns an iterator over the non-empty list accessors.
     *
     * @param time The time of the query.
     * @return The iterator.
     */
    public Iterator<ListAccessor> iterator(long time) {
        return new Iterator<ListAccessor>() {
            Comparable last = null;

            @Override
            public boolean hasNext() {
                if (last == null)
                    return firstKey(time) != null;
                return higherKey(last, time) != null;
            }

            @Override
            public ListAccessor next() {
                Comparable next = last == null ? firstKey(time) : higherKey(last, time);
                if (next == null)
                    throw new NoSuchElementException();
                last = next;
                return listAccessor(last, time);
            }
        };
    }

    /**
     * Returns a map accessor for the latest time.
     * But after calling add, a previously created accessor becomes invalid.
     *
     * @return A map accessor for the latest time.
     */
    public MapAccessor mapAccessor() {
        return mapAccessor(DurableListNode.MAX_TIME);
    }

    /**
     * Returns a map accessor for a given time.
     *
     * @param time The time of the query.
     * @return A map accessor for the given time.
     */
    public MapAccessor mapAccessor(long time) {
        return new MapAccessor() {

            @Override
            public long time() {
                return time;
            }

            @Override
            public int size() {
                return DurableMapNode.this.size(time);
            }

            @Override
            public ListAccessor listAccessor(Comparable key) {
                return DurableMapNode.this.listAccessor(key, time);
            }

            @Override
            public NavigableSet<Comparable> flatKeys() {
                return DurableMapNode.this.flatKeys(time);
            }

            @Override
            public Comparable firstKey() {
                return DurableMapNode.this.firstKey(time);
            }

            @Override
            public Comparable lastKey() {
                return DurableMapNode.this.lastKey(time);
            }

            @Override
            public Comparable higherKey(Comparable key) {
                return DurableMapNode.this.higherKey(key, time);
            }

            @Override
            public Comparable ceilingKey(Comparable key) {
                return DurableMapNode.this.ceilingKey(key, time);
            }

            @Override
            public Comparable lowerKey(Comparable key) {
                return DurableMapNode.this.lowerKey(key, time);
            }

            @Override
            public Comparable floorKey(Comparable key) {
                return DurableMapNode.this.floorKey(key, time);
            }

            @Override
            public Iterator<ListAccessor> iterator() {
                return DurableMapNode.this.iterator(time);
            }

            @Override
            public NavigableMap<Comparable, List> flatMap() {
                return DurableMapNode.this.flatMap(time);
            }
        };
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
            byteBuffer.putChar(MAP_NIL_ID);
            return;
        }
        byteBuffer.putChar(MAP_NODE_ID);
        serialize(byteBuffer);
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer    Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer) {
        byteBuffer.putInt(level);
        keyFactory.writeDurable(key, byteBuffer);
        leftNode.writeDurable(byteBuffer);
        listNode.writeDurable(byteBuffer);
        rightNode.writeDurable(byteBuffer);
    }
}

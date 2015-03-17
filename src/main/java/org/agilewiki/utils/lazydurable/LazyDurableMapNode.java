package org.agilewiki.utils.lazydurable;

import org.agilewiki.utils.maplist.ListAccessor;
import org.agilewiki.utils.maplist.MapAccessor;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable map of versioned lists.
 */
public class LazyDurableMapNode {
    public final static char MAP_NODE_ID = 'm';
    public final static char MAP_NIL_ID = '2';

    /**
     * The root node of an empty tree.
     */
    public final static LazyDurableMapNode MAP_NIL = new LazyDurableMapNode();

    protected final AtomicReference<DurableMapNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected LazyDurableMapNode() {
        dataReference.set(new DurableMapNodeData(this));
        durableLength = 2;
    }

    protected LazyDurableMapNode(ByteBuffer byteBuffer) {
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected LazyDurableMapNode(int level,
                                  LazyDurableMapNode leftNode,
                                  LazyDurableListNode listNode,
                                  LazyDurableMapNode rightNode,
                                  Comparable key) {
        DurableMapNodeData data = new DurableMapNodeData(
                this,
                level,
                leftNode,
                listNode,
                rightNode,
                key);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    protected DurableMapNodeData getData() {
        DurableMapNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new DurableMapNodeData(this, byteBuffer.slice()));
        return dataReference.get();
    }

    protected boolean isNil() {
        return this == MAP_NIL;
    }

    protected LazyDurableListNode getList(Comparable key) {
        if (isNil())
            return LazyDurableListNode.LIST_NIL;
        return getData().getList(key);
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

    /**
     * Add a non-null value to the end of the list.
     *
     * @param key   The key of the list.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public LazyDurableMapNode add(Comparable key, Object value, long time) {
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
    public LazyDurableMapNode add(Comparable key, int ndx, Object value, long time) {
        return add(key, ndx, value, time, Integer.MAX_VALUE);
    }

    protected LazyDurableMapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil()) {
            LazyDurableListNode listNode = LazyDurableListNode.LIST_NIL.add(ndx, value, created, deleted);
            return new LazyDurableMapNode(1, MAP_NIL, listNode, MAP_NIL, key);
        }
        return getData().add(key, ndx, value, created, deleted);
    }

    /**
     * Mark a value as deleted.
     *
     * @param key  The key of the list.
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public LazyDurableMapNode remove(Comparable key, int ndx, long time) {
        if (isNil())
            return this;
        return getData().remove(key, ndx, time);
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public LazyDurableMapNode clearList(Comparable key, long time) {
        if (isNil())
            return this;
        return getData().clearList(key, time);
    }

    /**
     * Replace the list entries with a single value.
     *
     * @param key      The key of the list.
     * @param value    The new value.
     * @param time     The time of the replacement.
     * @return The revised node.
     */
    public LazyDurableMapNode set(Comparable key, Object value, long time) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            LazyDurableListNode listNode = LazyDurableListNode.LIST_NIL.add(value, time);
            return new LazyDurableMapNode(1, MAP_NIL, listNode, MAP_NIL, key);
        }
        return getData().set(key, value, time);
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned map.
     */
    public LazyDurableMapNode clearMap(long time) {
        if (isNil())
            return this;
        return getData().clearMap(time);
    }

    /**
     * Perform a complete list copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public LazyDurableListNode copyList(Comparable key) {
        return getList(key).copyList();
    }

    /**
     * Copy everything in the list except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public LazyDurableListNode copyList(Comparable key, long time) {
        return getList(key).copyList(time);
    }

    /**
     * Returns a set of all keys with non-empty lists for the given time.
     *
     * @param time The time of the query.
     * @return A set of the keys with content at the time of the query.
     */
    public NavigableSet<Comparable> flatKeys(long time) {
        NavigableSet keys = new TreeSet<>();
        getData().flatKeys(keys, time);
        return keys;
    }

    /**
     * Returns a map of all the keys and values present at the given time.
     *
     * @param time The time of the query.
     * @return A map of lists.
     */
    public NavigableMap<Comparable, List> flatMap(long time) {
        NavigableMap<Comparable, List> map = new TreeMap<Comparable, List>();
        getData().flatMap(map, time);
        return map;
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public LazyDurableMapNode copyMap() {
        return copyMap(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the lists are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the map without some historical values.
     */
    public LazyDurableMapNode copyMap(long time) {
        return getData().copyMap(MAP_NIL, time);
    }

    /**
     * Returns the count of all the keys in the map, empty or not.
     *
     * @return The count of all the keys in the map.
     */
    public int totalSize() {
        if (isNil())
            return 0;
        return getData().totalSize();
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
        return getData().size(time);
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
        return getData().firstKey(time);
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
        return getData().lastKey(time);
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
        return getData().higherKey(key, time);
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
        return getData().ceilingKey(key, time);
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
        return getData().lowerKey(key, time);
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
        return getData().floorKey(key, time);
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
        return mapAccessor(LazyDurableListNode.MAX_TIME);
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
                return LazyDurableMapNode.this.size(time);
            }

            @Override
            public ListAccessor listAccessor(Comparable key) {
                return LazyDurableMapNode.this.listAccessor(key, time);
            }

            @Override
            public NavigableSet<Comparable> flatKeys() {
                return LazyDurableMapNode.this.flatKeys(time);
            }

            @Override
            public Comparable firstKey() {
                return LazyDurableMapNode.this.firstKey(time);
            }

            @Override
            public Comparable lastKey() {
                return LazyDurableMapNode.this.lastKey(time);
            }

            @Override
            public Comparable higherKey(Comparable key) {
                return LazyDurableMapNode.this.higherKey(key, time);
            }

            @Override
            public Comparable ceilingKey(Comparable key) {
                return LazyDurableMapNode.this.ceilingKey(key, time);
            }

            @Override
            public Comparable lowerKey(Comparable key) {
                return LazyDurableMapNode.this.lowerKey(key, time);
            }

            @Override
            public Comparable floorKey(Comparable key) {
                return LazyDurableMapNode.this.floorKey(key, time);
            }

            @Override
            public Iterator<ListAccessor> iterator() {
                return LazyDurableMapNode.this.iterator(time);
            }

            @Override
            public NavigableMap<Comparable, List> flatMap() {
                return LazyDurableMapNode.this.flatMap(time);
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
        byteBuffer.putInt(getDurableLength());
        if (this.byteBuffer == null) {
            getData().serialize(byteBuffer);
            return;
        }
        byteBuffer.put(this.byteBuffer.slice());
        ByteBuffer bb = byteBuffer.slice();
        bb.limit(durableLength - 6);
        this.byteBuffer = bb;
        dataReference.set(null); //limit memory footprint, plugs memory leak.
    }
}

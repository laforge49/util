package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * An immutable map of lists.
 */
public interface MapNode extends Releasable {

    /**
     * Returns the database factory registry.
     *
     * @return The registry.
     */
    DbFactoryRegistry getRegistry();

    /**
     * Returns the database.
     *
     * @return The database.
     */
    default Db getDb() {
        return getRegistry().db;
    }

    /**
     * Returns the current timestamp, a unique
     * identifier for the current transaction.
     *
     * @return The current transaction's timestamp
     */
    default long getTimestamp() {
        return getDb().getTimestamp();
    }

    MapNodeData getData();

    default boolean isNil() {
        return this == getRegistry().nilMap;
    }

    default ListNode getList(Comparable key) {
        if (isNil())
            return getRegistry().nilList;
        return getData().getList(key);
    }

    /**
     * Returns the count of all the values in the list,
     * including deleted values.
     *
     * @param key The list identifier.
     * @return The count of all the values in the list.
     */
    default int totalSize(Comparable key) {
        return getList(key).totalSize();
    }

    /**
     * Returns a list accessor.
     *
     * @param key The key for the list.
     * @return A list accessor.
     */
    default ListAccessor listAccessor(Comparable key) {
        return getList(key).listAccessor(key);
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param key   The key of the list.
     * @param value The value to be added.
     * @return The revised root node.
     */
    default MapNode add(Comparable key, Object value) {
        return add(key, -1, value);
    }

    /**
     * Add a non-null value to the list.
     *
     * @param key   The key of the list.
     * @param ndx   Where to add the value.
     * @param value The value to be added.
     * @return The revised root node.
     */
    default MapNode add(Comparable key, int ndx, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key may not be null");
        }
        if (isNil()) {
            ListNode listNode = getRegistry().nilList.add(ndx, value);
            return getData().replace(1, listNode, key);
        }
        return getData().add(key, ndx, value);
    }

    /**
     * Remove a value from the list.
     *
     * @param key The key of the list.
     * @param ndx The index of the value.
     * @return The revised node.
     */
    default MapNode remove(Comparable key, int ndx) {
        if (isNil())
            return this;
        return getData().remove(key, ndx);
    }

    /**
     * Delete the list.
     *
     * @param key The key of the list.
     * @return The revised node.
     */
    default MapNode remove(Comparable key) {
        if (isNil())
            return this;
        return getData().remove(key);
    }

    /**
     * Replace the list entries with a single value.
     *
     * @param key   The key of the list.
     * @param value The new value.
     * @return The revised node.
     */
    default MapNode set(Comparable key, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        if (isNil()) {
            ListNode listNode = getRegistry().nilList.add(value);
            return getData().replace(1, listNode, key);
        }
        return getData().set(key, value);
    }

    /**
     * Returns a set of all keys with non-empty lists.
     *
     * @return A set of the keys.
     */

    default NavigableSet flatKeys() {
        NavigableSet keys = new TreeSet<>();
        getData().flatKeys(keys);
        return keys;
    }

    /**
     * Returns a map of all the keys and values.
     *
     * @return A map of lists.
     */
    default NavigableMap<Comparable, List> flatMap() {
        NavigableMap<Comparable, List> map = new TreeMap<Comparable, List>();
        getData().flatMap(map);
        return map;
    }

    /**
     * Returns the count of all the keys in the map.
     *
     * @return The count of all the keys in the map.
     */
    default int totalSize() {
        if (isNil())
            return 0;
        return getData().totalSize();
    }

    /**
     * Returns the count of all the keys.
     *
     * @return The current size of the map.
     */
    default int size() {
        if (isNil())
            return 0;
        return getData().size();
    }

    /**
     * Returns the smallest key.
     *
     * @return The smallest key, or null.
     */
    default Comparable firstKey() {
        if (isNil())
            return null;
        return getData().firstKey();
    }

    /**
     * Returns the largest key.
     *
     * @return The largest key, or null.
     */
    default Comparable lastKey() {
        if (isNil())
            return null;
        return getData().lastKey();
    }

    /**
     * Returns the next greater key.
     *
     * @param key The given key.
     * @return The next greater key, or null.
     */
    default Comparable higherKey(Comparable key) {
        if (isNil())
            return null;
        return getData().higherKey(key);
    }

    /**
     * Returns the key that is greater than or equal to the given key.
     *
     * @param key The given key.
     * @return The key greater than or equal to the given key, or null.
     */
    default Comparable ceilingKey(Comparable key) {
        if (isNil())
            return null;
        return getData().ceilingKey(key);
    }

    /**
     * Returns the next smaller key.
     *
     * @param key The given key.
     * @return The next smaller key, or null.
     */
    default Comparable lowerKey(Comparable key) {
        if (isNil())
            return null;
        return getData().lowerKey(key);
    }

    /**
     * Returns the key that is smaller than or equal to the given key.
     *
     * @param key The given key.
     * @return The key smaller than or equal to the given key, or null.
     */
    default Comparable floorKey(Comparable key) {
        if (isNil())
            return null;
        return getData().floorKey(key);
    }

    /**
     * Returns an iterator over the list accessors.
     *
     * @return The iterator.
     */
    default Iterator<ListAccessor> iterator() {
        return new Iterator<ListAccessor>() {
            Comparable last = null;

            @Override
            public boolean hasNext() {
                if (last == null)
                    return firstKey() != null;
                return higherKey(last) != null;
            }

            @Override
            public ListAccessor next() {
                Comparable next = last == null ? firstKey() : higherKey(last);
                if (next == null)
                    throw new NoSuchElementException();
                last = next;
                return listAccessor(last);
            }
        };
    }

    /**
     * Returns a map accessor.
     *
     * @return A map accessor.
     */
    default MapAccessor mapAccessor() {
        return new MapAccessor() {

            @Override
            public long time() {
                return FactoryRegistry.MAX_TIME;
            }

            @Override
            public int size() {
                return MapNode.this.size();
            }

            @Override
            public ListAccessor listAccessor(Comparable key) {
                return MapNode.this.listAccessor(key);
            }

            @Override
            public NavigableSet<Comparable> flatKeys() {
                return MapNode.this.flatKeys();
            }

            @Override
            public Comparable firstKey() {
                return MapNode.this.firstKey();
            }

            @Override
            public Comparable lastKey() {
                return MapNode.this.lastKey();
            }

            @Override
            public Comparable higherKey(Comparable key) {
                return MapNode.this.higherKey(key);
            }

            @Override
            public Comparable ceilingKey(Comparable key) {
                return MapNode.this.ceilingKey(key);
            }

            @Override
            public Comparable lowerKey(Comparable key) {
                return MapNode.this.lowerKey(key);
            }

            @Override
            public Comparable floorKey(Comparable key) {
                return MapNode.this.floorKey(key);
            }

            @Override
            public Iterator<ListAccessor> iterator() {
                return MapNode.this.iterator();
            }

            @Override
            public NavigableMap<Comparable, List> flatMap() {
                return MapNode.this.flatMap();
            }
        };
    }

    /**
     * Returns the size of a byte array needed to serialize this object,
     * including the space needed for the durable id.
     *
     * @return The size in bytes of the serialized data.
     */
    int getDurableLength();

    /**
     * Write the durable to a byte buffer.
     *
     * @param byteBuffer The byte buffer.
     */
    void writeDurable(ByteBuffer byteBuffer);

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer);

    @Override
    default void releaseAll() {
        getData().releaseAll();
    }

    @Override
    default Object resize(int maxSize, int maxBlockSize) {
        return getData().resize(maxSize, maxBlockSize);
    }

    /**
     * Returns a ByteBuffer loaded with the serialized contents of the immutable.
     *
     * @return The loaded ByteBuffer.
     */
    default ByteBuffer toByteBuffer() {
        ImmutableFactory factory = getRegistry().getImmutableFactory(this);
        return factory.toByteBuffer(this);
    }
}

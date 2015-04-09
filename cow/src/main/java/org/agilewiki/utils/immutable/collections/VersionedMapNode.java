package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * An immutable map of versioned lists.
 */
public interface VersionedMapNode extends Releasable {

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

    VersionedMapNodeData getData();

    default boolean isNil() {
        return this == getRegistry().versionedNilMap;
    }

    /**
     * Returns the list for the node.
     *
     * @param key The key for the node.
     * @return The list, or null.
     */
    default VersionedListNode getList(Comparable key) {
        if (isNil())
            return getRegistry().versionedNilList;
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
     * Returns a list accessor for the latest time.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    default ListAccessor listAccessor(Comparable key) {
        return getList(key).listAccessor(key);
    }

    /**
     * Returns a list accessor for the given time.
     *
     * @param key  The key for the list.
     * @param timestamp The time of the query.
     * @return A list accessor for the given time.
     */
    default ListAccessor listAccessor(Comparable key, long timestamp) {
        return getList(key).listAccessor(key, timestamp);
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param key   The key of the list.
     * @param value The value to be added.
     * @return The revised root node.
     */
    default VersionedMapNode add(Comparable key, Object value) {
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
    default VersionedMapNode add(Comparable key, int ndx, Object value) {
        return add(key, ndx, value, getTimestamp(), Long.MAX_VALUE);
    }

    default VersionedMapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil()) {
            DbFactoryRegistry registry = getRegistry();
            VersionedListNode listNode = registry.versionedNilList.add(ndx, value, created, deleted);
            return getData().replace(1, listNode, key);
        }
        return getData().add(key, ndx, value, created, deleted);
    }

    /**
     * Mark a value as deleted.
     *
     * @param key  The key of the list.
     * @param ndx  The index of the value.
     * @return The revised node.
     */
    default VersionedMapNode remove(Comparable key, int ndx) {
        if (isNil())
            return this;
        return getData().remove(key, ndx);
    }

    /**
     * Remove the first occurance of a value from a list.
     *
     * @param key The key of the list.
     * @param x    The value to be removed.
     * @return The updated root.
     */
    default VersionedMapNode remove(Comparable key, Object x) {
        if (isNil())
            return this;
        VersionedListNode ln = getList(key);
        if (ln == null)
            return this;
        return set(key, ln.remove(x));
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @return The revised node.
     */
    default VersionedMapNode clearList(Comparable key) {
        if (isNil())
            return this;
        return getData().clearList(key);
    }

    /**
     * Replace the list entries with a single value.
     *
     * @param key   The key of the list.
     * @param value The new value.
     * @return The revised node.
     */
    default VersionedMapNode set(Comparable key, Object value) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            DbFactoryRegistry registry = getRegistry();
            VersionedListNode listNode = registry.versionedNilList.add(value);
            return getData().replace(1, listNode, key);
        }
        return getData().set(key, value);
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @return The currently empty versioned map.
     */
    default VersionedMapNode clearMap() {
        if (isNil())
            return this;
        return getData().clearMap();
    }

    /**
     * Perform a complete list copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    default VersionedListNode copyList(Comparable key) {
        return getList(key).copyList();
    }

    /**
     * Copy everything in the list except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param timestamp The given time.
     * @return A shortened copy of the list without some historical values.
     */
    default VersionedListNode copyList(Comparable key, long timestamp) {
        return getList(key).copyList(timestamp);
    }

    /**
     * Returns a set of all keys with non-empty lists for the given time.
     *
     * @param timestamp The time of the query.
     * @return A set of the keys with content at the time of the query.
     */
    default NavigableSet<Comparable> flatKeys(long timestamp) {
        NavigableSet keys = new TreeSet<>();
        getData().flatKeys(keys, timestamp);
        return keys;
    }

    /**
     * Returns a map of all the keys and values present at the given time.
     *
     * @param timestamp The time of the query.
     * @return A map of lists.
     */
    default NavigableMap<Comparable, List> flatMap(long timestamp) {
        NavigableMap<Comparable, List> map = new TreeMap<Comparable, List>();
        getData().flatMap(map, timestamp);
        return map;
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    default VersionedMapNode copyMap() {
        return copyMap(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the lists are not copied.)
     *
     * @param timestamp The given time.
     * @return A shortened copy of the map without some historical values.
     */
    default VersionedMapNode copyMap(long timestamp) {
        return getData().copyMap(getRegistry().versionedNilMap, timestamp);
    }

    /**
     * Returns the count of all the keys in the map, empty or not.
     *
     * @return The count of all the keys in the map.
     */
    default int totalSize() {
        if (isNil())
            return 0;
        return getData().totalSize();
    }

    /**
     * Returns the count of all the keys with a non-empty list.
     *
     * @param timestamp The time of the query.
     * @return The current size of the map.
     */
    default int size(long timestamp) {
        if (isNil())
            return 0;
        return getData().size(timestamp);
    }

    /**
     * Returns the smallest key of the non-empty lists for the given time.
     *
     * @param timestamp The time of the query.
     * @return The smallest key, or null.
     */
    default Comparable firstKey(long timestamp) {
        if (isNil())
            return null;
        return getData().firstKey(timestamp);
    }

    /**
     * Returns the largest key of the non-empty lists for the given time.
     *
     * @param timestamp The time of the query.
     * @return The largest key, or null.
     */
    default Comparable lastKey(long timestamp) {
        if (isNil())
            return null;
        return getData().lastKey(timestamp);
    }

    /**
     * Returns the next greater key.
     *
     * @param key  The given key.
     * @param timestamp The time of the query.
     * @return The next greater key with content at the time of the query, or null.
     */
    default Comparable higherKey(Comparable key, long timestamp) {
        if (isNil())
            return null;
        return getData().higherKey(key, timestamp);
    }

    /**
     * Returns the key with content that is greater than or equal to the given key.
     *
     * @param key  The given key.
     * @param timestamp The time of the query.
     * @return The key greater than or equal to the given key, or null.
     */
    default Comparable ceilingKey(Comparable key, long timestamp) {
        if (isNil())
            return null;
        return getData().ceilingKey(key, timestamp);
    }

    /**
     * Returns the next smaller key.
     *
     * @param key  The given key.
     * @param timestamp The time of the query.
     * @return The next smaller key with content at the time of the query, or null.
     */
    default Comparable lowerKey(Comparable key, long timestamp) {
        if (isNil())
            return null;
        return getData().lowerKey(key, timestamp);
    }

    /**
     * Returns the key with content that is smaller than or equal to the given key.
     *
     * @param key  The given key.
     * @param timestamp The time of the query.
     * @return The key smaller than or equal to the given key, or null.
     */
    default Comparable floorKey(Comparable key, long timestamp) {
        if (isNil())
            return null;
        return getData().floorKey(key, timestamp);
    }

    /**
     * Returns an iterator over the non-empty list accessors.
     *
     * @param timestamp The time of the query.
     * @return The iterator.
     */
    default Iterator<ListAccessor> iterator(long timestamp) {
        return new Iterator<ListAccessor>() {
            Comparable last = null;

            @Override
            public boolean hasNext() {
                if (last == null)
                    return firstKey(timestamp) != null;
                return higherKey(last, timestamp) != null;
            }

            @Override
            public ListAccessor next() {
                Comparable next = last == null ? firstKey(timestamp) : higherKey(last, timestamp);
                if (next == null)
                    throw new NoSuchElementException();
                last = next;
                return listAccessor(last, timestamp);
            }
        };
    }

    /**
     * Returns an iterator over the list accessors
     * with keys whose toString start with the given prefix.
     *
     * @param prefix The qualifying prefix.
     * @param timestamp The time of the query.
     * @return The iterator.
     */
    default Iterator<ListAccessor> iterator(String prefix, long timestamp) {
        return iterable(prefix, timestamp).iterator();
    }

    /**
     * Returns an iterable over the list accessors
     * with keys whose toString start with the given prefix.
     *
     * @param prefix The qualifying prefix.
     * @param timestamp The time of the query.
     * @return The iterator.
     */
    default Iterable<ListAccessor> iterable(String prefix, long timestamp) {
        return new Iterable<ListAccessor>() {
            @Override
            public Iterator<ListAccessor> iterator() {
                return new Iterator<ListAccessor>() {
                    Comparable last = null;

                    @Override
                    public boolean hasNext() {
                        if (last == null)
                            return ceilingKey(prefix, timestamp) != null;
                        Comparable hk = higherKey(last, timestamp);
                        if (hk == null)
                            return false;
                        return hk.toString().startsWith(prefix);
                    }

                    @Override
                    public ListAccessor next() {
                        Comparable next = last == null ? ceilingKey(prefix, timestamp) : higherKey(last, timestamp);
                        if (next == null || !next.toString().startsWith(prefix))
                            throw new NoSuchElementException();
                        last = next;
                        return listAccessor(last);
                    }
                };
            }
        };
    }

    /**
     * Returns a map accessor for the time of the current transaction.
     *
     * @return A map accessor for the latest time.
     */
    default MapAccessor mapAccessor() {
        return mapAccessor(getTimestamp());
    }

    /**
     * Returns a map accessor for a given time.
     *
     * @param timestamp The time of the query.
     * @return A map accessor for the given time.
     */
    default MapAccessor mapAccessor(long timestamp) {
        return new MapAccessor() {

            @Override
            public long timestamp() {
                return timestamp;
            }

            @Override
            public int size() {
                return VersionedMapNode.this.size(timestamp);
            }

            @Override
            public ListAccessor listAccessor(Comparable key) {
                return VersionedMapNode.this.listAccessor(key, timestamp);
            }

            @Override
            public NavigableSet<Comparable> flatKeys() {
                return VersionedMapNode.this.flatKeys(timestamp);
            }

            @Override
            public Comparable firstKey() {
                return VersionedMapNode.this.firstKey(timestamp);
            }

            @Override
            public Comparable lastKey() {
                return VersionedMapNode.this.lastKey(timestamp);
            }

            @Override
            public Comparable higherKey(Comparable key) {
                return VersionedMapNode.this.higherKey(key, timestamp);
            }

            @Override
            public Comparable ceilingKey(Comparable key) {
                return VersionedMapNode.this.ceilingKey(key, timestamp);
            }

            @Override
            public Comparable lowerKey(Comparable key) {
                return VersionedMapNode.this.lowerKey(key, timestamp);
            }

            @Override
            public Comparable floorKey(Comparable key) {
                return VersionedMapNode.this.floorKey(key, timestamp);
            }

            @Override
            public Iterator<ListAccessor> iterator() {
                return VersionedMapNode.this.iterator(timestamp);
            }

            @Override
            public Iterator<ListAccessor> iterator(final String prefix) {
                return VersionedMapNode.this.iterator(prefix, timestamp);
            }

            @Override
            public Iterable<ListAccessor> iterable(final String prefix) {
                return VersionedMapNode.this.iterable(prefix, timestamp);
            }

            @Override
            public NavigableMap<Comparable, List> flatMap() {
                return VersionedMapNode.this.flatMap(timestamp);
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
    void serialize(ByteBuffer byteBuffer);

    @Override
    default void releaseAll() {
        if (isNil())
            return;
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

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.virtualcow.DbFactoryRegistry;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.Releasable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An immutable versioned list.
 */
public interface VersionedListNode extends Releasable {

    DbFactoryRegistry getRegistry();

    VersionedListNodeData getData();

    /**
     * Returns the count of all the values in the list, deleted or not.
     *
     * @return The count of all the values in the list.
     */
    default int totalSize() {
        return isNil() ? 0 : getData().totalSize;
    }

    default boolean isNil() {
        return this == getRegistry().versionedNilList;
    }

    /**
     * Returns the count of all the values currently in the list.
     *
     * @param time The time of the query.
     * @return The current size of the list.
     */
    default int size(long time) {
        if (isNil())
            return 0;
        return getData().size(time);
    }

    /**
     * Returns a value if it is in range and the value exists for the given time.
     *
     * @param ndx  The index of the selected value.
     * @param time The time of the query.
     * @return A value, or null.
     */
    default Object getExistingValue(int ndx, long time) {
        VersionedListNode n = getData().getListNode(ndx);
        if (n == null)
            return null;
        return n.getData().getExistingValue(time);
    }

    /**
     * Get the index of an existing value with the same identity (==).
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    default int getIndex(Object value, long time) {
        if (isNil())
            return -1;
        return getData().getIndex(value, time);
    }

    /**
     * Get the index of an existing value with the same identity (==).
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    default int getIndexRight(Object value, long time) {
        if (isNil())
            return -1;
        return getData().getIndexRight(value, time);
    }

    /**
     * Find the index of an equal existing value.
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    default int findIndex(Object value, long time) {
        if (isNil())
            return -1;
        return getData().findIndex(value, time);
    }

    /**
     * Find the index of an equal existing value.
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @param time  The time of the query.
     * @return The index, or -1.
     */
    default int findIndexRight(Object value, long time) {
        if (isNil())
            return -1;
        return getData().findIndexRight(value, time);
    }

    /**
     * Returns the index of an existing value higher than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher, or -1.
     */
    default int higherIndex(int ndx, long time) {
        return getData().higherIndex(ndx, time);
    }

    /**
     * Returns the index of an existing value higher than or equal to the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher or equal, or -1.
     */
    default int ceilingIndex(int ndx, long time) {
        return getData().ceilingIndex(ndx, time);
    }

    /**
     * Returns the index of the first existing value in the list.
     *
     * @param time The time of the query.
     * @return The index of the first existing value in the list, or -1.
     */
    default int firstIndex(long time) {
        return ceilingIndex(0, time);
    }

    /**
     * Returns the index of an existing value lower than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is lower, or -1.
     */
    default int lowerIndex(int ndx, long time) {
        if (ndx <= 0 || isNil())
            return -1; //out of range
        return getData().lowerIndex(ndx, time);
    }

    /**
     * Returns the index of an existing value lower than or equal to the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is lower or equal, or -1.
     */
    default int floorIndex(int ndx, long time) {
        if (ndx < 0 || isNil())
            return -1; //out of range
        return getData().floorIndex(ndx, time);
    }

    /**
     * Returns the index of the last existing value in the list.
     *
     * @param time The time of the query.
     * @return The index of the last existing value in the list, or -1.
     */
    default int lastIndex(long time) {
        return floorIndex(totalSize(), time);
    }

    /**
     * Returns true if there are no values present for the given time.
     *
     * @param time The time of the query.
     * @return Returns true if the list is empty for the given time.
     */
    default boolean isEmpty(long time) {
        if (isNil())
            return true;
        return getData().isEmpty(time);
    }

    /**
     * Returns a list of all the values that are present for a given time.
     *
     * @param time The time of the query.
     * @return A list of all values present for the given time.
     */
    default List flatList(long time) {
        List list = new ArrayList<>();
        getData().flatList(list, time);
        return list;
    }

    /**
     * Returns an iterator over the existing values.
     *
     * @param time The time of the query.
     * @return The iterator.
     */
    default Iterator iterator(long time) {
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
                return getExistingValue(last, time);
            }
        };
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @return A list accessor for the latest time.
     */
    default ListAccessor listAccessor() {
        return listAccessor(null, FactoryRegistry.MAX_TIME);
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    default ListAccessor listAccessor(Comparable key) {
        return listAccessor(key, FactoryRegistry.MAX_TIME);
    }

    /**
     * Returns a list accessor for the given time.
     *
     * @param key  The key for the list.
     * @param time The time of the query.
     * @return A list accessor for the given time.
     */
    default ListAccessor listAccessor(Comparable key, long time) {
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
                return VersionedListNode.this.size(time);
            }

            @Override
            public Object get(int ndx) {
                return VersionedListNode.this.getExistingValue(ndx, time);
            }

            @Override
            public int getIndex(Object value) {
                return VersionedListNode.this.getIndex(value, time);
            }

            @Override
            public int getIndexRight(Object value) {
                return VersionedListNode.this.getIndexRight(value, time);
            }

            @Override
            public int findIndex(Object value) {
                return VersionedListNode.this.findIndex(value, time);
            }

            @Override
            public int findIndexRight(Object value) {
                return VersionedListNode.this.findIndexRight(value, time);
            }

            @Override
            public int higherIndex(int ndx) {
                return VersionedListNode.this.higherIndex(ndx, time);
            }

            @Override
            public int ceilingIndex(int ndx) {
                return VersionedListNode.this.ceilingIndex(ndx, time);
            }

            @Override
            public int firstIndex() {
                return VersionedListNode.this.firstIndex(time);
            }

            @Override
            public int lowerIndex(int ndx) {
                return VersionedListNode.this.lowerIndex(ndx, time);
            }

            @Override
            public int floorIndex(int ndx) {
                return VersionedListNode.this.floorIndex(ndx, time);
            }

            @Override
            public int lastIndex() {
                return VersionedListNode.this.lastIndex(time);
            }

            @Override
            public boolean isEmpty() {
                return VersionedListNode.this.isEmpty(time);
            }

            @Override
            public List flatList() {
                return VersionedListNode.this.flatList(time);
            }

            @Override
            public Iterator iterator() {
                return VersionedListNode.this.iterator(time);
            }
        };
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    default VersionedListNode add(Object value, long time)
            throws IOException {
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
    default VersionedListNode add(int ndx, Object value, long time)
            throws IOException {
        return add(ndx, value, time, Long.MAX_VALUE);
    }

    default VersionedListNode add(int ndx, Object value, long created, long deleted) throws IOException {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            if (ndx != 0 && ndx != -1)
                throw new IllegalArgumentException("index out of range");
            return getData().replace(
                    1,
                    1,
                    created,
                    deleted,
                    value);
        }
        return getData().add(ndx, value, created, deleted);
    }

    /**
     * Mark a value as deleted.
     *
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    default VersionedListNode remove(int ndx, long time)
            throws IOException {
        if (isNil())
            return this;
        return getData().remove(ndx, time);
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    default VersionedListNode copyList()
            throws IOException {
        return copyList(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    default VersionedListNode copyList(long time)
            throws IOException {
        return getData().copyList(getRegistry().versionedNilList, time);
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned list.
     */
    default VersionedListNode clearList(long time)
            throws IOException {
        return getData().clearList(time);
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
    default void writeDurable(ByteBuffer byteBuffer) {
        if (isNil()) {
            byteBuffer.putChar(getRegistry().versionedNilListId);
            return;
        }
        byteBuffer.putChar(getRegistry().versionedListNodeImplId);
        serialize(byteBuffer);
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    void serialize(ByteBuffer byteBuffer);

    @Override
    default void releaseAll()
            throws IOException {
        getData().releaseAll();
    }

    @Override
    default Object resize(int maxSize) throws IOException {
        return getData().resize(maxSize);
    }
}

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.Releasable;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An immutable list.
 */
public interface ListNode extends Releasable {

    DbFactoryRegistry getRegistry();

    ListNodeData getData();

    /**
     * Returns the count of all the values in the list, deleted or not.
     *
     * @return The count of all the values in the list.
     */
    default int totalSize() {
        return isNil() ? 0 : getData().totalSize;
    }

    default boolean isNil() {
        return this == getRegistry().nilList;
    }

    /**
     * Returns the count of all the values currently in the list.
     *
     * @return The current size of the list.
     */
    default int size() {
        if (isNil())
            return 0;
        return getData().size();
    }

    /**
     * Returns a value if it is in range.
     *
     * @param ndx The index of the selected value.
     * @return A value, or null.
     */
    default Object get(int ndx) {
        ListNode n = getData().getListNode(ndx);
        if (n == null)
            return null;
        return n.getData().get();
    }

    /**
     * Get the index of a value with the same identity (==).
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @return The index, or -1.
     */
    default int getIndex(Object value) {
        if (isNil())
            return -1;
        return getData().getIndex(value);
    }

    /**
     * Get the index of a value with the same identity (==).
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @return The index, or -1.
     */
    default int getIndexRight(Object value) {
        if (isNil())
            return -1;
        return getData().getIndexRight(value);
    }

    /**
     * Find the index of an equal value.
     * (The list is searched in order.)
     *
     * @param value The value sought.
     * @return The index, or -1.
     */
    default int findIndex(Object value) {
        if (isNil())
            return -1;
        return getData().findIndex(value);
    }

    /**
     * Find the index of an equal existing value.
     * (The list is searched in reverse order.)
     *
     * @param value The value sought.
     * @return The index, or -1.
     */
    default int findIndexRight(Object value) {
        if (isNil())
            return -1;
        return getData().findIndexRight(value);
    }

    /**
     * Returns the index higher than the given index.
     *
     * @param ndx A given index.
     * @return An index that is higher, or -1.
     */
    default int higherIndex(int ndx) {
        if (isNil())
            return -1;
        if (ndx < 0)
            return 0;
        return (totalSize() > ndx + 1) ? ndx + 1 : -1;
    }

    /**
     * Returns the index equal to the given index.
     *
     * @param ndx A given index.
     * @return An index that is equal, or -1.
     */
    default int ceilingIndex(int ndx) {
        if (isNil())
            return -1;
        if (ndx < 0)
            return 0;
        return (totalSize() > ndx) ? ndx : -1;
    }

    /**
     * Returns the first value in the list.
     *
     * @return The index of the first value in the list, or -1.
     */
    default int firstIndex() {
        return isNil() ? -1 : 0;
    }

    /**
     * Returns the index lower than the given index.
     *
     * @param ndx A given index.
     * @return An index of an existing value that is lower, or -1.
     */
    default int lowerIndex(int ndx) {
        if (ndx <= 0 || isNil())
            return -1; //out of range
        int t = totalSize();
        if (ndx >= t)
            return t - 1;
        return ndx - 1;
    }

    /**
     * Returns the index if in range.
     *
     * @param ndx A given index.
     * @return The index, or -1.
     */
    default int floorIndex(int ndx) {
        if (ndx < 0 || isNil())
            return -1; //out of range
        int t = totalSize();
        if (ndx >= t)
            return t - 1;
        return ndx;
    }

    /**
     * Returns the index of the last value in the list.
     *
     * @return The index of the last value in the list, or -1.
     */
    default int lastIndex() {
        return totalSize() - 1;
    }

    /**
     * Returns true if there are no values present for the given time.
     *
     * @return Returns true if the list is empty for the given time.
     */
    default boolean isEmpty() {
        return isNil();
    }

    /**
     * Returns a list of all the values.
     *
     * @return A list of all values.
     */
    default List flatList() {
        List list = new ArrayList<>();
        getData().flatList(list);
        return list;
    }

    /**
     * Returns an iterator over the values.
     *
     * @return The iterator.
     */
    default Iterator iterator() {
        return new Iterator() {
            int last = -1;

            @Override
            public boolean hasNext() {
                return higherIndex(last) > -1;
            }

            @Override
            public Object next() {
                int next = higherIndex(last);
                if (next == -1)
                    throw new NoSuchElementException();
                last = next;
                return get(last);
            }
        };
    }

    /**
     * Returns a list accessor.
     *
     * @return A list accessor for the latest time.
     */
    default ListAccessor listAccessor() {
        return listAccessor(null);
    }

    /**
     * Returns a list accessor.
     *
     * @param key The key for the list.
     * @return A list accessor for the given time.
     */
    default ListAccessor listAccessor(Comparable key) {
        return new ListAccessor() {
            @Override
            public Comparable key() {
                return key;
            }

            @Override
            public long time() {
                return FactoryRegistry.MAX_TIME;
            }

            @Override
            public int size() {
                return ListNode.this.size();
            }

            @Override
            public Object get(int ndx) {
                return ListNode.this.get(ndx);
            }

            @Override
            public int getIndex(Object value) {
                return ListNode.this.getIndex(value);
            }

            @Override
            public int getIndexRight(Object value) {
                return ListNode.this.getIndexRight(value);
            }

            @Override
            public int findIndex(Object value) {
                return ListNode.this.findIndex(value);
            }

            @Override
            public int findIndexRight(Object value) {
                return ListNode.this.findIndexRight(value);
            }

            @Override
            public int higherIndex(int ndx) {
                return ListNode.this.higherIndex(ndx);
            }

            @Override
            public int ceilingIndex(int ndx) {
                return ListNode.this.ceilingIndex(ndx);
            }

            @Override
            public int firstIndex() {
                return ListNode.this.firstIndex();
            }

            @Override
            public int lowerIndex(int ndx) {
                return ListNode.this.lowerIndex(ndx);
            }

            @Override
            public int floorIndex(int ndx) {
                return ListNode.this.floorIndex(ndx);
            }

            @Override
            public int lastIndex() {
                return ListNode.this.lastIndex();
            }

            @Override
            public boolean isEmpty() {
                return ListNode.this.isEmpty();
            }

            @Override
            public List flatList() {
                return ListNode.this.flatList();
            }

            @Override
            public Iterator iterator() {
                return ListNode.this.iterator();
            }
        };
    }

    /**
     * Add a non-null value to the end of the list.
     *
     * @param value The value to be added.
     * @return The revised root node.
     */
    default ListNode add(Object value) {
        return add(-1, value);
    }

    /**
     * Add a non-null value to the list.
     *
     * @param ndx   Where to add the value, or -1 to append to the end.
     * @param value The value to be added.
     * @return The revised root node.
     */
    default ListNode add(int ndx, Object value) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            if (ndx != 0 && ndx != -1)
                throw new IllegalArgumentException("index out of range");
            return getData().replace(1, 1, value);
        }
        return getData().add(ndx, value);
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
        int expected = byteBuffer.position() + getDurableLength();
        if (isNil()) {
            byteBuffer.putChar(getRegistry().nilListId);
        } else {
            byteBuffer.putChar(getRegistry().listNodeImplId);
            serialize(byteBuffer);
        }
        if (expected != byteBuffer.position()) {
            getRegistry().db.close();
            throw new SerializationException();
        }
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    void serialize(ByteBuffer byteBuffer);

    default ListNode remove(int ndx) {
        if (isNil())
            return this;
        if (ndx < 0)
            return this;
        return getData().remove(ndx);
    }

    @Override
    default void releaseAll() {
        getData().releaseAll();
    }

    @Override
    default Object resize(int maxSize, int maxBlockSize) {
        return getData().resize(maxSize, maxBlockSize);
    }
}

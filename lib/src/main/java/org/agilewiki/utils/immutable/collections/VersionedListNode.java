package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.Releasable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable versioned list.
 */
public class VersionedListNode implements Releasable {

    public final VersionedListNodeFactory factory;

    protected final AtomicReference<VersionedListNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected VersionedListNode(VersionedListNodeFactory factory) {
        this.factory = factory;
        dataReference.set(new VersionedListNodeData(this));
        durableLength = 2;
    }

    protected VersionedListNode(VersionedListNodeFactory factory, ByteBuffer byteBuffer) {
        this.factory = factory;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected VersionedListNode(VersionedListNodeFactory factory,
                                int level,
                                int totalSize,
                                long created,
                                long deleted,
                                VersionedListNode leftNode,
                                Object value,
                                VersionedListNode rightNode) {
        this.factory = factory;
        VersionedListNodeData data = new VersionedListNodeData(
                this,
                level,
                totalSize,
                created,
                deleted,
                leftNode,
                value,
                rightNode);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    protected VersionedListNodeData getData() {
        VersionedListNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new VersionedListNodeData(this, byteBuffer.slice()));
        return dataReference.get();
    }

    /**
     * Returns the count of all the values in the list, deleted or not.
     *
     * @return The count of all the values in the list.
     */
    public int totalSize() {
        return isNil() ? 0 : getData().totalSize;
    }

    protected boolean isNil() {
        return this == factory.nilVersionedList;
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
        return getData().size(time);
    }

    /**
     * Returns a value if it is in range and the value exists for the given time.
     *
     * @param ndx  The index of the selected value.
     * @param time The time of the query.
     * @return A value, or null.
     */
    public Object getExistingValue(int ndx, long time) {
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
    public int getIndex(Object value, long time) {
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
    public int getIndexRight(Object value, long time) {
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
    public int findIndex(Object value, long time) {
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
    public int findIndexRight(Object value, long time) {
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
    public int higherIndex(int ndx, long time) {
        return getData().higherIndex(ndx, time);
    }

    /**
     * Returns the index of an existing value higher than or equal to the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher or equal, or -1.
     */
    public int ceilingIndex(int ndx, long time) {
        return getData().ceilingIndex(ndx, time);
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
        return getData().lowerIndex(ndx, time);
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
        return getData().floorIndex(ndx, time);
    }

    /**
     * Returns the index of the last existing value in the list.
     *
     * @param time The time of the query.
     * @return The index of the last existing value in the list, or -1.
     */
    public int lastIndex(long time) {
        return floorIndex(totalSize(), time);
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
        return getData().isEmpty(time);
    }

    /**
     * Returns a list of all the values that are present for a given time.
     *
     * @param time The time of the query.
     * @return A list of all values present for the given time.
     */
    public List flatList(long time) {
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
                return getExistingValue(last, time);
            }
        };
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor() {
        return listAccessor(null, FactoryRegistry.MAX_TIME);
    }

    /**
     * Returns a list accessor for the latest time.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor(Comparable key) {
        return listAccessor(key, FactoryRegistry.MAX_TIME);
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
    public VersionedListNode add(Object value, long time) {
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
    public VersionedListNode add(int ndx, Object value, long time) {
        return add(ndx, value, time, Long.MAX_VALUE);
    }

    protected VersionedListNode add(int ndx, Object value, long created, long deleted) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            if (ndx != 0 && ndx != -1)
                throw new IllegalArgumentException("index out of range");
            return new VersionedListNode(factory, 1, 1, created, deleted, factory.nilVersionedList, value, factory.nilVersionedList);
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
    public VersionedListNode remove(int ndx, long time) {
        if (isNil())
            return this;
        return getData().remove(ndx, time);
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public VersionedListNode copyList() {
        return copyList(0L);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public VersionedListNode copyList(long time) {
        return getData().copyList(factory.nilVersionedList, time);
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned list.
     */
    public VersionedListNode clearList(long time) {
        return getData().clearList(time);
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
     * @param byteBuffer The byte buffer.
     */
    public void writeDurable(ByteBuffer byteBuffer) {
        if (isNil()) {
            byteBuffer.putChar(factory.nilVersionedListId);
            return;
        }
        byteBuffer.putChar(factory.id);
        serialize(byteBuffer);
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer) {
        if (this.byteBuffer == null) {
            byteBuffer.putInt(getDurableLength());
            getData().serialize(byteBuffer);
            return;
        }
        ByteBuffer bb = byteBuffer.slice();
        bb.limit(durableLength - 6);
        byteBuffer.put(this.byteBuffer.slice());
        this.byteBuffer = bb;
        dataReference.set(null); //limit memory footprint, plugs memory leak.
    }

    @Override
    public void release()
            throws IOException {
        getData().release();
    }
}

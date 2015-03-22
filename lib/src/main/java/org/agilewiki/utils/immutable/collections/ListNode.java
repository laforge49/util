package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable list.
 */
public class ListNode {

    public final ListNodeFactory factory;

    protected final AtomicReference<ListNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected ListNode(ListNodeFactory factory) {
        this.factory = factory;
        dataReference.set(new ListNodeData(this));
        durableLength = 2;
    }

    protected ListNode(ListNodeFactory factory, ByteBuffer byteBuffer) {
        this.factory = factory;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected ListNode(ListNodeFactory factory,
                       int level,
                       int totalSize,
                       ListNode leftNode,
                       Object value,
                       ListNode rightNode) {
        this.factory = factory;
        ListNodeData data = new ListNodeData(
                this,
                level,
                totalSize,
                leftNode,
                value,
                rightNode);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    protected ListNodeData getData() {
        ListNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new ListNodeData(this, byteBuffer.slice()));
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
        return this == factory.nilList;
    }

    /**
     * Returns the count of all the values currently in the list.
     *
     * @return The current size of the list.
     */
    public int size() {
        if (isNil())
            return 0;
        return getData().size();
    }

    /**
     * Returns a value if it is in range.
     *
     * @param ndx  The index of the selected value.
     * @return A value, or null.
     */
    public Object get(int ndx) {
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
    public int getIndex(Object value) {
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
    public int getIndexRight(Object value) {
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
    public int findIndex(Object value) {
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
    public int findIndexRight(Object value) {
        if (isNil())
            return -1;
        return getData().findIndexRight(value);
    }

    /**
     * Returns the index higher than the given index.
     *
     * @param ndx  A given index.
     * @return An index that is higher, or -1.
     */
    public int higherIndex(int ndx) {
        if (isNil())
            return -1;
        if (ndx < 0)
            return 0;
        return (totalSize() > ndx + 1) ? ndx + 1 : -1;
    }

    /**
     * Returns the index equal to the given index.
     *
     * @param ndx  A given index.
     * @return An index that is equal, or -1.
     */
    public int ceilingIndex(int ndx) {
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
    public int firstIndex() {
        return isNil() ? -1 : 0;
    }

    /**
     * Returns the index lower than the given index.
     *
     * @param ndx  A given index.
     * @return An index of an existing value that is lower, or -1.
     */
    public int lowerIndex(int ndx) {
        if (ndx <= 0 || isNil())
            return -1; //out of range
        int t = totalSize();
        if (ndx >= t)
            return t -1;
        return ndx - 1;
    }

    /**
     * Returns the index if in range.
     *
     * @param ndx  A given index.
     * @return The index, or -1.
     */
    public int floorIndex(int ndx) {
        if (ndx < 0 || isNil())
            return -1; //out of range
        int t = totalSize();
        if (ndx >= t)
            return t -1;
        return ndx;
    }

    /**
     * Returns the index of the last value in the list.
     *
     * @return The index of the last value in the list, or -1.
     */
    public int lastIndex() {
        return totalSize() - 1;
    }

    /**
     * Returns true if there are no values present for the given time.
     *
     * @return Returns true if the list is empty for the given time.
     */
    public boolean isEmpty() {
        return isNil();
    }

    /**
     * Returns a list of all the values.
     *
     * @return A list of all values.
     */
    public List flatList() {
        List list = new ArrayList<>();
        getData().flatList(list);
        return list;
    }

    /**
     * Returns an iterator over the values.
     *
     * @return The iterator.
     */
    public Iterator iterator() {
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
    public ListAccessor listAccessor() {
        return listAccessor(null);
    }

    /**
     * Returns a list accessor.
     *
     * @param key  The key for the list.
     * @return A list accessor for the given time.
     */
    public ListAccessor listAccessor(Comparable key) {
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
    public ListNode add(Object value) {
        return add(-1, value);
    }

    /**
     * Add a non-null value to the list.
     *
     * @param ndx   Where to add the value, or -1 to append to the end.
     * @param value The value to be added.
     * @return The revised root node.
     */
    public ListNode add(int ndx, Object value) {
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            if (ndx != 0 && ndx != -1)
                throw new IllegalArgumentException("index out of range");
            return new ListNode(factory, 1, 1, factory.nilList, value, factory.nilList);
        }
        return getData().add(ndx, value);
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
            byteBuffer.putChar(factory.nilListId);
            return;
        }
        byteBuffer.putChar(factory.nilListId);
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

    public ListNode remove(int ndx) {
        if (isNil())
            return this;
        if (ndx < 0)
            return this;
        return getData().remove(ndx);
    }

    public String toString() {
        if (isNil())
            return "";
        return getData().toString();
    }
}

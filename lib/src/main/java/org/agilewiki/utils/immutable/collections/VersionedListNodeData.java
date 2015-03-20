package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * The durable data elements of a list node.
 */
public class VersionedListNodeData {

    /**
     * The node which holds this data.
     */
    public final VersionedListNode thisNode;

    /**
     * Composite node depth--see AA Tree algorithm.
     */
    public final int level;

    /**
     * Number of nodes in this subtree.
     */
    public final int totalSize;

    /**
     * Creation time of this node.
     */
    public final long created;

    /**
     * Deletion time of this node.
     */
    public final long deleted;

    /**
     * Left subtree node.
     */
    public final VersionedListNode leftNode;

    /**
     * The value of the node.
     */
    public final Object value;

    /**
     * Right subtree node.
     */
    public final VersionedListNode rightNode;

    /**
     * The factory for the value.
     */
    protected final ImmutableFactory valueFactory;

    /**
     * Create the nil node data.
     *
     * @param thisNode The node which holds this data.
     */
    public VersionedListNodeData(VersionedListNode thisNode) {
        this.thisNode = thisNode;
        this.level = 0;
        totalSize = 0;
        this.created = 0L;
        this.deleted = 0L;
        this.leftNode = thisNode;
        this.value = null;
        this.rightNode = thisNode;

        valueFactory = null;
    }

    /**
     * Create non-nill node data.
     *
     * @param thisNode  The node which holds this data.
     * @param level     Composite node depth--see AA Tree algorithm.
     * @param totalSize Number of nodes in this subtree.
     * @param created   Creation time of this node.
     * @param deleted   Deletion time of this node.
     * @param leftNode  Left subtree node.
     * @param value     The value of the node.
     * @param rightNode Right subtree node.
     */
    public VersionedListNodeData(VersionedListNode thisNode,
                                 int level,
                                 int totalSize,
                                 long created,
                                 long deleted,
                                 VersionedListNode leftNode,
                                 Object value,
                                 VersionedListNode rightNode) {
        this.thisNode = thisNode;
        this.level = level;
        this.totalSize = totalSize;
        this.created = created;
        this.deleted = deleted;
        this.leftNode = leftNode;
        this.value = value;
        this.rightNode = rightNode;
        this.valueFactory = thisNode.factory.factoryRegistry.getImmutableFactory(value);
    }

    /**
     * Create non-nill node data.
     *
     * @param thisNode   The node which holds this data.
     * @param byteBuffer Holds the serialized data.
     */
    public VersionedListNodeData(VersionedListNode thisNode, ByteBuffer byteBuffer) {
        this.thisNode = thisNode;
        level = byteBuffer.getInt();
        totalSize = byteBuffer.getInt();
        created = byteBuffer.getLong();
        deleted = byteBuffer.getLong();
        FactoryRegistry registry = thisNode.factory.factoryRegistry;
        ImmutableFactory f = registry.readId(byteBuffer);
        leftNode = (VersionedListNode) f.deserialize(byteBuffer);
        valueFactory = registry.readId(byteBuffer);
        value = valueFactory.deserialize(byteBuffer);
        f = registry.readId(byteBuffer);
        rightNode = (VersionedListNode) f.deserialize(byteBuffer);
    }

    /**
     * Returns true if this is the data for the nil node.
     *
     * @return True if nil node.
     */
    public boolean isNil() {
        return level == 0;
    }

    /**
     * Returns the length of the serialized data, including the id and durable length.
     *
     * @return The length of the serialized data.
     */
    public int getDurableLength() {
        if (isNil())
            return 2;
        return 2 + 4 + 4 + 4 + 8 + 8 +
                leftNode.getDurableLength() +
                valueFactory.getDurableLength(value) +
                rightNode.getDurableLength();
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer) {
        byteBuffer.putInt(level);
        byteBuffer.putInt(totalSize);
        byteBuffer.putLong(created);
        byteBuffer.putLong(deleted);
        leftNode.writeDurable(byteBuffer);
        valueFactory.writeDurable(value, byteBuffer);
        rightNode.writeDurable(byteBuffer);
    }

    /**
     * Returns true if the value of the node exists for the given time.
     *
     * @param time The time of the query.
     * @return True if the value currently exists.
     */
    public boolean exists(long time) {
        return time >= created && time < deleted;
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
     * Returns the selected node.
     *
     * @param ndx Relative position of the selected node within the sublist.
     * @return The selected node, or null.
     */
    public VersionedListNode getListNode(int ndx) {
        if (ndx < 0 || ndx >= totalSize)
            return null; //out of range
        int leftSize = leftNode.totalSize();
        if (ndx < leftSize)
            return leftNode.getData().getListNode(ndx);
        if (ndx > leftSize)
            return rightNode.getData().getListNode(ndx - leftSize - 1);
        return thisNode;
    }

    /**
     * Returns the value if it exists.
     *
     * @param time The time of the query.
     * @return The value, or null.
     */
    public Object getExistingValue(long time) {
        return exists(time) ? value : null;
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
            return leftNode.totalSize();
        ndx = rightNode.getIndex(value, time);
        if (ndx == -1)
            return -1;
        return leftNode.totalSize() + 1 + ndx;
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
            return leftNode.totalSize() + 1 + ndx;
        if (this.value == value && exists(time))
            return leftNode.totalSize();
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
            return leftNode.totalSize();
        ndx = rightNode.findIndex(value, time);
        if (ndx == -1)
            return -1;
        return leftNode.totalSize() + 1 + ndx;
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
            return leftNode.totalSize() + 1 + ndx;
        if (exists(time) && this.value.equals(value))
            return leftNode.totalSize();
        ndx = leftNode.findIndexRight(value, time);
        if (ndx == -1)
            return -1;
        return ndx;
    }

    /**
     * Returns the index of an existing value higher than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is higher, or -1.
     */
    public int higherIndex(int ndx, long time) {
        if (ndx >= totalSize - 1 || isNil())
            return -1; //out of range
        int leftSize = leftNode.totalSize();
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
        if (ndx >= totalSize || isNil()) {
            return -1; //out of range
        }
        int leftSize = leftNode.totalSize();
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
     * Returns the index of an existing value lower than the given index.
     *
     * @param ndx  A given index.
     * @param time The time of the query.
     * @return An index of an existing value that is lower, or -1.
     */
    public int lowerIndex(int ndx, long time) {
        if (ndx <= 0 || isNil())
            return -1; //out of range
        int leftSize = leftNode.totalSize();
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
        int leftSize = leftNode.totalSize();
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

    /**
     * Appends existing values to the list.
     *
     * @param list The list being build.
     * @param time The time of the query.
     */
    public void flatList(List list, long time) {
        if (isNil())
            return;
        leftNode.getData().flatList(list, time);
        if (exists(time))
            list.add(value);
        rightNode.getData().flatList(list, time);
    }

    /**
     * AA Tree skew operation.
     *
     * @return Revised root node.
     */
    public VersionedListNode skew() {
        if (isNil() || leftNode.isNil())
            return thisNode;
        VersionedListNodeData leftData = leftNode.getData();
        if (leftData.level == level) {
            VersionedListNode t = new VersionedListNode(
                    thisNode.factory,
                    level,
                    totalSize - leftData.totalSize + leftData.leftNode.totalSize(),
                    created,
                    deleted,
                    leftData.rightNode,
                    value,
                    rightNode);
            return new VersionedListNode(
                    thisNode.factory,
                    leftData.level,
                    totalSize,
                    leftData.created,
                    leftData.deleted,
                    leftData.leftNode,
                    leftData.value,
                    t);
        } else
            return thisNode;
    }

    /**
     * AA Tree split
     *
     * @return The revised root node.
     */
    public VersionedListNode split() {
        if (isNil() || rightNode.isNil())
            return thisNode;
        VersionedListNodeData rightData = rightNode.getData();
        if (rightData.rightNode.isNil())
            return thisNode;
        if (level == rightData.rightNode.getData().level) {
            VersionedListNode t = new VersionedListNode(
                    thisNode.factory,
                    level,
                    totalSize - rightData.totalSize + rightData.leftNode.totalSize(),
                    created,
                    deleted,
                    leftNode,
                    value,
                    rightData.leftNode);
            VersionedListNode r = new VersionedListNode(
                    thisNode.factory,
                    rightData.level + 1,
                    totalSize,
                    rightData.created,
                    rightData.deleted,
                    t,
                    rightData.value,
                    rightData.rightNode);
            return r;
        }
        return thisNode;
    }

    /**
     * Add a non-null value to the list.
     *
     * @param ndx     Where to add the value, or -1 to append to the end.
     * @param value   The value to be added.
     * @param created Creation time.
     * @param deleted Deletion time, or MAX_VALUE.
     * @return The revised root node.
     */
    public VersionedListNode add(int ndx, Object value, long created, long deleted) {
        if (ndx == -1)
            ndx = totalSize;
        int leftSize = leftNode.totalSize();
        VersionedListNode t = thisNode;
        if (ndx <= leftSize) {
            t = new VersionedListNode(
                    thisNode.factory,
                    level,
                    totalSize + 1,
                    this.created,
                    this.deleted,
                    leftNode.add(ndx, value, created, deleted),
                    this.value,
                    rightNode);
        } else {
            t = new VersionedListNode(
                    thisNode.factory,
                    level,
                    totalSize + 1,
                    this.created,
                    this.deleted,
                    leftNode,
                    this.value,
                    rightNode.add(ndx - leftSize - 1, value, created, deleted));
        }
        return t.getData().skew().getData().split();
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
            return thisNode;
        int leftSize = leftNode.totalSize();
        if (ndx == leftSize) {
            if (exists(time))
                return new VersionedListNode(
                        thisNode.factory,
                        level,
                        totalSize,
                        created,
                        time,
                        leftNode,
                        value,
                        rightNode);
            return thisNode;
        }
        if (ndx < leftSize) {
            VersionedListNode n = leftNode.remove(ndx, time);
            if (leftNode == n)
                return thisNode;
            return new VersionedListNode(
                    thisNode.factory,
                    level,
                    totalSize,
                    created,
                    deleted,
                    n,
                    value,
                    rightNode);
        }
        VersionedListNode n = rightNode.remove(ndx - leftSize - 1, time);
        if (rightNode == n)
            return thisNode;
        return new VersionedListNode(
                thisNode.factory,
                level,
                totalSize,
                created,
                deleted,
                leftNode,
                value,
                n);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param n    The new list.
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public VersionedListNode copyList(VersionedListNode n, long time) {
        if (isNil())
            return n;
        n = leftNode.getData().copyList(n, time);
        if (deleted >= time)
            n = n.add(n.totalSize(), value, created, deleted);
        return rightNode.getData().copyList(n, time);
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned list.
     */
    public VersionedListNode clearList(long time) {
        if (isNil())
            return thisNode;
        VersionedListNode ln = leftNode.clearList(time);
        VersionedListNode rn = rightNode.clearList(time);
        if (ln == leftNode && rn == rightNode && !exists(time))
            return thisNode;
        return new VersionedListNode(thisNode.factory, level, totalSize, created, time, ln, value, rn);
    }
}

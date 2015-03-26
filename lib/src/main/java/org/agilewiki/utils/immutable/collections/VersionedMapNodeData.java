package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Releasable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * The durable data elements of a map node.
 */
public class VersionedMapNodeData implements Releasable {

    /**
     * The node which holds this data.
     */
    public final VersionedMapNode thisNode;

    /**
     * Composite node depth--see AA Tree algorithm.
     */
    public final int level;

    /**
     * Left subtree node.
     */
    public final VersionedMapNode leftNode;

    /**
     * The list of the node.
     */
    public final VersionedListNode listNode;

    /**
     * Right subtree node.
     */
    public final VersionedMapNode rightNode;

    /**
     * The list of the node.
     */
    public final Comparable key;

    /**
     * The factory for the key.
     */
    public final ImmutableFactory keyFactory;

    /**
     * Create the nil node data.
     *
     * @param thisNode The node which holds this data.
     */
    public VersionedMapNodeData(VersionedMapNode thisNode) {
        this.thisNode = thisNode;
        this.level = 0;
        this.leftNode = thisNode;
        this.listNode = thisNode.factory.nilVersionedList;
        this.rightNode = thisNode;
        key = null;

        keyFactory = null;
    }

    /**
     * Create non-nill node data.
     *
     * @param thisNode  The node which holds this data.
     * @param level     Composite node depth--see AA Tree algorithm.
     * @param leftNode  Left subtree node.
     * @param listNode  The list of the node.
     * @param rightNode Right subtree node.
     * @param key       The key of node.
     */
    public VersionedMapNodeData(VersionedMapNode thisNode,
                                int level,
                                VersionedMapNode leftNode,
                                VersionedListNode listNode,
                                VersionedMapNode rightNode,
                                Comparable key) {
        this.thisNode = thisNode;
        this.level = level;
        this.leftNode = leftNode;
        this.listNode = listNode;
        this.rightNode = rightNode;
        this.key = key;
        keyFactory = thisNode.factory.factoryRegistry.getImmutableFactory(key);
    }

    /**
     * Create non-nill node data.
     *
     * @param thisNode   The node which holds this data.
     * @param byteBuffer Holds the serialized data.
     */
    public VersionedMapNodeData(VersionedMapNode thisNode, ByteBuffer byteBuffer) {
        this.thisNode = thisNode;
        level = byteBuffer.getInt();
        FactoryRegistry factoryRegistry = thisNode.factory.factoryRegistry;
        ImmutableFactory f = factoryRegistry.readId(byteBuffer);
        leftNode = (VersionedMapNode) f.deserialize(byteBuffer);
        f = factoryRegistry.readId(byteBuffer);
        listNode = (VersionedListNode) f.deserialize(byteBuffer);
        f = factoryRegistry.readId(byteBuffer);
        rightNode = (VersionedMapNode) f.deserialize(byteBuffer);
        keyFactory = factoryRegistry.readId(byteBuffer);
        key = (Comparable) keyFactory.deserialize(byteBuffer);
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
     * Returns the list for the node.
     *
     * @param key The key for the node.
     * @return The list, or null.
     */
    public VersionedListNode getList(Comparable key) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c < 0)
            return leftNode.getList(key);
        if (c == 0)
            return listNode;
        return rightNode.getList(key);
    }

    /**
     * AA Tree skew operation.
     *
     * @return Revised root node.
     */
    public VersionedMapNode skew() {
        if (isNil() || leftNode.isNil())
            return thisNode;
        VersionedMapNodeData leftData = leftNode.getData();
        if (leftData.level == level) {
            VersionedMapNode t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftData.rightNode,
                    listNode,
                    rightNode,
                    key);
            return new VersionedMapNode(
                    thisNode.factory,
                    leftData.level,
                    leftData.leftNode,
                    leftData.listNode,
                    t,
                    leftData.key);
        } else
            return thisNode;
    }

    /**
     * AA Tree split
     *
     * @return The revised root node.
     */
    public VersionedMapNode split() {
        if (isNil() || rightNode.isNil())
            return thisNode;
        VersionedMapNodeData rightData = rightNode.getData();
        if (rightData.rightNode.isNil())
            return thisNode;
        if (level == rightData.rightNode.getData().level) {
            VersionedMapNode t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode,
                    rightData.leftNode,
                    key);
            VersionedMapNode r = new VersionedMapNode(
                    thisNode.factory,
                    rightData.level + 1,
                    t,
                    rightData.listNode,
                    rightData.rightNode,
                    rightData.key);
            return r;
        }
        return thisNode;
    }

    /**
     * Add a non-null value to the list.
     *
     * @param key     The key of the list.
     * @param ndx     Where to add the value.
     * @param value   The value to be added.
     * @param created Creation time.
     * @param deleted Deletion time, or MAX_VALUE.
     * @return The revised root node.
     */
    public VersionedMapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        VersionedMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode.add(key, ndx, value, created, deleted),
                    listNode,
                    rightNode,
                    this.key);
        } else if (c == 0) {
            return new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode.add(ndx, value, created, deleted),
                    rightNode,
                    this.key);
        } else {
            t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode,
                    rightNode.add(key, ndx, value, created, deleted),
                    this.key);
        }
        return t.getData().skew().getData().split();
    }

    /**
     * Mark a value as deleted.
     *
     * @param key  The key of the list.
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public VersionedMapNode remove(Comparable key, int ndx, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.remove(key, ndx, time);
            if (n == leftNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, n, listNode, rightNode, this.key);
        } else if (c == 0) {
            VersionedListNode n = listNode.remove(ndx, time);
            if (n == listNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, leftNode, n, rightNode, this.key);
        } else {
            VersionedMapNode n = rightNode.remove(key, ndx, time);
            if (n == rightNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, leftNode, listNode, n, this.key);
        }
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public VersionedMapNode clearList(Comparable key, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.clearList(key, time);
            if (n == leftNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, n, listNode, rightNode, this.key);
        } else if (c == 0) {
            VersionedListNode n = listNode.clearList(time);
            if (n == listNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, leftNode, n, rightNode, this.key);
        } else {
            VersionedMapNode n = rightNode.clearList(key, time);
            if (n == rightNode)
                return thisNode;
            return new VersionedMapNode(thisNode.factory, level, leftNode, listNode, n, this.key);
        }
    }

    /**
     * Replace the list entries with a single value.
     *
     * @param key   The key of the list.
     * @param value The new value.
     * @param time  The time of the replacement.
     * @return The revised node.
     */
    public VersionedMapNode set(Comparable key, Object value, long time) {
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.set(key, value, time);
            return new VersionedMapNode(thisNode.factory, level, n, listNode, rightNode, this.key);
        } else if (c == 0) {
            VersionedListNode n = listNode.clearList(time);
            n = n.add(value, time);
            return new VersionedMapNode(thisNode.factory, level, leftNode, n, rightNode, this.key);
        } else {
            VersionedMapNode n = rightNode.set(key, value, time);
            return new VersionedMapNode(thisNode.factory, level, leftNode, listNode, n, this.key);
        }
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned map.
     */
    public VersionedMapNode clearMap(long time) {
        if (isNil())
            return thisNode;
        VersionedMapNode ln = leftNode.clearMap(time);
        VersionedMapNode rn = rightNode.clearMap(time);
        if (ln == leftNode && rn == rightNode && listNode.isEmpty(time))
            return thisNode;
        return new VersionedMapNode(
                thisNode.factory,
                level,
                ln,
                listNode.clearList(time),
                rn,
                key);
    }

    /**
     * Builds a set of all keys with non-empty lists for the given time.
     *
     * @param keys The set being built.
     * @param time The time of the query.
     */
    public void flatKeys(NavigableSet<Comparable> keys, long time) {
        if (isNil())
            return;
        leftNode.getData().flatKeys(keys, time);
        if (!listNode.isEmpty(time))
            keys.add(key);
        rightNode.getData().flatKeys(keys, time);
    }

    /**
     * Builds a map of all the keys and values present at the given time.
     *
     * @param map  The map being built.
     * @param time The time of the query.
     */
    public void flatMap(NavigableMap<Comparable, List> map, long time) {
        if (isNil())
            return;
        leftNode.getData().flatMap(map, time);
        if (!listNode.isEmpty(time))
            map.put(key, listNode.flatList(time));
        rightNode.getData().flatMap(map, time);
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the lists are not copied.)
     *
     * @param n    The map being built.
     * @param time The time of the query.
     * @return The revised copy root.
     */
    public VersionedMapNode copyMap(VersionedMapNode n, long time) {
        if (isNil())
            return n;
        n = leftNode.getData().copyMap(n, time);
        n = n.getData().addList(key, listNode.copyList(time));
        return leftNode.getData().copyMap(n, time);
    }

    protected VersionedMapNode addList(Comparable key, VersionedListNode listNode) {
        if (listNode.isNil())
            return thisNode;
        if (isNil()) {
            return new VersionedMapNode(
                    thisNode.factory,
                    1,
                    thisNode.factory.nilVersionedMap,
                    listNode,
                    thisNode.factory.nilVersionedMap,
                    key);
        }
        VersionedMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode.getData().addList(key, listNode),
                    listNode,
                    rightNode,
                    key);
        } else if (c == 0) {
            throw new IllegalArgumentException("duplicate key not supported");
        } else {
            t = new VersionedMapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode,
                    rightNode.getData().addList(key, listNode),
                    key);
        }
        return t.getData().skew().getData().split();
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
     * Returns the length of the serialized data, including the id and durable length.
     *
     * @return The length of the serialized data.
     */
    public int getDurableLength() {
        if (isNil())
            return 2;
        return 2 + 4 + 4 +
                leftNode.getDurableLength() +
                listNode.getDurableLength() +
                rightNode.getDurableLength() +
                keyFactory.getDurableLength(key);
    }

    /**
     * Serialize this object into a ByteBuffer.
     *
     * @param byteBuffer Where the serialized data is to be placed.
     */
    public void serialize(ByteBuffer byteBuffer) {
        byteBuffer.putInt(level);
        leftNode.writeDurable(byteBuffer);
        listNode.writeDurable(byteBuffer);
        rightNode.writeDurable(byteBuffer);
        keyFactory.writeDurable(key, byteBuffer);
    }

    @Override
    public void release()
            throws IOException {
        if (leftNode instanceof Releasable)
            ((Releasable) leftNode).release();
        if (listNode instanceof Releasable)
            ((Releasable) listNode).release();
        if (rightNode instanceof Releasable)
            ((Releasable) rightNode).release();
    }
}

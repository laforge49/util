package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * The durable data elements of a map node.
 */
public class DurableMapNodeData {

    /**
     * The node which holds this data.
     */
    public final LazyDurableMapNode thisNode;

    /**
     * Composite node depth--see AA Tree algorithm.
     */
    public final int level;

    /**
     * Left subtree node.
     */
    public final LazyDurableMapNode leftNode;

    /**
     * The list of the node.
     */
    public final LazyDurableListNode listNode;

    /**
     * Right subtree node.
     */
    public final LazyDurableMapNode rightNode;

    /**
     * The list of the node.
     */
    public final Comparable key;

    /**
     * The factory for the key.
     */
    public final LazyDurableFactory keyFactory;

    /**
     * Create the nil node data.
     *
     * @param thisNode The node which holds this data.
     */
    public DurableMapNodeData(LazyDurableMapNode thisNode) {
        this.thisNode = thisNode;
        this.level = 0;
        this.leftNode = thisNode;
        this.listNode = null;
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
    public DurableMapNodeData(LazyDurableMapNode thisNode,
                               int level,
                               LazyDurableMapNode leftNode,
                               LazyDurableListNode listNode,
                               LazyDurableMapNode rightNode,
                               Comparable key) {
        this.thisNode = thisNode;
        this.level = level;
        this.leftNode = leftNode;
        this.listNode = listNode;
        this.rightNode = rightNode;
        this.key = key;
        keyFactory = FactoryRegistry.getDurableFactory(key);
    }

    /**
     * Create non-nill node data.
     *
     * @param thisNode   The node which holds this data.
     * @param byteBuffer Holds the serialized data.
     */
    public DurableMapNodeData(LazyDurableMapNode thisNode, ByteBuffer byteBuffer) {
        this.thisNode = thisNode;
        level = byteBuffer.getInt();
        LazyDurableFactory f = FactoryRegistry.readId(byteBuffer);
        leftNode = (LazyDurableMapNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        listNode = (LazyDurableListNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        rightNode = (LazyDurableMapNode) f.deserialize(byteBuffer);
        keyFactory = FactoryRegistry.readId(byteBuffer);
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
     * @param key    The key for the node.
     * @return The list, or null.
     */
    public LazyDurableListNode getList(Comparable key) {
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
    public LazyDurableMapNode skew() {
        if (isNil() || leftNode.isNil())
            return thisNode;
        DurableMapNodeData leftData = leftNode.getData();
        if (leftData.level == level) {
            LazyDurableMapNode t = new LazyDurableMapNode(
                    level,
                    leftData.rightNode,
                    listNode,
                    rightNode,
                    key);
            return new LazyDurableMapNode(
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
    public LazyDurableMapNode split() {
        if (isNil() || rightNode.isNil())
            return thisNode;
        DurableMapNodeData rightData = rightNode.getData();
        if (rightData.rightNode.isNil())
            return thisNode;
        if (level == rightData.rightNode.getData().level) {
            LazyDurableMapNode t = new LazyDurableMapNode(
                    level,
                    leftNode,
                    listNode,
                    rightData.leftNode,
                    key);
            LazyDurableMapNode r = new LazyDurableMapNode(
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
     * @param key   The key of the list.
     * @param ndx   Where to add the value.
     * @param value The value to be added.
     * @param created    Creation time.
     * @param deleted    Deletion time, or MAX_VALUE.
     * @return The revised root node.
     */
    public LazyDurableMapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        LazyDurableMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new LazyDurableMapNode(
                    level,
                    leftNode.add(key, ndx, value, created, deleted),
                    listNode,
                    rightNode,
                    key);
        } else if (c == 0) {
            return new LazyDurableMapNode(
                    level,
                    leftNode,
                    listNode.add(ndx, value, created, deleted),
                    rightNode,
                    key);
        } else {
            t = new LazyDurableMapNode(
                    level,
                    leftNode,
                    listNode,
                    rightNode.add(key, ndx, value, created, deleted),
                    key);
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
    public LazyDurableMapNode remove(Comparable key, int ndx, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            LazyDurableMapNode n = leftNode.remove(key, ndx, time);
            if (n == leftNode)
                return thisNode;
            return new LazyDurableMapNode(level, n, listNode, rightNode, key);
        } else if (c == 0) {
            LazyDurableListNode n = listNode.remove(ndx, time);
            if (n == listNode)
                return thisNode;
            return new LazyDurableMapNode(level, leftNode, n, rightNode, key);
        } else {
            LazyDurableMapNode n = rightNode.remove(key, ndx, time);
            if (n == rightNode)
                return thisNode;
            return new LazyDurableMapNode(level, leftNode, listNode, n, key);
        }
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public LazyDurableMapNode clearList(Comparable key, long time) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            LazyDurableMapNode n = leftNode.clearList(key, time);
            if (n == leftNode)
                return thisNode;
            return new LazyDurableMapNode(level, n, listNode, rightNode, key);
        } else if (c == 0) {
            LazyDurableListNode n = listNode.clearList(time);
            if (n == listNode)
                return thisNode;
            return new LazyDurableMapNode(level, leftNode, n, rightNode, key);
        } else {
            LazyDurableMapNode n = rightNode.clearList(key, time);
            if (n == rightNode)
                return thisNode;
            return new LazyDurableMapNode(level, leftNode, listNode, n, key);
        }
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
        int c = key.compareTo(this.key);
        if (c < 0) {
            LazyDurableMapNode n = leftNode.set(key, value, time);
            return new LazyDurableMapNode(level, n, listNode, rightNode, key);
        } else if (c == 0) {
            LazyDurableListNode n = listNode.clearList(time);
            n = n.add(value, time);
            return new LazyDurableMapNode(level, leftNode, n, rightNode, key);
        } else {
            LazyDurableMapNode n = rightNode.set(key, value, time);
            return new LazyDurableMapNode(level, leftNode, listNode, n, key);
        }
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned map.
     */
    public LazyDurableMapNode clearMap(long time) {
        if (isNil())
            return thisNode;
        LazyDurableMapNode ln = leftNode.clearMap(time);
        LazyDurableMapNode rn = rightNode.clearMap(time);
        if (ln == leftNode && rn == rightNode && listNode.isEmpty(time))
            return thisNode;
        return new LazyDurableMapNode(level,
                ln,
                listNode.clearList(time),
                rn,
                key);
    }

    /**
     * Builds a set of all keys with non-empty lists for the given time.
     *
     * @param keys    The set being built.
     * @param time    The time of the query.
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
     * @param map     The map being built.
     * @param time    The time of the query.
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
     * @param n       The map being built.
     * @param time    The time of the query.
     * @return The revised copy root.
     */
    public LazyDurableMapNode copyMap(LazyDurableMapNode n, long time) {
        if (isNil())
            return n;
        n = leftNode.getData().copyMap(n, time);
        n = n.getData().addList(key, listNode.copyList(time));
        return leftNode.getData().copyMap(n, time);
    }

    protected LazyDurableMapNode addList(Comparable key, LazyDurableListNode listNode) {
        if (listNode.isNil())
            return thisNode;
        if (isNil()) {
            return new LazyDurableMapNode(
                    1, LazyDurableMapNode.MAP_NIL, listNode, LazyDurableMapNode.MAP_NIL, key);
        }
        LazyDurableMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new LazyDurableMapNode(
                    level,
                    leftNode.getData().addList(key, listNode),
                    listNode,
                    rightNode,
                    key);
        } else if (c == 0) {
            throw new IllegalArgumentException("duplicate key not supported");
        } else {
            t = new LazyDurableMapNode(
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
}

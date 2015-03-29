package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.virtualcow.DbFactoryRegistry;
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
        this.listNode = thisNode.getRegistry().versionedNilList;
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
        keyFactory = thisNode.getRegistry().getImmutableFactory(key);
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
        DbFactoryRegistry registry = thisNode.getRegistry();
        ImmutableFactory f = registry.readId(byteBuffer);
        leftNode = (VersionedMapNode) f.deserialize(byteBuffer);
        f = registry.readId(byteBuffer);
        listNode = (VersionedListNode) f.deserialize(byteBuffer);
        f = registry.readId(byteBuffer);
        rightNode = (VersionedMapNode) f.deserialize(byteBuffer);
        keyFactory = registry.readId(byteBuffer);
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
    public VersionedMapNode skew()
            throws IOException {
        if (isNil() || leftNode.isNil())
            return thisNode;
        VersionedMapNodeData leftData = leftNode.getData();
        if (leftData.level == level) {
            VersionedMapNode t = replaceLeft(leftData.rightNode);
            return leftData.replaceRight(t);
        } else
            return thisNode;
    }

    /**
     * AA Tree split
     *
     * @return The revised root node.
     */
    public VersionedMapNode split()
            throws IOException {
        if (isNil() || rightNode.isNil())
            return thisNode;
        VersionedMapNodeData rightData = rightNode.getData();
        if (rightData.rightNode.isNil())
            return thisNode;
        if (level == rightData.rightNode.getData().level) {
            VersionedMapNode t = replaceRight(rightData.leftNode);
            VersionedMapNode r = rightData.replaceLeft(rightData.level + 1, t);
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
    public VersionedMapNode add(Comparable key, int ndx, Object value, long created, long deleted)
            throws IOException {
        VersionedMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = replaceLeft(leftNode.add(key, ndx, value, created, deleted));
        } else if (c == 0) {
            return replace(listNode.add(ndx, value, created, deleted));
        } else {
            t = replaceRight(rightNode.add(key, ndx, value, created, deleted));
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
    public VersionedMapNode remove(Comparable key, int ndx, long time)
            throws IOException {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.remove(key, ndx, time);
            if (n == leftNode)
                return thisNode;
            return replaceLeft(n);
        } else if (c == 0) {
            VersionedListNode n = listNode.remove(ndx, time);
            if (n == listNode)
                return thisNode;
            return replace(n);
        } else {
            VersionedMapNode n = rightNode.remove(key, ndx, time);
            if (n == rightNode)
                return thisNode;
            return replaceRight(n);
        }
    }

    /**
     * Empty the list by marking all the existing values as deleted.
     *
     * @param key  The key of the list.
     * @param time The time of the deletion.
     * @return The revised node.
     */
    public VersionedMapNode clearList(Comparable key, long time)
            throws IOException {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.clearList(key, time);
            if (n == leftNode)
                return thisNode;
            return replaceLeft(n);
        } else if (c == 0) {
            VersionedListNode n = listNode.clearList(time);
            if (n == listNode)
                return thisNode;
            return replace(n);
        } else {
            VersionedMapNode n = rightNode.clearList(key, time);
            if (n == rightNode)
                return thisNode;
            return replaceRight(n);
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
    public VersionedMapNode set(Comparable key, Object value, long time)
            throws IOException {
        int c = key.compareTo(this.key);
        if (c < 0) {
            VersionedMapNode n = leftNode.set(key, value, time);
            return replaceLeft(n);
        } else if (c == 0) {
            VersionedListNode n = listNode.clearList(time);
            n = n.add(value, time);
            return replace(n);
        } else {
            VersionedMapNode n = rightNode.set(key, value, time);
            return replaceRight(n);
        }
    }

    /**
     * Empty the map by marking all the existing values as deleted.
     *
     * @param time The time of the deletion.
     * @return The currently empty versioned map.
     */
    public VersionedMapNode clearMap(long time)
            throws IOException {
        if (isNil())
            return thisNode;
        VersionedMapNode ln = leftNode.clearMap(time);
        VersionedMapNode rn = rightNode.clearMap(time);
        if (ln == leftNode && rn == rightNode && listNode.isEmpty(time))
            return thisNode;
        return replace(ln, listNode.clearList(time), rn);
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
    public VersionedMapNode copyMap(VersionedMapNode n, long time)
            throws IOException {
        if (isNil())
            return n;
        n = leftNode.getData().copyMap(n, time);
        n = n.getData().addList(key, listNode.copyList(time));
        return leftNode.getData().copyMap(n, time);
    }

    protected VersionedMapNode addList(Comparable key, VersionedListNode listNode)
            throws IOException {
        if (listNode.isNil())
            return thisNode;
        DbFactoryRegistry registry = thisNode.getRegistry();
        if (isNil()) {
            return replace(1, listNode, key);
        }
        VersionedMapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = replaceLeft(leftNode.getData().addList(key, listNode));
        } else if (c == 0) {
            throw new IllegalArgumentException("duplicate key not supported");
        } else {
            t = replaceRight(rightNode.getData().addList(key, listNode));
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
    public void releaseAll()
            throws IOException {
        if (leftNode instanceof Releasable)
            ((Releasable) leftNode).releaseAll();
        if (listNode instanceof Releasable)
            ((Releasable) listNode).releaseAll();
        if (rightNode instanceof Releasable)
            ((Releasable) rightNode).releaseAll();
    }

    public VersionedMapNode replace(VersionedListNode listNode)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }

    public VersionedMapNode replace(int level, VersionedListNode listNode, Comparable key)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }

    public VersionedMapNode replace(VersionedMapNode leftNode, VersionedListNode listNode, VersionedMapNode rightNode)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }

    public VersionedMapNode replaceLeft(VersionedMapNode leftNode)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }

    public VersionedMapNode replaceLeft(int level, VersionedMapNode leftNode)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }

    public VersionedMapNode replaceRight(VersionedMapNode rightNode)
            throws IOException {
        thisNode.releaseLocal();
        return new VersionedMapNodeImpl(thisNode.getRegistry(), level, leftNode, listNode, rightNode, key);
    }
}

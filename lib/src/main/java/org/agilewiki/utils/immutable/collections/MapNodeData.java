package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;

import static java.lang.Math.min;

/**
 * The durable data elements of a map node.
 */
public class MapNodeData {

    /**
     * The node which holds this data.
     */
    public final MapNode thisNode;

    /**
     * Composite node depth--see AA Tree algorithm.
     */
    public final int level;

    /**
     * Left subtree node.
     */
    public final MapNode leftNode;

    /**
     * The list of the node.
     */
    public final ListNode listNode;

    /**
     * Right subtree node.
     */
    public final MapNode rightNode;

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
    public MapNodeData(MapNode thisNode) {
        this.thisNode = thisNode;
        this.level = 0;
        this.leftNode = thisNode;
        this.listNode = thisNode.factory.nilList;
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
    public MapNodeData(MapNode thisNode,
                       int level,
                       MapNode leftNode,
                       ListNode listNode,
                       MapNode rightNode,
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
    public MapNodeData(MapNode thisNode, ByteBuffer byteBuffer) {
        this.thisNode = thisNode;
        level = byteBuffer.getInt();
        FactoryRegistry factoryRegistry = thisNode.factory.factoryRegistry;
        ImmutableFactory f = factoryRegistry.readId(byteBuffer);
        leftNode = (MapNode) f.deserialize(byteBuffer);
        f = factoryRegistry.readId(byteBuffer);
        listNode = (ListNode) f.deserialize(byteBuffer);
        f = factoryRegistry.readId(byteBuffer);
        rightNode = (MapNode) f.deserialize(byteBuffer);
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
    public ListNode getList(Comparable key) {
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
    public MapNode skew() {
        if (isNil() || leftNode.isNil())
            return thisNode;
        MapNodeData leftData = leftNode.getData();
        if (leftData.level == level) {
            MapNode t = new MapNode(
                    thisNode.factory,
                    level,
                    leftData.rightNode,
                    listNode,
                    rightNode,
                    key);
            return new MapNode(
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
    public MapNode split() {
        if (isNil() || rightNode.isNil())
            return thisNode;
        MapNodeData rightData = rightNode.getData();
        if (rightData.rightNode.isNil())
            return thisNode;
        if (level == rightData.rightNode.getData().level) {
            MapNode t = new MapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode,
                    rightData.leftNode,
                    key);
            MapNode r = new MapNode(
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
     * @param key   The key of the list.
     * @param ndx   Where to add the value.
     * @param value The value to be added.
     * @return The revised root node.
     */
    public MapNode add(Comparable key, int ndx, Object value) {
        MapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new MapNode(
                    thisNode.factory,
                    level,
                    leftNode.add(key, ndx, value),
                    listNode,
                    rightNode,
                    this.key);
        } else if (c == 0) {
            return new MapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode.add(ndx, value),
                    rightNode,
                    this.key);
        } else {
            t = new MapNode(
                    thisNode.factory,
                    level,
                    leftNode,
                    listNode,
                    rightNode.add(key, ndx, value),
                    this.key);
        }
        return t.getData().skew().getData().split();
    }

    private MapNode successor() {
        return rightNode.getData().leftMost();
    }

    private MapNode leftMost() {
        if (!leftNode.isNil())
            return leftNode.getData().leftMost();
        return thisNode;
    }

    private MapNode predecessor() {
        return leftNode.getData().rightMost();
    }

    private MapNode rightMost() {
        if (!rightNode.isNil())
            return rightNode.getData().rightMost();
        return thisNode;
    }

    private MapNode decreaseLevel() {
        MapNodeData rd = rightNode.getData();
        int shouldBe = min(leftNode.getData().level, rd.level) + 1;
        if (shouldBe < level) {
            MapNode r;
            if (shouldBe < rd.level)
                r = new MapNode(
                        thisNode.factory,
                        shouldBe,
                        rd.leftNode,
                        rd.listNode,
                        rd.rightNode,
                        rd.key);
            else
                r = rightNode;
            return new MapNode(
                    thisNode.factory,
                    shouldBe,
                    leftNode,
                    listNode,
                    r,
                    key);
        }
        return thisNode;
    }

    public MapNode remove(Comparable key) {
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        MapNode t = thisNode;
        if (c > 0) {
            MapNode r = rightNode.remove(key);
            if (r != rightNode)
                t = new MapNode(thisNode.factory, level, leftNode, listNode, r, this.key);
        } else if (c < 0) {
            MapNode l = leftNode.remove(key);
            if (l != leftNode)
                t = new MapNode(thisNode.factory, level, l, listNode, rightNode, this.key);
        } else {
            MapNode nil = thisNode.factory.nilMap;
            if (leftNode.isNil() && rightNode.isNil()) {
                return nil;
            }
            if (leftNode.isNil()) {
                MapNode l = successor();
                MapNodeData ld = l.getData();
                t = new MapNode(thisNode.factory, level, nil, ld.listNode, rightNode.remove(ld.key), ld.key);
            } else {
                MapNode l = predecessor();
                MapNodeData ld = l.getData();
                t = new MapNode(thisNode.factory, level, leftNode.remove(ld.key), ld.listNode, rightNode, ld.key);
            }
        }
        t = t.getData().decreaseLevel().getData().skew();
        MapNodeData td = t.getData();
        MapNode r = td.rightNode.getData().skew();
        if (!r.isNil()) {
            MapNodeData rd = r.getData();
            MapNode rr = rd.rightNode.getData().skew();
            if (rd.rightNode != rr) {
                r = new MapNode(thisNode.factory, rd.level, rd.leftNode, rd.listNode, rr, rd.key);
            }
        }
        if (r != td.rightNode) {
            t = new MapNode(thisNode.factory, td.level, td.leftNode, td.listNode, r, td.key);
        }
        t = t.getData().split();
        r = t.getData().rightNode.getData().split();
        td = t.getData();
        if (r != td.rightNode) {
            t = new MapNode(thisNode.factory, td.level, td.leftNode, td.listNode, r, td.key);
        }
        return t;
    }

    /**
     * Delete a value from the list.
     *
     * @param key The key of the list.
     * @param ndx The index of the value.
     * @return The revised node.
     */
    public MapNode remove(Comparable key, int ndx) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (isNil())
            return thisNode;
        int c = key.compareTo(this.key);
        if (c < 0) {
            MapNode n = leftNode.remove(key, ndx);
            if (n == leftNode)
                return thisNode;
            return new MapNode(thisNode.factory, level, n, listNode, rightNode, this.key);
        }
        if (c > 0) {
            MapNode n = rightNode.remove(key, ndx);
            if (n == rightNode)
                return thisNode;
            return new MapNode(thisNode.factory, level, leftNode, listNode, n, this.key);
        }
        ListNode n = listNode.remove(ndx);
        if (n == listNode)
            return thisNode;
        if (n.isNil())
            return remove(key);
        return new MapNode(thisNode.factory, level, leftNode, n, rightNode, this.key);
    }

    /**
     * Replace the list entries with a single value.
     *
     * @param key   The key of the list.
     * @param value The new value.
     * @return The revised node.
     */
    public MapNode set(Comparable key, Object value) {
        int c = key.compareTo(this.key);
        if (c < 0) {
            MapNode n = leftNode.set(key, value);
            return new MapNode(thisNode.factory, level, n, listNode, rightNode, this.key);
        } else if (c == 0) {
            ListNode n = thisNode.factory.nilList.add(value);
            return new MapNode(thisNode.factory, level, leftNode, n, rightNode, this.key);
        } else {
            MapNode n = rightNode.set(key, value);
            return new MapNode(thisNode.factory, level, leftNode, listNode, n, this.key);
        }
    }

    /**
     * Builds a set of all keys with non-empty lists.
     *
     * @param keys The set being built.
     */
    public void flatKeys(NavigableSet<Comparable> keys) {
        if (isNil())
            return;
        leftNode.getData().flatKeys(keys);
        if (!listNode.isEmpty())
            keys.add(key);
        rightNode.getData().flatKeys(keys);
    }

    /**
     * Builds a map of all the keys and values.
     *
     * @param map The map being built.
     */
    public void flatMap(NavigableMap<Comparable, List> map) {
        if (isNil())
            return;
        leftNode.getData().flatMap(map);
        if (!listNode.isEmpty())
            map.put(key, listNode.flatList());
        rightNode.getData().flatMap(map);
    }

    protected MapNode addList(Comparable key, ListNode listNode) {
        if (listNode.isNil())
            return thisNode;
        if (isNil()) {
            return new MapNode(
                    thisNode.factory,
                    1,
                    thisNode.factory.nilMap,
                    listNode,
                    thisNode.factory.nilMap,
                    key);
        }
        MapNode t;
        int c = key.compareTo(this.key);
        if (c < 0) {
            t = new MapNode(
                    thisNode.factory,
                    level,
                    leftNode.getData().addList(key, listNode),
                    listNode,
                    rightNode,
                    key);
        } else if (c == 0) {
            throw new IllegalArgumentException("duplicate key not supported");
        } else {
            t = new MapNode(
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
     * Returns the count of all the keys in the map.
     *
     * @return The count of all the keys in the map.
     */
    public int totalSize() {
        if (isNil())
            return 0;
        return leftNode.totalSize() + 1 + rightNode.totalSize();
    }

    /**
     * Returns the count of all the keys.
     *
     * @return The size of the map.
     */
    public int size() {
        return totalSize();
    }

    /**
     * Returns the smallest key.
     *
     * @return The smallest key, or null.
     */
    public Comparable firstKey() {
        if (isNil())
            return null;
        Comparable k = leftNode.firstKey();
        if (k != null)
            return k;
        return key;
    }

    /**
     * Returns the largest key.
     *
     * @return The largest key, or null.
     */
    public Comparable lastKey() {
        if (isNil())
            return null;
        Comparable k = rightNode.lastKey();
        if (k != null)
            return k;
        return key;
    }

    /**
     * Returns the next greater key.
     *
     * @param key The given key.
     * @return The next greater key, or null.
     */
    public Comparable higherKey(Comparable key) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c <= 0) {
            Comparable k = leftNode.higherKey(key);
            if (k != null)
                return k;
        }
        if (c < 0)
            return this.key;
        return rightNode.higherKey(key);
    }

    /**
     * Returns the key that is greater than or equal to the given key.
     *
     * @param key The given key.
     * @return The key greater than or equal to the given key, or null.
     */
    public Comparable ceilingKey(Comparable key) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c < 0) {
            Comparable k = leftNode.ceilingKey(key);
            if (k != null)
                return k;
        }
        if (c <= 0)
            return this.key;
        return rightNode.ceilingKey(key);
    }

    /**
     * Returns the next smaller key.
     *
     * @param key The given key.
     * @return The next smaller key, or null.
     */
    public Comparable lowerKey(Comparable key) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c >= 0) {
            Comparable k = rightNode.lowerKey(key);
            if (k != null)
                return k;
        }
        if (c > 0)
            return this.key;
        return leftNode.lowerKey(key);
    }

    /**
     * Returns the key that is smaller than or equal to the given key.
     *
     * @param key The given key.
     * @return The key smaller than or equal to the given key, or null.
     */
    public Comparable floorKey(Comparable key) {
        if (isNil())
            return null;
        int c = key.compareTo(this.key);
        if (c > 0) {
            Comparable k = rightNode.floorKey(key);
            if (k != null)
                return k;
        }
        if (c >= 0)
            return this.key;
        return leftNode.floorKey(key);
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

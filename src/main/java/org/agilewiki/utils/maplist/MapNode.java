package org.agilewiki.utils.maplist;

/**
 * A node in an
 * <a href="http://en.wikipedia.org/wiki/AA_tree">AA Tree</a>
 * representing a map of versioned lists.
 */
public class MapNode {
    /**
     * The root node of an empty tree.
     */
    public final static MapNode MAP_NIL = new MapNode();

    protected int level;
    protected MapNode leftNode;
    protected MapNode rightNode;
    protected ListNode value;
    protected Comparable key;

    protected MapNode() {
        leftNode = this;
        rightNode = this;
        value = ListNode.LIST_NIL;
    }

    protected MapNode(int level,
                      MapNode leftNode,
                      MapNode rightNode,
                      ListNode value,
                      Comparable key) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.value = value;
        this.key = key;
    }

    protected boolean isNil() {
        return this == MAP_NIL;
    }

    protected ListNode getList(Comparable key) {
        if (isNil())
            return value;
        int c = key.compareTo(this.key);
        if (c < 0)
            return leftNode.getList(key);
        if (c == 0)
            return value;
        return rightNode.getList(key);
    }

    /**
     * Returns the count of all the values in the list,
     * including deleted values.
     *
     * @param key    The list identifier.
     * @return The count of all the values in the list.
     */
    public int maxSize(Comparable key) {
        return getList(key).maxSize();
    }

    protected MapNode skew() {
        if (isNil())
            return this;
        if (leftNode.isNil())
            return this;
        if (leftNode.level == level) {
            MapNode l = leftNode;
            leftNode = l.rightNode;
            l.rightNode = this;
            return l;
        } else
            return this;
    }

    protected MapNode split() {
        if (isNil())
            return this;
        if (rightNode.isNil() || rightNode.rightNode.isNil())
            return this;
        if (level == rightNode.rightNode.level) {
            MapNode r = rightNode;
            rightNode = r.leftNode;
            r.leftNode = this;
            r.level += 1;
            return r;
        }
        return this;
    }

    /**
     * Add a non-null value to the end of the list.
     * After calling add, a previously created accessor becomes invalid.
     *
     * @param key   The key of the list.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public MapNode add(Comparable key, Object value, long time) {
        return add(key, -1, value, time);
    }

    /**
     * Add a non-null value to the list.
     * After calling add, a previously created accessor becomes invalid.
     *
     * @param key   The key of the list.
     * @param ndx   Where to add the value.
     * @param value The value to be added.
     * @param time  The time the value is added.
     * @return The revised root node.
     */
    public MapNode add(Comparable key, int ndx, Object value, long time) {
        return add(key, ndx, value, time, Integer.MAX_VALUE);
    }

    protected MapNode add(Comparable key, int ndx, Object value, long created, long deleted) {
        if (key == null)
            throw new IllegalArgumentException("key may not be null");
        if (value == null)
            throw new IllegalArgumentException("value may not be null");
        if (isNil()) {
            ListNode listNode = ListNode.LIST_NIL.add(ndx, value, created, deleted);
            return new MapNode(1, MAP_NIL, MAP_NIL, listNode, key);
        }
        int c = key.compareTo(this.key);
        if (c < 0)
            leftNode = leftNode.add(key, ndx, value, created, deleted);
        else if (c == 0) {
            this.value = this.value.add(ndx, value, created, deleted);
            return this;
        } else
            rightNode = rightNode.add(key, ndx, value, created, deleted);
        return skew().split();
    }
}

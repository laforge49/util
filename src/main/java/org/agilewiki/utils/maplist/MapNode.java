package org.agilewiki.utils.maplist;

import java.util.NavigableSet;
import java.util.TreeSet;

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
    protected ListNode listNode;
    protected Comparable key;

    protected MapNode() {
        leftNode = this;
        rightNode = this;
        listNode = ListNode.LIST_NIL;
    }

    protected MapNode(int level,
                      MapNode leftNode,
                      MapNode rightNode,
                      ListNode listNode,
                      Comparable key) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.listNode = listNode;
        this.key = key;
    }

    protected boolean isNil() {
        return this == MAP_NIL;
    }

    protected ListNode getList(Comparable key) {
        if (isNil())
            return listNode;
        int c = key.compareTo(this.key);
        if (c < 0)
            return leftNode.getList(key);
        if (c == 0)
            return listNode;
        return rightNode.getList(key);
    }

    /**
     * Returns the count of all the values in the list,
     * including deleted values.
     *
     * @param key The list identifier.
     * @return The count of all the values in the list.
     */
    public int maxSize(Comparable key) {
        return getList(key).maxSize();
    }

    /**
     * Returns a list accessor for the latest time.
     * But after calling add, a previously created accessor becomes invalid.
     *
     * @param key The key for the list.
     * @return A list accessor for the latest time.
     */
    public ListAccessor listAccessor(Comparable key) {
        return getList(key).listAccessor(key);
    }

    /**
     * Returns a list accessor for the given time.
     * But after calling add, a previously created accessor becomes invalid.
     *
     * @param key  The key for the list.
     * @param time The time of the query.
     * @return A list accessor for the given time.
     */
    public ListAccessor listAccessor(Comparable key, long time) {
        return getList(key).listAccessor(key, time);
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
        if (isNil()) {
            ListNode listNode = ListNode.LIST_NIL.add(ndx, value, created, deleted);
            return new MapNode(1, MAP_NIL, MAP_NIL, listNode, key);
        }
        int c = key.compareTo(this.key);
        if (c < 0)
            leftNode = leftNode.add(key, ndx, value, created, deleted);
        else if (c == 0) {
            this.listNode = this.listNode.add(ndx, value, created, deleted);
            return this;
        } else
            rightNode = rightNode.add(key, ndx, value, created, deleted);
        return skew().split();
    }

    /**
     * Mark a value as deleted.
     *
     * @param key  The key of the list.
     * @param ndx  The index of the value.
     * @param time The time of the deletion.
     * @return The deleted value.
     */
    public Object remove(Comparable key, int ndx, long time) {
        return getList(key).remove(ndx, time);
    }

    /**
     * Perform a complete copy.
     *
     * @return A complete, but shallow copy of the list.
     */
    public ListNode copyList(Comparable key) {
        return getList(key).copyList();
    }

    /**
     * Copy everything except what was deleted before a given time.
     * (This is a shallow copy, as the values in the list are not copied.)
     *
     * @param time The given time.
     * @return A shortened copy of the list without some historical values.
     */
    public ListNode copyList(Comparable key, long time) {
        return getList(key).copyList(time);
    }

    protected void flatKeys(NavigableSet keys, long time) {
        if (isNil())
            return;
        leftNode.flatKeys(keys, time);
        if (!listNode.isEmpty(time))
            keys.add(key);
        rightNode.flatKeys(keys, time);
    }

    /**
     * Returns a set of all keys with non-empty lists for the given time.
     *
     * @param time    The time of the query.
     * @return A set of the keys with content at the time of the query.
     */
    public NavigableSet flatKeys(long time) {
        NavigableSet keys = new TreeSet<>();
        flatKeys(keys, time);
        return keys;
    }
}

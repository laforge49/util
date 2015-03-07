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
}

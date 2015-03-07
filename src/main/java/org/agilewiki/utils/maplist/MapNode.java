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
}

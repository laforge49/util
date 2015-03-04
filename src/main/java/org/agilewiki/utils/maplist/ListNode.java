package org.agilewiki.utils.maplist;

/**
 * A node in an AA Tree representing a versioned list.
 */
public class ListNode {
    private int level;
    private ListNode leftNode;
    private ListNode rightNode;
    private final Object value;
    private final long created;
    private long deleted;
    private int size;

    public final static ListNode LIST_NIL = new ListNode();

    private ListNode() {
        leftNode = this;
        rightNode = this;
        value = null;
        created = 0L;
    }

    private ListNode(int level,
                     ListNode leftNode,
                     ListNode rightNode,
                     Object value,
                     long created,
                     long deleted) {
        this.level = level;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.value = value;
        this.created = created;
        this.deleted = deleted;
        size = leftNode.size + rightNode.size + 1;
    }

    /**
     * Returns the count of all the values in the list,
     * including deleted values.
     *
     * @return The count of all the values in the list.
     */
    public int maxSize() {
        return size;
    }

    /**
     * Returns a value if it is in range and the value exists for the given time.
     *
     * @param ndx  The index of the selected value.
     * @param time The time of the query.
     * @return A value, or null.
     */
    public Object get(int ndx, long time) {
        if (ndx < 0 || ndx >= size)
            return null; //out of range
        int leftSize = leftNode.size;
        if (ndx < leftSize)
            return leftNode.get(ndx, time);
        if (ndx > leftSize)
            return rightNode.get(ndx - leftSize - 1, time);
        if (time < created || time >= deleted)
            return null;
        return value;
    }
}

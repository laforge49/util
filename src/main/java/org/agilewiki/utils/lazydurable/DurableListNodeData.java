package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * The durable data elements of a list node.
 */
public class DurableListNodeData {
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
    public final LazyDurableListNode leftNode;

    /**
     * The value of the node.
     */
    public final Object value;

    /**
     * Right subtree node.
     */
    public final LazyDurableListNode rightNode;

    /**
     * The factory for the value.
     */
    protected final LazyDurableFactory valueFactory;

    /**
     * Create the nil node data.
     *
     * @param thisNode    The nil node.
     */
    public DurableListNodeData(LazyDurableListNode thisNode) {
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
     * @param level        Composite node depth--see AA Tree algorithm.
     * @param totalSize    Number of nodes in this subtree.
     * @param created      Creation time of this node.
     * @param deleted      Deletion time of this node.
     * @param leftNode     Left subtree node.
     * @param value        The value of the node.
     * @param rightNode    Right subtree node.
     */
    public DurableListNodeData(int level,
                               int totalSize,
                               long created,
                               long deleted,
                               LazyDurableListNode leftNode,
                               Object value,
                               LazyDurableListNode rightNode) {
        this.level = level;
        this.totalSize = totalSize;
        this.created = created;
        this.deleted = deleted;
        this.leftNode = leftNode;
        this.value = value;
        this.rightNode = rightNode;
        this.valueFactory = FactoryRegistry.getDurableFactory(value);
    }

    /**
     * Create non-nill node data.
     *
     * @param byteBuffer    Holds the serialized data.
     */
    public DurableListNodeData(ByteBuffer byteBuffer) {
        level = byteBuffer.getInt();
        totalSize = byteBuffer.getInt();
        created = byteBuffer.getLong();
        deleted = byteBuffer.getLong();
        LazyDurableFactory f = FactoryRegistry.readId(byteBuffer);
        leftNode = (LazyDurableListNode) f.deserialize(byteBuffer);
        valueFactory = FactoryRegistry.readId(byteBuffer);
        value = valueFactory.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        rightNode = (LazyDurableListNode) f.deserialize(byteBuffer);
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
     * @param time    The time of the query.
     * @return True if the value currently exists.
     */
    public boolean exists(long time) {
        return time >= created && time < deleted;
    }
}

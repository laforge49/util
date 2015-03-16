package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

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

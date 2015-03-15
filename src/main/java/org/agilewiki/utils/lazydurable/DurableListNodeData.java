package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * The durable data elements of a list node.
 */
public class DurableListNodeData {
    public final int level;
    public final int totalSize;
    public final long created;
    public final long deleted;
    public final LazyDurableListNode leftNode;
    public final Object value;
    public final LazyDurableListNode rightNode;

    protected final LazyDurableFactory valueFactory;

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

    public boolean isNil() {
        return level == 0;
    }

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

    public boolean exists(long time) {
        return time >= created && time < deleted;
    }
}

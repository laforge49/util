package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class DurableListNodeFactory implements DurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new DurableListNodeFactory());
    }

    @Override
    public DurableFactory getDurableFactory(Object durable) {
        if (((DurableListNode) durable).isNil())
            return FactoryRegistry.getDurableFactory(DurableListNode.LIST_NIL_ID);
        return this;
    }

    @Override
    public char getId() {
        return DurableListNode.LIST_NODE_ID;
    }

    @Override
    public Class getDurableClass() {
        return DurableListNode.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((DurableListNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((DurableListNode) durable).serialize(byteBuffer);
    }

    @Override
    public DurableListNode deserialize(ByteBuffer byteBuffer) {
        int level = byteBuffer.getInt();
        long created = byteBuffer.getLong();
        long deleted = byteBuffer.getLong();
        DurableFactory f = FactoryRegistry.readId(byteBuffer);
        DurableListNode leftNode = (DurableListNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        Object value = f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        DurableListNode rightNode = (DurableListNode) f.deserialize(byteBuffer);
        return new DurableListNode(level, leftNode, rightNode, value, created, deleted);
    }
}

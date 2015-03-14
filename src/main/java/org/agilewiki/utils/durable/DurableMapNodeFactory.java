package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class DurableMapNodeFactory implements DurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new DurableMapNodeFactory());
    }

    @Override
    public DurableFactory getDurableFactory(Object durable) {
        if (((DurableMapNode) durable).isNil())
            return FactoryRegistry.getDurableFactory(DurableMapNode.MAP_NIL_ID);
        return this;
    }

    @Override
    public char getId() {
        return DurableMapNode.MAP_NODE_ID;
    }

    @Override
    public Class getDurableClass() {
        return DurableMapNode.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((DurableMapNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((DurableMapNode) durable).serialize(byteBuffer);
    }

    @Override
    public DurableMapNode deserialize(ByteBuffer byteBuffer) {
        int level = byteBuffer.getInt();
        DurableFactory keyFactory = FactoryRegistry.readId(byteBuffer);
        Comparable key = (Comparable) keyFactory.deserialize(byteBuffer);
        DurableFactory f = FactoryRegistry.readId(byteBuffer);
        DurableMapNode leftNode = (DurableMapNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        DurableListNode listNode = (DurableListNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        DurableMapNode rightNode = (DurableMapNode) f.deserialize(byteBuffer);
        return new DurableMapNode(level, leftNode, rightNode, listNode, key, keyFactory);
    }
}

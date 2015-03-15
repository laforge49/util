package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class LazyDurableMapNodeFactory implements LazyDurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new LazyDurableMapNodeFactory());
    }

    @Override
    public LazyDurableFactory getDurableFactory(Object durable) {
        if (((LazyDurableMapNode) durable).isNil())
            return FactoryRegistry.getDurableFactory(LazyDurableMapNode.MAP_NIL_ID);
        return this;
    }

    @Override
    public char getId() {
        return LazyDurableMapNode.MAP_NODE_ID;
    }

    @Override
    public Class getDurableClass() {
        return LazyDurableMapNode.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((LazyDurableMapNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((LazyDurableMapNode) durable).serialize(byteBuffer);
    }

    @Override
    public LazyDurableMapNode deserialize(ByteBuffer byteBuffer) {
        int durableLength = byteBuffer.getInt();
        int level = byteBuffer.getInt();
        LazyDurableFactory keyFactory = FactoryRegistry.readId(byteBuffer);
        Comparable key = (Comparable) keyFactory.deserialize(byteBuffer);
        LazyDurableFactory f = FactoryRegistry.readId(byteBuffer);
        LazyDurableMapNode leftNode = (LazyDurableMapNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        LazyDurableListNode listNode = (LazyDurableListNode) f.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        LazyDurableMapNode rightNode = (LazyDurableMapNode) f.deserialize(byteBuffer);
        return new LazyDurableMapNode(
                durableLength, level, leftNode, rightNode, listNode, key, keyFactory);
    }
}

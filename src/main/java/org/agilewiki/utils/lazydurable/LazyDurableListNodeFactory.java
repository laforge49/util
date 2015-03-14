package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class LazyDurableListNodeFactory implements LazyDurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new LazyDurableListNodeFactory());
    }

    @Override
    public LazyDurableFactory getDurableFactory(Object durable) {
        if (((LazyDurableListNode) durable).isNil())
            return FactoryRegistry.getDurableFactory(LazyDurableListNode.LIST_NIL_ID);
        return this;
    }

    @Override
    public char getId() {
        return LazyDurableListNode.LIST_NODE_ID;
    }

    @Override
    public Class getDurableClass() {
        return LazyDurableListNode.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((LazyDurableListNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((LazyDurableListNode) durable).serialize(byteBuffer);
    }

    @Override
    public LazyDurableListNode deserialize(ByteBuffer byteBuffer) {
        int level = byteBuffer.getInt();
        long created = byteBuffer.getLong();
        long deleted = byteBuffer.getLong();
        LazyDurableFactory f = FactoryRegistry.readId(byteBuffer);
        LazyDurableListNode leftNode = (LazyDurableListNode) f.deserialize(byteBuffer);
        LazyDurableFactory valueFactory = FactoryRegistry.readId(byteBuffer);
        Object value = valueFactory.deserialize(byteBuffer);
        f = FactoryRegistry.readId(byteBuffer);
        LazyDurableListNode rightNode = (LazyDurableListNode) f.deserialize(byteBuffer);
        return new LazyDurableListNode(level, leftNode, rightNode, value, created, deleted, valueFactory);
    }
}

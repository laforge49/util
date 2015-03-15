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
        return new LazyDurableListNode(byteBuffer);
    }
}

package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how a nil list node is serialized / deserialized.
 */
public class NilListNodeFactory implements LazyDurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new NilListNodeFactory());
    }

    @Override
    public char getId() {
        return LazyDurableListNode.LIST_NIL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((LazyDurableListNode) durable).isNil())
            throw new IllegalArgumentException("The immutable object is not a nil list node");
    }

    @Override
    public int getDurableLength(Object durable) {
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public LazyDurableListNode deserialize(ByteBuffer byteBuffer) {
        return LazyDurableListNode.LIST_NIL;
    }
}

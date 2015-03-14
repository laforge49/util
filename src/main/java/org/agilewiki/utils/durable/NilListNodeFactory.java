package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class NilListNodeFactory implements DurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new NilListNodeFactory());
    }

    @Override
    public char getId() {
        return DurableListNode.LIST_NIL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((DurableListNode) durable).isNil())
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
    public DurableListNode deserialize(ByteBuffer byteBuffer) {
        return DurableListNode.LIST_NIL;
    }
}

package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a nil map node is serialized / deserialized.
 */
public class NilMapNodeFactory implements DurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new NilMapNodeFactory());
    }

    @Override
    public char getId() {
        return DurableMapNode.MAP_NIL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((DurableMapNode) durable).isNil())
            throw new IllegalArgumentException("The immutable object is not a nil map node");
    }

    @Override
    public int getDurableLength(Object durable) {
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public DurableMapNode deserialize(ByteBuffer byteBuffer) {
        return DurableMapNode.MAP_NIL;
    }
}

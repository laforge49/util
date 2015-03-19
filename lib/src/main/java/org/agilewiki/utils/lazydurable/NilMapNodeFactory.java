package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how a nil map node is serialized / deserialized.
 */
public class NilMapNodeFactory implements org.agilewiki.utils.lazydurable.LazyDurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new NilMapNodeFactory());
    }

    @Override
    public char getId() {
        return org.agilewiki.utils.lazydurable.LazyDurableMapNode.MAP_NIL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((org.agilewiki.utils.lazydurable.LazyDurableMapNode) durable).isNil())
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
    public org.agilewiki.utils.lazydurable.LazyDurableMapNode deserialize(ByteBuffer byteBuffer) {
        return org.agilewiki.utils.lazydurable.LazyDurableMapNode.MAP_NIL;
    }
}

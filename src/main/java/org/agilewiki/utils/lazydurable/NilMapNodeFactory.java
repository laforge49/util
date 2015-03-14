package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how a nil map node is serialized / deserialized.
 */
public class NilMapNodeFactory implements LazyDurableFactory {

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new NilMapNodeFactory());
    }

    @Override
    public char getId() {
        return LazyDurableMapNode.MAP_NIL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((LazyDurableMapNode) durable).isNil())
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
    public LazyDurableMapNode deserialize(ByteBuffer byteBuffer) {
        return LazyDurableMapNode.MAP_NIL;
    }
}

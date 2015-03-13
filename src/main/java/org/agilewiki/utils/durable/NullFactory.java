package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a null is serialized / deserialized.
 */
public class NullFactory implements DurableFactory {
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new NullFactory());
    }

    @Override
    public char getId() {
        return 'N';
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public int getDurableLength(Object durable) {
        return 0;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public Void deserialize(ByteBuffer byteBuffer) {
        return null;
    }
}

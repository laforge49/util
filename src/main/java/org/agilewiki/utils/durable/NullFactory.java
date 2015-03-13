package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a null is serialized / deserialized.
 */
public class NullFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char NULL_ID = 'N';

    /**
     * Register this factory.
     *
     * @param factoryRegistry    The registry.
     */
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new NullFactory());
    }

    @Override
    public char getId() {
        return NULL_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (durable != null)
            throw new IllegalArgumentException("The immutable object is not null");
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

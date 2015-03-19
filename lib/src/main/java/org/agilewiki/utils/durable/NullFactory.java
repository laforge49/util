package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how null is serialized / deserialized.
 */
public class NullFactory implements org.agilewiki.utils.durable.DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char NULL_ID = 'N';

    /**
     * Register this factory.
     */
    public static void register() {
        org.agilewiki.utils.durable.FactoryRegistry.register(new NullFactory());
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
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public Void deserialize(ByteBuffer byteBuffer) {
        return null;
    }
}

package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how a Long is serialized / deserialized.
 */
public class LongFactory implements org.agilewiki.utils.lazydurable.LazyDurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char LONG_ID = 'L';

    /**
     * Register this factory.
     */
    public static void register() {
        org.agilewiki.utils.lazydurable.FactoryRegistry.register(new LongFactory());
    }

    @Override
    public char getId() {
        return LONG_ID;
    }

    @Override
    public Class getDurableClass() {
        return Long.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        if (durable == null)
            return 2;
        return 10;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        byteBuffer.putLong((Long) durable);
    }

    @Override
    public Long deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getLong();
    }
}

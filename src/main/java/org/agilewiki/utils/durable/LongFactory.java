package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a Long is serialized / deserialized.
 */
public class LongFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char LONG_ID = 'L';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new LongFactory());
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
            return 0;
        return 8;
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

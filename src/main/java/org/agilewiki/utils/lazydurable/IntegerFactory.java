package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how an Integer is serialized / deserialized.
 */
public class IntegerFactory implements LazyDurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char INTEGER_ID = 'I';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new IntegerFactory());
    }

    @Override
    public char getId() {
        return INTEGER_ID;
    }

    @Override
    public Class getDurableClass() {
        return Integer.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        if (durable == null)
            return 2;
        return 6;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        byteBuffer.putInt((Integer) durable);
    }

    @Override
    public Integer deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }
}

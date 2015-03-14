package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how an Float is serialized / deserialized.
 */
public class FloatFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char FLOAT_ID = 'F';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new FloatFactory());
    }

    @Override
    public char getId() {
        return FLOAT_ID;
    }

    @Override
    public Class getDurableClass() {
        return Float.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        if (durable == null)
            return 2;
        return 6;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        byteBuffer.putFloat((Float) durable);
    }

    @Override
    public Float deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getFloat();
    }
}

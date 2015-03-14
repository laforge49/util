package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a Double is serialized / deserialized.
 */
public class DoubleFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char DOUBLE_ID = 'D';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new DoubleFactory());
    }

    @Override
    public char getId() {
        return DOUBLE_ID;
    }

    @Override
    public Class getDurableClass() {
        return Double.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        if (durable == null)
            return 2;
        return 10;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        byteBuffer.putDouble((Double) durable);
    }

    @Override
    public Double deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getDouble();
    }
}

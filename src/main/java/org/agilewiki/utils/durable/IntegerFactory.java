package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how an Integer is serialized / deserialized.
 */
public class IntegerFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char INTEGER_ID = 'N';

    /**
     * Register this factory.
     *
     * @param factoryRegistry    The registry.
     */
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new IntegerFactory());
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
            return 0;
        return 4;
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

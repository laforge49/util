package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class BooleanFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char BOOLEAN_ID = 'B';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new BooleanFactory());
    }

    @Override
    public DurableFactory getDurableFactory(Object durable) {
        return FactoryRegistry.getDurableFactory((Boolean) durable ? 't' : 'f');
    }

    @Override
    public char getId() {
        return BOOLEAN_ID;
    }

    @Override
    public Class getDurableClass() {
        return Boolean.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean deserialize(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }
}

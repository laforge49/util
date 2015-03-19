package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class BooleanFactory implements org.agilewiki.utils.durable.DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char BOOLEAN_ID = 'B';

    /**
     * Register this factory.
     */
    public static void register() {
        org.agilewiki.utils.durable.FactoryRegistry.register(new BooleanFactory());
    }

    @Override
    public org.agilewiki.utils.durable.DurableFactory getDurableFactory(Object durable) {
        return org.agilewiki.utils.durable.FactoryRegistry.getDurableFactory(
                (Boolean) durable ? TrueFactory.TRUE_ID : FalseFactory.FALSE_ID);
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

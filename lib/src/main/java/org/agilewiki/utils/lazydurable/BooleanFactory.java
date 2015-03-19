package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class BooleanFactory implements org.agilewiki.utils.lazydurable.LazyDurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char BOOLEAN_ID = 'B';

    /**
     * Register this factory.
     */
    public static void register() {
        org.agilewiki.utils.lazydurable.FactoryRegistry.register(new BooleanFactory());
    }

    @Override
    public org.agilewiki.utils.lazydurable.LazyDurableFactory getDurableFactory(Object durable) {
        return org.agilewiki.utils.lazydurable.FactoryRegistry.getDurableFactory(
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

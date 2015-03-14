package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class BooleanFactory implements LazyDurableFactory {
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
    public LazyDurableFactory getDurableFactory(Object durable) {
        return FactoryRegistry.getDurableFactory(
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

package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class BooleanFactory implements DurableFactory {
    protected static FactoryRegistry _factoryRegistry;

    /**
     * The durable id for this factory.
     */
    public final static char BOOLEAN_ID = 'B';

    /**
     * Register this factory.
     *
     * @param factoryRegistry    The registry.
     */
    public static void register(FactoryRegistry factoryRegistry) {
        _factoryRegistry = factoryRegistry;
        factoryRegistry.register(new BooleanFactory());
    }

    @Override
    public DurableFactory getDurableFactory(Object durable) {
        return _factoryRegistry.getDurableFactory((Boolean) durable ? 't' : 'f');
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
        return 0;
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

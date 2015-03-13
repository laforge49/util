package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how false is serialized / deserialized.
 */
public class FalseFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char FALSE_ID = 'f';

    /**
     * Register this factory.
     *
     * @param factoryRegistry    The registry.
     */
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new FalseFactory());
    }

    @Override
    public char getId() {
        return FALSE_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!durable.equals(false))
            throw new IllegalArgumentException("The immutable object is not false");
    }

    @Override
    public int getDurableLength(Object durable) {
        return 0;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public Boolean deserialize(ByteBuffer byteBuffer) {
        return false;
    }
}

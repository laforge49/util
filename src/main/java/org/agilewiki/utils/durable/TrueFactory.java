package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class TrueFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char TRUE_ID = 't';

    /**
     * Register this factory.
     *
     * @param factoryRegistry    The registry.
     */
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new TrueFactory());
    }

    @Override
    public char getId() {
        return TRUE_ID;
    }

    @Override
    public Class getDurableClass() {
        return Boolean.class;
    }

    @Override
    public void match(Object durable) {
        if (!durable.equals(true))
            throw new IllegalArgumentException("The immutable object is not true");
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
        return true;
    }
}

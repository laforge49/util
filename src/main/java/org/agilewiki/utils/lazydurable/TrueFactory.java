package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class TrueFactory implements LazyDurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char TRUE_ID = 't';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new TrueFactory());
    }

    @Override
    public char getId() {
        return TRUE_ID;
    }

    @Override
    public Class getDurableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!durable.equals(true))
            throw new IllegalArgumentException("The immutable object is not true");
    }

    @Override
    public int getDurableLength(Object durable) {
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public Boolean deserialize(ByteBuffer byteBuffer) {
        return true;
    }
}

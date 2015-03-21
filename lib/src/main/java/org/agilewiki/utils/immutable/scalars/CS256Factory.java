package org.agilewiki.utils.immutable.scalars;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how a CS256 is serialized / deserialized.
 */
public class CS256Factory extends BaseFactory {
    public CS256Factory(FactoryRegistry factoryRegistry, char id) {
        super(factoryRegistry, id);
    }

    @Override
    public Class getImmutableClass() {
        return CS256.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        if (immutable == null)
            return 2;
        return 34;
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        byteBuffer.asLongBuffer().put(((CS256) immutable).toLongArray());
        byteBuffer.position(byteBuffer.position() + 32);
    }

    @Override
    public Object deserialize(ByteBuffer byteBuffer) {
        long[] longs = new long[4];
        byteBuffer.asLongBuffer().get(longs);
        byteBuffer.position(byteBuffer.position() + 32);
        return new CS256(longs);
    }
}

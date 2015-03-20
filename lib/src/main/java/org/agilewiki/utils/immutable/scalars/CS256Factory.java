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
        byteBuffer.put(((CS256) immutable).toByteArray());
    }

    @Override
    public Object deserialize(ByteBuffer byteBuffer) {
        return new CS256(byteBuffer);
    }
}

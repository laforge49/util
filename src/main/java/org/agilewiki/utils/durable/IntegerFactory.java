package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how an Integer is serialized / deserialized.
 */
public class IntegerFactory implements DurableFactory {
    @Override
    public char getId() {
        return 'I';
    }

    @Override
    public Class getDurableClass() {
        return Integer.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return 4;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        byteBuffer.putInt((Integer) durable);
    }

    @Override
    public Integer deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }
}

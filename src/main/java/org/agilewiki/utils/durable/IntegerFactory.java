package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how an Integer is serialized / deserialized.
 */
public class IntegerFactory implements DurableFactory {
    public static void register(FactoryRegistry factoryRegistry) {
        factoryRegistry.register(new IntegerFactory());
    }

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
        match(durable);
        byteBuffer.putChar(getId());
        byteBuffer.putInt((Integer) durable);
    }

    @Override
    public Integer deserialize(ByteBuffer byteBuffer) {
        return byteBuffer.getInt();
    }
}

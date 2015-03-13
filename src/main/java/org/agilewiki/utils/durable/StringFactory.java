package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Defines how a String is serialized / deserialized.
 */
public class StringFactory implements DurableFactory {
    /**
     * The durable id for this factory.
     */
    public final static char STRING_ID = 'S';

    /**
     * Register this factory.
     */
    public static void register() {
        FactoryRegistry.register(new StringFactory());
    }

    @Override
    public char getId() {
        return STRING_ID;
    }

    @Override
    public Class getDurableClass() {
        return String.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        if (durable == null)
            return 0;
        return 4 + 2 * ((String) durable).length();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        String string = (String) durable;
        byteBuffer.putInt(string.length());
        CharBuffer charBuffer = byteBuffer.asCharBuffer();
        charBuffer.put(string);
        byteBuffer.position(byteBuffer.position() + 2 * charBuffer.position());
    }

    @Override
    public String deserialize(ByteBuffer byteBuffer) {
        int length = byteBuffer.getInt();
        char[] c = new char[length];
        CharBuffer charBuffer = byteBuffer.asCharBuffer();
        charBuffer.get(c);
        byteBuffer.position(byteBuffer.position() + 2 * charBuffer.position());
        return new String(c);
    }
}

package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class StringTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        String string1 = "abc";
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(string1);
        assertTrue(durableFactory1 instanceof StringFactory);
        assertEquals(12, durableFactory1.getDurableLength(string1));
        durableFactory1.writeDurable(string1, byteBuffer);
        assertEquals(12, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof StringFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(12, byteBuffer.position());
        assertTrue(object2 instanceof String);
        String string2 = (String) object2;
        assertEquals("abc", string2);
    }
}

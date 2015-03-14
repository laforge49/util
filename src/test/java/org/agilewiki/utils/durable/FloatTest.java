package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class FloatTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Float float1 = 42.0f;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(float1);
        assertTrue(durableFactory1 instanceof FloatFactory);
        assertEquals(6, durableFactory1.getDurableLength(float1));
        durableFactory1.writeDurable(float1, byteBuffer);
        assertEquals(6, byteBuffer.position());
        byteBuffer.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(durableFactory2 instanceof FloatFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer);
        assertEquals(6, byteBuffer.position());
        assertTrue(object2 instanceof Float);
        float float2 = (Float) object2;
        assertEquals(42.0f, float2);
    }
}

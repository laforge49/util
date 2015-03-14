package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class FloatTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Float float1 = 42.0f;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(float1);
        assertTrue(lazyDurableFactory1 instanceof FloatFactory);
        assertEquals(6, lazyDurableFactory1.getDurableLength(float1));
        lazyDurableFactory1.writeDurable(float1, byteBuffer);
        assertEquals(6, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof FloatFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(6, byteBuffer.position());
        assertTrue(object2 instanceof Float);
        float float2 = (Float) object2;
        assertEquals(42.0f, float2);
    }
}

package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class FalseTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Boolean boolean1 = false;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(boolean1);
        assertTrue(lazyDurableFactory1 instanceof FalseFactory);
        assertEquals(2, lazyDurableFactory1.getDurableLength(boolean1));
        lazyDurableFactory1.writeDurable(boolean1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof FalseFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertTrue(object2.equals(false));
    }
}

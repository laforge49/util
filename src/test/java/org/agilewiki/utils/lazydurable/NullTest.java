package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class NullTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        Integer integer1 = null;
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(integer1);
        assertTrue(lazyDurableFactory1 instanceof NullFactory);
        assertEquals(2, lazyDurableFactory1.getDurableLength(integer1));
        lazyDurableFactory1.writeDurable(integer1, byteBuffer);
        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof NullFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(2, byteBuffer.position());
        assertNull(object2);
    }
}

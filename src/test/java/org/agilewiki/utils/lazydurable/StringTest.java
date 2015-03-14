package org.agilewiki.utils.lazydurable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class StringTest extends TestCase {
    public void test() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        String string1 = "abc";
        LazyDurableFactory lazyDurableFactory1 = FactoryRegistry.getDurableFactory(string1);
        assertTrue(lazyDurableFactory1 instanceof StringFactory);
        assertEquals(12, lazyDurableFactory1.getDurableLength(string1));
        lazyDurableFactory1.writeDurable(string1, byteBuffer);
        assertEquals(12, byteBuffer.position());
        byteBuffer.flip();
        LazyDurableFactory lazyDurableFactory2 = FactoryRegistry.readId(byteBuffer);
        assertTrue(lazyDurableFactory2 instanceof StringFactory);
        Object object2 = lazyDurableFactory2.deserialize(byteBuffer);
        assertEquals(12, byteBuffer.position());
        assertTrue(object2 instanceof String);
        String string2 = (String) object2;
        assertEquals("abc", string2);
    }
}

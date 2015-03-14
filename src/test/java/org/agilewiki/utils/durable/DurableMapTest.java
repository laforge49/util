package org.agilewiki.utils.durable;

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class DurableMapTest extends TestCase {
    public void test() throws Exception {
        DurableMapNode m1 = DurableMapNode.MAP_NIL;
        DurableFactory durableFactory1 = FactoryRegistry.getDurableFactory(m1);
        assertTrue(durableFactory1 instanceof NilMapNodeFactory);
        assertEquals(2, durableFactory1.getDurableLength(m1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(durableFactory1.getDurableLength(m1));
        durableFactory1.writeDurable(m1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        DurableFactory durableFactory2 = FactoryRegistry.readId(byteBuffer1);
        assertTrue(durableFactory2 instanceof NilMapNodeFactory);
        Object object2 = durableFactory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(DurableMapNode.MAP_NIL));

        DurableMapNode m2 = m1;
        m2 = m2.add("a", "1", 1);
        m2 = m2.add("a", "2", 2);
        m2 = m2.add("a", "3", 3);
        DurableFactory durableFactory3 = FactoryRegistry.getDurableFactory(m2);
        assertTrue(durableFactory3 instanceof DurableMapNodeFactory);
        assertEquals(116, durableFactory3.getDurableLength(m2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(durableFactory3.getDurableLength(m2));
        durableFactory3.writeDurable(m2, byteBuffer2);
        assertEquals(116, byteBuffer2.position());
        byteBuffer2.flip();
        DurableFactory durableFactory4 = FactoryRegistry.readId(byteBuffer2);
        assertTrue(durableFactory4 instanceof DurableMapNodeFactory);
        Object object4 = durableFactory4.deserialize(byteBuffer2);
        assertEquals(116, byteBuffer2.position());
        assertEquals("123", String.join("", ((DurableMapNode) object4).getList("a").flatList(3)));
    }
}

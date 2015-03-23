package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Registry;

import java.nio.ByteBuffer;

public class MapDurableTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        MapNode m1 = registry.nilMap;
        ImmutableFactory factory1 = registry.getImmutableFactory(m1);
        assertTrue(factory1 instanceof NilMapNodeFactory);
        assertEquals(2, factory1.getDurableLength(m1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(factory1.getDurableLength(m1));
        factory1.writeDurable(m1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        ImmutableFactory factory2 = registry.readId(byteBuffer1);
        assertTrue(factory2 instanceof NilMapNodeFactory);
        Object object2 = factory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(registry.nilMap));

        MapNode m2 = m1;
        m2 = m2.add("a", "1");
        m2 = m2.add("a", "2");
        m2 = m2.add("a", "3");
        ImmutableFactory factory3 = registry.getImmutableFactory(m2);
        assertTrue(factory3 instanceof MapNodeFactory);
        assertEquals(96, factory3.getDurableLength(m2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(factory3.getDurableLength(m2));
        factory3.writeDurable(m2, byteBuffer2);
        assertEquals(96, byteBuffer2.position());
        byteBuffer2.flip();
        ImmutableFactory factory4 = registry.readId(byteBuffer2);
        assertTrue(factory4 instanceof MapNodeFactory);
        Object object4 = factory4.deserialize(byteBuffer2);
        assertEquals(96, byteBuffer2.position());
        assertEquals("123", String.join("", ((MapNode) object4).getList("a").flatList()));
    }
}

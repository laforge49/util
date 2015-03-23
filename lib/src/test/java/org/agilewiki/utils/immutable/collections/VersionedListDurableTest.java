package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Registry;

import java.nio.ByteBuffer;

public class VersionedListDurableTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        VersionedListNode l1 = registry.nilVersionedList;
        ImmutableFactory factory1 = registry.getImmutableFactory(l1);
        assertTrue(factory1 instanceof NilVersionedListNodeFactory);
        assertEquals(2, factory1.getDurableLength(l1));
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(factory1.getDurableLength(l1));
        factory1.writeDurable(l1, byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        byteBuffer1.flip();
        ImmutableFactory factory2 = registry.readId(byteBuffer1);
        assertTrue(factory2 instanceof NilVersionedListNodeFactory);
        Object object2 = factory2.deserialize(byteBuffer1);
        assertEquals(2, byteBuffer1.position());
        assertTrue(object2.equals(registry.nilVersionedList));

        VersionedListNode l2 = l1;
        l2 = l2.add("1", 1);
        l2 = l2.add("2", 2);
        l2 = l2.add("3", 3);
        ImmutableFactory factory3 = registry.getImmutableFactory(l2);
        assertTrue(factory3 instanceof VersionedListNodeFactory);
        assertEquals(122, factory3.getDurableLength(l2));
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(factory3.getDurableLength(l2));
        factory3.writeDurable(l2, byteBuffer2);
        assertEquals(122, byteBuffer2.position());
        byteBuffer2.flip();
        ImmutableFactory factory4 = registry.readId(byteBuffer2);
        assertTrue(factory4 instanceof VersionedListNodeFactory);
        Object object4 = factory4.deserialize(byteBuffer2);
        assertEquals(122, byteBuffer2.position());
        assertEquals("123", String.join("", ((VersionedListNode) object4).flatList(3)));
    }
}
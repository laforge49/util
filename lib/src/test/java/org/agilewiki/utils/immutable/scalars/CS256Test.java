package org.agilewiki.utils.immutable.scalars;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Registry;

import java.nio.ByteBuffer;

public class CS256Test extends TestCase {
    public void test() throws Exception {
        FactoryRegistry registry = new Registry();

        CS256 cs256a = new CS256("abc".getBytes());
        CS256 cs256b = new CS256("123".getBytes());
        assertTrue(cs256a.equals(cs256a));
        assertFalse(cs256a.equals(cs256b));
        assertEquals(32, cs256a.toByteArray().length);

        ImmutableFactory factory1 = registry.getImmutableFactory(cs256a);
        assertTrue(factory1 instanceof CS256Factory);
        assertEquals(34, factory1.getDurableLength(cs256a));
        ByteBuffer byteBuffer = ByteBuffer.allocate(factory1.getDurableLength(cs256a));
        factory1.writeDurable(cs256a, byteBuffer);

        byteBuffer.flip();
        ImmutableFactory factory2 = registry.readId(byteBuffer);
        assertTrue(factory2 instanceof CS256Factory);
        Object object2 = factory2.deserialize(byteBuffer);
        CS256 cs256c = (CS256) object2;
        assertTrue(cs256c.equals(cs256a));
        assertFalse(cs256c.equals(cs256b));
    }
}

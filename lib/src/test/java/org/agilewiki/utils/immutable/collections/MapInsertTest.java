package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.Registry;

import java.util.Iterator;

public class MapInsertTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        MapNode m1 = registry.nilMap.add("1", "a");

        assertEquals(1, m1.totalSize("1"));

        assertEquals(1, m1.flatKeys().size());

        ListAccessor a1 = m1.listAccessor("1");
        assertEquals("a", a1.get(0));

        MapAccessor ma = m1.mapAccessor();
        assertEquals("1", ma.firstKey());
        assertEquals("1", ma.lastKey());
        assertEquals(1, ma.flatMap().size());
        assertEquals(1, ma.size());

        assertEquals("1", ma.higherKey(""));
        assertEquals("1", ma.ceilingKey(""));
        assertEquals("1", ma.ceilingKey("1"));
        assertEquals("1", ma.lowerKey("9"));
        assertEquals("1", ma.floorKey("9"));
        assertEquals("1", ma.floorKey("1"));

        Iterator<ListAccessor> it = ma.iterator();
        assertTrue(it.hasNext());
        assertEquals(1, it.next().size());
        assertFalse(it.hasNext());
    }
}
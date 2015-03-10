package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class MapRemoveTest extends TestCase {
    public void test() throws Exception {
        assertNull(MapNode.MAP_NIL.remove("", 0, 1));

        MapNode m1 = MapNode.MAP_NIL.add("1", "a", 2);
        assertEquals("a", m1.remove("1", 0, 3));

        assertEquals(1, m1.totalSize("1"));

        assertEquals(0, m1.flatKeys(4).size());

        MapAccessor ma = m1.mapAccessor();
        assertNull(ma.firstKey());
        assertNull(ma.lastKey());
        assertEquals(0, ma.flatMap().size());

        assertNull(ma.higherKey(""));
        assertNull(ma.ceilingKey(""));
        assertNull(ma.ceilingKey("1"));
        assertNull(ma.lowerKey("9"));
        assertNull(ma.floorKey("9"));
        assertNull(ma.floorKey("1"));
    }
}

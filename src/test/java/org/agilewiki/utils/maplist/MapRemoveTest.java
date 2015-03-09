package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class MapRemoveTest extends TestCase {
    public void test() throws Exception {
        assertNull(MapNode.MAP_NIL.remove("", 0, 1));

        MapNode m1 = MapNode.MAP_NIL.add("1", "a", 2);
        assertEquals("a", m1.remove("1", 0, 3));

        assertEquals(1, m1.maxSize("1"));

        assertEquals(0, m1.flatKeys(4).size());
    }
}

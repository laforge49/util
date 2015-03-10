package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class EmptyMapTest extends TestCase {
    public void test() throws Exception {

        assertEquals(0, MapNode.MAP_NIL.totalSize(""));

        assertEquals(0, MapNode.MAP_NIL.flatKeys(1).size());

        ListAccessor la = MapNode.MAP_NIL.listAccessor("");
        assertNull(la.get(0));

        MapAccessor ma = MapNode.MAP_NIL.mapAccessor();
        assertNull(ma.firstKey());
        assertNull(ma.lastKey());
        assertEquals(0, ma.flatMap().size());
        assertEquals(0, ma.size());

        assertNull(ma.higherKey(""));
        assertNull(ma.lowerKey("x"));
    }
}

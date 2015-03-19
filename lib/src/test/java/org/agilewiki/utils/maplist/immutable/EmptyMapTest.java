package org.agilewiki.utils.maplist.immutable;

import junit.framework.TestCase;
import org.agilewiki.utils.maplist.ListAccessor;
import org.agilewiki.utils.maplist.MapAccessor;

public class EmptyMapTest extends TestCase {
    public void test() throws Exception {

        assertEquals(0, ImmutableMapNode.MAP_NIL.totalSize(""));

        assertEquals(0, ImmutableMapNode.MAP_NIL.flatKeys(1).size());

        ListAccessor la = ImmutableMapNode.MAP_NIL.listAccessor("");
        assertNull(la.get(0));

        MapAccessor ma = ImmutableMapNode.MAP_NIL.mapAccessor();
        assertNull(ma.firstKey());
        assertNull(ma.lastKey());
        assertEquals(0, ma.flatMap().size());
        assertEquals(0, ma.size());

        assertNull(ma.higherKey(""));
        assertNull(ma.ceilingKey(""));
        assertNull(ma.lowerKey("x"));
        assertNull(ma.floorKey("x"));

        assertFalse(ma.iterator().hasNext());
    }
}

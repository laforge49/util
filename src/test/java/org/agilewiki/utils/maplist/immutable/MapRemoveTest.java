package org.agilewiki.utils.maplist.immutable;

import junit.framework.TestCase;
import org.agilewiki.utils.maplist.MapAccessor;

public class MapRemoveTest extends TestCase {
    public void test() throws Exception {
        assertEquals(ImmutableMapNode.MAP_NIL, ImmutableMapNode.MAP_NIL.remove("", 0, 1));
        ImmutableMapNode.MAP_NIL.clearMap(1);

        ImmutableMapNode m1 = ImmutableMapNode.MAP_NIL.add("1", "a", 2);
        m1 = m1.remove("1", 0, 3);

        ImmutableMapNode m2 = m1.copyMap();
        m2.clearMap(3);
        assertEquals(0, m2.size(4));

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

        assertFalse(ma.iterator().hasNext());
    }
}

package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.utils.immutable.Registry;

public class MapEmptyTest extends TestCase {
    public void test() throws Exception {
        Registry registry = new Registry();

        assertEquals(0, registry.nilMap.totalSize(""));

        assertEquals(0, registry.nilMap.flatKeys().size());

        ListAccessor la = registry.nilMap.listAccessor("");
        assertNull(la.get(0));

        MapAccessor ma = registry.nilMap.mapAccessor();
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

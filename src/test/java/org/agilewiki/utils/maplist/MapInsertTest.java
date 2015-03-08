package org.agilewiki.utils.maplist;

import junit.framework.TestCase;

public class MapInsertTest extends TestCase {
    public void test() throws Exception {

        MapNode l1 = MapNode.MAP_NIL.add("1", "a", 2);

        assertEquals(1, l1.maxSize("1"));

    }
}

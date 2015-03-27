package org.agilewiki.utils.virtualcow.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VersionedMapEmptyTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 10;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

            assertEquals(0, registry.nilVersionedMap.totalSize(""));

            assertEquals(0, registry.nilVersionedMap.flatKeys(1).size());

            ListAccessor la = registry.nilVersionedMap.listAccessor("");
            assertNull(la.get(0));

            MapAccessor ma = registry.nilVersionedMap.mapAccessor();
            assertNull(ma.firstKey());
            assertNull(ma.lastKey());
            assertEquals(0, ma.flatMap().size());
            assertEquals(0, ma.size());

            assertNull(ma.higherKey(""));
            assertNull(ma.ceilingKey(""));
            assertNull(ma.lowerKey("x"));
            assertNull(ma.floorKey("x"));

            assertFalse(ma.iterator().hasNext());
        } finally {
            Plant.close();
        }
    }
}

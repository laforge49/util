package org.agilewiki.utils.immutable.collections;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MapEmptyTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            int maxRootBlockSize = 1000;
            Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize);
            DbFactoryRegistry registry = db.dbFactoryRegistry;

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
        } finally {
            Plant.close();
        }
    }
}

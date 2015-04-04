package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.collections.MapNode;
import org.agilewiki.utils.virtualcow.collections.VersionedMapNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BigVersionedListTest extends TestCase {
    int k;
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("vcow.db");
            Files.deleteIfExists(dbPath);
            int maxBlockSize = 1000000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxBlockSize)) {
                db.open(true);
                for (k = 0; k < 2; ++k) {
                    Transaction t2 = new Transaction() {
                        @Override
                        public MapNode transform(MapNode mapNode) {
                            VersionedMapNode vmn = db.dbFactoryRegistry.versionedNilMap;
                            for (int i = 0; i < 10; i++) {
                                vmn = vmn.add(0, "", 123);
                            }
                            mapNode = mapNode.add(1, vmn);
                            return mapNode;
                        }
                    };
                    db.update(t2).call();
                }
                db.close();
            }
        } finally {
            Plant.close();
        }
    }
}

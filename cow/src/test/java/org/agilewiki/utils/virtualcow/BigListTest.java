package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.collections.MapNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BigListTest extends TestCase {
    int k;
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("vcow.db");
            Files.deleteIfExists(dbPath);
            int maxBlockSize = 1000000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxBlockSize)) {
                Files.deleteIfExists(dbPath);
                db.open(true);
                for (k = 0; k < 4; ++k) {
                    Transaction t2 = new Transaction() {
                        @Override
                        public MapNode transform(MapNode mapNode) {
                            for (int i = 0; i < 10; i++) {
                                mapNode = mapNode.add(0, k*100+i);
                            }
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

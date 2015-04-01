package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.collections.MapNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BigMapTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("vcow.db");
            Files.deleteIfExists(dbPath);
            int maxBlockSize = 100000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxBlockSize)) {
                Files.deleteIfExists(dbPath);
                db.open(true);
                for (int j = 0; j < 10; ++j) {
                    System.err.println("transaction set "+j);
                    Transaction t2 = new Transaction() {
                        int k = 0;
                        @Override
                        public MapNode transform(MapNode mapNode)
                                throws IOException {
                            for (int i = 0; i < 10; i++) {
                                mapNode = mapNode.add(k * 100000 + i, "");
                            }
                            k += 1;
                            System.out.println(mapNode.getDurableLength());
                            System.out.println(db.usage());
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

package org.agilewiki.utils.virtualcow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.BaseRegistry;
import org.agilewiki.utils.virtualcow.collections.MapNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Path dbPath = Paths.get("cow.db");
            Files.deleteIfExists(dbPath);
            int maxRootBlockSize = 1000;
            try (Db db = new Db(new BaseRegistry(), dbPath, maxRootBlockSize)) {
                Files.deleteIfExists(dbPath);
                db.open(true);
                Transaction t2 = new Transaction() {
                    @Override
                    public MapNode transform(MapNode mapNode)
                            throws IOException {
                        System.out.println("block usage: " + db.usage());
                        mapNode = mapNode.add("x", "hi!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                        BlockReference blockReference = new BlockReference(db, "ho!");
                        mapNode = mapNode.add("y", blockReference);
                        return mapNode;
                    }
                };
                db.update(t2).call();
                db.close();
                System.out.println(Files.size(dbPath));
                db.open();
                System.out.println(db.mapNode.firstKey());
                BlockReference br = (BlockReference) db.mapNode.listAccessor("y").get(0);
                System.out.println(br.get());
            }
        } finally {
            Plant.close();
        }
    }
}

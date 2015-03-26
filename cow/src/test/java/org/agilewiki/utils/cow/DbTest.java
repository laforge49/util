package org.agilewiki.utils.cow;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.Transaction;
import org.agilewiki.utils.immutable.Registry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Registry registry = new Registry();
            Path calfPath = Paths.get("cow.db");
            int maxRootBlockSize = 1000;
            try (Db db = new Db(registry, calfPath, maxRootBlockSize)) {
                Files.deleteIfExists(calfPath);
                db.open(true, "!");
                Transaction t2 = new Transaction() {
                    @Override
                    public Object transform(Object immutable) {
                        return "hi!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
                    }
                };
                db.update(t2).call();
                db.close();
                System.out.println(Files.size(calfPath));
                db.open();
                System.out.println(db.immutable);
            }
        } finally {
            Plant.close();
        }
    }
}

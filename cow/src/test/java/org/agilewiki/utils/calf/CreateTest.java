package org.agilewiki.utils.calf;

import junit.framework.TestCase;
import org.agilewiki.jactor2.core.impl.Plant;
import org.agilewiki.utils.immutable.Registry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateTest extends TestCase {
    public void test() throws Exception {
        new Plant();
        try {
            Registry registry = new Registry();
            Path calfPath = Paths.get("calf.db");
            int maxRootBlockSize = 1000;
            try (Db calf = new Db(registry, calfPath, maxRootBlockSize)) {
                calf.deleteIfExists();
                calf.open(true, "Hello world!").call();
                calf.update("hi!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!").call();
                calf.close();
                System.out.println(calf.size());
                calf.open();
                System.out.println(calf.immutable);
            }
        } finally {
            Plant.close();
        }
    }
}

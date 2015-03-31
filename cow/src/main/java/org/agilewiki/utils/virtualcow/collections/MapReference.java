package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.immutable.scalars.CS256Factory;
import org.agilewiki.utils.virtualcow.BlockReference;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A reference to an immutable map of versioned lists.
 */
public class MapReference extends BlockReference implements MapNode {
    public MapReference(Db db, int blockNbr, int blockLength, CS256 cs256) {
        super(db, blockNbr, blockLength, cs256);
    }

    public MapReference(Db db, Object immutable) throws IOException {
        super(db, immutable);
    }

    @Override
    public DbFactoryRegistry getRegistry() {
        return null;
    }

    @Override
    public MapNodeData getData() throws IOException {
        return (MapNodeData) super.getData();
    }
}

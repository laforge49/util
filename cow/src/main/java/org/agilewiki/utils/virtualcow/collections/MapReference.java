package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.virtualcow.BlockReference;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.io.IOException;

/**
 * A reference to an immutable map of versioned lists.
 */
public class MapReference extends BlockReference implements MapNode {
    public MapReference(DbFactoryRegistry registry,
                        int blockNbr,
                        int blockLength,
                        CS256 cs256) {
        super(registry, blockNbr, blockLength, cs256);
    }

    public MapReference(DbFactoryRegistry registry,
                        Object immutable) throws IOException {
        super(registry, immutable);
    }

    @Override
    public MapNodeData getData() throws IOException {
        return (MapNodeData) super.getData();
    }
}

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.virtualcow.BlockReference;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;

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

    @Override
    public MapNodeData getData() {
        return (MapNodeData) super.getData();
    }

    protected Object loadData(ByteBuffer byteBuffer) {
        return new MapNodeData(this, byteBuffer);
    }

    /**
     * Write the durable to a byte buffer.
     *
     * @param byteBuffer The byte buffer.
     */
    public void writeDurable(ByteBuffer byteBuffer) {
        int expected = byteBuffer.position() + getDurableLength();
        byteBuffer.putChar(getRegistry().mapReferenceId);
        serialize(byteBuffer);
        if (expected != byteBuffer.position()) {
            getRegistry().db.close();
            throw new SerializationException();
        }
    }
}

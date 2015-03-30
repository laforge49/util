package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.Releasable;
import org.agilewiki.utils.immutable.scalars.CS256;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * Holds a block number, block length and checksum.
 */
public class BlockReference implements Releasable {

    /**
     * The database this object is a part of.
     */
    final public Db db;

    /**
     * The number of the block being referenced.
     */
    final public int blockNbr;

    /**
     * The length of the durable data held by the block.
     */
    final public int blockLength;

    /**
     * The checksum of the contents of the block.
     */
    final public CS256 cs256;

    protected SoftReference softReference;

    /**
     * Create a reference to an existing block.
     *
     * @param db          The database this object is a part of.
     * @param blockNbr    The number of the block being referenced.
     * @param blockLength The length of the durable data held by the block.
     * @param cs256       The checksum of the contents of the block.
     */
    public BlockReference(Db db,
                          int blockNbr,
                          int blockLength,
                          CS256 cs256) {
        this.db = db;
        this.blockNbr = blockNbr;
        this.blockLength = blockLength;
        this.cs256 = cs256;
    }

    /**
     * Creates a new block and a reference to it.
     *
     * @param db        The database this object is a part of.
     * @param immutable The object to be saved in the new block.
     */
    public BlockReference(Db db, Object immutable)
            throws IOException {
        this.db = db;
        ImmutableFactory factory = db.dbFactoryRegistry.getImmutableFactory(immutable);
        int bl = factory.getDurableLength(immutable);
        if (bl > db.maxBlockSize && immutable instanceof Releasable) {
            immutable = ((Releasable) immutable).resize(db.maxBlockSize);
            bl = factory.getDurableLength(immutable);
        }
        if (bl > db.maxBlockSize) {
            db.getReactor().error("block size exceeds max block size");
            throw new IllegalStateException("block size exceeds max block size");
        }
        blockLength = bl;
        ByteBuffer byteBuffer = ByteBuffer.allocate(blockLength);
        factory.writeDurable(immutable, byteBuffer);
        byteBuffer.flip();
        cs256 = new CS256(byteBuffer);
        blockNbr = db.allocate();
        db.writeBlock(byteBuffer, blockNbr);
    }

    /**
     * Releases the contents of the block as well as the block.
     */
    @Override
    public void releaseAll()
            throws IOException {
        Object immutable = get();
        if (immutable instanceof Releasable)
            ((Releasable) immutable).releaseAll();
        releaseLocal();
    }

    /**
     * Releases the block.
     */
    @Override
    public void releaseLocal()
            throws IOException {
        db.release(blockNbr);
    }

    /**
     * Reads, validates, deserializes and returns the contents of the block.
     *
     * @return The contents of the block.
     */
    public Object get()
            throws IOException {
        if (softReference != null) {
            Object immutable = softReference.get();
            if (immutable != null)
                return immutable;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(blockLength);
        db.readBlock(byteBuffer, blockNbr);
        byteBuffer.flip();
        CS256 cs = new CS256(byteBuffer);
        if (!cs256.equals(cs)) {
            db.getReactor().error("block has bad checksum");
            throw new IllegalStateException("block has bad checksum");
        }
        ImmutableFactory factory = db.dbFactoryRegistry.readId(byteBuffer);
        Object immutable = factory.deserialize(byteBuffer);
        softReference = new SoftReference(immutable);
        return immutable;
    }
}

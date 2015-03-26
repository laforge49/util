package org.agilewiki.utils.cow;

import org.agilewiki.jactor2.core.blades.IsolationBladeBase;
import org.agilewiki.jactor2.core.messages.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.messages.impl.AsyncRequestImpl;
import org.agilewiki.utils.dsm.DiskSpaceManager;
import org.agilewiki.utils.immutable.CascadingRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.collections.MapNode;
import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.immutable.scalars.CS256Factory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

/**
 * A database that supports multiple blocks.
 */
public class Db extends IsolationBladeBase implements AutoCloseable {
    public final DbFactoryRegistry dbFactoryRegistry;
    public final Path dbPath;
    private FileChannel fc;
    private final int maxBlockSize;
    private long nextRootPosition;
    public MapNode mapNode;
    protected Thread privilegedThread;
    DiskSpaceManager dsm;

    /**
     * Create a Db actor.
     *
     * @param parentRegistry   The parent cascading registry.
     * @param dbPath           The path of the db file.
     * @param maxBlockSize The maximum root block size.
     */
    public Db(CascadingRegistry parentRegistry,
              Path dbPath,
              int maxBlockSize) throws Exception {
        dbFactoryRegistry = new DbFactoryRegistry(this, parentRegistry);
        this.dbPath = dbPath;
        this.maxBlockSize = maxBlockSize;
    }

    /**
     * Open the db, creating a new db file.
     *
     * @param createNew True when a db file must not already exist.
     */
    public void open(boolean createNew)
            throws IOException {
        if (fc != null) {
            getReactor().error("open on already open db");
            throw new IllegalStateException("already open");
        }
        mapNode = dbFactoryRegistry.nilMap;
        try {
            if (createNew)
                fc = FileChannel.open(dbPath, READ, WRITE, SYNC, CREATE_NEW);
            else
                fc = FileChannel.open(dbPath, READ, WRITE, SYNC, CREATE);
            dsm = new DiskSpaceManager();
            dsm.allocate();
            dsm.allocate();
            _update(mapNode);
            _update(mapNode);
        } catch (Exception ex) {
            fc = null;
            getReactor().error("unable to open db to create a new file", ex);
            throw ex;
        }
    }

    /**
     * Update the database.
     *
     * @param transaction The transaction which will transform the db contents.
     * @return The request to perform the update.
     */
    public AReq<Void> update(Transaction transaction) {
        return new AReq<Void>("update") {
            @Override
            protected void processAsyncOperation(AsyncRequestImpl _asyncRequestImpl,
                                                 AsyncResponseProcessor<Void> _asyncResponseProcessor)
                    throws Exception {
                try {
                    privilegedThread = Thread.currentThread();
                    try {
                        _update(transaction.transform(mapNode));
                    } finally {
                        privilegedThread = null;
                    }
                    _asyncResponseProcessor.processAsyncResponse(null);
                } catch (Exception ex) {
                    getReactor().error("unable to update db", ex);
                    throw ex;
                }
            }
        };
    }

    /**
     * Verifies that the thread is processing a transaction.
     * Otherwise an IllegalStateException is thrown.
     */
    public void checkPrivilege() {
        if (Thread.currentThread() != privilegedThread)
            throw new IllegalStateException("privileged operation");
    }

    protected void _update(MapNode mapNode)
            throws IOException {
        if (mapNode == this.mapNode)
            return;
        ImmutableFactory factory = dbFactoryRegistry.getImmutableFactory(mapNode);
        dsm.commit();
        int contentSize = 8 + dsm.durableLength() + factory.getDurableLength(mapNode);
        int blockSize = 4 + 4 + 34 + contentSize;
        if (blockSize > maxBlockSize) {
            throw new IllegalStateException("maxRootBlockSize is smaller than the block size " +
                    blockSize);
        }
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentSize);
        contentBuffer.putLong(System.currentTimeMillis());
        dsm.write(contentBuffer);
        factory.writeDurable(mapNode, contentBuffer);
        contentBuffer.flip();
        CS256 cs256 = new CS256(contentBuffer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(blockSize);
        byteBuffer.putInt(maxBlockSize);
        byteBuffer.putInt(blockSize);
        ImmutableFactory cs256Factory = dbFactoryRegistry.getImmutableFactory(cs256);
        cs256Factory.writeDurable(cs256, byteBuffer);
        byteBuffer.put(contentBuffer);
        byteBuffer.flip();
        long p = nextRootPosition;
        while (byteBuffer.remaining() > 0) {
            p += fc.write(byteBuffer, p);
        }
        nextRootPosition = (nextRootPosition + maxBlockSize) % (2 * maxBlockSize);
        this.mapNode = mapNode;
        return;
    }

    public void readBlock(ByteBuffer byteBuffer, int blockNbr)
            throws IOException {
        checkPrivilege();
        long position = blockNbr * (long) maxBlockSize;
        while (byteBuffer.remaining() > 0) {
            position += fc.read(byteBuffer, position);
        }
    }

    public void write(ByteBuffer byteBuffer, int blockNbr)
            throws IOException {
        checkPrivilege();
        long position = blockNbr * (long) maxBlockSize;
        while (byteBuffer.remaining() > 0) {
            position += fc.write(byteBuffer, position);
        }
    }

    @Override
    public void close() throws Exception {
        if (fc != null) {
            fc.close();
            fc = null;
        }
    }

    /**
     * Open an existing database.
     */
    public void open()
            throws IOException {
        if (fc != null) {
            getReactor().error("open on already open db");
            throw new IllegalStateException("already open");
        }
        if (Files.notExists(dbPath)) {
            getReactor().error("file does not exist: " + dbPath);
            throw new IllegalStateException("file does not exist: " + dbPath);
        }
        if (!Files.isReadable(dbPath)) {
            getReactor().error("file is not readable: " + dbPath);
            throw new IllegalStateException("file is not readable: " + dbPath);
        }
        if (!Files.isWritable(dbPath)) {
            getReactor().error("file is not writable: " + dbPath);
            throw new IllegalStateException("file is not writable: " + dbPath);
        }
        if (!Files.isRegularFile(dbPath)) {
            getReactor().error("file is not a regular file: " + dbPath);
            throw new IllegalStateException("file is not a regular file: " + dbPath);
        }
        try {
            fc = FileChannel.open(dbPath, READ, WRITE, SYNC);
            RootBlock rb0 = readRootBlock(0L);
            RootBlock rb1 = readRootBlock(maxBlockSize);
            if (rb0 == null && rb1 == null) {
                throw new IllegalStateException("no valid root blocks found");
            }
            RootBlock rb;
            if (rb0 == null) {
                rb = rb1;
                nextRootPosition = 0L;
            } else if (rb1 == null) {
                rb = rb0;
                nextRootPosition = maxBlockSize;
            } else if (rb0.timestamp > rb1.timestamp) {
                rb = rb0;
                nextRootPosition = maxBlockSize;
            } else {
                rb = rb1;
                nextRootPosition = 0L;
            }
            dsm = new DiskSpaceManager(rb.serializedContent);
            ImmutableFactory factory = dbFactoryRegistry.readId(rb.serializedContent);
            mapNode = (MapNode) factory.deserialize(rb.serializedContent);
        } catch (Exception ex) {
            fc = null;
            getReactor().error("Unable to open existing db file", ex);
            throw ex;
        }
    }

    protected RootBlock readRootBlock(long position)
            throws IOException {
        try {
            ByteBuffer header = ByteBuffer.allocate(4 + 4 + 34);
            while (header.remaining() > 0) {
                position += fc.read(header, position);
            }
            header.flip();
            int maxSize = header.getInt();
            if (maxBlockSize != maxSize) {
                getReactor().warn("root block max size is incorrect");
                return null;
            }
            int blockSize = header.getInt();
            if (blockSize < 4 + 4 + 34 + 8 + 4 + 2) {
                getReactor().warn("root block size is too small");
                return null;
            }
            if (blockSize > maxBlockSize) {
                getReactor().warn("root block size exceeds max root block size");
                return null;
            }
            ImmutableFactory csf = dbFactoryRegistry.readId(header);
            if (!(csf instanceof CS256Factory)) {
                getReactor().warn("expecting CS256 in root block");
                return null;
            }
            CS256 cs1 = (CS256) csf.deserialize(header);
            ByteBuffer body = ByteBuffer.allocate(blockSize - 4 - 4 - 34);
            while (body.remaining() > 0) {
                position += fc.read(body, position);
            }
            body.flip();
            CS256 cs2 = new CS256(body);
            if (!cs1.equals(cs2)) {
                getReactor().warn("root block has bad checksum");
                return null;
            }
            RootBlock rb = new RootBlock();
            rb.timestamp = body.getLong();
            rb.serializedContent = body;
            return rb;
        } catch (Exception ex) {
            getReactor().warn("unable to read root block", ex);
            return null;
        }
    }

    protected class RootBlock {
        long timestamp;
        ByteBuffer serializedContent;
    }

    /**
     * Allocates a block of disk space.
     * But if not processing a transaction when called,
     * an IllegalStateException is thrown.
     *
     * @return The number of the block that was allocated.
     */
    public int allocate() {
        checkPrivilege();
        return dsm.allocate();
    }

    /**
     * Returns the number of allocated pages.
     * But if not processing a transaction when called,
     * an IllegalStateException is thrown.
     *
     * @return The number of pages in use.
     */
    public int usage() {
        checkPrivilege();
        return dsm.usage();
    }

    /**
     * Release a block.
     * It will become available on the next transaction.
     * But if not processing a transaction when called,
     * an IllegalStateException is thrown.
     *
     * @param i The block to be released.
     */
    public void release(int i) {
        checkPrivilege();
        dsm.release(i);
    }
}

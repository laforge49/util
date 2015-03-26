package org.agilewiki.utils.cow;

import org.agilewiki.jactor2.core.blades.IsolationBladeBase;
import org.agilewiki.jactor2.core.messages.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.messages.impl.AsyncRequestImpl;
import org.agilewiki.jactor2.core.reactors.IsolationReactor;
import org.agilewiki.utils.Transaction;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
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
    private final FactoryRegistry registry;
    public final Path dbPath;
    private FileChannel fc;
    private final int maxRootBlockSize;
    private long nextRootPosition;
    public Object immutable;
    protected Thread privilegedThread;
    //todo define dsm

    /**
     * Create a Db actor.
     *
     * @param registry         The immutable factory registry.
     * @param dbPath           The path of the db file.
     * @param maxRootBlockSize The maximum root block size.
     */
    public Db(FactoryRegistry registry, Path dbPath, int maxRootBlockSize) throws Exception {
        this.registry = registry;
        this.dbPath = dbPath;
        this.maxRootBlockSize = maxRootBlockSize;
    }

    /**
     * Create a Db actor.
     *
     * @param _reactor         The reactor of the actor.
     * @param registry         The immutable factory registry.
     * @param dbPath           The path of the db file.
     * @param maxRootBlockSize The maximum root block size.
     */
    public Db(IsolationReactor _reactor,
              FactoryRegistry registry,
              Path dbPath,
              int maxRootBlockSize) {
        super(_reactor);
        this.registry = registry;
        this.dbPath = dbPath;
        this.maxRootBlockSize = maxRootBlockSize;
    }

    /**
     * Open the db, creating a new db file.
     *
     * @param createNew True when a db file must not already exist.
     * @param immutable The initial value held by the db.
     */
    public void open(boolean createNew, Object immutable)
            throws IOException {
        if (fc != null) {
            getReactor().error("open on already open db");
            throw new IllegalStateException("already open");
        }
        try {
            if (createNew)
                fc = FileChannel.open(dbPath, READ, WRITE, SYNC, CREATE_NEW);
            else
                fc = FileChannel.open(dbPath, READ, WRITE, SYNC, CREATE);
            _update(immutable);
            _update(immutable);
        } catch (Exception ex) {
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
                        _update(transaction.transform(immutable));
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
     * Returns true if the current thread is executing a transaction.
     *
     * @return True if the thread has transactional privileges.
     */
    public boolean isPrivileged() {
        return Thread.currentThread() == privilegedThread;
    }

    protected void _update(Object immutable)
            throws IOException {
        //todo save dsm
        ImmutableFactory factory = registry.getImmutableFactory(immutable);
        int contentSize = 8 + factory.getDurableLength(immutable);
        int blockSize = 4 + 4 + 34 + contentSize;
        if (blockSize > maxRootBlockSize) {
            throw new IllegalStateException("maxRootBlockSize is smaller than the block size " +
                    blockSize);
        }
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentSize);
        contentBuffer.putLong(System.currentTimeMillis());
        factory.writeDurable(immutable, contentBuffer);
        contentBuffer.flip();
        CS256 cs256 = new CS256(contentBuffer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(blockSize);
        byteBuffer.putInt(maxRootBlockSize);
        byteBuffer.putInt(blockSize);
        ImmutableFactory cs256Factory = registry.getImmutableFactory(cs256);
        cs256Factory.writeDurable(cs256, byteBuffer);
        byteBuffer.put(contentBuffer);
        byteBuffer.flip();
        long p = nextRootPosition;
        while (byteBuffer.remaining() > 0) {
            p += fc.write(byteBuffer, p);
        }
        nextRootPosition = (nextRootPosition + maxRootBlockSize) % (2 * maxRootBlockSize);
        this.immutable = immutable;
        return;
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
            RootBlock rb1 = readRootBlock(maxRootBlockSize);
            if (rb0 == null && rb1 == null) {
                throw new IllegalStateException("no valid root blocks found");
            }
            RootBlock rb;
            if (rb0 == null) {
                rb = rb1;
                nextRootPosition = 0L;
            } else if (rb1 == null) {
                rb = rb0;
                nextRootPosition = maxRootBlockSize;
            } else if (rb0.timestamp > rb1.timestamp) {
                rb = rb0;
                nextRootPosition = maxRootBlockSize;
            } else {
                rb = rb1;
                nextRootPosition = 0L;
            }
            //todo load dsm
            ImmutableFactory factory = registry.readId(rb.immutableBytes);
            immutable = factory.deserialize(rb.immutableBytes);
        } catch (Exception ex) {
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
            if (maxRootBlockSize != maxSize) {
                getReactor().warn("root block max size is incorrect");
                return null;
            }
            int blockSize = header.getInt();
            if (blockSize < 4 + 4 + 34 + 8 + 2) {
                getReactor().warn("root block size is too small");
                return null;
            }
            if (blockSize > maxRootBlockSize) {
                getReactor().warn("root block size exceeds max root block size");
                return null;
            }
            ImmutableFactory csf = registry.readId(header);
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
            rb.immutableBytes = body;
            return rb;
        } catch (Exception ex) {
            getReactor().warn("unable to read root block", ex);
            return null;
        }
    }

    protected class RootBlock {
        long timestamp;
        ByteBuffer immutableBytes;
    }
}

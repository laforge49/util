package org.agilewiki.utils.calf;

import org.agilewiki.jactor2.core.blades.IsolationBladeBase;
import org.agilewiki.jactor2.core.messages.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.messages.impl.AsyncRequestImpl;
import org.agilewiki.jactor2.core.reactors.IsolationReactor;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.scalars.CS256;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;

/**
 * A database with one block stored alternatively in two locations.
 */
public class Db extends IsolationBladeBase implements AutoCloseable {
    private final FactoryRegistry registry;
    private final Path dbPath;
    private SeekableByteChannel sbc;
    private final int maxRootBlockSize;
    private long nextRootPosition;
    public Object immutable;

    public Db(FactoryRegistry registry, Path dbPath, int maxRootBlockSize) throws Exception {
        this.registry = registry;
        this.dbPath = dbPath;
        this.maxRootBlockSize = maxRootBlockSize;
    }

    public Db(IsolationReactor _reactor,
              FactoryRegistry registry,
              Path dbPath,
              int maxRootBlockSize) {
        super(_reactor);
        this.registry = registry;
        this.dbPath = dbPath;
        this.maxRootBlockSize = maxRootBlockSize;
    }

    public boolean usable() {
        return Files.exists(dbPath) &&
                Files.isReadable(dbPath) &&
                Files.isWritable(dbPath) &&
                Files.isRegularFile(dbPath);
    }

    public boolean notExists() {
        return Files.notExists(dbPath);
    }

    public boolean deleteIfExists()
            throws IOException {
        return Files.deleteIfExists(dbPath);
    }

    public long size()
            throws IOException {
        return Files.size(dbPath);
    }

    public AReq<Void> create(boolean createNew, Object immutable) {
        return new AReq<Void>("create") {
            @Override
            protected void processAsyncOperation(final AsyncRequestImpl _asyncRequestImpl,
                                                 final AsyncResponseProcessor<Void> _asyncResponseProcessor)
                    throws Exception {
                if (createNew)
                    sbc = Files.newByteChannel(dbPath, READ, WRITE, SYNC, CREATE_NEW);
                else
                    sbc = Files.newByteChannel(dbPath, READ, WRITE, SYNC, CREATE);
                _update(immutable);
                _update(immutable);
                _asyncResponseProcessor.processAsyncResponse(null);
            }
        };
    }

    public AReq<Void> update(Object immutable) {
        return new AReq<Void>("update") {
            @Override
            protected void processAsyncOperation(AsyncRequestImpl _asyncRequestImpl, AsyncResponseProcessor<Void> _asyncResponseProcessor) throws Exception {
                _update(immutable);
                _asyncResponseProcessor.processAsyncResponse(null);
            }
        };
    }

    protected void _update(Object immutable)
            throws IOException {
        ImmutableFactory factory = registry.getImmutableFactory(immutable);
        int contentSize = 8 + factory.getDurableLength(immutable);
        int blockSize = 4 + 4 + 32 + contentSize;
        if (blockSize > maxRootBlockSize) {
            throw new IllegalStateException("maxRootBlockSize is smaller than the block size " +
                    blockSize);
        }
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentSize);
        contentBuffer.putLong(System.currentTimeMillis());
        factory.serialize(immutable, contentBuffer);
        contentBuffer.flip();
        CS256 cs256 = new CS256(contentBuffer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(blockSize);
        byteBuffer.putInt(maxRootBlockSize);
        ImmutableFactory cs256Factory = registry.getImmutableFactory(cs256);
        cs256Factory.serialize(cs256, byteBuffer);
        byteBuffer.put(contentBuffer);
        byteBuffer.putInt(blockSize);
        byteBuffer.flip();
        sbc.position(nextRootPosition);
        while(byteBuffer.remaining() > 0) {
            sbc.write(byteBuffer);
        }
        nextRootPosition = (nextRootPosition + maxRootBlockSize) % (2 * maxRootBlockSize);
        this.immutable = immutable;
        return;
    }

    @Override
    public void close() throws Exception {
        if (sbc != null)
            sbc.close();
    }
}

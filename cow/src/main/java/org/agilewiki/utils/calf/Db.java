package org.agilewiki.utils.calf;

import org.agilewiki.jactor2.core.blades.IsolationBladeBase;
import org.agilewiki.jactor2.core.reactors.IsolationReactor;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.immutable.scalars.CS256Factory;

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

    public void create(boolean createNew, char nilImmutableId)
            throws IOException {
        if (createNew)
            sbc = Files.newByteChannel(dbPath, READ, WRITE, SYNC, CREATE_NEW);
        else
            sbc = Files.newByteChannel(dbPath, READ, WRITE, SYNC, CREATE);
        ByteBuffer byteBuffer0 = initialBlock(nilImmutableId);
        sbc.position(0L);
        while(byteBuffer0.remaining() > 0) {
            sbc.write(byteBuffer0);
        }
    }

    protected ByteBuffer initialBlock(char nilImmutableId) {
        int contentSize = 8 + 2;
        int initialBlockSize = 4 + 4 + 32 + contentSize;
        if (initialBlockSize > maxRootBlockSize) {
            throw new IllegalStateException("maxRootBlockSize is smaller than the initial block size");
        }
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentSize);
        contentBuffer.putLong(System.currentTimeMillis());
        contentBuffer.putChar(nilImmutableId);
        contentBuffer.flip();
        CS256 cs256 = new CS256(contentBuffer);
        ByteBuffer byteBuffer = ByteBuffer.allocate(initialBlockSize);
        byteBuffer.putInt(maxRootBlockSize);
        ImmutableFactory cs256Factory = registry.getImmutableFactory(cs256);
        cs256Factory.serialize(cs256, byteBuffer);
        byteBuffer.put(contentBuffer);
        byteBuffer.putInt(initialBlockSize);
        byteBuffer.flip();
        return byteBuffer;
    }

    @Override
    public void close() throws Exception {
        if (sbc != null)
            sbc.close();
    }
}

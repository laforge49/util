package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable versioned list.
 */
public class VersionedListNodeImpl implements VersionedListNode {

    public final DbFactoryRegistry registry;

    protected final AtomicReference<VersionedListNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected VersionedListNodeImpl(DbFactoryRegistry registry) {
        this.registry = registry;
        dataReference.set(new VersionedListNodeData(this));
        durableLength = 2;
    }

    protected VersionedListNodeImpl(DbFactoryRegistry registry, ByteBuffer byteBuffer) {
        this.registry = registry;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected VersionedListNodeImpl(DbFactoryRegistry registry,
                                    int level,
                                    int totalSize,
                                    long created,
                                    long deleted,
                                    VersionedListNode leftNode,
                                    Object value,
                                    VersionedListNode rightNode) {
        this.registry = registry;
        VersionedListNodeData data = new VersionedListNodeData(
                this,
                level,
                totalSize,
                created,
                deleted,
                leftNode,
                value,
                rightNode);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    @Override
    public DbFactoryRegistry getRegistry() {
        return registry;
    }

    @Override
    public VersionedListNodeData getData() {
        VersionedListNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new VersionedListNodeData(this, byteBuffer.slice()));
        return dataReference.get();
    }

    @Override
    public int getDurableLength() {
        return durableLength;
    }

    @Override
    public void serialize(ByteBuffer byteBuffer) {
        if (this.byteBuffer == null) {
            byteBuffer.putInt(getDurableLength());
            getData().serialize(byteBuffer);
            return;
        }
        ByteBuffer bb = byteBuffer.slice();
        bb.limit(durableLength - 6);
        byteBuffer.put(this.byteBuffer.slice());
        this.byteBuffer = bb;
        dataReference.set(null); //limit memory footprint, plugs memory leak.
    }
}

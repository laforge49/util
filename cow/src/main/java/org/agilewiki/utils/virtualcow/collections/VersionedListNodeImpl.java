package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.Releasable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable versioned list.
 */
public class VersionedListNodeImpl implements VersionedListNode {

    public final VersionedListNodeFactory factory;

    protected final AtomicReference<VersionedListNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected VersionedListNodeImpl(VersionedListNodeFactory factory) {
        this.factory = factory;
        dataReference.set(new VersionedListNodeData(this));
        durableLength = 2;
    }

    protected VersionedListNodeImpl(VersionedListNodeFactory factory, ByteBuffer byteBuffer) {
        this.factory = factory;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected VersionedListNodeImpl(VersionedListNodeFactory factory,
                                    int level,
                                    int totalSize,
                                    long created,
                                    long deleted,
                                    VersionedListNode leftNode,
                                    Object value,
                                    VersionedListNode rightNode) {
        this.factory = factory;
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
    public VersionedListNodeFactory getFactory() {
        return factory;
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

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable map of versioned lists.
 */
public class VersionedMapNodeImpl implements VersionedMapNode {

    public final VersionedMapNodeFactory factory;

    protected final AtomicReference<VersionedMapNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected VersionedMapNodeImpl(VersionedMapNodeFactory factory) {
        this.factory = factory;
        dataReference.set(new VersionedMapNodeData(this));
        durableLength = 2;
    }

    protected VersionedMapNodeImpl(VersionedMapNodeFactory factory, ByteBuffer byteBuffer) {
        this.factory = factory;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected VersionedMapNodeImpl(VersionedMapNodeFactory factory,
                                   int level,
                                   VersionedMapNode leftNode,
                                   VersionedListNode listNode,
                                   VersionedMapNode rightNode,
                                   Comparable key) {
        this.factory = factory;
        VersionedMapNodeData data = new VersionedMapNodeData(
                this,
                level,
                leftNode,
                listNode,
                rightNode,
                key);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    @Override
    public VersionedMapNodeFactory getFactory() {
        return factory;
    }

    @Override
    public VersionedMapNodeData getData() {
        VersionedMapNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new VersionedMapNodeData(this, byteBuffer.slice()));
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
        bb.limit(durableLength - 2);
        byteBuffer.put(this.byteBuffer.slice());
        this.byteBuffer = bb;
        dataReference.set(null); //limit memory footprint, plugs memory leak.
    }
}

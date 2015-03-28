package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.Releasable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable map of versioned lists.
 */
public class MapNodeImpl implements MapNode {

    public final MapNodeFactory factory;

    protected final AtomicReference<MapNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected MapNodeImpl(MapNodeFactory factory) {
        this.factory = factory;
        dataReference.set(new MapNodeData(this));
        durableLength = 2;
    }

    protected MapNodeImpl(MapNodeFactory factory, ByteBuffer byteBuffer) {
        this.factory = factory;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected MapNodeImpl(MapNodeFactory factory,
                          int level,
                          MapNode leftNode,
                          ListNode listNode,
                          MapNode rightNode,
                          Comparable key) {
        this.factory = factory;
        MapNodeData data = new MapNodeData(
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
    public MapNodeFactory getFactory() {
        return factory;
    }

    @Override
    public MapNodeData getData() {
        MapNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new MapNodeData(this, byteBuffer.slice()));
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

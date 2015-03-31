package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.immutable.scalars.CS256Factory;
import org.agilewiki.utils.virtualcow.Db;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable map of versioned lists.
 */
public class MapNodeImpl implements MapNode {

    public final DbFactoryRegistry registry;

    protected final AtomicReference<MapNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected MapNodeImpl(DbFactoryRegistry registry) {
        this.registry = registry;
        dataReference.set(new MapNodeData(this));
        durableLength = 2;
    }

    protected MapNodeImpl(DbFactoryRegistry registry, ByteBuffer byteBuffer) {
        this.registry = registry;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected MapNodeImpl(DbFactoryRegistry registry,
                          int level,
                          MapNode leftNode,
                          ListNode listNode,
                          MapNode rightNode,
                          Comparable key) {
        this.registry = registry;
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
    public DbFactoryRegistry getRegistry() {
        return registry;
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

    @Override
    public Object shrink() throws IOException {
        Db db = registry.db;
        MapNodeData data = getData();
        ByteBuffer byteBuffer = ByteBuffer.allocate(durableLength);
        data.serialize(byteBuffer);
        byteBuffer.flip();
        CS256 cs256 = new CS256(byteBuffer);
        int blockNbr = db.allocate();
        db.writeBlock(byteBuffer, blockNbr);
        return new MapReference(registry, blockNbr, durableLength, cs256);
    }
}

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An immutable list.
 */
public class ListNodeImpl implements ListNode {

    public final DbFactoryRegistry registry;

    protected final AtomicReference<ListNodeData> dataReference = new AtomicReference<>();
    protected final int durableLength;
    protected ByteBuffer byteBuffer;

    protected ListNodeImpl(DbFactoryRegistry registry) {
        this.registry = registry;
        dataReference.set(new ListNodeData(this));
        durableLength = 2;
    }

    protected ListNodeImpl(DbFactoryRegistry registry, ByteBuffer byteBuffer) {
        this.registry = registry;
        durableLength = byteBuffer.getInt();
        this.byteBuffer = byteBuffer.slice();
        this.byteBuffer.limit(durableLength - 6);
        byteBuffer.position(byteBuffer.position() + durableLength - 6);
    }

    protected ListNodeImpl(DbFactoryRegistry registry,
                           int level,
                           int totalSize,
                           ListNode leftNode,
                           Object value,
                           ListNode rightNode) {
        this.registry = registry;
        ListNodeData data = new ListNodeData(
                this,
                level,
                totalSize,
                leftNode,
                value,
                rightNode);
        durableLength = data.getDurableLength();
        dataReference.set(data);
    }

    public DbFactoryRegistry getRegistry() {
        return registry;
    }

    public ListNodeData getData() {
        ListNodeData data = dataReference.get();
        if (data != null)
            return data;
        dataReference.compareAndSet(null, new ListNodeData(this, byteBuffer.slice()));
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

    @Override
    public String toString() {
        if (isNil())
            return "";
        return getData().toString();
    }
}

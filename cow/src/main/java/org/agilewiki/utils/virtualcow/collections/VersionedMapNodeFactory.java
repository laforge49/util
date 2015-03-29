package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class VersionedMapNodeFactory extends BaseFactory {

    public final char nilVersionedMapId;
    public final VersionedMapNode nilVersionedMap;
    public final VersionedListNode nilVersionedList;

    public VersionedMapNodeFactory(
            DbFactoryRegistry factoryRegistry,
            char id,
            char nilVersionedMapId,
            VersionedListNode nilVersionedList) {
        super(factoryRegistry, id);
        this.nilVersionedMapId = nilVersionedMapId;
        this.nilVersionedList = nilVersionedList;
        new VersionedNilMapNodeFactory(factoryRegistry);
        nilVersionedMap = new VersionedMapNodeImpl(this);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object immutable) {
        if (((VersionedMapNode) immutable).isNil())
            return factoryRegistry.getImmutableFactory(nilVersionedMapId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return VersionedMapNodeImpl.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((VersionedMapNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((VersionedMapNode) durable).serialize(byteBuffer);
    }

    @Override
    public VersionedMapNode deserialize(ByteBuffer byteBuffer) {
        return new VersionedMapNodeImpl(this, byteBuffer);
    }
}

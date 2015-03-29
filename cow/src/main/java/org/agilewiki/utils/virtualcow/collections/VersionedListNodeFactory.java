package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how a versioned list is serialized / deserialized.
 */
public class VersionedListNodeFactory extends BaseFactory {

    public final char nilVersionedListId;
    public final VersionedListNode nilVersionedList;

    public VersionedListNodeFactory(DbFactoryRegistry factoryRegistry, char id, char nilVersionedListId) {
        super(factoryRegistry, id);
        this.nilVersionedListId = nilVersionedListId;
        new VersionedNilListNodeFactory(factoryRegistry);
        nilVersionedList = new VersionedListNodeImpl(this);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object immutable) {
        if (((VersionedListNode) immutable).isNil())
            return factoryRegistry.getImmutableFactory(nilVersionedListId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return VersionedListNodeImpl.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        return ((VersionedListNode) immutable).getDurableLength();
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        ((VersionedListNode) immutable).serialize(byteBuffer);
    }

    @Override
    public VersionedListNode deserialize(ByteBuffer byteBuffer) {
        return new VersionedListNodeImpl(this, byteBuffer);
    }
}

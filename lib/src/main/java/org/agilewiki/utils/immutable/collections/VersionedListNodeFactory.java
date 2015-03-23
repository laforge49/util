package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;

import java.nio.ByteBuffer;

/**
 * Defines how a versioned list is serialized / deserialized.
 */
public class VersionedListNodeFactory extends BaseFactory {

    public final char nilVersionedListId;
    public final VersionedListNode nilVersionedList;

    public VersionedListNodeFactory(FactoryRegistry factoryRegistry, char id, char nilVersionedListId) {
        super(factoryRegistry, id);
        this.nilVersionedListId = nilVersionedListId;
        new NilVersionedListNodeFactory(this, nilVersionedListId);
        nilVersionedList = new VersionedListNode(this);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object immutable) {
        if (((VersionedListNode) immutable).isNil())
            return factoryRegistry.getImmutableFactory(nilVersionedListId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return VersionedListNode.class;
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
        return new VersionedListNode(this, byteBuffer);
    }
}

package org.agilewiki.utils.cow.collections;

import org.agilewiki.utils.cow.BaseFactory;
import org.agilewiki.utils.cow.FactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class VersionedListNodeFactory extends BaseFactory {

    public final char listNilId;
    public final VersionedListNode listNil;

    public VersionedListNodeFactory(FactoryRegistry factoryRegistry, char id, char listNilId) {
        super(factoryRegistry, id);
        this.listNilId = listNilId;
        new NilVersionedListNodeFactory(this, listNilId);
        listNil = new VersionedListNode(this);
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

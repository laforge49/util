package org.agilewiki.utils.cow.collections;

import org.agilewiki.utils.cow.BaseFactory;
import org.agilewiki.utils.cow.FactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class ImmutableListNodeFactory extends BaseFactory {

    public final char listNilId;
    public final ImmutableListNode listNil;

    public ImmutableListNodeFactory(FactoryRegistry factoryRegistry, char id, char listNilId) {
        super(factoryRegistry, id);
        this.listNilId = listNilId;
        new NilListNodeFactory(this, listNilId);
        listNil = new ImmutableListNode(this);
    }

    @Override
    public Class getImmutableClass() {
        return ImmutableListNode.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        return ((ImmutableListNode) immutable).getDurableLength();
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        ((ImmutableListNode) immutable).serialize(byteBuffer);
    }

    @Override
    public ImmutableListNode deserialize(ByteBuffer byteBuffer) {
        return new ImmutableListNode(this, byteBuffer);
    }
}

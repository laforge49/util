package org.agilewiki.utils.cow.collections;

import org.agilewiki.utils.cow.BaseFactory;
import org.agilewiki.utils.cow.FactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how a nil list node is serialized / deserialized.
 */
public class NilListNodeFactory extends BaseFactory {

    public final ImmutableListNodeFactory factory;

    public NilListNodeFactory(ImmutableListNodeFactory factory, char id) {
        super(factory.factoryRegistry, id);
        this.factory = factory;
    }

    @Override
    public Class getImmutableClass() {
        return getClass();
    }

    @Override
    public void match(Object immutable) {
        if (!((ImmutableListNode) immutable).isNil())
            throw new IllegalArgumentException("The immutable object is not a nil list node");
    }

    @Override
    public int getDurableLength(Object immutable) {
        return 2;
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
    }

    @Override
    public ImmutableListNode deserialize(ByteBuffer byteBuffer) {
        return factory.listNil;
    }
}

package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;

import java.nio.ByteBuffer;

/**
 * Defines how a nil list node is serialized / deserialized.
 */
public class NilListNodeFactory extends BaseFactory {

    public final ListNodeFactory factory;

    public NilListNodeFactory(ListNodeFactory factory, char id) {
        super(factory.factoryRegistry, id);
        this.factory = factory;
    }

    @Override
    public Class getImmutableClass() {
        return getClass();
    }

    @Override
    public void match(Object immutable) {
        if (!((ListNode) immutable).isNil())
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
    public ListNode deserialize(ByteBuffer byteBuffer) {
        return factory.nilList;
    }
}

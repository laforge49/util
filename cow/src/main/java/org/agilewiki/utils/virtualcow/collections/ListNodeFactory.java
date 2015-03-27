package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;

import java.nio.ByteBuffer;

/**
 * Defines how a list is serialized / deserialized.
 */
public class ListNodeFactory extends BaseFactory {

    public final char nilListId;
    public final ListNode nilList;

    public ListNodeFactory(FactoryRegistry factoryRegistry, char id, char nilListId) {
        super(factoryRegistry, id);
        this.nilListId = nilListId;
        new NilListNodeFactory(this, nilListId);
        nilList = new ListNodeImpl(this);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object immutable) {
        if (((ListNode) immutable).isNil())
            return factoryRegistry.getImmutableFactory(nilListId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return ListNodeImpl.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        return ((ListNode) immutable).getDurableLength();
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        ((ListNode) immutable).serialize(byteBuffer);
    }

    @Override
    public ListNode deserialize(ByteBuffer byteBuffer) {
        return new ListNodeImpl(this, byteBuffer);
    }
}

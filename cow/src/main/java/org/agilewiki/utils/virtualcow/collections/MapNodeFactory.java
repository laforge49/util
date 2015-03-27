package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class MapNodeFactory extends BaseFactory {

    public final char nilMapId;
    public final MapNode nilMap;
    public final ListNode nilList;

    public MapNodeFactory(
            FactoryRegistry factoryRegistry,
            char id,
            char nilMapId,
            ListNode nilList) {
        super(factoryRegistry, id);
        this.nilMapId = nilMapId;
        this.nilList = nilList;
        new NilMapNodeFactory(this, nilMapId);
        nilMap = new MapNode(this);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object durable) {
        if (((MapNode) durable).isNil())
            return factoryRegistry.getImmutableFactory(nilMapId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return MapNode.class;
    }

    @Override
    public int getDurableLength(Object durable) {
        return ((MapNode) durable).getDurableLength();
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
        ((MapNode) durable).serialize(byteBuffer);
    }

    @Override
    public MapNode deserialize(ByteBuffer byteBuffer) {
        return new MapNode(this, byteBuffer);
    }
}

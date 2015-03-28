package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class MapNodeFactory extends BaseFactory {

    public final DbFactoryRegistry factoryRegistry;
    public final char nilMapId;
    public final MapNode nilMap;

    public MapNodeFactory(
            DbFactoryRegistry factoryRegistry,
            char id,
            char nilMapId) {
        super(factoryRegistry, id);
        this.factoryRegistry = factoryRegistry;
        this.nilMapId = nilMapId;
        new NilMapNodeFactory(this, nilMapId);
        nilMap = new MapNodeImpl(factoryRegistry);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object durable) {
        if (((MapNode) durable).isNil())
            return factoryRegistry.getImmutableFactory(nilMapId);
        return this;
    }

    @Override
    public Class getImmutableClass() {
        return MapNodeImpl.class;
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
        return new MapNodeImpl(factoryRegistry, byteBuffer);
    }
}

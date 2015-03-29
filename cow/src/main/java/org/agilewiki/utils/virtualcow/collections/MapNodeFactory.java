package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how true is serialized / deserialized.
 */
public class MapNodeFactory extends BaseFactory {

    public final DbFactoryRegistry registry;
    public final MapNode nilMap;

    public MapNodeFactory(DbFactoryRegistry registry) {
        super(registry, registry.mapNodeImplId);
        this.registry = registry;
        new NilMapNodeFactory(registry);
        nilMap = new MapNodeImpl(registry);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object durable) {
        if (((MapNode) durable).isNil())
            return registry.getImmutableFactory(registry.nilMapId);
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
        return new MapNodeImpl(registry, byteBuffer);
    }
}

package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how map is serialized / deserialized.
 */
public class MapNodeFactory extends BaseFactory {

    public final MapNode nilMap;
    public final NilMapNodeFactory nilMapNodeFactory;

    public MapNodeFactory(DbFactoryRegistry registry) {
        super(registry, registry.mapNodeImplId);
        nilMapNodeFactory = new NilMapNodeFactory(registry);
        nilMap = new MapNodeImpl(registry);
        new MapReferenceFactory(registry);
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object durable) {
        if (((MapNode) durable).isNil())
            return nilMapNodeFactory;
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
        return new MapNodeImpl(((DbFactoryRegistry) factoryRegistry), byteBuffer);
    }
}

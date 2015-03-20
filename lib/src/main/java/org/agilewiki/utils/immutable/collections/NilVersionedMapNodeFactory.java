package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.*;

import java.nio.ByteBuffer;

/**
 * Defines how a nil map node is serialized / deserialized.
 */
public class NilVersionedMapNodeFactory extends BaseFactory {

    public final VersionedMapNodeFactory factory;

    public NilVersionedMapNodeFactory(VersionedMapNodeFactory factory, char id) {
        super(factory.factoryRegistry, id);
        this.factory = factory;
    }

    @Override
    public Class getImmutableClass() {
        return getClass();
    }

    @Override
    public void match(Object durable) {
        if (!((VersionedMapNode) durable).isNil())
            throw new IllegalArgumentException("The immutable object is not a nil map node");
    }

    @Override
    public int getDurableLength(Object durable) {
        return 2;
    }

    @Override
    public void serialize(Object durable, ByteBuffer byteBuffer) {
    }

    @Override
    public VersionedMapNode deserialize(ByteBuffer byteBuffer) {
        return factory.nilVersionedMap;
    }
}

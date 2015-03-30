package org.agilewiki.utils.virtualcow.collections;

import org.agilewiki.utils.virtualcow.BlockReferenceFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

import java.nio.ByteBuffer;

/**
 * Defines how a map reference is serialized / deserialized.
 */
public class MapReferenceFactory extends BlockReferenceFactory {

    /**
     * Create and register the factory.
     *
     * @param registry The registry where the factory is registered.
     */
    public MapReferenceFactory(DbFactoryRegistry registry) {
        super(registry, registry.mapReferenceId);
    }

    @Override
    public Class getImmutableClass() {
        return MapReference.class;
    }
}

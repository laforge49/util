package org.agilewiki.utils.immutable.collections;

import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.virtualcow.BlockReference;
import org.agilewiki.utils.virtualcow.BlockReferenceFactory;
import org.agilewiki.utils.virtualcow.DbFactoryRegistry;

/**
 * Defines how a list reference is serialized / deserialized.
 */
public class ListReferenceFactory extends BlockReferenceFactory {

    /**
     * Create and register the factory.
     *
     * @param registry The registry where the factory is registered.
     */
    public ListReferenceFactory(DbFactoryRegistry registry) {
        super(registry, registry.listReferenceId);
    }

    @Override
    public Class getImmutableClass() {
        return ListReference.class;
    }

    protected BlockReference createReference(DbFactoryRegistry registry,
                                             int blockNbr,
                                             int blockLength,
                                             CS256 cs256) {
        return new ListReference(registry, blockNbr, blockLength, cs256);
    }
}

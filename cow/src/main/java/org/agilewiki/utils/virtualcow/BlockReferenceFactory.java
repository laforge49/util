package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.BaseFactory;
import org.agilewiki.utils.immutable.FactoryRegistry;
import org.agilewiki.utils.immutable.ImmutableFactory;
import org.agilewiki.utils.immutable.scalars.CS256;
import org.agilewiki.utils.immutable.scalars.CS256Factory;

import java.nio.ByteBuffer;

/**
 * Defines how a BlockReference is serialized / deserialized.
 */
public class BlockReferenceFactory extends BaseFactory {

    /**
     * the CS256 factory.
     */
    final public CS256Factory cs256Factory;

    /**
     * Create and register the factory.
     *
     * @param registry        The registry where the factory is registered.
     */
    public BlockReferenceFactory(DbFactoryRegistry registry) {
        super(registry, registry.blockReferenceFactoryId);
        cs256Factory = (CS256Factory) registry.getImmutableFactory(CS256.class);
    }

    /**
     * Create and register the factory.
     *
     * @param registry        The registry where the factory is registered.
     * @param id              The char used to identify the factory.
     */
    protected BlockReferenceFactory(DbFactoryRegistry registry, char id) {
        super(registry, id);
        cs256Factory = (CS256Factory) registry.getImmutableFactory(CS256.class);
    }

    @Override
    public char getId() {
        return id;
    }

    @Override
    public Class getImmutableClass() {
        return BlockReference.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        return ((BlockReference) immutable).getDurableLength();
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        ((BlockReference) immutable).serialize(byteBuffer);
    }

    @Override
    public Object deserialize(ByteBuffer byteBuffer) {
        int blockNbr = byteBuffer.getInt();
        int blockLength = byteBuffer.getInt();
        ImmutableFactory factory = factoryRegistry.readId(byteBuffer);
        CS256 cs256 = (CS256) factory.deserialize(byteBuffer);
        return new BlockReference(((DbFactoryRegistry) factoryRegistry).db, blockNbr, blockLength, cs256);
    }
}

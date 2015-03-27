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
     * The database.
     */
    final public Db db;

    /**
     * the CS256 factory.
     */
    final public CS256Factory cs256Factory;

    /**
     * Create and register the factory.
     *
     * @param factoryRegistry The registry where the factory is registered.
     * @param id              The char used to identify the factory.
     */
    public BlockReferenceFactory(FactoryRegistry factoryRegistry, char id, Db db) {
        super(factoryRegistry, id);
        this.db = db;
        cs256Factory = (CS256Factory) factoryRegistry.getImmutableFactory(CS256.class);
    }

    @Override
    public Class getImmutableClass() {
        return BlockReference.class;
    }

    @Override
    public int getDurableLength(Object immutable) {
        return 2 + 4 + 4 + CS256Factory.DURABLE_LENGTH;
    }

    @Override
    public void serialize(Object immutable, ByteBuffer byteBuffer) {
        BlockReference blockReference = (BlockReference) immutable;
        byteBuffer.putInt(blockReference.blockNbr);
        byteBuffer.putInt(blockReference.blockLength);
        cs256Factory.writeDurable(blockReference.cs256, byteBuffer);
    }

    @Override
    public Object deserialize(ByteBuffer byteBuffer) {
        int blockNbr = byteBuffer.getInt();
        int blockLength = byteBuffer.getInt();
        ImmutableFactory factory = factoryRegistry.readId(byteBuffer);
        CS256 cs256 = (CS256) factory.deserialize(byteBuffer);
        return new BlockReference(db, blockNbr, blockLength, cs256);
    }
}

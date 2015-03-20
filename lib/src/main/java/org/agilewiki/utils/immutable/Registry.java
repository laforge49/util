package org.agilewiki.utils.immutable;

import org.agilewiki.utils.immutable.collections.VersionedListNode;
import org.agilewiki.utils.immutable.collections.VersionedListNodeFactory;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;
import org.agilewiki.utils.immutable.collections.VersionedMapNodeFactory;
import org.agilewiki.utils.immutable.scalars.*;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the factory registry.
 */
public class Registry implements FactoryRegistry {
    public final VersionedListNode nilVersionedList;
    public final VersionedMapNode nilVersionedMap;

    protected final ConcurrentHashMap<Character, ImmutableFactory> idMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);
    protected final ConcurrentHashMap<Class, ImmutableFactory> classMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);

    /**
     * Creates the registry and registers the default factories.
     */
    public Registry() {
        new NullFactory(this, NULL_ID); // 'N'
        new StringFactory(this, 'S');
        new DoubleFactory(this, 'D');
        new BooleanFactory(this, 'B', 't', 'f');
        new FloatFactory(this, 'F');
        new IntegerFactory(this, 'I');
        new LongFactory(this, 'L');
        nilVersionedList = new VersionedListNodeFactory(this, 'l', '1').nilVersionedList;
        nilVersionedMap = new VersionedMapNodeFactory(this, 'm', '2', nilVersionedList).nilVersionedMap;
        new CS256Factory(this, 'c');
    }

    @Override
    public void register(ImmutableFactory factory) {
        idMap.put(factory.getId(), factory);
        classMap.put(factory.getImmutableClass(), factory);
    }

    @Override
    public ImmutableFactory getImmutableFactory(char id) {
        return idMap.get(id);
    }

    @Override
    public ImmutableFactory readId(ByteBuffer byteBuffer) {
        char id = byteBuffer.getChar();
        ImmutableFactory factory = idMap.get(id);
        if (factory == null)
            throw new IllegalStateException("Unknown durable id: " + id);
        return factory;
    }

    @Override
    public ImmutableFactory getImmutableFactory(Object immutable) {
        Class c = immutable == null ? NullFactory.class : immutable.getClass();
        ImmutableFactory factory = classMap.get(c);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown class: " + c.getName());
        }
        return factory.getImmutableFactory(immutable);
    }
}

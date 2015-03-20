package org.agilewiki.utils.cow;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the factory registry.
 */
public class Registry implements FactoryRegistry {

    protected final ConcurrentHashMap<Character, ImmutableFactory> idMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);
    protected final ConcurrentHashMap<Class, ImmutableFactory> classMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);

    /**
     * Creates the registry and registers the default factories.
     */
    public Registry() {
        new NullFactory(this, NULL_ID);
        new StringFactory(this, 'S');
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

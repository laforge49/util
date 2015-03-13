package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A durable factory registry.
 */
public class FactoryRegistry {

    protected final static ConcurrentHashMap<Character, DurableFactory> idMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);
    protected final static ConcurrentHashMap<Class, DurableFactory> classMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);

    /**
     * Initialize the factory registry.
     */
    static {
        NullFactory.register();
        IntegerFactory.register();
        FloatFactory.register();
        LongFactory.register();
        DoubleFactory.register();
        BooleanFactory.register();
        TrueFactory.register();
        FalseFactory.register();
        StringFactory.register();
    }

    public static DurableFactory getDurableFactory(char id) {
        return idMap.get(id);
    }

    /**
     * Register a durable factory.
     *
     * @param durableFactory    The factory to be registered.
     */
    public static void register(DurableFactory durableFactory) {
        idMap.put(durableFactory.getId(), durableFactory);
        classMap.put(durableFactory.getDurableClass(), durableFactory);
    }

    /**
     * Read an id and map it to a durable factory instance.
     *
     * @param byteBuffer    The byte buffer to be read.
     * @return The durable factory instance.
     * @throws java.lang.IllegalStateException when the durable id is not recognized.
     */
    public static DurableFactory readId(ByteBuffer byteBuffer) {
        char id = byteBuffer.getChar();
        DurableFactory durableFactory = idMap.get(id);
        if (durableFactory == null)
            throw new IllegalStateException("Unknown durable id: " + id);
        return durableFactory;
    }

    /**
     * Map a durable object to an durable factory instance.
     * Nulls are mapped to the registered NullFactory.
     *
     * @param durable    The durable object, or null.
     * @return The durable factory.
     * @throws java.lang.IllegalArgumentException when the durable class is not recognized.
     */
    public static DurableFactory getDurableFactory(Object durable) {
        Class c = durable == null ? NullFactory.class : durable.getClass();
        DurableFactory durableFactory = classMap.get(c);
        if (durableFactory == null) {
            throw new IllegalArgumentException("Unknown class: " + c.getName());
        }
        return durableFactory.getDurableFactory(durable);
    }
}

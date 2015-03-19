package org.agilewiki.utils.lazydurable;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A durable factory registry.
 */
public class FactoryRegistry {

    protected final static ConcurrentHashMap<Character, org.agilewiki.utils.lazydurable.LazyDurableFactory> idMap =
            new ConcurrentHashMap<>(16, 0.75f, 1);
    protected final static ConcurrentHashMap<Class, org.agilewiki.utils.lazydurable.LazyDurableFactory> classMap =
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
        org.agilewiki.utils.lazydurable.LazyDurableListNodeFactory.register();
        org.agilewiki.utils.lazydurable.NilListNodeFactory.register();
        org.agilewiki.utils.lazydurable.LazyDurableMapNodeFactory.register();
        org.agilewiki.utils.lazydurable.NilMapNodeFactory.register();
    }

    public static org.agilewiki.utils.lazydurable.LazyDurableFactory getDurableFactory(char id) {
        return idMap.get(id);
    }

    /**
     * Register a durable factory.
     *
     * @param lazyDurableFactory The factory to be registered.
     */
    public static void register(org.agilewiki.utils.lazydurable.LazyDurableFactory lazyDurableFactory) {
        idMap.put(lazyDurableFactory.getId(), lazyDurableFactory);
        classMap.put(lazyDurableFactory.getDurableClass(), lazyDurableFactory);
    }

    /**
     * Read an id and map it to a durable factory instance.
     *
     * @param byteBuffer The byte buffer to be read.
     * @return The durable factory instance.
     * @throws IllegalStateException when the durable id is not recognized.
     */
    public static org.agilewiki.utils.lazydurable.LazyDurableFactory readId(ByteBuffer byteBuffer) {
        char id = byteBuffer.getChar();
        org.agilewiki.utils.lazydurable.LazyDurableFactory lazyDurableFactory = idMap.get(id);
        if (lazyDurableFactory == null)
            throw new IllegalStateException("Unknown durable id: " + id);
        return lazyDurableFactory;
    }

    /**
     * Map a durable object to an durable factory instance.
     * Nulls are mapped to the registered NullFactory.
     *
     * @param durable The durable object, or null.
     * @return The durable factory.
     * @throws IllegalArgumentException when the durable class is not recognized.
     */
    public static org.agilewiki.utils.lazydurable.LazyDurableFactory getDurableFactory(Object durable) {
        Class c = durable == null ? NullFactory.class : durable.getClass();
        org.agilewiki.utils.lazydurable.LazyDurableFactory lazyDurableFactory = classMap.get(c);
        if (lazyDurableFactory == null) {
            throw new IllegalArgumentException("Unknown class: " + c.getName());
        }
        return lazyDurableFactory.getDurableFactory(durable);
    }
}

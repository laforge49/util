package org.agilewiki.utils.immutable;

import org.agilewiki.utils.immutable.collections.*;
import org.agilewiki.utils.immutable.scalars.*;

/**
 * Initialize the factory registry.
 */
public class BaseRegistry extends CascadingRegistry {

    /**
     * Creates the registry and registers the default factories.
     */
    public BaseRegistry() {
        new NullFactory(this, NULL_ID); // 'N'
        new StringFactory(this, 'S');
        new DoubleFactory(this, 'D');
        new BooleanFactory(this, 'B', 't', 'f');
        new FloatFactory(this, 'F');
        new IntegerFactory(this, 'I');
        new LongFactory(this, 'L');
        new CS256Factory(this, 'c');
    }
}

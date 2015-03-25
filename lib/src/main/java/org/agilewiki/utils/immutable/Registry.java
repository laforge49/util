package org.agilewiki.utils.immutable;

import org.agilewiki.utils.immutable.collections.*;
import org.agilewiki.utils.immutable.scalars.*;

/**
 * Initialize the factory registry.
 */
public class Registry extends CascadingRegistry {
    public final VersionedListNode nilVersionedList;
    public final VersionedMapNode nilVersionedMap;
    public final ListNode nilList;
    public final MapNode nilMap;

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
        nilList = new ListNodeFactory(this, 'n', '3').nilList;
        nilMap = new MapNodeFactory(this, 'o', '4', nilList).nilMap;
    }
}

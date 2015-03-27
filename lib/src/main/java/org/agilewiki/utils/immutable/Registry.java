package org.agilewiki.utils.immutable;

import org.agilewiki.utils.immutable.collections.*;

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
        this(new BaseRegistry());
    }

    /**
     * Create a cascading factory registry.
     *
     * @param parent The parent registry.
     */
    public Registry(CascadingRegistry parent) {
        super(parent);
        nilVersionedList = new VersionedListNodeFactory(this, 'l', '1').nilVersionedList;
        nilVersionedMap = new VersionedMapNodeFactory(this, 'm', '2', nilVersionedList).nilVersionedMap;
        nilList = new ListNodeFactory(this, 'n', '3').nilList;
        nilMap = new MapNodeFactory(this, 'o', '4', nilList).nilMap;
    }
}

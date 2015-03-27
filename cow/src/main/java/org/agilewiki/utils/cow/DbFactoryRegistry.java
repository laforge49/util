package org.agilewiki.utils.cow;

import org.agilewiki.utils.immutable.CascadingRegistry;
import org.agilewiki.utils.immutable.collections.*;

/**
 * Initialize the factory registry.
 */
public class DbFactoryRegistry extends CascadingRegistry {
    public final Db db;

    public final VersionedListNode nilVersionedList;
    public final VersionedMapNode nilVersionedMap;
    public final ListNode nilList;
    public final MapNode nilMap;
    public final BlockReferenceFactory blockReferenceFactory;

    /**
     * Create a cascading factory registry.
     *
     * @param db     The database.
     * @param parent The parent registry.
     */
    public DbFactoryRegistry(Db db, CascadingRegistry parent) {
        super(parent);
        this.db = db;
        nilVersionedList = new VersionedListNodeFactory(this, 'l', '1').nilVersionedList;
        nilVersionedMap = new VersionedMapNodeFactory(this, 'm', '2', nilVersionedList).nilVersionedMap;
        nilList = new ListNodeFactory(this, 'n', '3').nilList;
        nilMap = new MapNodeFactory(this, 'o', '4', nilList).nilMap;
        blockReferenceFactory = new BlockReferenceFactory(this, 'r', db);
    }
}

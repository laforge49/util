package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.immutable.CascadingRegistry;
import org.agilewiki.utils.virtualcow.collections.*;

/**
 * Initialize the factory registry.
 */
public class DbFactoryRegistry extends CascadingRegistry {
    public final Db db;

    public final VersionedListNode nilVersionedList;
    public final VersionedMapNode nilVersionedMap;
    public final ListNode nilList;
    public final MapNodeFactory mapNodeFactory;
    public final char mapNodeImplId = 'o';
    public final MapNode nilMap;
    public final char nilMapId = '4';
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
        mapNodeFactory = new MapNodeFactory(this, mapNodeImplId, nilMapId);
        nilMap = mapNodeFactory.nilMap;
        blockReferenceFactory = new BlockReferenceFactory(this, 'r', db);
    }
}

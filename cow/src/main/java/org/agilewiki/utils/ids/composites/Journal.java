package org.agilewiki.utils.ids.composites;

import org.agilewiki.utils.ids.NameId;
import org.agilewiki.utils.ids.Timestamp;
import org.agilewiki.utils.ids.ValueId;
import org.agilewiki.utils.immutable.collections.EmptyIterable;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;
import org.agilewiki.utils.immutable.collections.VersionedMapNode;
import org.agilewiki.utils.virtualcow.Db;

import java.util.Iterator;

/**
 * Connecting journal entries to the things they update.
 */
public class Journal {
    /**
     * Prefix to a timestamp/id composite.
     */
    public static final String MODIFIES_ID = "$A";

    /**
     * The key to the list of timestamps identifying the
     * journal entry which modified the vmn.
     */
    public static final String JOURNAL_ID = "$B";

    /**
     * Returns a composite id used to connect a journal entry to what it modifies.
     *
     * @param timestampId    The id of the journal entry.
     * @param id           The id of the modified versioned map list.
     * @return A composite of 3 ids.
     */
    public static String modifiesId(String timestampId, String id) {
        Timestamp.validateId(timestampId);
        ValueId.validateAnId(id);
        return MODIFIES_ID + timestampId + id;
    }

    public static String journalId(String id) {
        ValueId.validateAnId(id);
        return JOURNAL_ID + id;
    }

    /**
     * Returns the last id in a composite.
     *
     * @param composite    A composite of several ids.
     * @return The last id in the composite.
     */
    public static String lastId(String composite) {
        return composite.substring(composite.lastIndexOf('$'));
    }

    /**
     * Iterates over the ids of the Versioned Map Lists (VMLs) modified by the
     * given journal entry.
     *
     * @param db             The database.
     * @param timestampId    The timestampId of the journal entry.
     * @return The iterable.
     */
    public static Iterable<String> modifies(Db db, String timestampId) {
        Timestamp.validateId(timestampId);
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> it = ma.iterator(MODIFIES_ID + timestampId);
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public String next() {
                        return lastId((String) it.next().key());
                    }
                };
            }
        };
    }

    /**
     * Iterates over the timestamps of the journal entries which modified a given
     * Virtual Map Node (VMN).
     *
     * @param db    The database.
     * @param id    The id of the VMN.
     * @return The iterable.
     */
    public static Iterable<String> journal(Db db, String id) {
        NameId.validateAnId(id);
        MapAccessor ma = db.mapAccessor();
        ListAccessor la = ma.listAccessor(journalId(id));
        if (la == null) {
            return new EmptyIterable<String>();
        }
        VersionedMapNode vmn = (VersionedMapNode) la.get(0);
        Iterator<ListAccessor> lait = vmn.iterator(db.getTimestamp());
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return lait.hasNext();
                    }

                    @Override
                    public String next() {
                        return lait.next().key().toString();
                    }
                };
            }
        };
    }
}

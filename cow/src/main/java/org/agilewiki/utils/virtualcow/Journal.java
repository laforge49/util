package org.agilewiki.utils.virtualcow;

import org.agilewiki.utils.NameId;
import org.agilewiki.utils.Timestamp;
import org.agilewiki.utils.immutable.collections.ListAccessor;
import org.agilewiki.utils.immutable.collections.MapAccessor;

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
     * Prefix to an id/timestamp composite key.
     */
    public static final String JOURNAL_ID = "$B";

    /**
     * Returns a key used to connect a journal entry to what it modifies.
     *
     * @param timestampId    The id of the journal entry.
     * @param id           The id of the modified item.
     * @return A key with 3 ids.
     */
    public static String modifiesKey(String timestampId, String id) {
        Timestamp.validateId(timestampId);
        NameId.validateId(id);
        return MODIFIES_ID + timestampId + id;
    }

    /**
     * Returns a key used to connect an item with the journal entries which modified it.
     *
     * @param timestampId    The id of the journal entry.
     * @param id           The id of the modified item.
     * @return A key with 3 ids.
     */
    public static String journalEntryKey(String id, String timestampId) {
        Timestamp.validateId(timestampId);
        NameId.validateId(id);
        return JOURNAL_ID + id + timestampId;
    }

    /**
     * Returns the last id in a composite key.
     *
     * @param key    A composite of several ids.
     * @return The last id in the string.
     */
    public static String lastId(String key) {
        return key.substring(key.lastIndexOf('$'));
    }

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

    public static Iterable<String> journal(Db db, String id) {
        NameId.validateId(id);
        MapAccessor ma = db.mapAccessor();
        Iterator<ListAccessor> it = ma.iterator(JOURNAL_ID + id);
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
}

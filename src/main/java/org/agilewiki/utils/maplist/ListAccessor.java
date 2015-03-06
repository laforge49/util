package org.agilewiki.utils.maplist;

import java.util.List;

/**
 * Accesses a versioned list for a given time.
 */
public interface ListAccessor {

    /**
     * Returns the time being accessed.
     *
     * @return The time being accessed.
     */
    long time();

    /**
     * Returns a value if it is in range and the value exists for the given time.
     *
     * @param ndx  The index of the selected value.
     * @return A value, or null.
     */
    Object get(int ndx);

    /**
     * Returns the index of an existing value higher than the given index.
     *
     * @param ndx    A given index.
     * @return An index of an existing value that is higher, or -1.
     */
    int higherIndex(int ndx);

    /**
     * Returns the index of an existing value higher than or equal to the given index.
     *
     * @param ndx  A given index.
     * @return An index of an existing value that is higher or equal, or -1.
     */
    int ceilingIndex(int ndx);

    /**
     * Returns the index of an existing value lower than the given index.
     *
     * @param ndx  A given index.
     * @return An index of an existing value that is lower, or -1.
     */
    int lowerIndex(int ndx);

    /**
     * Returns true if there are no values present for the given time.
     *
     * @return Returns true if the list is empty for the given time.
     */
    boolean isEmpty();

    /**
     * Returns a list of all the values that are present for a given time.
     *
     * @return A list of all values present for the given time.
     */
    List flat();
}

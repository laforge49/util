package org.agilewiki.utils;

/**
 * Prefices a name with $n.
 * This is used when an identifier is needed which is neither random nor a timestamp.
 */
public class NameId {
    public static final String PREFIX = "$n";

    /**
     * Generate an id by prefixing a name with $n.
     *
     * @param name    A string that does not contain $.
     * @return The string $n + name.
     */
    public String generate(String name) {
        if (name.contains("$"))
            throw new IllegalArgumentException("may not contain $");
        return PREFIX + name;
    }
}

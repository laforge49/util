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
     * @param name    A string that does not contain a space, ! , quote, #, $
     *                or lessor character.
     * @return The string $n + name.
     */
    public static String generate(String name) {
        for (char c: name.toCharArray()) {
            if (c <= '$')
                throw new IllegalArgumentException("may not contain char <= $");
        }
        return PREFIX + name;
    }
}

package org.agilewiki.utils.ids;

/**
 * Usable only as a postfix to a composite id, there are no restrictions on the
 * content of a ValueId.
 */
public class ValueId {
    public static final String PREFIX = "$v";

    /**
     * Generate an id by prefixing a string value with $v.
     *
     * @param value    A string.
     * @return The string $v + value.
     */
    public static String generate(String value) {
        return PREFIX + value;
    }
}

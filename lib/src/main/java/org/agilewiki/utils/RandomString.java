package org.agilewiki.utils;

import java.security.SecureRandom;

/**
 * Creates a random hex string, prefaced with $i.
 */
public class RandomString {
    public static final String PREFIX = "$i";
    private SecureRandom secureRandom = new SecureRandom();

    private String generate64() {
        return Long.toHexString(secureRandom.nextLong());
    }

    /**
     * Returns a 32 digit random hex string.
     *
     * @return A secure random identifier that starts with $i.
     */
    public synchronized String generate() {
        return PREFIX+generate64() + generate64();
    }
}

package org.agilewiki.utils;

import java.security.SecureRandom;

/**
 * Creates a random hex string.
 */
public class RandomString {
    private SecureRandom secureRandom = new SecureRandom();

    private String generate64() {
        return Long.toHexString(secureRandom.nextLong());
    }

    /**
     * Returns a 32 digit random hex string.
     *
     * @return A secure random identifier.
     */
    public synchronized String generate() {
        return generate64() + generate64();
    }
}

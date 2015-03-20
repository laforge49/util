package org.agilewiki.utils.immutable.scalars;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * An immutable 256-bit checksum inspired by zfs.
 */
public class CS256 {
    private final BitSet bitSet;

    public CS256(byte[] bytes) {
        bitSet = new BitSet(256);
        bitSet.flip(255);
        for (int i=0; i < bytes.length; i++) {
            bitSet.flip((((int) bytes[i]) - (int) Byte.MIN_VALUE + i * 7) % 256);
        }
    }

    public CS256(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer1 = (ByteBuffer) byteBuffer.slice().limit(32);
        bitSet = BitSet.valueOf(byteBuffer1);
        byteBuffer.position(byteBuffer.position() + 32);
    }

    public boolean equals(Object obj) {
        return bitSet.equals(((CS256) obj).bitSet);
    }

    public byte[] toByteArray() {
        return bitSet.toByteArray();
    }
}

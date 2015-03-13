package org.agilewiki.utils.durable;

import java.nio.ByteBuffer;

/**
 * Defines how a class of immutable is serialized / deserialized.
 */
public interface DurableFactory {
    /**
     * Returns a char used to identify the durable factory in a serialized object.
     *
     * @return A char used to identify the factory.
     */
    char getId();

    /**
     * Returns the class of an immutable that the factory serializes / deserializes.
     *
     * @return
     */
    Class getDurableClass();

    /**
     * Validate that the immutable object is a match for the factory.
     *
     * @param durable    The immutable object.
     * @throws java.lang.IllegalArgumentException when the object is not a match.
     */
    default void match(Object durable) {
        if (durable.getClass() != getDurableClass())
            throw new IllegalArgumentException("The immutable object is not a match");
    }

    /**
     * Returns the size of a byte array needed to serialize the durable object.
     * This does not include the space needed for the durable id.
     *
     * @param durable    The immutable object to be serialized.
     * @return The size in bytes of the serialized data.
     */
    int getDurableLength(Object durable);

    /**
     * Serialize an immutable object into a ByteBuffer.
     *
     * @param durable       The immutable object to be serialized.
     * @param byteBuffer    Where the serialized data is to be placed.
     */
    void serialize(Object durable, ByteBuffer byteBuffer);

    /**
     * Deserialize an immutable object from the content of a ByteBuffer.
     *
     * @param byteBuffer    Holds the data used to create the immutable object.
     * @return The deserialized object.
     */
    Object deserialize(ByteBuffer byteBuffer);
}

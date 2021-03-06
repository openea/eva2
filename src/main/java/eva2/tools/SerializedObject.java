package eva2.tools;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class stores an object serialized in memory. It allows compression,
 * to be used to conserve memory (for example, when storing large strings
 * in memory), or can be used as a mechanism for deep copying objects.
 *
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 */
public class SerializedObject implements Serializable {
    /**
     * Stores the serialized object
     */
    private byte[] serializedBytes;
    /**
     * True if the object has been compressed during storage
     */
    private boolean isCompressed;

    /**
     * Serializes the supplied object into a byte array without compression.
     *
     * @param obj the Object to serialize.
     * @throws IOException
     */
    public SerializedObject(Object obj) throws IOException {
        this(obj, false);
    }

    /**
     * Serializes the supplied object into a byte array.
     *
     * @param obj      the Object to serialize.
     * @param compress true if the object should be stored compressed.
     * @throws IOException
     */
    public SerializedObject(Object obj, boolean compress) throws IOException {
        isCompressed = compress;
        serializedBytes = toByteArray(obj, isCompressed);
    }

    /**
     * Serializes the supplied object to a byte array.
     *
     * @param obj      the Object to serialize
     * @param compress true if the object should be compressed.
     * @return the byte array containing the serialized object.
     * @throws IOException   if the object is not Serializable.
     */
    protected static byte[] toByteArray(Object obj, boolean compress) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        OutputStream os = bo;
        if (compress) {
            os = new GZIPOutputStream(os);
        }
        os = new BufferedOutputStream(os);
        ObjectOutputStream oo = new ObjectOutputStream(os);
        oo.writeObject(obj);
        oo.close();
        return bo.toByteArray();
    }

    /**
     * Gets the object stored in this SerializedObject. The object returned
     * will be a deep copy of the original stored object.
     *
     * @return the deserialized Object.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object getObject() throws IOException, ClassNotFoundException {
//		try {
        InputStream is = new ByteArrayInputStream(serializedBytes);
        if (isCompressed) {
            is = new GZIPInputStream(is);
        }
        is = new BufferedInputStream(is);
        ObjectInputStream oi = new ObjectInputStream(is);
        Object result = oi.readObject();
        oi.close();
        return result;
    }

    /**
     * Compares this object with another for equality.
     *
     * @param other the other Object.
     * @return true if the objects are equal.
     */
    @Override
    public final boolean equals(Object other) {

        // Check class type
        if ((other == null) || !(other.getClass().equals(this.getClass()))) {
            return false;
        }
        // Check serialized length
        byte[] os = ((SerializedObject) other).serializedBytes;
        if (os.length != serializedBytes.length) {
            return false;
        }
        // Check serialized contents
        for (int i = 0; i < serializedBytes.length; i++) {
            if (serializedBytes[i] != os[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hashcode for this object.
     *
     * @return the hashcode for this object.
     */
    @Override
    public final int hashCode() {
        return serializedBytes.length;
    }

    /**
     * Returns a text representation of the state of this object.
     *
     * @return a String representing this object.
     */
    @Override
    public String toString() {
        return (isCompressed ? "Compressed object: " : "Uncompressed object: ")
                + serializedBytes.length + " bytes";
    }
}

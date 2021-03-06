/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eva2.tools;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author becker
 */
public class SerializerTest {

    private ExampleDataStruct dataStructObject;

    public SerializerTest() {
    }

    @Before
    public void setUp() {
        // Create a eva2.problems.simple object graph
        dataStructObject = new ExampleDataStruct();
        dataStructObject.message = "hello world";
        dataStructObject.data = new int[]{1, 2, 3, 4};
        dataStructObject.other = new ExampleDataStruct();
        dataStructObject.other.message = "nested structure";
        dataStructObject.other.data = new int[]{9, 8, 7};
    }

    /**
     * Test of deepClone method, of class Serializer.
     */
    @Test
    public void testDeepClone() {
        ExampleDataStruct copy = (ExampleDataStruct) Serializer.deepClone(dataStructObject);
        assertNotSame("Objects are the same", copy, dataStructObject);
    }

    /**
     * Test of storeString method, of class Serializer.
     */
    @Test
    public void testStoreString() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        String data = "This is a test string to write.";
        Serializer.storeString(outStream, data);
        String writtenData = new String(outStream.toByteArray());
        assertEquals("Wrong data in stream", data, writtenData);
    }

    /**
     * Test of loadString method, of class Serializer.
     */
    @Test
    public void testLoadString() {
        String data = "This is a test string to read.";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        String readData = Serializer.loadString(inputStream);
        assertEquals("Wrong data in stream", data, readData);
    }

    /**
     * Test of storeObject method, of class Serializer.
     */
    @Test
    public void testStoreObject() {
        OutputStream outStream = null;
        Serializable s = null;
        Serializer.storeObject(outStream, s);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of loadObject method, of class Serializer.
     */
    @Test
    public void testLoadObject_InputStream() {
        InputStream inputStream = null;
        Object expResult = null;
        Object result = Serializer.loadObject(inputStream);
        assertEquals(expResult, result);
    }
}

/**
 * This is a eva2.problems.simple serializable data structure that we use below for testing the methods above
 *
 */
class ExampleDataStruct implements Serializable {

    String message;
    int[] data;
    ExampleDataStruct other;

    @Override
    public String toString() {
        String msg = message;
        for (int i = 0; i < data.length; i++) {
            msg += " " + data[i];
        }
        if (other != null) {
            msg += "\n\t" + other.toString();
        }
        return msg;
    }
}
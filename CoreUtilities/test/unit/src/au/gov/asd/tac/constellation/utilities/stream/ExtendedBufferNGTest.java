/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.utilities.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class ExtendedBufferNGTest {
    
    final private int size = 10;
    
    public ExtendedBufferNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test the default constructor - essentially just show it creates the
     * 
     */
    @Test
    public void testDefaultConstructor() throws Exception {

        ExtendedBuffer localBuffer = new ExtendedBuffer(size);
        
        Field bufferSize = ExtendedBuffer.class.getDeclaredField("bufferSize");
        bufferSize.setAccessible(true);
        assertEquals(size, (int)bufferSize.get(localBuffer));
        assertEquals(localBuffer.getAvailableSize(), 0);
    }

    /**
     * Test of getOutputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testOutputStream() throws Exception {

        ExtendedBuffer buffer = new ExtendedBuffer(size);
        OutputStream outputStream = buffer.getOutputStream();
        byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();

        try {
            outputStream.write(bytes, 0, bytes.length);
            outputStream.write(1);
            outputStream.write(2);
            outputStream.write(-2147483648); // lowest 8 bits = 00000000 = 0
            outputStream.write(2147483647);  // lowest 8 bits = 11111111 = -1 (signed byte)
            
        } finally {
            outputStream.close();
        }
        
        // Show that entire bytes array has been read into buffer, despite exceeding buffer size
        assertEquals(buffer.getAvailableSize(), bytes.length + 4);
        
        // Extract the data from buffer and confirm is matches what was iused to
        // populate the buffer
        byte[] outBytes = buffer.getData();
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(outBytes[i], i + 65);
        }
        assertEquals(outBytes[bytes.length], 1);
        assertEquals(outBytes[bytes.length + 1], 2);
        assertEquals(outBytes[bytes.length + 2], 0);
        assertEquals(outBytes[bytes.length + 3], -1);
    }
    
    /**
     * Test of getInputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testInputStreamReadByte() throws Exception {
        
        ExtendedBuffer buffer = new ExtendedBuffer(size);
        OutputStream outputStream = buffer.getOutputStream();
        InputStream inputStream = buffer.getInputStream();

        byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        
        // Populate data to write from inputStream
        try {
            outputStream.write(bytes, 0, bytes.length);
        } finally {
            outputStream.close();
        }
        
        // Ready the initial content out one byte at a time
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(inputStream.read(), i + 65);
        }
    }
    
    /**
     * Test of getInputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testInputStreamReadArray() throws Exception {
        
        ExtendedBuffer buffer = new ExtendedBuffer(size);
        OutputStream outputStream = buffer.getOutputStream();
        InputStream inputStream = buffer.getInputStream();

        byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        
        // Populate data to write from inputStream
        try {
            outputStream.write(bytes, 0, bytes.length);
        } finally {
            outputStream.close();
        }
        
        byte[] readBytes = new byte[21];
        int result = inputStream.read(readBytes, 0, 21);
    }

    /**
     * Test of getData method, of class ExtendedBuffer.
     */
    @Test
    public void testGetData() throws Exception {
        
        ExtendedBuffer buffer = new ExtendedBuffer(size);
        OutputStream outputStream = buffer.getOutputStream();
        InputStream inputStream = buffer.getInputStream();

        byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        
        // Populate data to write from inputStream
        try {
            outputStream.write(bytes, 0, bytes.length);
        } finally {
            outputStream.close();
        }
        
        byte[] result = buffer.getData();
        
        // Ready the initial content out
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], result[i]);
        }
        
        
    }
    
}

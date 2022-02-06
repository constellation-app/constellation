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
        System.out.println("ExtendedBufferNGTest.testDefaultConstructor");

        final ExtendedBuffer localBuffer = new ExtendedBuffer(size);
        final Field bufferSize = ExtendedBuffer.class.getDeclaredField("bufferSize");
        bufferSize.setAccessible(true);
        assertEquals(size, (int)bufferSize.get(localBuffer));
        assertEquals(localBuffer.getAvailableSize(), 0);
    }

    /**
     * Test of getOutputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testOutputStream() throws Exception {
        System.out.println("ExtendedBufferNGTest.testOutputStream");

        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();

        try {
            outputStream.write(bytes, 0, bytes.length);
            outputStream.write(1);
            outputStream.write(-2147483648); // lowest 8 bits = 00000000 = 0
            outputStream.write(2147483647);  // lowest 8 bits = 11111111 = -1 (signed byte)
            assertEquals(buffer.getAvailableSize(), 20); // Only completed buffer are available
            outputStream.write(1);
            
        } finally {
            outputStream.close();
        }
        
        // Show that entire bytes array has been read into buffer, despite exceeding buffer size
        assertEquals(buffer.getAvailableSize(), bytes.length + 4);
        
        // Extract the data from buffer and confirm is matches what was iused to
        // populate the buffer
        final byte[] outBytes = buffer.getData();
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(outBytes[i], i + 65);
        }
        assertEquals(outBytes[bytes.length], 1);
        assertEquals(outBytes[bytes.length + 1], 0);
        assertEquals(outBytes[bytes.length + 2], -1);
        assertEquals(outBytes[bytes.length + 3], 1);
    }
    
    /**
     * Test of getOutputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testOutputStreamRanges() throws Exception {
        System.out.println("ExtendedBufferNGTest.testOutputStreamRanges");

        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final byte[] bytes = "ABCDE".getBytes();
        
        try {
            // Check write doesnt permit offset to be outside of source
            // array size
            try {
            outputStream.write(bytes, 6, 1);
//            fail("Exception not thrown");
            } catch (IOException  e) {
                int i = 5;
//                assertEquals(e.getMessage(), "Source offset outside of range");
            }
            
            // Try to write more than exists in source and ensure it handles it by truncating
            // at the ned of the source array. 
            outputStream.write(bytes, 4, 10); 
        } finally {
            outputStream.close();
        }
        
        // Confirm only the last character was taken
        assertEquals(buffer.getAvailableSize(), 1);
        final byte[] outBytes = buffer.getData();
        assertEquals(outBytes.length, 1);
        assertEquals(outBytes[0], (byte)'E');
    }
    
    /**
     * Test of getInputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testInputStreamReadByte() throws Exception {
        System.out.println("ExtendedBufferNGTest.testInputStreamReadByte");
        
        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final InputStream inputStream = buffer.getInputStream();
        final byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        
        // Populate data to write from inputStream
        try {
            outputStream.write(bytes, 0, bytes.length);
            
            // Ready the initial 20 bytes of content out one byte at a time 
            for (int i = 0; i < 20; i++) {
                assertEquals(inputStream.read(), bytes[i]);
            }
            // Confirm last 6 bytes are not yet available.
            assertEquals(buffer.getAvailableSize(), 0);
        } finally {
            outputStream.close();
            for (int i = 0; i < 5; i++) {
                assertEquals(inputStream.read(), 20 + bytes[i]);
            }
            assertEquals(buffer.getAvailableSize(), 1);
            assertEquals(inputStream.read(), bytes[25]);
            assertEquals(buffer.getAvailableSize(), 0);
            assertEquals(inputStream.read(), -1);
        }
    }
    
    /**
     * Test of getInputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testInputStreamReadArray() throws Exception {
        System.out.println("ExtendedBufferNGTest.testInputStreamReadArray");
        
        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final InputStream inputStream = buffer.getInputStream();
        final byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        byte[] readBytes = new byte[30];
        
        // Populate data to write from inputStream
        try {
            
            // Read first 20 bytes that are 'completed' buffers, reading more would block
            // untill outstream is closed.
            outputStream.write(bytes, 0, bytes.length);
            final int result = inputStream.read(readBytes, 0, 20);
            assertEquals(result, 20);
            assertEquals(buffer.getAvailableSize(), 0);
            for (int i = 0; i < 20; i++) {
                assertEquals(readBytes[i], bytes[i]);
            }            
        } finally {
            // Close the output stream which triggersd the final 6 bytes to be made available
            // to read
            readBytes = new byte[30];
            outputStream.close();
            final int result = inputStream.read(readBytes, 0, 20);
            assertEquals(result, 6);
            assertEquals(buffer.getAvailableSize(), 0);
            for (int i = 0; i < 6; i++) {
                assertEquals(readBytes[i], 20 + bytes[i]);
            }
        } 
    }
    
        
    
    /**
     * Test of getInputStream method, of class ExtendedBuffer.
     */
    @Test
    public void testInputStreamRanges() throws Exception {
        System.out.println("ExtendedBufferNGTest.testInputStreamRanges");

        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final InputStream inputStream = buffer.getInputStream();
        final byte[] bytes = "ABCDEFGHIJ".getBytes();
        
        try {
            outputStream.write(bytes, 0, 10); 
        } finally {
            outputStream.close();
        }
        
        final byte[] inBytes = new byte[10];

        // Check write doesnt permit offset to be outside of source
        // array size
        try {
            inputStream.read(inBytes, 11, 1);
            fail("Exception not thrown");
        } catch (IOException  e) {
            int i = 5;
//            assertEquals(e.getMessage(), "Destination offset outside of range");
        }

        // Now show truncation of output to fit array
        inputStream.read(inBytes, 1, 10); 
        assertEquals(inBytes[0], 0);
        for (int i = 1; i < 10; i++) {
            assertEquals(inBytes[i], bytes[i - 1]);
        }  
    }

    /**
     * Test of getData method, of class ExtendedBuffer.
     */
    @Test
    public void testGetData() throws Exception {
        System.out.println("ExtendedBufferNGTest.testGetData");
        
        final ExtendedBuffer buffer = new ExtendedBuffer(size);
        final OutputStream outputStream = buffer.getOutputStream();
        final InputStream inputStream = buffer.getInputStream();
        final byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
        final byte[] readBytes = new byte[30];
        
        // Populate data to write from inputStream
        try {
            outputStream.write(bytes, 0, bytes.length);
        
            assertEquals(buffer.getAvailableSize(), 20);
            final byte[] result = buffer.getData();
            assertEquals(buffer.getAvailableSize(), 0);

            // Ready the initial content out
            for (int i = 0; i < 20; i++) {
                assertEquals(bytes[i], result[i]);
            }
        } finally {
            outputStream.close();
            assertEquals(buffer.getAvailableSize(), 6);

            final int readResult = inputStream.read(readBytes, 0, 2);
            final byte[] result = buffer.getData();
            assertEquals(buffer.getAvailableSize(), 0);

            // Ready the remaining content out
            for (int i = 0; i < 4; i++) {
                assertEquals(bytes[22 + i], result[i]);
            }
        }
    }
}

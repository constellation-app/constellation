/*
 * Copyright 2010-2025 Australian Signals Directorate
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.https;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class HttpsUtilitiesNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getInputStream method, of class HttpsUtilities. Null connection
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testGetInputStreamNullConnection() throws Exception {
        System.out.println("getInputStreamNullConnection");

        HttpsUtilities.getInputStream(null);
    }
    
    /**
     * Test of getInputStream method, of class HttpsUtilities.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetInputStream() throws Exception {
        System.out.println("getInputStream");
        
        final HttpURLConnection mockNullEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockGzipEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockDeflateEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockOtherEncodedConnection = mock(HttpURLConnection.class);
        
        when(mockNullEncodedConnection.getContentEncoding()).thenReturn(null);
        when(mockGzipEncodedConnection.getContentEncoding()).thenReturn("gzip");
        when(mockDeflateEncodedConnection.getContentEncoding()).thenReturn("deflate");
        when(mockOtherEncodedConnection.getContentEncoding()).thenReturn("Something else");
        
        final InputStream mockStream = mock(InputStream.class);
        // can't mock the stream for the gzip instance
        final InputStream gzipStream = new FileInputStream(HttpsUtilitiesNGTest.class.getResource("resources/test.txt.gz").getPath());
        
        when(mockNullEncodedConnection.getInputStream()).thenReturn(mockStream);
        when(mockGzipEncodedConnection.getInputStream()).thenReturn(gzipStream);
        when(mockDeflateEncodedConnection.getInputStream()).thenReturn(mockStream);
        when(mockOtherEncodedConnection.getInputStream()).thenReturn(mockStream);

        final InputStream result1 = HttpsUtilities.getInputStream(mockNullEncodedConnection);
        assertEquals(result1, mockStream);
        final InputStream result2 = HttpsUtilities.getInputStream(mockGzipEncodedConnection);
        assertTrue(result2 instanceof GZIPInputStream);
        final InputStream result3 = HttpsUtilities.getInputStream(mockDeflateEncodedConnection);
        assertTrue(result3 instanceof InflaterInputStream);
        final InputStream result4 = HttpsUtilities.getInputStream(mockOtherEncodedConnection);
        assertEquals(result4, mockStream);
    }
    
    /**
     * Test of getErrorStream method, of class HttpsUtilities. Null connection
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testGetErrorStreamNullConnection() throws Exception {
        System.out.println("getErrorStreamNullConnection");

        HttpsUtilities.getErrorStream(null);
    }

    /**
     * Test of getErrorStream method, of class HttpsUtilities.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetErrorStream() throws Exception {
        System.out.println("getErrorStream");
        
        final HttpURLConnection mockNullEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockGzipEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockDeflateEncodedConnection = mock(HttpURLConnection.class);
        final HttpURLConnection mockOtherEncodedConnection = mock(HttpURLConnection.class);
        
        when(mockNullEncodedConnection.getContentEncoding()).thenReturn(null);
        when(mockGzipEncodedConnection.getContentEncoding()).thenReturn("gzip");
        when(mockDeflateEncodedConnection.getContentEncoding()).thenReturn("deflate");
        when(mockOtherEncodedConnection.getContentEncoding()).thenReturn("Something else");
        
        final InputStream mockStream = mock(InputStream.class);
        // can't mock the stream for the gzip instance
        final InputStream gzipStream = new FileInputStream(HttpsUtilitiesNGTest.class.getResource("resources/test.txt.gz").getPath());
        
        when(mockNullEncodedConnection.getErrorStream()).thenReturn(mockStream);
        when(mockGzipEncodedConnection.getErrorStream()).thenReturn(gzipStream);
        when(mockDeflateEncodedConnection.getErrorStream()).thenReturn(mockStream);
        when(mockOtherEncodedConnection.getErrorStream()).thenReturn(mockStream);

        final InputStream result1 = HttpsUtilities.getErrorStream(mockNullEncodedConnection);
        assertEquals(result1, mockStream);
        final InputStream result2 = HttpsUtilities.getErrorStream(mockGzipEncodedConnection);
        assertTrue(result2 instanceof GZIPInputStream);
        final InputStream result3 = HttpsUtilities.getErrorStream(mockDeflateEncodedConnection);
        assertTrue(result3 instanceof InflaterInputStream);
        final InputStream result4 = HttpsUtilities.getErrorStream(mockOtherEncodedConnection);
        assertEquals(result4, mockStream);
    }

    /**
     * Test of readErrorStreamAndThrowException method, of class HttpsUtilities. Null connection
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testReadErrorStreamAndThrowExceptionNullConnection() throws Exception {
        System.out.println("readErrorStreamAndThrowExceptionNullConnection");

        HttpsUtilities.readErrorStreamAndThrowException(null, null);
    }
    
    /**
     * Test of readErrorStreamAndThrowException method, of class HttpsUtilities. Null system
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = """
                                                                                    An error occurred with the null service: 101 Test response message
                                                                                    
                                                                                    If problems persist, contact support via Help -> Support
                                                                                    
                                                                                    Technical Error: This is a test""")
    public void testReadErrorStreamAndThrowExceptionNullSystem() throws Exception {
        System.out.println("readErrorStreamAndThrowExceptionNullSystem");
        
        final HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        final InputStream stream = new FileInputStream(HttpsUtilitiesNGTest.class.getResource("resources/test.txt").getPath());
        when(mockConnection.getErrorStream()).thenReturn(stream);
        when(mockConnection.getResponseCode()).thenReturn(101);
        when(mockConnection.getResponseMessage()).thenReturn("Test response message");
        
        HttpsUtilities.readErrorStreamAndThrowException(mockConnection, null);
    }
    
    /**
     * Test of readErrorStreamAndThrowException method, of class HttpsUtilities.
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = """
                                                                                    An error occurred with the My system service: 101 Test response message
                                                                                    
                                                                                    If problems persist, contact support via Help -> Support
                                                                                    
                                                                                    Technical Error: This is a test""")
    public void testReadErrorStreamAndThrowException() throws Exception {
        System.out.println("readErrorStreamAndThrowException");
        
        final HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        final InputStream stream = new FileInputStream(HttpsUtilitiesNGTest.class.getResource("resources/test.txt").getPath());
        when(mockConnection.getErrorStream()).thenReturn(stream);
        when(mockConnection.getResponseCode()).thenReturn(101);
        when(mockConnection.getResponseMessage()).thenReturn("Test response message");
        
        HttpsUtilities.readErrorStreamAndThrowException(mockConnection, "My system");
    }
}

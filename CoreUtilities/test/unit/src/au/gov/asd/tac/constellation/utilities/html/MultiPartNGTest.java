/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.html;

import au.gov.asd.tac.constellation.utilities.html.MultiPart.MultiPartException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javafx.util.Pair;
import javax.net.ssl.HttpsURLConnection;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class MultiPartNGTest {

    MultiPart multiPart;

    public MultiPartNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        multiPart = new MultiPart();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of addText method, of class MultiPart.
     */
    @Test
    public void testAddText() {
        System.out.println("addText");

        assertEquals(multiPart.size(), 0);
        multiPart.addText("testKey", "testValue");
        assertEquals(multiPart.size(), 97);
    }

    /**
     * Test of addText method, of class MultiPart. Trying to add after end is called
     */
    @Test(expectedExceptions = {MultiPartException.class}, expectedExceptionsMessageRegExp = "Not allowed after calling end\\(\\).")
    public void testAddTextBadAdd() {
        System.out.println("addTextBadAdd");

        multiPart.end();
        multiPart.addText("testKey", "testValue");
    }

    /**
     * Test of addBytes method, of class MultiPart.
     */
    @Test
    public void testAddBytes() {
        System.out.println("addBytes");

        assertEquals(multiPart.size(), 0);

        final String testContent = "testContent";
        multiPart.addBytes("testFilename", testContent.getBytes(), "application/octet-stream");
        assertEquals(multiPart.size(), 161);
    }

    /**
     * Test of addBytes method, of class MultiPart. Trying to add after end is called
     */
    @Test(expectedExceptions = {MultiPartException.class}, expectedExceptionsMessageRegExp = "Not allowed after calling end\\(\\).")
    public void testAddBytesBadAdd() {
        System.out.println("addBytesBadAdd");

        multiPart.end();
        multiPart.addBytes("testFilename", null, null);
    }

    /**
     * Test of end method, of class MultiPart.
     */
    @Test
    public void testEnd() {
        System.out.println("end");

        assertEquals(multiPart.size(), 0);

        multiPart.end();
        //confirm the end sequence bytes have been added
        assertEquals(multiPart.size(), 36);
    }

    /**
     * Test of end method, of class MultiPart. Trying to end more than once
     */
    @Test(expectedExceptions = {MultiPartException.class}, expectedExceptionsMessageRegExp = "Not allowed after calling end\\(\\).")
    public void testEndTwice() {
        System.out.println("endTwice");

        multiPart.end();
        multiPart.end();
    }

    /**
     * Test of getBoundary method, of class MultiPart.
     */
    @Test
    public void testGetBoundary() {
        System.out.println("getBoundary");

        try (final MockedStatic<UUID> uuidMockedStatic = Mockito.mockStatic(UUID.class)) {
            final UUID mockUuid = mock(UUID.class);
            when(mockUuid.toString()).thenReturn("1-2-3-4");

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUuid);

            final MultiPart instance = new MultiPart();
            assertEquals(instance.getBoundary(), "1234");
        }
    }

    /**
     * Test of getBuffer method, of class MultiPart.
     */
    @Test
    public void testGetBuffer() {
        System.out.println("getBuffer");

        try (final MockedStatic<UUID> uuidMockedStatic = Mockito.mockStatic(UUID.class)) {
            final UUID mockUuid = mock(UUID.class);
            when(mockUuid.toString()).thenReturn("1-2-3-4");

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUuid);

            final MultiPart instance = new MultiPart();

            instance.end();
            assertEquals(instance.getBuffer(), "--1234--".getBytes());
        }
    }

    /**
     * Test of getBuffer method, of class MultiPart. Try to get before end is called
     */
    @Test(expectedExceptions = {MultiPartException.class}, expectedExceptionsMessageRegExp = "Must call end\\(\\) first.")
    public void testGetBufferBeforeEnd() {
        System.out.println("getBufferBeforeEnd");

        multiPart.getBuffer();
    }

    /**
     * Test of post method, of class MultiPart. 200 response code
     * @throws java.lang.Exception
     */
    @Test
    public void testPostSuccess() throws Exception {
        System.out.println("postSuccess");

        final HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            when(mockConnection.getOutputStream()).thenReturn(stream);
            when(mockConnection.getResponseCode()).thenReturn(200);
            when(mockConnection.getHeaderField("Location")).thenReturn("testLocation");

            multiPart.end();
            assertEquals(stream.size(), 0);
            final Pair<String, String> postResult = multiPart.post(mockConnection);
            // assert that we get the expected result for the location value
            assertEquals(postResult.getValue(), "testLocation");
            assertEquals(stream.size(), 36);
        }
    }

    /**
     * Test of post method, of class MultiPart. Non-200 response code
     * @throws java.lang.Exception
     */
    @Test
    public void testPostFailed() throws Exception {
        System.out.println("postFailed");

        final HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            // since we're simulating something going wrong, we're not going to check the output stream
            // still add this in though to allow the function to complete
            when(mockConnection.getOutputStream()).thenReturn(stream);
            when(mockConnection.getResponseCode()).thenReturn(300);
            when(mockConnection.getHeaderField("Location")).thenReturn("testLocation");

            multiPart.end();
            final Pair<String, String> postResult = multiPart.post(mockConnection);
            // assert that we get the expected result for the location value
            assertEquals(postResult.getValue(), null);

        }
    }

    /**
     * Test of getBody method, of class MultiPart. 200 response code
     * @throws java.lang.Exception
     */
    @Test
    public void testGetBodySuccess() throws Exception {
        System.out.println("getBodySuccess");

        final HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        final byte[] input = "From the input stream".getBytes();
        final byte[] error = "From the error stream".getBytes();
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
                final ByteArrayInputStream errorStream = new ByteArrayInputStream(error)) {
            when(mockConnection.getInputStream()).thenReturn(inputStream);
            when(mockConnection.getErrorStream()).thenReturn(errorStream);

            final byte[] body = MultiPart.getBody(mockConnection, 200);
            assertEquals(body, input);
        }
    }

    /**
     * Test of getBody method, of class MultiPart. Non-200 response code
     * @throws java.lang.Exception
     */
    @Test
    public void testGetBodyFailed() throws Exception {
        System.out.println("getBodyFailed");

        final HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        final byte[] input = "From the input stream".getBytes();
        final byte[] error = "From the error stream".getBytes();
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
                final ByteArrayInputStream errorStream = new ByteArrayInputStream(error)) {
            when(mockConnection.getInputStream()).thenReturn(inputStream);
            when(mockConnection.getErrorStream()).thenReturn(errorStream);

            final byte[] body = MultiPart.getBody(mockConnection, 300);
            assertEquals(body, error);
        }
    }   
}
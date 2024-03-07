/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.file;

import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;
import javafx.scene.image.Image;
import javax.net.ssl.HttpsURLConnection;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.testfx.api.FxToolkit;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author groombridge34a
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DownloadImageUtilitiesNGTest extends ConstellationTest {
    
    private static final String TEST_IMAGE = "pixel.png";
    
    private static final int TIMEOUT = 10000;
    
    /**
     * Can get an image from the specified URL.
     * 
     * @throws IOException if mocking the HTTPS connection classes fails.
     */
    @Test
    public void testGetImage() throws IOException {
        final String url = "dummy";
        
        try (
                final MockedStatic<HttpsConnection> connMockedStatic = mockStatic(HttpsConnection.class);
                final MockedStatic<HttpsUtilities> utilsMockedStatic = mockStatic(HttpsUtilities.class);) 
        {
            // set up mocks
            final HttpsURLConnection urlConn = mock(HttpsURLConnection.class);
            when(urlConn.getResponseCode()).thenReturn(HttpsURLConnection.HTTP_OK);
            
            final HttpsConnection conn = mock(HttpsConnection.class);
            when(conn.acceptPng()).thenReturn(conn);
            when(conn.withReadTimeout(TIMEOUT)).thenReturn(conn);
            when(conn.get()).thenReturn(urlConn);
            
            connMockedStatic.when(() -> HttpsConnection.withUrl(url)).thenReturn(conn);

            final InputStream stream = this.getClass().getResourceAsStream(TEST_IMAGE);
            utilsMockedStatic.when(() -> HttpsUtilities.getInputStream(urlConn)).thenReturn(stream);
            
            // execute and assert
            final Image i = DownloadImageUtilities.getImage(url);
            assertEquals(i.getHeight(), 1D);
            assertEquals(i.getWidth(), 1D);
            assertNull(i.getException());
            assertFalse(i.isError());
            verify(urlConn).disconnect();
        }
    }
    
    /**
     * Null is returned when a HTTP error is received from the image endpoint.
     * 
     * @throws IOException if mocking the HTTPS connection classes fails.
     */
    @Test
    public void testHttpError() throws IOException {
        try (final MockedStatic<HttpsConnection> connMockedStatic = mockStatic(HttpsConnection.class)) {
            // set up mocks
            final HttpsURLConnection urlConn = mock(HttpsURLConnection.class);
            when(urlConn.getResponseCode()).thenReturn(HttpsURLConnection.HTTP_INTERNAL_ERROR);
            
            final HttpsConnection conn = mock(HttpsConnection.class);
            when(conn.acceptPng()).thenReturn(conn);
            when(conn.withReadTimeout(anyInt())).thenReturn(conn);
            when(conn.get()).thenReturn(urlConn);
            
            connMockedStatic.when(() -> HttpsConnection.withUrl(anyString())).thenReturn(conn);
            
            // execute and assert
            final Image i = DownloadImageUtilities.getImage("dummy");
            assertNull(i);
            verify(urlConn).disconnect();
        }
    }
        
    /**
     * The "download failed" image is returned when an IOException occurs.
     * 
     * @throws IOException if mocking the HTTPS connection classes fails.
     * @throws TimeoutException if setting up the test JavaFx environment fails.
     */
    @Test
    public void testException() throws IOException, TimeoutException {
        FxToolkit.registerPrimaryStage();
        try (final MockedStatic<HttpsConnection> connMockedStatic = mockStatic(HttpsConnection.class)) {           
            /* It's possible to throw the IOException from HttpsURLConnection
            instead of HttpsConnection. Throwing earlier exercises the case
            where the finally block doesn't call disconnect on the connection.
            */
            final HttpsConnection conn = mock(HttpsConnection.class);
            when(conn.acceptPng()).thenReturn(conn);
            when(conn.withReadTimeout(anyInt())).thenReturn(conn);
            when(conn.get()).thenThrow(IOException.class);
            connMockedStatic.when(() -> HttpsConnection.withUrl(anyString())).thenReturn(conn);
            assertTrue(DownloadImageUtilities.getImage("dummy").getUrl().contains("resources/download_failed.png"));
        } finally {
            FxToolkit.cleanupStages();
        }
    }
}

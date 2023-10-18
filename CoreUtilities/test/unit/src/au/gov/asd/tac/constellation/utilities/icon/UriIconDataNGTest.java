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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for UriIconData.
 *
 * @author sol695510
 */
public class UriIconDataNGTest {

    private static MockedStatic<HttpsUtilities> httpsUtilitiesStaticMock;
    private static MockedStatic<HttpsConnection> httpsConnectionStaticMock;
    private static MockedStatic<URI> uriStaticMock;
    private static URI uriMock;
    private static HttpsConnection httpsConnectionMock;
    private static HttpsURLConnection httpsURLConnectionMock;
    private static URL urlMock;
    private static FileNotFoundException fileNotFoundExceptionMock;
    private static InputStream inputStreamMock;

    public UriIconDataNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        httpsUtilitiesStaticMock = Mockito.mockStatic(HttpsUtilities.class);
        httpsConnectionStaticMock = Mockito.mockStatic(HttpsConnection.class);
        uriStaticMock = Mockito.mockStatic(URI.class);

        uriMock = Mockito.mock(URI.class);
        httpsConnectionMock = Mockito.mock(HttpsConnection.class);
        httpsURLConnectionMock = Mockito.mock(HttpsURLConnection.class);
        fileNotFoundExceptionMock = Mockito.mock(FileNotFoundException.class);
        urlMock = Mockito.mock(URL.class);
        inputStreamMock = Mockito.mock(InputStream.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        httpsUtilitiesStaticMock.close();
        httpsConnectionStaticMock.close();
        uriStaticMock.close();
    }

    /**
     * Test of constructor method with String, of class UriIconData, when
     * URI.create() throws IllegalArgumentException.
     *
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUriIconData_withStringThrowsException() throws Exception {
        System.out.println("testUriIconData_withStringThrowsException");

        uriStaticMock.when(() -> URI.create(Mockito.any(String.class))).thenCallRealMethod();

        final UriIconData instance = new UriIconData("someURIString");
    }

    /**
     * Test of constructor method with String, of class UriIconData, when
     * isAbsolute() throws AssertionError.
     *
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = AssertionError.class)
    public void testUriIconData_withStringThrowsError() throws Exception {
        System.out.println("testUriIconData_withStringThrowsError");

        uriStaticMock.when(() -> URI.create(Mockito.any(String.class))).thenReturn(uriMock);

        // If the URI is not absolute and does not have a scheme component.
        when(uriMock.isAbsolute()).thenReturn(false);

        final UriIconData instance = new UriIconData("someURIString");
    }

    /**
     * Test of constructor method with URI, of class UriIconData, when
     * isAbsolute() throws IllegalArgumentException.
     *
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUriIconData_withURIThrowsException() throws Exception {
        System.out.println("testUriIconData_withURIThrowsException");

        // If the URI is not absolute and does not have a scheme component.
        when(uriMock.isAbsolute()).thenReturn(false);

        final UriIconData instance = new UriIconData(uriMock);
    }

    /**
     * Test of createRasterInputStream method, of class UriIconData, when the String
 constructor is invoked.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateInputStream_withStringConstructor() throws Exception {
        System.out.println("testCreateInputStream_withStringConstructor");

        uriStaticMock.when(() -> URI.create(Mockito.any(String.class))).thenReturn(uriMock);

        when(uriMock.isAbsolute()).thenReturn(true);
        when(uriMock.getScheme()).thenReturn("HTTPS");
        when(uriMock.toURL()).thenReturn(urlMock);

        final UriIconData instance = new UriIconData("someURIString");

        httpsConnectionStaticMock.when(() -> HttpsConnection.withUrl(Mockito.any(String.class))).thenReturn(httpsConnectionMock);
        when(httpsConnectionMock.get()).thenReturn(httpsURLConnectionMock);

        httpsUtilitiesStaticMock.when(() -> HttpsUtilities.getInputStream(httpsURLConnectionMock)).thenReturn(inputStreamMock);

        final InputStream expResult = inputStreamMock;
        final InputStream result = instance.createRasterInputStream();

        httpsUtilitiesStaticMock.verify(() -> HttpsUtilities.getInputStream(Mockito.any(HttpsURLConnection.class)), times(1));

        assertEquals(result, expResult);
    }

    /**
     * Test of createRasterInputStream method, of class UriIconData, when the URI
 constructor is invoked.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateInputStream_withURIConstructor() throws Exception {
        System.out.println("testCreateInputStream_withURIConstructor");

        when(uriMock.isAbsolute()).thenReturn(true);
        when(uriMock.getScheme()).thenReturn("HTTPS");
        when(uriMock.toURL()).thenReturn(urlMock);

        final UriIconData instance = new UriIconData(uriMock);

        httpsConnectionStaticMock.when(() -> HttpsConnection.withUrl(Mockito.any(String.class))).thenReturn(httpsConnectionMock);
        when(httpsConnectionMock.get()).thenReturn(httpsURLConnectionMock);

        httpsUtilitiesStaticMock.when(() -> HttpsUtilities.getInputStream(httpsURLConnectionMock)).thenReturn(inputStreamMock);

        final InputStream expResult = inputStreamMock;
        final InputStream result = instance.createRasterInputStream();

        httpsUtilitiesStaticMock.verify(() -> HttpsUtilities.getInputStream(Mockito.any(HttpsURLConnection.class)), times(1));

        assertEquals(result, expResult);
    }

    /**
     * Test of createRasterInputStream method, of class UriIconData, when the URI
 scheme does not equal HTTPS.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateInputStream_schemeNotHTTPS() throws Exception {
        System.out.println("testCreateInputStream_schemeNotHTTPS");

        when(uriMock.isAbsolute()).thenReturn(true);
        when(uriMock.getScheme()).thenReturn("notHTTPS");
        when(uriMock.toURL()).thenReturn(urlMock);

        final UriIconData instance = new UriIconData(uriMock);

        when(urlMock.openStream()).thenReturn(inputStreamMock);

        final InputStream expResult = inputStreamMock;
        final InputStream result = instance.createRasterInputStream();

        verify(urlMock, times(1)).openStream();

        assertEquals(result, expResult);
    }

    /**
     * Test of createRasterInputStream method, of class UriIconData, when the
 HttpURLConnection throws FileNotFoundException.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateInputStream_throwsException() throws Exception {
        System.out.println("testCreateInputStream_throwsException");

        when(uriMock.isAbsolute()).thenReturn(true);
        when(uriMock.getScheme()).thenReturn("HTTPS");
        when(uriMock.toURL()).thenReturn(urlMock);

        final UriIconData instance = new UriIconData(uriMock);

        httpsConnectionStaticMock.when(() -> HttpsConnection.withUrl(Mockito.any(String.class))).thenReturn(httpsConnectionMock);
        when(httpsConnectionMock.get()).thenThrow(fileNotFoundExceptionMock);

        final InputStream expResult = null;
        final InputStream result = instance.createRasterInputStream();

        assertEquals(result, expResult);
    }
}

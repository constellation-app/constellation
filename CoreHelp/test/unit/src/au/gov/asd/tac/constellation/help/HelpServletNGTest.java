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
package au.gov.asd.tac.constellation.help;

import au.gov.asd.tac.constellation.help.utilities.HelpMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for HelpServlet
 *
 * @author Delphinus8821
 */
public class HelpServletNGTest {
    
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
     * Test of doGet method, of class HelpServlet
     *
     * @throws jakarta.servlet.ServletException
     */
    @Test
    public void testDoGet() throws ServletException {
        try (MockedStatic<ConstellationHelpDisplayer> helpDisplayerStaticMock = Mockito.mockStatic(ConstellationHelpDisplayer.class)) {
            helpDisplayerStaticMock.when(() -> ConstellationHelpDisplayer.copy(Mockito.anyString(), Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            HttpServletRequest requestMock = mock(HttpServletRequest.class);
            HttpServletResponse responseMock = mock(HttpServletResponse.class);

            when(requestMock.getRequestURI()).thenReturn("/file:/C:/Projects/constellation/build/cluster/modules/ext/docs/"
                    + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/docs/analytic-view.md");
            when(requestMock.getHeader("referer")).thenReturn(null);

            HelpServlet instance = new HelpServlet();
            instance.doGet(requestMock, responseMock);

            helpDisplayerStaticMock.verify(() -> ConstellationHelpDisplayer.copy(Mockito.anyString(), Mockito.any()), times(1));
            verify(requestMock, times(1)).getRequestURI();
            verify(requestMock, times(1)).getHeader(Mockito.anyString());
        }
    }

    /**
     * Test of doGet method, of class HelpServlet, where a redirect is required
     *
     * @throws java.io.IOException
     * @throws jakarta.servlet.ServletException
     */
    @Test
    public void testDoGetRedirect() throws IOException, ServletException {
        HttpServletRequest requestMock1 = mock(HttpServletRequest.class);
        HttpServletResponse responseMock1 = mock(HttpServletResponse.class);

        try (MockedStatic<ConstellationHelpDisplayer> helpDisplayerStaticMock = Mockito.mockStatic(ConstellationHelpDisplayer.class);
             MockedStatic<HelpMapper> helpMapperStaticMock = Mockito.mockStatic(HelpMapper.class)) {
            helpDisplayerStaticMock.when(() -> ConstellationHelpDisplayer.copy(Mockito.anyString(), Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            when(requestMock1.getRequestURI()).thenReturn("/file:/C:/Projects/constellation/build/cluster/modules/ext/docs/"
                    + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/ext/docs/"
                    + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/question-best-connects-network.md");
            doNothing().when(responseMock1).sendRedirect(Mockito.eq("/file:/C:/Projects/constellation/build/cluster/modules/ext/docs/"
                    + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/question-best-connects-network.md"));
            when(requestMock1.getHeader("referer")).thenReturn("/file:/C:/Projects/constellation/build/cluster/modules/ext/docs/"
                    + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/analytic-view.md");
            
            final Map<String, String> mappings = new HashMap<>();
            final String sep = File.separator;
            final String helpPagePath = ".." + sep + "constellation" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep + "gov"
                    + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "question-best-connects-network.md";
            mappings.put("test", helpPagePath);
            helpMapperStaticMock.when(() -> HelpMapper.getMappings()).thenReturn(mappings);

            HelpServlet instance = new HelpServlet();
            instance.doGet(requestMock1, responseMock1);

            verify(responseMock1, times(1)).sendRedirect(Mockito.anyString());
            helpDisplayerStaticMock.verify(() -> ConstellationHelpDisplayer.copy(Mockito.anyString(), Mockito.any()), times(1));
            verify(requestMock1, times(1)).getRequestURI();
            verify(requestMock1, times(1)).getHeader(Mockito.anyString());
        }
    }

    /**
     * Test of doGet method, of class HelpServlet, to throw an exception
     *
     * @throws jakarta.servlet.ServletException
     */
    @Test(expectedExceptions = Exception.class)
    public void doGetException() throws ServletException {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        HelpServlet instance = new HelpServlet();
        instance.doGet(request, response);
    }

    /**
     * Test of redirectPath method, of class HelpServlet. Test case where no
     * redirect is required
     */
    @Test
    public void testRedirectPathNoRedirect() {
        String referer = null;
        String requestPath = "/file:/C:/Projects/constellation/build/cluster/modules/ext/docs/"
                + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/docs/analytic-view.md";
        final URL fileUrl = HelpServlet.redirectPath(requestPath, referer);
        assertFalse(HelpServlet.isRedirect());
        assertEquals(fileUrl, null);
    }

    /**
     * Test of redirectPath method, of class HelpServlet Test case where a
     * redirect is required
     */
    @Test
    public void testRedirectPathRedirect() {
        String referer = "http://localhost:1517/file:/constellation/build/cluster/modules/ext/docs/"
                + "CoreAttributeEditorView/src/au/gov/asd/tac/constellation/views/attributeeditor/attribute-editor.md";
        String requestPath = "/file:/constellation/build/cluster/modules/ext/docs/"
                + "CoreAttributeEditorView/src/au/gov/asd/tac/constellation/views/ext/docs/"
                + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/analytic-view.md";
        try (MockedStatic<HelpMapper> helpMapperStaticMock = Mockito.mockStatic(HelpMapper.class)) {
            final Map<String, String> mappings = new HashMap<>();
            final String sep = File.separator;
            final String helpPagePath = ".." + sep + "constellation" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep + "gov"
                    + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "analytic-view.md";
            mappings.put("test", helpPagePath);
            helpMapperStaticMock.when(() -> HelpMapper.getMappings()).thenReturn(mappings);
            
            final URL fileUrl = HelpServlet.redirectPath(requestPath, referer);
            assertTrue(fileUrl.toString().contains("CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/analytic-view.md"));
            assertTrue(HelpServlet.isRedirect());
        }
    }

    /**
     * Test of redirectPath method, of class HelpServlet Test case where a
     * redirect is required within the same module
     */
    @Test
    public void testRedirectPathRedirectWithinSameModule() {
        String referer = "http://localhost:1517/file:/constellation/build/cluster/modules/ext/docs/"
                + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/analytic-view.md";
        String requestPath = "/file:/constellation/build/cluster/modules/ext/docs/"
                + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/ext/docs/"
                + "CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/question-best-connects-network.md";
        try (MockedStatic<HelpMapper> helpMapperStaticMock = Mockito.mockStatic(HelpMapper.class)) { 
            final Map<String, String> mappings = new HashMap<>();
            final String sep = File.separator;
            final String helpPagePath = ".." + sep + "constellation" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep + "gov"
                    + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "analyticview" + sep + "question-best-connects-network.md";
            mappings.put("test", helpPagePath);
            helpMapperStaticMock.when(() -> HelpMapper.getMappings()).thenReturn(mappings);
            
            final URL fileUrl = HelpServlet.redirectPath(requestPath, referer);
            assertTrue(fileUrl.toString().contains("CoreAnalyticView/src/au/gov/asd/tac/constellation/views/analyticview/question-best-connects-network.md"));
            assertTrue(HelpServlet.isRedirect());
        }
    }

    /**
     * Test of redirectPath method, of class HelpServlet Test of when null
     * parameters are entered, should not be redirected
     */
    @Test
    public void testRedirectPathNull() {
        String referer = null;
        String requestPath = null;
        HelpServlet.setWasRedirect(true);
        final URL fileUrl = HelpServlet.redirectPath(requestPath, referer);
        assertEquals(fileUrl, null);
        assertFalse(HelpServlet.isRedirect());
    }

    @Test
    public void testStripLeadingPath() {
        System.out.println("running testStripLeadingPath");

        final String referer = "/file:/C:/Projects/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String expectedResult = "/Projects/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String result = HelpServlet.stripLeadingPath(referer);
        assertEquals(result, expectedResult);

        final String referer2 = "/file:/J:/Users/Username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String expectedResult2 = "/Users/Username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String result2 = HelpServlet.stripLeadingPath(referer2);
        assertEquals(result2, expectedResult2);

        final String referer3 = "/file:/home/username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String expectedResult3 = "/home/username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String result3 = HelpServlet.stripLeadingPath(referer3);
        assertEquals(result3, expectedResult3);

        final String referer4 = "file:/home/username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String expectedResult4 = "/home/username/Constellation/constellation/CoreHelp/path/to/toc.md";
        final String result4 = HelpServlet.stripLeadingPath(referer4);
        assertEquals(result4, expectedResult4);
    }
}

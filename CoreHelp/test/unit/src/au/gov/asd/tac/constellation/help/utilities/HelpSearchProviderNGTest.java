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
package au.gov.asd.tac.constellation.help.utilities;

import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for HelpSearchProvider
 *
 * @author Delphinus8821
 */
public class HelpSearchProviderNGTest {

    SearchResponse response;
    SearchRequest request;
    
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
     * Test of evaluate method, of class HelpSearchProvider. Pass in an unmatchable string
     */
    @Test
    public void testEvaluateFail() {
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        when(request.getText()).thenReturn("nothingshouldmatchthisstring");
        when(response.addResult(Mockito.any(), Mockito.anyString())).thenReturn(true);

        HelpSearchProvider instance = new HelpSearchProvider();
        instance.evaluate(request, response);

        verify(response, never()).addResult(Mockito.any(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class HelpSearchProvider. Pass in null
     */
    @Test
    public void testEvaluateNull() {
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        when(request.getText()).thenReturn(null);
        when(response.addResult(Mockito.any(), Mockito.anyString())).thenReturn(true);

        HelpSearchProvider instance = new HelpSearchProvider();
        instance.evaluate(request, response);

        verify(response, never()).addResult(Mockito.any(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class HelpSearchProvider. Pass in a valid string
     */
    @Test
    public void testEvaluateSuccess() {
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        when(request.getText()).thenReturn("help");
        when(response.addResult(Mockito.any(), Mockito.eq(HelpSearchProvider.QuickSearchUtils.CIRCLED_H + "  help options"))).thenReturn(true);

        HelpSearchProvider instance = new HelpSearchProvider();
        instance.evaluate(request, response);

        verify(response, times(1)).addResult(Mockito.any(), Mockito.eq(HelpSearchProvider.QuickSearchUtils.CIRCLED_H + "  help options"));
    }
}

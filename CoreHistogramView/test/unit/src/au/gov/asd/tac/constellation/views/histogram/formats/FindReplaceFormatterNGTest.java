/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class FindReplaceFormatterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Left blank for now
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Left blank for now
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Left blank for now
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Left blank for now
    }

    /**
     * Test of createParameters method, of class FindReplaceFormatter.
     */
    @Test
    public void testCreateParameters() {
        final FindReplaceFormatter instance = new FindReplaceFormatter();
        final String expResult = "";

        final PluginParameters result = instance.createParameters();
        assertEquals(result.getParameters().get(FindReplaceFormatter.FIND_PARAMETER_ID).getStringValue(), expResult);
        assertEquals(result.getParameters().get(FindReplaceFormatter.REPLACE_PARAMETER_ID).getStringValue(), expResult);
    }

    /**
     * Test of appliesToBin method, of class FindReplaceFormatter.
     */
    @Test
    public void testAppliesToBin() {
        System.out.println("appliesToBin");
        final Bin mockBin = mock(Bin.class);
        final Bin mockStringBin = mock(StringBin.class);
        final FindReplaceFormatter instance = new FindReplaceFormatter();

        assertTrue(instance.appliesToBin(mockStringBin));
        assertFalse(instance.appliesToBin(mockBin));
    }

    /**
     * Test of createBin method, of class FindReplaceFormatter.
     */
    @Test
    public void testCreateBin() {
        System.out.println("createBin");      
        final GraphReadMethods graph = null;
        final int attribute = 0;

        final FindReplaceFormatter instance = new FindReplaceFormatter();

        final PluginParameters mockPluginParameters = mock(PluginParameters.class);
        final Map<String, PluginParameter<?>> mockParameters = mock(Map.class);
        final PluginParameter mockParam = mock(PluginParameter.class);
        final Bin mockBin = mock(StringBin.class);
        final String mockString = "test string";

        when(mockPluginParameters.getParameters()).thenReturn(mockParameters);
        when(mockParameters.get(FindReplaceFormatter.FIND_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParameters.get(FindReplaceFormatter.REPLACE_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParam.getStringValue()).thenReturn(mockString);

        // Run function
        instance.createBin(graph, attribute, mockPluginParameters, mockBin);

        verify(mockPluginParameters, times(2)).getParameters();
        verify(mockParameters).get(FindReplaceFormatter.FIND_PARAMETER_ID);
        verify(mockParameters).get(FindReplaceFormatter.REPLACE_PARAMETER_ID);
        verify(mockParam, times(2)).getStringValue();
    }

    /**
     * Test of setKey method, of class FindReplaceFormatBin.
     */
    @Test
    public void testFindReplaceFormatBinSetKey() {
        System.out.println("findReplaceFormatBinSetKey");

        final GraphReadMethods graph = null;
        final int attribute = 0;
        final int element = 0;

        final FindReplaceFormatter findReplaceFormatter = new FindReplaceFormatter();

        final PluginParameters mockPluginParameters = mock(PluginParameters.class);
        final Map<String, PluginParameter<?>> mockParameters = mock(Map.class);
        final PluginParameter mockParam = mock(PluginParameter.class);
        final StringBin mockBin = mock(StringBin.class);
        final String mockString = "test string";

        when(mockPluginParameters.getParameters()).thenReturn(mockParameters);
        when(mockParameters.get(FindReplaceFormatter.FIND_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParameters.get(FindReplaceFormatter.REPLACE_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParam.getStringValue()).thenReturn(mockString);

        // Create instance
        StringBin instance = (StringBin) findReplaceFormatter.createBin(graph, attribute, mockPluginParameters, mockBin);
        instance.setKey(graph, attribute, element);
        assertNull(instance.getKey());

        when(mockBin.getKey()).thenReturn(mockString);

        // Create instance
        instance = (StringBin) findReplaceFormatter.createBin(graph, attribute, mockPluginParameters, mockBin);
        instance.setKey(graph, attribute, element);
        assertNotNull(instance.getKey());

        verify(mockBin, times(2)).setKey(graph, attribute, element);
        verify(mockBin, times(2)).getKey();
    }
}

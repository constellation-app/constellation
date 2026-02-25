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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class DateTimeCustomFormatterNGTest {

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
     * Test of createBin method, of class DateTimeCustomFormatter.
     */
    @Test
    public void testCreateBin() {
        System.out.println("createBin");
        final GraphReadMethods graph = null;
        final int attribute = 0;

        final PluginParameters mockPluginParameters = mock(PluginParameters.class);
        final Map<String, PluginParameter<?>> mockParameters = mock(Map.class);
        final PluginParameter mockParam = mock(PluginParameter.class);
        final Bin mockBin = mock(AttributeBin.class);
        final String mockString = "yyyy-MM-dd hh:mm:ss";

        when(mockPluginParameters.getParameters()).thenReturn(mockParameters);
        when(mockParameters.get(DateTimeCustomFormatter.FORMAT_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParam.getStringValue()).thenReturn(mockString);

        final DateTimeCustomFormatter instance = new DateTimeCustomFormatter();

        // Run function
        instance.createBin(graph, attribute, mockPluginParameters, mockBin);

        verify(mockPluginParameters).getParameters();
        verify(mockParameters).get(DateTimeCustomFormatter.FORMAT_PARAMETER_ID);
        verify(mockParam).getStringValue();
    }
}

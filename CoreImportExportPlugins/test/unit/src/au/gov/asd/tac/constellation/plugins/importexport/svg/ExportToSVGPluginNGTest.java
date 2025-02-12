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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link ExportToSVGPlugin}
 * 
 * @author capricornunicorn123
 */
public class ExportToSVGPluginNGTest {
    
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
     * Test of createParameters method, of class ExportToSVGPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        PluginParameters result = new ExportToSVGPlugin().createParameters();
        
        // Ensure that all parameters have been created
        assertEquals(result.getParameters().size(), 12);
        
        // Ensure defaultvalues are as expected
        assertNull(result.getObjectValue(ExportToSVGPlugin.GRAPH_TITLE_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.EXPORT_CORES_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.FILE_NAME_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.BACKGROUND_COLOR_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.EXPORT_PERSPECTIVE_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.IMAGE_MODE_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SELECTED_ELEMENTS_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SHOW_BLAZES_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SHOW_CONNECTIONS_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SHOW_CONNECTION_LABELS_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SHOW_NODES_PARAMETER_ID));
        assertNotNull(result.getObjectValue(ExportToSVGPlugin.SHOW_NODE_LABELS_PARAMETER_ID));
    }  
}

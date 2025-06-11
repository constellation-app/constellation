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
package au.gov.asd.tac.constellation.graph.schema.visual;

import java.util.Map;
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
public class VisualSchemaHelpProviderNGTest {
    
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
     * Test of getHelpMap method, of class VisualSchemaHelpProvider.
     */
    @Test
    public void testGetHelpMap() {
        System.out.println("getHelpMap");
        
        final VisualSchemaHelpProvider instance = new VisualSchemaHelpProvider();
        final Map<String, String> helpMap = instance.getHelpMap();

        assertEquals(helpMap.size(), 1);
        //not going to go through and assert the existence of each specific item in the map 
        //but will assert they're all correctly located within the visual package
        final String packageStart = "au.gov.asd.tac.constellation.graph.schema.visual.";
        for (final String path : helpMap.keySet()) {
            assertTrue(path.startsWith(packageStart));
        }
    }

    /**
     * Test of getHelpTOC method, of class VisualSchemaHelpProvider.
     */
    @Test
    public void testGetHelpTOC() {
        System.out.println("getHelpTOC");
        
        final VisualSchemaHelpProvider instance = new VisualSchemaHelpProvider();
        final String tocLocation = instance.getHelpTOC();

        //the file separator used will vary from OS to OS so rather than copying the
        //exact string from the actual class and asserting what we got is the same,
        //we'll assert that some of the keyparts of the expected filepath are present
        assertTrue(tocLocation.contains("docs"));
        assertTrue(tocLocation.contains("CoreVisualSchema"));
        assertTrue(tocLocation.contains("visualschema-toc.xml"));

        assertTrue(tocLocation.indexOf("docs") < tocLocation.indexOf("CoreVisualSchema"));
        assertTrue(tocLocation.indexOf("CoreVisualSchema") < tocLocation.indexOf("visualschema-toc.xml"));
    }   
}

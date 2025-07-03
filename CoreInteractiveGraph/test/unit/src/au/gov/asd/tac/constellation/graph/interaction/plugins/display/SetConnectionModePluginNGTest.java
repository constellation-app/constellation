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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.display.SetConnectionModePlugin.CONNECTION_MODE_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
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
public class SetConnectionModePluginNGTest {
    
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
     * Test of createParameters method, of class SetConnectionModePlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final SetConnectionModePlugin instance = new SetConnectionModePlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 1);
        assertTrue(params.getParameters().containsKey(CONNECTION_MODE_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class SetConnectionModePlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int connectionModeAttribute = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(graph);
        
        final SetConnectionModePlugin instance = new SetConnectionModePlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setObjectValue(CONNECTION_MODE_PARAMETER_ID, ConnectionMode.LINK);
        
        assertEquals(graph.getObjectValue(connectionModeAttribute, 0), ConnectionMode.EDGE);
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getObjectValue(connectionModeAttribute, 0), ConnectionMode.LINK);
    }
}

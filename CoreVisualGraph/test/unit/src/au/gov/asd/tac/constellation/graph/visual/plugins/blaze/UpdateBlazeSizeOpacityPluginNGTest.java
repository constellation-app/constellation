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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
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
public class UpdateBlazeSizeOpacityPluginNGTest {
    
    private StoreGraph graph;

    private int blazeSizeGraphAttribute;
    private int blazeOpacityGraphAttribute;

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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        blazeSizeGraphAttribute = VisualConcept.GraphAttribute.BLAZE_SIZE.ensure(graph);
        blazeOpacityGraphAttribute = VisualConcept.GraphAttribute.BLAZE_OPACITY.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class UpdateBlazeSizeOpacityPlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEdit() throws InterruptedException {
        System.out.println("edit");
        
        final UpdateBlazeSizeOpacityPlugin instance = new UpdateBlazeSizeOpacityPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setFloatValue(UpdateBlazeSizeOpacityPlugin.SIZE_PARAMETER_ID, 0.3F);
        parameters.setFloatValue(UpdateBlazeSizeOpacityPlugin.OPACITY_PARAMETER_ID, 1F);
        
        assertEquals(graph.getFloatValue(blazeSizeGraphAttribute, 0), 0.3F);
        assertEquals(graph.getFloatValue(blazeOpacityGraphAttribute, 0), 1F);
        
        instance.edit(graph, null, parameters);
        
        // nothing should happen since we are attempting to set to the default values
        assertEquals(graph.getFloatValue(blazeSizeGraphAttribute, 0), 0.3F);
        assertEquals(graph.getFloatValue(blazeOpacityGraphAttribute, 0), 1F);
        
        parameters.setFloatValue(UpdateBlazeSizeOpacityPlugin.SIZE_PARAMETER_ID, 0.5F);
        parameters.setFloatValue(UpdateBlazeSizeOpacityPlugin.OPACITY_PARAMETER_ID, 0.5F);
        
        instance.edit(graph, null, parameters);
        
        assertEquals(graph.getFloatValue(blazeSizeGraphAttribute, 0), 0.5F);
        assertEquals(graph.getFloatValue(blazeOpacityGraphAttribute, 0), 0.5F);
    }
}

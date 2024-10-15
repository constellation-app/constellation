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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.plugins.CompleteSchemaPlugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class CompleteSchemaPluginNGTest {
    
 
     /**
     * Test of edit method, of class CompleteSchemaPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testEdit() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        final int vx0 = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, vx0, "foo");
        graph.setBooleanValue(vertexSelectedAttr, vx0, false);

        final PluginInteraction interaction = new TextPluginInteraction();
        final CompleteSchemaPlugin instance = new CompleteSchemaPlugin();
        final PluginParameters parameters = instance.createParameters();
        instance.edit(graph, interaction, parameters);
        
        assertTrue(interaction.getCurrentMessage().contains("Completed"));
        assertEquals(1, graph.getVertexCount());
    }    
}

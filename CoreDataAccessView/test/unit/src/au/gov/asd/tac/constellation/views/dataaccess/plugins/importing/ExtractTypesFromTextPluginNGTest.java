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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extract Types from Text Plugin Test.
 *
 * @author Delphinus8821
 */
public class ExtractTypesFromTextPluginNGTest {
    private StoreGraph graph;
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
    }

    /**
     * Test of getType method, of class ExtractTypesFromTextPlugin.
     */
    @Test
    public void testGetType() {
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        String expResult = "Import";
        String result = instance.getType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getPosition method, of class ExtractTypesFromTextPlugin.
     */
    @Test
    public void testGetPosition() {
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        int expResult = 1000;
        int result = instance.getPosition();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDescription method, of class ExtractTypesFromTextPlugin.
     */
    @Test
    public void testGetDescription() {
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        String expResult = "Identify schema type values within text and add them to your graph";
        String result = instance.getDescription();
        assertEquals(result, expResult);
    }

    /**
     * Test of createParameters method, of class ExtractTypesFromTextPlugin.
     */
    @Test
    public void testCreateParameters() {
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        PluginParameters result = instance.createParameters();

        // Test correct number of parameters are added
        final int expResult = 1;
        assertEquals(result.getParameters().size(), expResult);

        // Test the right parameters are added
        assertTrue(result.getParameters().containsKey(ExtractTypesFromTextPlugin.TEXT_PARAMETER_ID));
    }

    /**
     * Test of query method, of class ExtractTypesFromTextPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQuery() throws Exception {
        RecordStore query = new GraphRecordStore();
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        PluginInteraction interaction = new TextPluginInteraction();

        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(ExtractTypesFromTextPlugin.TEXT_PARAMETER_ID).setStringValue("Email Person Communication");

        RecordStore expResult = new GraphRecordStore();
        RecordStore result = instance.query(query, interaction, parameters);
        assertEquals(result, expResult);
    }

    /**
     * Test of query method in class ExtractTypesFromTextPlugin using a null
     * string
     *
     * @throws Exception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testNullQuery() throws Exception {
        RecordStore query = new GraphRecordStore();
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        PluginInteraction interaction = new TextPluginInteraction();

        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(ExtractTypesFromTextPlugin.TEXT_PARAMETER_ID).setStringValue(null);
        
        RecordStore result = instance.query(query, interaction, parameters);
    }

    /**
     * Test of query method in class ExtractTypesFromTextPlugin using a string
     * where a detection regex is picked up
     *
     * @throws Exception
     */
    @Test
    public void testRegexQuery() throws Exception {
        RecordStore query = new GraphRecordStore();
        ExtractTypesFromTextPlugin instance = new ExtractTypesFromTextPlugin();
        PluginInteraction interaction = new TextPluginInteraction();

        PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(ExtractTypesFromTextPlugin.TEXT_PARAMETER_ID).setStringValue("abc@def.ghi");

        RecordStore result = instance.query(query, interaction, parameters);
        RecordStore expResult = new GraphRecordStore();
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "abc@def.ghi");
        expResult.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, AnalyticConcept.VertexType.EMAIL_ADDRESS);
        expResult.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SEED, "true");
        assertEquals(result, expResult);
    }
}

/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Split Nodes Plugin Test.
 *
 * @author arcturus
 * @author antares
 */
public class SplitNodesPluginNGTest {

    public SplitNodesPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of query method, of class SplitNodesPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryWithNoTypes() throws Exception {
        final RecordStore query = new GraphRecordStore();
        query.add();
        query.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "abc@def");

        final RecordStore expResult = new GraphRecordStore();
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, null);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "abc");
        expResult.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "def");
        expResult.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.CORRELATION);

        final PluginInteraction interaction = new TextPluginInteraction();
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SplitNodesPlugin.SPLIT_PARAMETER_ID).setStringValue("@");

        final RecordStore result = instance.query(query, interaction, parameters);
        assertEquals(result, expResult);
    }

    @Test
    public void testQueryWithTypes() throws Exception {
        final RecordStore query = new GraphRecordStore();
        query.add();
        query.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "+123456789:192.168.1.1");

        final RecordStore expResult = new GraphRecordStore();
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, null);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "+123456789");
        expResult.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER);
        expResult.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "192.168.1.1");
        expResult.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, AnalyticConcept.VertexType.URL);
        expResult.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.CORRELATION);

        final PluginInteraction interaction = new TextPluginInteraction();
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SplitNodesPlugin.SPLIT_PARAMETER_ID).setStringValue(":");

        final RecordStore result = instance.query(query, interaction, parameters);
        assertEquals(result, expResult);
    }

    @Test
    public void testQueryAllOccurancesSelected() throws Exception {
        final RecordStore query = new GraphRecordStore();
        query.add();
        query.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "1:2:3:4");

        final RecordStore expResult = new GraphRecordStore();
        // adding each of the expected nodes
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, null);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "1");
        expResult.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "2");
        expResult.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.CORRELATION);
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, null);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "1");
        expResult.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "3");
        expResult.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.CORRELATION);
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, null);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "1");
        expResult.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "4");
        expResult.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.CORRELATION);

        final PluginInteraction interaction = new TextPluginInteraction();
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SplitNodesPlugin.SPLIT_PARAMETER_ID).setStringValue(":");
        parameters.getParameters().get(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID).setBooleanValue(true);

        final RecordStore result = instance.query(query, interaction, parameters);
        assertEquals(result, expResult);
    }

}

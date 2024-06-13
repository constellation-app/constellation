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
package au.gov.asd.tac.constellation.testing.integration;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Graph RecordStore Utilities Test.
 *
 * @author arcturus
 */
public class GraphRecordStoreUtilitiesNGTest {

    StoreGraph graph;

    public GraphRecordStoreUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        graph.getSchema().newGraph(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testAddRecordStoreToGraphIncludesAllRecords() {
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx2");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx3");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "CustomAttribute", "Removed");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx4");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "CustomAttribute", "Changed");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx4");
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + "CustomAttribute", "Removed");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx2");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx4");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx5");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "CustomAttribute", "Added");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx5");
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + "CustomAttribute", "Added");

        // add recordstore to graph
        final boolean initializeWithSchema = true;
        final boolean completeWithSchema = false;
        final List<String> vertexIdAttributes = new ArrayList<>();
        vertexIdAttributes.add(VisualConcept.VertexAttribute.LABEL.getName() + "<string>");
        final List<Integer> veritices = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes);

        // check counts
        assertEquals(veritices.size(), 6);

        final int vxCount = graph.getVertexCount();
        assertEquals(vxCount, 6);

        final int txCount = graph.getTransactionCount();
        assertEquals(txCount, 4);

        // check that all vertices start with vx
        final int vxNameAttribute = VisualConcept.VertexAttribute.LABEL.get(graph);
        for (int i = 0; i < vxCount; i++) {
            int vxId = graph.getVertex(i);
            assertEquals(graph.getStringValue(vxNameAttribute, vxId), String.valueOf("vx" + vxId));
        }

//        try {
//            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, "testAddRecordStoreToGraphIncludesAllRecords");
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
    }

    @Test
    public void addRecordStoreToGraphWithExistingGraph() {
        int vx0, vx1;
        int identifierAttribute, colorAttribute;

        identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        colorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        graph.setPrimaryKey(GraphElementType.VERTEX, identifierAttribute);

        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        graph.setStringValue(identifierAttribute, vx0, "vx0");
        graph.setStringValue(identifierAttribute, vx1, "vx1");

        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "vx0");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + "color", "DarkGreen");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "vx1");

        final boolean initializeWithSchema = false;
        final boolean completeWithSchema = false;
        final List<String> vertexIdAttributes = new ArrayList<>();
        final Map<String, Integer> vertexMap = new HashMap<>();
        final Map<String, Integer> transactionMap = new HashMap<>();

        final List<Integer> verticies = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, vertexMap, transactionMap);
        graph.validateKeys();
        assertEquals(3, verticies.size());
        assertEquals(2, graph.getVertexCount());
        assertEquals(1, graph.getTransactionCount());

        vx0 = graph.getVertex(vx0);
        vx1 = graph.getVertex(vx1);

        assertEquals("vx0", graph.getStringValue(identifierAttribute, vx0));
        assertEquals("DarkGreen", graph.getStringValue(colorAttribute, vx0));
        assertEquals("vx1", graph.getStringValue(identifierAttribute, vx1));
    }

    @Test
    public void addedRecordStoreSupportsTypesWithOverriddenDirectionWithInitialiseSchema() {
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "vx1");
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + GraphRecordStoreUtilities.DIRECTED_KEY, false);
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.COMMUNICATION); // the communication type is directed

        final boolean initializeWithSchema = true;
        final boolean completeWithSchema = false;
        final List<String> vertexIdAttributes = new ArrayList<>();
        final Map<String, Integer> vertexMap = new HashMap<>();
        final Map<String, Integer> transactionMap = new HashMap<>();
        final List<Integer> veritices = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, vertexMap, transactionMap);

        assertEquals(2, veritices.size());
        assertEquals(2, graph.getVertexCount());
        assertEquals(1, graph.getTransactionCount());

//        try {
//            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, "testAddedRecordStoreSupportsTypesWithOverriddenDirectionWithInitialiseSchema");
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
        final int transactionTypeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        SchemaTransactionType type = (SchemaTransactionType) graph.getObjectValue(transactionTypeId, 0);
        Assert.assertFalse(type.isDirected());
    }

    @Test
    public void addedRecordStoreSupportsTypesWithOverriddenDirectionWithInitialiseAndCompleteWithSchema() {
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "vx0");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "vx1");
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + GraphRecordStoreUtilities.DIRECTED_KEY, false);
        recordStore.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, AnalyticConcept.TransactionType.COMMUNICATION); // the communication type is directed

        final boolean initializeWithSchema = true;
        final boolean completeWithSchema = true;
        final List<String> vertexIdAttributes = new ArrayList<>();
        final Map<String, Integer> vertexMap = new HashMap<>();
        final Map<String, Integer> transactionMap = new HashMap<>();
        final List<Integer> veritices = GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, initializeWithSchema, completeWithSchema, vertexIdAttributes, vertexMap, transactionMap);

        assertEquals(2, veritices.size());
        assertEquals(2, graph.getVertexCount());
        assertEquals(1, graph.getTransactionCount());

//        try {
//            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, "testAddedRecordStoreSupportsTypesWithOverriddenDirectionWithInitialiseAndCompleteWithSchema");
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
        final int transactionTypeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        SchemaTransactionType type = (SchemaTransactionType) graph.getObjectValue(transactionTypeId, 0);
        Assert.assertFalse(type.isDirected());
    }

}

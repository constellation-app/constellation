/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.compare;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.PrimaryKeyUtilities;
import au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Compare Graph Plugin Test.
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CompareGraphPluginNGTest extends ConstellationTest {

    public static final ConstellationColor ADDED_COLOR = ConstellationColor.DARK_GREEN;
    public static final ConstellationColor REMOVED_COLOR = ConstellationColor.RED;
    public static final ConstellationColor CHANGED_COLOR = ConstellationColor.YELLOW;
    public static final ConstellationColor UNCHANGED_COLOR = ConstellationColor.GREY;

    public CompareGraphPluginNGTest() {
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

    @Test
    public void testReadWithMultipleChangesToVerticies() {
        int vx0, vx1, vx2, vx3, vx4, vx5, tx0, tx1, tx2;
        int labelAttribute, fooAttribute, lineStyleAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        labelAttribute = originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Label", "", "", null);
        lineStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(originalGraph);
        fooAttribute = originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "foo", "", "", null);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, lineStyleAttribute);

        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        vx2 = originalGraph.addVertex();
        vx3 = originalGraph.addVertex();
        vx4 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        tx1 = originalGraph.addTransaction(vx0, vx4, true);
        originalGraph.setStringValue(labelAttribute, vx0, "vx0");
        originalGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(labelAttribute, vx2, "vx2");
        originalGraph.setStringValue(labelAttribute, vx3, "vx3");
        originalGraph.setStringValue(labelAttribute, vx4, "vx4");

        final StoreGraph compareGraph = new StoreGraph(schema);
        labelAttribute = compareGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Label", "", "", null);
        fooAttribute = compareGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "foo", "", "", null);
        lineStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(compareGraph);
        compareGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        compareGraph.setPrimaryKey(GraphElementType.TRANSACTION, lineStyleAttribute);

        vx0 = compareGraph.addVertex();
        vx1 = compareGraph.addVertex();
        vx2 = compareGraph.addVertex();
//        vx3 = compareGraph.addVertex();
        vx4 = compareGraph.addVertex();
        vx5 = compareGraph.addVertex();
        tx0 = compareGraph.addTransaction(vx0, vx1, true);
//        tx1 = originalGraph.addTransaction(vx0, vx4, true); // # change is REMOVE
        tx2 = compareGraph.addTransaction(vx0, vx5, true); // # change is ADDED
        compareGraph.setStringValue(labelAttribute, vx0, "vx0");
        compareGraph.setStringValue(labelAttribute, vx1, "vx1");
        compareGraph.setStringValue(labelAttribute, vx2, "vx2");
//        compareGraph.setStringValue(labelAttribute, vx3, "vx3"); // # change is REMOVE
        compareGraph.setStringValue(labelAttribute, vx4, "vx4");
        compareGraph.setStringValue(fooAttribute, vx4, "bar"); // # change is CHANGE
        compareGraph.setStringValue(labelAttribute, vx5, "vx5"); // # change is ADDED

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);
        final GraphRecordStore compareAll = GraphRecordStoreUtilities.getAll(compareGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreTransactionAttributes.add("[id]");

        // debug
        System.out.println("originalAll ==>\n" + originalAll.toStringVerbose());
        System.out.println("compareAll ==>\n" + compareAll.toStringVerbose());

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        Graph finalGraph = null;
        GraphRecordStore changes = new GraphRecordStore();
        try {
            changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
            System.out.println("changes ==>\n" + changes.toStringVerbose());
            assertEquals(changes.size(), 9);

            finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        } catch (InterruptedException | PluginException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        changes.reset();
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx3");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx4");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.CHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx4");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx5");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.ADDED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx5");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.ADDED);

        final ReadableGraph rg = finalGraph.getReadableGraph();
        try {
            int vxCount = rg.getVertexCount();
            int txCount = rg.getTransactionCount();

            assertEquals(vxCount, 6);
            assertEquals(txCount, 3);
        } finally {
            rg.release();
        }

        try {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
            SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph");
            SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
        } catch (IOException | InterruptedException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testReadWithDuplicateGraphScenario() throws InterruptedException {
        int vx0, vx1, vx2, tx0, tx1;
        int identifierAttribute, typeAttribute, uniqueIdAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(originalGraph);
//        typeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(originalGraph);
        uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, identifierAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute);

        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        vx2 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        tx1 = originalGraph.addTransaction(vx1, vx2, true);
        originalGraph.setStringValue(identifierAttribute, vx0, "vx0");
        originalGraph.setStringValue(identifierAttribute, vx1, "vx1");
        originalGraph.setStringValue(identifierAttribute, vx2, "vx2");

        Graph compareGraph;
        GraphRecordStore compareAll;

        try {
            final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
            final PluginParameters copyParams = copyGraphPlugin.createParameters();
            copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID).setBooleanValue(true);
            PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyParams).executeNow((GraphReadMethods) originalGraph);
            compareGraph = (Graph) copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();
        } catch (PluginException ex) {
            compareGraph = null;
            Assert.fail(ex.getLocalizedMessage());
        }

        final WritableGraph wg = compareGraph.getWritableGraph("remove a node", true);
        try {
            Assert.assertEquals(wg.getVertexCount(), 3);
            Assert.assertEquals(wg.getTransactionCount(), 2);
            wg.removeVertex(vx1);
            Assert.assertEquals(wg.getVertexCount(), 2);
            Assert.assertEquals(wg.getTransactionCount(), 0);
        } finally {
            wg.commit();
        }

        ReadableGraph rg = compareGraph.getReadableGraph();
        try {
            compareAll = GraphRecordStoreUtilities.getAll(rg, false, true);
        } finally {
            rg.release();
        }

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        // debug
        System.out.println("originalAll ==>\n" + originalAll.toStringVerbose());
        System.out.println("compareAll ==>\n" + compareAll.toStringVerbose());

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        Graph finalGraph = null;
        GraphRecordStore changes = new GraphRecordStore();
        try {
            changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
            System.out.println("changes ==>\n" + changes.toStringVerbose());
            assertEquals(changes.size(), 5);

            finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        } catch (InterruptedException | PluginException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        changes.reset();
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);

        rg = finalGraph.getReadableGraph();
        try {
            int vxCount = rg.getVertexCount();
            int txCount = rg.getTransactionCount();

            assertEquals(vxCount, 3);
            assertEquals(txCount, 2);
        } finally {
            rg.release();
        }

        try {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
            SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph", true);
            SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
        } catch (IOException | InterruptedException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

//    @Test(expectedExceptions = DuplicateKeyException.class)
    @Test
    public void testReadWithDuplicateGraphScenarioInReverse() throws InterruptedException {
        int vx0, vx1, vx2, tx0, tx1;
        int identifierAttribute, vertexTypeAttribute, uniqueIdAttribute, transactionTypeAttribute, transactionDateTimeAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(originalGraph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(originalGraph);
        uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(originalGraph);
        transactionDateTimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, identifierAttribute, vertexTypeAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute, transactionTypeAttribute, transactionDateTimeAttribute);
        originalGraph.validateKeys();

        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        vx2 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        tx1 = originalGraph.addTransaction(vx1, vx2, true);
        originalGraph.setStringValue(identifierAttribute, vx0, "Vertex #0");
        originalGraph.setStringValue(identifierAttribute, vx1, "Vertex #1");
        originalGraph.setStringValue(identifierAttribute, vx2, "Vertex #2"); // mimic creating nodes on visual schema which will create a DuplicateKeyException - i.e. this is a known issue
        originalGraph.setStringValue(vertexTypeAttribute, vx0, "Unknown");
        originalGraph.setStringValue(vertexTypeAttribute, vx1, "Unknown");
        originalGraph.setStringValue(vertexTypeAttribute, vx2, "Unknown");

        Graph compareGraph;
        GraphRecordStore compareAll;

        try {
            final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
            final PluginParameters copyGraphParams = copyGraphPlugin.createParameters();
            PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyGraphParams).executeNow((GraphReadMethods) originalGraph);
            compareGraph = (Graph) copyGraphParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();
        } catch (PluginException ex) {
            compareGraph = null;
            Assert.fail(ex.getLocalizedMessage());
        }

        final WritableGraph wg = compareGraph.getWritableGraph("remove a node", true);
        try {
            Assert.assertEquals(wg.getVertexCount(), 3);
            wg.removeVertex(vx1);
            Assert.assertEquals(wg.getVertexCount(), 2);
        } finally {
            wg.commit();
        }

        final ReadableGraph rg = compareGraph.getReadableGraph();
        try {
            compareAll = GraphRecordStoreUtilities.getAll(rg, false, true);
        } finally {
            rg.release();
        }

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);

        Set<String> vertexPrimaryKeys = null;
        Set<String> transactionPrimaryKeys = null;
        final ReadableGraph rg2 = compareGraph.getReadableGraph();
        try {
            vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg2, GraphElementType.VERTEX);
            transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg2, GraphElementType.TRANSACTION);
        } finally {
            rg2.release();
        }

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        // debug
        System.out.println("originalAll ==>\n" + originalAll.toStringVerbose());
        System.out.println("compareAll ==>\n" + compareAll.toStringVerbose());

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        Graph finalGraph = null;
        GraphRecordStore changes = new GraphRecordStore();
        try {
            changes = instance.compareGraphs("", compareAll, originalAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
            System.out.println("changes ==>\n" + changes.toStringVerbose());
//            assertEquals(changes.size(), 3);

            finalGraph = instance.createComparisonGraph(compareGraph, changes);
        } catch (InterruptedException | PluginException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

//        changes.reset();
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx3");
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx4");
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.CHANGED);
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
//        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx4");
//        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx5");
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.ADDED);
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
//        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx5");
//        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.ADDED);
//
//
//        final ReadableGraph rg = finalGraph.getReadableGraph();
//        try {
//            int vxCount = rg.getVertexCount();
//            int txCount = rg.getTransactionCount();
//
//            assertEquals(vxCount, 6);
//            assertEquals(txCount, 3);
//        } finally {
//            rg.release();
//        }
        try {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
            SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph", true);
            SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
        } catch (IOException | InterruptedException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testReadWithNodesInDifferentOrder() {
        int vx0, vx1, vx2, vx3, tx0;
        int labelAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        labelAttribute = originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        vx2 = originalGraph.addVertex();
        vx3 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.setStringValue(labelAttribute, vx0, "vx0");
        originalGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(labelAttribute, vx2, "vx2");
        originalGraph.setStringValue(labelAttribute, vx3, "vx3");

        final StoreGraph compareGraph = new StoreGraph(schema);
        labelAttribute = compareGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        compareGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        vx0 = compareGraph.addVertex();
        vx1 = compareGraph.addVertex();
        vx2 = compareGraph.addVertex();
        vx3 = compareGraph.addVertex();
        tx0 = compareGraph.addTransaction(vx3, vx2, true);
        compareGraph.setStringValue(labelAttribute, vx0, "vx3");
        compareGraph.setStringValue(labelAttribute, vx1, "vx2");
        compareGraph.setStringValue(labelAttribute, vx2, "vx1");
        compareGraph.setStringValue(labelAttribute, vx3, "vx0");

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);
        final GraphRecordStore compareAll = GraphRecordStoreUtilities.getAll(compareGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        // debug
        System.out.println("originalAll ==>\n" + originalAll.toStringVerbose());
        System.out.println("compareAll ==>\n" + compareAll.toStringVerbose());

        final CompareGraphPlugin instance = new CompareGraphPlugin();

        GraphRecordStore changes = new GraphRecordStore();
        try {
            changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
            System.out.println("changes ==>\n" + changes.toStringVerbose());
            assertEquals(changes.size(), 5);
        } catch (PluginException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        try {
            final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        } catch (InterruptedException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    @Test
    public void testReadWithTransactionsInBothDirections() {
        int vx0, vx1, vx2, vx3, tx0, tx1;
        int labelAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        vx2 = originalGraph.addVertex();
        vx3 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        tx1 = originalGraph.addTransaction(vx1, vx2, true);
        originalGraph.setStringValue(labelAttribute, vx0, "vx0");
        originalGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(labelAttribute, vx2, "vx2");
        originalGraph.setStringValue(labelAttribute, vx3, "vx3");

        final StoreGraph compareGraph = new StoreGraph(schema);
        labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(compareGraph);
        compareGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        vx0 = compareGraph.addVertex();
        vx1 = compareGraph.addVertex();
        vx2 = compareGraph.addVertex();
        vx3 = compareGraph.addVertex();
        tx0 = compareGraph.addTransaction(vx0, vx1, true);
        tx1 = compareGraph.addTransaction(vx2, vx1, true); // this is the change
        compareGraph.setStringValue(labelAttribute, vx0, "vx0");
        compareGraph.setStringValue(labelAttribute, vx1, "vx1");
        compareGraph.setStringValue(labelAttribute, vx2, "vx2");
        compareGraph.setStringValue(labelAttribute, vx3, "vx3");

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);
        final GraphRecordStore compareAll = GraphRecordStoreUtilities.getAll(compareGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        // debug
        System.out.println("originalAll ==>\n" + originalAll.toStringVerbose());
        System.out.println("compareAll ==>\n" + compareAll.toStringVerbose());

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        GraphRecordStore changes = new GraphRecordStore();
        try {
            changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
            System.out.println("changes ==>\n" + changes.toStringVerbose());
            assertEquals(changes.size(), 7);
        } catch (PluginException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

        changes.reset();
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx3");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx2");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.ADDED);

        try {
            final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
            SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "testReadWithTransactionsInBothDirections", true);
        } catch (InterruptedException | IOException ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    /**
     * Test of compareGraphs method, of class CompareGraphPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCompareGraphsWithIdenticalGraphs() throws Exception {
        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, original, new HashSet<>(), new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        assertEquals(changes.size(), 1);

        final GraphRecordStore expResult = new GraphRecordStore();
        expResult.add();
        expResult.set(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE, CompareGraphPlugin.UNCHANGED);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.OVERLAY_COLOR, UNCHANGED_COLOR);
        expResult.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");

        System.out.println(expResult.toStringVerbose());
        System.out.println(changes.toStringVerbose());

        assertEquals(changes, expResult);
    }

    @Test
    public void testCompareGraphsWithAnExtraNodeOnCompareGraph() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx2");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println(changes.toStringVerbose());
        assertEquals(changes.size(), 4);

        changes.reset();
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx2", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.ADDED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
    }

    @Test
    public void testCompareGraphsWhenEverythingIsAnAddChange() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("original=>" + original.toStringVerbose());
        System.out.println("compare=>" + compare.toStringVerbose());
        System.out.println("changes=>" + changes.toStringVerbose());

        assertEquals(changes.size(), 3);

        changes.reset();
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.ADDED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.ADDED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.ADDED, changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE));
    }

    @Test
    public void testCompareGraphsWithMissingNodeOnCompareGraph() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("changes=>" + changes.toStringVerbose());
        assertEquals(changes.size(), 3);

        changes.reset();
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE));

    }

    @Test
    public void testCompareGraphsWithDifferentValuesOnCompareGraph() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.SOURCE + "foo", "vx0");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.set(GraphRecordStoreUtilities.SOURCE + "foo", "bar");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("original ==>\n" + original.toStringVerbose());
        System.out.println("compare ==>\n" + compare.toStringVerbose());
        System.out.println("changes ==>\n" + changes.toStringVerbose());

        assertEquals(changes.size(), 3);

        changes.reset();
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.CHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
    }

    @Test
    public void testCompareGraphsWithDuplicateChanges() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");

        final GraphRecordStore compare = new GraphRecordStore();

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, new HashSet<>(), ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("original ==>\n" + original.toStringVerbose());
        System.out.println("compare ==>\n" + compare.toStringVerbose());
        System.out.println("changes ==>\n" + changes.toStringVerbose());

        assertEquals(changes.size(), 2);

        changes.reset();
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
    }

    @Test
    public void testCompareGraphsWithMissingTransactions() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "2");

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        compare.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "1");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final Set<String> transactionPrimaryKeys = new HashSet<>();
        transactionPrimaryKeys.add(VisualConcept.TransactionAttribute.IDENTIFIER.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("original ==>\n" + original.toStringVerbose());
        System.out.println("compare ==>\n" + compare.toStringVerbose());
        System.out.println("changes ==>\n" + changes.toStringVerbose());

        assertEquals(changes.size(), 3);

        changes.reset();
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        // TODO: missing 1 more which is an unchanged for vx1 ??
//        changes.next();
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx1");
//        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.UNCHANGED);
        changes.next();
        assertEquals(changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL), "vx0");
        assertEquals(changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL), "vx1");
        assertEquals(changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE), CompareGraphPlugin.REMOVED);
    }

    @Test
    public void testCompareGraphsWithMissingNodeInChain() throws Exception {
        // mimic GraphRecordStoreUtilities.getAll()

        final GraphRecordStore original = new GraphRecordStore();
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx2");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "1");
        original.add();
        original.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx1");
        original.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "vx2");
        original.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "2");

        final GraphRecordStore compare = new GraphRecordStore();
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx0");
        compare.add();
        compare.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "vx2");

        final Set<String> vertexPrimaryKeys = new HashSet<>();
        vertexPrimaryKeys.add(VisualConcept.VertexAttribute.LABEL.getName());

        final Set<String> transactionPrimaryKeys = new HashSet<>();
        transactionPrimaryKeys.add(VisualConcept.TransactionAttribute.IDENTIFIER.getName());

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", original, compare, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        System.out.println("original ==>\n" + original.toStringVerbose());
        System.out.println("compare ==>\n" + compare.toStringVerbose());
        System.out.println("changes ==>\n" + changes.toStringVerbose());

        assertEquals(changes.size(), 5);

        changes.reset();
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx2", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals(CompareGraphPlugin.UNCHANGED, changes.get(GraphRecordStoreUtilities.SOURCE + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx0", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL));
        assertEquals("1", changes.get(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER));
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE));
        changes.next();
        assertEquals("vx1", changes.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL));
        assertEquals("vx2", changes.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL));
        assertEquals("2", changes.get(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER));
        assertEquals(CompareGraphPlugin.REMOVED, changes.get(GraphRecordStoreUtilities.TRANSACTION + CompareGraphPlugin.COMPARE_ATTRIBUTE));
    }

    /**
     * Test of createComparisonGraph method, of class CompareGraphPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCreateComparisonGraphWithAddedTransactionInReverse() throws Exception {
        int vx0, vx1, tx0, tx1;
        int labelAttribute, uniqueIdAttribute;

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(originalGraph);
        uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute);
        vx0 = originalGraph.addVertex();
        vx1 = originalGraph.addVertex();
        tx0 = originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.setStringValue(labelAttribute, vx0, "vx0");
        originalGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(uniqueIdAttribute, tx0, "1");

        final StoreGraph compareGraph = new StoreGraph(schema);
        labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(compareGraph);
        uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(compareGraph);
        compareGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        compareGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute);
        vx0 = compareGraph.addVertex();
        vx1 = compareGraph.addVertex();
        tx0 = compareGraph.addTransaction(vx1, vx0, true);
        compareGraph.setStringValue(labelAttribute, vx0, "vx0");
        compareGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(uniqueIdAttribute, tx0, "2");

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);
        final GraphRecordStore compareAll = GraphRecordStoreUtilities.getAll(compareGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        final boolean initializeWithSchema = true;
        final boolean completeWithSchema = false;

        final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        System.out.println("changes ==>\n" + changes.toStringVerbose());

        final ReadableGraph rg = finalGraph.getReadableGraph();
        try {
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(rg.getTransactionCount(), 2);

            vx0 = rg.getVertex(0);
            vx1 = rg.getVertex(1);
            tx0 = rg.getTransaction(0);
            tx1 = rg.getTransaction(1);

            labelAttribute = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.LABEL.getName());
            assertEquals(rg.getStringValue(labelAttribute, vx0), "vx0");
            assertEquals(rg.getStringValue(labelAttribute, vx1), "vx1");
            assertEquals(rg.getTransactionSourceVertex(tx0), vx0);
            assertEquals(rg.getTransactionSourceVertex(tx1), vx1);
        } finally {
            rg.release();
        }

        SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "testCreateComparisonGraphWithAddedTransactionInReverse", true);
    }

    /**
     * Test of collectStatisticsFromGraph method, of class CompareGraphPlugin.
     */
    @Test
    public void testCollectStatisticsFromGraph() {
        final int vx0, vx1, tx0;
        final StoreGraph graph = new StoreGraph();
        final int labelAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        vx0 = graph.addVertex();
        vx1 = graph.addVertex();
        tx0 = graph.addTransaction(vx0, vx1, true);
        graph.setStringValue(labelAttribute, vx0, "vx0");
        graph.setStringValue(labelAttribute, vx1, "vx1");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final Map<String, Integer> expResult = new HashMap<>();
        expResult.put(CompareGraphPlugin.GLOBAL_MODIFICATION_COUNT, 6);
        expResult.put(CompareGraphPlugin.NODES_COUNT, 2);
        expResult.put(CompareGraphPlugin.TRANSACTIONS_COUNT, 1);
        expResult.put(CompareGraphPlugin.NODE_ATTRIBUTE_COUNT, 0); // didn't run complete with schema
        expResult.put(CompareGraphPlugin.TRANSACTION_ATTRIBUTE_COUNT, 0); // didn't run complete with schema

        final Map<String, Integer> result = instance.collectStatisticsFromGraph(graph);
        assertEquals(expResult, result);
    }

    /**
     * Test of calculateStatisticalDifferences method, of class
     * CompareGraphPlugin.
     */
    @Test
    public void testCalculateStatisticalDifferences() {
        final Map<String, Integer> originalStatistics = new HashMap<>();
        originalStatistics.put(CompareGraphPlugin.GLOBAL_MODIFICATION_COUNT, 6);
        originalStatistics.put(CompareGraphPlugin.NODES_COUNT, 2);
        originalStatistics.put(CompareGraphPlugin.TRANSACTIONS_COUNT, 1);
        originalStatistics.put(CompareGraphPlugin.NODE_ATTRIBUTE_COUNT, 8);
        originalStatistics.put(CompareGraphPlugin.TRANSACTION_ATTRIBUTE_COUNT, 6);

        final Map<String, Integer> compareStatistics = new HashMap<>();
        compareStatistics.put(CompareGraphPlugin.GLOBAL_MODIFICATION_COUNT, 6);
        compareStatistics.put(CompareGraphPlugin.NODES_COUNT, 3);
        compareStatistics.put(CompareGraphPlugin.TRANSACTIONS_COUNT, 6);
        compareStatistics.put(CompareGraphPlugin.NODE_ATTRIBUTE_COUNT, 9);
        compareStatistics.put(CompareGraphPlugin.TRANSACTION_ATTRIBUTE_COUNT, 10);

        CompareGraphPlugin instance = new CompareGraphPlugin();

        final Map<String, Integer> expResult = new HashMap<>();
        expResult.put(CompareGraphPlugin.GLOBAL_MODIFICATION_COUNT, 0);
        expResult.put(CompareGraphPlugin.NODES_COUNT, 1);
        expResult.put(CompareGraphPlugin.TRANSACTIONS_COUNT, 5);
        expResult.put(CompareGraphPlugin.NODE_ATTRIBUTE_COUNT, 1);
        expResult.put(CompareGraphPlugin.TRANSACTION_ATTRIBUTE_COUNT, 4);

        final Map<String, Integer> result = instance.calculateStatisticalDifferences(originalStatistics, compareStatistics);
        assertEquals(expResult, result);
    }

//    /**
//     * Test of getVertexKeysToRecordstoreIndex method, of class
// CompareGraphPlugin.
//     */
//    @Test
//    public void testGetLabelToIndexMapping() {
//        final GraphRecordStore recordstore = new GraphRecordStore();
//        recordstore.add();
//        recordstore.set(GraphRecordStoreUtilities.SOURCE + "foo", "bar");
//        recordstore.add();
//        recordstore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "baz");
//
//        CompareGraphPlugin instance = new CompareGraphPlugin();
//        final Map<String, Integer> expResult = new HashMap<>();
//        expResult.put("baz", 1);
//
//        final Map<String, Integer> result = instance.getVertexLabelToRecordstoreIndexMapping(recordstore);
//        assertEquals(expResult, result);
//    }
}

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
public class CompareGraphPluginNGTest {

    public static final ConstellationColor ADDED_COLOR = ConstellationColor.DARK_GREEN;
    public static final ConstellationColor REMOVED_COLOR = ConstellationColor.RED;
    public static final ConstellationColor CHANGED_COLOR = ConstellationColor.YELLOW;
    public static final ConstellationColor UNCHANGED_COLOR = ConstellationColor.GREY;
    
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

    @Test
    public void testReadWithMultipleChangesToVertices() throws PluginException, InterruptedException, IOException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        int labelAttribute = originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Label", "", "", null);
        int lineStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(originalGraph);
        originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "foo", "", "", null);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, lineStyleAttribute);

        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        int vx2 = originalGraph.addVertex();
        int vx3 = originalGraph.addVertex();
        int vx4 = originalGraph.addVertex();
        
        originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.addTransaction(vx0, vx4, true);
        
        originalGraph.setStringValue(labelAttribute, vx0, "vx0");
        originalGraph.setStringValue(labelAttribute, vx1, "vx1");
        originalGraph.setStringValue(labelAttribute, vx2, "vx2");
        originalGraph.setStringValue(labelAttribute, vx3, "vx3");
        originalGraph.setStringValue(labelAttribute, vx4, "vx4");

        final StoreGraph compareGraph = new StoreGraph(schema);
        labelAttribute = compareGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Label", "", "", null);
        final int fooAttribute = compareGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "foo", "", "", null);
        lineStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(compareGraph);
        compareGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        compareGraph.setPrimaryKey(GraphElementType.TRANSACTION, lineStyleAttribute);

        vx0 = compareGraph.addVertex();
        vx1 = compareGraph.addVertex();
        vx2 = compareGraph.addVertex();
        vx4 = compareGraph.addVertex();
        int vx5 = compareGraph.addVertex();
        
        compareGraph.addTransaction(vx0, vx1, true);
        compareGraph.addTransaction(vx0, vx5, true); // # change is ADDED
        
        compareGraph.setStringValue(labelAttribute, vx0, "vx0");
        compareGraph.setStringValue(labelAttribute, vx1, "vx1");
        compareGraph.setStringValue(labelAttribute, vx2, "vx2");
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

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        final GraphRecordStore changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
        assertEquals(changes.size(), 9);

        final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);

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

        try (final ReadableGraph rg = finalGraph.getReadableGraph()) {
            int vxCount = rg.getVertexCount();
            int txCount = rg.getTransactionCount();

            assertEquals(vxCount, 6);
            assertEquals(txCount, 3);
        }

        SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
    }

    @Test
    public void testReadWithDuplicateGraphScenario() throws InterruptedException, PluginException, IOException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(originalGraph);
        final int uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, identifierAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute);

        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        int vx2 = originalGraph.addVertex();
        
        originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.addTransaction(vx1, vx2, true);
        
        originalGraph.setStringValue(identifierAttribute, vx0, "vx0");
        originalGraph.setStringValue(identifierAttribute, vx1, "vx1");
        originalGraph.setStringValue(identifierAttribute, vx2, "vx2");

        final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
        final PluginParameters copyParams = copyGraphPlugin.createParameters();
        copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID).setBooleanValue(true);
        PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyParams).executeNow((GraphReadMethods) originalGraph);
        Graph compareGraph = (Graph) copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();

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

        GraphRecordStore compareAll;
        try (final ReadableGraph rg = compareGraph.getReadableGraph()) {
            compareAll = GraphRecordStoreUtilities.getAll(rg, false, true);
        }

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);

        final Set<String> vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.VERTEX);
        final Set<String> transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(originalGraph, GraphElementType.TRANSACTION);

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        GraphRecordStore changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
        assertEquals(changes.size(), 5);

        Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);

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

        try (final ReadableGraph rg = finalGraph.getReadableGraph()) {
            int vxCount = rg.getVertexCount();
            int txCount = rg.getTransactionCount();

            assertEquals(vxCount, 3);
            assertEquals(txCount, 2);
        }

        SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph", true);
        SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
    }
    
    @Test
    public void testReadWithDuplicateGraphScenarioInReverse() throws InterruptedException, PluginException, IOException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(originalGraph);
        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(originalGraph);
        final int uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(originalGraph);
        final int transactionDateTimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, identifierAttribute, vertexTypeAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute, transactionTypeAttribute, transactionDateTimeAttribute);
        originalGraph.validateKeys();

        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        int vx2 = originalGraph.addVertex();
        
        originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.addTransaction(vx1, vx2, true);
        
        originalGraph.setStringValue(identifierAttribute, vx0, "Vertex #0");
        originalGraph.setStringValue(identifierAttribute, vx1, "Vertex #1");
        originalGraph.setStringValue(identifierAttribute, vx2, "Vertex #2"); // mimic creating nodes on visual schema which will create a DuplicateKeyException - i.e. this is a known issue
        originalGraph.setStringValue(vertexTypeAttribute, vx0, "Unknown");
        originalGraph.setStringValue(vertexTypeAttribute, vx1, "Unknown");
        originalGraph.setStringValue(vertexTypeAttribute, vx2, "Unknown");     

        final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
        final PluginParameters copyGraphParams = copyGraphPlugin.createParameters();
        PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyGraphParams).executeNow((GraphReadMethods) originalGraph);
        final Graph compareGraph = (Graph) copyGraphParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();

        final WritableGraph wg = compareGraph.getWritableGraph("remove a node", true);
        try {
            Assert.assertEquals(wg.getVertexCount(), 3);
            wg.removeVertex(vx1);
            Assert.assertEquals(wg.getVertexCount(), 2);
        } finally {
            wg.commit();
        }

        GraphRecordStore compareAll;
        try (final ReadableGraph rg = compareGraph.getReadableGraph()) {
            compareAll = GraphRecordStoreUtilities.getAll(rg, false, true);
        }

        final GraphRecordStore originalAll = GraphRecordStoreUtilities.getAll(originalGraph, false, true);

        Set<String> vertexPrimaryKeys;
        Set<String> transactionPrimaryKeys;
        try (final ReadableGraph rg = compareGraph.getReadableGraph()) {
            vertexPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg, GraphElementType.VERTEX);
            transactionPrimaryKeys = PrimaryKeyUtilities.getPrimaryKeyNames(rg, GraphElementType.TRANSACTION);
        }

        final List<String> ignoreVertexAttributes = new ArrayList<>();
        final List<String> ignoreTransactionAttributes = new ArrayList<>();
        ignoreVertexAttributes.add("[id]");
        ignoreTransactionAttributes.add("[id]");

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        GraphRecordStore changes = instance.compareGraphs("", compareAll, originalAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);

        final Graph finalGraph = instance.createComparisonGraph(compareGraph, changes);

        SaveGraphUtilities.saveGraphToTemporaryDirectory(originalGraph, "originalGraph");
        SaveGraphUtilities.saveGraphToTemporaryDirectory(compareGraph, "compareGraph", true);
        SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "finalGraph", true);
    }

    @Test
    public void testReadWithNodesInDifferentOrder() throws PluginException, InterruptedException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        int labelAttribute = originalGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        int vx2 = originalGraph.addVertex();
        int vx3 = originalGraph.addVertex();
        
        originalGraph.addTransaction(vx0, vx1, true);
        
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
        
        compareGraph.addTransaction(vx3, vx2, true);
        
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

        final CompareGraphPlugin instance = new CompareGraphPlugin();

        GraphRecordStore changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
        assertEquals(changes.size(), 5);

        instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
    }

    @Test
    public void testReadWithTransactionsInBothDirections() throws PluginException, InterruptedException, IOException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        int labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);

        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        int vx2 = originalGraph.addVertex();
        int vx3 = originalGraph.addVertex();
        
        originalGraph.addTransaction(vx0, vx1, true);
        originalGraph.addTransaction(vx1, vx2, true);
        
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
        
        compareGraph.addTransaction(vx0, vx1, true);
        compareGraph.addTransaction(vx2, vx1, true); // this is the change
        
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

        final CompareGraphPlugin instance = new CompareGraphPlugin();
        GraphRecordStore changes = instance.compareGraphs("", originalAll, compareAll, vertexPrimaryKeys, transactionPrimaryKeys, ignoreVertexAttributes, ignoreTransactionAttributes, ADDED_COLOR, REMOVED_COLOR, CHANGED_COLOR, UNCHANGED_COLOR);
        assertEquals(changes.size(), 7);

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
        
        final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        SaveGraphUtilities.saveGraphToTemporaryDirectory(finalGraph, "testReadWithTransactionsInBothDirections", true);
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        final StoreGraph originalGraph = new StoreGraph(schema);
        int labelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(originalGraph);
        int uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(originalGraph);
        originalGraph.setPrimaryKey(GraphElementType.VERTEX, labelAttribute);
        originalGraph.setPrimaryKey(GraphElementType.TRANSACTION, uniqueIdAttribute);
        
        int vx0 = originalGraph.addVertex();
        int vx1 = originalGraph.addVertex();
        
        int tx0 = originalGraph.addTransaction(vx0, vx1, true);
        
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

        final Graph finalGraph = instance.createComparisonGraph(new DualGraph(originalGraph, true), changes);
        
        try (final ReadableGraph rg = finalGraph.getReadableGraph()) {
            assertEquals(rg.getVertexCount(), 2);
            assertEquals(rg.getTransactionCount(), 2);

            vx0 = rg.getVertex(0);
            vx1 = rg.getVertex(1);
            tx0 = rg.getTransaction(0);
            int tx1 = rg.getTransaction(1);

            labelAttribute = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.LABEL.getName());
            assertEquals(rg.getStringValue(labelAttribute, vx0), "vx0");
            assertEquals(rg.getStringValue(labelAttribute, vx1), "vx1");
            assertEquals(rg.getTransactionSourceVertex(tx0), vx0);
            assertEquals(rg.getTransactionSourceVertex(tx1), vx1);
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
        final StoreGraph graph = new StoreGraph();
        final int labelAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        
        graph.addTransaction(vx0, vx1, true);
        
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
}

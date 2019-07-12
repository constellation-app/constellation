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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.pluginframework.text.TextPluginInteraction;
import au.gov.asd.tac.constellation.schema.analyticschema.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.schema.analyticschema.concept.AnalyticConcept;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Select TopN Test.
 *
 * @author arcturus
 */
public class SelectTopNNGTest {

    public SelectTopNNGTest() {
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

//    /**
//     * Test of getType method, of class SelectTopNPlugin.
//     */
//    @Test
//    public void testGetType() {
//        System.out.println("getType");
//        SelectTopNPlugin instance = new SelectTopNPlugin();
//        String expResult = "";
//        String result = instance.getType();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPosition method, of class SelectTopNPlugin.
//     */
//    @Test
//    public void testGetPosition() {
//        System.out.println("getPosition");
//        SelectTopNPlugin instance = new SelectTopNPlugin();
//        int expResult = 0;
//        int result = instance.getPosition();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDescription method, of class SelectTopNPlugin.
//     */
//    @Test
//    public void testGetDescription() {
//        System.out.println("getDescription");
//        SelectTopNPlugin instance = new SelectTopNPlugin();
//        String expResult = "";
//        String result = instance.getDescription();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createParameters method, of class SelectTopNPlugin.
//     */
//    @Test
//    public void testCreateParameters() {
//        System.out.println("createParameters");
//        SelectTopNPlugin instance = new SelectTopNPlugin();
//        PluginParameters expResult = null;
//        PluginParameters result = instance.createParameters();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of edit method, of class SelectTopNPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = PluginException.class)
    public void testEditWithNothingSelected() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int vx0 = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, vx0, "foo");
        graph.setBooleanValue(vertexSelectedAttr, vx0, false);

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        instance.edit(graph, interaction, parameters);
    }

    @Test
    public void testEditWithNoResults() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int vx0 = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, vx0, "foo");
        graph.setBooleanValue(vertexSelectedAttr, vx0, true);

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.NODE);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.VertexType.LOCATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.VertexType.COUNTRY.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(2);
        instance.edit(graph, interaction, parameters);

        assertEquals(1, graph.getVertexCount());
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, vx0));
    }

    @Test
    public void testEditWithTopFiveLocationsWithEqualCounts() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "source");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);
        graph.setObjectValue(vertexTypeAttr, sourceVxId, AnalyticConcept.VertexType.COUNTRY);

        // buildId the graph
        for (int i = 0; i < 5; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            graph.setObjectValue(vertexTypeAttr, desintationVxId, AnalyticConcept.VertexType.COUNTRY);
            for (int j = 0; j < 5; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.NODE);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.VertexType.LOCATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.VertexType.COUNTRY.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(5);
        instance.edit(graph, interaction, parameters);

        for (int i = 0; i < 5; i++) {
            assertTrue(graph.getBooleanValue(vertexSelectedAttr, i));
        }
    }

    @Test
    public void testEditWithTopTwoLocationsAndEverythingIsGci() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "source");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);
        graph.setObjectValue(vertexTypeAttr, sourceVxId, AnalyticConcept.VertexType.COUNTRY);

        // buildId the graph
        for (int i = 0; i < 10; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            graph.setObjectValue(vertexTypeAttr, desintationVxId, AnalyticConcept.VertexType.COUNTRY);
            for (int j = i; j < 10; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.NODE);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.VertexType.LOCATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.VertexType.COUNTRY.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(2);
        instance.edit(graph, interaction, parameters);

        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 1));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 2));

        for (int i = 3; i < 10; i++) {
            assertFalse(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + i));
        }
    }

    @Test
    public void testEditWithTopTwoLocationsContainingDifferentTypes() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "source");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);
        graph.setObjectValue(vertexTypeAttr, sourceVxId, AnalyticConcept.VertexType.COUNTRY);

        // buildId the graph
        for (int i = 0; i < 2; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            graph.setObjectValue(vertexTypeAttr, desintationVxId, AnalyticConcept.VertexType.COUNTRY);
            for (int j = i; j < 10; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        for (int i = 3; i < 10; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            graph.setObjectValue(vertexTypeAttr, desintationVxId, AnalyticConcept.VertexType.DOCUMENT);
            for (int j = i; j < 10; j++) {
                graph.addTransaction(sourceVxId, desintationVxId, true);
            }
        }

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.NODE);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.VertexType.LOCATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.VertexType.COUNTRY.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(2);
        instance.edit(graph, interaction, parameters);

        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 1));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 2));

        for (int i = 3; i < 10; i++) {
            assertFalse(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + i));
        }
    }

    @Test
    public void testEditWithTopTwoContactsAndEverythingIsCommunicationTransactions() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "source");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);

        // buildId the graph
        for (int i = 0; i < 10; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            for (int j = i; j < 10; j++) {
                int txId = graph.addTransaction(sourceVxId, desintationVxId, true);
                graph.setObjectValue(transactionTypeAttr, txId, AnalyticConcept.TransactionType.COMMUNICATION.getName());
            }
        }

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.TRANSACTION);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.TransactionType.COMMUNICATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.TransactionType.COMMUNICATION.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(2);
        instance.edit(graph, interaction, parameters);

        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 1));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 2));

        for (int i = 3; i < 10; i++) {
            assertFalse(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + i));
        }
    }

    @Test
    public void testEditWithTopTwoLocationsContainingDifferentTransactionTypes() throws Exception {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vertexLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vertexTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        final int sourceVxId = graph.addVertex();
        graph.setStringValue(vertexLabelAttr, sourceVxId, "source");
        graph.setBooleanValue(vertexSelectedAttr, sourceVxId, true);

        // buildId the graph
        for (int i = 0; i < 2; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            for (int j = i; j < 10; j++) {
                int txId = graph.addTransaction(sourceVxId, desintationVxId, true);
                graph.setObjectValue(transactionTypeAttr, txId, AnalyticConcept.TransactionType.COMMUNICATION.getName());
            }
        }

        for (int i = 3; i < 10; i++) {
            final int desintationVxId = graph.addVertex();
            graph.setStringValue(vertexLabelAttr, desintationVxId, String.format("destination %s", i));
            for (int j = i; j < 10; j++) {
                int txId = graph.addTransaction(sourceVxId, desintationVxId, true);
                graph.setObjectValue(transactionTypeAttr, txId, AnalyticConcept.TransactionType.NETWORK);
            }
        }

        final PluginInteraction interaction = new TextPluginInteraction();
        final SelectTopNPlugin instance = new SelectTopNPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.getParameters().get(SelectTopNPlugin.MODE_PARAMETER_ID).setStringValue(SelectTopNPlugin.TRANSACTION);
        parameters.getParameters().get(SelectTopNPlugin.TYPE_CATEGORY_PARAMETER_ID).setStringValue(AnalyticConcept.TransactionType.COMMUNICATION.getName());
        final PluginParameter subTypeParameter = parameters.getParameters().get(SelectTopNPlugin.TYPE_PARAMETER_ID);
        final List<String> arrayList = new ArrayList<>();
        arrayList.add(AnalyticConcept.TransactionType.COMMUNICATION.getName());
        MultiChoiceParameterType.setChoices(subTypeParameter, arrayList);
        parameters.getParameters().get(SelectTopNPlugin.LIMIT_PARAMETER_ID).setIntegerValue(2);
        instance.edit(graph, interaction, parameters);

        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 1));
        assertTrue(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + 2));

        for (int i = 3; i < 10; i++) {
            assertFalse(graph.getBooleanValue(vertexSelectedAttr, sourceVxId + i));
        }
    }

}

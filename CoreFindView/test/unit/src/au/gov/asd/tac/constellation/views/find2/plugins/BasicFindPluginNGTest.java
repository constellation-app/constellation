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
package au.gov.asd.tac.constellation.views.find2.plugins;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.FindViewTopComponent;
import au.gov.asd.tac.constellation.views.find2.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindGraphSelectionPlugin;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class BasicFindPluginNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Graph graph;
    private Graph graph2;
    private int selectedV, selectedT;
    private int labelV, identifierV, xV, labelT, identiferT, widthT;
    private int vxId1, vxId2, vxId3, vxId4, vxId5UpperCase, vxId6, vxId7, vxId8, txId1, txId2, txId3, txId4;
    private List<Attribute> attributeList = new ArrayList<>();
    private BasicFindReplaceParameters parameters = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, false, false, false);
    private BasicFindReplaceParameters parametersTransactionType = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.TRANSACTION, attributeList, true, false, false, false, false, false, false, false, false);
    private BasicFindReplaceParameters parametersRegEx = new BasicFindReplaceParameters("la*", "", GraphElementType.GRAPH.VERTEX, attributeList, false, true, false, false, false, false, false, false, false);
    private BasicFindReplaceParameters parametersIgnoreCase = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, true, false, false, false, false, false, false);
    private BasicFindReplaceParameters parametersExactMatch = new BasicFindReplaceParameters("label", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, true, false, false, false, false, false);
    private BasicFindReplaceParameters parametersFindIn = new BasicFindReplaceParameters("test", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, true, false, false, false, false);
    private BasicFindReplaceParameters parametersAddTo = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, true, false, false, false);
    private BasicFindReplaceParameters parametersRemoveFrom = new BasicFindReplaceParameters("label name", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, true, false, false);

    private BasicFindReplaceParameters parameters1 = new BasicFindReplaceParameters("equal", "replace", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, false, false, false);
    private BasicFindReplaceParameters parameters2 = new BasicFindReplaceParameters("notEqual", "replace", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, false, false, false);
    private BasicFindReplaceParameters parametersClearSelections = new BasicFindReplaceParameters("clear", "", GraphElementType.GRAPH.VERTEX, attributeList, true, false, false, false, false, false, false, false, false);
    private static final Logger LOGGER = Logger.getLogger(BasicFindPluginNGTest.class.getName());

    public BasicFindPluginNGTest() {
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
     * Test of edit method, of class BasicFindPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");
        setupGraph();
        attributeList.addAll(getAttributes());

        BasicFindPlugin basicFindPlugin = new BasicFindPlugin(parameters, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        ReadableGraph rg = graph.getReadableGraph();
        /**
         * Testing finding all elements with "label name", Should select
         * vxId1,2,3,4
         */
        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        /**
         * Testing finding all elements with "clear", should deselect all
         * existing elements as no elements have clear in their name
         */
        basicFindPlugin = new BasicFindPlugin(parametersClearSelections, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        rg.close();

        /**
         * Testing finding next element with "label name", Should select vxId1
         */
        basicFindPlugin = new BasicFindPlugin(parameters, false, true);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        BasicFindGraphSelectionPlugin findGraphSelectionPlugin = new BasicFindGraphSelectionPlugin(parameters, false);
        ActiveFindResultsList.getBasicResultsList().incrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        rg.close();

        /**
         * Testing finding next element with "label name", Should select vxId2
         * and deselect vxId1
         */
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        findGraphSelectionPlugin = new BasicFindGraphSelectionPlugin(parameters, false);
        ActiveFindResultsList.getBasicResultsList().incrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        rg.close();

        /**
         * Testing finding previous element with "label name", Should select
         * vxId1 and deselect vxId2
         */
        basicFindPlugin = new BasicFindPlugin(parameters, false, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        findGraphSelectionPlugin = new BasicFindGraphSelectionPlugin(parameters, false);
        ActiveFindResultsList.getBasicResultsList().decrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        rg.close();

        /**
         * Testing finding all transaction elements with "label name", Should
         * select txId1,2,3,4
         */
        basicFindPlugin = new BasicFindPlugin(parametersTransactionType, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), true);
        assertEquals(rg.getBooleanValue(selectedT, txId4), true);
        rg.close();

        /**
         * clear selection
         */
        basicFindPlugin = new BasicFindPlugin(parametersClearSelections, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);

        /**
         * Testing finding all elements with regEx "la*", should select
         * vxId1,2,3,4
         */
        basicFindPlugin = new BasicFindPlugin(parametersRegEx, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        /**
         * clear selection
         */
        basicFindPlugin = new BasicFindPlugin(parametersClearSelections, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);

        /**
         * Testing finding all elements with ignoreCase "label name" should
         * select vxId1,2,3,4,5UpperCase
         */
        basicFindPlugin = new BasicFindPlugin(parametersIgnoreCase, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId5UpperCase), true);
        rg.close();

        /**
         * clear selection
         */
        basicFindPlugin = new BasicFindPlugin(parametersClearSelections, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);

        /**
         * Testing finding all elements without ignoreCase "label name" should
         * select vxId1,2,3,4
         */
        basicFindPlugin = new BasicFindPlugin(parameters, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId5UpperCase), false);
        rg.close();

        /**
         * clear selection
         */
        basicFindPlugin = new BasicFindPlugin(parametersClearSelections, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);

        /**
         * Testing finding all elements with exactMatch "label" shouldn't select
         * anything
         */
        basicFindPlugin = new BasicFindPlugin(parametersExactMatch, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        rg.close();

        /**
         * Testing finding all elements within the current selection that
         * contain the word "test" should select vxId6 but not vxId8 since it
         * isn't selected
         */
        basicFindPlugin = new BasicFindPlugin(parametersFindIn, true, false);
        WritableGraph wg = graph.getWritableGraph("", true);

        wg.setBooleanValue(selectedV, vxId1, true);
        wg.setBooleanValue(selectedV, vxId2, true);
        wg.setBooleanValue(selectedV, vxId3, true);
        wg.setBooleanValue(selectedV, vxId4, true);
        wg.setBooleanValue(selectedV, vxId6, true);
        wg.setBooleanValue(selectedV, vxId7, true);
        wg.setBooleanValue(selectedV, vxId8, false);
        wg.commit();

        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId8), false);
        rg.close();

        /**
         * Testing finding all elements with "label name" and adding them to the
         * current selected items. vxId1,2,3,4 and 6 should be selected
         */
        basicFindPlugin = new BasicFindPlugin(parametersAddTo, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId8), false);
        rg.close();

        /**
         * Testing removing all elements "label name" from the currently
         * selected. Only vxId6 should remain selected
         */
        basicFindPlugin = new BasicFindPlugin(parametersRemoveFrom, true, false);
        PluginExecution.withPlugin(basicFindPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId8), false);
        rg.close();

    }

    /**
     * Test of getName method, of class BasicFindPlugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        BasicFindPlugin instance = new BasicFindPlugin(parameters, true, false);
        String expResult = "Find: Find and Replace";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        graph2 = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap.put(graph.getId(), graph);
        graphMap.put(graph2.getId(), graph2);
        try {

            WritableGraph wg = graph.getWritableGraph("", true);

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            labelV = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            identifierV = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            xV = VisualConcept.VertexAttribute.X.ensure(wg);

            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            labelT = VisualConcept.TransactionAttribute.LABEL.ensure(wg);
            identiferT = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(wg);
            widthT = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);

            vxId1 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId1, false);
            wg.setStringValue(labelV, vxId1, "label name");
            wg.setStringValue(identifierV, vxId1, "identifer name");
            wg.setIntValue(xV, vxId1, 1);

            vxId2 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId2, false);
            wg.setStringValue(labelV, vxId2, "label name");
            wg.setStringValue(identifierV, vxId2, "identifer name");
            wg.setIntValue(xV, vxId2, 1);

            vxId3 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId3, false);
            wg.setStringValue(labelV, vxId3, "label name");
            wg.setStringValue(identifierV, vxId3, "identifer name");
            wg.setIntValue(xV, vxId3, 1);

            vxId4 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId4, false);
            wg.setStringValue(labelV, vxId4, "label name");
            wg.setStringValue(identifierV, vxId4, "identifer name");
            wg.setIntValue(xV, vxId4, 1);

            vxId5UpperCase = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId5UpperCase, false);
            wg.setStringValue(labelV, vxId5UpperCase, "LABEL NAME");
            wg.setStringValue(identifierV, vxId5UpperCase, "IDENTIFIER NAME");
            wg.setIntValue(xV, vxId5UpperCase, 1);

            vxId6 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId6, false);
            wg.setStringValue(labelV, vxId6, "test");
            wg.setStringValue(identifierV, vxId6, "a vertex");
            wg.setIntValue(xV, vxId6, 1);

            vxId7 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId7, false);
            wg.setStringValue(labelV, vxId7, "experiment");
            wg.setStringValue(identifierV, vxId7, "a vertex");
            wg.setIntValue(xV, vxId7, 1);

            vxId8 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId8, false);
            wg.setStringValue(labelV, vxId8, "test");
            wg.setStringValue(identifierV, vxId8, "a node");
            wg.setIntValue(xV, vxId8, 1);

            txId1 = wg.addTransaction(vxId1, vxId1, false);
            wg.setBooleanValue(selectedT, txId1, false);
            wg.setStringValue(labelT, txId1, "label name");
            wg.setStringValue(identiferT, txId1, "identifer name");
            wg.setIntValue(widthT, txId1, 1);

            txId2 = wg.addTransaction(vxId1, vxId2, false);
            wg.setBooleanValue(selectedT, txId2, false);
            wg.setStringValue(labelT, txId2, "label name");
            wg.setStringValue(identiferT, txId2, "identifer name");
            wg.setIntValue(widthT, txId2, 1);

            txId3 = wg.addTransaction(vxId1, vxId3, false);
            wg.setBooleanValue(selectedT, txId3, false);
            wg.setStringValue(labelT, txId3, "label name");
            wg.setStringValue(identiferT, txId3, "identifer name");
            wg.setIntValue(widthT, txId3, 1);

            txId4 = wg.addTransaction(vxId1, vxId4, false);
            wg.setBooleanValue(selectedT, txId4, false);
            wg.setStringValue(labelT, txId4, "label name");
            wg.setStringValue(identiferT, txId4, "identifer name");
            wg.setIntValue(widthT, txId4, 1);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Used to convert the string variant of attributes to the Attribute object
     *
     * @return the list of Attribute objects
     */
    private ArrayList<Attribute> getAttributes() {
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class
        );
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);
        final GraphManager gm = Mockito.mock(GraphManager.class
        );
        when(gm.getAllGraphs()).thenReturn(graphMap);

        ArrayList<Attribute> attributes = new ArrayList<>();

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(
                    () -> GraphManager.getDefault()).thenReturn(gm);

            GraphElementType type = GraphElementType.VERTEX;
            List<String> result = instance.populateAttributes(type, attributes, Long.MIN_VALUE);

            ReadableGraph rg = graph.getReadableGraph();

            for (int i = 0;
                    i < result.size();
                    i++) {
                int attributeInt = rg.getAttribute(type, result.get(i));
                GraphAttribute ga = new GraphAttribute(rg, attributeInt);
                if (ga.getAttributeType().equals("string")) {
                    attributes.add(new GraphAttribute(rg, attributeInt));
                    System.out.println(attributes.get(i).getName() + " = attribute name");
                }
            }

            rg.close();
        }
        return attributes;

    }
}

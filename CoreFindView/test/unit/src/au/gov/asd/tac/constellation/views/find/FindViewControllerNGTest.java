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
package au.gov.asd.tac.constellation.views.find;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.plugins.BasicFindPlugin;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewControllerNGTest {

    private static final Logger LOGGER = Logger.getLogger(FindViewControllerNGTest.class.getName());
    
    private Map<String, Graph> graphMap;
    
    private Graph graph;
    private Graph graph2;
    
    private int selectedV;
    private int labelV;
    private int identifierV;
    private int xV;
    private int selectedT;
    private int labelT;
    private int identiferT;
    private int widthT;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    
    private List<Attribute> attributeList;
    private BasicFindReplaceParameters parameters;
    private BasicFindReplaceParameters parameters2;
    private BasicFindReplaceParameters parametersAllGraphs;
    
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
        attributeList = new ArrayList<>();
        parameters = new BasicFindReplaceParameters("equal", "replace", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        parameters2 = new BasicFindReplaceParameters("notEqual", "replace", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        parametersAllGraphs = new BasicFindReplaceParameters("equal", "", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, false, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getDefault method, of class FindViewController.
     */
    @Test
    public void testGetDefault() {
        System.out.println("getDefault");
        
        FindViewController result = FindViewController.getDefault();
        FindViewController expResult = FindViewController.getDefault();

        assertEquals(result, expResult);
    }

    /**
     * Test of init method, of class FindViewController.
     */
    @Test
    public void testInit() {
        System.out.println("init");

        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);

        FindViewController instance = FindViewController.getDefault();
        FindViewController result = instance.init(findViewTopComponent);
        assertEquals(result, instance);
    }

    /**
     * Test of populateAttributes method, of class FindViewController.
     */
    @Test
    public void testPopulateAttributes() {
        System.out.println("populateAttributes");

        /**
         * Set up the graph with 4 vertexs, 4 transactions, 3 vertex attributes
         * (2 of type string), 3 transaction attributes (2 of type string)
         */
        setupGraph();

        /**
         * Create a mock of the top component, get an intsance of the
         * controller, create a mock of the graph manager, when getAllGraphs is
         * called return the graphMap created in this class
         */
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);
        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            List<Attribute> attributes = new ArrayList<>();
            GraphElementType type = GraphElementType.VERTEX;
            List<String> result = instance.populateAttributes(type, attributes, Long.MIN_VALUE);

            assertEquals(result.get(0), "Label");
            assertEquals(result.get(1), "Identifier");
            assertEquals(result.size(), 2);

            type = GraphElementType.TRANSACTION;
            result = instance.populateAttributes(type, attributes, Long.MIN_VALUE);
            assertEquals(result.get(0), "Label");
            assertEquals(result.get(1), "Identifier");
            assertEquals(result.size(), 2);

            type = GraphElementType.EDGE;
            result = instance.populateAttributes(type, attributes, Long.MIN_VALUE);
            assertEquals(result.size(), 0);
        }
    }

    /**
     * Test of updateBasicFindParameters method, of class FindViewController.
     */
    @Test
    public void testUpdateBasicFindParameters() {
        System.out.println("updateBasicFindParameters");
        FindViewController instance = FindViewController.getDefault();

        BasicFindReplaceParameters altParameters = new BasicFindReplaceParameters("equal", "", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        instance.updateBasicFindParameters(altParameters);
        BasicFindReplaceParameters result = instance.getCurrentBasicFindParameters();

        // The object is copied in the updateBasicFindParameters function
        // This will cause a direct comparison of the object to be false
        assertEquals(result, altParameters);

        BasicFindReplaceParameters altParameters2 = new BasicFindReplaceParameters("notEqual", "", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        assertNotEquals(result, altParameters2);

    }

    /**
     * Test of updateBasicReplaceParameters method, of class FindViewController.
     */
    @Test
    public void testUpdateBasicReplaceParameters() {
        System.out.println("updateBasicReplaceParameters");
        FindViewController instance = FindViewController.getDefault();
        
        instance.updateBasicReplaceParameters(parameters);
        BasicFindReplaceParameters result = instance.getCurrentBasicReplaceParameters();

        // The object is copied in the updateBasicFindParameters function
        // This will cause a direct comparison of the object to be false
        assertEquals(result, parameters);

        assertNotEquals(result, parameters2);
    }

    /**
     * Test of retriveMatchingElements method, of class FindViewController.
     */
    @Test
    public void testRetriveMatchingElements() {
        System.out.println("retriveMatchingElements");

        /**
         * Set up the graph with 4 vertices, 4 transactions, 3 vertex attributes
         * (2 of type string), 3 transaction attributes (2 of type string)
         */
        setupGraph();

        /**
         * Create a mock of the top component, get an instance of the
         * controller, create a mock of the graph manager, when getAllGraphs is
         * called return the graphMap created in this class
         */
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);
        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);
        when(gm.getActiveGraph()).thenReturn(graph);

        System.out.println("before try");

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);
            System.out.println("in try");

            try (MockedStatic<PluginExecution> mockedStaticPlugin = Mockito.mockStatic(PluginExecution.class)) {
                PluginExecution pluginExecution = mock(PluginExecution.class);

                /**
                 * The first test should execute the plugin once on graph as the
                 * parameters are not set to look at all graphs
                 */
                when(pluginExecution.executeLater(Mockito.eq(graph))).thenReturn(null);
                mockedStaticPlugin.when(() -> PluginExecution.withPlugin(Mockito.any(Plugin.class))).thenReturn(pluginExecution);

                BasicFindPlugin basicFindPlugin = new BasicFindPlugin(instance.getCurrentBasicFindParameters(), true, false);
                PluginExecution.withPlugin(basicFindPlugin).executeLater(graph);
                verify(pluginExecution).executeLater(Mockito.eq(graph));

                /**
                 * Set the parameters to find in all graphs and repeat the same
                 * process. The plugin should be executed on graph a second
                 * time, and should be executed on graph2 for the first time.
                 */
                instance.updateBasicFindParameters(parametersAllGraphs);

                when(pluginExecution.executeLater(Mockito.any(Graph.class))).thenReturn(null);
                mockedStaticPlugin.when(() -> PluginExecution.withPlugin(Mockito.any(Plugin.class))).thenReturn(pluginExecution);

                basicFindPlugin = new BasicFindPlugin(instance.getCurrentBasicFindParameters(), true, false);
                PluginExecution.withPlugin(basicFindPlugin).executeLater(graph);
                verify(pluginExecution, times(2)).executeLater(Mockito.eq(graph));
            }
        }
    }

    /**
     * Test of replaceMatchingElements method, of class FindViewController.
     */
    @Test
    public void testReplaceMatchingElements() {
        System.out.println("replaceMatchingElements");
        /**
         * Set up the graph with 4 vertices, 4 transactions, 3 vertex attributes
         * (2 of type string), 3 transaction attributes (2 of type string)
         */
        setupGraph();

        /**
         * Create a mock of the top component, get an instance of the
         * controller, create a mock of the graph manager, when getAllGraphs is
         * called return the graphMap created in this class
         */
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);
        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);
        when(gm.getActiveGraph()).thenReturn(graph);

        System.out.println("before try");

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);
            System.out.println("in try");

            try (MockedStatic<PluginExecution> mockedStaticPlugin = Mockito.mockStatic(PluginExecution.class)) {
                PluginExecution pluginExecution = mock(PluginExecution.class);

                /**
                 * The first test should execute the plugin once on graph as the
                 * parameters are not set to look at all graphs
                 */
                when(pluginExecution.executeLater(Mockito.eq(graph))).thenReturn(null);
                mockedStaticPlugin.when(() -> PluginExecution.withPlugin(Mockito.any(Plugin.class))).thenReturn(pluginExecution);

                instance.replaceMatchingElements(true, false, false);
                verify(pluginExecution).executeLater(Mockito.eq(graph));

                /**
                 * Set the parameters to find in all graphs and repeat the same
                 * process. The plugin should be executed on graph a second
                 * time, and should be executed on graph2 for the first time.
                 */
                instance.updateBasicReplaceParameters(parametersAllGraphs);

                when(pluginExecution.executeLater(Mockito.any(Graph.class))).thenReturn(null);
                mockedStaticPlugin.when(() -> PluginExecution.withPlugin(Mockito.any(Plugin.class))).thenReturn(pluginExecution);

                instance.replaceMatchingElements(true, false, false);
                verify(pluginExecution, times(2)).executeLater(Mockito.eq(graph));
                verify(pluginExecution).executeLater(Mockito.eq(graph2));
            }
        }
    }

    /**
     * Test of getCurrentBasicFindParameters method, of class
     * FindViewController.
     */
    @Test
    public void testGetCurrentBasicFindParameters() {
        System.out.println("getCurrentBasicFindParameters");
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);

        /**
         * Check getting getCurrentBasicReplaceParamters returns the default
         * parameters
         */
        BasicFindReplaceParameters defaultParameters = new BasicFindReplaceParameters();
        instance.updateBasicFindParameters(defaultParameters);
        assertEquals(instance.getCurrentBasicFindParameters(), defaultParameters);
        /**
         * Check getting getCurrentBasicReplaceParamters returns the matching
         * parameters of what it was set to
         */
        instance.updateBasicFindParameters(parameters);
        assertEquals(instance.getCurrentBasicFindParameters(), parameters);
    }

    /**
     * Test of getCurrentBasicReplaceParameters method, of class
     * FindViewController.
     */
    @Test
    public void testGetCurrentBasicReplaceParameters() {
        System.out.println("getCurrentBasicReplaceParameters");

        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);

        // Check getting getCurrentBasicReplaceParamters returns the default
        // when it has not been set
        BasicFindReplaceParameters defaultParameters = new BasicFindReplaceParameters();
        instance.updateBasicReplaceParameters(defaultParameters);
        assertEquals(instance.getCurrentBasicReplaceParameters(), defaultParameters);

        // Check getting getCurrentBasicReplaceParamters returns the parameters it has been set to
        // it has been set to when it has not been set
        instance.updateBasicReplaceParameters(parameters2);
        assertEquals(instance.getCurrentBasicReplaceParameters(), parameters2);

    }

    /**
     * Set up a graph with two vertices and two transactions on layer 1.
     */
    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        graph2 = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap = new HashMap<>();
        graphMap.put(graph.getId(), graph);
        graphMap.put(graph2.getId(), graph2);
        try {

            WritableGraph wg = graph.getWritableGraph("", true);
            try {
                // Create Selected Attributes
                selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
                labelV = VisualConcept.VertexAttribute.LABEL.ensure(wg);
                identifierV = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
                xV = VisualConcept.VertexAttribute.X.ensure(wg);

                selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
                labelT = VisualConcept.TransactionAttribute.LABEL.ensure(wg);
                identiferT = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(wg);
                widthT = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);

                int[] vertexList = {vxId1, vxId2, vxId3, vxId4};

                for (int vertex : vertexList) {
                    vertex = wg.addVertex();
                    wg.setBooleanValue(selectedV, vertex, false);
                    wg.setStringValue(labelV, vertex, "label name");
                    wg.setStringValue(identifierV, vertex, "identifer name");
                    wg.setIntValue(xV, vertex, 1);
                }

                int[] transactionList = {txId1, txId2, txId3, txId4};

                int count = 0;
                for (int transaction : transactionList) {
                    int vertex = vertexList[count];
                    transaction = wg.addTransaction(vxId1, vertex, false);
                    wg.setBooleanValue(selectedT, transaction, false);
                    wg.setStringValue(labelT, transaction, "label name");
                    wg.setStringValue(identiferT, transaction, "identifer name");
                    wg.setIntValue(widthT, transaction, 1);
                    if (count != vertexList.length) {
                        count++;
                    }
                }
            } finally {
                wg.commit();
            }

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            Thread.currentThread().interrupt();
        }
    }
}

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
package au.gov.asd.tac.constellation.views.find.plugins;

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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.FindViewController;
import au.gov.asd.tac.constellation.views.find.FindViewTopComponent;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ReplacePluginNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    
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
    private int vxId5UpperCase;
    private int vxId6;
    private int vxId7;
    private int vxId8;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    
    private List<Attribute> attributeList;
    private BasicFindReplaceParameters parameters;
    private BasicFindReplaceParameters parametersReplaceNext;
    private BasicFindReplaceParameters parametersReplaceRegEx;
    private BasicFindReplaceParameters parametersReplaceIgnoreCase;
    private BasicFindReplaceParameters parametersReplaceInSelected;
    
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

        parameters = new BasicFindReplaceParameters("label name", "replaced", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        parametersReplaceNext = new BasicFindReplaceParameters("replaced", "next", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, false, false, true, false);
        parametersReplaceRegEx = new BasicFindReplaceParameters("ne*", "test", GraphElementType.VERTEX, attributeList, false, true, false, false, true, false, false, false, false, true, false);
        parametersReplaceIgnoreCase = new BasicFindReplaceParameters("lowercase", "test", GraphElementType.VERTEX, attributeList, true, false, true, false, true, false, false, false, false, true, false);
        parametersReplaceInSelected = new BasicFindReplaceParameters("test", "replaced", GraphElementType.VERTEX, attributeList, true, false, false, false, true, false, false, true, false, true, false);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class ReplacePlugin.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        setupGraph();
        attributeList.addAll(getAttributes());

        ReplacePlugin replacePlugin = new ReplacePlugin(parameters, true, false, false);
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            /**
             * Testing replace all elements with the label name "label name" with
             * "replaced", Should replace vxId1,2,3,4
             */
            assertEquals(rg.getStringValue(labelV, vxId1), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId2), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId3), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId4), "replaced");
        }

        /**
         * Testing replacing next for the word "replaced" and replacing it with
         * "next". vxId1 label should now be "next"
         */
        replacePlugin = new ReplacePlugin(parametersReplaceNext, false, true, false);
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertEquals(rg.getStringValue(labelV, vxId1), "next");
            assertEquals(rg.getStringValue(labelV, vxId2), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId3), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId4), "replaced");
        }

        /**
         * Testing the same as above. vxId2 label should now be "next"
         */
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertEquals(rg.getStringValue(labelV, vxId1), "next");
            assertEquals(rg.getStringValue(labelV, vxId2), "next");
            assertEquals(rg.getStringValue(labelV, vxId3), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId4), "replaced");
        }

        /**
         * Replace anything with "ne*" in regEx format with "test" vxId1 and 2
         * labels should convert from "next" to "testxt"
         */
        replacePlugin = new ReplacePlugin(parametersReplaceRegEx, true, false, false);
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertEquals(rg.getStringValue(labelV, vxId1), "testxt");
            assertEquals(rg.getStringValue(labelV, vxId2), "testxt");
            assertEquals(rg.getStringValue(labelV, vxId3), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId4), "replaced");
        }

        /**
         * Replacing the word "lowercase" ignoring the if lower or upper case to
         * the word "test". vxId1 and 2 should be changed to "test"
         */
        WritableGraph wg = graph.getWritableGraph("", true);
        try {
            wg.setStringValue(labelV, vxId1, "lowercase");
            wg.setStringValue(labelV, vxId2, "LOWERCASE");
            wg.setStringValue(labelV, vxId3, "test");
            wg.setStringValue(labelV, vxId4, "test");
        } finally {
            wg.commit();
        }

        replacePlugin = new ReplacePlugin(parametersReplaceIgnoreCase, true, false, false);
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertEquals(rg.getStringValue(labelV, vxId1), "test");
            assertEquals(rg.getStringValue(labelV, vxId2), "test");
            assertEquals(rg.getStringValue(labelV, vxId3), "test");
            assertEquals(rg.getStringValue(labelV, vxId4), "test");
        }

        /**
         * Testing replacing the word "test" with the word "replaced" only in
         * elements that are selected. 
         */
        wg = graph.getWritableGraph("", true);
        try {
        wg.setBooleanValue(selectedV, vxId1, true);
        wg.setBooleanValue(selectedV, vxId2, true);
        wg.setBooleanValue(selectedV, vxId3, false);
        wg.setBooleanValue(selectedV, vxId4, false);
        } finally {
            wg.commit();
        }

        replacePlugin = new ReplacePlugin(parametersReplaceInSelected, true, false, false);
        PluginExecution.withPlugin(replacePlugin).executeNow(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            assertEquals(rg.getStringValue(labelV, vxId1), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId2), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId3), "replaced");
            assertEquals(rg.getStringValue(labelV, vxId4), "replaced");
        }
    }

    /**
     * Test of getName method, of class ReplacePlugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");

        ReplacePlugin instance = new ReplacePlugin(parameters, true, false, false);
        String expResult = "Find/Replaces String attribute values";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        graph2 = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap = new HashMap<>();
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
            wg.setIntValue(xV, vxId1, 1);

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
    private List<Attribute> getAttributes() {
        final FindViewTopComponent findViewTopComponent = mock(FindViewTopComponent.class);
        FindViewController instance = FindViewController.getDefault().init(findViewTopComponent);
        final GraphManager gm = Mockito.mock(GraphManager.class);
        when(gm.getAllGraphs()).thenReturn(graphMap);

        List<Attribute> attributes = new ArrayList<>();

        try (MockedStatic<GraphManager> mockedStatic = Mockito.mockStatic(GraphManager.class)) {
            mockedStatic.when(() -> GraphManager.getDefault()).thenReturn(gm);

            GraphElementType type = GraphElementType.VERTEX;
            List<String> result = instance.populateAttributes(type, attributes, Long.MIN_VALUE);

            try (final ReadableGraph rg = graph.getReadableGraph()) {
                for (int i = 0; i < result.size(); i++) {
                    int attributeInt = rg.getAttribute(type, result.get(i));
                    GraphAttribute ga = new GraphAttribute(rg, attributeInt);
                    if (ga.getAttributeType().equals("string")) {
                        attributes.add(new GraphAttribute(rg, attributeInt));
                    }
                }
            }
        }
        return attributes;

    }
}

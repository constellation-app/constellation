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
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.mock;
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
public class GraphAttributePluginNGTest {

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
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    
    private List<Attribute> attributeList;
    
    private final long amc = Long.MIN_VALUE;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required

    }
    @BeforeMethod
    public void setUpMethod() {
        attributeList = new ArrayList<>();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of read method, of class GraphAttributePlugin.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testRead() throws InterruptedException {
        System.out.println("read");

        setupGraph();

        PluginInteraction interaction = mock(PluginInteraction.class);
        PluginParameters pluginParameters = mock(PluginParameters.class);

        WritableGraph wg = graph.getWritableGraph("", true);
        try {
            GraphElementType type = GraphElementType.VERTEX;

            GraphAttributePlugin graphAttributePlugin = new GraphAttributePlugin(type, attributeList, amc);

            int selectedInt = wg.getAttribute(type, 0);
            Attribute selectedAttribute = new GraphAttribute(wg, selectedInt);
            int labelInt = wg.getAttribute(type, 1);
            Attribute labelAttribute = new GraphAttribute(wg, labelInt);
            int identifierInt = wg.getAttribute(type, 2);
            Attribute identifierAttribute = new GraphAttribute(wg, identifierInt);
            int xInt = wg.getAttribute(type, 3);
            Attribute xAttribute = new GraphAttribute(wg, xInt);

            List<Attribute> expectedList = new ArrayList<>();
            expectedList.add(selectedAttribute);
            expectedList.add(labelAttribute);
            expectedList.add(identifierAttribute);
            expectedList.add(xAttribute);

            /**
             * The attributeList should contain all the avalible attributes of the
             * given type that exist on the graph. It should contain the selected,
             * label, identifier and x attributes as they are the vertex attributes
             * the graph has been set with.
             */
            graphAttributePlugin.read(wg, interaction, pluginParameters);
            assertEquals(attributeList, expectedList);
        } finally {
            wg.commit();
        }
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
}

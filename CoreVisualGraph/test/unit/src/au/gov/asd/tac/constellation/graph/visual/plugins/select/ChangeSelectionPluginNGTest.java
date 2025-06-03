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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.util.BitSet;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ChangeSelectionPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int tId1;
    private int tId2;
    private int tId3;
    
    private int selectedVertexAttribute;
    private int selectedTransactionAttribute;

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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        
        tId1 = graph.addTransaction(vxId1, vxId2, true);
        tId2 = graph.addTransaction(vxId2, vxId3, true);
        tId3 = graph.addTransaction(vxId3, vxId1, true);
        
        selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId3, true);
        graph.setBooleanValue(selectedTransactionAttribute, tId3, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of edit method, of class ChangeSelectionPlugin. Add transactions
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAddTransactions() throws InterruptedException, PluginException {
        System.out.println("editAddTransactions");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(tId2);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.ADD);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Add nodes
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditAddNodes() throws InterruptedException, PluginException {
        System.out.println("editAddNodes");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(vxId2);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.VERTEX));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.ADD);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Remove transactions
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditRemoveTransactions() throws InterruptedException, PluginException {
        System.out.println("editRemoveTransactions");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(tId3);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REMOVE);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Remove nodes
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditRemoveNodes() throws InterruptedException, PluginException {
        System.out.println("editRemoveNodes");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(vxId3);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.VERTEX));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REMOVE);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Replace transactions
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditReplaceTransactions() throws InterruptedException, PluginException {
        System.out.println("editReplaceTransactions");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(tId2);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Replace nodes
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditReplaceNodes() throws InterruptedException, PluginException {
        System.out.println("editReplaceNodes");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(vxId1);
        elementIds.set(vxId2);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.VERTEX));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Invert transactions
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditInvertTransactions() throws InterruptedException, PluginException {
        System.out.println("editInvertTransactions");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(tId1);
        elementIds.set(tId3);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.INVERT);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
    
    /**
     * Test of edit method, of class ChangeSelectionPlugin. Invert nodes
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEditInvertNodes() throws InterruptedException, PluginException {
        System.out.println("editInvertNodes");
        
        final ChangeSelectionPlugin instance = new ChangeSelectionPlugin();
        
        final PluginParameters parameters = instance.createParameters();
        final BitSet elementIds = new BitSet();
        elementIds.set(vxId1);
        elementIds.set(vxId2);
        parameters.setObjectValue(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds);
        final PluginParameter<SingleChoiceParameterValue> elementTypeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID);
        SingleChoiceParameterType.setChoiceData(elementTypeParam, new ElementTypeParameterValue(GraphElementType.VERTEX));
        parameters.setObjectValue(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.INVERT);
        
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
        
        instance.edit(graph, null, parameters);
        
        assertFalse(graph.getBooleanValue(selectedVertexAttribute, vxId1));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId2));
        assertTrue(graph.getBooleanValue(selectedVertexAttribute, vxId3));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId1));
        assertFalse(graph.getBooleanValue(selectedTransactionAttribute, tId2));
        assertTrue(graph.getBooleanValue(selectedTransactionAttribute, tId3));
    }
}

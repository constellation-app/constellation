/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Mockito;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class SelectOnGraphPluginNGTest {

    public SelectOnGraphPluginNGTest() {
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
     * Test of edit method, of class SelectOnGraphPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");

        boolean isSelectingVertex = true;

        final List<Integer> selectedNodeList = new ArrayList<>();

        selectedNodeList.add(3);
        selectedNodeList.add(7);

        final int vertexCount = 5;
        final int transactionCount = 8;
        final int vertexID = 3;
        final int transactionID = 7;


        GraphWriteMethods graph = Mockito.spy(new StoreGraph());
        Mockito.when(graph.getVertexCount()).thenReturn(vertexCount);
        Mockito.when(graph.getTransactionCount()).thenReturn(transactionCount);
        Mockito.when(graph.getVertex(Mockito.anyInt())).thenReturn(vertexID);
        Mockito.when(graph.getTransaction(Mockito.anyInt())).thenReturn(transactionID);

        final int vertexSelectID = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int transactionSelectID = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        Mockito.doNothing().when(graph).setBooleanValue(vertexSelectID, vertexID, selectedNodeList.contains(vertexID));
        Mockito.doNothing().when(graph).setBooleanValue(transactionSelectID, transactionID, selectedNodeList.contains(transactionID));

        final PluginInteraction interaction = Mockito.mock(PluginInteraction.class);
        final PluginParameters parameters = Mockito.mock(PluginParameters.class);

        SelectOnGraphPlugin instance = new SelectOnGraphPlugin(selectedNodeList, isSelectingVertex);
        instance.edit(graph, interaction, parameters);

        Mockito.verify(graph, Mockito.atMost(vertexCount)).setBooleanValue(vertexSelectID, vertexID, selectedNodeList.contains(vertexID));

        isSelectingVertex = false;

        instance = new SelectOnGraphPlugin(selectedNodeList, isSelectingVertex);
        instance.edit(graph, interaction, parameters);

        Mockito.verify(graph, Mockito.atMost(transactionCount)).setBooleanValue(transactionSelectID, transactionID, selectedNodeList.contains(transactionID));
    }

    /**
     * Test of getName method, of class SelectOnGraphPlugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        final SelectOnGraphPlugin instance = new SelectOnGraphPlugin();
        final String expResult = "SelectOnGraphPlugin2";
        final String result = instance.getName();
        assertEquals(result, expResult);
    }

}

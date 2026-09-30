/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 *
 * @author Quasar985
 */
public class NewArrangeInTreesPluginNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Left Intentially Blank
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Left Intentially Blank
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Left Intentially Blank
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Left Intentially Blank
    }

    /**
     * Test of createParameters method, of class NewArrangeInTreesPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        final NewArrangeInTreesPlugin instance = new NewArrangeInTreesPlugin();
        final PluginParameters result = instance.createParameters();

        assertNotNull(result);

        final Map<String, PluginParameter<?>> params = result.getParameters();

        assertEquals(params.size(), 4);
        assertTrue(params.containsKey(NewArrangeInTreesPlugin.SPLIT_INTO_TREES_PARAMETER_ID));
        assertTrue(params.containsKey(NewArrangeInTreesPlugin.DIM_OR_HIDE_PARAMETER_ID));
        assertTrue(params.containsKey(NewArrangeInTreesPlugin.COLOUR_SUBGRAPHS_PARAMETER_ID));
        assertTrue(params.containsKey(NewArrangeInTreesPlugin.SCALE_PARAMETER_ID));
    }

    /**
     * Test of edit method, of class NewArrangeInTreesPlugin.
     */
    @Test
    public void testEditEarlyReturn() throws Exception {
        System.out.println("edit early return");
        final GraphWriteMethods graph = mock(StoreGraph.class);
        when(graph.getVertexCount()).thenReturn(0);

        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);

        final NewArrangeInTreesPlugin instance = new NewArrangeInTreesPlugin();
        instance.edit(graph, interaction, parameters);

        verify(interaction).setProgress(0, 0, "Arranging...", true);
        verify(interaction).setProgress(1, 0, "Finished", true);
        verify(graph).getVertexCount();
        verify(parameters, never()).getParameters();
    }

    /**
     * Test of edit method, of class NewArrangeInTreesPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");
        // Setup graph and mocks
        final GraphWriteMethods graph = spy(createTestGraph());
        final int bgiconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int colorAttr = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int transactionDimmedAttribute = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);

        final PluginInteraction interaction = mock(PluginInteraction.class);

        // Mock parameters
        final PluginParameters parameters = mock(PluginParameters.class);
        final Map<String, PluginParameter<?>> mockParams = mock(Map.class);
        final PluginParameter splitParam = mock(PluginParameter.class);
        final PluginParameter dimParam = mock(PluginParameter.class);
        final PluginParameter colorParam = mock(PluginParameter.class);
        final PluginParameter scaleParam = mock(PluginParameter.class);

        when(parameters.getParameters()).thenReturn(mockParams);

        when(mockParams.get(NewArrangeInTreesPlugin.SPLIT_INTO_TREES_PARAMETER_ID)).thenReturn(splitParam);
        when(mockParams.get(NewArrangeInTreesPlugin.DIM_OR_HIDE_PARAMETER_ID)).thenReturn(dimParam);
        when(mockParams.get(NewArrangeInTreesPlugin.COLOUR_SUBGRAPHS_PARAMETER_ID)).thenReturn(colorParam);
        when(mockParams.get(NewArrangeInTreesPlugin.SCALE_PARAMETER_ID)).thenReturn(scaleParam);

        when(splitParam.getBooleanValue()).thenReturn(true);
        when(dimParam.getStringValue()).thenReturn("Dim");
        when(colorParam.getBooleanValue()).thenReturn(true);
        when(scaleParam.getIntegerValue()).thenReturn(10);

        final GraphTaxonomy mockTreeTax = mock(GraphTaxonomy.class);
        final MutableIntObjectMap<MutableIntSet> taxa = createExpectedTaxa();
        when(mockTreeTax.getTaxa()).thenReturn(taxa);

        try (final MockedStatic<TreeTaxonArranger> mockedTreeTaxonArranger = Mockito.mockStatic(TreeTaxonArranger.class, Mockito.CALLS_REAL_METHODS)) {
            // Setup mock taxa
            mockedTreeTaxonArranger.when(() -> TreeTaxonArranger.getTreeTaxonomy(graph, true)).thenReturn(mockTreeTax);

            // Run edit function
            final NewArrangeInTreesPlugin instance = new NewArrangeInTreesPlugin();
            instance.edit(graph, interaction, parameters);

            // Verify certain functions were called as they call getTaxa()
            verify(mockTreeTax, atLeast(2)).getTaxa();
        }

        // Verify plugin finished
        verify(interaction).setProgress(0, 0, "Arranging...", true);
        verify(interaction).setProgress(1, 0, "Finished", true);

        // Verify params were gotten
        verify(parameters, times(4)).getParameters();

        //Verify particular transactions were dimmed
        final int[] expectedDimmedTransIds = {0, 1, 12};
        // Create an array of all dimmed transactions and compare to expected values (done this way to help debug if test fails)
        final MutableIntList dimmedTrans = new IntArrayList();
        for (int i = 0; i < graph.getTransactionCount(); i++) {
            final int txId = graph.getTransaction(i);
            if (graph.getBooleanValue(transactionDimmedAttribute, txId)) {
                dimmedTrans.add(txId);
            }
        }
        final int[] dimmedTransArray = dimmedTrans.toArray();
        Arrays.sort(expectedDimmedTransIds);
        Arrays.sort(dimmedTransArray);
        assertArrayEquals(expectedDimmedTransIds, dimmedTransArray);

        // Verify that each node in a subgraph has the same color, and that the background was set
        taxa.forEachValue(subgraph -> {
            final ConstellationColor expectedColor = graph.getObjectValue(colorAttr, subgraph.intIterator().next());
            subgraph.forEach(vxId -> {
                assertEquals(graph.getStringValue(bgiconAttr, vxId), "Background.Round Circle");
                assertEquals(graph.getObjectValue(colorAttr, vxId), expectedColor);
            });
        });
    }

    private MutableIntObjectMap<MutableIntSet> createExpectedTaxa() {
        final MutableIntObjectMap<MutableIntSet> taxa = new IntObjectHashMap<>();

        taxa.put(0, new IntHashSet(0, 3, 10, 11, 12));
        taxa.put(1, new IntHashSet(1, 4, 5, 6));
        taxa.put(2, new IntHashSet(2, 7, 8, 9));

        return taxa;
    }

    private StoreGraph createTestGraph() {
        final StoreGraph storeGraph = new StoreGraph();

        final int vx0 = storeGraph.addVertex();

        final int vx1 = storeGraph.addVertex();
        final int vx2 = storeGraph.addVertex();
        final int vx3 = storeGraph.addVertex();

        final int vx4 = storeGraph.addVertex();
        final int vx5 = storeGraph.addVertex();
        final int vx6 = storeGraph.addVertex();

        final int vx7 = storeGraph.addVertex();
        final int vx8 = storeGraph.addVertex();
        final int vx9 = storeGraph.addVertex();

        final int vx10 = storeGraph.addVertex();
        final int vx11 = storeGraph.addVertex();
        final int vx12 = storeGraph.addVertex();

        storeGraph.addTransaction(vx0, vx1, true);
        storeGraph.addTransaction(vx0, vx2, true);
        storeGraph.addTransaction(vx0, vx3, true);

        storeGraph.addTransaction(vx1, vx4, true);
        storeGraph.addTransaction(vx1, vx5, true);
        storeGraph.addTransaction(vx1, vx6, true);

        storeGraph.addTransaction(vx2, vx7, true);
        storeGraph.addTransaction(vx2, vx8, true);
        storeGraph.addTransaction(vx2, vx9, true);

        storeGraph.addTransaction(vx3, vx10, true);
        storeGraph.addTransaction(vx3, vx11, true);
        storeGraph.addTransaction(vx3, vx12, true);

        // Adding this extra transaction will make the graph have three sub graphs
        storeGraph.addTransaction(vx1, vx2, true);

        VisualConcept.VertexAttribute.SELECTED.ensure(storeGraph);

        return storeGraph;
    }

}

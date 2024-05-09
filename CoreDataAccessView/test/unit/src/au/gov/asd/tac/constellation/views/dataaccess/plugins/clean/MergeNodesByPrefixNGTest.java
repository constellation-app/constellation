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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.LEAD_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.LONGEST_VERTEX_CHOOSER;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGER_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.SELECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.SHORTEST_VERTEX_CHOOSER;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.THRESHOLD_PARAMETER_ID;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for MergeNodesByPrefix.
 *
 * @author sol695510
 */
public class MergeNodesByPrefixNGTest {

    private Schema schema;
    private StoreGraph graph;
    private int vxId1, vxId2, vxId3, vxId4, vxId5;
    private int vertexIdentifierAttribute, vertexSelectedAttribute;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Create graph.
        schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // Add attributes.
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // Add vertices.
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();

        // Set vertice identifiers.
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "pre v1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "pre v2");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "prefix v3");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "fix v4");
        graph.setStringValue(vertexIdentifierAttribute, vxId5, "fix v5");

        // Set vertice selection.
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, false);
    }

    /**
     * Test of updateParameters method.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("testUpdateParameters");

        final PluginParameters pluginParameters = new PluginParameters();

        pluginParameters.addParameter(BooleanParameterType.build(MERGE_TYPE_PARAMETER_ID));
        pluginParameters.addParameter(IntegerParameterType.build(THRESHOLD_PARAMETER_ID));
        pluginParameters.addParameter(BooleanParameterType.build(MERGER_PARAMETER_ID));
        pluginParameters.addParameter(BooleanParameterType.build(LEAD_PARAMETER_ID));
        pluginParameters.addParameter(BooleanParameterType.build(SELECTED_PARAMETER_ID));

        final Map<String, PluginParameter<?>> parameters = pluginParameters.getParameters();

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        instance.updateParameters(parameters);

        assertTrue(parameters.get(MERGE_TYPE_PARAMETER_ID).isEnabled());
        assertEquals(parameters.get(THRESHOLD_PARAMETER_ID).getDescription(), "The prefix length to consider");
        assertEquals(parameters.get(THRESHOLD_PARAMETER_ID).getIntegerValue(), 9);
        assertTrue(parameters.get(THRESHOLD_PARAMETER_ID).isEnabled());
        assertTrue(parameters.get(MERGER_PARAMETER_ID).isEnabled());
        assertTrue(parameters.get(LEAD_PARAMETER_ID).isEnabled());
        assertTrue(parameters.get(SELECTED_PARAMETER_ID).isEnabled());
    }

    /**
     * Test of getNodesToMerge method with shortestLeadVertexChooser and
     * threshold = 0 and a graph with no vertices with an indentifierAttribute.
     */
    @Test
    public void testGetNodesToMerge_shortestLeadVertexChooser_threshold0_NoIdentifierAttribute() {
        System.out.println("testGetNodesToMerge_shortestLeadVertexChooser_threshold0");

        final Comparator<String> leadVertexChooser = SHORTEST_VERTEX_CHOOSER;
        final int threshold = 0;
        final boolean selectedOnly = false;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final StoreGraph graph_NoIdentierAttribute = new StoreGraph(schema);
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph_NoIdentierAttribute, leadVertexChooser, threshold, selectedOnly);

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();

        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method with shortestLeadVertexChooser and
     * threshold = 0.
     */
    @Test
    public void testGetNodesToMerge_shortestLeadVertexChooser_threshold0() {
        System.out.println("testGetNodesToMerge_shortestLeadVertexChooser_threshold0");

        final Comparator<String> leadVertexChooser = SHORTEST_VERTEX_CHOOSER;
        final int threshold = 0;
        final boolean selectedOnly = false;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);

        final Set<Integer> cluster = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();
        expResult.put(3, cluster);

        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method with shortestLeadVertexChooser and
     * threshold = 3.
     */
    @Test
    public void testGetNodesToMerge_shortestLeadVertexChooser_threshold3() {
        System.out.println("testGetNodesToMerge_shortestLeadVertexChooser_threshold0");

        final Comparator<String> leadVertexChooser = SHORTEST_VERTEX_CHOOSER;
        final int threshold = 3;
        final boolean selectedOnly = false;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);

        final Set<Integer> cluster = new HashSet<>(Arrays.asList(0, 1, 2));
        final Set<Integer> cluster2 = new HashSet<>(Arrays.asList(3, 4));

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();
        expResult.put(0, cluster);
        expResult.put(3, cluster2);

        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method with longestLeadVertexChooser and
     * threshold = 5.
     */
    @Test
    public void testGetNodesToMerge_longestLeadVertexChooser_threshold5() {
        System.out.println("testGetNodesToMerge_longestLeadVertexChooser_threshold0");

        final Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        final int threshold = 5;
        final boolean selectedOnly = false;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);

        final Set<Integer> cluster = new HashSet<>(Arrays.asList(0, 1));
        final Set<Integer> cluster2 = new HashSet<>(Arrays.asList(3, 4));

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();
        expResult.put(0, cluster);
        expResult.put(3, cluster2);

        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method with longestLeadVertexChooser and
     * threshold = 5 and selectOnly = true.
     */
    @Test
    public void testGetNodesToMerge_longestLeadVertexChooser_threshold5_selectOnlyTrue() {
        System.out.println("testGetNodesToMerge_longestLeadVertexChooser_threshold5_selectOnlyTrue");

        final Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        final int threshold = 5;
        final boolean selectedOnly = true;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);

        final Set<Integer> cluster = new HashSet<>(Arrays.asList(0, 1));

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();
        expResult.put(0, cluster);

        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method with longestLeadVertexChooser and
     * threshold = 7 and selectOnly = true.
     */
    @Test
    public void testGetNodesToMerge_longestLeadVertexChooser_threshold7_selectOnlyTrue() {
        System.out.println("testGetNodesToMerge_longestLeadVertexChooser_threshold5_selectOnlyTrue");

        final Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        final int threshold = 7;
        final boolean selectedOnly = true;

        final MergeNodesByPrefix instance = new MergeNodesByPrefix();
        final Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);

        final Map<Integer, Set<Integer>> expResult = new HashMap<>();

        assertEquals(result, expResult);
    }
}

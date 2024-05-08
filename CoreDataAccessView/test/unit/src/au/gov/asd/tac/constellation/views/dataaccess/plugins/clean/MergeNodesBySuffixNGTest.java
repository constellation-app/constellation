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
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class MergeNodesBySuffixNGTest {

    private int vertexIdentifierAttribute, vertexSelectedAttribute;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6;
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        vxId6 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "d1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "One Id1");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "Id2");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "One Id2");
        graph.setStringValue(vertexIdentifierAttribute, vxId5, "Pine Id2");
        graph.setStringValue(vertexIdentifierAttribute, vxId6, "Id3");

        // select all
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId6, true);
    }

    /**
     * Test of updateParameters method, of class MergeNodesBySuffix.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("testUpdateParameters");

        final MergeNodesBySuffix instance = new MergeNodesBySuffix();

        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterType.BooleanParameterValue> mergeTypeParameter = BooleanParameterType.build(MERGE_TYPE_PARAMETER_ID);
        parameters.addParameter(mergeTypeParameter);

        final PluginParameter<IntegerParameterType.IntegerParameterValue> thresholdParameter = IntegerParameterType.build(THRESHOLD_PARAMETER_ID);
        parameters.addParameter(thresholdParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> mergeParameter = BooleanParameterType.build(MERGER_PARAMETER_ID);
        parameters.addParameter(mergeParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> leadParameter = BooleanParameterType.build(LEAD_PARAMETER_ID);
        parameters.addParameter(leadParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> selectedParameter = BooleanParameterType.build(SELECTED_PARAMETER_ID);
        parameters.addParameter(selectedParameter);

        final Map<String, PluginParameter<?>> parametersMap = parameters.getParameters();

        instance.updateParameters(parametersMap);

        assertEquals(parametersMap.size(), 5);
        assertTrue(parametersMap.get(MERGE_TYPE_PARAMETER_ID).isEnabled());
        assertTrue(parametersMap.get(THRESHOLD_PARAMETER_ID).isEnabled());
        assertEquals(parametersMap.get(THRESHOLD_PARAMETER_ID).getDescription(), "The suffix length to consider");
        assertEquals(parametersMap.get(THRESHOLD_PARAMETER_ID).getIntegerValue(), 9);
        assertTrue(parametersMap.get(MERGER_PARAMETER_ID).isEnabled());
        assertTrue(parametersMap.get(LEAD_PARAMETER_ID).isEnabled());
        assertTrue(parametersMap.get(SELECTED_PARAMETER_ID).isEnabled());
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesBySuffix, with no
     * identifier Attribute
     */
    @Test
    public void testGetNodesToMerge_NoIdentifierAttribute() throws Exception {
        System.out.println("testGetNodesToMerge_NoIdentifierAttribute");
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesBySuffix instance = new MergeNodesBySuffix();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();

        // create a new analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        StoreGraph graph = new StoreGraph(schema);

        // add vertices
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesBySuffix, with a
     * Longest LeadVertexChooser and Threshold = 0
     */
    @Test
    public void testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold0() {
        System.out.println("testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold0");

        Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesBySuffix instance = new MergeNodesBySuffix();

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(0, 1, 2, 3, 4, 5));
        expResult.put(4, cluster);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesBySuffix, with Longest
     * LeadVertexChooser and SelectedOnly = false and Threshold = 2
     */
    @Test
    public void testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold2() {
        System.out.println("testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold2");

        Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        int threshold = 2;
        boolean selectedOnly = false;
        MergeNodesBySuffix instance = new MergeNodesBySuffix();

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(0, 1));
        expResult.put(1, cluster);

        Set<Integer> cluster2 = new HashSet<>();
        cluster2.addAll(Arrays.asList(2, 3, 4));
        expResult.put(4, cluster2);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesBySuffix, with valid
     * LeadVertexChooser
     */
    @Test
    public void testGetNodesToMerge_WithShortestLeadVertexChooser_And_Threshold2() {
        System.out.println("testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold2");

        Comparator<String> leadVertexChooser = SHORTEST_VERTEX_CHOOSER;
        int threshold = 2;
        boolean selectedOnly = false;
        MergeNodesBySuffix instance = new MergeNodesBySuffix();

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(0, 1));
        expResult.put(0, cluster);

        Set<Integer> cluster2 = new HashSet<>();
        cluster2.addAll(Arrays.asList(2, 3, 4));
        expResult.put(2, cluster2);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesBySuffix, with Longest
     * LeadVertexChooser and SelectedOnly=true
     */
    @Test
    public void testGetNodesToMerge_WithLongestLeadVertexChooser_And_SelectedOnly() {
        System.out.println("testGetNodesToMerge_WithLongestLeadVertexChooser_And_Threshold2");

        Comparator<String> leadVertexChooser = LONGEST_VERTEX_CHOOSER;
        int threshold = 2;
        boolean selectedOnly = true;
        MergeNodesBySuffix instance = new MergeNodesBySuffix();

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(0, 1));
        expResult.put(1, cluster);

        // deselect some nodes
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId6, true);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }
}

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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.LEAD_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGER_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.SELECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.THRESHOLD_PARAMETER_ID;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class MergeNodesByTypeNGTest {

    private int vertexIdentifierAttribute;
    private int vertexTypeAttribute;
    private int vertexSelectedAttribute;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        vxId6 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "V2");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "V2");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "V3");
        graph.setStringValue(vertexIdentifierAttribute, vxId5, "V3");

        // select all
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);
    }

    /**
     * Test of updateParameters method, of class MergeNodesByType.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("testUpdateParameters");

        final MergeNodesByType instance = new MergeNodesByType();

        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterType.BooleanParameterValue> mergeTypeParameter = BooleanParameterType.build(MERGE_TYPE_PARAMETER_ID);
        mergeTypeParameter.setBooleanValue(false);
        parameters.addParameter(mergeTypeParameter);

        final PluginParameter<IntegerParameterType.IntegerParameterValue> thresholdParameter = IntegerParameterType.build(THRESHOLD_PARAMETER_ID);
        thresholdParameter.setIntegerValue(0);
        parameters.addParameter(thresholdParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> mergeParameter = BooleanParameterType.build(MERGER_PARAMETER_ID);
        mergeParameter.setBooleanValue(false);
        parameters.addParameter(mergeParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> leadParameter = BooleanParameterType.build(LEAD_PARAMETER_ID);
        leadParameter.setBooleanValue(false);
        parameters.addParameter(leadParameter);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> selectedParameter = BooleanParameterType.build(SELECTED_PARAMETER_ID);
        selectedParameter.setBooleanValue(false);
        parameters.addParameter(selectedParameter);

        final Map<String, PluginParameter<?>> parametersMap = parameters.getParameters();

        instance.updateParameters(parametersMap);

        assertEquals(parametersMap.size(), 5);
        assertTrue(parametersMap.get(MERGE_TYPE_PARAMETER_ID).isEnabled());
        assertFalse(parametersMap.get(THRESHOLD_PARAMETER_ID).isEnabled());
        assertTrue(parametersMap.get(MERGER_PARAMETER_ID).isEnabled());
        assertFalse(parametersMap.get(LEAD_PARAMETER_ID).isEnabled());
        assertTrue(parametersMap.get(SELECTED_PARAMETER_ID).isEnabled());
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByType, with no
     * identifier Attribute
     */
    @Test
    public void testGetNodesToMerge_NoIdentifierAttribute() {
        System.out.println("testGetNodesToMerge_NoTypes");
        
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesByType instance = new MergeNodesByType();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();

        // create a new analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        StoreGraph graph2 = new StoreGraph(schema);

        // add attributes
        int vertexTypeAttribute2 = AnalyticConcept.VertexAttribute.TYPE.ensure(graph2);

        // add vertices
        int vxId21 = graph2.addVertex();
        int vxId22 = graph2.addVertex();
        int vxId23 = graph2.addVertex();

        // set the identifier of each vertex to something unique
        graph2.setStringValue(vertexTypeAttribute2, vxId21, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph2.setStringValue(vertexTypeAttribute2, vxId22, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph2.setStringValue(vertexTypeAttribute2, vxId23, AnalyticConcept.VertexType.ONLINE_IDENTIFIER.getName());

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph2, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByType, with no types
     * Attribute
     */
    @Test
    public void testGetNodesToMerge_NoTypeAttribute() {
        System.out.println("testGetNodesToMerge_NoTypes");
        
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesByType instance = new MergeNodesByType();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();

        // create a new analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        StoreGraph graph2 = new StoreGraph(schema);

        // add attributes
        int vertexIdentifierAttribute2 = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph2);

        // add vertices
        int vxId21 = graph2.addVertex();
        int vxId22 = graph2.addVertex();
        int vxId23 = graph2.addVertex();

        // set the identifier of each vertex to something unique
        graph2.setStringValue(vertexIdentifierAttribute2, vxId21, "V1");
        graph2.setStringValue(vertexIdentifierAttribute2, vxId22, "V2");
        graph2.setStringValue(vertexIdentifierAttribute2, vxId23, "V2");

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph2, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByType, with no types
     */
    @Test
    public void testGetNodesToMerge_NoTypes() {
        System.out.println("testGetNodesToMerge_NoTypes");
        
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesByType instance = new MergeNodesByType();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByType, with types
     */
    @Test
    public void testGetNodesToMerge_WithTypes() {
        System.out.println("getNodesToMerge");
        
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = false;
        MergeNodesByType instance = new MergeNodesByType();

        // set the vertex Type Attribute of each vertex
        graph.setStringValue(vertexTypeAttribute, vxId1, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph.setStringValue(vertexTypeAttribute, vxId2, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph.setStringValue(vertexTypeAttribute, vxId3, AnalyticConcept.VertexType.ONLINE_IDENTIFIER.getName());
        graph.setStringValue(vertexTypeAttribute, vxId4, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph.setStringValue(vertexTypeAttribute, vxId5, AnalyticConcept.VertexType.DOCUMENT.getName());
        graph.setStringValue(vertexTypeAttribute, vxId6, AnalyticConcept.VertexType.DOCUMENT.getName());

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(1, 2));
        expResult.put(1, cluster);
        Set<Integer> cluster2 = new HashSet<>();
        cluster2.addAll(Arrays.asList(3, 4));
        expResult.put(3, cluster2);
        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByType, with types
     */
    @Test
    public void testGetNodesToMerge_selectedOnly() {
        System.out.println("getNodesToMerge");
        
        Comparator<String> leadVertexChooser = null;
        int threshold = 0;
        boolean selectedOnly = true;
        MergeNodesByType instance = new MergeNodesByType();

        // set the vertex Type Attribute of each vertex
        graph.setStringValue(vertexTypeAttribute, vxId1, AnalyticConcept.VertexType.ONLINE_IDENTIFIER.getName());
        graph.setStringValue(vertexTypeAttribute, vxId2, AnalyticConcept.VertexType.PERSON.getName());
        graph.setStringValue(vertexTypeAttribute, vxId3, AnalyticConcept.VertexType.PERSON.getName());
        graph.setStringValue(vertexTypeAttribute, vxId4, AnalyticConcept.VertexType.PERSON.getName());
        graph.setStringValue(vertexTypeAttribute, vxId5, AnalyticConcept.VertexType.PERSON.getName());
        graph.setStringValue(vertexTypeAttribute, vxId6, AnalyticConcept.VertexType.ONLINE_IDENTIFIER.getName());

        // select some nodes
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId6, false);

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(3, 4));
        expResult.put(3, cluster);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }
}

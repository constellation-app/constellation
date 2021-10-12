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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class MergeNodesByLocationNGTest {

    private int vertexIdentifierAttribute, vertexTypeAttribute, vertexLatitudeAttribute, vertexLongitudeAttribute, vertexSelectedAttribute;
    private int vxId1, vxId2, vxId3, vxId4, vxId5;
    private int txId1, txId2, txId3;
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        vertexLatitudeAttribute = SpatialConcept.VertexAttribute.LATITUDE.ensure(graph);
        vertexLongitudeAttribute = SpatialConcept.VertexAttribute.LONGITUDE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "V2");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "V3");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "V4");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "V5");

        // set the Latitude of each vertex to something unique
        graph.setStringValue(vertexLatitudeAttribute, vxId1, "15");
        graph.setStringValue(vertexLatitudeAttribute, vxId2, "25");
        graph.setStringValue(vertexLatitudeAttribute, vxId3, "37");
        graph.setStringValue(vertexLatitudeAttribute, vxId4, "2600");
        graph.setStringValue(vertexLatitudeAttribute, vxId5, "3800");

        // set the Longitude of each vertex to something unique
        graph.setStringValue(vertexLongitudeAttribute, vxId1, "35");
        graph.setStringValue(vertexLongitudeAttribute, vxId2, "45");
        graph.setStringValue(vertexLongitudeAttribute, vxId3, "56");
        graph.setStringValue(vertexLongitudeAttribute, vxId4, "4600");
        graph.setStringValue(vertexLongitudeAttribute, vxId5, "5700");

        // add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId3, vxId4, false);
        txId3 = graph.addTransaction(vxId5, vxId3, false);

        // select all
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByLocation, with
     * missing LatitudeAttribute
     */
    @Test
    public void testGetNodesToMerge_NoLatitudeAttribute() throws Exception {
        System.out.println("testGetNodesToMerge_NoLatitudeAttribute");
        Comparator<String> leadVertexChooser = null;
        int threshold = 1600000;
        boolean selectedOnly = false;
        MergeNodesByLocation instance = new MergeNodesByLocation();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();

        // create a new analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        StoreGraph graph = new StoreGraph(schema);

        // add attributes
        int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);

        // add vertices
        int vxId1 = graph.addVertex();
        int vxId2 = graph.addVertex();
        int vxId3 = graph.addVertex();

        // set the identifier of each vertex to something unique
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "V1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "V2");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "V3");

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByLocation, with
     * selectedOnly = False
     */
    @Test
    public void testGetNodesToMergee_Threshold_1600000_selectedOnlyFalse() throws Exception {
        System.out.println("testGetNodesToMergee_Threshold_1600000_selectedOnlyFalse_2");
        Comparator<String> leadVertexChooser = null;
        int threshold = 1600000;
        boolean selectedOnly = false;
        MergeNodesByLocation instance = new MergeNodesByLocation();

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(new Integer[]{0, 1}));
        expResult.put(5, cluster);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByLocation, with
     * selectedOnly = false and threshold = 10
     */
    @Test
    public void testGetNodesToMerge_Threshold_10() throws Exception {
        System.out.println("testGetNodesToMerge_Threshold_10");
        Comparator<String> leadVertexChooser = null;
        int threshold = 10;
        boolean selectedOnly = false;
        MergeNodesByLocation instance = new MergeNodesByLocation();
        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByLocation, with
     * selectedOnly = True and all Nodes selected
     */
    @Test
    public void testGetNodesToMergee_selectedOnlyTrue_AllNodesSelected() throws Exception {
        System.out.println("testGetNodesToMergee_selectedOnlyTrue_AllNodesSelected");
        Comparator<String> leadVertexChooser = null;
        int threshold = 1600000;
        boolean selectedOnly = true;
        MergeNodesByLocation instance = new MergeNodesByLocation();

        // select all vertices
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(new Integer[]{0, 1}));
        expResult.put(5, cluster);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }

    /**
     * Test of getNodesToMerge method, of class MergeNodesByLocation, with
     * selectedOnly = True and some Nodes selected
     */
    @Test
    public void testGetNodesToMergee_selectedOnlyTrue_SomeNodesSelected() throws Exception {
        System.out.println("testGetNodesToMergee_selectedOnlyTrue_SomeNodesSelected");
        Comparator<String> leadVertexChooser = null;
        int threshold = 1590000;
        boolean selectedOnly = true;
        MergeNodesByLocation instance = new MergeNodesByLocation();

        // set the Latitude of each vertex to something unique
        graph.setStringValue(vertexLatitudeAttribute, vxId1, "15");
        graph.setStringValue(vertexLatitudeAttribute, vxId2, "25");
        graph.setStringValue(vertexLatitudeAttribute, vxId3, "37");
        graph.setStringValue(vertexLatitudeAttribute, vxId4, "26");
        graph.setStringValue(vertexLatitudeAttribute, vxId5, "38");

        // set the Longitude of each vertex to something unique
        graph.setStringValue(vertexLongitudeAttribute, vxId1, "35");
        graph.setStringValue(vertexLongitudeAttribute, vxId2, "45");
        graph.setStringValue(vertexLongitudeAttribute, vxId3, "56");
        graph.setStringValue(vertexLongitudeAttribute, vxId4, "46");
        graph.setStringValue(vertexLongitudeAttribute, vxId5, "57");

        // select some nodes
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, false);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);

        Map<Integer, Set<Integer>> expResult = new HashMap<>();
        Set<Integer> cluster = new HashSet<>();
        cluster.addAll(Arrays.asList(new Integer[]{2, 4}));
        expResult.put(5, cluster);

        Map<Integer, Set<Integer>> result = instance.getNodesToMerge(graph, leadVertexChooser, threshold, selectedOnly);
        assertEquals(result, expResult);
    }
}

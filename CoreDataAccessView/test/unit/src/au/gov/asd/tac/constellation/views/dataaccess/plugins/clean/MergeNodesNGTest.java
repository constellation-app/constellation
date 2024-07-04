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
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Merge Nodes Test.
 *
 * @author cygnus_x-1
 */
public class MergeNodesNGTest {

    private int vertexIdentifierAttribute, vertexTypeAttribute, vertexLatitudeAttribute, vertexLongitudeAttribute, vertexShapeAttribute, vertexSelectedAttribute;
    private int vxId1, vxId2, vxId3, vxId4;
    private StoreGraph graph;
    private final boolean SAVE_GRAPH_FILES = false; // change this to true if you want to see the graph files

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
        vertexShapeAttribute = SpatialConcept.VertexAttribute.SHAPE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // set the identifier of three vertices to somthing unique but similar, and the remaining vertex to a duplicate
        graph.setStringValue(vertexIdentifierAttribute, vxId1, "VERTEX_1");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "VERTEX_2");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, "SPECIAL_VERTEX_1");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "VERTEX_1");

        // set the type of two vertices to a schema type, and the remaining two vertices to a non-schema type
        graph.setStringValue(vertexTypeAttribute, vxId1, "Online Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId2, "Online Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId3, "Special Identifier");
        graph.setStringValue(vertexTypeAttribute, vxId4, "Special Identifier");

        // set the latitude and longitude of each pair of vertices to be geospatially close
        graph.setFloatValue(vertexLatitudeAttribute, vxId1, 25.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId1, 25.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId2, 26.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId2, 26.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId3, -25.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId3, -25.0f);
        graph.setFloatValue(vertexLatitudeAttribute, vxId4, -30.0f);
        graph.setFloatValue(vertexLongitudeAttribute, vxId4, -30.0f);

        // set all vertices to be selected
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @Test
    public void testGetType() {
        MergeNodesPlugin instance = new MergeNodesPlugin();
        String expResult = DataAccessPluginCoreType.CLEAN;
        String result = instance.getType();
        assertEquals(result, expResult);
    }

    private void saveGraphToFile(final String filename) throws InterruptedException, IOException {
        if (SAVE_GRAPH_FILES) {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, filename);
            System.out.println("Saved graph to " + System.getProperty("java.io.tmpdir") + filename);
        }
    }

    @Test
    public void testMergeNodesByPrefix() throws Exception {
//        MergeNodesPlugin instance = new MergeNodesPlugin();
//
//        saveGraphToFile("testMergeNodesByPrefix-before");
//
//        PluginParameters parameters = instance.createParameters();
//        parameters.setStringValue(MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID, "Identifier Prefix Length");
//        parameters.setStringValue(MergeNodesPlugin.THRESHOLD_PARAMETER_ID, "7");
//        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);
//
//        saveGraphToFile("testMergeNodesByPrefix-after");
//
//        assertEquals(graph.getVertexCount(), 2);
    }

    @Test
    public void testMergeNodesBySuffix() throws Exception {
//        MergeNodesPlugin instance = new MergeNodesPlugin();
//
//        saveGraphToFile("testMergeNodesBySuffix-before");
//
//        PluginParameters parameters = instance.createParameters();
//        parameters.setStringValue(MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID, "Identifier Suffix Length");
//        parameters.setStringValue(MergeNodesPlugin.THRESHOLD_PARAMETER_ID, "1");
//        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);
//
//        saveGraphToFile("testMergeNodesBySuffix-after");
//
//        assertEquals(graph.getVertexCount(), 2);
    }

    @Test
    public void testMergeNodesByType() throws Exception {
//        MergeNodesPlugin instance = new MergeNodesPlugin();
//
//        saveGraphToFile("testMergeNodesByType-before");
//
//        PluginParameters parameters = instance.createParameters();
//        parameters.setStringValue(MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID, "Supported Type");
//        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);
//
//        saveGraphToFile("testMergeNodesByType-after");
//
//        assertEquals(graph.getVertexCount(), 3);
    }

    @Test
    public void testMergeNodesByLocation() throws Exception {
//        MergeNodesPlugin instance = new MergeNodesPlugin();
//
//        saveGraphToFile("testMergeNodesByLocation-before");
//
//        PluginParameters parameters = instance.createParameters();
//        parameters.setStringValue(MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID, "Geospatial Distance");
//        parameters.setStringValue(MergeNodesPlugin.THRESHOLD_PARAMETER_ID, "200000");
//        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);
//
//        saveGraphToFile("testMergeNodesByLocation-after");
//
//        assertEquals(graph.getVertexCount(), 3);
//        parameters.setStringValue(MergeNodesPlugin.THRESHOLD_PARAMETER_ID, "800000");
//        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);
//
//        saveGraphToFile("testMergeNodesByLocation-after2");
//
//        assertEquals(graph.getVertexCount(), 2);
    }
}

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
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Split Nodes Plugin Split Logic Test.
 */
public class SplitNodesPluginSplitLogicNGTest {

    private int vertexIdentifierAttribute;
    private int vertexTypeAttribute;
    private int vertexLatitudeAttribute;
    private int vertexLongitudeAttribute;
    private int vertexSelectedAttribute;
    private int vertexAttributeX;
    private int vertexAttributeY;
    private int vertexAttributeZ;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);

        final int attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        final int attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int attrZ = VisualConcept.VertexAttribute.Z.ensure(graph);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        vertexAttributeX = VisualConcept.VertexAttribute.X.ensure(graph);
        vertexAttributeY = VisualConcept.VertexAttribute.Y.ensure(graph);
        vertexAttributeZ = VisualConcept.VertexAttribute.Z.ensure(graph);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        vertexLatitudeAttribute = SpatialConcept.VertexAttribute.LATITUDE.ensure(graph);
        vertexLongitudeAttribute = SpatialConcept.VertexAttribute.LONGITUDE.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // add vertices
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();

        // set the identifier of four vertices to somthing unique but similar, and the remaining vertex to a duplicate
        graph.setStringValue(vertexIdentifierAttribute, vxId1, ",,,");
        graph.setStringValue(vertexIdentifierAttribute, vxId2, "v,,v");
        graph.setStringValue(vertexIdentifierAttribute, vxId3, ",,v");
        graph.setStringValue(vertexIdentifierAttribute, vxId4, "v,,");

        // set the x,y,z of the vertices that will be splitted
        graph.setDoubleValue(vertexAttributeX, vxId1, attrX);
        graph.setDoubleValue(vertexAttributeY, vxId1, attrY);
        graph.setDoubleValue(vertexAttributeZ, vxId1, attrZ);
        graph.setDoubleValue(vertexAttributeX, vxId2, attrX + 10);
        graph.setDoubleValue(vertexAttributeY, vxId2, attrY + 10);
        graph.setDoubleValue(vertexAttributeZ, vxId2, attrZ);

        // set the type of four vertices to a schema type, and the remaining two vertices to a non-schema type
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

        //Add Transactions
        graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId2, vxId4, true);
        graph.addTransaction(vxId1, vxId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    /**
     * Test of splitting logic, of class SplitNodesPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testQueryAllOccurancesNotSelected_AllSplitParameter() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        // Node renamed to ",,"
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId1), ",,");
    }

    @Test
    public void testQueryAllOccurancesNotSelected_SplitParameterRepeatsInTheMiddle() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 4);
        // Creates 2 nodes (v and ,v)
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId2), "v");
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4 + 1), ",v");
    }

    @Test
    public void testQueryAllOccurancesNotSelected_SplitParameterRepeatsInTheBeginning() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        // Node renamed to ",v"
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId3), ",v");
    }

    @Test
    public void testQueryAllOccurancesNotSelected_SplitParameterRepeatsInTheEnd() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 4);
        // Creates 2 nodes (v and ,)
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "v");
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4 + 1), ",");
    }

    @Test
    public void testQueryAllOccurancesSelected_AllSplitParameter() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        // Does Nothing
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId1), ",,,");
    }

    @Test
    public void testQueryAllOccurancesSelected_SplitParameterRepeatsInTheMiddle() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getTransactionCount(), 4);
        // Creates 2 nodes (v and v)
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId2), "v");
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4 + 1), "v");
    }

    @Test
    public void testQueryAllOccurancesSelected_SplitParameterRepeatsInTheBeginning() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        // Node renamed to "v"
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId3), "v");
    }

    @Test
    public void testQueryAllOccurancesSelected_SplitParameterRepeatsInTheEnd() throws Exception {
        final SplitNodesPlugin instance = new SplitNodesPlugin();
        final PluginParameters parameters = instance.createParameters();

        parameters.setStringValue(SplitNodesPlugin.SPLIT_PARAMETER_ID, ",");
        parameters.setBooleanValue(SplitNodesPlugin.ALL_OCCURRENCES_PARAMETER_ID, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        PluginExecution.withPlugin(instance).withParameters(parameters).executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 3);
        // Node renamed to "v"
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId4), "v");
    }
}

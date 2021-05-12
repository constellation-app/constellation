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
package au.gov.asd.tac.constellation.graph.visual.framework;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class GraphVisualAccessNGTest {
    
    DualGraph graph;
    StoreGraph sGraph;
    
    int vxId1;
    int vxId2;
    
    int tId1;
    
    public GraphVisualAccessNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        
        sGraph = new StoreGraph();
        
        vxId1 = sGraph.addVertex();
        vxId2 = sGraph.addVertex();
        
        tId1 = sGraph.addTransaction(vxId1, vxId2, true);
        
        graph = new DualGraph(schema, sGraph);       
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getIndigenousChanges method, of class GraphVisualAccess.
     */
    @Test
    public void testGetIndigenousChanges() {
        System.out.println("getIndigenousChanges");
        
        final GraphVisualAccess instance = new GraphVisualAccess(graph);
        
        instance.beginUpdate();        
        final List<VisualChange> changes = instance.getIndigenousChanges();       
        instance.endUpdate();

        assertEquals(changes.size(), 37);
    }

    /**
     * Test of updateInternally method, of class GraphVisualAccess.
     */
    @Test
    public void testUpdateInternally() {
        System.out.println("updateInternally");
        final GraphVisualAccess instance = new GraphVisualAccess(graph);
        instance.updateInternally();
        
        //update(false)
    }

    /**
     * Test of getDrawFlags method, of class GraphVisualAccess.
     */
    @Test
    public void testGetDrawFlags() {
        System.out.println("getDrawFlags");
        GraphVisualAccess instance = null;
        DrawFlags expResult = null;
        DrawFlags result = instance.getDrawFlags();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCamera method, of class GraphVisualAccess.
     */
    @Test
    public void testGetCamera() {
        System.out.println("getCamera");
        GraphVisualAccess instance = null;
        Camera expResult = null;
        Camera result = instance.getCamera();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionId method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionId() {
        System.out.println("getConnectionId");
        int connection = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getConnectionId(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionDirection method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionDirection() {
        System.out.println("getConnectionDirection");
        int connection = 0;
        GraphVisualAccess instance = null;
        VisualAccess.ConnectionDirection expResult = null;
        VisualAccess.ConnectionDirection result = instance.getConnectionDirection(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionDirected method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionDirected() {
        System.out.println("getConnectionDirected");
        int connection = 0;
        GraphVisualAccess instance = null;
        boolean expResult = false;
        boolean result = instance.getConnectionDirected(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVertexColor method, of class GraphVisualAccess.
     */
    @Test
    public void testGetVertexColor() {
        System.out.println("getVertexColor");
        int vertex = 0;
        GraphVisualAccess instance = null;
        ConstellationColor expResult = null;
        ConstellationColor result = instance.getVertexColor(vertex);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVertexVisibility method, of class GraphVisualAccess.
     */
    @Test
    public void testGetVertexVisibility() {
        System.out.println("getVertexVisibility");
        int vertex = 0;
        GraphVisualAccess instance = null;
        float expResult = 0.0F;
        float result = instance.getVertexVisibility(vertex);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBlazed method, of class GraphVisualAccess.
     */
    @Test
    public void testGetBlazed() {
        System.out.println("getBlazed");
        int vertex = 0;
        GraphVisualAccess instance = null;
        boolean expResult = false;
        boolean result = instance.getBlazed(vertex);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBlazeAngle method, of class GraphVisualAccess.
     */
    @Test
    public void testGetBlazeAngle() {
        System.out.println("getBlazeAngle");
        int vertex = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getBlazeAngle(vertex);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBlazeColor method, of class GraphVisualAccess.
     */
    @Test
    public void testGetBlazeColor() {
        System.out.println("getBlazeColor");
        int vertex = 0;
        GraphVisualAccess instance = null;
        ConstellationColor expResult = null;
        ConstellationColor result = instance.getBlazeColor(vertex);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionColor method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionColor() {
        System.out.println("getConnectionColor");
        int connection = 0;
        GraphVisualAccess instance = null;
        ConstellationColor expResult = null;
        ConstellationColor result = instance.getConnectionColor(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionSelected method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionSelected() {
        System.out.println("getConnectionSelected");
        int connection = 0;
        GraphVisualAccess instance = null;
        boolean expResult = false;
        boolean result = instance.getConnectionSelected(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionVisibility method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionVisibility() {
        System.out.println("getConnectionVisibility");
        int connection = 0;
        GraphVisualAccess instance = null;
        float expResult = 0.0F;
        float result = instance.getConnectionVisibility(connection);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionDimmed method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionDimmed() {
        System.out.println("getConnectionDimmed");
        int connection = 0;
        GraphVisualAccess instance = null;
        boolean expResult = false;
        boolean result = instance.getConnectionDimmed(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionLineStyle method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionLineStyle() {
        System.out.println("getConnectionLineStyle");
        int connection = 0;
        GraphVisualAccess instance = null;
        LineStyle expResult = null;
        LineStyle result = instance.getConnectionLineStyle(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionWidth method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionWidth() {
        System.out.println("getConnectionWidth");
        int connection = 0;
        GraphVisualAccess instance = null;
        float expResult = 0.0F;
        float result = instance.getConnectionWidth(connection);
        assertEquals(result, expResult, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionLowVertex method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionLowVertex() {
        System.out.println("getConnectionLowVertex");
        int connection = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getConnectionLowVertex(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionHighVertex method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionHighVertex() {
        System.out.println("getConnectionHighVertex");
        int connection = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getConnectionHighVertex(connection);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLinkSource method, of class GraphVisualAccess.
     */
    @Test
    public void testGetLinkSource() {
        System.out.println("getLinkSource");
        int link = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getLinkSource(link);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLinkDestination method, of class GraphVisualAccess.
     */
    @Test
    public void testGetLinkDestination() {
        System.out.println("getLinkDestination");
        int link = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getLinkDestination(link);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLinkConnectionCount method, of class GraphVisualAccess.
     */
    @Test
    public void testGetLinkConnectionCount() {
        System.out.println("getLinkConnectionCount");
        int link = 0;
        GraphVisualAccess instance = null;
        int expResult = 0;
        int result = instance.getLinkConnectionCount(link);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionLabelText method, of class GraphVisualAccess.
     */
    @Test
    public void testGetConnectionLabelText() {
        System.out.println("getConnectionLabelText");
        int connection = 0;
        int labelNum = 0;
        GraphVisualAccess instance = null;
        String expResult = "";
        String result = instance.getConnectionLabelText(connection, labelNum);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateModCounts method, of class GraphVisualAccess.
     */
    @Test
    public void testUpdateModCounts() {
        System.out.println("updateModCounts");
        ReadableGraph readGraph = null;
        GraphVisualAccess instance = null;
        instance.updateModCounts(readGraph);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }    
}

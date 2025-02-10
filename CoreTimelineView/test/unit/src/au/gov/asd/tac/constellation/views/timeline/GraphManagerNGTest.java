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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class GraphManagerNGTest {

    public GraphManagerNGTest() {
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

//    /**
//     * Test of getDefault method, of class GraphManager.
//     */
//    @Test
//    public void testGetDefault() {
//        System.out.println("getDefault");
//        GraphManager expResult = null;
//        GraphManager result = GraphManager.getDefault();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    /**
//     * Test of select method, of class GraphManager.
//     */
//    @Test
//    public void testSelect() {
//        System.out.println("select");
//        int srcID = 0;
//        int destID = 0;
//        int transID = 0;
//        boolean isCtrlDown = false;
//        GraphManager instance = null;
//        instance.select(srcID, destID, transID, isCtrlDown);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of selectAllInRange method, of class GraphManager.
     */
    @Test
    public void testSelectAllInRange() {
        System.out.println("selectAllInRange");
        long lowerTimeExtent = 0L;
        long upperTimeExtent = 0L;
        boolean isCtrlDown = false;
        boolean isDragSelection = false;
        boolean selectedOnly = false;

        final GraphNode mockGraphNode = mock(GraphNode.class);
        final Graph mockGraph = mock(Graph.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        final TopComponent.Registry mockReg = mock(TopComponent.Registry.class);
        final Node[] nodes = {mockGraphNode};
        when(mockReg.getActivatedNodes()).thenReturn(nodes);

        try (MockedStatic<TopComponent> mockTopComponent = Mockito.mockStatic(TopComponent.class)) {
            mockTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
            assertThat(TopComponent.getRegistry()).isEqualTo(mockReg);

            final GraphManager instance = GraphManager.getDefault();
            assertEquals(instance.getClass(), GraphManager.class);

            instance.setDatetimeAttr("");
            instance.resultChanged(null);

            instance.selectAllInRange(lowerTimeExtent, upperTimeExtent, isCtrlDown, isDragSelection, selectedOnly);

            mockTopComponent.verify(TopComponent::getRegistry, times(2));
            verify(mockReg).getActivatedNodes();
            verify(mockGraphNode, times(2)).getGraph();
            verify(mockGraph).getReadableGraph();
        }
    }

//    /**
//     * Test of setElementSelected method, of class GraphManager.
//     */
//    @Test
//    public void testSetElementSelected() {
//        System.out.println("setElementSelected");
//        boolean isElementSelected = false;
//        GraphManager instance = null;
//        instance.setElementSelected(isElementSelected);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isElementSelected method, of class GraphManager.
//     */
//    @Test
//    public void testIsElementSelected() {
//        System.out.println("isElementSelected");
//        GraphManager instance = null;
//        boolean expResult = false;
//        boolean result = instance.isElementSelected();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDatetimeAttr method, of class GraphManager.
//     */
//    @Test
//    public void testGetDatetimeAttr() {
//        System.out.println("getDatetimeAttr");
//        GraphManager instance = null;
//        String expResult = "";
//        String result = instance.getDatetimeAttr();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDatetimeAttr method, of class GraphManager.
//     */
//    @Test
//    public void testSetDatetimeAttr() {
//        System.out.println("setDatetimeAttr");
//        String datetimeAttr = "";
//        GraphManager instance = null;
//        instance.setDatetimeAttr(datetimeAttr);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getVertexAttributeNames method, of class GraphManager.
     */
    @Test
    public void testGetVertexAttributeNames() {
        System.out.println("getVertexAttributeNames");
        final List expResult = new ArrayList<>();

        final GraphNode mockGraphNode = mock(GraphNode.class);
        final Graph mockGraph = mock(Graph.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        final TopComponent.Registry mockReg = mock(TopComponent.Registry.class);
        final Node[] nodes = {mockGraphNode};
        when(mockReg.getActivatedNodes()).thenReturn(nodes);

        try (MockedStatic<TopComponent> mockTopComponent = Mockito.mockStatic(TopComponent.class)) {
            mockTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
            assertThat(TopComponent.getRegistry()).isEqualTo(mockReg);

            final GraphManager instance = GraphManager.getDefault();
            assertEquals(instance.getClass(), GraphManager.class);

            instance.setDatetimeAttr("");
            instance.resultChanged(null);

            final List result = instance.getVertexAttributeNames();
            assertEquals(result, expResult);

            mockTopComponent.verify(TopComponent::getRegistry, times(3));
            verify(mockReg).getActivatedNodes();
            verify(mockGraphNode, times(1)).getGraph();
            verify(mockGraph).getReadableGraph();
        }
    }

//    /**
//     * Test of resultChanged method, of class GraphManager.
//     */
//    @Test
//    public void testResultChanged() {
//        System.out.println("resultChanged");
//        LookupEvent lev = null;
//        GraphManager instance = null;
//        instance.resultChanged(lev);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

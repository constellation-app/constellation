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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.awt.UndoRedo;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class HistogramTopComponent2NGTest {

    /**
     * Test of componentShowing method, of class HistogramTopComponent2.
     */
    @Test
    public void testComponentShowing() {
        System.out.println("componentShowing");
        final HistogramController mockController = mock(HistogramController.class);
        when(mockController.init(any(HistogramTopComponent2.class))).thenReturn(mockController);

        try (final MockedStatic<HistogramController> mockedStaticController = Mockito.mockStatic(HistogramController.class)) {

            mockedStaticController.when(HistogramController::getDefault).thenReturn(mockController);
            final HistogramTopComponent2 instance = new HistogramTopComponent2();
            instance.componentShowing();

            verify(mockController).readState();
        }

    }

    /**
     * Test of createContent method, of class HistogramTopComponent2.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");
        try (MockedConstruction<HistogramPane> mockConstructor = Mockito.mockConstruction(HistogramPane.class)) {

            final HistogramTopComponent2 instance = new HistogramTopComponent2();
            final HistogramPane result = instance.createContent();

            assertEquals(1, mockConstructor.constructed().size());
            assertEquals(result.getClass(), HistogramPane.class);
        }
    }

    /**
     * Test of createStyle method, of class HistogramTopComponent2.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        final HistogramTopComponent2 instance = new HistogramTopComponent2();

        //final String expResult =  "resources/histogram-view.css"; // Uncomment when histogram rewrite fully replaces old histogram
        final String expResult = "histogram-view.css";// Remove this when histogram rewrite fully replaces old histogram

        final String result = instance.createStyle();
        assertEquals(result, expResult);
    }

    /**
     * Test of handleComponentOpened method, of class HistogramTopComponent2.
     */
    @Test
    public void testHandleComponentOpened() {
        System.out.println("handleComponentOpened");

        final Graph mockGraph = mock(Graph.class);
        final GraphManager mockGraphManager = mock(GraphManager.class);
        when(mockGraphManager.getActiveGraph()).thenReturn(mockGraph);

        try (final MockedStatic<GraphManager> mockedGraphManager = Mockito.mockStatic(GraphManager.class)) {

            mockedGraphManager.when(GraphManager::getDefault).thenReturn(mockGraphManager);

            final HistogramTopComponent2 instance = new HistogramTopComponent2();
            instance.handleComponentOpened();

            verify(mockGraphManager).getActiveGraph();
        }
    }

    /**
     * Test of handleComponentClosed method, of class HistogramTopComponent2.
     */
    @Test
    public void testHandleComponentClosed() {
        // Hard to test really
        System.out.println("handleComponentClosed");
        final HistogramTopComponent2 instance = new HistogramTopComponent2();
        instance.handleComponentClosed();
    }

    /**
     * Test of handleNewGraph method, of class HistogramTopComponent2.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("handleNewGraph");
        final Graph graph = mock(Graph.class);
        final HistogramTopComponent2 instance = spy(new HistogramTopComponent2());
        instance.handleNewGraph(graph);

        verify(instance).reset();
    }

    /**
     * Test of getUndoRedo method, of class HistogramTopComponent2.
     */
    @Test
    public void testGetUndoRedo() {
        System.out.println("getUndoRedo");
        final HistogramTopComponent2 instance = new HistogramTopComponent2();
        final UndoRedo expResult = null;
        final UndoRedo result = instance.getUndoRedo();
        assertEquals(result, expResult);
    }

    /**
     * Test of handleGraphChange method, of class HistogramTopComponent2.
     */
    @Test
    public void testHandleGraphChange() {
        System.out.println("handleGraphChange");
        final GraphChangeEvent event = mock(GraphChangeEvent.class);
        when(event.getLatest()).thenReturn(event);
        when(event.getId()).thenReturn(1L);

        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        when(mockReadableGraph.getStructureModificationCounter()).thenReturn(Long.MIN_VALUE);
        when(mockReadableGraph.getAttributeModificationCounter()).thenReturn(Long.MIN_VALUE);

        final Graph mockGraph = mock(Graph.class);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        final HistogramTopComponent2 instance = new HistogramTopComponent2();
        instance.newActiveGraph(mockGraph);
        instance.handleGraphChange(event);

        verify(mockReadableGraph).getStructureModificationCounter();
        verify(mockReadableGraph).getAttributeModificationCounter();
    }

//    /**
//     * Test of modifyBinHeight method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testModifyBinHeight() {
//        System.out.println("modifyBinHeight");
//        int change = 0;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.modifyBinHeight(change);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of reset method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testReset_0args() {
//        System.out.println("reset");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.reset();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of reset method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testReset_GraphReadMethods() {
//        System.out.println("reset");
//        GraphReadMethods graph = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.reset(graph);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setHistogramViewOptions method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetHistogramViewOptions() {
//        System.out.println("setHistogramViewOptions");
//        GraphElementType elementType = null;
//        AttributeType attributeType = null;
//        String attribute = "";
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setHistogramViewOptions(elementType, attributeType, attribute);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setGraphElementType method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetGraphElementType() {
//        System.out.println("setGraphElementType");
//        GraphElementType elementType = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setGraphElementType(elementType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAttributeType method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetAttributeType() {
//        System.out.println("setAttributeType");
//        AttributeType attributeType = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setAttributeType(attributeType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAttribute method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetAttribute() {
//        System.out.println("setAttribute");
//        String attribute = "";
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setAttribute(attribute);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setBinComparator method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetBinComparator() {
//        System.out.println("setBinComparator");
//        BinComparator binComparator = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setBinComparator(binComparator);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setBinFormatter method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetBinFormatter() {
//        System.out.println("setBinFormatter");
//        BinFormatter binFormatter = null;
//        PluginParameters parameters = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setBinFormatter(binFormatter, parameters);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setBinSelectionMode method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSetBinSelectionMode() {
//        System.out.println("setBinSelectionMode");
//        BinSelectionMode binSelectionMode = null;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.setBinSelectionMode(binSelectionMode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of selectOnlyBins method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSelectOnlyBins() {
//        System.out.println("selectOnlyBins");
//        int firstBin = 0;
//        int lastBin = 0;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.selectOnlyBins(firstBin, lastBin);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of filterOnSelection method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testFilterOnSelection() {
//        System.out.println("filterOnSelection");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.filterOnSelection();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearFilter method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testClearFilter() {
//        System.out.println("clearFilter");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.clearFilter();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of selectBins method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSelectBins() {
//        System.out.println("selectBins");
//        int firstBin = 0;
//        int lastBin = 0;
//        boolean select = false;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.selectBins(firstBin, lastBin, select);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of invertBins method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testInvertBins() {
//        System.out.println("invertBins");
//        int firstBin = 0;
//        int lastBin = 0;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.invertBins(firstBin, lastBin);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of completeBins method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testCompleteBins() {
//        System.out.println("completeBins");
//        int firstBin = 0;
//        int lastBin = 0;
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.completeBins(firstBin, lastBin);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of filterSelection method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testFilterSelection() {
//        System.out.println("filterSelection");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.filterSelection();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveBinsToGraph method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSaveBinsToGraph() {
//        System.out.println("saveBinsToGraph");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.saveBinsToGraph();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveBinsToClipboard method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testSaveBinsToClipboard() {
//        System.out.println("saveBinsToClipboard");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.saveBinsToClipboard();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of expandSelection method, of class HistogramTopComponent2.
//     */
//    @Test
//    public void testExpandSelection() {
//        System.out.println("expandSelection");
//        HistogramTopComponent2 instance = new HistogramTopComponent2();
//        instance.expandSelection();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
}

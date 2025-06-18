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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLogger;
import au.gov.asd.tac.constellation.views.layers.components.LayersViewPane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class LayersViewTopComponentNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of setPaneStatus method, of class LayersViewTopComponent.
     */
    @Test
    public void testSetPaneStatus() {
        System.out.println("setPaneStatus");

        final GraphManager graphManager = mock(GraphManager.class);
        when(graphManager.getActiveGraph()).thenReturn(null);

        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());

        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        when(mockedTopComponent.createContent()).thenReturn(lvp);

        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);) {
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            mockedTopComponent.setPaneStatus();

            verify(mockedTopComponent).createContent();
            verify(lvp).setEnabled(Mockito.eq(false));
        }
    }

    /**
     * Test of setPaneStatus method, of class LayersViewTopComponent.
     */
    @Test
    public void testSetPaneStatusTrue() {
        System.out.println("setPaneStatusTrue");

        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        when(graphManager.getActiveGraph()).thenReturn(graph);

        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());

        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        when(mockedTopComponent.createContent()).thenReturn(lvp);

        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class);) {
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            mockedTopComponent.setPaneStatus();

            verify(mockedTopComponent).createContent();
            verify(lvp).setEnabled(Mockito.eq(true));
        }
    }

    /**
     * Test of handleGraphOpened method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleGraphOpened() {
        System.out.println("testHandleGraphOpened");

        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        when(graphManager.getActiveGraph()).thenReturn(graph);
        
        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setDefaultLayers();
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());
        when(mockedTopComponent.createContent()).thenReturn(lvp);

        final LayersViewController controller = mock(LayersViewController.class);
        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);

        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        doCallRealMethod().when(mockedTopComponent).preparePane();
        doCallRealMethod().when(mockedTopComponent).handleGraphOpened(graph);

        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);
        doNothing().when(controller).readState();
        doNothing().when(controller).addAttributes();        

        mockedTopComponent.handleGraphOpened(graph);
        verify(mockedTopComponent, times(1)).preparePane();
        // called once in preparePane() and once in setPaneStatus()
        verify(mockedTopComponent, times(2)).createContent();
        verify(mockedTopComponent, times(1)).setPaneStatus();
    }

    /**
     * Test of handleNewGraph method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("testHandleNewGraph");

        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        when(graphManager.getActiveGraph()).thenReturn(graph);

        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setDefaultLayers();
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());
        when(mockedTopComponent.createContent()).thenReturn(lvp);
        
        final LayersViewController controller = mock(LayersViewController.class);
        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);
        doCallRealMethod().when(mockedTopComponent).handleNewGraph(graph);
        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        doCallRealMethod().when(mockedTopComponent).preparePane();

        mockedTopComponent.handleNewGraph(graph);
        verify(mockedTopComponent, times(1)).preparePane();
        // called once in preparePane() and once in setPaneStatus()
        verify(mockedTopComponent, times(2)).createContent();
        verify(mockedTopComponent, times(1)).setPaneStatus();

    }

    /**
     * Test of handleGraphClosed method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleGraphClosed() {
        System.out.println("testHandleGraphClosed");
        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        when(graphManager.getActiveGraph()).thenReturn(graph);
    
        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setDefaultLayers();
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());
        when(mockedTopComponent.createContent()).thenReturn(lvp);

        final LayersViewController controller = mock(LayersViewController.class);
        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);
        doCallRealMethod().when(mockedTopComponent).handleGraphClosed(graph);
        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        doCallRealMethod().when(mockedTopComponent).preparePane();

        mockedTopComponent.handleGraphClosed(graph);
        verify(mockedTopComponent, times(1)).preparePane();
        // called once in preparePane() and once in setPaneStatus()
        verify(mockedTopComponent, times(2)).createContent();
        verify(mockedTopComponent, times(1)).setPaneStatus();
    }

    /**
     * Test of componentShowing method, of class LayersViewTopComponent.
     */
    @Test
    public void testComponentShowing() {
        System.out.println("testComponentShowing");

        final LayersViewController controller = mock(LayersViewController.class);
        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);

        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);
        doNothing().when(mockedTopComponent).componentActivated();
        doNothing().when(controller).readState();
        doNothing().when(controller).addAttributes();
        doNothing().when(mockedTopComponent).setPaneStatus();

        doCallRealMethod().when(mockedTopComponent).componentShowing();
        mockedTopComponent.componentShowing();
        verify(mockedTopComponent, times(1)).componentActivated();
        verify(controller, times(1)).readState();
        verify(controller, times(1)).addAttributes();
        verify(mockedTopComponent, times(1)).setPaneStatus();
    }

    /**
     * Test of componentHidden method, of class LayersViewTopComponent.
     */
    @Test
    public void testComponentHidden() {
        System.out.println("testComponentHidden");

        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);

        doCallRealMethod().when(mockedTopComponent).componentHidden();
        mockedTopComponent.componentHidden();
        assertFalse(mockedTopComponent.isShowingFlag());
        assertFalse(mockedTopComponent.getVisibility());
    }

    /**
     * Test of componentActivated method, of class
     * LayersViewTopComponent.
     */
    @Test
    public void testComponentActivated() {
        System.out.println("testComponentActivated");

        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);

        try {
            // mock WindowManager and ConstellationLogger
            final MockedStatic<WindowManager> windowManagerStatic = Mockito.mockStatic(WindowManager.class);
            final MockedStatic<ConstellationLogger> constellationLoggerStatic = Mockito.mockStatic(ConstellationLogger.class);

            final WindowManager mockedWindowManager = spy(WindowManager.class);
            windowManagerStatic.when(WindowManager::getDefault).thenReturn(mockedWindowManager);
            when(mockedWindowManager.isTopComponentFloating(Mockito.any())).thenReturn(Boolean.TRUE);
            when(mockedWindowManager.isTopComponentMinimized(Mockito.any())).thenReturn(Boolean.TRUE);

            final ConstellationLogger mockedConstellationLogger = spy(ConstellationLogger.class);
            constellationLoggerStatic.when(ConstellationLogger::getDefault).thenReturn(mockedConstellationLogger);

            doCallRealMethod().when(mockedTopComponent).componentActivated();

            mockedTopComponent.componentActivated();
            verify(mockedTopComponent, times(1)).isShowingFlag();
            verify(mockedConstellationLogger, times(1)).viewInfo(mockedTopComponent, "Activated / Floating");
            verify(mockedConstellationLogger, times(1)).viewInfo(mockedTopComponent, "Activated / Minimised");
            verify(mockedWindowManager, times(1)).isTopComponentFloating(mockedTopComponent);
            verify(mockedWindowManager, times(2)).isTopComponentMinimized(mockedTopComponent);

            when(mockedWindowManager.isTopComponentFloating(Mockito.any())).thenReturn(Boolean.FALSE);
            when(mockedWindowManager.isTopComponentMinimized(Mockito.any())).thenReturn(Boolean.FALSE);
            mockedTopComponent.componentActivated();
            verify(mockedTopComponent, times(2)).isShowingFlag();
            // if it is not minimised and not floating, it's docked
            verify(mockedConstellationLogger, times(1)).viewInfo(mockedTopComponent, "Activated / Docked");

            verify(mockedWindowManager, times(3)).isTopComponentFloating(mockedTopComponent); // 1(in 1st call when true)+2(in 2nd call)
            verify(mockedWindowManager, times(4)).isTopComponentMinimized(mockedTopComponent); // 2(in 1st call when true)+2(in 2nd call)
            windowManagerStatic.close();
            constellationLoggerStatic.close();
            
        } catch (final Exception e) {
            System.out.println("Error in testComponentActivated: " + e.getMessage());
            assertFalse(true);
        }        
    }
    
     /**
     * Test of handleComponentOpened method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleComponentOpened() {
        System.out.println("testHandleComponentOpened");
             
        final LayersViewTopComponent mockedTopComponent = mock(LayersViewTopComponent.class);
        final LayersViewPane lvp = mock(LayersViewPane.class);
        doNothing().when(lvp).setDefaultLayers();
        doNothing().when(lvp).setEnabled(Mockito.anyBoolean());
        when(mockedTopComponent.createContent()).thenReturn(lvp);

        final LayersViewController controller = mock(LayersViewController.class);
        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);

        doCallRealMethod().when(mockedTopComponent).setPaneStatus();
        doCallRealMethod().when(mockedTopComponent).preparePane();
        doCallRealMethod().when(mockedTopComponent).handleComponentOpened();

        when(mockedTopComponent.getLayersViewController()).thenReturn(controller);
        doNothing().when(controller).readState();
        doNothing().when(controller).addAttributes();        

        mockedTopComponent.handleComponentOpened();
        verify(mockedTopComponent, times(1)).preparePane();
        // called once in preparePane() and once in setPaneStatus()
        verify(mockedTopComponent, times(2)).createContent();
        verify(mockedTopComponent, times(1)).setPaneStatus();
    }

}

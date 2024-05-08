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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.layers.components.LayersViewPane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    
    public LayersViewTopComponentNGTest() {
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

    /**
     * Test of update method, of class LayersViewTopComponent.
     */
    @Test
    public void testUpdate() {
    }

    /**
     * Test of removeValueHandlers method, of class LayersViewTopComponent.
     */
    @Test
    public void testRemoveValueHandlers() {
    }

    /**
     * Test of setChangeListeners method, of class LayersViewTopComponent.
     */
    @Test
    public void testSetChangeListeners() {
    }

    /**
     * Test of createStyle method, of class LayersViewTopComponent.
     */
    @Test
    public void testCreateStyle() {
    }

    /**
     * Test of createContent method, of class LayersViewTopComponent.
     */
    @Test
    public void testCreateContent() {
    }

    /**
     * Test of handleNewGraph method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
    }

    /**
     * Test of handleGraphOpened method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleGraphOpened() {
    }

    /**
     * Test of handleGraphClosed method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleGraphClosed() {
    }

    /**
     * Test of handleComponentOpened method, of class LayersViewTopComponent.
     */
    @Test
    public void testHandleComponentOpened() {
    }

    /**
     * Test of componentShowing method, of class LayersViewTopComponent.
     */
    @Test
    public void testComponentShowing() {
    }

    /**
     * Test of preparePane method, of class LayersViewTopComponent.
     */
    @Test
    public void testPreparePane() {
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
    
}

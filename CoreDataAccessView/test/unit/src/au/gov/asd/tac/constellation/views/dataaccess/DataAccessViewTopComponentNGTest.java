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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
import java.util.List;
import javafx.application.Platform;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessViewTopComponentNGTest {
    private static MockedStatic<ProxyUtilities> proxyUtilsMockedStatic;
    
    private List<DataAccessPane> constructedDataAccessPanes;
    
    private DataAccessViewTopComponent dataAccessViewTopComponent;
    
    @BeforeClass(enabled = false)
    public static void setUpClass() throws Exception {
        proxyUtilsMockedStatic = Mockito.mockStatic(ProxyUtilities.class);
    }

    @AfterClass(enabled = false)
    public static void tearDownClass() throws Exception {
        proxyUtilsMockedStatic.close();
    }
    
    @BeforeMethod(enabled = false)
    public void setUpMethod() throws Exception {
        try (
                MockedConstruction<DataAccessPane> dapMockedConstruction =
                        Mockito.mockConstruction(DataAccessPane.class);
        ) {
            dataAccessViewTopComponent = new DataAccessViewTopComponent();
            
            constructedDataAccessPanes = List.copyOf(dapMockedConstruction.constructed());
        }
    }
    
    @AfterMethod(enabled = false)
    public void tearDownMethod() throws Exception {
        proxyUtilsMockedStatic.reset();
    }
    
    @Test(enabled = false)
    public void init() {
        assertEquals(dataAccessViewTopComponent.getName(), "Data Access View");
        assertEquals(dataAccessViewTopComponent.getToolTipText(), "Data Access View");
        
        assertEquals(constructedDataAccessPanes.size(), 1);
        
        verify(constructedDataAccessPanes.get(0)).addUIComponents();
        assertSame(dataAccessViewTopComponent.getDataAccessPane(), constructedDataAccessPanes.get(0));
        assertSame(dataAccessViewTopComponent.createContent(), constructedDataAccessPanes.get(0));
        
        assertNotNull(dataAccessViewTopComponent.getExecutorService());
        
        proxyUtilsMockedStatic.verify(() -> ProxyUtilities.setProxySelector(null));
        
        assertEquals(dataAccessViewTopComponent.createStyle(), "resources/data-access-view.css");
    }
    
    @Test(enabled = false)
    public void handleNewGraph() {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        final DataAccessViewTopComponent spiedTopComponent = spy(dataAccessViewTopComponent);
        doReturn(dataAccessPane).when(spiedTopComponent).getDataAccessPane();
        
        final Graph graph = mock(Graph.class);
        
        try (
                final MockedStatic<Platform> platformMockedStatic =
                        Mockito.mockStatic(Platform.class);
                final MockedStatic<DataAccessUtilities> utilitiesMockedStatic =
                        Mockito.mockStatic(DataAccessUtilities.class);
        ) {
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer((iom) -> {
                        final Runnable runnable = iom.getArgument(0);
                        runnable.run();
                        return null;
                    });

            spiedTopComponent.componentOpened();
            
            spiedTopComponent.handleNewGraph(graph);

            verify(dataAccessPane).update(graph);

            utilitiesMockedStatic.verify(() -> DataAccessUtilities
                    .loadDataAccessState(dataAccessPane, graph));
        }
    }
    
    @Test(enabled = false)
    public void handleNewGraph_data_access_view_not_open() {
        final Graph graph = mock(Graph.class);
        
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        final DataAccessViewTopComponent spiedTopComponent = spy(dataAccessViewTopComponent);
        
        doReturn(dataAccessPane).when(spiedTopComponent).getDataAccessPane();
        
        spiedTopComponent.handleNewGraph(graph);
        
        verify(dataAccessPane, never()).update(graph);
    }
    
    @Test(enabled = false)
    public void componentShowing() {
        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        
        when(graphManager.getActiveGraph()).thenReturn(graph);

        final DataAccessViewTopComponent spiedTopComponent = spy(dataAccessViewTopComponent);
        
        doNothing().when(spiedTopComponent).handleNewGraph(graph);
        
        try (
                final MockedStatic<GraphManager> graphManagerMockedStatic =
                        Mockito.mockStatic(GraphManager.class);
        ) {
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            
            spiedTopComponent.componentShowing();
            
            verify(spiedTopComponent).handleNewGraph(graph);
        }
    }
    
    @Test(enabled = false)
    public void handleComponentClosed() {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        final DataAccessViewTopComponent spiedTopComponent = spy(dataAccessViewTopComponent);
        
        doReturn(dataAccessPane).when(spiedTopComponent).getDataAccessPane();
        
        try (
                final MockedStatic<QualityControlAutoVetter> qualityControlAutoVetterMockedStatic =
                        Mockito.mockStatic(QualityControlAutoVetter.class);
        ) {
            final QualityControlAutoVetter instance = mock(QualityControlAutoVetter.class);
            qualityControlAutoVetterMockedStatic.when(QualityControlAutoVetter::getInstance).thenReturn(instance);
            
            spiedTopComponent.handleComponentClosed();
            
            verify(instance).removeObserver(dataAccessPane);
        }
    }
    
    @Test(enabled = false)
    public void handleComponentOpened() {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        final DataAccessViewTopComponent spiedTopComponent = spy(dataAccessViewTopComponent);
        
        doReturn(dataAccessPane).when(spiedTopComponent).getDataAccessPane();
        
        try (
                final MockedStatic<QualityControlAutoVetter> qualityControlAutoVetterMockedStatic =
                        Mockito.mockStatic(QualityControlAutoVetter.class);
        ) {
            final QualityControlAutoVetter instance = mock(QualityControlAutoVetter.class);
            qualityControlAutoVetterMockedStatic.when(QualityControlAutoVetter::getInstance).thenReturn(instance);
            
            spiedTopComponent.handleComponentOpened();
            
            verify(instance).addObserver(dataAccessPane);
        }
    }
}

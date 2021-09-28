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
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessPreferencesIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
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
    
    private MockedConstruction<DataAccessPane> dapMockedConstruction;
    
    private DataAccessViewTopComponent dataAccessViewTopComponent;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        proxyUtilsMockedStatic = Mockito.mockStatic(ProxyUtilities.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        proxyUtilsMockedStatic.close();
    }
    
    @BeforeMethod
    public void setUpMethod() {
        dapMockedConstruction = Mockito.mockConstruction(DataAccessPane.class);
        
        dataAccessViewTopComponent = spy(new DataAccessViewTopComponent());
    }
    
    @AfterMethod
    public void tearDownMethod() throws Exception {
        proxyUtilsMockedStatic.reset();
        
        dapMockedConstruction.close();
    }
    
    @Test
    public void init() {
        assertEquals(dataAccessViewTopComponent.getName(), "Data Access View");
        assertEquals(dataAccessViewTopComponent.getToolTipText(), "Data Access View");
        
        assertEquals(dapMockedConstruction.constructed().size(), 1);
        
        final DataAccessPane constructedPane = dapMockedConstruction.constructed().get(0);
        verify(constructedPane).addUIComponents();
        assertSame(dataAccessViewTopComponent.getDataAccessPane(), constructedPane);
        assertSame(dataAccessViewTopComponent.createContent(), constructedPane);
        
        assertNotNull(dataAccessViewTopComponent.getExecutorService());
        
        proxyUtilsMockedStatic.verify(() -> ProxyUtilities.setProxySelector(null));
        
        assertEquals(dataAccessViewTopComponent.createStyle(), "resources/data-access-view.css");
    }
    
    @Test
    public void handleNewGraph() {
        dataAccessViewTopComponent.componentOpened();

        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        doReturn(dataAccessPane).when(dataAccessViewTopComponent).getDataAccessPane();
        
        final Graph graph = mock(Graph.class);
        
        try (
                final MockedStatic<Platform> platformMockedStatic =
                        Mockito.mockStatic(Platform.class);
                final MockedStatic<DataAccessPreferencesIoProvider> dapIOProvMockedStatic =
                        Mockito.mockStatic(DataAccessPreferencesIoProvider.class);
        ) {
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer((iom) -> {
                        final Runnable runnable = iom.getArgument(0);
                        runnable.run();
                        return null;
                    });
            
            dataAccessViewTopComponent.handleNewGraph(graph);

            verify(dataAccessPane).update(graph);

            dapIOProvMockedStatic.verify(() -> DataAccessPreferencesIoProvider
                    .loadDataAccessState(dataAccessPane, graph));
        }
    }
    
    @Test
    public void handleNewGraph_data_access_view_not_open() {
        final Graph graph = mock(Graph.class);
        
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        doReturn(dataAccessPane).when(dataAccessViewTopComponent).getDataAccessPane();
        
        dataAccessViewTopComponent.handleNewGraph(graph);
        
        verify(dataAccessPane, never()).update(graph);
    }
    
    @Test
    public void componentShowing() {
        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        
        when(graphManager.getActiveGraph()).thenReturn(graph);
        
        doNothing().when(dataAccessViewTopComponent).handleNewGraph(graph);
        
        try (
                final MockedStatic<GraphManager> graphManagerMockedStatic =
                        Mockito.mockStatic(GraphManager.class);
        ) {
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            
            dataAccessViewTopComponent.componentShowing();
            
            
            verify(dataAccessViewTopComponent).handleNewGraph(graph);
        }
    }
    
    @Test
    public void handleComponentClosed() {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        doReturn(dataAccessPane).when(dataAccessViewTopComponent).getDataAccessPane();
        
        try (
                final MockedStatic<QualityControlAutoVetter> qualityControlAutoVetterMockedStatic =
                        Mockito.mockStatic(QualityControlAutoVetter.class);
        ) {
            final QualityControlAutoVetter instance = mock(QualityControlAutoVetter.class);
            qualityControlAutoVetterMockedStatic.when(QualityControlAutoVetter::getInstance).thenReturn(instance);
            
            dataAccessViewTopComponent.handleComponentClosed();
            
            verify(instance).removeObserver(dataAccessPane);
        }
    }
    
    @Test
    public void handleComponentOpened() {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        doReturn(dataAccessPane).when(dataAccessViewTopComponent).getDataAccessPane();
        
        try (
                final MockedStatic<QualityControlAutoVetter> qualityControlAutoVetterMockedStatic =
                        Mockito.mockStatic(QualityControlAutoVetter.class);
        ) {
            final QualityControlAutoVetter instance = mock(QualityControlAutoVetter.class);
            qualityControlAutoVetterMockedStatic.when(QualityControlAutoVetter::getInstance).thenReturn(instance);
            
            dataAccessViewTopComponent.handleComponentOpened();
            
            verify(instance).addObserver(dataAccessPane);
        }
    }
}

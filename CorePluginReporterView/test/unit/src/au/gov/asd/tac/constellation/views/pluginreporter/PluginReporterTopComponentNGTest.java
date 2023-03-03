/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.pluginreporter;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for PluginReporterTopComponent
 * 
 * @author Delphinus8821
 */
public class PluginReporterTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(PluginReporterTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of handleComponentOpened method, of class PluginReporterTopComponent.
     */
    @Test
    public void testHandleComponentOpened() {
        System.out.println("handleComponentOpened");
        PluginReporterTopComponent instance = mock(PluginReporterTopComponent.class);

        doCallRealMethod().when(instance).handleComponentOpened();
        doNothing().when(instance).handleNewGraph(any(Graph.class));

        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph activeGraph = mock(Graph.class);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(activeGraph);

            instance.handleComponentOpened();

            verify(instance).handleNewGraph(activeGraph);
        }
    }

    /**
     * Test of componentShowing method, of class PluginReporterTopComponent.
     */
    @Test
    public void testComponentShowing() {
        System.out.println("componentShowing");
        PluginReporterTopComponent instance = mock(PluginReporterTopComponent.class);
        
        doCallRealMethod().when(instance).componentShowing();
        doNothing().when(instance).handleNewGraph(any(Graph.class));
        
        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph activeGraph = mock(Graph.class);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(activeGraph);

            instance.componentShowing();

            verify(instance).handleNewGraph(activeGraph);
        }
    }

    /**
     * Test of createStyle method, of class PluginReporterTopComponent.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        PluginReporterTopComponent instance = mock(PluginReporterTopComponent.class);
        doCallRealMethod().when(instance).createStyle();

        assertEquals("resources/plugin-reporter.css", instance.createStyle());
    }
}

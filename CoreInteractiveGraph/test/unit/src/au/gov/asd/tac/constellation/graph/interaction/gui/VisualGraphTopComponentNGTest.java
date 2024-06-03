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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.SaveAsAction;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class VisualGraphTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(VisualGraphTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (Exception e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }
    }

    // THESE TESTS WORK CORRECTLY ON LOCAL MACHINE, ONLY PARTIALLY ONLINE
    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     */
    @Test
    public void testSaveGraphNotInMemory() throws Exception {
        System.out.println("saveGraph not in memeory");
        Platform.runLater(() -> {
            // Mock variables
            final GraphDataObject mockGDO = mock(GraphDataObject.class);
            when(mockGDO.isInMemory()).thenReturn(true);

            // Mock contruct save as action, GraphNode
            try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class);) {

                VisualGraphTopComponent instance = new VisualGraphTopComponent();
                instance.getGraphNode().setDataObject(mockGDO);
                instance.saveGraph();

                assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
                assertEquals(mockSaveAsAction.constructed().size(), 1);
                verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
                verify(mockSaveAsAction.constructed().get(0)).isSaved();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Caught exception in VisualGraphTopComponent test {0}", e);
            }
        });
    }

    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     */
    @Test
    public void testSaveGraphInvalid() throws Exception {
        System.out.println("saveGraph invalid");
        Platform.runLater(() -> {
            // Mock variables
            final GraphDataObject mockGDO = mock(GraphDataObject.class);
            when(mockGDO.isValid()).thenReturn(false);

            // Mock contruct save as action, GraphNode
            try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class);) {

                VisualGraphTopComponent instance = new VisualGraphTopComponent();
                instance.getGraphNode().setDataObject(mockGDO);
                instance.saveGraph();

                assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
                assertEquals(mockSaveAsAction.constructed().size(), 1);
                verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
                verify(mockSaveAsAction.constructed().get(0)).isSaved();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Caught exception in VisualGraphTopComponent test {0}", e);
            }
        });
    }
}

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
package au.gov.asd.tac.constellation.views.welcome;

import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for WelcomeTopComponent
 * 
 * @author Delphinus8821
 */
public class WelcomeTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(WelcomeTopComponentNGTest.class.getName());

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
     * Test of createStyle method, of class WelcomeTopComponent.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        final WelcomeTopComponent instance = mock(WelcomeTopComponent.class);
        doCallRealMethod().when(instance).createStyle();

        String expResult = "resources/light_welcome.css";

        assertEquals(instance.createStyle(), expResult);
    }
    
    /**
     * Test of createContent method, of class WelcomeTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");
        try (final MockedStatic recentFilesStatic = Mockito.mockStatic(RecentFiles.class)) {
            final List<RecentFiles.HistoryItem> mockList = new ArrayList<>();
            final String path = "C:Temp";
            final RecentFiles.HistoryItem mockItem = new RecentFiles.HistoryItem(0, path);
            mockList.add(mockItem);
            recentFilesStatic.when(RecentFiles::getUniqueRecentFiles).thenReturn(mockList);

            final WelcomeTopComponent instance = spy(WelcomeTopComponent.class);
            final WelcomeViewPane pane = instance.createContent();

            // check WelcomeTopComponent creates WelcomeViewPane
            assertNotNull(pane);
            // check that the WelcomeViewPane contains the HBox for recent files
            assertTrue(pane.getBottomRecentSection() instanceof HBox);

            pane.refreshRecentFiles();
            final Node firstChild = pane.getBottomRecentSection().getChildren().getFirst();
            // check that the flow pane has been created
            assertTrue(firstChild instanceof FlowPane);
            final FlowPane flowPane = (FlowPane) firstChild;
            assertTrue(flowPane.getChildren().getFirst() instanceof Button);
            final Button firstButton = (Button) flowPane.getChildren().getFirst();
            // check that the first button in the recent files list is the mocked filename
            assertTrue(firstButton.getText().equals(path));
        }
    }
}

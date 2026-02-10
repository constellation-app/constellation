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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WelcomeViewPane
 * 
 * @author Delphinus8821
 */
public class WelcomeViewPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(WelcomeViewPaneNGTest.class.getName());

    private WelcomeViewPane welcomePane;

    @BeforeClass
    public static void setUpClass() throws Exception {
        final WelcomeStartup ws = new WelcomeStartup();
        ws.run();
        RecentFiles.init();
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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        welcomePane = spy(new WelcomeViewPane());
    }

    /**
     * Test of setButtonProps method, of class WelcomeViewPane.
     */
    @Test
    public void testSetButtonProps() {
        System.out.println("setButtonProps");
        final Button button = mock(Button.class);
        
        welcomePane.setButtonProps(button);

        verify(button).setMaxSize(150, 150);
        verify(button).setContentDisplay(ContentDisplay.TOP);
    }

    /**
     * Test of createRecentButtons method, of class WelcomeViewPane.
     */
    @Test
    public void testCreateRecentButtons() {
        System.out.println("createRecentButtons");
        final Button button = mock(Button.class);

        welcomePane.createRecentButtons(button);

        verify(button).setMaxSize(175, 175);
        verify(button).setId("recent-button");
        verify(button).setContentDisplay(ContentDisplay.TOP);
    }

    /**
     * Test of setInfoButtons method, of class WelcomeViewPane.
     */
    @Test
    public void testSetInfoButtons() {
        System.out.println("setInfoButtons");
        final Button button = mock(Button.class);
        
        welcomePane.setInfoButtons(button);

        verify(button).setMaxSize(310, 50);
        verify(button).setStyle("-fx-background-color: transparent;");
        verify(button).setAlignment(Pos.CENTER_LEFT);
    }
    
    /**
     * Test of createRecentButtons method, of class WelcomeViewPane.
     */
    @Test
    public void testRefreshRecentFiles() {
        System.out.println("refreshRecentFiles");
        final FlowPane flowPaneMock = mock(FlowPane.class);
        final WelcomeViewPane welcomeViewPaneMock = spy(WelcomeViewPane.class);
        final ObservableList mockList = mock(ObservableList.class);
        final HBox bottomHBoxMock = mock(HBox.class);
                
        when(welcomeViewPaneMock.getBottomRecentSection()).thenReturn(bottomHBoxMock);
        when(welcomeViewPaneMock.getBottomRecentSection().getChildren()).thenReturn(mockList);
        welcomeViewPaneMock.refreshRecentFiles();
        // getBottomRecentSection.getChildren() called 3x in refreshRecentFiles()
        verify(welcomeViewPaneMock.getBottomRecentSection(), times(3)).getChildren();        
        verify(welcomeViewPaneMock.getBottomRecentSection().getChildren(), times(1)).remove(0);
        verify(welcomeViewPaneMock.getBottomRecentSection().getChildren(), times(1)).add(Mockito.any());
    }
}

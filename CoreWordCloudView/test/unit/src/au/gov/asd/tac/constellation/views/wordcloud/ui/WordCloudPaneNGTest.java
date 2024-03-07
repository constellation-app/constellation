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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Hyperlink;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WordCloudPane
 * 
 * @author Delphinus8821
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class WordCloudPaneNGTest extends ConstellationTest {

    private static final Logger LOGGER = Logger.getLogger(WordCloudPaneNGTest.class.getName());
    private final WordCloudTopComponent topComponent = mock(WordCloudTopComponent.class);

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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setInProgress method, of class WordCloudPane.
     */
    @Test
    public void testSetInProgress() {
        System.out.println("setInProgress");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            
            instance.setInProgress();
            assertTrue(instance.getCloudStackPane().getChildren().contains(instance.getSpinner()));
        }
    }

    /**
     * Test of setProgressComplete method, of class WordCloudPane.
     */
    @Test
    public void testSetProgressComplete() {
        System.out.println("setProgressComplete");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            instance.setInProgress();
            instance.setProgressComplete();
            assertFalse(instance.getCloudStackPane().getChildren().contains(instance.getSpinner()));
        }
    }

    /**
     * Test of enableTheCloud method, of class WordCloudPane.
     */
    @Test
    public void testEnableTheCloud() {
        System.out.println("enableTheCloud");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            boolean unionButtonSelected = true;
            boolean frequencyButtonSelected = false;
            boolean hasSignificances = false;
            instance.getTheCloud().getChildren().add(instance.getSliderBar());
            
            instance.enableTheCloud(unionButtonSelected, frequencyButtonSelected, hasSignificances);
            assertFalse(instance.getTheCloud().getChildren().contains(instance.getSliderBar()));
            
            unionButtonSelected = false;
            frequencyButtonSelected = true;
            hasSignificances = true;
            instance.enableTheCloud(unionButtonSelected, frequencyButtonSelected, hasSignificances);
            assertTrue(instance.getTheCloud().getChildren().contains(instance.getSliderBar()));
        }
    }

    /**
     * Test of disableTheCloud method, of class WordCloudPane.
     */
    @Test
    public void testDisableTheCloud() {
        System.out.println("disableTheCloud");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            instance.disableTheCloud();
            assertFalse(instance.getTheCloud().isVisible());
        }
    }

    /**
     * Test of createWords method, of class WordCloudPane.
     */
    @Test
    public void testCreateWords() {
        System.out.println("createWords");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            final SortedMap<String, Float> wordListWithSizes = new TreeMap<>();
            wordListWithSizes.put("Node", (float) 2);
            wordListWithSizes.put("Edge", (float) 4);
            wordListWithSizes.put("Link", (float) 7);
            wordListWithSizes.put("Transaction", (float) 1);
            final String queryInfo = "Phrase length 1";
            final int baseFontSize = 12;
            instance.createWords(wordListWithSizes, queryInfo, baseFontSize);
            
            final Map<String, Hyperlink> result = instance.getWordButtons();
            assertTrue(result.size() == 4);
        }
    }

    /**
     * Test of updateSelection method, of class WordCloudPane.
     */
    @Test
    public void testUpdateSelection() {
        System.out.println("updateSelection");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            
            final SortedMap<String, Float> wordListWithSizes = new TreeMap<>();
            wordListWithSizes.put("Node", (float) 2);
            wordListWithSizes.put("Edge", (float) 4);
            wordListWithSizes.put("Link", (float) 7);
            wordListWithSizes.put("Transaction", (float) 1);
            final Set<String> selectedWords = new HashSet<>();
            selectedWords.add("Link");
            selectedWords.add("Node");
            
            // Add words to be selected
            final String queryInfo = "Phrase length 1";
            final int baseFontSize = 12;
            instance.createWords(wordListWithSizes, queryInfo, baseFontSize);
            
            // Select only 2 words
            instance.updateSelection(selectedWords);
            final Map<String, Hyperlink> result = instance.getWordButtons();
            assertTrue(result.get("Link").isVisited());
            assertTrue(result.get("Node").isVisited());
            assertFalse(result.get("Edge").isVisited());
            assertFalse(result.get("Transaction").isVisited());
        }
    }

    /**
     * Test of updateWords method, of class WordCloudPane.
     */
    @Test
    public void testUpdateWords() {
        System.out.println("updateWords");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane instance = new WordCloudPane(controller);
            
            // Add words to be selected
            final SortedSet<String> wordsToDisplay = new TreeSet<>();
            wordsToDisplay.add("Node");
            wordsToDisplay.add("Link");
            
            // Create word buttons 
            final SortedMap<String, Float> wordListWithSizes = new TreeMap<>();
            wordListWithSizes.put("Node", (float) 2);
            wordListWithSizes.put("Edge", (float) 4);
            wordListWithSizes.put("Link", (float) 7);
            wordListWithSizes.put("Transaction", (float) 1);    
            final String queryInfo = "Phrase length 1";
            final int baseFontSize = 12;
            instance.createWords(wordListWithSizes, queryInfo, baseFontSize);
            
            // Update words 
            final boolean reapplySort = true;
            instance.updateWords(wordsToDisplay, reapplySort);
            assertTrue(instance.getWords().getChildren().size() == 2);
        }
    }
}

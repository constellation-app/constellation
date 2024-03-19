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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.wordcloud.content.PhraseTokenHandler;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WordCloudController
 * 
 * @author Delphinus8821
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class WordCloudControllerNGTest extends ConstellationTest {

    private static final Logger LOGGER = Logger.getLogger(WordCloudControllerNGTest.class.getName());
    private final WordCloudTopComponent topComponent = mock(WordCloudTopComponent.class);
    private final Graph graph = mock(Graph.class);
    private final ReadableGraph rg = mock(ReadableGraph.class);

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
     * Test of setIsSizeSorted method, of class WordCloudController.
     */
    @Test
    public void testSetIsSizeSorted() {
        System.out.println("setIsSizeSorted");
        when(graph.getReadableGraph()).thenReturn(rg);
        
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final PhraseTokenHandler handler = new PhraseTokenHandler();
            final WordCloud wordCloud = new WordCloud(handler, GraphElementType.TRANSACTION, 1, true);
            final int cloudAttr = 2;
            when(rg.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR)).thenReturn(cloudAttr);
            when(rg.getObjectValue(cloudAttr, 0)).thenReturn(wordCloud);
            doNothing().when(controller).updateWordsOnPane();
            doNothing().when(controller).setAttributeSelectionEnabled(Mockito.anyBoolean());
            controller.updateActiveGraph(graph);
            
            final boolean val = false;
            controller.setIsSizeSorted(val);
            assertFalse(wordCloud.getIsSizeSorted());
           
        }
    }

    /**
     * Test of setSignificance method, of class WordCloudController.
     */
    @Test
    public void testSetSignificance() {
        System.out.println("setSignificance");
        when(graph.getReadableGraph()).thenReturn(rg);

        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final PhraseTokenHandler handler = new PhraseTokenHandler();
            final WordCloud wordCloud = new WordCloud(handler, GraphElementType.TRANSACTION, 1, true);
            final int cloudAttr = 2;
            when(rg.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR)).thenReturn(cloudAttr);
            when(rg.getObjectValue(cloudAttr, 0)).thenReturn(wordCloud);
            doNothing().when(controller).setAttributeSelectionEnabled(Mockito.anyBoolean());
            doNothing().when(controller).updateSignificanceOnPane();
            controller.updateActiveGraph(graph);

            final double significance = 0.2;
            controller.setSignificance(significance);
            assertEquals(significance, wordCloud.getCurrentSignificance());
        }
    }

    /**
     * Test of setIsUnionSelect method, of class WordCloudController.
     */
    @Test
    public void testSetIsUnionSelect() {
        System.out.println("setIsUnionSelect");
        when(graph.getReadableGraph()).thenReturn(rg);

        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final PhraseTokenHandler handler = new PhraseTokenHandler();
            final WordCloud wordCloud = new WordCloud(handler, GraphElementType.TRANSACTION, 1, true);
            final int cloudAttr = 2;
            when(rg.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR)).thenReturn(cloudAttr);
            when(rg.getObjectValue(cloudAttr, 0)).thenReturn(wordCloud);
            doNothing().when(controller).setAttributeSelectionEnabled(Mockito.anyBoolean());
            controller.updateActiveGraph(graph);
            
            final boolean val = true;
            controller.setIsUnionSelect(val);
            assertTrue(wordCloud.getIsUnionSelect());
        }
    }

    /**
     * Test of alterSelection method, of class WordCloudController.
     */
    @Test
    public void testAlterSelection() {
        System.out.println("alterSelection");
        when(graph.getReadableGraph()).thenReturn(rg);

        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final PhraseTokenHandler handler = new PhraseTokenHandler();
            final WordCloud wordCloud = new WordCloud(handler, GraphElementType.TRANSACTION, 1, true);
            final int cloudAttr = 2;
            when(rg.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR)).thenReturn(cloudAttr);
            when(rg.getObjectValue(cloudAttr, 0)).thenReturn(wordCloud);
            final String word = "";
            final boolean accumulativeSelection = false;
            final boolean deselect = true;
            doNothing().when(controller).selectElements();
            doNothing().when(controller).updateSelectedWordsOnPane();
            doNothing().when(controller).setAttributeSelectionEnabled(Mockito.anyBoolean());
            controller.updateActiveGraph(graph);
            controller.alterSelection(word, accumulativeSelection, deselect);
            verify(controller, times(1)).updateSelectedWordsOnPane();
            verify(controller, times(1)).selectElements();
        }
    }
}

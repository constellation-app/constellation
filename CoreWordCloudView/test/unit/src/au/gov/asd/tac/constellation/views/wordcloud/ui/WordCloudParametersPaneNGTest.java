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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis.PhrasiphyContentParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WordCloudParameterPane 
 * 
 * @author Delphinus8821
 */
public class WordCloudParametersPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(WordCloudParametersPaneNGTest.class.getName());
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of updateParameters method, of class WordCloudParametersPane.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("updateParameters");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane pane = new WordCloudPane(controller);
            
            final List<String> nodeAttributes = new ArrayList<>();
            final List<String> transAttributes = new ArrayList<>();
            nodeAttributes.add(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES);
            transAttributes.add(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS);
            
            final WordCloudParametersPane instance = new WordCloudParametersPane(pane);
            instance.updateParameters(nodeAttributes, transAttributes);
            
            assertTrue(instance.getNodeAttributes().contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES));
            assertTrue(instance.getTransAttributes().contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS));
        }
    }

    /**
     * Test of validityChanged method, of class WordCloudParametersPane.
     */
    @Test
    public void testValidityChanged() {
        System.out.println("validityChanged");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane pane = new WordCloudPane(controller);
            final boolean enabled = false;
            final WordCloudParametersPane instance = new WordCloudParametersPane(pane);
            instance.validityChanged(enabled);
            assertTrue(instance.getRun().isDisable());
        }
    }

    /**
     * Test of setAttributeSelectionEnabled method, of class WordCloudParametersPane.
     */
    @Test
    public void testSetAttributeSelectionEnabled() {
        System.out.println("setAttributeSelectionEnabled");
        try (final MockedStatic<WordCloudController> controllerStatic = Mockito.mockStatic(WordCloudController.class)) {
            final WordCloudController controller = spy(WordCloudController.class);
            controllerStatic.when(WordCloudController::getDefault).thenReturn(controller);
            when(controller.init(topComponent)).thenReturn(controller);
            final WordCloudPane pane = new WordCloudPane(controller);
            final boolean val = true;
            final WordCloudParametersPane instance = new WordCloudParametersPane(pane);
            instance.setAttributeSelectionEnabled(val);
            assertTrue(instance.getParams().getParameters().get(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID).isEnabled());  
        }
    }    
}

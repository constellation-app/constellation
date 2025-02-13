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
package au.gov.asd.tac.constellation.views.analyticview.visualisation;

import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewController;
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractColorTranslator;
import au.gov.asd.tac.constellation.views.analyticview.translators.AnalyticTranslator;
import au.gov.asd.tac.constellation.views.analyticview.translators.ScoreToColorTranslator;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ColorVisualisation
 * 
 * @author Delphinus8821
 */
public class ColorVisualisationNGTest {

    private static final Logger LOGGER = Logger.getLogger(ColorVisualisationNGTest.class.getName());
    
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
     * Test of deactivate method, of class ColorVisualisation.
     */
    @Test
    public void testDeactivate() {
        System.out.println("deactivate");
        
        try (final MockedStatic<AnalyticViewController> controllerStatic = Mockito.mockStatic(AnalyticViewController.class)) {
            final AnalyticViewController controller = spy(AnalyticViewController.class);
            controllerStatic.when(AnalyticViewController::getDefault).thenReturn(controller);
            final boolean reset = true;
            final AbstractColorTranslator translator = new ScoreToColorTranslator();
            final ColorVisualisation instance = new ColorVisualisation(translator);
            instance.deactivate(reset);
            
            final boolean isActive = instance.isActive();
            assertFalse(isActive);
            verify(controller).updateGraphVisualisations(Mockito.any(), Mockito.anyBoolean());
        }
    }

    /**
     * Test of getName method, of class ColorVisualisation.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        
        final AbstractColorTranslator translator = new ScoreToColorTranslator();
        final ColorVisualisation instance = new ColorVisualisation(translator);
        final String expResult = "Color Elements";
        final String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getTranslator method, of class ColorVisualisation.
     */
    @Test
    public void testGetTranslator() {
        System.out.println("getTranslator");
        
        final AbstractColorTranslator translator = new ScoreToColorTranslator();
        final ColorVisualisation instance = new ColorVisualisation(translator);
        final AnalyticTranslator result = instance.getTranslator();
        assertEquals(result, translator);
    }

    /**
     * Test of getAffectedAttributes method, of class ColorVisualisation.
     */
    @Test
    public void testGetAffectedAttributes() {
        System.out.println("getAffectedAttributes");
        
        final AbstractColorTranslator translator = new ScoreToColorTranslator();
        final ColorVisualisation instance = new ColorVisualisation(translator);
        final List expResult = Arrays.asList(
                VisualConcept.VertexAttribute.OVERLAY_COLOR,
                VisualConcept.TransactionAttribute.OVERLAY_COLOR,
                VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE,
                VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE);
        final List result = instance.getAffectedAttributes();
        assertEquals(result, expResult);
    }

    /**
     * Test of isActive method, of class ColorVisualisation.
     */
    @Test
    public void testIsActive() {
        System.out.println("isActive");
        
        final AbstractColorTranslator translator = new ScoreToColorTranslator();
        final ColorVisualisation instance = new ColorVisualisation(translator);
        final boolean expResult = false;
        final boolean result = instance.isActive();
        assertEquals(result, expResult);
    }

    /**
     * Test of setSelected method, of class ColorVisualisation.
     */
    @Test
    public void testSetSelected() {
        System.out.println("setSelected");
        
        final boolean selected = false;
        final AbstractColorTranslator translator = new ScoreToColorTranslator();
        final ColorVisualisation instance = new ColorVisualisation(translator);
        instance.setSelected(selected);
        final boolean result = instance.isActive();
        assertEquals(result, selected);
    }    
}

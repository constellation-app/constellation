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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.SizeVisualisation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ScoreToSizeTranslator 
 * 
 * @author Delphinus8821
 */
public class ScoreToSizeTranslatorNGTest {

    private static final Logger LOGGER = Logger.getLogger(ScoreToSizeTranslatorNGTest.class.getName());
    
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
     * Test of getName method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final String expResult = "Multi-Score -> Size Visualisation";
        final String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getResultType method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testGetResultType() {
        System.out.println("getResultType");
        
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final Class expResult = ScoreResult.class;
        final Class result = instance.getResultType();
        assertEquals(result, expResult);
    }

    /**
     * Test of buildControl method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testBuildControl() {
        System.out.println("buildControl");
        
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final SizeVisualisation expResult = new SizeVisualisation(instance);
        final SizeVisualisation result = instance.buildControl();
        assertEquals(result, expResult);
    }

    /**
     * Test of setVertexSizes method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testSetVertexSizes() {
        System.out.println("setVertexSizes");
        
        final Map<Integer, Float> sizes = new HashMap<>();
        sizes.put(25, 2.0F);
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        instance.setVertexSizes(sizes);
        final Map<Integer, Float> result = instance.getVertexSizes();
        assertEquals(result, sizes);
    }

    /**
     * Test of setTransactionSizes method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testSetTransactionSizes() {
        System.out.println("setTransactionSizes");
        
        final Map<Integer, Float> sizes = new HashMap<>();
        sizes.put(25, 2.0F);
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        instance.setTransactionSizes(sizes);
        final Map<Integer, Float> result = instance.getTransactionSizes();
        assertEquals(result, sizes);
    }
}

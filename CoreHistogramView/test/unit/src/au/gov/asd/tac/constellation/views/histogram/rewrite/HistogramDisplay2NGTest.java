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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.logging.Level;
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
public class HistogramDisplay2NGTest {

    private static final Logger LOGGER = Logger.getLogger(HistogramDisplay2NGTest.class.getName());

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
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of setBinCollection method, of class HistogramDisplay2.
     */
    @Test
    public void testSetBinCollection() {
        System.out.println("setBinCollection");
        final BinCollection binCollection = mock(BinCollection.class);
        final Bin[] bins = new Bin[0];
        final BinIconMode binIconMode = BinIconMode.NONE;

        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        when(binCollection.getBins()).thenReturn(bins);

        instance.setBinSelectionMode(BinSelectionMode.ADD_TO_SELECTION);
        instance.setBinCollection(binCollection, binIconMode);

        assertEquals(instance.getBinCollection(), binCollection);
        assertEquals(instance.getBinIconMode(), binIconMode);
    }

    /**
     * Test of updateBinCollection method, of class HistogramDisplay2.
     */
    @Test
    public void testUpdateBinCollection() {
        System.out.println("updateBinCollection");
        final BinCollection binCollection = mock(BinCollection.class);
        final Bin[] bins = new Bin[0];
        final BinIconMode binIconMode = BinIconMode.NONE;
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        when(binCollection.getBins()).thenReturn(bins);

        instance.setBinSelectionMode(BinSelectionMode.ADD_TO_SELECTION);
        instance.setBinCollection(binCollection, binIconMode);

        instance.updateBinCollection();

        verify(binCollection).deactivateBins();
    }

    /**
     * Test of setBinSelectionMode method, of class HistogramDisplay2.
     */
    @Test
    public void testSetBinSelectionMode() {
        System.out.println("setBinSelectionMode");
        final BinSelectionMode binSelectionMode = BinSelectionMode.ADD_TO_SELECTION;
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        instance.setBinSelectionMode(binSelectionMode);

        assertEquals(instance.getBinSelectionMode(), binSelectionMode);
    }

    /**
     * Test of updateDisplay method, of class HistogramDisplay2.
     */
//    @Test
//    public void testUpdateDisplay() {
//        System.out.println("updateDisplay");
//        HistogramDisplay2 instance = null;
//        instance.updateDisplay();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of decreaseBarHeight method, of class HistogramDisplay2.
     */
//    @Test
//    public void testDecreaseBarHeight() {
//        System.out.println("decreaseBarHeight");
//        HistogramDisplay2 instance = null;
//        instance.decreaseBarHeight();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of increaseBarHeight method, of class HistogramDisplay2.
     */
//    @Test
//    public void testIncreaseBarHeight() {
//        System.out.println("increaseBarHeight");
//        HistogramDisplay2 instance = null;
//        instance.increaseBarHeight();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}

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
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import au.gov.asd.tac.constellation.views.histogram.HistogramState;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import javafx.util.Pair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class HistogramPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(HistogramPaneNGTest.class.getName());

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
     * Test of decreaseBarHeight method, of class HistogramPane.
     */
    @Test
    public void testDecreaseBarHeight() {
        System.out.println("decreaseBarHeight");

        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));
        final HistogramDisplay2 display = instance.getDisplay();
        final int prevValue = display.getBarHeightBase();

        instance.decreaseBarHeight();
        assertTrue(prevValue > display.getBarHeightBase());
    }

    /**
     * Test of increaseBarHeight method, of class HistogramPane.
     */
    @Test
    public void testIncreaseBarHeight() {
        System.out.println("increaseBarHeight");

        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));
        final HistogramDisplay2 display = instance.getDisplay();
        final int prevValue = display.getBarHeightBase();

        instance.increaseBarHeight();
        assertTrue(prevValue < display.getBarHeightBase());
    }

    /**
     * Test of setBinCollection method, of class HistogramPane.
     */
    @Test
    public void testSetBinCollection() {
        System.out.println("setBinCollection");
        final BinCollection binCollection = mock(BinCollection.class);
        final BinIconMode binIconMode = BinIconMode.ICON;

        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));
        instance.setBinCollection(binCollection, binIconMode);

        final HistogramDisplay2 display = instance.getDisplay();
        assertEquals(binCollection, display.getBinCollection());
        assertEquals(binIconMode, display.getBinIconMode());
    }

    /**
     * Test of setBinSelectionMode method, of class HistogramPane.
     */
    @Test
    public void testSetBinSelectionMode() {
        System.out.println("setBinSelectionMode");
        final BinSelectionMode binSelectionMode = BinSelectionMode.FREE_SELECTION;

        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));
        instance.setBinSelectionMode(binSelectionMode);

        final HistogramDisplay2 display = instance.getDisplay();
        assertEquals(binSelectionMode, display.getBinSelectionMode());
    }

    /**
     * Test of updateBinCollection method, of class HistogramPane.
     */
    @Test
    public void testUpdateBinCollection() {
        System.out.println("updateBinCollection");

        // Mocks
        final BinCollection mockBinCollection = mock(BinCollection.class);
        final Bin[] mockBins = {new IntBin(), new IntBin(), new IntBin()};
        when(mockBinCollection.getBins()).thenReturn(mockBins);
        final BinIconMode binIconMode = BinIconMode.ICON;

        // Instance
        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));
        instance.setBinCollection(mockBinCollection, binIconMode);
        instance.updateBinCollection();

        // Assert funciton was run correctly
        final HistogramDisplay2 display = instance.getDisplay();
        final BinCollection binCollection = display.getBinCollection();
        final Bin[] bins = binCollection.getBins();

        for (final Bin bin : bins) {
            assertFalse(bin.getIsActivated());
        }
    }

    /**
     * Test of setHistogramState method, of class HistogramPane.
     */
    @Test
    public void testSetHistogramState() {
        System.out.println("setHistogramState");
        final HistogramState histogramState = new HistogramState();
        final Map<String, BinCreator> attributes = new HashMap<>();
        final HistogramPane instance = new HistogramPane(mock(HistogramTopComponent2.class));

        instance.setHistogramState(histogramState, attributes);
    }

    private Pair<HistogramTopComponent2, HistogramPane> createPanehelper() {
        final HistogramTopComponent2 mockTopComponent = mock(HistogramTopComponent2.class);

        // Set up instance
        final HistogramPane spy = spy(new HistogramPane(mockTopComponent));
        // Stub this method, so variable "isAdjusting" is not set to true
        Mockito.doNothing().when(spy).setHistogramState(any(), any());

        return new Pair<>(mockTopComponent, spy);
    }

    /**
     * Test of saveBinsToGraph method, of class HistogramPane.
     */
    @Test
    public void testSaveBinsToGraph() {
        System.out.println("saveBinsToGraph");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();

        instance.saveBinsToGraph();
        verify(mockTopComponent).saveBinsToGraph();
    }

    /**
     * Test of updateBinComparator method, of class HistogramPane.
     */
    @Test
    public void testUpdateBinComparator() {
        System.out.println("updateBinComparator");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();

        instance.updateBinComparator();
        verify(mockTopComponent).setBinComparator(any());
    }
}

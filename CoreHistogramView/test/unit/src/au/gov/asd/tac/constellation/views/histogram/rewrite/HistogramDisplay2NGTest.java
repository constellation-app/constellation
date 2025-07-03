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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.awt.Color;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
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
    @Test
    public void testUpdateDisplay() {
        System.out.println("updateDisplay");

        final ObjectBin mockBin = mock(ObjectBin.class);
        final ConstellationColor mockKey = mock(ConstellationColor.class);
        when(mockBin.getKeyAsObject()).thenReturn(mockKey);
        when(mockKey.getJavaColor()).thenReturn(Color.RED);

        final BinCollection binCollection = mock(BinCollection.class);
        final Bin[] bins = {mockBin};
        final BinIconMode binIconMode = BinIconMode.COLOR;
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        when(binCollection.getBins()).thenReturn(bins);
        when(binCollection.getMaxElementCount()).thenReturn(1);
        when(binCollection.getSelectedBins()).thenReturn(bins);

        instance.setBinSelectionMode(BinSelectionMode.ADD_TO_SELECTION);
        instance.setBinCollection(binCollection, binIconMode);

        instance.updateBinCollection();

        instance.updateDisplay();

        verify(binCollection, atLeast(1)).getBins();
        verify(binCollection, atLeast(1)).getMaxElementCount();
        verify(binCollection, atLeast(1)).getSelectedBins();
    }

    /**
     * Test of decreaseBarHeight method, of class HistogramDisplay2.
     */
    @Test
    public void testDecreaseBarHeight() {
        System.out.println("decreaseBarHeight");
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        final int oldBarHeight = instance.getBarHeightBase();
        instance.decreaseBarHeight();

        assertTrue(oldBarHeight > instance.getBarHeightBase());
    }

    /**
     * Test of increaseBarHeight method, of class HistogramDisplay2.
     */
    @Test
    public void testIncreaseBarHeight() {
        System.out.println("increaseBarHeight");
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));

        final int oldBarHeight = instance.getBarHeightBase();
        instance.increaseBarHeight();

        assertTrue(oldBarHeight < instance.getBarHeightBase());
    }

    /**
     * Test of handleMouseClicked method, of class HistogramDisplay2.
     */
    @Test
    public void testHandleMouseClicked() {
        System.out.println("handleMouseClicked");

        // Mocks
        final BinCollection binCollection = mock(BinCollection.class);
        final Bin[] bins = new Bin[0];
        final BinIconMode binIconMode = BinIconMode.NONE;
        when(binCollection.getBins()).thenReturn(bins);

        // Mouse event mock
        final MouseEvent e = mock(MouseEvent.class);
        when(e.getButton()).thenReturn(MouseButton.SECONDARY);

        final BinSelectionMode binSelectionMode = BinSelectionMode.ADD_TO_SELECTION;

        final ObservableList mockItems = mock(ObservableList.class);

        try (final MockedConstruction<ContextMenu> mockConstructor = Mockito.mockConstruction(ContextMenu.class, (mock, context) -> {
            when(mock.getItems()).thenReturn(mockItems);
        })) {

            // Set up instance
            final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));
            instance.setBinSelectionMode(binSelectionMode);
            instance.setBinCollection(binCollection, binIconMode);

            // Run function
            instance.handleMouseClicked(e);

            // Verify functions were called
            verify(e).getButton();

            // Assert context menu was made and function was called
            assertTrue(!mockConstructor.constructed().isEmpty());
            final ContextMenu menu = mockConstructor.constructed().getLast();
            verify(menu).show(any(Node.class), anyDouble(), anyDouble());
        }
    }

    /**
     * Test of handleMousePressed method, of class HistogramDisplay2.
     */
    @Test

    public void testHandleMousePressed() {
        System.out.println("handleMousePressed");

        // Mocks
        final BinCollection binCollection = mock(BinCollection.class);
        final Bin[] bins = new Bin[0];
        final BinIconMode binIconMode = BinIconMode.NONE;
        when(binCollection.getBins()).thenReturn(bins);

        // Mouse event mock
        final MouseEvent e = mock(MouseEvent.class);
        when(e.getButton()).thenReturn(MouseButton.PRIMARY);
        when(e.isShiftDown()).thenReturn(true);
        when(e.isControlDown()).thenReturn(true);

        final BinSelectionMode binSpy = spy(BinSelectionMode.ADD_TO_SELECTION);

        // Set up instance
        final HistogramDisplay2 instance = new HistogramDisplay2(mock(HistogramTopComponent2.class));
        instance.setBinSelectionMode(binSpy);
        instance.setBinCollection(binCollection, binIconMode);

        // Run function
        instance.handleMousePressed(e);

        // Verify functions were called
        verify(e).getButton();
        verify(binSpy).mousePressed(e.isShiftDown(), e.isControlDown(), binCollection.getBins(), -1, -1);
    }

}

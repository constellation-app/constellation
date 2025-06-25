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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramTopComponent2;
import java.awt.Color;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class BinSelectionModeNGTest {

    /**
     * Test of mouseReleased method, of class BinSelectionMode.
     */
    @Test
    public void testMouseReleasedFreeSelection() {
        System.out.println("testMouseReleasedFreeSelection");
        final Bin[] bins = new Bin[1];
        final int dragStart = 0;
        final int dragEnd = 1;
        final HistogramTopComponent2 topComponent = mock(HistogramTopComponent2.class);
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;

        final int firstBar = Math.max(0, Math.min(dragStart, dragEnd));
        final int lastBar = Math.min(bins.length - 1, Math.max(dragStart, dragEnd));

        instance.mouseReleased(false, false, bins, dragStart, dragEnd, topComponent);
        verify(topComponent).selectOnlyBins(firstBar, lastBar);

        instance.mouseReleased(false, true, bins, dragStart, dragEnd, topComponent);
        verify(topComponent).completeBins(firstBar, lastBar);

        instance.mouseReleased(true, false, bins, dragStart, dragEnd, topComponent);
        verify(topComponent).selectBins(firstBar, lastBar, true);
    }

    /**
     * Test of toString method, of class BinSelectionMode.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;
        final String expResult = "Free Selection";
        final String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of getBarColor method, of class BinSelectionMode.
     */
    @Test
    public void testGetBarColor() {
        System.out.println("getBarColor");
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;
        final Color expResult = HistogramDisplay.BAR_COLOR;
        final Color result = instance.getBarColor();
        assertEquals(result, expResult);
    }

    /**
     * Test of getActivatedBarColor method, of class BinSelectionMode.
     */
    @Test
    public void testGetActivatedBarColor() {
        System.out.println("getActivatedBarColor");
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;
        final Color expResult = HistogramDisplay.BAR_COLOR;
        final Color result = instance.getActivatedBarColor();
        assertEquals(result, expResult);
    }

    /**
     * Test of getSelectedColor method, of class BinSelectionMode.
     */
    @Test
    public void testGetSelectedColor() {
        System.out.println("getSelectedColor");
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;
        final Color expResult = HistogramDisplay.SELECTED_COLOR;
        final Color result = instance.getSelectedColor();
        assertEquals(result, expResult);
    }

    /**
     * Test of getActivatedSelectedColor method, of class BinSelectionMode.
     */
    @Test
    public void testGetActivatedSelectedColor() {
        System.out.println("getActivatedSelectedColor");
        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;
        final Color expResult = HistogramDisplay.SELECTED_COLOR;
        final Color result = instance.getActivatedSelectedColor();
        assertEquals(result, expResult);
    }

}

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

import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramDisplay2;
import au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramTopComponent2;
import javafx.scene.paint.Color;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class BinSelectionModeNGTest {

    /**
     * Test of mousePressed method, of class BinSelectionMode.
     */
    @Test
    public void testMousePressedFreeSelection() {
        System.out.println("testMousePressedFreeSelection");

        final int elementCount = 4;

        final int dragStart = 0;
        final int dragEnd = 1;

        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;

        final int numTests = 3;
        final boolean[] shiftDownArray = {false, false, true};
        final boolean[] controlDownArray = {false, true, false};

        final int[] expectedSelectedCountArray = {elementCount, 0, elementCount};
        final int[] expectedSavedSelectedCountArray = {0, 1, 1};

        for (int i = 0; i < numTests; i++) {
            final Bin bin = new StringBin();
            bin.setSelectedCount(1);
            bin.setElementCount(elementCount);

            instance.mousePressed(shiftDownArray[i], controlDownArray[i], new Bin[]{bin}, dragStart, dragEnd);

            assertEquals(bin.getSelectedCount(), expectedSelectedCountArray[i]);
            assertEquals(bin.getSavedSelectedCount(), expectedSavedSelectedCountArray[i]);
            assertEquals(bin.getElementCount(), elementCount);
        }
    }

    /**
     * Test of mouseDragged method, of class BinSelectionMode.
     */
    @Test
    public void testMouseDraggedFreeSelection() {
        System.out.println("testMouseDraggedFreeSelection");

        final int elementCount = 4;

        final int dragStart = 0;
        final int oldDragEnd = 1;
        final int newDragEnd = 1;

        final BinSelectionMode instance = BinSelectionMode.FREE_SELECTION;

        final int numTests = 3;
        final boolean[] shiftDownArray = {false, false, true};
        final boolean[] controlDownArray = {false, true, false};

        final int expectedSavedSelectedCount = 0;

        for (int i = 0; i < numTests; i++) {
            final Bin bin = new StringBin();
            bin.setSelectedCount(1);
            bin.setElementCount(elementCount);

            instance.mouseDragged(shiftDownArray[i], controlDownArray[i], new Bin[]{bin}, dragStart, oldDragEnd, newDragEnd);

            assertEquals(bin.getSelectedCount(), elementCount);
            assertEquals(bin.getSavedSelectedCount(), expectedSavedSelectedCount);
            assertEquals(bin.getElementCount(), elementCount);
        }
    }

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

    // WITHIN SELECTION
    /**
     * Test of mousePressed method, of class BinSelectionMode.
     */
    @Test
    public void testMousePressedWithinSelection() {
        System.out.println("testMousePressedWithinSelection");

        final int selectedCount = 1;
        final int elementCount = 4;

        final int dragStart = 0;
        final int dragEnd = 0;

        final BinSelectionMode instance = BinSelectionMode.WITHIN_SELECTION;

        final int numTests = 3;
        final boolean[] shiftDownArray = {false, false, true};
        final boolean[] controlDownArray = {false, true, false};

        for (int i = 0; i < numTests; i++) {
            final Bin bin1 = new StringBin();
            bin1.setSelectedCount(selectedCount);
            bin1.setElementCount(elementCount);

            final Bin bin2 = new StringBin();
            bin2.setSelectedCount(selectedCount);
            bin2.setElementCount(elementCount);

            instance.mousePressed(shiftDownArray[i], controlDownArray[i], new Bin[]{bin1, bin2}, dragStart, dragEnd);

            assertEquals(bin1.getIsActivated(), true);
            assertEquals(bin2.getIsActivated(), false);
        }
    }

    /**
     * Test of mouseDragged method, of class BinSelectionMode.
     */
    @Test
    public void testMouseDraggedWithinSelection() {
        System.out.println("testMouseDraggedWithinSelection");

        final int dragStart = 0;
        final int oldDragEnd = 1;
        final int newDragEnd = 1;

        final BinSelectionMode instance = BinSelectionMode.WITHIN_SELECTION;

        final int numTests = 3;
        final boolean[] shiftDownArray = {false, false, true};
        final boolean[] controlDownArray = {false, true, false};

        final boolean[] expectedActivatedArray = {true, false, true};

        for (int i = 0; i < numTests; i++) {
            final Bin bin1 = new StringBin();
            bin1.setIsActivated(false);
            bin1.setSavedActivated(false);

            final Bin bin2 = new StringBin();
            bin2.setIsActivated(true);
            bin2.setSavedActivated(true);

            instance.mouseDragged(shiftDownArray[i], controlDownArray[i], new Bin[]{bin1, bin2}, dragStart, oldDragEnd, newDragEnd);

            assertEquals(bin1.getIsActivated(), true);
            assertEquals(bin2.getIsActivated(), expectedActivatedArray[i]);
        }
    }

    /**
     * Test of mousePressed method, of class BinSelectionMode.
     */
    @Test
    public void testMousePressedAddToSelection() {
        System.out.println("testMousePressedAddToSelection");

        final int dragStart = 0;
        final int dragEnd = 1;

        final BinSelectionMode instance = BinSelectionMode.ADD_TO_SELECTION;

        final int numTests = 3;
        final boolean[] shiftDownArray = {false, false, true};
        final boolean[] controlDownArray = {false, true, false};

        final boolean[] expectedActivatedArray = {true, false, true};

        for (int i = 0; i < numTests; i++) {
            final Bin bin1 = new StringBin();
            bin1.setIsActivated(false);
            bin1.setSavedActivated(false);

            final Bin bin2 = new StringBin();
            bin2.setIsActivated(true);
            bin2.setSavedActivated(true);

            instance.mousePressed(shiftDownArray[i], controlDownArray[i], new Bin[]{bin1, bin2}, dragStart, dragEnd);

            assertEquals(bin1.getIsActivated(), true);
            assertEquals(bin2.getIsActivated(), expectedActivatedArray[i]);
        }
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
        final Color expResult = HistogramDisplay2.getBarColor();
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
        final Color expResult = HistogramDisplay2.getBarColor();
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
        final Color expResult = HistogramDisplay2.getSelectedColor();
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
        final Color expResult = HistogramDisplay2.getSelectedColor();
        final Color result = instance.getActivatedSelectedColor();
        assertEquals(result, expResult);
    }

}

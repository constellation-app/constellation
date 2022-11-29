/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import java.awt.Color;

/**
 * A BinSelectionMode represents the different ways the user can select bins in
 * the histogram.
 *
 * @author sirius
 */
public enum BinSelectionMode {

    /**
     * The selection on the graph is immediately updated as the user clicks on
     * bins in the histogram.
     */
    FREE_SELECTION("Free Selection", HistogramDisplay.BAR_COLOR, HistogramDisplay.BAR_COLOR, HistogramDisplay.SELECTED_COLOR, HistogramDisplay.SELECTED_COLOR) {
        @Override
        public void mousePressed(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd) {

            int firstBar = Math.max(0, Math.min(dragStart, dragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, dragEnd));

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (Bin bin : bins) {
                    bin.selectedCount = 0;
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedSelectedCount = bins[i].selectedCount;
                    bins[i].selectedCount = bins[i].selectedCount == 0 ? bins[i].elementCount : 0;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedSelectedCount = bins[i].selectedCount;
                    bins[i].selectedCount = bins[i].elementCount;
                }
            }
        }

        @Override
        public void mouseDragged(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int oldDragEnd, int newDragEnd) {
            int firstBar = Math.max(0, Math.min(dragStart, oldDragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, oldDragEnd));

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].selectedCount = bins[i].savedSelectedCount;
            }

            firstBar = Math.max(0, Math.min(dragStart, newDragEnd));
            lastBar = Math.min(bins.length - 1, Math.max(dragStart, newDragEnd));

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedSelectedCount = bins[i].selectedCount;
                    bins[i].selectedCount = bins[i].selectedCount == 0 ? bins[i].elementCount : 0;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedSelectedCount = bins[i].selectedCount;
                    bins[i].selectedCount = bins[i].elementCount;
                }
            }
        }

        @Override
        public void mouseReleased(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd, HistogramTopComponent topComponent) {
            int firstBar = Math.max(0, Math.min(dragStart, dragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, dragEnd));

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (controlDown) {
                topComponent.completeBins(firstBar, lastBar);
            } else if (shiftDown) {
                topComponent.selectBins(firstBar, lastBar, true);
            } else {
                topComponent.selectOnlyBins(firstBar, lastBar);
            }
        }

        @Override
        public void select(HistogramTopComponent topComponent) {
            // Intentionally left blank
        }
    },
    /**
     * Selections in the histogram are recorded as the user clicks and drags
     * over bins but is not reflected on the graph until the user chooses to
     * apply the selection. At this point the selection of the graph is
     * represents only those elements that are in the intersection of the
     * current and new selections.
     */
    WITHIN_SELECTION("Within Existing Selection", HistogramDisplay.BAR_COLOR, HistogramDisplay.BAR_COLOR, HistogramDisplay.SELECTED_COLOR, HistogramDisplay.ACTIVE_COLOR) {
        @Override
        public void mousePressed(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd) {

            int firstBar = Math.min(dragStart, dragEnd);
            int lastBar = Math.max(dragStart, dragEnd);

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (Bin bin : bins) {
                    bin.activated = false;
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = !bins[i].activated;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = true;
                }
            }
        }

        @Override
        public void mouseDragged(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int oldDragEnd, int newDragEnd) {
            int firstBar = Math.max(0, Math.min(dragStart, oldDragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, oldDragEnd));

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].activated = bins[i].savedActivated;
            }

            firstBar = Math.max(0, Math.min(dragStart, newDragEnd));
            lastBar = Math.min(bins.length - 1, Math.max(dragStart, newDragEnd));

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = !bins[i].activated;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = true;
                }
            }
        }

        @Override
        public void mouseReleased(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd, HistogramTopComponent topComponent) {
            // Do nothing
        }

        @Override
        public void select(HistogramTopComponent topComponent) {
            topComponent.filterSelection();
        }
    },
    /**
     * Selections in the histogram are recorded as the user clicks and drags
     * over bins but is not reflected on the graph until the user chooses to
     * apply the selection. At this point the selection of the graph is
     * represents only those elements that are in the union of the current and
     * new selections.
     */
    ADD_TO_SELECTION("Add To Existing Selection", HistogramDisplay.BAR_COLOR, HistogramDisplay.ACTIVE_COLOR, HistogramDisplay.SELECTED_COLOR, HistogramDisplay.SELECTED_COLOR) {
        @Override
        public void mousePressed(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd) {

            int firstBar = Math.max(0, Math.min(dragStart, dragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, dragEnd));

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (Bin bin : bins) {
                    bin.activated = false;
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = !bins[i].activated;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = true;
                }
            }
        }

        @Override
        public void mouseDragged(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int oldDragEnd, int newDragEnd) {
            int firstBar = Math.max(0, Math.min(dragStart, oldDragEnd));
            int lastBar = Math.min(bins.length - 1, Math.max(dragStart, oldDragEnd));

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].activated = bins[i].savedActivated;
            }

            firstBar = Math.max(0, Math.min(dragStart, newDragEnd));
            lastBar = Math.min(bins.length - 1, Math.max(dragStart, newDragEnd));

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = !bins[i].activated;
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].savedActivated = bins[i].activated;
                    bins[i].activated = true;
                }
            }
        }

        @Override
        public void mouseReleased(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd, HistogramTopComponent topComponent) {
            // Do nothing
        }

        @Override
        public void select(HistogramTopComponent topComponent) {
            topComponent.expandSelection();
        }
    };

    private final String label;
    private final Color barColor;
    private final Color activatedBarColor;
    private final Color selectedColor;
    private final Color activatedSelectedColor;

    private BinSelectionMode(String label, Color barColor, Color activatedBarColor, Color selectedColor, Color activatedSelectedColor) {
        this.label = label;
        this.barColor = barColor;
        this.activatedBarColor = activatedBarColor;
        this.selectedColor = selectedColor;
        this.activatedSelectedColor = activatedSelectedColor;
    }

    public abstract void mousePressed(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int oldDragEnd);

    public abstract void mouseDragged(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int oldDragEnd, int newDragEnd);

    public abstract void mouseReleased(boolean shiftDown, boolean controlDown, Bin[] bins, int dragStart, int dragEnd, HistogramTopComponent topComponent);

    public abstract void select(HistogramTopComponent topComponent);

    @Override
    public String toString() {
        return label;
    }

    public Color getBarColor() {
        return barColor;
    }

    public Color getActivatedBarColor() {
        return activatedBarColor;
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public Color getActivatedSelectedColor() {
        return activatedSelectedColor;
    }
}

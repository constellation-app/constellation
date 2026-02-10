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

import au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramDisplay2;
import au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramTopComponent2;
import javafx.scene.paint.Color;

/**
 * A BinSelectionMode represents the different ways the user can select bins in the histogram.
 *
 * @author sirius
 */
public enum BinSelectionMode {

    /**
     * The selection on the graph is immediately updated as the user clicks on bins in the histogram.
     */
    FREE_SELECTION("Free Selection", HistogramDisplay2.getBarColor(), HistogramDisplay2.getBarColor(), HistogramDisplay2.getSelectedColor(), HistogramDisplay2.getSelectedColor()) {
        @Override
        public void mousePressed(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd) {

            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            final int firstBar = Math.clamp(Math.min(dragStart, dragEnd), min, max); // Picks the smallest index
            final int lastBar = Math.clamp(Math.max(dragStart, dragEnd), min, max); // Picks the largest index

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (final Bin bin : bins) {
                    bin.setSelectedCount(0);
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedSelectedCount(bins[i].getSelectedCount());
                    bins[i].setSelectedCount(bins[i].getSelectedCount() == 0 ? bins[i].getElementCount() : 0);
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedSelectedCount(bins[i].getSelectedCount());
                    bins[i].setSelectedCount(bins[i].getElementCount());
                }
            }
        }

        @Override
        public void mouseDragged(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int oldDragEnd, final int newDragEnd) {
            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            int firstBar = Math.clamp(Math.min(dragStart, oldDragEnd), min, max); // Picks the smallest index
            int lastBar = Math.clamp(Math.max(dragStart, oldDragEnd), min, max); // Picks the largest index

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].setSelectedCount(bins[i].getSavedSelectedCount());
            }

            firstBar = Math.clamp(Math.min(dragStart, newDragEnd), min, max); // Picks the smallest index
            lastBar = Math.clamp(Math.max(dragStart, newDragEnd), min, max); // Picks the largest index

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedSelectedCount(bins[i].getSelectedCount());
                    bins[i].setSelectedCount(bins[i].getSelectedCount() == 0 ? bins[i].getElementCount() : 0);
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedSelectedCount(bins[i].getSelectedCount());
                    bins[i].setSelectedCount(bins[i].getElementCount());
                }
            }
        }

        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent topComponent) {
            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            final int firstBar = Math.clamp(Math.min(dragStart, dragEnd), min, max); // Picks the smallest index
            final int lastBar = Math.clamp(Math.max(dragStart, dragEnd), min, max); // Picks the largest index

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

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent2 topComponent) {
            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            final int firstBar = Math.clamp(Math.min(dragStart, dragEnd), min, max); // Picks the smallest index
            final int lastBar = Math.clamp(Math.max(dragStart, dragEnd), min, max); // Picks the largest index

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
        public void select(final HistogramTopComponent topComponent) {
            // Intentionally left blank
        }

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void select(final HistogramTopComponent2 topComponent) {
            // Intentionally left blank
        }
    },
    /**
     * Selections in the histogram are recorded as the user clicks and drags over bins but is not reflected on the graph
     * until the user chooses to apply the selection. At this point the selection of the graph is represents only those
     * elements that are in the intersection of the current and new selections.
     */
    WITHIN_SELECTION("Within Existing Selection", HistogramDisplay2.getBarColor(), HistogramDisplay2.getBarColor(), HistogramDisplay2.getSelectedColor(), HistogramDisplay2.getActiveColor()) {
        @Override
        public void mousePressed(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd) {

            final int firstBar = Math.min(dragStart, dragEnd);
            final int lastBar = Math.max(dragStart, dragEnd);

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (final Bin bin : bins) {
                    bin.setIsActivated(false);
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(!bins[i].getIsActivated());
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(true);
                }
            }
        }

        @Override
        public void mouseDragged(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int oldDragEnd, final int newDragEnd) {
            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            int firstBar = Math.clamp(Math.min(dragStart, oldDragEnd), min, max); // Picks the smallest index
            int lastBar = Math.clamp(Math.max(dragStart, oldDragEnd), min, max); // Picks the largest index

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].setIsActivated(bins[i].getSavedActivated());
            }

            firstBar = Math.clamp(Math.min(dragStart, newDragEnd), min, max); // Picks the smallest index
            lastBar = Math.clamp(Math.max(dragStart, newDragEnd), min, max); // Picks the largest index

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(!bins[i].getIsActivated());
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(true);
                }
            }
        }

        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent topComponent) {
            // Do nothing
        }

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent2 topComponent) {
            // Do nothing
        }

        @Override
        public void select(final HistogramTopComponent topComponent) {
            topComponent.filterSelection();
        }

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void select(final HistogramTopComponent2 topComponent) {
            topComponent.filterSelection();
        }
    },
    /**
     * Selections in the histogram are recorded as the user clicks and drags over bins but is not reflected on the graph
     * until the user chooses to apply the selection. At this point the selection of the graph is represents only those
     * elements that are in the union of the current and new selections.
     */
    ADD_TO_SELECTION("Add To Existing Selection", HistogramDisplay2.getBarColor(), HistogramDisplay2.getActiveColor(), HistogramDisplay2.getSelectedColor(), HistogramDisplay2.getSelectedColor()) {
        @Override
        public void mousePressed(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd) {

            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            final int firstBar = Math.clamp(Math.min(dragStart, dragEnd), min, max); // Picks the smallest index
            final int lastBar = Math.clamp(Math.max(dragStart, dragEnd), min, max); // Picks the largest index

            if (firstBar >= bins.length || lastBar < 0) {
                return;
            }

            if (!shiftDown && !controlDown) {
                for (final Bin bin : bins) {
                    bin.setIsActivated(false);
                }
            }

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(!bins[i].getIsActivated());
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(true);
                }
            }
        }

        @Override
        public void mouseDragged(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int oldDragEnd, final int newDragEnd) {
            final int min = 0;
            final int max = Math.max(0, bins.length - 1);
            int firstBar = Math.clamp(Math.min(dragStart, oldDragEnd), min, max); // Picks the smallest index
            int lastBar = Math.clamp(Math.max(dragStart, oldDragEnd), min, max); // Picks the largest index

            for (int i = firstBar; i <= lastBar; i++) {
                bins[i].setIsActivated(bins[i].getSavedActivated());
            }

            firstBar = Math.clamp(Math.min(dragStart, newDragEnd), min, max); // Picks the smallest index
            lastBar = Math.clamp(Math.max(dragStart, newDragEnd), min, max); // Picks the largest index

            if (controlDown) {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(!bins[i].getIsActivated());
                }
            } else {
                for (int i = firstBar; i <= lastBar; i++) {
                    bins[i].setSavedActivated(bins[i].getIsActivated());
                    bins[i].setIsActivated(true);
                }
            }
        }

        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent topComponent) {
            // Do nothing
        }

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent2 topComponent) {
            // Do nothing
        }

        @Override
        public void select(final HistogramTopComponent topComponent) {
            topComponent.expandSelection();
        }

        // TODO: Remove this function when Histogram rewrite is fully merged
        @Override
        public void select(final HistogramTopComponent2 topComponent) {
            topComponent.expandSelection();
        }
    };

    private final String label;
    private final Color barColor;
    private final Color activatedBarColor;
    private final Color selectedColor;
    private final Color activatedSelectedColor;

    private BinSelectionMode(final String label, final Color barColor, final Color activatedBarColor, final Color selectedColor, final Color activatedSelectedColor) {
        this.label = label;
        this.barColor = barColor;
        this.activatedBarColor = activatedBarColor;
        this.selectedColor = selectedColor;
        this.activatedSelectedColor = activatedSelectedColor;
    }

    public abstract void mousePressed(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int oldDragEnd);

    public abstract void mouseDragged(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int oldDragEnd, final int newDragEnd);

    public abstract void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent topComponent);

    // TODO: Remove this function when Histogram rewrite is fully merged
    public abstract void mouseReleased(final boolean shiftDown, final boolean controlDown, final Bin[] bins, final int dragStart, final int dragEnd, final HistogramTopComponent2 topComponent);

    public abstract void select(final HistogramTopComponent topComponent);

    // TODO: Remove this function when Histogram rewrite is fully merged
    public abstract void select(final HistogramTopComponent2 topComponent);

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

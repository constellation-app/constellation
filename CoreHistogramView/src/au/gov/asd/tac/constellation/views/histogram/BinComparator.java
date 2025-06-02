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

import java.util.Comparator;

/**
 * BinComparator represents the different ways in which 2 bins can be compared
 * and therefore sorted in the histogram.
 *
 * @author sirius
 */
public enum BinComparator implements Comparator<Bin> {

    KEY_NUMBER("Label (Number)", true, false) {
        @Override
        public int compare(final Bin o1, final Bin o2) {
            if (isNumeric(o1.toString()) && isNumeric(o2.toString())) {
                if (Float.parseFloat(o1.toString()) > Float.parseFloat(o2.toString())) {
                    return -1;
                } else if (Float.parseFloat(o1.toString()) < Float.parseFloat(o2.toString())) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return o2.compareTo(o1);
        }

        @Override
        public BinComparator getReverse() {
            return REVERSE_KEY_NUMBER;
        }
    },
    REVERSE_KEY_NUMBER("Label (Number ascending)", false, false) {
        @Override
        public int compare(final Bin o1, final Bin o2) {
            if (isNumeric(o1.toString()) && isNumeric(o2.toString())) {
                if (Float.parseFloat(o1.toString()) > Float.parseFloat(o2.toString())) {
                    return 1;
                } else if (Float.parseFloat(o1.toString()) < Float.parseFloat(o2.toString())) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return o1.compareTo(o2);
        }

        @Override
        public BinComparator getReverse() {
            return KEY_NUMBER;
        }
    },
    /**
     * Sort bins by their keys.
     */
    KEY("Label (Text)", true, false) {
        @Override
        public int compare(Bin o1, Bin o2) {
            return o2.compareTo(o1);
        }

        @Override
        public BinComparator getReverse() {
            return REVERSE_KEY;
        }
    },
    /**
     * Sort bins by their keys in reverse order.
     */
    REVERSE_KEY("Label (Text ascending)", false, false) {
        @Override
        public int compare(Bin o1, Bin o2) {
            return o1.compareTo(o2);
        }

        @Override
        public BinComparator getReverse() {
            return KEY;
        }
    },
    /**
     * Sort bins by the number of elements in each bin.
     */
    TOTAL_COUNT("Total Count", true, false) {
        @Override
        public int compare(Bin o1, Bin o2) {
            if (o1.elementCount > o2.elementCount) {
                return 1;
            } else if (o1.elementCount < o2.elementCount) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return REVERSE_TOTAL_COUNT;
        }
    },
    /**
     * Sort bins by the number of elements in each bin in reverse order.
     */
    REVERSE_TOTAL_COUNT("Total Count (descending)", false, false) {
        @Override
        public int compare(Bin o1, Bin o2) {
            if (o1.elementCount > o2.elementCount) {
                return -1;
            } else if (o1.elementCount < o2.elementCount) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return TOTAL_COUNT;
        }
    },
    /**
     * Sort bins by the number of selected elements in each bin.
     */
    SELECTED_COUNT("Selected Count", true, true) {
        @Override
        public int compare(Bin o1, Bin o2) {
            if (o1.selectedCount > o2.selectedCount) {
                return 1;
            } else if (o1.selectedCount < o2.selectedCount) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return REVERSE_SELECTED_COUNT;
        }
    },
    /**
     * Sort bins by the number of selected elements in each bin in reverse
     * order.
     */
    REVERSE_SELECTED_COUNT("Selected Count (descending)", false, true) {
        @Override
        public int compare(Bin o1, Bin o2) {
            if (o1.selectedCount > o2.selectedCount) {
                return -1;
            } else if (o1.selectedCount < o2.selectedCount) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return SELECTED_COUNT;
        }
    },
    /**
     * Sort bins by the proportion of elements in the bin that are selected.
     */
    SELECTED_PROPORTION("Selected Proportion", true, true) {
        @Override
        public int compare(Bin o1, Bin o2) {
            final int o1Score = o1.selectedCount * o2.elementCount;
            final int o2Score = o2.selectedCount * o1.elementCount;
            if (o1Score > o2Score) {
                return 1;
            } else if (o1Score < o2Score) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return REVERSE_SELECTED_PROPORTION;
        }
    },
    /**
     * Sort bins by the proportion of elements in the bin that are selected in
     * reverse order.
     */
    REVERSE_SELECTED_PROPORTION("Selected Proportion (descending)", false, true) {
        @Override
        public int compare(Bin o1, Bin o2) {
            final int o1Score = o1.selectedCount * o2.elementCount;
            final int o2Score = o2.selectedCount * o1.elementCount;
            if (o1Score > o2Score) {
                return -1;
            } else if (o1Score < o2Score) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public BinComparator getReverse() {
            return SELECTED_PROPORTION;
        }
    };

    private final String label;
    private final boolean ascending;
    private final boolean usesSelection;

    private BinComparator(String label, boolean ascending, boolean usesSelection) {
        this.label = label;
        this.ascending = ascending;
        this.usesSelection = usesSelection;
    }

    public abstract BinComparator getReverse();

    public boolean isAscending() {
        return ascending;
    }

    public boolean usesSelection() {
        return usesSelection;
    }

    protected boolean isNumeric(final String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Float.valueOf(strNum);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return label;
    }
}

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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.utilities.ElementSet;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A BinCollection represents all the bins in a single histogram. All elements in the graph will exist in exactly one
 * bin unless they have been excluded through a filter.
 *
 * @author sirius
 */
public class BinCollection {

    private GraphElementType elementType;
    private Bin[] bins;
    private int[] binElements;
    private int maxElementCount = -1;

    private final BinSelector binSelector = new BinSelector();

    /**
     * Returns the element type that is currently being binned.
     *
     * @return the element type this BinCollection operates on.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    public Bin[] getBins() {
        return bins;
    }

    public Bin[] getSelectedBins() {
        final List<Bin> selectedBins = new ArrayList<>();
        int count = 0;
        for (final Bin bin : getBins()) {
            if (bin.getSelectedCount() > 0) {
                count++;
                selectedBins.add(bin);
            }
        }
        final Bin[] selectedBinsArray = new Bin[count];
        return selectedBins.toArray(selectedBinsArray);
    }

    public int[] getBinElements() {
        return binElements.clone();
    }

    public void sort(BinComparator binComparator) {
        Arrays.sort(bins, binComparator);
    }

    public int getMaxElementCount() {
        if (maxElementCount < 0) {
            for (final Bin bin : bins) {
                maxElementCount = Math.max(maxElementCount, bin.getElementCount());
            }
        }
        return maxElementCount;
    }

    public void deactivateBins() {
        for (final Bin bin : bins) {
            bin.setIsActivated(false);
        }
    }

    public void updateSelection(GraphReadMethods graph) {

        binSelector.setElementType(graph, elementType);

        for (final Bin bin : bins) {
            bin.setSelectedCount(0);

            int position = bin.getFirstElement();
            while (position >= 0) {
                final int element = elementType.getElement(graph, position);
                if (binSelector.isSelected(graph, element)) {
                    bin.setSelectedCount(bin.getSelectedCount() + 1);
                }
                position = binElements[position];
            }
        }
    }

    public void selectOnlyBins(GraphWriteMethods graph, int firstBin, int lastBin) {
        binSelector.setElementType(graph, elementType);
        for (int binPosition = 0; binPosition < bins.length; binPosition++) {
            final Bin bin = bins[binPosition];

            final boolean select = binPosition >= firstBin && binPosition <= lastBin;

            int position = bin.getFirstElement();
            while (position >= 0) {
                int element = elementType.getElement(graph, position);
                binSelector.select(graph, element, select);
                position = binElements[position];
            }
        }
    }

    public void selectBins(GraphWriteMethods graph, int firstBin, int lastBin, boolean select) {
        binSelector.setElementType(graph, elementType);
        for (int binPosition = firstBin; binPosition <= lastBin; binPosition++) {
            final Bin bin = bins[binPosition];

            int position = bin.getFirstElement();
            while (position >= 0) {
                int element = elementType.getElement(graph, position);
                binSelector.select(graph, element, select);
                position = binElements[position];
            }
        }
    }

    public void invertBins(GraphWriteMethods graph, int firstBin, int lastBin) {
        binSelector.setElementType(graph, elementType);
        for (int binPosition = firstBin; binPosition <= lastBin; binPosition++) {
            final Bin bin = bins[binPosition];

            int position = bin.getFirstElement();
            while (position >= 0) {
                int element = elementType.getElement(graph, position);
                binSelector.select(graph, element, !binSelector.isSelected(graph, element));
                position = binElements[position];
            }
        }
    }

    public void completeBins(GraphWriteMethods graph, int firstBin, int lastBin) {
        binSelector.setElementType(graph, elementType);
        for (int binPosition = firstBin; binPosition <= lastBin; binPosition++) {
            final Bin bin = bins[binPosition];

            final boolean select = bin.getSelectedCount() > 0;

            int position = bin.getFirstElement();
            while (position >= 0) {
                int element = elementType.getElement(graph, position);
                binSelector.select(graph, element, select);
                position = binElements[position];
            }
        }
    }

    public void filterSelection(GraphWriteMethods graph) {
        binSelector.setElementType(graph, elementType);
        for (final Bin bin : bins) {
            if (!bin.getIsActivated()) {
                bin.setSelectedCount(0);
                int position = bin.getFirstElement();
                while (position >= 0) {
                    int element = elementType.getElement(graph, position);
                    binSelector.select(graph, element, false);
                    position = binElements[position];
                }
            }
            bin.setIsActivated(false);
        }
    }

    public void expandSelection(GraphWriteMethods graph) {
        binSelector.setElementType(graph, elementType);
        for (final Bin bin : bins) {
            if (bin.getIsActivated()) {
                bin.setIsActivated(false);
                bin.setSelectedCount(bin.getElementCount());
                int position = bin.getFirstElement();
                while (position >= 0) {
                    int element = elementType.getElement(graph, position);
                    binSelector.select(graph, element, true);
                    position = binElements[position];
                }
            }
        }
    }

    public void saveBinsToGraph(GraphWriteMethods graph, int attributeId) {
        for (final Bin bin : bins) {
            int position = bin.getFirstElement();
            while (position >= 0) {
                final int element = elementType.getElement(graph, position);
                graph.setObjectValue(attributeId, element, bin);
                position = binElements[position];
            }
        }
    }

    public void saveBinsToClipboard() {
        final StringBuilder buf = new StringBuilder();
        for (final Bin bin : bins) {
            final String label = bin.getLabel() != null ? bin.getLabel() : HistogramDisplay.NO_VALUE;
            buf.append(String.format("%s\t%d\n", label, bin.getElementCount()));
        }

        if (buf.length() > 0) {
            final StringSelection ss = new StringSelection(buf.toString());
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
        }
    }

    public static BinCollection createBinCollection(GraphReadMethods graph, GraphElementType elementType, String attribute, BinCreator binCreator, ElementSet filter, BinFormatter formatter, PluginParameters binFormatterParameters) {

        final int elementCount = elementType.getElementCount(graph);

        final int[] binElements = new int[elementCount];
        final Map<Bin, Bin> bins = new HashMap<>();

        binCreator.createBins(graph, elementType, attribute, bins, binElements, filter, formatter, binFormatterParameters);

        BinCollection binCollection = new BinCollection();
        binCollection.elementType = elementType;
        binCollection.bins = bins.keySet().toArray(new Bin[bins.size()]);
        binCollection.binElements = binElements;

        return binCollection;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append("BinCollection");
        String divider = "[";
        for (final Bin bin : bins) {
            out.append(divider);
            divider = ", ";
            out.append(bin);
            out.append('=');
            out.append(bin.getSelectedCount());
            out.append('/');
            out.append(bin.getElementCount());
        }
        out.append(']');
        return out.toString();
    }
}

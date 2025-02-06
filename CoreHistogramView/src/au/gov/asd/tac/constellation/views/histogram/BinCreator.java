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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.utilities.ElementSet;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.util.Map;

/**
 * A BinCreator is the class that actually creates a collection of bins from the
 * elements in the graph. Most of the hard work is passed to the bin itself.
 *
 * @author sirius
 */
public class BinCreator {

    protected final boolean attributeBased;
    protected final GraphElementType attributeElementType;
    protected final ElementRepresentative representative;
    protected final Bin bin;

    public BinCreator(boolean attributeBased, GraphElementType attributeElementType, ElementRepresentative representative, Bin bin) {
        this.attributeBased = attributeBased;
        this.attributeElementType = attributeElementType;
        this.representative = representative;
        this.bin = bin;
    }

    public boolean isAttributeBased() {
        return attributeBased;
    }

    public GraphElementType getAttributeElementType() {
        return attributeElementType;
    }

    public Bin getBin() {
        return bin;
    }

    public void createBins(GraphReadMethods graph, GraphElementType elementType, String attribute, Map<Bin, Bin> bins, int[] binElements, ElementSet filter, BinFormatter formatter, PluginParameters binFormatterParameters) {
        if (representative != null) {
            createElementBins(graph, representative, bin.create(), elementType, attribute, bins, binElements, filter, formatter, binFormatterParameters);
        }
    }

    private static void createElementBins(GraphReadMethods graph, ElementRepresentative representative, Bin bin, GraphElementType elementType, String binnedAttribute, Map<Bin, Bin> bins, int[] binElements, ElementSet filter, BinFormatter formatter, PluginParameters binFormatterParameters) {
        final GraphElementType representativeElementType = representative.getRepresentativeElementType(elementType);
        final int binnedAttributeId = binnedAttribute == null ? Graph.NOT_FOUND : graph.getAttribute(representativeElementType, binnedAttribute);
        bin.init(graph, binnedAttributeId);

        final int selectedAttributeId = graph.getAttribute(elementType.getSelectionElementType(), "selected");
        final int elementCount = elementType.getElementCount(graph);

        if (formatter != null) {
            bin = formatter.createBin(graph, binnedAttributeId, binFormatterParameters, bin);
        }

        for (int position = 0; position < elementCount; position++) {
            final int element = elementType.getElement(graph, position);

            if (filter == null || filter.contains(graph, element)) {
                bin.setKey(graph, binnedAttributeId, representative.findRepresentative(graph, elementType, element));
                Bin currentBin = bins.get(bin);
                if (currentBin == null && !bin.isOnlyNullElements()) {
                    currentBin = bin;
                    currentBin.prepareForPresentation();
                    bin = currentBin.create();
                    bins.put(currentBin, currentBin);
                }
                if (currentBin != null) {
                    currentBin.elementCount++;
                    if (selectedAttributeId != Graph.NOT_FOUND && elementType.isSelected(graph, element, selectedAttributeId)) {
                        currentBin.selectedCount++;
                    }
                    binElements[position] = currentBin.firstElement < 0 ? -1 : currentBin.firstElement;
                    currentBin.firstElement = position;
                }
            }
        }
    }
}

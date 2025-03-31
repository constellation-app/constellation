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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.utilities.ElementSet;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state of Histogram View.
 *
 * @author sirius
 * @author sol695510
 */
public final class HistogramState {

    private Map<GraphElementType, GraphElementState> elementStateMap = Map.of(
            GraphElementType.VERTEX, new GraphElementState(),
            GraphElementType.TRANSACTION, new GraphElementState(),
            GraphElementType.EDGE, new GraphElementState(),
            GraphElementType.LINK, new GraphElementState());

    private GraphElementType elementType;

    private AttributeType attributeType;
    private String attribute;
    private BinFormatter binFormatter;

    private PluginParameters binFormatterParameters;
    private BinComparator binComparator;
    private BinSelectionMode binSelectionMode;

    private ElementSet[] filters = new ElementSet[GraphElementType.values().length];

    public HistogramState() {
        this.elementType = GraphElementType.VERTEX;
        setElementState();
        this.binFormatterParameters = BinFormatter.DEFAULT_BIN_FORMATTER.createParameters();
        this.binComparator = BinComparator.REVERSE_KEY;
        this.binSelectionMode = BinSelectionMode.FREE_SELECTION;
    }

    public HistogramState(final HistogramState original) {
        if (original == null) {
            this.elementType = GraphElementType.VERTEX;
            setElementState();
            this.binFormatterParameters = BinFormatter.DEFAULT_BIN_FORMATTER.createParameters();
            this.binComparator = BinComparator.REVERSE_KEY;
            this.binSelectionMode = BinSelectionMode.FREE_SELECTION;
        } else {
            this.elementStateMap = Map.copyOf(original.elementStateMap);
            this.elementType = original.elementType;
            setElementState();
            this.binFormatterParameters = original.binFormatterParameters != null ? original.binFormatterParameters.copy() : this.binFormatter.createParameters();
            this.binFormatter.updateParameters(this.binFormatterParameters);
            this.binComparator = original.binComparator;
            this.binSelectionMode = original.binSelectionMode;
            this.filters = Arrays.copyOf(original.filters, original.filters.length);
        }
    }

    /**
     * Stores the state of the choices for attributeType, attribute and
     * binFormatter for each Graph Element Type respectively.
     */
    public final class GraphElementState {

        private AttributeType attributeType = AttributeType.ATTRIBUTE;
        private String attribute = null;
        private BinFormatter binFormatter = BinFormatter.DEFAULT_BIN_FORMATTER;

        private AttributeType getAttributeType() {
            return attributeType;
        }

        private void setAttributeType(final AttributeType attributeType) {
            this.attributeType = attributeType;
        }

        private String getAttribute() {
            return attribute;
        }

        private void setAttribute(final String attribute) {
            this.attribute = attribute;
        }

        private BinFormatter getBinFormatter() {
            return binFormatter;
        }

        private void setBinFormatter(final BinFormatter binFormatter) {
            this.binFormatter = binFormatter;

        }
    }

    /**
     * Sets the attributeType, attribute and binFormatter of the HistogramState
     * to the values saved in elementStateMap for the currently selected Graph
     * Element Type.
     */
    public void setElementState() { // Set back to protected after histogram rewrite fully replaces old version
        attributeType = elementStateMap.get(elementType).getAttributeType();
        attribute = elementStateMap.get(elementType).getAttribute();
        binFormatter = elementStateMap.get(elementType).getBinFormatter();
    }

    /**
     * Ensure that this HistogramState is compatible with the current graph. The
     * HistogramState can become invalid if the Schema has altered the graph as
     * it is loaded and removed the attribute that the HistogramState is looking
     * at.
     *
     * @param graph the graph to be validated.
     */
    public void validate(final GraphReadMethods graph) {
        if (attribute != null && attributeType != null) {
            Map<String, BinCreator> binCreators = new HashMap<>();
            attributeType.addBinCreators(graph, elementType, binCreators);
            if (!binCreators.containsKey(attribute)) {
                attribute = null;
            }
        }
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public void setElementType(final GraphElementType elementType) {
        this.elementType = elementType;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(final AttributeType attributeType) {
        this.attributeType = attributeType;
        elementStateMap.get(elementType).setAttributeType(this.attributeType);
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
        elementStateMap.get(elementType).setAttribute(this.attribute);
    }

    public BinFormatter getBinFormatter() {
        return binFormatter;
    }

    public void setBinFormatter(final BinFormatter binFormatter) {
        this.binFormatter = binFormatter;
        elementStateMap.get(elementType).setBinFormatter(this.binFormatter);
    }

    public PluginParameters getBinFormatterParameters() {
        return binFormatterParameters;
    }

    public void setBinFormatterParameters(final PluginParameters parameters) {
        binFormatterParameters = parameters;
    }

    public BinComparator getBinComparator() {
        return binComparator;
    }

    public void setBinComparator(final BinComparator binComparator) {
        this.binComparator = binComparator;
    }

    public BinSelectionMode getBinSelectionMode() {
        return binSelectionMode;
    }

    public void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        this.binSelectionMode = binSelectionMode;
    }

    public ElementSet getFilter(final GraphElementType type) {
        return filters[type.ordinal()];
    }

    public void setFilter(final GraphElementType type, final ElementSet filter) {
        this.filters[type.ordinal()] = filter;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("HistogramState[elementType=");
        out.append(elementType);
        out.append(", attributeType=");
        out.append(attributeType);
        out.append(", attribute");
        out.append(attribute);
        out.append(", comparator=");
        out.append(binComparator);
        out.append(", binFormatter=");
        out.append(binFormatter);
        out.append(", selection mode=");
        out.append(binSelectionMode);
        out.append(", filter=");
        out.append(Arrays.toString(filters));
        out.append("]");
        return out.toString();
    }
}

/*
 * Copyright 2010-2019 Australian Signals Directorate
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
 *
 * @author sirius
 */
public class HistogramState {

    private GraphElementType elementType;
    private AttributeType attributeType;
    private String attribute;
    private BinComparator binComparator;
    private BinFormatter binFormatter;
    private BinSelectionMode binSelectionMode;
    private final ElementSet[] filters = new ElementSet[GraphElementType.values().length];

    private PluginParameters binFormatterParameters;

    public HistogramState() {
        this.elementType = GraphElementType.VERTEX;
        this.attributeType = AttributeType.ATTRIBUTE;
        this.attribute = null;
        this.binComparator = BinComparator.REVERSE_KEY;
        this.binFormatter = BinFormatter.DEFAULT_BIN_FORMATTER;
        this.binFormatterParameters = BinFormatter.DEFAULT_BIN_FORMATTER.createParameters();
        this.binSelectionMode = BinSelectionMode.FREE_SELECTION;
    }

    public HistogramState(GraphElementType elementType, AttributeType attributeType, String attribute, BinComparator binComparator, BinFormatter binFormatter, BinSelectionMode binSelectionMode, ElementSet filter) {
        this.elementType = elementType;
        this.attributeType = attributeType;
        this.attribute = attribute;
        this.binComparator = binComparator;
        this.binFormatter = binFormatter;
        this.binFormatterParameters = binFormatter.createParameters();
        this.binFormatter.updateParameters(this.binFormatterParameters);
        this.binSelectionMode = binSelectionMode;
    }

    public HistogramState(HistogramState original) {
        if (original == null) {
            this.elementType = GraphElementType.VERTEX;
            this.attributeType = AttributeType.ATTRIBUTE;
            this.attribute = null;
            this.binComparator = BinComparator.REVERSE_KEY;
            this.binFormatter = BinFormatter.DEFAULT_BIN_FORMATTER;
            this.binFormatterParameters = BinFormatter.DEFAULT_BIN_FORMATTER.createParameters();
            this.binSelectionMode = BinSelectionMode.FREE_SELECTION;
        } else {
            this.elementType = original.elementType;
            this.attributeType = original.attributeType;
            this.attribute = original.attribute;
            this.binComparator = original.binComparator;
            this.binFormatter = original.binFormatter;
            this.binFormatterParameters = original.binFormatterParameters == null ? null : original.binFormatterParameters.copy();
            this.binFormatter.updateParameters(this.binFormatterParameters);
            this.binSelectionMode = original.binSelectionMode;

            for (int i = 0; i < filters.length; i++) {
                this.filters[i] = original.filters[i];
            }
        }
    }

    /**
     * Ensure that this HistogramState is compatible with the current graph. The
     * HistogramState can become invalid if the Schema has altered the graph as
     * it is loaded and removed the attribute that the HistogramState is looking
     * at.
     *
     * @param graph the graph to be validated.
     */
    public void validate(GraphReadMethods graph) {
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

    public void setElementType(GraphElementType elementType) {
        this.elementType = elementType;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public BinComparator getBinComparator() {
        return binComparator;
    }

    public void setBinComparator(BinComparator binComparator) {
        this.binComparator = binComparator;
    }

    public PluginParameters getBinFormatterParameters() {
        return binFormatterParameters;
    }

    public void setBinFormatterParameters(PluginParameters parameters) {
        binFormatterParameters = parameters;
    }

    public BinFormatter getBinFormatter() {
        return binFormatter;
    }

    public void setBinFormatter(BinFormatter binFormatter) {
        this.binFormatter = binFormatter;
    }

    public BinSelectionMode getBinSelectionMode() {
        return binSelectionMode;
    }

    public void setBinSelectionMode(BinSelectionMode binSelectionMode) {
        this.binSelectionMode = binSelectionMode;
    }

    public ElementSet getFilter(GraphElementType type) {
        return filters[type.ordinal()];
    }

    public void setFilter(GraphElementType type, ElementSet filter) {
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

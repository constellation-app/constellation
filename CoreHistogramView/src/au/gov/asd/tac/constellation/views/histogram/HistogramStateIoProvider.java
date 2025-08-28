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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IOProvider that allows the histogram state to be saved and loaded in a
 * Constellation file and therefore persisted as a graph is saved and loaded.
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class HistogramStateIoProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return HistogramConcept.MetaAttribute.HISTOGRAM_STATE.getAttributeType();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final HistogramState state = new HistogramState();
            state.setElementType(GraphElementType.valueOf(jnode.get("elementType").asText()));
            state.setAttributeType(AttributeType.valueOf(jnode.get("attributeType").asText()));
            state.setAttribute(jnode.get("attribute").asText());
            state.setBinComparator(BinComparator.valueOf(jnode.get("binComparator").asText()));

            final JsonNode binFormatterNode = jnode.get("binFormatter");
            if (binFormatterNode == null) {
                state.setBinFormatter(BinFormatter.DEFAULT_BIN_FORMATTER);
            } else {
                state.setBinFormatter(BinFormatter.getBinFormatter(binFormatterNode.asText()));
                PluginParameters parameters = state.getBinFormatter().createParameters();
                state.getBinFormatter().updateParameters(parameters);
                if (parameters != null) {
                    JsonNode binFormatterParametersNode = jnode.get("binFormatterParameters");
                    if (binFormatterParametersNode != null) {
                        for (PluginParameter<?> parameter : parameters.getParameters().values()) {
                            JsonNode valueNode = binFormatterParametersNode.get(parameter.getId());
                            if (valueNode != null) {
                                parameter.setStringValue(valueNode.asText());
                            }
                        }
                    }
                }
                state.setBinFormatterParameters(parameters);
            }

            state.setBinSelectionMode(BinSelectionMode.valueOf(jnode.get("binSelectionMode").asText()));

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final HistogramState histogramStateToSave = (HistogramState) graph.getObjectValue(attr.getId(), elementId);
            if (histogramStateToSave == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeStringField("elementType", histogramStateToSave.getElementType().name());
                jsonGenerator.writeStringField("attributeType", histogramStateToSave.getAttributeType().name());
                jsonGenerator.writeStringField("attribute", histogramStateToSave.getAttribute());
                jsonGenerator.writeStringField("binComparator", histogramStateToSave.getBinComparator().name());
                jsonGenerator.writeStringField("binFormatter", histogramStateToSave.getBinFormatter().getId());
                PluginParameters parameters = histogramStateToSave.getBinFormatterParameters();
                if (parameters != null) {
                    jsonGenerator.writeObjectFieldStart("binFormatterParameters");
                    for (PluginParameter<?> parameter : parameters.getParameters().values()) {
                        jsonGenerator.writeStringField(parameter.getId(), parameter.getStringValue());
                    }
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeStringField("binSelectionMode", histogramStateToSave.getBinSelectionMode().name());
                jsonGenerator.writeEndObject();
            }
        }
    }
}

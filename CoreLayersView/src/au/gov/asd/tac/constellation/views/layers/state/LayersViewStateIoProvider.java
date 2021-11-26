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
package au.gov.asd.tac.constellation.views.layers.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttributeUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the LayersViewState object.
 *
 * @author aldebaran30701
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class LayersViewStateIoProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final List<BitMaskQuery> vxlayerDescriptions = new ArrayList<>();
            final ArrayNode vertexLayersArray = (ArrayNode) jnode.withArray("vertexLayers");
            for (int i = 0; i < vertexLayersArray.size(); i++) {
                if (vertexLayersArray.get(i).isNull()) {
                    vxlayerDescriptions.add(null);
                } else {
                    final BitMaskQuery query = new BitMaskQuery(
                            new Query(GraphElementType.VERTEX,
                                    StringUtils.equals("null", vertexLayersArray.get(i).get(2).asText()) ? ""
                                    : vertexLayersArray.get(i).get(2).asText()),
                            vertexLayersArray.get(i).get(0).asInt(),
                            vertexLayersArray.get(i).get(3).asText()
                    );
                    query.setVisibility(vertexLayersArray.get(i).get(1).asBoolean());
                    vxlayerDescriptions.add(query);

                }
            }

            final List<BitMaskQuery> txlayerDescriptions = new ArrayList<>();
            final ArrayNode transactionLayersArray = (ArrayNode) jnode.withArray("transactionLayers");
            for (int i = 0; i < transactionLayersArray.size(); i++) {
                if (transactionLayersArray.get(i).isNull()) {
                    txlayerDescriptions.add(null);
                } else {
                    final BitMaskQuery query = new BitMaskQuery(
                            new Query(GraphElementType.TRANSACTION,
                                    StringUtils.equals("null", transactionLayersArray.get(i).get(2).asText()) ? ""
                                    : transactionLayersArray.get(i).get(2).asText()),
                            transactionLayersArray.get(i).get(0).asInt(),
                            transactionLayersArray.get(i).get(3).asText()
                    );
                    query.setVisibility(transactionLayersArray.get(i).get(1).asBoolean());
                    txlayerDescriptions.add(query);

                }
            }
            final List<SchemaAttribute> layerAttributes = new ArrayList<>();
            final ArrayNode layerAttributesArray = (ArrayNode) jnode.withArray("layerAttributes");
            for (int i = 0; i < layerAttributesArray.size(); i++) {
                if (layerAttributesArray.get(i).isNull()) {
                    layerAttributes.add(null);
                } else {
                    layerAttributes.add(SchemaAttributeUtilities.getAttribute(
                            GraphElementType.getValue(layerAttributesArray.get(i).get(0).asText()),
                            layerAttributesArray.get(i).get(1).asText()));
                }
            }

            BitMaskQueryCollection vxQueries = new BitMaskQueryCollection(GraphElementType.VERTEX);
            vxQueries.setQueries(vxlayerDescriptions.toArray(new BitMaskQuery[64]));

            BitMaskQueryCollection txQueries = new BitMaskQueryCollection(GraphElementType.TRANSACTION);
            txQueries.setQueries(txlayerDescriptions.toArray(new BitMaskQuery[64]));

            final LayersViewState state = new LayersViewState(layerAttributes, vxQueries, txQueries);
            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {

        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final LayersViewState originalState = graph.getObjectValue(attribute.getId(), elementId);

            if (originalState == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                // make a copy in case the state on the graph is currently being modified.
                final LayersViewState state = new LayersViewState(originalState);
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeArrayFieldStart("vertexLayers");

                for (final BitMaskQuery layer : state.getVxQueriesCollection().getQueries()) {
                    if (layer == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray("index", layer.getIndex());
                        jsonGenerator.writeNumber(layer.getIndex());
                        jsonGenerator.writeBoolean(layer.isVisible());
                        jsonGenerator.writeString(layer.getQueryString());
                        jsonGenerator.writeString(layer.getDescription());
                        jsonGenerator.writeEndArray();
                    }
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart("transactionLayers");

                for (final BitMaskQuery layer : state.getTxQueriesCollection().getQueries()) {
                    if (layer == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray("index", layer.getIndex());
                        jsonGenerator.writeNumber(layer.getIndex());
                        jsonGenerator.writeBoolean(layer.isVisible());
                        jsonGenerator.writeString(layer.getQueryString());
                        jsonGenerator.writeString(layer.getDescription());
                        jsonGenerator.writeEndArray();
                    }
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart("layerAttributes");
                int count = 0;
                for (final SchemaAttribute attr : state.getLayerAttributes()) {
                    if (attr == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray("index", count++);
                        jsonGenerator.writeString(attr.getElementType().toString());
                        jsonGenerator.writeString(attr.getName());
                        jsonGenerator.writeEndArray();
                    }
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();
            }
        }
    }
}

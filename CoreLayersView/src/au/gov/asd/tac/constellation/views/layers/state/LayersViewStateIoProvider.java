/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.utilities.Query;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            final List<BitMaskQuery> layerDescriptions = new ArrayList<>();
            final ArrayNode layersArray = (ArrayNode) jnode.withArray("layers");
            for (int i = 0; i < layersArray.size(); i++) {
                if (layersArray.get(i).isNull()) {
                    layerDescriptions.add(null);
                } else {
                    // create LayerDescription with index, visibility, query and description

                    final BitMaskQuery query = new BitMaskQuery(new Query(GraphElementType.VERTEX, layersArray.get(i).get(2).asText()),
                            layersArray.get(i).get(0).asInt(),
                            layersArray.get(i).get(3).asText()
                    );
                    query.setVisibility(layersArray.get(i).get(1).asBoolean());
                    layerDescriptions.add(query);

                }
            }
            final List<SchemaAttribute> layerAttributes = new ArrayList<>();
            final ArrayNode layerAttributesArray = (ArrayNode) jnode.withArray("layerAttributes");
            for (int i = 0; i < layerAttributesArray.size(); i++) {
                if (layerAttributesArray.get(i).isNull()) {
                    layerAttributes.add(null);
                } else {
                    // create SchemaAttribute here // TODO: This currently adds both V and T attributes.
                    layerAttributes.add(SchemaAttributeUtilities.getAttribute(GraphElementType.VERTEX, layerAttributesArray.get(i).get(0).asText()));
                    layerAttributes.add(SchemaAttributeUtilities.getAttribute(GraphElementType.TRANSACTION, layerAttributesArray.get(i).get(0).asText()));
                }
            }
            final LayersViewState state = new LayersViewState(layerDescriptions, layerAttributes, new BitMaskQueryCollection(layerDescriptions));
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
                jsonGenerator.writeArrayFieldStart("layers");

                for (BitMaskQuery layer : state.getLayers()) {
                    if (layer == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray(layer.getIndex());
                        jsonGenerator.writeNumber(layer.getIndex());
                        jsonGenerator.writeBoolean(layer.getVisibility());
                        jsonGenerator.writeString(layer.getQueryString());
                        jsonGenerator.writeString(layer.getDescription());
                        jsonGenerator.writeEndArray();
                    }
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart("layerAttributes");
                int count = 0;
                for (SchemaAttribute attr : state.getLayerAttributes()) {
                    if (attr == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray(count++);
                        // TODO: Specify Vertex/Transaction?
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

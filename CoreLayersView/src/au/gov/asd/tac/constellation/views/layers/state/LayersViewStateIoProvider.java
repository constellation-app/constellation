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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
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
            final List<LayerDescription> layerDescriptions = new ArrayList<>();
            final ArrayNode layersArray = (ArrayNode) jnode.withArray("layers");
            for (int i = 0; i < layersArray.size(); i++) {
                if (layersArray.get(i).isNull()) {
                    layerDescriptions.add(null);
                } else {
                    // create LayerDescription with index, visibility, query and description
                    layerDescriptions.add(new LayerDescription(
                            layersArray.get(i).get(0).asInt(),
                            layersArray.get(i).get(1).asBoolean(),
                            layersArray.get(i).get(2).asText(),
                            layersArray.get(i).get(3).asText()
                    ));
                }
            }
            final LayersViewState state = new LayersViewState(layerDescriptions);
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

                for (LayerDescription layer : state.getLayers()) {
                    if (layer == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray(layer.getLayerIndex());
                        jsonGenerator.writeNumber(layer.getLayerIndex());
                        jsonGenerator.writeBoolean(layer.getCurrentLayerVisibility());
                        jsonGenerator.writeString(layer.getLayerQuery());
                        jsonGenerator.writeString(layer.getLayerDescription());
                        jsonGenerator.writeEndArray();
                    }
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}

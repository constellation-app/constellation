
/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author serpens24
 */
public abstract class AbstractGraphLabelsIOProvider extends AbstractGraphIOProvider {

    private static final String ATTRIBUTE_NAME = "attribute_name";
    private static final String COLOR = "color";
    private static final String RADIUS = "radius";

    /**
     * Deserialise an object from a JsonNode.
     * <p>
     * Refer to base class for detailed description.
     * 
     * @param attributeId The id of the attribute being read.
     * @param elementId The id of the element being read.
     * @param jnode The JsonNode to read from.
     * @param graph The graph that the resulting object will be placed in. Provided in case
     * the object requires some graph data.
     * @param vertexMap (not used) A mapping from a vertex id in the file to the vertex id
     * in the graph.
     * @param transactionMap (not used) A mapping from a transaction id in the file to the
     * transaction id in the graph.
     * @param byteReader (not used) The byte reader containing ancillary data (e.g. images)
     * that doesn't easily fit into a JSON document.
     * @param (not used) cache A cache that can be used to dedup identical instances of the
     * same immutable objects.
     * @throws java.io.IOException If there's a problem reading the document. 
     */
    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        final List<GraphLabel> labels = new ArrayList<>();
        if (!jnode.isNull() && jnode.isArray()) {
            for (int i = 0; i < jnode.size(); i++) {
                final JsonNode attributeName = jnode.get(i).get(ATTRIBUTE_NAME);
                final ConstellationColor colorValue = ColorIOProvider.readColorObject(jnode.get(i).get(COLOR));
                final JsonNode radius = jnode.get(i).get(RADIUS);

                labels.add(new GraphLabel(attributeName.textValue(), colorValue, radius.floatValue()));
            }

            graph.setObjectValue(attributeId, elementId, new GraphLabels(labels));
        } else {
            final String attrVal = jnode.isNull() ? null : jnode.textValue();
            graph.setStringValue(attributeId, elementId, attrVal);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final GraphLabels graphLabels = graph.getObjectValue(attr.getId(), elementId);
            if (graphLabels == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeArrayFieldStart(attr.getName());
                for (GraphLabel graphLabel : graphLabels.getLabels()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(ATTRIBUTE_NAME, graphLabel.getAttributeName());

                    final ConstellationColor color = graphLabel.getColor();
                    jsonGenerator.writeObjectFieldStart(COLOR);
                    ColorIOProvider.writeColorObject(color, jsonGenerator);
                    jsonGenerator.writeEndObject();

                    jsonGenerator.writeNumberField(RADIUS, graphLabel.getSize());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
        }
    }
}

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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription}
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class ColorIOProvider extends AbstractGraphIOProvider {

    private static final String NAME = "name";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";
    private static final String ALPHA = "alpha";

    /**
     * Get a string representing the type of data that this provider handles.
     * 
     * @return A unique name indicating the type of data handled by this
     * provider.
     */
    @Override
    public String getName() {
        return ColorAttributeDescription.ATTRIBUTE_NAME;
    }

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
     * @param cache A cache that can be used to dedup identical instances of the
     * same immutable objects.
     * @throws java.io.IOException If there's a problem reading the document. 
     */
    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull() && jnode.isObject()) {
            final ConstellationColor attributeValue = readColorObject(jnode);
            graph.setObjectValue(attributeId, elementId, cache.deduplicate(attributeValue));
        } else {
            // legacy
            final String attributeValue = jnode.isNull() ? null : jnode.textValue();
            graph.setStringValue(attributeId, elementId, cache.deduplicate(attributeValue));
        }
    }

    /**
     * Write this object to the JSON generator.
     * <p>
     * Refer to base class for detailed description.
     * 
     * @param attr The attribute being written.
     * @param elementId The id of the element being written.
     * @param jsonGenerator The JsonGenerator used to write to the JSON document.
     * @param graph The graph that the object belongs to. Provided in case the object requires some 
     * graph data.
     * @param byteWriter (not used)  For ancillary data (e.g. images) that doesn't easily
     * fit into a JSON document.
     * @param verbose Determines whether to write default values of attributes or not.
     * @throws IOException 
     */
    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final String attributeValue = graph.getStringValue(attr.getId(), elementId);
            if (attributeValue == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                ConstellationColor color = ConstellationColor.getColorValue(attributeValue);
                writeColorObject(color, jsonGenerator);
                jsonGenerator.writeEndObject();
            }
        }
    }

    /**
     * Read a json object representing a color and return a ColorValue object
     *
     * @param color The json node representing the color
     * @return A ColorValue object representing the color
     */
    public static final ConstellationColor readColorObject(final JsonNode color) {
        if (color.has(NAME)) {
            final String name = color.get(NAME).textValue();
            return ConstellationColor.getColorValue(name);
        } else {
            final float red = color.get(RED).floatValue();
            final float green = color.get(GREEN).floatValue();
            final float blue = color.get(BLUE).floatValue();
            final float alpha = color.get(ALPHA).floatValue();
            return ConstellationColor.getColorValue(red, green, blue, alpha);
        }
    }

    /**
     * Write a json object representing the color
     *
     * @param color The color value
     * @param jsonGenerator The JsonGenerator used to write to the JSON
     * document.
     * @throws IOException
     */
    public static final void writeColorObject(final ConstellationColor color, final JsonGenerator jsonGenerator) throws IOException {
        if (color.getName() != null) {
            jsonGenerator.writeStringField(NAME, color.getName());
        } else {
            jsonGenerator.writeNumberField(RED, color.getRed());
            jsonGenerator.writeNumberField(GREEN, color.getGreen());
            jsonGenerator.writeNumberField(BLUE, color.getBlue());
            jsonGenerator.writeNumberField(ALPHA, color.getAlpha());
        }
    }
}

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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription}
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class DecoratorsIOProvider extends AbstractGraphIOProvider {

    private static final String NORTH_WEST = "north_west";
    private static final String NORTH_EAST = "north_east";
    private static final String SOUTH_EAST = "south_east";
    private static final String SOUTH_WEST = "south_west";

    /**
     * Get a string representing the type of data that this provider handles.
     * 
     * @return A unique name indicating the type of data handled by this
     * provider.
     */
    @Override
    public String getName() {
        return DecoratorsAttributeDescription.ATTRIBUTE_NAME;
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
     * @param (not used) cache A cache that can be used to dedup identical instances of the
     * same immutable objects.
     * @throws java.io.IOException If there's a problem reading the document. 
     */
    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull() && jnode.isObject()) {
            final String nw = jnode.get(NORTH_WEST).textValue();
            final String ne = jnode.get(NORTH_EAST).textValue();
            final String se = jnode.get(SOUTH_EAST).textValue();
            final String sw = jnode.get(SOUTH_WEST).textValue();
            graph.setObjectValue(attributeId, elementId, new VertexDecorators(nw, ne, se, sw));
        } else {
            final String attributeValue = jnode.isNull() ? null : jnode.textValue();
            graph.setStringValue(attributeId, elementId, attributeValue);
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
            final VertexDecorators decorators = graph.getObjectValue(attr.getId(), elementId);
            if (decorators == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeStringField(NORTH_WEST, decorators.getNorthWestDecoratorAttribute());
                jsonGenerator.writeStringField(NORTH_EAST, decorators.getNorthEastDecoratorAttribute());
                jsonGenerator.writeStringField(SOUTH_EAST, decorators.getSouthEastDecoratorAttribute());
                jsonGenerator.writeStringField(SOUTH_WEST, decorators.getSouthWestDecoratorAttribute());
                jsonGenerator.writeEndObject();
            }
        }
    }
}

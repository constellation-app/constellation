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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class DecoratorsIOProvider extends AbstractGraphIOProvider {

    private static final String NORTH_WEST = "north_west";
    private static final String NORTH_EAST = "north_east";
    private static final String SOUTH_EAST = "south_east";
    private static final String SOUTH_WEST = "south_west";

    @Override
    public String getName() {
        return DecoratorsAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull() && jnode.isObject()) {
            final String nw = jnode.get(NORTH_WEST).textValue();
            final String ne = jnode.get(NORTH_EAST).textValue();
            final String se = jnode.get(SOUTH_EAST).textValue();
            final String sw = jnode.get(SOUTH_WEST).textValue();
            graph.setObjectValue(attributeId, elementId, new VertexDecorators(nw, ne, se, sw));
        } else {
            final String attrVal = jnode.isNull() ? null : jnode.textValue();
            graph.setStringValue(attributeId, elementId, attrVal);
        }
    }

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

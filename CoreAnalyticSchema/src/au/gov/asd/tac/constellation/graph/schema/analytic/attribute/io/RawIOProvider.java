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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription}
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class RawIOProvider extends AbstractGraphIOProvider {

    private static final String RAW_IDENTIFIER_TAG = "rawIdentifier";
    private static final String RAW_TYPE_TAG = "rawType";

    @Override
    public String getName() {
        return RawAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (jnode.isNull()) {
            graph.setObjectValue(attributeId, elementId, null);
        } else {
            final JsonNode rawIdentifier = jnode.get(RAW_IDENTIFIER_TAG);
            final JsonNode rawType = jnode.get(RAW_TYPE_TAG);
            final RawData rawValue = new RawData(rawIdentifier.isNull() ? null : rawIdentifier.textValue(), rawType.isNull() ? null : jnode.get(RAW_TYPE_TAG).textValue());
            graph.setObjectValue(attributeId, elementId, cache.deduplicate(rawValue));
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final RawData rawValue = graph.getObjectValue(attr.getId(), elementId);
            if (rawValue == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeStringField(RAW_IDENTIFIER_TAG, rawValue.getRawIdentifier());
                jsonGenerator.writeStringField(RAW_TYPE_TAG, rawValue.getRawType());
                jsonGenerator.writeEndObject();
            }
        }
    }
}

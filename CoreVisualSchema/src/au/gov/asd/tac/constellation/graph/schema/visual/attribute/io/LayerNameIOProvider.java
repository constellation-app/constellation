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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LayerNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.schema.visual.attribute.LayerNameAttributeDescription}
 *
 * @author algol
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class LayerNameIOProvider extends AbstractGraphIOProvider {

    private static final String NAME_TAG = "name";
    private static final String LAYER_TAG = "layer";

    @Override
    public String getName() {
        return LayerNameAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            if (jnode.has(NAME_TAG) && jnode.has(LAYER_TAG)) {
                final String name = jnode.get(NAME_TAG).textValue();
                final int layer = jnode.get(LAYER_TAG).intValue();
                final LayerName ln = new LayerName(layer, name);
                graph.setObjectValue(attributeId, elementId, ln);
            }
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final LayerName ln = (LayerName) graph.getObjectValue(attribute.getId(), elementId);
            if (ln != null) {
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeStringField(NAME_TAG, ln.getName());
                jsonGenerator.writeNumberField(LAYER_TAG, ln.getLayer());
                jsonGenerator.writeEndObject();
            } else {
                jsonGenerator.writeNullField(attribute.getName());
            }
        }
    }
}

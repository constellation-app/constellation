/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessConcept;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessState;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IOProvider that allows the data access state to be saved and loaded in a
 * Constellation file and therefore persisted as a graph is saved and loaded.
 *
 * @author arcturus
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class DataAccessStateIoProvider extends AbstractGraphIOProvider {
    private static final String GLOBAL_OBJECT = "global";
    private static final String PLUGINS_OBJECT = "plugins";
    
    @Override
    public String getName() {
        return DataAccessConcept.MetaAttribute.DATAACCESS_STATE.getAttributeType();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        final DataAccessState state = new DataAccessState();

        if (!jnode.isNull() && jnode.isArray()) {
            for (int i = 0; i < jnode.size(); i++) {
                state.newTab();

                final JsonNode tab = jnode.get(i).get(GLOBAL_OBJECT);
                final Iterator<String> globalParameterNames = tab.fieldNames();
                while (globalParameterNames.hasNext()) {
                    final String globalParameterName = globalParameterNames.next();
                    state.add(globalParameterName, tab.get(globalParameterName).isNull()
                            ? null : tab.get(globalParameterName).textValue());
                }

                // TODO: retrieve plugin state information
            }
        }

        graph.setObjectValue(attributeId, elementId, state);
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final DataAccessState state = (DataAccessState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeArrayFieldStart(attr.getName());
                for (final Map<String, String> tab : state.getState()) {
                    jsonGenerator.writeStartObject();

                    jsonGenerator.writeObjectFieldStart(GLOBAL_OBJECT);
                    for (final Entry<String, String> globalParameter : tab.entrySet()) {
                        jsonGenerator.writeStringField(globalParameter.getKey(), globalParameter.getValue());
                    }
                    jsonGenerator.writeEndObject();

                    jsonGenerator.writeObjectFieldStart(PLUGINS_OBJECT);
                    // TODO: write plugin state information
                    jsonGenerator.writeEndObject();

                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
        }
    }
}

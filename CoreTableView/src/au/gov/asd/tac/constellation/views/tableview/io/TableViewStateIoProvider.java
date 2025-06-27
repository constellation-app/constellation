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
package au.gov.asd.tac.constellation.views.tableview.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the TableViewState object.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class TableViewStateIoProvider extends AbstractGraphIOProvider {

    private static final String ATTRIBUTE_ELEMENT_TYPE = "attributeElementType";
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String ATTRIBUTE_PREFIX = "attributePrefix";
    private static final String TRANSACTION_COLUMN_ATTRIBUTES = "transactionColumnAttributes";
    private static final String VERTEX_COLUMN_ATTRIBUTES = "vertexColumnAttributes";

    @Override
    public String getName() {
        return TableViewConcept.MetaAttribute.TABLE_VIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph,
            final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader,
            final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull() && !jnode.isEmpty()) {
            final boolean selectedOnly = jnode.get("selectedOnly").asBoolean();
            final GraphElementType elementType = GraphElementType.valueOf(jnode.get("elementType").asText());
            final List<Tuple<String, Attribute>> transactionColumnAttributes = new ArrayList<>();
            if (jnode.get(TRANSACTION_COLUMN_ATTRIBUTES) != null) {
                final Iterator<JsonNode> attributeIterator = jnode.get(TRANSACTION_COLUMN_ATTRIBUTES).iterator();
                while (attributeIterator.hasNext()) {
                    final JsonNode attributeNode = attributeIterator.next();
                    final String attributePrefix = attributeNode.get(ATTRIBUTE_PREFIX).asText();
                    final Attribute attribute = new GraphAttribute(graph, graph.getAttribute(
                            GraphElementType.valueOf(attributeNode.get(ATTRIBUTE_ELEMENT_TYPE).asText()),
                            attributeNode.get(ATTRIBUTE_NAME).asText()));
                    transactionColumnAttributes.add(Tuple.create(attributePrefix, attribute));
                }
            }
            final List<Tuple<String, Attribute>> vertexColumnAttributes = new ArrayList<>();
            if (jnode.get(VERTEX_COLUMN_ATTRIBUTES) != null) {
                final Iterator<JsonNode> attributeIterator = jnode.get(VERTEX_COLUMN_ATTRIBUTES).iterator();
                while (attributeIterator.hasNext()) {
                    final JsonNode attributeNode = attributeIterator.next();
                    final String attributePrefix = attributeNode.get(ATTRIBUTE_PREFIX).asText();
                    final Attribute attribute = new GraphAttribute(graph, graph.getAttribute(
                            GraphElementType.valueOf(attributeNode.get(ATTRIBUTE_ELEMENT_TYPE).asText()),
                            attributeNode.get(ATTRIBUTE_NAME).asText()));
                    vertexColumnAttributes.add(Tuple.create(attributePrefix, attribute));
                }
            }

            final TableViewState state = new TableViewState();
            state.setSelectedOnly(selectedOnly);
            state.setElementType(elementType);
            state.setTransactionColumnAttributes(transactionColumnAttributes);
            state.setVertexColumnAttributes(vertexColumnAttributes);

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final TableViewState state = (TableViewState) graph.getObjectValue(attribute.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeBooleanField("selectedOnly", state.isSelectedOnly());
                jsonGenerator.writeStringField("elementType", state.getElementType().name());
                if (state.getTransactionColumnAttributes() == null) {
                    jsonGenerator.writeNullField(TRANSACTION_COLUMN_ATTRIBUTES);
                } else {
                    jsonGenerator.writeArrayFieldStart(TRANSACTION_COLUMN_ATTRIBUTES);
                    for (final Tuple<String, Attribute> columnAttribute : state.getTransactionColumnAttributes()) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(ATTRIBUTE_PREFIX, columnAttribute.getFirst());
                        jsonGenerator.writeStringField(ATTRIBUTE_ELEMENT_TYPE, columnAttribute.getSecond().getElementType().name());
                        jsonGenerator.writeStringField(ATTRIBUTE_NAME, columnAttribute.getSecond().getName());
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                }
                if (state.getVertexColumnAttributes() == null) {
                    jsonGenerator.writeNullField(VERTEX_COLUMN_ATTRIBUTES);
                } else {
                    jsonGenerator.writeArrayFieldStart(VERTEX_COLUMN_ATTRIBUTES);
                    for (final Tuple<String, Attribute> columnAttribute : state.getVertexColumnAttributes()) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(ATTRIBUTE_PREFIX, columnAttribute.getFirst());
                        jsonGenerator.writeStringField(ATTRIBUTE_ELEMENT_TYPE, columnAttribute.getSecond().getElementType().name());
                        jsonGenerator.writeStringField(ATTRIBUTE_NAME, columnAttribute.getSecond().getName());
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                }
                jsonGenerator.writeEndObject();
            }
        }
    }
}

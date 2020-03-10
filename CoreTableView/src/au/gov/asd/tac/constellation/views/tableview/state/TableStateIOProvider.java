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
package au.gov.asd.tac.constellation.views.tableview.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.Segment;
import au.gov.asd.tac.constellation.views.tableview.state.TableState.ColumnState;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.awt.Point;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author algol
 */
public abstract class TableStateIOProvider extends AbstractGraphIOProvider {

    private static final String SELECTED_ONLY = "selectedOnly";
    private static final Logger LOGGER = Logger.getLogger(TableStateIOProvider.class.getName());

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final int posx = jnode.get("posx").asInt(0);
            final int posy = jnode.get("posy").asInt(0);
            final boolean selectedOnly = jnode.hasNonNull(SELECTED_ONLY) ? jnode.get(SELECTED_ONLY).asBoolean(false) : false;

            final TableState state = new TableState(new Point(posx, posy), selectedOnly);

            final ArrayNode columns = (ArrayNode) jnode.withArray("columns");
            for (final JsonNode column : columns) {
                // Null labels means dummy value.
                final String label = column.get("label").isNull() ? null : column.get("label").asText();

                // If width is missing, use a marker <0
                // to indicate deafult width.
                final int width = column.get("width").asInt(-1);

                final String segmentText = column.get("segment").asText();
                try {
                    final Segment segment = Segment.valueOf(segmentText);

                    final ColumnState cs = new ColumnState(label, segment, width);
                    state.columns.add(cs);
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.SEVERE, "Segment value {0} illegal", segmentText);
                    ex.printStackTrace();
                }
            }

            final ArrayNode sortOrder = (ArrayNode) jnode.withArray("sortOrder");
            for (final JsonNode so : sortOrder) {
                final String s = so.asText();
                state.sortOrder.add(s);
            }

            graph.setObjectValue(attributeId, 0, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final TableState state = graph.getObjectValue(attribute.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeNumberField("posx", state.viewPosition.x);
                jsonGenerator.writeNumberField("posy", state.viewPosition.y);
                jsonGenerator.writeBooleanField(SELECTED_ONLY, state.selectedOnly);

                jsonGenerator.writeArrayFieldStart("columns");
                for (final ColumnState cs : state.columns) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("label", cs.label);
                    jsonGenerator.writeStringField("segment", cs.segment.toString());
                    jsonGenerator.writeNumberField("width", cs.width);
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart("sortOrder");
                for (final String so : state.sortOrder) {
                    jsonGenerator.writeString(so);
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();
            }
        }
    }
}

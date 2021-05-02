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
package au.gov.asd.tac.constellation.views.notes.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the NotesViewState object.
 *
 * @author aldebaran30701
 * @author sol695510
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class NotesViewStateIoProvider extends AbstractGraphIOProvider {

    private static final String FILTERS_FIELD = "filters";
    private static final String NOTES_FIELD = "notes";

    @Override
    public String getName() {
        return NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {

        if (!jnode.isNull()) {
            // Reading notes from state.
            final List<NotesViewEntry> noteViewEntries = new ArrayList<>();
            final ArrayNode notesArray = (ArrayNode) jnode.withArray(NOTES_FIELD);

            for (int i = 0; i < notesArray.size(); i++) {
                if (!notesArray.get(i).isNull()) {
                    noteViewEntries.add(new NotesViewEntry(
                            notesArray.get(i).get(0).asText(),
                            notesArray.get(i).get(1).asText(),
                            notesArray.get(i).get(2).asText(),
                            notesArray.get(i).get(3).asBoolean()
                    ));
                }
            }
            // Reading filters from state.
            final List<String> selectedFilters = new ArrayList<>();
            final ArrayNode filtersArray = (ArrayNode) jnode.withArray(FILTERS_FIELD);

            for (int i = 0; i < filtersArray.size(); i++) {
                if (!filtersArray.get(i).isNull()) {
                    selectedFilters.add(filtersArray.get(i).asText());
                }
            }

            final NotesViewState state = new NotesViewState(noteViewEntries, selectedFilters);
            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {

        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final NotesViewState originalState = graph.getObjectValue(attribute.getId(), elementId);

            if (originalState == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                // Make a copy in case the state on the graph is currently being modified.
                final NotesViewState state = new NotesViewState(originalState);

                jsonGenerator.writeObjectFieldStart(attribute.getName());

                jsonGenerator.writeArrayFieldStart(NOTES_FIELD); // Start writing notes to state.

                for (final NotesViewEntry note : state.getNotes()) {
                    if (note == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeStartArray();
                        jsonGenerator.writeString(note.getDateTime());
                        jsonGenerator.writeString(note.getNoteTitle());
                        jsonGenerator.writeString(note.getNoteContent());
                        jsonGenerator.writeBoolean(note.isUserCreated());
                        jsonGenerator.writeEndArray();
                    }
                }

                jsonGenerator.writeEndArray(); // End writing notes to state.

                jsonGenerator.writeArrayFieldStart(FILTERS_FIELD); // Start writing filters to state.

                for (final String filter : state.getFilters()) {
                    if (filter == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeString(filter);
                    }
                }

                jsonGenerator.writeEndArray(); // End writing filters to state.

                jsonGenerator.writeEndObject();
            }
        }
    }
}

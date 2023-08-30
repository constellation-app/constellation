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
import java.util.Iterator;
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
    private static final String TAGS_FILTERS_FIELD = "tags_filters";

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
                    if (notesArray.get(i).get(4) != null) {
                        noteViewEntries.add(new NotesViewEntry(
                                notesArray.get(i).get(0).asText(),
                                notesArray.get(i).get(1).asText(),
                                notesArray.get(i).get(2).asText(),
                                notesArray.get(i).get(3).asBoolean(),
                                notesArray.get(i).get(4).asBoolean(),
                                "#942483",
                                false
                        ));

                        if (notesArray.get(i).get(7) != null) {
                            noteViewEntries.get(i).setNodeColour(notesArray.get(i).get(7).asText());
                        }

                        if (notesArray.get(i).get(8) != null) {
                            noteViewEntries.get(i).setInMarkdown(notesArray.get(i).get(8).asBoolean());
                        }

                        if (notesArray.get(i).get(3).asBoolean() == true && notesArray.get(i).get(4).asBoolean() == false) {

                            final JsonNode nodesArrayNode = notesArray.get(i).get(5);
                            final JsonNode transactionsArrayNode = notesArray.get(i).get(6);

                            // Add the selected nodes
                            if (nodesArrayNode != null) {
                                final List<Integer> selectedNodes = new ArrayList<>();
                                for (int j = 0; j < nodesArrayNode.size(); j++) {
                                    selectedNodes.add(nodesArrayNode.get(j).asInt());
                                }
                                noteViewEntries.get(i).setNodesSelected(selectedNodes);
                            }

                            // Add the selected transactions
                            if (transactionsArrayNode != null) {
                                List<Integer> selectedTransactions = new ArrayList<>();
                                for (int j = 0; j < transactionsArrayNode.size(); j++) {
                                    selectedTransactions.add(transactionsArrayNode.get(j).asInt());
                                }
                                noteViewEntries.get(i).setTransactionsSelected(selectedTransactions);
                            }

                        } else if (notesArray.get(i).get(3).asBoolean() && notesArray.get(i).get(4).asBoolean()) {

                            if (notesArray.get(i).get(5) != null) {
                                noteViewEntries.get(i).setNodeColour(notesArray.get(i).get(5).asText());
                            }

                            if (notesArray.get(i).get(6) != null) {
                                noteViewEntries.get(i).setInMarkdown(notesArray.get(i).get(6).asBoolean());
                            }

                        } else if (notesArray.get(i).get(3).asBoolean() == false) {
                            // Create auto notes with the tags they have assigned to them
                            final JsonNode tagsArrayNode = notesArray.get(i).get(6);
                            if (tagsArrayNode != null) {
                                final List<String> tagsArray = new ArrayList<>();
                                for (int j = 0; j < tagsArrayNode.size(); j++) {
                                    tagsArray.add(tagsArrayNode.get(j).asText());
                                }
                                noteViewEntries.get(i).setTags(tagsArray);
                            }

                        }

                    } else {
                        // If a note was created without a graphAttribute boolean variable, it will now be recreated with the variable
                        // This variable will be true by default, meaning it is applied to the entire graph
                        noteViewEntries.add(new NotesViewEntry(
                                notesArray.get(i).get(0).asText(),
                                notesArray.get(i).get(1).asText(),
                                notesArray.get(i).get(2).asText(),
                                notesArray.get(i).get(3).asBoolean(),
                                true,
                                notesArray.get(i).get(7).asText(),
                                notesArray.get(i).get(8).asBoolean()
                        ));
                    }
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

            // Reading tags filters from state
            final List<String> selectedTagsFilters = new ArrayList<>();
            final ArrayNode tagsFiltersArray = (ArrayNode) jnode.withArray(TAGS_FILTERS_FIELD);

            for (int i = 0; i < tagsFiltersArray.size(); i++) {
                if (!tagsFiltersArray.get(i).isNull()) {
                    selectedTagsFilters.add(tagsFiltersArray.get(i).asText());
                }
            }

            final NotesViewState state = new NotesViewState(noteViewEntries, selectedFilters, selectedTagsFilters);
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
                        if (note.getUndone()) {
                            continue;
                        }

                        jsonGenerator.writeStartArray();
                        jsonGenerator.writeString(note.getDateTime());
                        jsonGenerator.writeString(note.getNoteTitle());
                        jsonGenerator.writeString(note.getNoteContent());
                        jsonGenerator.writeBoolean(note.isUserCreated());
                        jsonGenerator.writeBoolean(note.isGraphAttribute());

                        if (!note.isGraphAttribute() && note.isUserCreated()) {

                            if (note.getNodesSelected() != null) {
                                // Add nodes that are selected to the note
                                final int nodesLength = note.getNodesSelected().size();
                                final int[] nodesArray = new int[nodesLength];
                                final List<Integer> nodesSelected = note.getNodesSelected();
                                final Iterator<Integer> nodesIterator = nodesSelected.iterator();
                                for (int i = 0; i < nodesLength; i++) {
                                    nodesArray[i] = nodesIterator.next();
                                }
                                jsonGenerator.writeArray(nodesArray, 0, nodesLength);
                            }

                            if (note.getTransactionsSelected() != null) {
                                // Add transactions that are selected to the note
                                final int transactionsLength = note.getTransactionsSelected().size();
                                final int[] transactionsArray = new int[transactionsLength];
                                final List<Integer> transactionsSelected = note.getTransactionsSelected();
                                final Iterator<Integer> transactionsIterator = transactionsSelected.iterator();
                                for (int i = 0; i < transactionsLength; i++) {
                                    transactionsArray[i] = transactionsIterator.next();
                                }
                                jsonGenerator.writeArray(transactionsArray, 0, transactionsLength);
                            }

                            jsonGenerator.writeString(note.getNodeColour());

                        } else if (note.isGraphAttribute() && note.isUserCreated()) {
                            jsonGenerator.writeString(note.getNodeColour());
                        }

                        if (note.isUserCreated()) {
                            jsonGenerator.writeBoolean(note.isInMarkdown());
                        }

                        if (!note.isUserCreated()) {
                            final int tagsLength = note.getTags().size();
                            final String[] tagsArray = new String[tagsLength];
                            final Iterator<String> tagsIterator = note.getTags().iterator();
                            for (int i = 0; i < tagsLength; i++) {
                                tagsArray[i] = tagsIterator.next();
                            }

                            jsonGenerator.writeArray(tagsArray, 0, tagsLength);
                        }

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
                jsonGenerator.writeArrayFieldStart(TAGS_FILTERS_FIELD); // Start writing tags filters to state.

                for (final String filter : state.getTagsFilters()) {
                    if (filter == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeString(filter);
                    }
                }
                jsonGenerator.writeEndArray(); // End writing tags filters to state.
                jsonGenerator.writeEndObject();
            }
        }
    }
}

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
package au.gov.asd.tac.constellation.views.namedselection.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelection;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelectionManager;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphIOProvider for NamedSelection instances.
 *
 * @author betelgeuse
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public final class NamedSelectionStateIOProvider extends AbstractGraphIOProvider {
    
    private static final Logger LOGGER = Logger.getLogger(NamedSelectionStateIOProvider.class.getName());

    private static final String QUANTITY = "quantity";
    private static final String ALLOCATED = "allocated";
    private static final String DIM_OTHERS = "dim_others";
    private static final String SELECT_RESULTS = "select_results";
    private static final String SELECTION = "named_selection";
    private static final String SELECTION_ID = "select_id";
    private static final String SELECTION_NAME = "select_name";
    private static final String SELECTION_HOTKEY = "select_hotkey";
    private static final String SELECTION_DESCRIPTION = "select_description";
    private static final String SELECTION_LOCKED = "select_locked";

    /**
     * Helper method to serialise an individual <code>NamedSelection</code> to a
     * JSON node.
     *
     * @param node The parent node of the selection being serialised.
     * @param selection The selection to be serialised.
     *
     * @see NamedSelection
     */
    private static void addSelection(final JsonGenerator jg, final NamedSelection selection) {
        try {
            jg.writeStartObject();
            jg.writeNumberField(SELECTION_ID, selection.getID());
            jg.writeStringField(SELECTION_NAME, selection.getName());
            jg.writeStringField(SELECTION_HOTKEY, selection.getHotkey());
            jg.writeStringField(SELECTION_DESCRIPTION, selection.getDescription());
            jg.writeBooleanField(SELECTION_LOCKED, selection.isLocked());
            jg.writeEndObject();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Helper method that deserialises an individual <code>NamedSelection</code>
     * from a JSON node.
     *
     * @param graph The graph that is being loaded.
     * @param node The node that contains an * * * * * * * * individual
     * <code>NamedSelection</code>.
     * @return A single <code>NamedSelection</code>.
     *
     * @see NamedSelection
     */
    private NamedSelection getSelection(final JsonNode node) {
        final NamedSelection selection = new NamedSelection(node.get(SELECTION_ID).asInt());

        selection.setName(node.get(SELECTION_NAME).asText());
        selection.setHotkey(node.get(SELECTION_HOTKEY).asText());
        selection.setDescription(node.get(SELECTION_DESCRIPTION).asText());
        selection.setLocked(node.get(SELECTION_LOCKED).asBoolean());

        return selection;
    }

    /**
     * Helper method to convert a given long into its corresponding
     * <code>BitSet</code> representation.
     *
     * @param value The long to convert to a its equivalent <code>BitSet</code>.
     * @return The converted <code>BitSet</code>.
     *
     * @see BitSet
     */
    private BitSet convertToBitSet(long value) {
        BitSet bits = new BitSet();
        int index = 0;

        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }

            ++index;
            value = value >>> 1;
        }

        return bits;
    }

    /**
     * Helper method to convert a given <code>BitSet</code> into its
     * corresponding long representation.
     *
     * @param value The <code>BitSet</code> to convert to a its equivalent long.
     * @return The converted long.
     *
     * @see BitSet
     */
    private long convertFromBitSet(final BitSet bits) {
        long value = 0L;

        for (int i = 0; i < bits.length(); i++) {
            value += bits.get(i) ? (1L << i) : 0L;
        }

        return value;
    }

    @Override
    public String getName() {
        return NamedSelectionStateAttributeDescription.ATTRIBUTE_NAME;
    }

    /**
     * Create a new NamedSelectionState object from a JsonNode.
     *
     * @param graph The graph that is being loaded.
     * @param jnode The JsonNode to deserialise a FindState instance from.
     * @throws java.io.IOException
     */
    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final int length = jnode.get(QUANTITY).asInt();

            final NamedSelectionState state = new NamedSelectionState();

            // Get the named selection information off the graph:
            if (length > 0) {
                // Get the currently assigned NamedSelections.
                state.setAllocatedFromGraph(convertToBitSet(jnode.get(ALLOCATED).asLong()));

                // Get the current dim and select results states:
                state.setDimOthers(jnode.get(DIM_OTHERS).asBoolean());
                state.setSelectResults(jnode.get(SELECT_RESULTS).asBoolean());

                final Iterator<JsonNode> itr = jnode.get(SELECTION).iterator();

                while (itr.hasNext()) {
                    final JsonNode node = itr.next();

                    state.addSelectionFromGraph(getSelection(node));
                }
            } else {
                // We don't have anything on the graph, so create a blank BitSet to reflect this:
                state.setAllocatedFromGraph(new BitSet(NamedSelectionManager.getMaximumSelectionCount()));
            }

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final NamedSelectionState state = (NamedSelectionState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                // Note the number of selections being saved:
                jsonGenerator.writeNumberField(QUANTITY, state.getSelectionCount());

                // Write the currently allocated state list:
                jsonGenerator.writeNumberField(ALLOCATED, convertFromBitSet(state.getCurrentlyAllocated()));

                // Write the current dim and select result states:
                jsonGenerator.writeBooleanField(DIM_OTHERS, state.isDimOthers());
                jsonGenerator.writeBooleanField(SELECT_RESULTS, state.isSelectResults());

                jsonGenerator.writeArrayFieldStart(SELECTION);
                for (NamedSelection item : state.getNamedSelections()) {
                    addSelection(jsonGenerator, item);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}

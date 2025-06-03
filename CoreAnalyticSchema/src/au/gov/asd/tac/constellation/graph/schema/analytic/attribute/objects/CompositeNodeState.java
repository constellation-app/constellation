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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * A CompositeNodeState holds the details of a node that is either a composite
 * itself, or part of a currently expanded composite node. For a given node,
 * depending on this categorisation, precisely one of {@link #expandedState} and
 * {@link #contractedState} will be non-null. These objects are used to actually
 * expand or contract a given node.
 * <p>
 * CompositeNodeState objects are stored in a node attribute. This attribute
 * provides a complete picture of composites within the graph.
 * CompositeNodeState objects have methods that assist with displaying,
 * serialising and de-serialising themselves.
 * <p>
 *
 * @see ExpandedCompositeNodeState
 * @see ContractedCompositeNodeState
 * @author twilight_sparkle
 */
public class CompositeNodeState {

    private static final String MEAN = "mean";
    private static final String AFFECTED_EXPANDED_IDS = "affectedExpandedIds";
    private static final String EXPANDED_IDS = "expandedIds";
    private static final String CONSTITUENT_NODE_STORE = "constituentNodeStore";
    private static final String CONTRACTED_STATE = "contractedState";
    private static final String NUMBER_OF_NODES = "numberOfNodes";
    private static final String AFFECTING = "isAffecting";
    private static final String COMPOSITE_ID = "compositeId";
    private static final String COMPOSITE_NODE_STORE = "compositeNodeStore";
    private static final String EXPANDED_STATE = "expandedState";
    private static final String NODE_ID = "nodeId";

    private final int nodeId;
    public final ExpandedCompositeNodeState expandedState;
    public final ContractedCompositeNodeState contractedState;

    /**
     * Create a new CompositeNodeState object for a node that is part of an
     * expanded composite.
     *
     * @param nodeId The graph ID of the node this state is for.
     * @param expandedState The object describing, and allowing contraction of,
     * the expanded composite node to which this node belongs.
     */
    public CompositeNodeState(final int nodeId, final ExpandedCompositeNodeState expandedState) {
        this(nodeId, expandedState, null);
    }

    /**
     * Create a new CompositeNodeState object for a node that itself a
     * contracted composite.
     *
     * @param nodeId The graph ID of the node this state is for.
     * @param contractedState The object describing, and allowing expansion of,
     * the composite node.
     */
    public CompositeNodeState(final int nodeId, final ContractedCompositeNodeState contractedState) {
        this(nodeId, null, contractedState);
    }

    private CompositeNodeState(final int nodeId, final ExpandedCompositeNodeState expandedState, final ContractedCompositeNodeState contractedState) {
        this.nodeId = nodeId;
        this.expandedState = expandedState;
        this.contractedState = contractedState;
    }

    /**
     * Get the number of nodes that belong to the expanded or contracted
     * composite related to this sate.
     *
     * @return The number of nodes belonging to the composite related to this
     * state.
     */
    public int getNumberOfNodes() {
        if (expandedState == null && contractedState == null) {
            return 0;
        } else if (contractedState != null) {
            return contractedState.getNumberOfNodes();
        } else {
            return expandedState.getNumberOfNodes();
        }
    }

    /**
     * Get the {@link CompositeStatus} corresponding to this state.
     *
     * @return The {@link CompositeStatus} corresponding to this state.
     */
    public CompositeStatus getStatus() {
        if (expandedState == null && contractedState == null) {
            return CompositeStatus.NOT_A_COMPOSITE;
        } else if (contractedState != null) {
            return CompositeStatus.IS_A_COMPOSITE;
        } else if (expandedState.isAffectingNode()) {
            return CompositeStatus.LEADER_OF_A_COMPOSITE;
        } else {
            return CompositeStatus.PART_OF_A_COMPOSITE;
        }
    }

    /**
     * Is this state for a node that is itself a contracted composite?
     *
     * @return True if the node is a composite, false otherwise.
     */
    public boolean isComposite() {
        return getStatus() == CompositeStatus.IS_A_COMPOSITE;
    }

    /**
     * Is this state for a node that is part of an expanded composite?
     *
     * @return True if the node is a part of a composite, false otherwise.
     */
    public boolean comprisesAComposite() {
        return getStatus() == CompositeStatus.LEADER_OF_A_COMPOSITE || getStatus() == CompositeStatus.PART_OF_A_COMPOSITE;
    }

    /**
     * Serialise this state as a JSON Node and convert the JSON to a string.
     *
     * @return The serialised String representation of this state,
     * @throws IllegalArgumentException If there is an issue with building the
     * JSON from this state.
     */
    public String convertToString() {
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (final JsonGenerator jg = new JsonFactory().createGenerator(outputStream)) {
                jg.writeStartObject();

                jg.writeNumberField(NODE_ID, nodeId);
                if (expandedState == null) {
                    jg.writeNullField(EXPANDED_STATE);
                } else {
                    jg.writeFieldName(EXPANDED_STATE);
                    jg.writeStartObject();
                    jg.writeStringField(COMPOSITE_NODE_STORE, RecordStoreUtilities.toJson(expandedState.getCompositeNodeStore()));
                    jg.writeStringField(COMPOSITE_ID, expandedState.getCompositeId());
                    jg.writeBooleanField(AFFECTING, expandedState.isAffectingNode());
                    jg.writeNumberField(NUMBER_OF_NODES, expandedState.getNumberOfNodes());
                    jg.writeEndObject();
                }
                if (contractedState == null) {
                    jg.writeNullField(CONTRACTED_STATE);
                } else {
                    jg.writeFieldName(CONTRACTED_STATE);
                    jg.writeStartObject();
                    jg.writeStringField(CONSTITUENT_NODE_STORE, RecordStoreUtilities.toJson(contractedState.getConstituentNodeStore()));
                    jg.writeArrayFieldStart(EXPANDED_IDS);
                    for (final String id : contractedState.getExpandedIds()) {
                        jg.writeString(id);
                    }
                    jg.writeEndArray();
                    jg.writeArrayFieldStart(AFFECTED_EXPANDED_IDS);
                    for (final String id : contractedState.getAffectedExpandedIds()) {
                        jg.writeString(id);
                    }
                    jg.writeEndArray();
                    jg.writeArrayFieldStart(MEAN);
                    for (final float coord : contractedState.getMean()) {
                        jg.writeNumber(coord);
                    }
                    jg.writeEndArray();
                    jg.writeEndObject();
                }

                jg.writeEndObject();
            }

            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error converting this composite node state to a string");
        }
    }

    /**
     * De-serialise this state from the String representation of a JSON Node.
     *
     * @param s The composite state string
     *
     * @return The De-serialised CompositeNodeState.
     * @throws IllegalArgumentException If there is an issue with reading the
     * JSON.
     */
    public static CompositeNodeState createFromString(final String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        try (final JsonParser parser = new MappingJsonFactory().createParser(s)) {
            final JsonNode jn = parser.readValueAsTree();
            final int nodeId = jn.get(NODE_ID).asInt();

            final ExpandedCompositeNodeState expandedState;
            if (jn.hasNonNull(EXPANDED_STATE)) {
                final JsonNode expandedNode = jn.get(EXPANDED_STATE);
                final RecordStore compositeNodeStore = RecordStoreUtilities.fromJson(new ByteArrayInputStream(expandedNode.get(COMPOSITE_NODE_STORE).asText().getBytes(StandardCharsets.UTF_8.name())));
                final String compositeId = expandedNode.get(COMPOSITE_ID).asText();
                final boolean isAffecting = expandedNode.get(AFFECTING).asBoolean();
                final int numberOfNodes = expandedNode.get(NUMBER_OF_NODES).asInt();
                expandedState = new ExpandedCompositeNodeState(compositeNodeStore, compositeId, isAffecting, numberOfNodes);
            } else {
                expandedState = null;
            }

            final ContractedCompositeNodeState contractedState;
            if (jn.hasNonNull(CONTRACTED_STATE)) {
                final JsonNode contractedNode = jn.get(CONTRACTED_STATE);
                final RecordStore constituentNodeStore = RecordStoreUtilities.fromJson(new ByteArrayInputStream(contractedNode.get(CONSTITUENT_NODE_STORE).asText().getBytes(StandardCharsets.UTF_8.name())));
                final List<String> expandedIds = new ArrayList<>();
                final Iterator<JsonNode> expandedIdsIterator = contractedNode.get(EXPANDED_IDS).iterator();
                while (expandedIdsIterator.hasNext()) {
                    expandedIds.add(expandedIdsIterator.next().asText());
                }
                final List<String> affectedExpandedIds = new ArrayList<>();
                final Iterator<JsonNode> affectedExpandedIdsIterator = contractedNode.get(AFFECTED_EXPANDED_IDS).iterator();
                while (affectedExpandedIdsIterator.hasNext()) {
                    affectedExpandedIds.add(affectedExpandedIdsIterator.next().asText());
                }
                final float[] mean = new float[3];
                final Iterator<JsonNode> meanIterator = contractedNode.get(MEAN).iterator();
                int i = 0;
                while (meanIterator.hasNext() && i < 3) {
                    mean[i++] = (float) meanIterator.next().asDouble();
                }
                contractedState = new ContractedCompositeNodeState(constituentNodeStore, expandedIds, affectedExpandedIds, mean);
            } else {
                contractedState = null;
            }

            return new CompositeNodeState(nodeId, expandedState, contractedState);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error converting this string to a composite node state");
        }
    }
}

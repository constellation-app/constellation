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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class HierarchicalStateIoProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return HierarchicalStateAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) {
        if (!jnode.isNull()) {
            final HierarchicalState coi = new HierarchicalState();
            if (jnode.has("mod_count")) {
                coi.modificationCounter = jnode.get("mod_count").asLong();
            }
            if (jnode.has("struc_mod_count")) {
                coi.strucModificationCount = jnode.get("struc_mod_count").asLong();
            }

            if (jnode.has("interactive")) {
                coi.interactive = jnode.get("interactive").asBoolean();
            }

            if (jnode.has("colored")) {
                coi.colored = jnode.get("colored").asBoolean();
            }

            coi.steps = jnode.get("steps").intValue();
            coi.currentStep = jnode.get("current_step").asInt();
            if (jnode.has("optimum_step")) {
                coi.optimumStep = jnode.get("optimum_step").asInt();
            }
            coi.excludeSingleVertices = jnode.get("exclude_single_vertices").asBoolean();
            coi.excludedElementsDimmed = jnode.get("exclude_elements_dimmed").asBoolean();
            coi.redrawCount = jnode.get("redraw_count").asInt();

            coi.clusterNumbers = new int[graph.getVertexCapacity()];
            final ArrayNode cnNode = (ArrayNode) jnode.get("cluster_numbers");
            int ix = 0;
            for (final JsonNode jn : cnNode) {
                if (vertexMap.containsKey(ix)) {
                    coi.clusterNumbers[vertexMap.get(ix)] = jn.intValue();
                }

                ix++;
            }

            coi.clusterSeenBefore = new int[graph.getVertexCapacity()];
            final ArrayNode csbNode = (ArrayNode) jnode.get("cluster_seen_before");
            ix = 0;
            for (final JsonNode jn : csbNode) {
                if (vertexMap.containsKey(ix)) {
                    coi.clusterSeenBefore[vertexMap.get(ix)] = jn.intValue();
                }

                ix++;
            }

            coi.groups = new FastNewman.Group[graph.getVertexCapacity()];
            final int[] parentLinks = new int[graph.getVertexCapacity()];
            Arrays.fill(parentLinks, Graph.NOT_FOUND);

            final ArrayNode groupsNode = (ArrayNode) jnode.get("groups");
            for (final JsonNode jn : groupsNode) {
                final FastNewman.Group group;
                if (jn.isNull()) {
                    group = null;
                } else {
                    group = new FastNewman.Group();
                    final int jsonId = jn.get("vertex").asInt();
                    group.vertex = vertexMap.get(jsonId);
                    group.mergeStep = jn.get("merge_step").asInt();
                    group.singleStep = jn.get("single_step").asInt();
                    group.color = ConstellationColor.getColorValue(jn.get("color").asText());

                    final int groupIx = graph.getVertexPosition(group.vertex);
                    coi.groups[groupIx] = group;

                    if (jn.has("parent")) {
                        final int parentVxId = vertexMap.get(jn.get("parent").asInt());
                        parentLinks[groupIx] = graph.getVertexPosition(parentVxId);
                    }
                }
            }

            // Use the parentLinks indices to hook up the Group parents.
            for (int i = 0; i < parentLinks.length; i++) {
                if (parentLinks[i] != Graph.NOT_FOUND) {
                    coi.groups[i].parent = coi.groups[parentLinks[i]];
                }
            }

            graph.setObjectValue(attributeId, elementId, coi);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final HierarchicalState state = (HierarchicalState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {

                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeNumberField("mod_count", state.modificationCounter);
                jsonGenerator.writeNumberField("struc_mod_count", state.strucModificationCount);
                jsonGenerator.writeNumberField("steps", state.steps);
                jsonGenerator.writeNumberField("current_step", state.currentStep);
                jsonGenerator.writeNumberField("optimum_step", state.optimumStep);
                jsonGenerator.writeBooleanField("exclude_single_vertices", state.excludeSingleVertices);
                jsonGenerator.writeBooleanField("exclude_elements_dimmed", state.excludedElementsDimmed);
                jsonGenerator.writeNumberField("redraw_count", state.redrawCount);
                jsonGenerator.writeBooleanField("interactive", state.interactive);
                jsonGenerator.writeBooleanField("colored", state.colored);

                jsonGenerator.writeArrayFieldStart("cluster_numbers");
                for (final int value : state.clusterNumbers) {
                    jsonGenerator.writeNumber(value);
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart("cluster_seen_before");
                for (final int value : state.clusterSeenBefore) {
                    jsonGenerator.writeNumber(value);
                }
                jsonGenerator.writeEndArray();

                // TODO: groups array is sized for capacity, not max vertex id: this means too much stuff is being written.
                jsonGenerator.writeArrayFieldStart("groups");
                for (final FastNewman.Group group : state.groups) {
                    if (group != null) {
                        jsonGenerator.writeStartObject();
                        graph.getVertexPosition(state.steps);
                        jsonGenerator.writeNumberField("vertex", group.vertex);
                        jsonGenerator.writeNumberField("merge_step", group.mergeStep);
                        jsonGenerator.writeNumberField("single_step", group.singleStep);
                        jsonGenerator.writeStringField("color", group.color.toString());

                        if (group.parent != null) {
                            // Get the vertex of the parent of this Group.
                            final int parentVxId = group.parent.vertex;
                            jsonGenerator.writeNumberField("parent", parentVxId);
                        }
                        jsonGenerator.writeEndObject();
                    } else {
                        jsonGenerator.writeNull();
                    }
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}

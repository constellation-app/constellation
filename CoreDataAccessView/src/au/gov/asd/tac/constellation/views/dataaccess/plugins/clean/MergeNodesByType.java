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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.LEAD_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGER_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.SELECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.THRESHOLD_PARAMETER_ID;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Merge nodes based on the type, overriding nodes with the supported type
 *
 * @author arcturus
 */
@ServiceProvider(service = MergeNodeType.class)
public class MergeNodesByType implements MergeNodeType {

    private static final String MERGE_TYPE_NAME = "Supported Type";

    @Override
    public String getName() {
        return MERGE_TYPE_NAME;
    }

    @Override
    public void updateParameters(final Map<String, PluginParameter<?>> parameters) {
        parameters.get(MERGE_TYPE_PARAMETER_ID).setEnabled(true);
        parameters.get(THRESHOLD_PARAMETER_ID).setEnabled(false);
        parameters.get(MERGER_PARAMETER_ID).setEnabled(true);
        parameters.get(LEAD_PARAMETER_ID).setEnabled(false);
        parameters.get(SELECTED_PARAMETER_ID).setEnabled(true);
    }

    @Override
    public final Map<Integer, Set<Integer>> getNodesToMerge(final GraphWriteMethods graph, final Comparator<String> leadVertexChooser, final int threshold, final boolean selectedOnly) {
        final Map<String, Map<Integer, SchemaVertexType>> typeMap = new HashMap<>();
        final Map<Integer, Set<Integer>> nodesToMerge = new HashMap<>();

        final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        if (identifierAttribute == Graph.NOT_FOUND) {
            return nodesToMerge;
        }

        final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        if (typeAttribute == Graph.NOT_FOUND) {
            return nodesToMerge;
        }

        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vxId = graph.getVertex(vertexPosition);
            final String identifier = graph.getStringValue(identifierAttribute, vxId);
            final SchemaVertexType type = graph.getObjectValue(typeAttribute, vxId);

            if (!selectedOnly || graph.getBooleanValue(selectedAttribute, vxId)) {
                Map<Integer, SchemaVertexType> matchingVertices = typeMap.get(identifier);
                if (matchingVertices == null) {
                    matchingVertices = new LinkedHashMap<>();
                    typeMap.put(identifier, matchingVertices);
                }
                matchingVertices.put(vxId, type);
            }
        }

        // get a list type names for a quick lookup
        final Collection<? extends SchemaVertexType> supportedTypes = SchemaVertexTypeUtilities.getTypes();
        for (final Map<Integer, SchemaVertexType> matchingVertices : typeMap.values()) {
            if (matchingVertices.size() > 1) {
                int leadVertex = Graph.NOT_FOUND;
                for (Map.Entry<Integer, SchemaVertexType> entry : matchingVertices.entrySet()) {
                    if (supportedTypes.contains(entry.getValue())) {
                        leadVertex = entry.getKey();
                        break;
                    }
                }

                if (leadVertex != Graph.NOT_FOUND) {
                    nodesToMerge.put(leadVertex, matchingVertices.keySet());
                }
            }
        }

        return nodesToMerge;
    }
}

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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.functionality.dialog.ItemsRow;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.LEAD_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGER_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.MERGE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.SELECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin.THRESHOLD_PARAMETER_ID;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Merge nodes based on the identifier suffix length
 *
 * @author arcturus
 */
@ServiceProvider(service = MergeNodeType.class)
public class MergeNodesBySuffix implements MergeNodeType {

    private static final Logger LOGGER = Logger.getLogger(MergeNodesBySuffix.class.getName());

    private static final String MERGE_TYPE_NAME = "Identifier Suffix Length";

    @Override
    public String getName() {
        return MERGE_TYPE_NAME;
    }

    @Override
    public void updateParameters(final Map<String, PluginParameter<?>> parameters) {
        parameters.get(MERGE_TYPE_PARAMETER_ID).setEnabled(true);
        parameters.get(THRESHOLD_PARAMETER_ID).setDescription("The suffix length to consider");
        parameters.get(THRESHOLD_PARAMETER_ID).setIntegerValue(9);
        parameters.get(THRESHOLD_PARAMETER_ID).setEnabled(true);
        parameters.get(MERGER_PARAMETER_ID).setEnabled(true);
        parameters.get(LEAD_PARAMETER_ID).setEnabled(true);
        parameters.get(SELECTED_PARAMETER_ID).setEnabled(true);
    }

    @Override
    public final Map<Integer, Set<Integer>> getNodesToMerge(final GraphWriteMethods graph, final Comparator<String> leadVertexChooser, final int threshold, final boolean selectedOnly) {
        final Map<String, Map<Integer, String>> prefixMap = new HashMap<>();
        final Map<Integer, Set<Integer>> nodesToMerge = new HashMap<>();

        final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        if (identifierAttribute != Graph.NOT_FOUND) {
            final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
            final int vertexCount = graph.getVertexCount();
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertex = graph.getVertex(vertexPosition);

                final String identifier = graph.getStringValue(identifierAttribute, vertex);
                if (identifier.length() >= threshold && (!selectedOnly || graph.getBooleanValue(selectedAttribute, vertex))) {
                    String key = reverseString(identifier);
                    key = key.substring(0, threshold);
                    Map<Integer, String> matchingVertices = prefixMap.get(key);
                    if (matchingVertices == null) {
                        matchingVertices = new LinkedHashMap<>();
                        prefixMap.put(key, matchingVertices);
                    }
                    matchingVertices.put(vertex, identifier);
                }
            }

            for (final Map<Integer, String> matchingVertices : prefixMap.values()) {
                if (matchingVertices.size() > 1) {
                    int leadVertex = Graph.NOT_FOUND;
                    String leadValue = null;
                    if (leadVertexChooser != null) {
                        // calculate the lead vertex
                        for (final Map.Entry<Integer, String> e : matchingVertices.entrySet()) {
                            if (leadVertex < 0 || leadVertexChooser.compare(e.getValue(), leadValue) < 0) {
                                leadVertex = e.getKey();
                                leadValue = e.getValue();
                            }
                        }
                    } else {
                        // ask user to choose the lead vertex
                        final ObservableList<ItemsRow<Integer>> duplicateVertices = FXCollections.observableArrayList();
                        for (final Map.Entry<Integer, String> e : matchingVertices.entrySet()) {
                            duplicateVertices.add(new ItemsRow<>(e.getKey(), e.getValue(), ""));
                        }

                        final int[] selected = new int[1];
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(() -> {
                            final LeadNodeSelectionDialog dialog = new LeadNodeSelectionDialog(null, duplicateVertices, null);
                            dialog.setOkButtonAction(e2 -> {
                                dialog.hideDialog();
                                selected[0] = dialog.getLeadVertexId();
                                latch.countDown();
                            });

                            dialog.setCancelButtonAction(e2 -> {
                                dialog.hideDialog();
                                selected[0] = -1;
                                latch.countDown();
                            });

                            dialog.showDialog();
                        });

                        try {
                            latch.await();
                            leadVertex = selected[0];
                        } catch (final InterruptedException ex) {
                            LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
                            Thread.currentThread().interrupt();
                        }
                    }

                    if (leadVertex != Graph.NOT_FOUND) {
                        nodesToMerge.put(leadVertex, matchingVertices.keySet());
                    }
                }
            }
        }

        return nodesToMerge;
    }

    private String reverseString(final String string) {
        final char[] stringAsCharArray = string.toCharArray();
        for (int i = 0, j = string.length() - 1; i < string.length() / 2; i++, j--) {
            final char c = stringAsCharArray[i];
            stringAsCharArray[i] = stringAsCharArray[j];
            stringAsCharArray[j] = c;
        }
        return String.valueOf(stringAsCharArray);
    }
}

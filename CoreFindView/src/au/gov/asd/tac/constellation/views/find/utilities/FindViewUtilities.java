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
package au.gov.asd.tac.constellation.views.find.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Utility class for the Find View
 *
 * @author Delphinus8821
 */
public class FindViewUtilities {

    private static final Logger LOGGER = Logger.getLogger(FindViewUtilities.class.getName());

    private FindViewUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Changes the active graph to the one where a graph element has been found and selected
     * 
     * @param graph
     * @param zoomToSelection
     */
    public static void searchAllGraphs(final GraphWriteMethods graph, final boolean zoomToSelection) {
        final Set<TopComponent> topComponents = WindowManager.getDefault().getRegistry().getOpened();
        if (topComponents != null) {
            for (final TopComponent component : topComponents) {
                if (component instanceof VisualGraphTopComponent vgtComponent && vgtComponent.getGraphNode().getGraph().getId().equals(graph.getId())) {
                    try {
                        EventQueue.invokeAndWait(vgtComponent::requestActive);
                        if (zoomToSelection) {
                            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.ZOOM_TO_SELECTION).executeLater(GraphManager.getDefault().getActiveGraph());
                        } else {
                            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeLater(GraphManager.getDefault().getActiveGraph());
                        }
                        break;
                    } catch (final InterruptedException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                        Thread.currentThread().interrupt();
                    } catch (final InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                    }
                }
            }
        }

    }

    /**
     * This function clears all the currently selected elements on the graph.
     *
     * @param graph
     */
    public static void clearSelection(final GraphWriteMethods graph) {
        final int nodesCount = GraphElementType.VERTEX.getElementCount(graph);
        final int nodeSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionsCount = GraphElementType.TRANSACTION.getElementCount(graph);
        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        // loop through all nodes that are selected and deselect them
        if (nodeSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < nodesCount; i++) {
                final int currElement = GraphElementType.VERTEX.getElement(graph, i);
                graph.setBooleanValue(nodeSelectedAttribute, currElement, false);
            }
        }
        // loop through all transactions that are selected and deselect them
        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < transactionsCount; i++) {
                final int currElement = GraphElementType.TRANSACTION.getElement(graph, i);
                graph.setBooleanValue(transactionSelectedAttribute, currElement, false);
            }
        }
    }
}

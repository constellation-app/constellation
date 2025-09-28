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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.find.utilities.FindResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all of the logic for performing searches on a given
 * <code>Graph</code>.
 * <p>
 * There are two main ways that searches can be performed. These are known as
 * <i>quickQuery</i>, and <i>advancedQuery</i>.
 * <p>
 * <code>quickQuery()</code>s can be used to quickly check all aspects of a
 * given <code>Graph</code> for any occurrences of a given string.
 * <p>
 * <code>advancedQuery()</code>s can be used to perform targeted searches with
 * very specific criteria, across different types of attributes (such as
 * <code>Float</code>s, <code>String</code>s, and <code>Date</code>s amongst
 * others), with any results being constrained to a single GraphElementType.
 * This mode would typically be used by services such as the
 * <code>FindTopComponent</code>.
 * <p>
 * It should be noted that all search operations are multi-threaded, and are
 * performed in parallel when there are sufficient resources on the platform.
 *
 * @author betelgeuse
 */
public class QueryServices {
    
    private static final Logger LOGGER = Logger.getLogger(QueryServices.class.getName());

    private static final int AVAILABLE_THREADS = Math.max(1, (Runtime.getRuntime().availableProcessors() - 1));
    private static final int MAX_THRESHOLD = 10000; // The maximum number of items to assign each thread (until we don't have enough threads anyway).
    private static final String SELECTED = "selected";
    private Graph graph;
    private List<FindResult> findResults = new ArrayList<>();
    
    private static final String THREAD_INTERRUPTED = "Thread was interrupted";

    /**
     * Constructs a new <code>QueryServices</code>.
     *
     * @param context the graph on which to perform the query.
     */
    public QueryServices(final Graph context) {
        this.graph = context;
    }

    /**
     * Performs a 'quick query'.
     * <p>
     * This method determines the maximum number of needed threads, and assigns
     * a work package to each thread. Upon all child threads completing their
     * queries, it joins and returns the results.
     *
     * @param type The <code>GraphElementType</code> to perform a quick query
     * on.
     * @param content The string to find instances of across the graph.
     * @return List of <code>FindResults</code>, with each
     * <code>FindResult</code> representing an individual positive result to the
     * query.
     *
     * @see ArrayList
     * @see FindResult
     * @see GraphElementType
     */
    public List<FindResult> quickQuery(final GraphElementType type, final String content) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int sampleSpaceSize = type.getElementCount(rg);
            if (sampleSpaceSize > 0) {
                final int neededThreads = Math.min(AVAILABLE_THREADS, (int) Math.ceil((double) sampleSpaceSize / (double) MAX_THRESHOLD));
                final int loadPerThread = (int) Math.ceil((double) sampleSpaceSize / (double) neededThreads);
                final CyclicBarrier barrier = new CyclicBarrier(neededThreads + 1);

                try {
                    final ThreadedFind[] worker = new ThreadedFind[neededThreads];
                    // Create the requisite number of workers:
                    for (int i = 0; i < neededThreads; i++) {
                        final int workloadLBound = i * loadPerThread;
                        final int workloadUBound = Math.min((sampleSpaceSize - 1), ((i + 1) * loadPerThread) - 1);

                        worker[i] = new ThreadedFind(rg, barrier, this, type, content, i, workloadLBound, workloadUBound);

                        // Start the worker now that it knows its workload:
                        final Thread t = new Thread(worker[i]);
                        t.start();
                    }
                } finally {
                    try {
                        barrier.await();
                    } catch (final InterruptedException ex) {
                        LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED, ex);
                        Thread.currentThread().interrupt();
                    } catch (final BrokenBarrierException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                }
            }
            return findResults;
        } finally {
            rg.release();
        }
    }

    /**
     * Selects each item in the given list on the graph.
     *
     * @param graph the graph on which to perform the selection.
     * @param content A list of <code>FindResult</code>s, with each item
     * representing one match on the graph.
     * @param clearSelection <code>true</code> to clear previously selected
     * items on the graph, <code>false</code> to add to them.
     */
    public static void selectOnGraph(final GraphWriteMethods graph, final List<FindResult> content, final boolean clearSelection) {
        if (clearSelection) {
            clearSelection(graph);
        }

        final int selectedVertexAttr = graph.getAttribute(GraphElementType.VERTEX, SELECTED);
        final int selectedLinkAttr = graph.getAttribute(GraphElementType.LINK, SELECTED);
        final int selectedEdgeAttr = graph.getAttribute(GraphElementType.EDGE, SELECTED);
        final int selectedTranAttr = graph.getAttribute(GraphElementType.TRANSACTION, SELECTED);

        for (FindResult result : content) {
            final int id = result.getID();
            final GraphElementType type = result.getType();
            final long uid = type.getUID(graph, id);

            if (type.elementExists(graph, id) && uid == result.getUID()) {
                // Select relevant:
                if (result.getType() == GraphElementType.VERTEX && selectedVertexAttr != Graph.NOT_FOUND) {
                    graph.setBooleanValue(selectedVertexAttr, result.getID(), true);
                }
                if (result.getType() == GraphElementType.LINK) {
                    if (selectedLinkAttr != Graph.NOT_FOUND) {
                        graph.setBooleanValue(selectedLinkAttr, result.getID(), true);
                    }

                    // Select any child edges:
                    final int edgeCount = graph.getLinkEdgeCount(result.getID());

                    if (selectedEdgeAttr != Graph.NOT_FOUND) {
                        for (int position = 0; position < edgeCount; position++) {
                            final int edge = graph.getLinkEdge(result.getID(), position);
                            graph.setBooleanValue(selectedEdgeAttr, edge, true);
                        }
                    }

                    // Select any child transactions:
                    final int transactionCount = graph.getLinkTransactionCount(result.getID());

                    if (selectedTranAttr != Graph.NOT_FOUND) {
                        for (int position = 0; position < transactionCount; position++) {
                            final int transaction = graph.getLinkTransaction(result.getID(), position);
                            graph.setBooleanValue(selectedTranAttr, transaction, true);
                        }
                    }

                }
                if (result.getType() == GraphElementType.EDGE) {
                    if (selectedEdgeAttr != Graph.NOT_FOUND) {
                        graph.setBooleanValue(selectedEdgeAttr, result.getID(), true);
                    }

                    // Select any child transactions:
                    final int transactionCount = graph.getEdgeTransactionCount(result.getID());

                    if (selectedEdgeAttr != Graph.NOT_FOUND) {
                        for (int position = 0; position < transactionCount; position++) {
                            final int transaction = graph.getEdgeTransaction(result.getID(), position);
                            graph.setBooleanValue(selectedTranAttr, transaction, true);
                        }
                    }
                }
                if (result.getType() == GraphElementType.TRANSACTION && selectedTranAttr != Graph.NOT_FOUND) {
                    graph.setBooleanValue(selectedTranAttr, result.getID(), true);
                }
            }
        }
    }

    /**
     * Iterates over the entire graph and removes and sets all instances of the
     * selected attribute to false.
     */
    private static void clearSelection(final GraphWriteMethods graph) {
        final int selectedVertexAttr = graph.getAttribute(GraphElementType.VERTEX, SELECTED);
        final int selectedLinkAttr = graph.getAttribute(GraphElementType.LINK, SELECTED);
        final int selectedEdgeAttr = graph.getAttribute(GraphElementType.EDGE, SELECTED);
        final int selectedTranAttr = graph.getAttribute(GraphElementType.TRANSACTION, SELECTED);

        // Unselect everything:
        if (selectedVertexAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getVertexCount(); i++) {
                graph.setBooleanValue(selectedVertexAttr, i, false);
            }
        }
        if (selectedLinkAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getLinkCount(); i++) {
                graph.setBooleanValue(selectedLinkAttr, i, false);
            }
        }
        if (selectedEdgeAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getEdgeCount(); i++) {
                graph.setBooleanValue(selectedEdgeAttr, i, false);
            }
        }
        if (selectedTranAttr != Graph.NOT_FOUND) {
            for (int i = 0; i < graph.getTransactionCount(); i++) {
                graph.setBooleanValue(selectedTranAttr, i, false);
            }
        }
    }

    /**
     * Private class responsible for handling queries in a threaded manner.
     * <p>
     * Each thread is assigned an instance of this internal class.
     */
    private class ThreadedFind implements Runnable {

        private final GraphReadMethods rg;
        private final CyclicBarrier barrier;
        private final QueryServices parent;
        private final int threadID;
        // Simple mode variables:
        private int workloadLBound = -1;
        private int workloadUBound = -1;
        private final String content;
        private final GraphElementType type;

        /**
         * Constructs a new <code>ThreadedFind</code>, and prepares it for a
         * 'quick query'.
         * <p>
         * This constructor should only be used for quick queries.
         * <p>
         * This mode works by splitting the graph into roughly equal sections,
         * with each section being searched by an individual instance of
         * <code>ThreadedFind</code>. All results are returned in this mode,
         * with no 'AND' or 'OR' checks occurring.
         *
         * @param barrier <code>CyclicBarrier</code> used to handle
         * multi-threadedness.
         * @param parent The <code>QueryServices</code> that instantiated this
         * <code>ThreadedFind</code>.
         * @param type The <code>GraphElementType</code> to perform the quick
         * search operation on.
         * @param content The string to search the graph for instances of.
         * @param threadID The id of this thread.
         * @param workloadLBound The lower index of the graph that this instance
         * of <code>ThreadedFind</code> is responsible for querying.
         * @param workloadUBound The upper index of the graph that this instance
         * of <code>ThreadedFind</code> is responsible for querying.
         *
         * @see CyclicBarrier
         * @see QueryServices
         * @see GraphElementType
         */
        public ThreadedFind(final GraphReadMethods rg, final CyclicBarrier barrier, final QueryServices parent,
                final GraphElementType type, final String content, final int threadID,
                final int workloadLBound, final int workloadUBound) {
            this.rg = rg;
            this.barrier = barrier;
            this.parent = parent;
            this.type = type;
            this.content = content;
            this.threadID = threadID;
            this.workloadLBound = workloadLBound;
            this.workloadUBound = workloadUBound;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName("Find.FindServices.Thread." + threadID);
                quickFind();
            } finally {
                // This thread is now done, so wait for all others to finish:
                try {
                    barrier.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED);
                    Thread.currentThread().interrupt();
                } catch (final BrokenBarrierException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        }

        // <editor-fold defaultstate="collapsed" desc="Quick Find Logic">
        /**
         * Searches the graph for occurrences of the given string.
         * <p>
         * Removes the recent search suffix from the search if its found in the
         * search text. For example if the recent search was "Orange : Name"
         * then just search for "Orange"
         */
        private void quickFind() {
            for (int i = 0; i < rg.getAttributeCount(type); i++) {
                final int attrID = rg.getAttribute(type, i);
                final String recentSearchSuffix = FindResult.SEPARATOR + rg.getAttributeName(attrID);
                final String searchText = content.contains(recentSearchSuffix) ? content.replace(recentSearchSuffix, "") : content;

                for (int elementPosition = workloadLBound; elementPosition <= workloadUBound; elementPosition++) {
                    final int elementId = type.getElement(rg, elementPosition);
                    final long elementUid = type.getUID(rg, elementId);
                    final String retrieved = rg.getStringValue(attrID, elementId);

                    // Check if we have a match:
                    if (retrieved != null && retrieved.toLowerCase().contains(searchText.toLowerCase())) {
                        final FindResult fr = new FindResult(elementId, elementUid, type, new GraphAttribute(rg, attrID).getName(), retrieved, null);
                        parent.findResults.add(fr);
                    }
                }
            }
        }
    }
}

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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
    private boolean[] results;
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
        ReadableGraph rg = graph.getReadableGraph();
        try {
            final int sampleSpaceSize = type.getElementCount(rg);

            if (sampleSpaceSize > 0) {
                final int neededThreads = Math.min(AVAILABLE_THREADS, (int) Math.ceil((double) sampleSpaceSize / (double) MAX_THRESHOLD));
                final int loadPerThread = (int) Math.ceil((double) sampleSpaceSize / (double) neededThreads);

                final CyclicBarrier barrier = new CyclicBarrier(neededThreads + 1);

                try {
                    ThreadedFind[] worker = new ThreadedFind[neededThreads];

                    // Create the requisite number of workers:
                    for (int i = 0; i < neededThreads; i++) {
                        final int workloadLBound = i * loadPerThread;
                        final int workloadUBound = Math.min((sampleSpaceSize - 1), ((i + 1) * loadPerThread) - 1);

                        worker[i] = new ThreadedFind(rg, barrier, this, type, content, i, workloadLBound, workloadUBound);

                        // Start the worker now that it knows its workload:
                        Thread t = new Thread(worker[i]);
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

                return findResults; // Successfully found content.
            }

            return findResults;
        } finally {
            rg.release();
        }
    }

    /**
     * Performs an 'advanced query'.
     * <p>
     * This method determines the maximum number of needed threads, and assigns
     * a work package to each thread. Upon all child threads completing their
     * queries, it joins and returns the results.
     *
     * @param rules List of individual rules to perform queries for.
     * @param type The <code>GraphElementType</code> to perform a quick query
     * on.
     * @param isAnd <code>true</code> to perform 'AND' based search,
     * <code>false</code> to perform 'OR'.
     *
     * @return List of <code>FindResults</code>, with each
     * <code>FindResult</code> representing an individual positive result to the
     * query.
     *
     * @see ArrayList
     * @see FindResult
     * @see GraphElementType
     */
    public List<FindResult> advancedQuery(final ArrayList<FindRule> rules, final GraphElementType type, final boolean isAnd) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {

            results = new boolean[type.getElementCount(rg)];
            Arrays.fill(results, isAnd);

            final int numThreadsNeeded = Math.min(AVAILABLE_THREADS, rules.size());
            final CyclicBarrier barrier = new CyclicBarrier(numThreadsNeeded + 1);

            try {
                // Determine the workpackage for each thread. Each 'package' is comprised of 1 or more FindRules.
                final List<List<FindRule>> workPackage = new ArrayList<>(numThreadsNeeded);
                for (int i = 0; i < numThreadsNeeded; i++) {
                    workPackage.add(new ArrayList<>());
                }
                for (int i = 0; i < rules.size(); i++) {
                    final int allocateTo = i % numThreadsNeeded;
                    workPackage.get(allocateTo).add(rules.get(i));
                }

                // Allocate work and start threads:
                for (int i = 0; i < numThreadsNeeded; i++) {
                    final ThreadedFind tf = new ThreadedFind(rg, barrier, this, type, workPackage.get(i), isAnd, i);
                    final Thread t = new Thread(tf);
                    t.start();
                }

            } finally {
                // Await the conclusion of all threads:
                try {
                    barrier.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, THREAD_INTERRUPTED, ex);
                    Thread.currentThread().interrupt();
                } catch (final BrokenBarrierException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }

            // Determine the actual results from the bitset:
            for (int i = 0; i < results.length; i++) {
                if (results[i]) {
                    // Construct a new FindResult and store the id and type of the found element.
                    final int id = type.getElement(rg, i);
                    final long uid = type.getUID(rg, id);
                    final FindResult fr = new FindResult(id, uid, type);
                    findResults.add(fr);
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
        if (!clearSelection) {
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
        private final List<FindRule> rules;
        private final int threadID;
        private final boolean simpleMode;
        private boolean isAnd = false;
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

            // Disable advancedMode:
            simpleMode = true;
            this.rules = null;
        }

        /**
         * Constructs a new <code>ThreadedFind</code>, and prepares it for an
         * 'advanced query'.
         * <p>
         * This constructor should only be used for advanced queries.
         * <p>
         * This mode works by assigning an individual thread to each rule, and
         * updates a central result set with results. The benefit of this mode
         * is that shorter search durations will progress more quickly, and
         * complex operations being performed in parallel may not need to check
         * as many graph items.
         * <p>
         * The efficiency of this algorithm should be self balanced.
         *
         * @param barrier <code>CyclicBarrier</code> used to handle
         * multi-threadedness.
         * @param parent The <code>QueryServices</code> that instantiated this
         * <code>ThreadedFind</code>.
         * @param type The <code>GraphElementType</code> to perform the quick
         * search operation on.
         * @param rules List of <code>FindRule</code>s that represent the
         * criteria that should be matched for a positive query result.
         * @param isAnd <code>true</code> if all <code>FindRule</code> criterion
         * should be checked as 'AND', <code>false</code> if should be checked
         * as 'OR'.
         * @param threadID The id of this thread.
         *
         * @see CyclicBarrier
         * @see ArrayList
         * @see FindRule
         * @see QueryServices
         * @see GraphElementType
         */
        public ThreadedFind(final GraphReadMethods rg, final CyclicBarrier barrier, final QueryServices parent,
                final GraphElementType type, final List<FindRule> rules,
                final boolean isAnd, final int threadID) {
            this.rg = rg;

            this.barrier = barrier;

            this.parent = parent;
            this.rules = rules;
            this.threadID = threadID;

            // Disable quickSearch mode:
            simpleMode = false;
            this.type = type;
            this.content = null;

            this.isAnd = isAnd;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().setName("Find.FindServices.Thread." + threadID);

                if (simpleMode) {
                    quickFind();
                } else {
                    advancedFind();
                }

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
                    if (retrieved != null && FindComparisons.StringComparisons.evaluateContains(retrieved, searchText, false)) {
                        FindResult fr = new FindResult(elementId, elementUid, type, new GraphAttribute(rg, attrID).getName(), retrieved);
                        parent.findResults.add(fr);
                    }
                }
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Advanced Find Logic">
        /**
         * Searches the graph for matches to the given <code>FindRule</code>.
         *
         * @see FindRule
         */
        private void advancedFind() {
            for (FindRule rule : rules) {
                for (int i = 0; i < parent.results.length; i++) {
                    boolean queryResult = false;

                    final int item = type.getElement(rg, i);

                    if ((isAnd && parent.results[i]) || (!isAnd && !parent.results[i])) {
                        switch (rule.getType()) {
                            case BOOLEAN:
                                queryResult = advancedFindBoolean(rule, item);
                                break;
                            case COLOR:
                                queryResult = advancedFindColor(rule, item);
                                break;
                            case DATE:
                                queryResult = advancedFindDate(rule, item);
                                break;
                            case DATETIME:
                                queryResult = advancedFindDateTime(rule, item);
                                break;
                            case FLOAT:
                                queryResult = advancedFindFloat(rule, item);
                                break;
                            case INTEGER:
                                queryResult = advancedFindInt(rule, item);
                                break;
                            case ICON:
                                queryResult = advancedFindIcon(rule, item);
                                break;
                            case STRING:
                                queryResult = advancedFindString(rule, item);
                                break;
                            case TIME:
                                queryResult = advancedFindTime(rule, item);
                                break;
                            default:
                                // Ignore
                                break;
                        }

                        // Update the bitset:
                        updateResultSet(i, queryResult);
                    }
                }
            }
        }

        /**
         * Helper function that updates the central result set at the given
         * index.
         * <p>
         * This method, and the result set are atomic, and as such do not need
         * synchronisation for thread-safety. It is using this technique that we
         * are able to achieve efficiencies in searching the graph without
         * needing to synchronise all joins.
         *
         * @param index The index of the GraphElement that has been evaluated.
         * @param queryResult The result of the evaluation of the query.
         */
        private void updateResultSet(final int index, final boolean queryResult) {
            if (isAnd) {
                if (!queryResult) {
                    parent.results[index] = false;
                }
            } else {
                if (queryResult) {
                    parent.results[index] = true;
                }
            }
        }

        /**
         * Perform a Boolean based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindBoolean(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final boolean item = rg.getBooleanValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            // Query the graph:
            if (rule.getOperator().equals(FindTypeOperators.Operator.IS)) {
                queryResult = FindComparisons.BooleanComparisons.evaluateIs(item, rule.getBooleanContent());
            }

            return queryResult;
        }

        /**
         * Perform a Color based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindColor(final FindRule rule, final int index) {
            boolean queryResult = false;

            // Retrieve content from the graph:
            final ConstellationColor item = (ConstellationColor) rg.getObjectValue(rule.getAttribute().getId(), index);

            // Query the graph:
            if (rule.getOperator().equals(FindTypeOperators.Operator.IS)) {
                queryResult = FindComparisons.ColorComparisons.evaluateIs(item.getJavaColor(), rule.getColorContent());
            } else if (rule.getOperator().equals(FindTypeOperators.Operator.IS_NOT)) {
                queryResult = FindComparisons.ColorComparisons.evaluateIsNot(item.getJavaColor(), rule.getColorContent());
            } else {
                // Do nothing
            }

            return queryResult;
        }

        /**
         * Perform a Date based query for the given <code>FindRule</code> on the
         * graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindDate(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            Date item = (Date) rg.getObjectValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            Calendar cleanser = Calendar.getInstance();
            cleanser.setTime(item);
            cleanser.set(Calendar.HOUR_OF_DAY, 0);
            cleanser.set(Calendar.MINUTE, 0);
            cleanser.set(Calendar.SECOND, 0);
            cleanser.set(Calendar.MILLISECOND, 0);

            item = cleanser.getTime();

            switch (rule.getOperator()) {
                case OCCURRED_ON:
                    queryResult = FindComparisons.DateComparisons.evaluateOccurredOn(item, rule.getDateFirstArg());
                    break;
                case NOT_OCCURRED_ON:
                    queryResult = FindComparisons.DateComparisons.evaluateNotOccurredOn(item, rule.getDateFirstArg());
                    break;
                case OCCURRED_BEFORE:
                    queryResult = FindComparisons.DateComparisons.evaluateBefore(item, rule.getDateFirstArg());
                    break;
                case OCCURRED_AFTER:
                    queryResult = FindComparisons.DateComparisons.evaluateAfter(item, rule.getDateFirstArg());
                    break;
                case OCCURRED_BETWEEN:
                    queryResult = FindComparisons.DateComparisons.evaluateBetween(item,
                            rule.getDateFirstArg(), rule.getDateSecondArg());
                    break;
                default:
                    // Unable to pass any form of test:
                    queryResult = false;
                    break;
            }

            return queryResult;
        }

        /**
         * Perform a DateTime based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindDateTime(final FindRule rule, final int index) {
            boolean queryResult = false;

            // Retrieve content from the graph:
            final Date retrieved = new Date(rg.getLongValue(rule.getAttribute().getId(), index));

            Calendar item = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            item.setTimeInMillis(retrieved.getTime());
            item.set(Calendar.MILLISECOND, 0);

            switch (rule.getOperator()) {
                case OCCURRED_ON:
                    queryResult = FindComparisons.DateTimeComparisons.evaluateOccurredOn(item, rule.getDateTimeFirstArg());
                    break;
                case NOT_OCCURRED_ON:
                    queryResult = FindComparisons.DateTimeComparisons.evaluateNotOccurredOn(item, rule.getDateTimeFirstArg());
                    break;
                case OCCURRED_BEFORE:
                    queryResult = FindComparisons.DateTimeComparisons.evaluateBefore(item, rule.getDateTimeFirstArg());
                    break;
                case OCCURRED_AFTER:
                    queryResult = FindComparisons.DateTimeComparisons.evaluateAfter(item, rule.getDateTimeFirstArg());
                    break;
                case OCCURRED_BETWEEN:
                    queryResult = FindComparisons.DateTimeComparisons.evaluateBetween(item,
                            rule.getDateTimeFirstArg(), rule.getDateTimeSecondArg());
                    break;
                default:
                    // Unable to pass any form of test:
                    queryResult = false;
                    break;
            }

            return queryResult;
        }

        /**
         * Perform a Float based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindFloat(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final float item = rg.getFloatValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            // Query the graph:
            switch (rule.getOperator()) {
                case IS:
                    queryResult = FindComparisons.FloatComparisons.evaluateIs(item, rule.getFloatFirstArg());
                    break;
                case IS_NOT:
                    queryResult = FindComparisons.FloatComparisons.evaluateIsNot(item, rule.getFloatFirstArg());
                    break;
                case LESS_THAN:
                    queryResult = FindComparisons.FloatComparisons.evaluateLessThan(item, rule.getFloatFirstArg());
                    break;
                case GREATER_THAN:
                    queryResult = FindComparisons.FloatComparisons.evaluateGreaterThan(item, rule.getFloatFirstArg());
                    break;
                case BETWEEN:
                    queryResult = FindComparisons.FloatComparisons.evaluateBetween(item,
                            rule.getFloatFirstArg(), rule.getFloatSecondArg());
                    break;
                default:
                    // Unable to pass any form of test:
                    queryResult = false;
                    break;
            }

            return queryResult;
        }

        /**
         * Perform a Int based query for the given <code>FindRule</code> on the
         * graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindInt(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final int item = rg.getIntValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            // Query the graph:
            switch (rule.getOperator()) {
                case IS:
                    queryResult = FindComparisons.IntComparisons.evaluateIs(item, rule.getIntFirstArg());
                    break;
                case IS_NOT:
                    queryResult = FindComparisons.IntComparisons.evaluateIsNot(item, rule.getIntFirstArg());
                    break;
                case LESS_THAN:
                    queryResult = FindComparisons.IntComparisons.evaluateLessThan(item, rule.getIntFirstArg());
                    break;
                case GREATER_THAN:
                    queryResult = FindComparisons.IntComparisons.evaluateGreaterThan(item, rule.getIntFirstArg());
                    break;
                case BETWEEN:
                    queryResult = FindComparisons.IntComparisons.evaluateBetween(item,
                            rule.getIntFirstArg(), rule.getIntSecondArg());
                    break;
                default:
                    // Unable to pass any form of test:
                    queryResult = false;
                    break;
            }

            return queryResult;
        }

        /**
         * Perform an Icon based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindIcon(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final String item = rg.getStringValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            // Query the graph:
            if (rule.getOperator().equals(FindTypeOperators.Operator.IS)) {
                queryResult = FindComparisons.IconComparisons.evaluateIs(item, rule.getIconContent());
            } else if (rule.getOperator().equals(FindTypeOperators.Operator.IS_NOT)) {
                queryResult = FindComparisons.IconComparisons.evaluateIsNot(item, rule.getIconContent());
            } else {
                // Do nothing
            }

            return queryResult;
        }

        /**
         * Perform a String based query for the given <code>FindRule</code> on
         * the graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindString(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final String item = rg.getStringValue(rule.getAttribute().getId(), index);
            boolean queryResult = false;

            String[] terms;
            if (rule.isStringUsingList()
                    && !rule.getOperator().equals(FindTypeOperators.Operator.REGEX)) {
                terms = rule.getStringContent().split(",");
            } else {
                String[] content = new String[1];
                content[0] = rule.getStringContent();
                terms = content;
            }
            int i = 0;

            // Query the graph:
            while (i < terms.length && !queryResult) {
                switch (rule.getOperator()) {
                    case IS:
                        queryResult = FindComparisons.StringComparisons.evaluateIs(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case IS_NOT:
                        queryResult = FindComparisons.StringComparisons.evaluateIsNot(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case CONTAINS:
                        queryResult = FindComparisons.StringComparisons.evaluateContains(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case NOT_CONTAINS:
                        queryResult = FindComparisons.StringComparisons.evaluateNotContains(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case BEGINS_WITH:
                        queryResult = FindComparisons.StringComparisons.evaluateBeginsWith(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case ENDS_WITH:
                        queryResult = FindComparisons.StringComparisons.evaluateEndsWith(item,
                                terms[i], rule.isStringCaseSensitivity());
                        break;
                    case REGEX:
                        queryResult = FindComparisons.StringComparisons.evaluateRegex(item,
                                terms[i]);
                        break;
                    default:
                        // Unable to pass any form of test:
                        queryResult = false;
                        break;
                }
                i++;
            }

            return queryResult;
        }

        /**
         * Perform a Time based query for the given <code>FindRule</code> on the
         * graph at the given index.
         *
         * @param rule The rule to query the given GraphElement for.
         * @param index The index of the GraphElement to be queried.
         * @return <code>true</code> if search operation returned a positive
         * result, <code>false</code> if it returned a negative result.
         *
         * @see FindRule
         */
        private boolean advancedFindTime(final FindRule rule, final int index) {
            // Retrieve content from the graph:
            final int item = (Integer) rg.getObjectValue(rule.getAttribute().getId(), index);

            final int comparison1 = 0 /*TimeAttributeDescription.getAsInteger(rule.getTimeFirstArg())*/;
            final int comparison2 = 0 /*TimeAttributeDescription.getAsInteger(rule.getTimeSecondArg())*/;

            boolean queryResult = false;

            // Determine whether content matches our rule:
            switch (rule.getOperator()) {
                case OCCURRED_ON:
                    queryResult = FindComparisons.TimeComparisons.evaluateOccurredOn(item, comparison1);
                    break;
                case NOT_OCCURRED_ON:
                    queryResult = FindComparisons.TimeComparisons.evaluateNotOccurredOn(item, comparison1);
                    break;
                case OCCURRED_BEFORE:
                    queryResult = FindComparisons.TimeComparisons.evaluateBefore(item, comparison1);
                    break;
                case OCCURRED_AFTER:
                    queryResult = FindComparisons.TimeComparisons.evaluateAfter(item, comparison1);
                    break;
                case OCCURRED_BETWEEN:
                    queryResult = FindComparisons.TimeComparisons.evaluateBetween(item,
                            comparison1, comparison2);
                    break;
                default:
                    // Unable to pass any form of test:
                    queryResult = false;
                    break;
            }

            return queryResult;
        }
        // </editor-fold>
    }
}

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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import au.gov.asd.tac.constellation.views.find.advanced.GraphAttributePlugin;
import au.gov.asd.tac.constellation.views.find2.gui.FindViewPane;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewController {

    // Layers view controller instance
    private static FindViewController instance = null;
    private FindViewTopComponent parentComponent;
    private static final Logger LOGGER = Logger.getLogger(FindViewController.class.getName());

    private BasicFindReplaceParameters currentBasicParameters;
    private BasicFindReplaceParameters previousBasicParameters;
    private FindResultsList resultsList = new FindResultsList();
    private boolean addToCurrentSelection = false;
    private boolean removeFromCurrentSelection = false;
    private int currentIndex = 0;

    /**
     * Private constructor for singleton
     */
    private FindViewController() {
    }

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized FindViewController getDefault() {
        if (instance == null) {
            instance = new FindViewController();
        }
        return instance;
    }

    public FindViewController init(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        return instance;
    }

    public void updateUI() {
        if (currentBasicParameters != null) {
            populateAttributes(currentBasicParameters.getGraphElement(), currentBasicParameters.getAttributeList(), Long.MIN_VALUE);
            updateBasicParameters(currentBasicParameters);
        }
    }

//    public void resetIndex() {
//        WritableGraph wg = GraphManager.getDefault().getActiveGraph().getWritableGraph("reseting the index", true);
//        int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);
//    }

    /**
     * This loops through all the attribute lists in each open graph adding all
     * unique attributes to the UI attribute list if they are of type string
     *
     * @param type
     * @param attributes
     * @param attributeModificationCounter
     * @param inAttributesMenu
     */
    public ArrayList<String> populateAttributes(GraphElementType type, ArrayList<Attribute> attributes, long attributeModificationCounter) {
        attributes.clear();

        ArrayList<String> attributeList = new ArrayList<>();

        for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
            final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
            final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

            // Wait for the search to find its results:
            try {
                future.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (attrPlugin.getAttributeModificationCounter() != attributeModificationCounter) {
                attributes = attrPlugin.getAttributes();
                for (Attribute a : attributes) {
                    if (a.getAttributeType().equals("string")) {
                        attributeList.add(a.getName());
                    }
                }
            }
        }
        Set<String> set = new LinkedHashSet<>();
        set.addAll(attributeList);
        attributeList.clear();
        attributeList.addAll(set);
        return attributeList;
    }

    public void disableFindView(FindViewPane pane, boolean disable) {
        pane.setDisable(disable);
    }

    public void updateBasicParameters(BasicFindReplaceParameters parameters) {
        currentBasicParameters = parameters;
    }

    /**
     * updates the add to current and remove from current variables based on the
     * UI selection
     *
     * @param addToCurrent
     * @param removeFromCurrent
     */
    public void updateSelectionFactors(final boolean addToCurrent, final boolean removeFromCurrent) {
        this.addToCurrentSelection = addToCurrent;
        this.removeFromCurrentSelection = removeFromCurrent;
    }

    public void retriveMatchingElements(boolean selectAll, boolean getNext) {
        BasicFindPlugin basicfindPlugin = new BasicFindPlugin(currentBasicParameters, addToCurrentSelection, selectAll, getNext);
        PluginExecution.withPlugin(basicfindPlugin).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    public void selectNextPrev(GraphElementType type, boolean incrament) {

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Thread t = new Thread("Selecting Found Graph Elements") {
                @Override
                public void run() {
                    try {
                        WritableGraph wg = GraphManager.getDefault().getActiveGraph().getWritableGraph("Select the next graph element", true);
                        int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(wg);
                        if (stateId != Graph.NOT_FOUND) {
                            resultsList = wg.getObjectValue(stateId, 0);
                            if (wg.getId().equals(resultsList.getGraphId())) {
                                LOGGER.log(Level.SEVERE, String.valueOf(resultsList.size()));
                                if (resultsList.size() != 0) {
                                    final int element = resultsList.get(currentIndex).getID();
                                    int selectedAttr = 0;
                                    if (type == GraphElementType.VERTEX) {
                                        selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
                                    }
                                    if (type == GraphElementType.TRANSACTION) {
                                        selectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(wg);
                                    }
                                    wg.setBooleanValue(selectedAttr, element, true);

                                    if (incrament == true) {
                                        changeIndex(incrament);
                                    } else {
                                        changeIndex(!incrament);
                                    }
                                }
                            }
                        }
                        wg.commit();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Exceptions.printStackTrace(ex);
                    }
                    latch.countDown();
                }
            };
            t.start();
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void changeIndex(boolean incrament) {
        ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph();
        int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.get(rg);
        if (stateId != Graph.NOT_FOUND) {
            resultsList = rg.getObjectValue(stateId, 0);

            for (FindResult res : resultsList) {
                LOGGER.log(Level.SEVERE, res.getType().getShortLabel());
//                LOGGER.log(Level.SEVERE, );

            }

            if (incrament == true) {
                if (currentIndex == resultsList.size() - 1) {
                    currentIndex = 0;
                } else {
                    currentIndex++;
                }
            } else {
                if (currentIndex == 0) {
                    currentIndex = resultsList.size() - 1;
                } else {
                    currentIndex--;
                }
            }
        }
        rg.close();
    }
    //    public void findAll() {
    //        selectAllListElements(GraphManager.getDefault().getAllGraphs().values(), currentBasicParameters.getGraphElement());
    //    }
    //
    //    public void selectAllListElements(Collection<Graph> openGraphs, GraphElementType type) {
    //        try {
    //            final CountDownLatch latch = new CountDownLatch(1);
    //            final Thread t = new Thread("Selecting Found Graph Elements") {
    //                @Override
    //                public void run() {
    //                    for (Graph graph : openGraphs) {
    //                        try {
    //                            final WritableGraph wg = graph.getWritableGraph("Select All Results", true);
    //                            int selectedAttr = 0;
    //                            if (type == GraphElementType.VERTEX) {
    //                                selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
    //                            }
    //                            if (type == GraphElementType.TRANSACTION) {
    //                                selectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(wg);
    //                            }
    //                            for (FindResultsList resList : resultsList) {
    //                                if (!resList.isEmpty()) {
    //                                    if (resList.getGraphId().equals(graph.getId())) {
    //                                        for (FindResult res : resList) {
    //
    //                                            wg.setBooleanValue(selectedAttr, res.getID(), true);
    //                                        }
    //                                    }
    //                                }
    //                            }
    //                            wg.commit();
    //                        } catch (InterruptedException ex) {
    //                            Thread.currentThread().interrupt();
    //                            Exceptions.printStackTrace(ex);
    //                        }
    //                    }
    //                    latch.countDown();
    //                }
    //            };
    //            t.start();
    //            latch.await();
    //        } catch (InterruptedException ex) {
    //            Thread.currentThread().interrupt();
    //        }
    //    }

    /**
     * Depending on the value of isSearchAllGraphs in the
     * currentBasicParameters, this function looks at all open graphs or just
     * the single active graph and will populate the results lists with all the
     * matching elements to the currentBasicParameters
     */
    //    public void populateFindResultsLists() {
    //
    //        // dont need to re run the search as its the same
    //        // re select all elements on the graph
    //        //A new find function is running, so clear the existing search results list
    //        clearSavedValues();
    //        if (currentBasicParameters.isSearchAllGraphs()) {
    //            for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
    //                gatherResultsList(graph);
    //            }
    //        } else {
    //            gatherResultsList(GraphManager.getDefault().getActiveGraph());
    //        }
    //        // The search is different, we need to re run the search
    //
    //    }
    /**
     * Populates a list of FindResults for the current graph and adds it to the
     * FindResultsList
     *
     * @param graph
     * @return the ArrayList Containing the lists of results
     */
    //    public ArrayList<FindResultsList> gatherResultsList(Graph graph) {
    //        FindResultsList results = new FindResultsList();
    //
    //        try (final ReadableGraph rg = graph.getReadableGraph()) {
    //            final int elementCount = currentBasicParameters.getGraphElement().getElementCount(rg);
    //            LOGGER.log(Level.SEVERE, "Total Element count :" + String.valueOf(elementCount));
    //
    //            for (int i = 0; i < elementCount; i++) {
    //                //LOGGER.log(Level.SEVERE, "looking at element :" + String.valueOf(rg.getVertex(i)));
    //
    //                // Run the search for vertex elements
    //                if (currentBasicParameters.getGraphElement() == GraphElementType.VERTEX) {
    //                    if (matchFindParameters(rg.getVertex(i), rg) != -1) {
    //                        results.add(createFindResult(currentBasicParameters.getGraphElement(), rg.getVertex(i), rg));
    //                    }
    //                }
    //                // Run the search for transaction elements
    //                if (currentBasicParameters.getGraphElement() == GraphElementType.TRANSACTION) {
    //                    if (matchFindParameters(rg.getTransaction(i), rg) != -1) {
    //                        results.add(createFindResult(currentBasicParameters.getGraphElement(), rg.getTransaction(i), rg));
    //                    }
    //                }
    //                // Run the search for edge elements
    //                if (currentBasicParameters.getGraphElement() == GraphElementType.EDGE) {
    //                    if (matchFindParameters(rg.getEdge(i), rg) != -1) {
    //                        results.add(createFindResult(currentBasicParameters.getGraphElement(), rg.getEdge(i), rg));
    //                    }
    //                }
    //                // Run the search for link elements
    //                if (currentBasicParameters.getGraphElement() == GraphElementType.LINK) {
    //                    if (matchFindParameters(rg.getLink(i), rg) != -1) {
    //                        results.add(createFindResult(currentBasicParameters.getGraphElement(), rg.getLink(i), rg));
    //                    }
    //                }
    //            }
    //        }
    //        results.setGraphId(graph.getId());
    //        resultsList.add(results);
    //        return resultsList;
    //    }
    /**
     * Loops through all the attributes in the currentBasicParameters and checks
     * their respective content. If the content matches the find string, it will
     * return the elements id, if not it will return -1
     *
     * @param graphElement the id of the graph element
     * @param rg the readable graph
     * @return the id of the graph element
     */
    public int matchFindParameters(int graphElement, ReadableGraph rg) {
        for (Attribute a : currentBasicParameters.getAttributeList()) {
            LOGGER.log(Level.SEVERE, "lookin at attribute :" + a.getName());

            String findValue = currentBasicParameters.getFindString();
            String value = rg.getStringValue(rg.getAttribute(currentBasicParameters.getGraphElement(), a.getName()), graphElement);
            if (value != null) {
                LOGGER.log(Level.SEVERE, "elements " + a.getName() + "value = " + value);

                //If ignore case is true, compare the values in lowercase
                if (currentBasicParameters.isIgnoreCase()) {
                    findValue.toLowerCase();
                    value.toLowerCase();
                }
                //If exact match is true, compare the two valus are exactly equal
                if (currentBasicParameters.isExactMatch()) {
                    if (value.equals(currentBasicParameters.getFindString())) {
                        return a.getId();
                    }
                    //If exact match is false, check if the value contains the findvalue
                } else if (!currentBasicParameters.isExactMatch()) {
                    if (value.contains(findValue)) {
                        return a.getId();
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Creates a findResult based on the parameters given
     *
     * @param type the graph element type (Vertex, Transaction, Link, Edge)
     * @param graphElement the id of the graph element
     * @param rg the readable graph
     * @return the newly created FindResult;
     */
    public FindResult createFindResult(GraphElementType type, int graphElement, ReadableGraph rg) {
        final int id = type.getElement(rg, graphElement);
        final long uid = type.getUID(rg, id);
        final FindResult fr = new FindResult(id, uid, type);

        LOGGER.log(Level.SEVERE, "made a result");
        return fr;
    }

    public void clearSavedValues() {
        if (!resultsList.isEmpty()) {
            resultsList.clear();
        }
    }

}

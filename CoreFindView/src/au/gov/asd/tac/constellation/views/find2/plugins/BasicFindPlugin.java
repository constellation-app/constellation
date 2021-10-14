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
package au.gov.asd.tac.constellation.views.find2.plugins;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResultsList;
import au.gov.asd.tac.constellation.views.find2.state.FindViewConcept;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class does the actual action of finding.
 *
 * @author twinkle2_little, Atlas139mkm
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {"SEARCH"})
public class BasicFindPlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final ArrayList<Attribute> selectedAttributes;
    private String findString;
    private boolean regex;
    private final boolean ignorecase;
    private final boolean matchWholeWord;
    private final boolean addToSelection;
    private final boolean removeFromCurrentSelection;
    private final boolean findInCurrentSelection;
    private final boolean selectAll;
    private final boolean getNext;
    private final BasicFindReplaceParameters parameters;
    private long modificationCounter = 0;
    private final boolean modified;

    private final static int STARTING_INDEX = -1;
    private static final Logger LOGGER = Logger.getLogger(BasicFindPlugin.class.getName());

    public BasicFindPlugin(BasicFindReplaceParameters parameters, boolean addToSelection, boolean removeFromCurrentSelection, boolean findInCurrentSelection, boolean selectAll, boolean getNext, long elementModificationCounter) {
        this.elementType = parameters.getGraphElement();
        this.selectedAttributes = parameters.getAttributeList();
        this.findString = parameters.getFindString();
        this.regex = parameters.isRegEx();
        this.ignorecase = parameters.isIgnoreCase();
        this.matchWholeWord = parameters.isExactMatch();
        this.addToSelection = addToSelection;
        this.selectAll = selectAll;
        this.getNext = getNext;
        this.parameters = parameters;
        this.removeFromCurrentSelection = removeFromCurrentSelection;
        this.findInCurrentSelection = findInCurrentSelection;
        if (this.modificationCounter != elementModificationCounter) {
            this.modified = true;
        } else {
            this.modified = false;
        }
    }

    private void clearSelection(GraphWriteMethods graph) {
        final int nodesCount = GraphElementType.VERTEX.getElementCount(graph);
        final int nodeSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionsCount = GraphElementType.TRANSACTION.getElementCount(graph);
        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        if (nodeSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < nodesCount; i++) {
                int currElement = GraphElementType.VERTEX.getElement(graph, i);
                graph.setBooleanValue(nodeSelectedAttribute, currElement, false);
            }
        }
        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < transactionsCount; i++) {
                int currElement = GraphElementType.TRANSACTION.getElement(graph, i);
                graph.setBooleanValue(transactionSelectedAttribute, currElement, false);
            }
        }
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        //Retrieve the existing FindResultList Meta attribute
        int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        FindResultsList foundResult = null;
        foundResult = graph.getObjectValue(stateId, 0);

        /**
         * If it doesn't exist or is null, create a new list with the starting
         * index and the current find parameters. If it does exist, create a
         * list with the correct index and the current find parameters
         */
        if (foundResult == null) {
            foundResult = new FindResultsList(STARTING_INDEX, this.parameters);
        } else {
            final FindResultsList oldList = new FindResultsList(STARTING_INDEX, foundResult.getSearchParameters());
            foundResult = new FindResultsList(getIndex(foundResult, oldList), this.parameters);
        }

        //Set the found result list to the current graph
        foundResult.setGraphId(graph.getId());
        foundResult.clear();
        graph.setObjectValue(stateId, 0, foundResult);

        if (findString.isEmpty()) {
            findString = "^$";
            regex = true;
        }

        boolean found;
        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
        final int elementCount = elementType.getElementCount(graph);

        // do this if add to selection
        if (!addToSelection && !removeFromCurrentSelection && !findInCurrentSelection) {
            clearSelection(graph);
        }

        FindResultsList findInCurrentSelectionList = new FindResultsList();
        FindResultsList removeFromCurrentSelectionList = new FindResultsList();

        String searchString = regex ? findString : Pattern.quote(findString);
        int caseSensitivity = ignorecase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        Pattern searchPattern = Pattern.compile(searchString, caseSensitivity);

        /**
         * Loop through all selected attributes, get the current element of the
         * selected type and its value, check the value isn't null, then compare
         * the value with the find string based on the search preferences. If
         * that element matches the search criteria, change its selected value
         * to true if selecting all. Otherwise create a FindResult and add that
         * find result to the foundResults list
         */
        for (Attribute a : selectedAttributes) {
            for (int i = 0; i < elementCount; i++) {
                int currElement = elementType.getElement(graph, i);
                String value = graph.getStringValue(graph.getAttribute(elementType, a.getName()), currElement);
                if (value != null) {
                    Matcher match = searchPattern.matcher(value);
                    if (matchWholeWord) {
                        found = match.matches();
                    } else {
                        found = match.find();
                    }
                    if (found) {
                        if (findInCurrentSelection || removeFromCurrentSelection) {
                            final long uid = elementType.getUID(graph, currElement);
                            if (graph.getBooleanValue(selectedAttribute, currElement)) {
                                findInCurrentSelectionList.add(new FindResult(currElement, uid, elementType));
                                removeFromCurrentSelectionList.add(new FindResult(currElement, uid, elementType));
                            }
                        }
                        if (selectAll && !findInCurrentSelection && !removeFromCurrentSelection) {
                            graph.setBooleanValue(selectedAttribute, currElement, true);
                        }
                        final long uid = elementType.getUID(graph, currElement);
                        FindResult fr = new FindResult(currElement, uid, elementType);
                        foundResult.add(fr);
                    }
                }
            }
        }
        if (findInCurrentSelection) {
            if (!findInCurrentSelectionList.isEmpty()) {
                clearSelection(graph);
                for (FindResult fr : findInCurrentSelectionList) {
                    graph.setBooleanValue(selectedAttribute, fr.getID(), true);
                }
            }
        }
        if (removeFromCurrentSelection) {
            if (!removeFromCurrentSelectionList.isEmpty()) {
                for (FindResult fr : removeFromCurrentSelectionList) {
                    graph.setBooleanValue(selectedAttribute, fr.getID(), false);
                    if (getNext) {
                        break;
                    }
                }
            }
        }

        /**
         * If the user clicked find next or find previous
         */
        if (!selectAll) {

            // Clean the find results list to only contain unique graph elements
            final List<FindResult> distinctValues = foundResult.stream().distinct().collect(Collectors.toList());
            foundResult.clear();
            foundResult.addAll(distinctValues);

            /**
             * If the list isn't empty, and the user clicked find next,
             * increment the found lists index by 1, otherwise decrement it by
             * 1. Set the element at the specified index to selected.
             */
            if (!foundResult.isEmpty()) {
                if (getNext == true) {
                    foundResult.incrementCurrentIndex();
                } else {
                    foundResult.decrementCurrentIndex();
                }
                int elementId = foundResult.get(foundResult.getCurrentIndex()).getID();
                graph.setBooleanValue(selectedAttribute, elementId, true);
                if (removeFromCurrentSelection) {
                    graph.setBooleanValue(selectedAttribute, elementId, false);
                }
            }
            graph.setObjectValue(stateId, 0, foundResult);

        }
        //If no results are found, set the meta attribute to null
        if (foundResult.isEmpty()) {
            graph.setObjectValue(stateId, 0, null);
        }
    }

    /**
     * Determines what index is correct for the found results list based on if
     * the user is finding all, doing their first find, doing a different find
     * to their previous
     *
     * @param foundResult the list of foundResults
     * @return the correct current index
     */
    private int getIndex(FindResultsList foundResult, FindResultsList lastFoundResult) {
        // If selecting all elements, reset the index
        if (selectAll) {
            return STARTING_INDEX;
        }
        if (modified) {
            return STARTING_INDEX;
        }
        // If the foundresult has been created
        if (foundResult != null) {
            // If the query hasnt changed and there must be elements in the list
            // get the current index
            if (this.parameters.equals(lastFoundResult.getSearchParameters())) {
                return foundResult.getCurrentIndex();
            } else {
                return STARTING_INDEX;
            }
        }
        // If all else fails reset the index
        return STARTING_INDEX;
    }

    @Override
    public String getName() {
        return "Find: Find and Replace";
    }
}

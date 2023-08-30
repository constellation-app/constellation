/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.plugins;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindResult;
import au.gov.asd.tac.constellation.views.find.utilities.FindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.FindViewUtilities;
import java.util.List;
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
    private final List<Attribute> selectedAttributes;
    private String findString;
    private boolean regex;
    private final boolean ignorecase;
    private final boolean matchWholeWord;
    private final boolean replaceCurrentSelection;
    private final boolean addToSelection;
    private final boolean removeFromCurrentSelection;
    private final boolean selectAll;
    private final boolean getNext;
    private final boolean currentSelection;
    private final BasicFindReplaceParameters parameters;
    private FindResultsList foundResult;

    private static final int STARTING_INDEX = -1;

    public BasicFindPlugin(final BasicFindReplaceParameters parameters, final boolean selectAll, final boolean getNext) {
        this.elementType = parameters.getGraphElement();
        this.selectedAttributes = parameters.getAttributeList();
        this.findString = parameters.getFindString();
        this.regex = parameters.isRegEx();
        this.ignorecase = parameters.isIgnoreCase();
        this.matchWholeWord = parameters.isExactMatch();
        this.selectAll = selectAll;
        this.getNext = getNext;
        this.parameters = parameters;
        this.addToSelection = parameters.isAddTo();
        this.removeFromCurrentSelection = parameters.isRemoveFrom();
        this.currentSelection = parameters.isCurrentSelection();
        this.replaceCurrentSelection = parameters.isReplaceSelection();
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        //Retrieve the existing FindResultList Meta attribute
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        foundResult = graph.getObjectValue(stateId, 0);

        /**
         * If it doesn't exist or is null, create a new list with the starting
         * index and the current find parameters. If it does exist, create a
         * list with the correct index and the current find parameters
         */
        if (foundResult == null) {
            foundResult = new FindResultsList(STARTING_INDEX, this.parameters);
        } else {
            /**
             * This is delicate, so don't change. This process, captures the
             * users previous search and their current search, compares the 2 to
             * see if they are the same. If yes then get the previous index of
             * the last foundResult. If its different reset the index. The
             * parameters are instantiated as new variables as they were
             * manipulation issues elsewhere causing this process to fail.
             */
            final FindResultsList oldList = new FindResultsList(STARTING_INDEX, foundResult.getSearchParameters());
            final BasicFindReplaceParameters oldparameters = oldList.getSearchParameters();
            final BasicFindReplaceParameters newParamters = new BasicFindReplaceParameters(this.parameters);
            int newIndex = getIndex(newParamters, oldparameters, foundResult.getCurrentIndex());
            foundResult = new FindResultsList(newIndex, newParamters);
        }

        foundResult.clear();
        graph.setObjectValue(stateId, 0, foundResult);

        if (findString.isEmpty()) {
            findString = "^$";
            regex = true;
        }

        boolean found;
        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
        final int elementCount = elementType.getElementCount(graph);

        // do this if replace selection or delete from graph
        if (!addToSelection && !removeFromCurrentSelection && !currentSelection) {
            FindViewUtilities.clearSelection(graph);
        }

        final FindResultsList findInCurrentSelectionList = new FindResultsList();
        final FindResultsList removeFromCurrentSelectionList = new FindResultsList();

        final String searchString = regex ? findString : Pattern.quote(findString);
        final int caseSensitivity = ignorecase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        final Pattern searchPattern = Pattern.compile(searchString, caseSensitivity);

        /**
         * Loop through all selected attributes, get the current element of the
         * selected type and its value, check the value isn't null, then compare
         * the value with the find string based on the search preferences. If
         * that element matches the search criteria, change its selected value
         * to true if selecting all. Otherwise create a FindResult and add that
         * find result to the foundResults list
         */
        for (final Attribute a : selectedAttributes) {
            // if the attribute exists on the current graph
            if (graph.getAttribute(elementType, a.getName()) >= 0) {
                // for all elements on the graph of the given type
                for (int i = 0; i < elementCount; i++) {

                    // get the current element
                    final int currElement = elementType.getElement(graph, i);
                    // get string value of it graph elements attribute
                    final String value = graph.getStringValue(graph.getAttribute(elementType, a.getName()), currElement);
                    // if the value isnt null
                    if (value != null) {

                        // Determine if the find string matches the attribute string
                        Matcher match = searchPattern.matcher(value);
                        found = matchWholeWord ? match.matches() : match.find();
                        if (found) {
                            // get the UID of the element and the graph
                            final long uid = elementType.getUID(graph, currElement);
                            // if the user wants to find it in, or remove it from their current selection
                            if (removeFromCurrentSelection || (currentSelection && replaceCurrentSelection)) {
                                // if the element is selected
                                if (graph.getBooleanValue(selectedAttribute, currElement)) {
                                    // Add the element to the find in and remove from list
                                    findInCurrentSelectionList.add(new FindResult(currElement, uid, elementType, graph.getId()));
                                    removeFromCurrentSelectionList.add(new FindResult(currElement, uid, elementType, graph.getId()));
                                }
                            }
                            // if the user wants to select all, select the matching element
                            if (selectAll && !removeFromCurrentSelection && !(currentSelection && replaceCurrentSelection)) {
                                graph.setBooleanValue(selectedAttribute, currElement, true);
                            }
                            // add the graph element to the foundResult list
                            foundResult.add(new FindResult(currElement, uid, elementType, graph.getId()));
                        }
                    }
                }
            }
        }
        /**
         * If currentSelection and replaceCurrentSelection are true, clear the current selection and
         * loop through the list of found elements and set them to selected.
         */
        if (currentSelection && replaceCurrentSelection) {
            selectFindInResults(currentSelection, findInCurrentSelectionList, graph, selectedAttribute);
            foundResult.clear();
            foundResult.addAll(findInCurrentSelectionList);
        }

        /**
         * If removeFromCurrentSelection is true, loop through the list of
         * found elements and set their selection status to false.
         */
        if (removeFromCurrentSelection) {
            selectRemoveFromResults(removeFromCurrentSelection, removeFromCurrentSelectionList, graph, selectedAttribute);
            foundResult.clear();
            foundResult.addAll(removeFromCurrentSelectionList);
        }
        
        // Clean the find results list to only contain unique graph elements
        final List<FindResult> distinctValues = foundResult.stream().distinct().collect(Collectors.toList());
        foundResult.clear();
        foundResult.addAll(distinctValues);

        // If the results list is null, has different parameters to the current list, and is not searching all graphs, then create a new results list
        if (ActiveFindResultsList.getBasicResultsList() == null || !ActiveFindResultsList.getBasicResultsList().getSearchParameters().equals(this.parameters)
                || (!this.parameters.isSearchAllGraphs() && !ActiveFindResultsList.getBasicResultsList().isEmpty()
                && !ActiveFindResultsList.getBasicResultsList().get(0).getGraphId().equals(graph.getId()))) {
            ActiveFindResultsList.setBasicResultsList(foundResult);
            ActiveFindResultsList.getBasicResultsList().setCurrentIndex(-1);
        } else {
            ActiveFindResultsList.addToBasicFindResultsList(foundResult);
        }
    }

    /**
     * Completes the steps required to select the FindResults within the
     * findInCurrentSelectionList. Its split into a separate function to reduce
     * cognitive complexity of the edit function.
     *
     * @param findInCurrentSelection
     * @param findInCurrentSelectionList
     * @param foundResult
     * @param graph
     * @param selectedAttribute
     */
    private void selectFindInResults(final boolean findInCurrentSelection, final FindResultsList findInCurrentSelectionList,
           final GraphWriteMethods graph, final int selectedAttribute) {
        if (findInCurrentSelection && !findInCurrentSelectionList.isEmpty()) {
            FindViewUtilities.clearSelection(graph);
            for (final FindResult fr : findInCurrentSelectionList) {
                graph.setBooleanValue(selectedAttribute, fr.getID(), true);
            }     
        }
    }

    /**
     * Completes the steps required to deselected the FindResults within the
     * removeFromCurrentSelectionList. Its split into a separate function to
     * reduce cognitive complexity of the edit function.
     *
     * @param removeFromCurrentSelection
     * @param removeFromCurrentSelectionList
     * @param foundResult
     * @param graph
     * @param selectedAttribute
     */
    private void selectRemoveFromResults(final boolean removeFromCurrentSelection, final FindResultsList removeFromCurrentSelectionList,
            final GraphWriteMethods graph, final int selectedAttribute) {
        if (removeFromCurrentSelection && !removeFromCurrentSelectionList.isEmpty()) {
            for (final FindResult fr : removeFromCurrentSelectionList) {
                graph.setBooleanValue(selectedAttribute, fr.getID(), false);
                if (getNext) {
                    foundResult.clear();
                    foundResult.addAll(removeFromCurrentSelectionList);
                    break;
                }
            }
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
    private int getIndex(final BasicFindReplaceParameters currentParameters, final BasicFindReplaceParameters oldParameters, int currentIndex) {
        if (!selectAll && currentParameters.equals(oldParameters)) {
            // If the query hasnt changed and there must be elements in the list
            // get the current index
            return currentIndex;
        }
        // If selecting all elements, reset the index
        return STARTING_INDEX;
    }

    @Override
    public String getName() {
        return "Find: Find and Replace";
    }
}

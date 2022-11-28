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
package au.gov.asd.tac.constellation.views.find2.plugins.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find2.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find2.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResult;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResultsList;
import au.gov.asd.tac.constellation.views.find2.utilities.FindViewUtilities;
import static au.gov.asd.tac.constellation.views.find2.utilities.FindViewUtilities.clearSelection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class handles the logic for selecting the correct elements on the graphs
 * based on the advanced search tabs user input.
 *
 * @author Atlas139mkm
 */
public class AdvancedSearchPlugin extends SimpleEditPlugin {

    private final boolean selectAll;
    private final AdvancedSearchParameters parameters;
    private final List<FindCriteriaValues> criteriaList;
    private final GraphElementType elementType;
    private final String allOrAny;
    private final String currentSelection;

    private FindResultsList findInCurrentSelectionList;

    private static final String ANY = "Any";
    private static final String ALL = "All";
    private static final String IGNORE = "Ignore";
    private static final String ADD_TO = "Add To";
    private static final String FIND_IN = "Find In";
    private static final String REMOVE_FROM = "Remove From";
    private static final String IS = "Is";
    private static final String IS_NOT = "Is Not";

    private static final int STARTING_INDEX = -1;

    public AdvancedSearchPlugin(final AdvancedSearchParameters parameters, final boolean selectAll, final boolean selectNext) {
        this.parameters = parameters;
        this.selectAll = selectAll;
        elementType = parameters.getGraphElementType();
        allOrAny = parameters.getAllOrAny();
        currentSelection = parameters.getCurrentSelection();
        criteriaList = parameters.getCriteriaValuesList();
    }

    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        //Retrieve the existing FindResultList Meta attribute
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        FindResultsList foundResult = graph.getObjectValue(stateId, 0);

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
             * parameters are instantiated as new variables as there were
             * manipulation issues elsewhere causing this process to fail.
             */
            final FindResultsList oldList = new FindResultsList(STARTING_INDEX, foundResult.getAdvancedSearchParameters());
            final AdvancedSearchParameters oldparameters = oldList.getAdvancedSearchParameters();
            final AdvancedSearchParameters newParamters = new AdvancedSearchParameters(this.parameters);
            final int newIndex = getIndex(newParamters, oldparameters, foundResult.getCurrentIndex());
            foundResult = new FindResultsList(newIndex, newParamters);
        }

        // do this if ignore selection
        if (IGNORE.equals(currentSelection)) {
            FindViewUtilities.clearSelection(graph);
        }

        foundResult.clear();
        graph.setObjectValue(stateId, 0, foundResult);

        final int elementCount = elementType.getElementCount(graph);

        findInCurrentSelectionList = new FindResultsList();
        final FindResultsList findAllMatchingResultsList = new FindResultsList();

        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());

        final Set<FindResult> findResultSet = new HashSet<>();

        /**
         * This loops through all existing graph elements, then loops through
         * all criteria values, then determines what data type the criteria
         * value corresponds to and proceeds the search based on that data type.
         *
         * For example if the data type of the criteria value was a string, it
         * would continue the search as if the graph element attribute in
         * question is a string
         */
        for (int i = 0; i < elementCount; i++) {
            // tracking number to see if a elements value matches all of the
            // users input critera
            int matchesAllCount = 0;

            // get the current element and its unique id
            final int currElement = elementType.getElement(graph, i);
            final long uid = elementType.getUID(graph, currElement);

            // loop through all of the criteriaValues
            for (final FindCriteriaValues values : criteriaList) {

                // get the int value of the current attribute
                int attributeInt = graph.getAttribute(elementType, values.getAttribute());

                // if the attribute int is greater than 0 it exists on this graph
                if (attributeInt >= 0) {

                    /**
                     * matches will change to false if the attributes value
                     * matches the given critera.
                     */
                    boolean matches = false;
                    // Determine what data type to treat the search as
                    switch (values.getAttributeType()) {
                        case StringAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsString(values, attributeInt, currElement, graph);
                            break;
                        case FloatAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsFloat(values, attributeInt, currElement, graph);
                            break;
                        case BooleanAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsBoolean(values, attributeInt, currElement, graph);
                            break;
                        case ColorAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsColor(values, attributeInt, currElement, graph);
                            break;
                        case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsDateTime(values, attributeInt, currElement, graph);
                            break;
                        case IconAttributeDescription.ATTRIBUTE_NAME:
                            matches = searchAsIcon(values, attributeInt, currElement, graph);
                            break;
                        default:
                            break;
                    }
                    // if a match was found
                    if (matches) {
                        // increase the matchesAllCount by 1
                        matchesAllCount++;

                        // If the serach is select all and match critera = any
                        if (selectAll && ANY.equals(allOrAny)) {

                            // If the current selection = ignore or add to
                            if ((IGNORE.equals(currentSelection) || ADD_TO.equals(currentSelection))) {

                                // set the elements selection attribute to true
                                graph.setBooleanValue(selectedAttribute, currElement, true);
                                // add a new find result to the found results list
                                // of the element
                                foundResult.add(new FindResult(currElement, uid, elementType, graph.getId()));

                                // if the current selection = find in and the graph element is already selected
                            } else if (FIND_IN.equals(currentSelection) && graph.getBooleanValue(selectedAttribute, currElement)) {
                                // add a new FindResult of the graph element to the findInCurrentSelection list
                                findInCurrentSelectionList.add(new FindResult(currElement, uid, elementType, graph.getId()));

                                // if the current selection = remove from and the graph element is already selected
                            } else if (REMOVE_FROM.equals(currentSelection) && graph.getBooleanValue(selectedAttribute, currElement)) {
                                // set the graph element selection attribute to false
                                graph.setBooleanValue(selectedAttribute, currElement, false);
                                // add a new find result to the found results list
                                // of the element
                                foundResult.add(new FindResult(currElement, uid, elementType, graph.getId()));
                            }
                        } else if (ANY.equals(allOrAny)) {
                            // if not select all and the match criteria = any
                            // add a new find result to the found results list
                            // of the element
                            foundResult.add(new FindResult(currElement, uid, elementType, graph.getId()));
                        } 
                    }
                }

                /**
                 * if match criteria = all and the attributes values match all
                 * of the criteria.
                 */
                if (allOrAny.contains(ALL) && matchesAllCount == criteriaList.size()) {

                    // add a new find result to the found results list
                    // of the element
                    foundResult.add(new FindResult(currElement, uid, elementType, graph.getId()));
                    findResultSet.add(new FindResult(currElement, uid, elementType, graph.getId()));
                    // if the attribute is already selected
                    if (graph.getBooleanValue(selectedAttribute, currElement)) {
                        // add it to the find in current selection list
                        findInCurrentSelectionList.add(new FindResult(currElement, uid, elementType, graph.getId()));
                    }
                }
            }
        }

        findAllMatchingResultsList.addAll(findResultSet);

        // if Find in select all the find in results
        if (FIND_IN.equals(currentSelection)) {
            selectFindInResults(FIND_IN.equals(currentSelection), findInCurrentSelectionList, foundResult, graph, selectedAttribute);

        // if remove from, deselect all the remove from results
        } else if (REMOVE_FROM.equals(currentSelection)) {
            removeFindInResults(REMOVE_FROM.equals(currentSelection), findInCurrentSelectionList, foundResult, graph, selectedAttribute);

        // if select all, select all the results that match all criteria
        } else if (selectAll) {
            selectMatchingAllResults(ALL.equals(allOrAny), findAllMatchingResultsList, foundResult, graph, selectedAttribute);
        }

        // Clean the find results list to only contain unique graph elements
        final List<FindResult> distinctValues = foundResult.stream().distinct().collect(Collectors.toList());
        foundResult.clear();
        foundResult.addAll(distinctValues);

        if (ActiveFindResultsList.getAdvancedResultsList() == null || !ActiveFindResultsList.getAdvancedResultsList().getAdvancedSearchParameters().equals(this.parameters)
                || (!this.parameters.isSearchAllGraphs() && ActiveFindResultsList.getAdvancedResultsList().get(0) != null && !ActiveFindResultsList.getAdvancedResultsList().get(0).getGraphId().equals(graph.getId()))) {
            ActiveFindResultsList.setAdvancedResultsList(foundResult);
        } else {
            ActiveFindResultsList.addToAdvancedFindResultsList(foundResult);
        }
    }

    /**
     * This is called when the user clicks select all and they are wanted to the
     * search to match all their criteria.
     *
     * @param findAllMatching allOrAny.equals("All")
     * @param findAllMatchingResultsList a list of all matching results
     * @param foundResult the foundResult list
     * @param graph the current graph
     * @param selectedAttribute the selectedAttribute
     */
    private void selectMatchingAllResults(final boolean findAllMatching, final FindResultsList findAllMatchingResultsList,
            final FindResultsList foundResult, final GraphWriteMethods graph, final int selectedAttribute) {

        // if match criteria == all and the results list isnt empty
        if (findAllMatching && !findAllMatchingResultsList.isEmpty()) {
            // if not adding to the current selection, clear the selection
            if (!ADD_TO.equals(currentSelection)) {
                clearSelection(graph);
            }
            // if select all, loop through all find results and select them
            if (selectAll) {
                for (final FindResult fr : findAllMatchingResultsList) {
                    graph.setBooleanValue(selectedAttribute, fr.getID(), true);
                }
            }

            // clear the found result and add the findAllmatching results to it
            foundResult.clear();
            foundResult.addAll(findAllMatchingResultsList);
        }
    }

    /**
     * This is called when the user wanted to find the results in their current
     * selection.
     *
     * @param findInCurrentSelection
     * @param findInCurrentSelectionList
     * @param foundResult
     * @param graph
     * @param selectedAttribute
     */
    private void selectFindInResults(final boolean findInCurrentSelection, final FindResultsList findInCurrentSelectionList,
            final FindResultsList foundResult, final GraphWriteMethods graph, final int selectedAttribute) {

        // if currenet selection = find in and the list isnt empty
        if (findInCurrentSelection && !findInCurrentSelectionList.isEmpty()) {
            // clear the seleection
            clearSelection(graph);

            // loop through the list selecting all find results
            for (final FindResult fr : findInCurrentSelectionList) {
                graph.setBooleanValue(selectedAttribute, fr.getID(), true);
            }

            // clear the foundResult and add the findIncurrentSelection list to it
            foundResult.clear();
            foundResult.addAll(findInCurrentSelectionList);
        }
    }

    /**
     * This is called when the user wanted to remove the results from their
     * current selection.
     *
     * @param findInCurrentSelection
     * @param findInCurrentSelectionList
     * @param foundResult
     * @param graph
     * @param selectedAttribute
     */
    private void removeFindInResults(final boolean findInCurrentSelection, final FindResultsList removeFromCurrentSelectionList,
            final FindResultsList foundResult, final GraphWriteMethods graph, final int selectedAttribute) {
        if (findInCurrentSelection && !removeFromCurrentSelectionList.isEmpty()) {

            // loop through all results and de select them
            for (final FindResult fr : removeFromCurrentSelectionList) {
                graph.setBooleanValue(selectedAttribute, fr.getID(), false);
            }

            // clear the foundResult and add the findIncurrentSelection list to it
            foundResult.clear();
            foundResult.addAll(removeFromCurrentSelectionList);
        }
    }

    /**
     * This function checks to see if a graph elements string attribute matches
     * the criteria specified by the stringCriteriaValues.
     *
     * @param values the string criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsString(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {
        final StringCriteriaValues stringValues = (StringCriteriaValues) values;
        String value = graph.getStringValue(attributeInt, currElement);

        // create a list that will contain the string searches
        final List<String> allSearchableString = new ArrayList<>();
        allSearchableString.add(stringValues.getText());

        // if using the list, clear it and re add all values in the text list
        if (stringValues.isUseList()) {
            allSearchableString.clear();
            allSearchableString.addAll(stringValues.getTextList());
        }

        // if ignoring the case set the value and each of the strings to lower case
        if (stringValues.isIgnoreCase() && value != null) {
            value = value.toLowerCase();
            int i = 0;
            for (String str : allSearchableString) {
                allSearchableString.set(i, str.toLowerCase());
                i++;
            }
        }
        boolean matches = false;

        /**
         * This switch handles the matching check based on the filter choice by
         * the user. It checks all str within the allSearchable strings list.
         */
        switch (stringValues.getFilter()) {
            case IS:
                for (final String str : allSearchableString) {
                    if (str.equals(value)) {
                        matches = true;
                    }
                }
                break;
            case IS_NOT:
                for (final String str : allSearchableString) {
                    if (!str.equals(value)) {
                        matches = true;
                    }
                }
                break;
            case "Contains":
                for (final String str : allSearchableString) {
                    if (value != null && value.contains(str)) {
                        matches = true;
                    }
                }
                break;
            case "Doesn't Contain":
                for (final String str : allSearchableString) {
                    if (value != null && !value.contains(str)) {
                        matches = true;
                    }
                }

                break;
            case "Begins With":
                for (final String str : allSearchableString) {
                    if (value != null && value.startsWith(str)) {
                        matches = true;
                    }
                }
                break;
            case "Ends With":
                for (final String str : allSearchableString) {
                    if (value != null && value.endsWith(str)) {
                        matches = true;
                    }
                }
                break;
            case "Matches (Regex)":
                for (final String str : allSearchableString) {
                    if (value != null) {
                        final int caseSensitivity = Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
                        final Pattern searchPattern = Pattern.compile(str, caseSensitivity);
                        final Matcher match = searchPattern.matcher(value);
                        matches = match.find();
                    }
                }
                break;
            default:
                break;
        }
        return matches;
    }

    /**
     * This function checks to see if a graph elements float attribute matches
     * the criteria specified by the floatCriteriaValues.
     *
     * @param values the float criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsFloat(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {
        final FloatCriteriaValues floatValues = (FloatCriteriaValues) values;
        final float value = graph.getFloatValue(attributeInt, currElement);

        boolean matches = false;

        /**
         * This switch handles the matching check based on the filter choice by
         * the user. It checks the users float value against the attributes
         * float value
         */
        switch (floatValues.getFilter()) {
            case IS:
                if (floatValues.getFloatValuePrimary() == value) {
                    matches = true;
                }
                break;
            case IS_NOT:
                if (floatValues.getFloatValuePrimary() != value) {
                    matches = true;
                }
                break;
            case "Is Less Than":
                if (floatValues.getFloatValuePrimary() > value) {
                    matches = true;
                }
                break;
            case "Is Greater Than":
                if (floatValues.getFloatValuePrimary() < value) {
                    matches = true;
                }
                break;
            case "Is Between":
                if (value < floatValues.getFloatValuePrimary() && value > floatValues.getFloatValueSecondary()) {
                    matches = true;
                }
                if (value > floatValues.getFloatValuePrimary() && value < floatValues.getFloatValueSecondary()) {
                    matches = true;
                }

                break;
            default:
                break;
        }
        return matches;
    }

    /**
     * This function checks to see if a graph elements boolean attribute matches
     * the criteria specified by the BooleanCriteriaValues.
     *
     * @param values the boolean criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsBoolean(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {

        final BooleanCriteriaValues booleanValues = (BooleanCriteriaValues) values;
        final boolean value = graph.getBooleanValue(attributeInt, currElement);
        boolean matches = false;

        // if the attributes bool value == the users bool value matches = true
        if (booleanValues.getBoolValue() == value) {
            matches = true;
        }
        return matches;
    }

    /**
     * This function checks to see if a graph elements color attribute matches
 the criteria specified by the ColorCriteriaValues.
     *
     * @param values the color criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsColor(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {
        final ColorCriteriaValues colorValues = (ColorCriteriaValues) values;
        final ConstellationColor color = graph.getObjectValue(attributeInt, currElement);
        boolean matches = false;

        // if the color of the attribute matches the users color matches = true
        if (colorValues.getFilter().equals(IS) && colorValues.getColorValue().equals(color)) {
            matches = true;
            // if the color of the attribute doesnt match the users color matches = true
        } else if (colorValues.getFilter().equals(IS_NOT) && !colorValues.getColorValue().equals(color)) {
            matches = true;
        }
        return matches;
    }

    /**
     * This function checks to see if a graph elements zoned date time attribute
     * matches the criteria specified by the DateTimeCriteriaValues.
     *
     * @param values the date time criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsDateTime(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {
        final DateTimeCriteriaValues dateTimeValues = (DateTimeCriteriaValues) values;
        String dateTimeString = graph.getStringValue(attributeInt, currElement);
        boolean matches = false;

        if (dateTimeString == null || dateTimeValues.getDateTimeStringPrimaryValue().isEmpty()) {
            if ("Didn't Occur On".equals(dateTimeValues.getFilter())) {
                matches = true;
            }
            return matches;
        }
        // convert the string date time into a zonedDateTime for comparisons
        final String[] splitDateTime = dateTimeValues.getDateTimeStringPrimaryValue().split(" ");
        final String parseFormatedString = splitDateTime[0] + "T" + splitDateTime[1] + splitDateTime[2];
        final ZonedDateTime valueDateTime = ZonedDateTime.parse(parseFormatedString);

        // convert the string date time into a zonedDateTime for comparisons
        final String[] splitAttributeDateTime = dateTimeString.split(" ");
        final String parseAttributeFormatedString = splitAttributeDateTime[0] + "T" + splitAttributeDateTime[1] + splitAttributeDateTime[2];
        final ZonedDateTime attributeDateTime = ZonedDateTime.parse(parseAttributeFormatedString);

        /**
         * This switch handles the matching check based on the filter choice by
         * the user. It checks the users date time values against the attributes
         * date time values
         */
        switch (dateTimeValues.getFilter()) {
            case "Occured On":
                if (valueDateTime.isEqual(attributeDateTime)) {
                    matches = true;
                }
                break;

            case "Didn't Occur On":
                if (!valueDateTime.isEqual(attributeDateTime)) {
                    matches = true;
                }
                break;
            case "Occured Before":
                if (!valueDateTime.isBefore(attributeDateTime)) {
                    matches = true;
                }
                break;
            case "Occured After":
                if (!valueDateTime.isAfter(attributeDateTime)) {
                    matches = true;
                }
                break;
            case "Occured Between":
                if (dateTimeValues.getDateTimeStringSecondaryValue().isEmpty()) {
                    return matches;
                }
                final String[] splitDateTimeTwo = dateTimeValues.getDateTimeStringSecondaryValue().split(" ");
                final String parseFormatedStringTwo = splitDateTimeTwo[0] + "T" + splitDateTimeTwo[1] + splitDateTimeTwo[2];
                final ZonedDateTime valueDateTimeTwo = ZonedDateTime.parse(parseFormatedStringTwo);
                if (attributeDateTime.isAfter(valueDateTime) && attributeDateTime.isBefore(valueDateTimeTwo)) {
                    matches = true;
                }
                if (attributeDateTime.isAfter(valueDateTimeTwo) && attributeDateTime.isBefore(valueDateTime)) {
                    matches = true;
                }
                break;
        }
        return matches;
    }

    /**
     * This function checks to see if a graph elements icon attribute matches
     * the criteria specified by the IconCriteriaValues.
     *
     * @param values the icon criteriaValues
     * @param attributeInt the int of the attribute
     * @param currElement the currentElement
     * @param graph the current graph
     * @return
     */
    private boolean searchAsIcon(final FindCriteriaValues values, final int attributeInt, final int currElement, final GraphWriteMethods graph) {
        final IconCriteriaValues iconValues = (IconCriteriaValues) values;
        final ConstellationIcon icon = graph.getObjectValue(attributeInt, currElement);
        boolean matches = false;

        // if the icon of the attribute matches the users icon matches = true
        if (iconValues.getFilter().equals(IS) && iconValues.getIconValue().equals(icon)) {
            matches = true;

            // if the icon of the attribute does not match the users icon matches = true
        } else if (iconValues.getFilter().equals(IS_NOT) && !iconValues.getIconValue().equals(icon)) {
            matches = true;
        }
        return matches;
    }

    /**
     * Determines what index is correct for the found results list based on if
     * the user is finding all, doing their first find, doing a different find
     * to their previous
     *
     * @param foundResult the list of foundResults
     * @return the correct current index
     */
    private int getIndex(final AdvancedSearchParameters currentParameters, final AdvancedSearchParameters oldParameters, int currentIndex) {
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
        return "Find: Advanced Search";
    }
}

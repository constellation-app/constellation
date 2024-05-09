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
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindViewUtilities;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds String based attributes within the graph and replaces its value.
 *
 * @author twinkle2_little
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {"SEARCH", "MODIFY"})
public class ReplacePlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final List<Attribute> selectedAttributes;
    private String findString;
    private final String replaceString;
    private boolean regex;
    private final boolean ignorecase;
    private final boolean replaceNext;
    private final boolean currentSelection;
    private final boolean searchAllGraphs;

    public ReplacePlugin(final BasicFindReplaceParameters parameters, final boolean replaceAll, final boolean replaceNext) {
        this.elementType = parameters.getGraphElement();
        this.selectedAttributes = parameters.getAttributeList();
        this.findString = parameters.getFindString();
        this.replaceString = parameters.getReplaceString();
        this.regex = parameters.isRegEx();
        this.ignorecase = parameters.isIgnoreCase();
        this.replaceNext = replaceNext;
        this.searchAllGraphs = parameters.isSearchAllGraphs();
        this.currentSelection = parameters.isCurrentSelection();
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        if (findString.isEmpty()) {
            findString = "^$";
            regex = true;
        }

        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
        final int elementCount = elementType.getElementCount(graph);
        final String searchString = regex ? findString : Pattern.quote(findString);
        final int caseSensitivity = ignorecase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        final Pattern searchPattern = Pattern.compile(searchString, caseSensitivity);

        /**
         * Loop through all selected attributes, get the current element of the
         * selected type and its value, check the value isn't null, then compare
         * the value with the replace string based on the search preferences. If
         * that element matches the search criteria, change its matching part of
         * the value to the replace string.
         */
        for (final Attribute a : selectedAttributes) {
            // If the attribute exists on the graph
            if (graph.getAttribute(elementType, a.getName()) >= 0) {

                // for each element of the given type
                for (int i = 0; i < elementCount; i++) {

                    // get the current graph element
                    final int currElement = elementType.getElement(graph, i);

                    // get string value of it graph elements attribute
                    final String value = graph.getStringValue(a.getId(), currElement);

                    // get the selected value of that graph element
                    boolean selected = graph.getBooleanValue(selectedAttribute, currElement);

                    // If the value isnt null
                    if (value != null) {

                        final Matcher match = searchPattern.matcher(value);
                        final String newValue = match.replaceAll(replaceString);

                        if (!newValue.equals(value)) {
                            // if replace in selected is false
                            if (!currentSelection) {
                                // set the string of the element types attribute
                                // to the new value
                                graph.setStringValue(a.getId(), currElement, newValue);
                                // Swap to view the graph where the element is found
                                if (searchAllGraphs) {
                                    FindViewUtilities.searchAllGraphs(graph);
                                }
                                if (replaceNext) {
                                    return;
                                }
                            } else {
                                // if selected is true
                                if (selected) {
                                    // set the string of the element types attribute
                                    // to the new value
                                    graph.setStringValue(a.getId(), currElement, newValue);
                                    // Swap to view the graph where the element is found
                                    if (searchAllGraphs) {
                                        FindViewUtilities.searchAllGraphs(graph);
                                    }
                                    if (replaceNext) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }  
    }

    @Override
    public String getName() {
        return "Find/Replaces String attribute values";
    }
}

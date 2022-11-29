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
package au.gov.asd.tac.constellation.views.find;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class does the actual action of finding.
 *
 * @author twinkle2_little
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
public class BasicFindPlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final ArrayList<Attribute> selectedAttributes;
    private String findString;
    private boolean regex;
    private final boolean ignorecase;
    private final boolean matchWholeWord;
    private final boolean addToSelection;

    public BasicFindPlugin(GraphElementType elementType, ArrayList<Attribute> stringAttr, String findString, Boolean regex, boolean ignorecase, boolean matchWholeWord, boolean addToSelection) {
        this.elementType = elementType;
        this.selectedAttributes = stringAttr;
        this.findString = findString;
        this.regex = regex;
        this.ignorecase = ignorecase;
        this.matchWholeWord = matchWholeWord;
        this.addToSelection = addToSelection;
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
        if (findString.isEmpty()) {
            findString = "^$";
            regex = true;
        }
        boolean found;
        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
        final int elementCount = elementType.getElementCount(graph);
        // do this if add to selection
        if (!addToSelection) {
            clearSelection(graph);
        }
        String searchString = regex ? findString : Pattern.quote(findString);
        int caseSensitivity = ignorecase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        Pattern searchPattern = Pattern.compile(searchString, caseSensitivity);
        for (Attribute a : selectedAttributes) {
            for (int i = 0; i < elementCount; i++) {
                int currElement = elementType.getElement(graph, i);
                String value = graph.getStringValue(a.getId(), currElement);
                if (value != null) {
                    Matcher match = searchPattern.matcher(value);
                    if (matchWholeWord) {
                        found = match.matches();
                    } else {
                        found = match.find();
                    }
                    if (found) {
                        graph.setBooleanValue(selectedAttribute, currElement, true);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Find: Find and Replace";
    }
}

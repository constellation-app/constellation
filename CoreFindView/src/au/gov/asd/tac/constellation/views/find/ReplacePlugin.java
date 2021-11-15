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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
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
 * Finds String based attributes within the graph and replaces its value.
 *
 * @author twinkle2_little
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {"SEARCH", PluginTags.MODIFY})
public class ReplacePlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final ArrayList<Attribute> selectedAttributes;
    private String findString;
    private final String replaceString;
    private Boolean regex;
    private final Boolean ignorecase;

    public ReplacePlugin(GraphElementType elementType, ArrayList<Attribute> stringAttr, String findString, String replaceString, Boolean regex, Boolean ignorecase) {
        this.elementType = elementType;
        this.selectedAttributes = stringAttr;
        this.findString = findString;
        this.replaceString = replaceString;
        this.regex = regex;
        this.ignorecase = ignorecase;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        if (findString.isEmpty()) {
            findString = "^$";
            regex = true;
        }
        int elementCount = elementType.getElementCount(graph);
        String searchString = regex ? findString : Pattern.quote(findString);
        int caseSensitivity = ignorecase ? Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE : 0;
        Pattern searchPattern = Pattern.compile(searchString, caseSensitivity);
        for (Attribute a : selectedAttributes) {
            for (int i = 0; i < elementCount; i++) {
                int currElement = elementType.getElement(graph, i);
                String value = graph.getStringValue(a.getId(), currElement);
                if (value != null) {
                    Matcher match = searchPattern.matcher(value);
                    String newValue = match.replaceAll(replaceString);
                    if (!newValue.equals(value)) {
                        graph.setStringValue(a.getId(), currElement, newValue);
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

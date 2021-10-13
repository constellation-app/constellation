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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;

/**
 *
 * @author Atlas139mkm
 */
public class BasicFindReplaceParameters {

    private final String findString;
    private final String replaceString;
    private final GraphElementType graphElement;
    private final ArrayList<Attribute> attributeList;
    private final boolean standardText;
    private final boolean regEx;
    private final boolean ignoreCase;
    private final boolean exactMatch;
    private final boolean searchAllGraphs;

    public BasicFindReplaceParameters(BasicFindReplaceParameters parameters) {
        this.findString = parameters.findString;
        this.replaceString = parameters.replaceString;
        this.graphElement = parameters.graphElement;
        this.attributeList = parameters.attributeList;
        this.standardText = parameters.standardText;
        this.regEx = parameters.regEx;
        this.ignoreCase = parameters.ignoreCase;
        this.exactMatch = parameters.exactMatch;
        this.searchAllGraphs = parameters.searchAllGraphs;

    }

    public BasicFindReplaceParameters(String findString, String replaceString, GraphElementType graphElement,
            ArrayList<Attribute> attributeList, boolean standardText, boolean regEx, boolean ignoreCase,
            boolean exactMatch, boolean searchAllGraphs) {

        this.findString = findString;
        if (replaceString.isEmpty() || replaceString == "") {
            this.replaceString = "";
        } else {
            this.replaceString = replaceString;
        }
        this.graphElement = graphElement;

        this.attributeList = attributeList;

        this.standardText = standardText;
        this.regEx = regEx;
        this.ignoreCase = ignoreCase;
        this.exactMatch = exactMatch;
        this.searchAllGraphs = searchAllGraphs;

        trimText(findString);
        ignoreCase(findString);

    }

    public boolean equals(BasicFindReplaceParameters object) {
        int matches = 0;

        if (this == null || object == null) {
            return false;
        }
        if (findString.equals(object.getFindString())) {
            matches++;
        }
        if (replaceString.equals(object.getReplaceString())) {
            matches++;
        }
        if (graphElement.equals(object.getGraphElement())) {
            matches++;
        }
        if (attributeList.equals(object.getAttributeList())) {
            matches++;
        }
        if (standardText == object.isStandardText()) {
            matches++;
        }
        if (regEx == object.isRegEx()) {
            matches++;
        }
        if (ignoreCase == object.isIgnoreCase()) {
            matches++;
        }
        if (exactMatch == object.isExactMatch()) {
            matches++;
        }
        if (searchAllGraphs == object.isSearchAllGraphs()) {
            matches++;
        }
        if (matches == 9) {
            return true;
        }
        return false;
    }

    public String getFindString() {
        return findString;
    }

    public String getReplaceString() {
        return replaceString;
    }

    public GraphElementType getGraphElement() {
        return graphElement;
    }

    public ArrayList<Attribute> getAttributeList() {
        return attributeList;
    }

    public boolean isStandardText() {
        return standardText;
    }

    public boolean isRegEx() {
        return regEx;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public boolean isSearchAllGraphs() {
        return searchAllGraphs;
    }

    private void trimText(final String text) {
        //  if (!isExactMatch()) {
        text.trim();
        // }
    }

    private void ignoreCase(final String text) {
        if (isIgnoreCase()) {
            text.toLowerCase();
        }
    }
}

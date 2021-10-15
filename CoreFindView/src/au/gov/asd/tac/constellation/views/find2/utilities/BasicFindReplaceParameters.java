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
package au.gov.asd.tac.constellation.views.find2.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Atlas139mkm
 */
public class BasicFindReplaceParameters {

    private String findString;
    private String replaceString;
    private GraphElementType graphElement;
    private List<Attribute> attributeList;
    private boolean standardText;
    private boolean regEx;
    private boolean ignoreCase;
    private boolean exactMatch;
    private boolean searchAllGraphs;

    public BasicFindReplaceParameters() {
        // May need initialisation for the class variables that can be null.
        attributeList = new ArrayList<>();
        graphElement = GraphElementType.VERTEX;
    }

    /**
     * Construct a Parameters object from another
     *
     * @param parameters the parameters object to copy from
     */
    public BasicFindReplaceParameters(final BasicFindReplaceParameters parameters) {
        this.copyParameters(parameters);
    }

    /**
     * Copy the parameters from a parameters object into the current object.
     *
     * @param parameters
     */
    public void copyParameters(final BasicFindReplaceParameters parameters) {
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

    public BasicFindReplaceParameters(final String findString, final String replaceString, final GraphElementType graphElement,
            final List<Attribute> attributeList, final boolean standardText, final boolean regEx, final boolean ignoreCase,
            final boolean exactMatch, final boolean searchAllGraphs) {

        this.findString = findString;
        this.replaceString = StringUtils.isBlank(replaceString) ? "" : replaceString;
        this.graphElement = graphElement;
        this.attributeList = attributeList;
        this.standardText = standardText;
        this.regEx = regEx;
        this.ignoreCase = ignoreCase;
        this.exactMatch = exactMatch;
        this.searchAllGraphs = searchAllGraphs;

        // Do these need to happen before the assignment to the class variables?
        trimText(findString);
        ignoreCase(findString);
    }

    public boolean equals(final BasicFindReplaceParameters object) {
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

    public List<Attribute> getAttributeList() {
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

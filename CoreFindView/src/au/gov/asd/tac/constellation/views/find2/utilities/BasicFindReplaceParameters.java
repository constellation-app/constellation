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
package au.gov.asd.tac.constellation.views.find2.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private boolean addTo;
    private boolean findIn;
    private boolean removeFrom;
    private boolean replaceIn;

    public BasicFindReplaceParameters() {
        // May need initialisation for the class variables that can be null.
        this.findString = "";
        this.replaceString = "";
        this.graphElement = GraphElementType.VERTEX;
        this.attributeList = new ArrayList<>();
        this.standardText = false;
        this.regEx = false;
        this.ignoreCase = false;
        this.exactMatch = false;
        this.searchAllGraphs = false;
        this.findIn = false;
        this.addTo = false;
        this.removeFrom = false;
        this.replaceIn = false;

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
        this.findIn = parameters.findIn;
        this.addTo = parameters.addTo;
        this.removeFrom = parameters.removeFrom;
        this.replaceIn = parameters.replaceIn;
    }

    /**
     * Create a new object with all of UI elements present on the basicFind and
     * replace tabs
     *
     * @param findString
     * @param replaceString
     * @param graphElement
     * @param attributeList
     * @param standardText
     * @param regEx
     * @param ignoreCase
     * @param exactMatch
     * @param findIn
     * @param addTo
     * @param removeFrom
     * @param replaceIn
     * @param searchAllGraphs
     */
    public BasicFindReplaceParameters(final String findString, final String replaceString, final GraphElementType graphElement,
            final List<Attribute> attributeList, final boolean standardText, final boolean regEx, final boolean ignoreCase,
            final boolean exactMatch, final boolean findIn, final boolean addTo, final boolean removeFrom, final boolean replaceIn, final boolean searchAllGraphs) {

        this.findString = findString;
        this.replaceString = StringUtils.isBlank(replaceString) ? "" : replaceString;
        this.graphElement = graphElement;
        this.attributeList = attributeList;
        this.standardText = standardText;
        this.regEx = regEx;
        this.ignoreCase = ignoreCase;
        this.exactMatch = exactMatch;
        this.searchAllGraphs = searchAllGraphs;
        this.findIn = findIn;
        this.addTo = addTo;
        this.removeFrom = removeFrom;
        this.replaceIn = replaceIn;

    }

    /**
     * Checks to see if two basicFindParameters are the same by checking if 9/9
     * parameters match
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(final Object object) {
        int matches = 0;
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BasicFindReplaceParameters parameters = (BasicFindReplaceParameters) object;

        if (findString.equals(parameters.getFindString())) {
            matches++;
        }
        if (replaceString.equals(parameters.getReplaceString())) {
            matches++;
        }
        if (graphElement.equals(parameters.getGraphElement())) {
            matches++;
        }
        if (attributeList.equals(parameters.getAttributeList())) {
            matches++;
        }
        if (standardText == parameters.isStandardText()) {
            matches++;
        }
        if (regEx == parameters.isRegEx()) {
            matches++;
        }
        if (ignoreCase == parameters.isIgnoreCase()) {
            matches++;
        }
        if (exactMatch == parameters.isExactMatch()) {
            matches++;
        }
        if (searchAllGraphs == parameters.isSearchAllGraphs()) {
            matches++;
        }
        return matches == 9;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.findString);
        hash = 83 * hash + Objects.hashCode(this.replaceString);
        hash = 83 * hash + Objects.hashCode(this.graphElement);
        hash = 83 * hash + Objects.hashCode(this.attributeList);
        hash = 83 * hash + (this.standardText ? 1 : 0);
        hash = 83 * hash + (this.regEx ? 1 : 0);
        hash = 83 * hash + (this.ignoreCase ? 1 : 0);
        hash = 83 * hash + (this.exactMatch ? 1 : 0);
        hash = 83 * hash + (this.searchAllGraphs ? 1 : 0);
        return hash;
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

    public boolean isAddTo() {
        return addTo;
    }

    public boolean isFindIn() {
        return findIn;
    }

    public boolean isRemoveFrom() {
        return removeFrom;
    }

    public boolean isReplaceIn() {
        return replaceIn;
    }

}

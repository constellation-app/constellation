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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Atlas139mkm
 */
public class AdvancedSearchParameters {

    private List<FindCriteriaValues> criteriaValuesList;
    private GraphElementType graphElementType;
    private String allOrAny;
    private String currentSelection;
    private boolean searchAllGraphs;

    public AdvancedSearchParameters() {
        this.criteriaValuesList = new ArrayList<>();
        this.graphElementType = GraphElementType.VERTEX;
        this.allOrAny = "All";
        this.currentSelection = "Ignore";
        this.searchAllGraphs = false;
    }

    public AdvancedSearchParameters(final List<FindCriteriaValues> criteriaValuesList, final GraphElementType graphElementType, String allOrAny, String currentSelection, final boolean searchAllGraphs) {
        this.criteriaValuesList = criteriaValuesList;
        this.graphElementType = graphElementType;
        this.allOrAny = allOrAny;
        this.currentSelection = currentSelection;
        this.searchAllGraphs = searchAllGraphs;
    }

    /**
     * Copy the parameters from a parameters object into the current object.
     *
     * @param parameters
     */
    public void copyParameters(final AdvancedSearchParameters parameters) {
        this.criteriaValuesList = parameters.criteriaValuesList;
        this.graphElementType = parameters.graphElementType;
        this.allOrAny = parameters.allOrAny;
        this.currentSelection = parameters.currentSelection;
    }

    public List<FindCriteriaValues> getCriteriaValuesList() {
        return criteriaValuesList;
    }

    public GraphElementType getGraphElementType() {
        return graphElementType;
    }

    public String getAllOrAny() {
        return allOrAny;
    }

    public String getCurrentSelection() {
        return currentSelection;
    }

    public boolean isSearchAllGraphs() {
        return searchAllGraphs;
    }

}

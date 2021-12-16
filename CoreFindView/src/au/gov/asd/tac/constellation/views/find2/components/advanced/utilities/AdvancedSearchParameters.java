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
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColourCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
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

    /**
     * Default constructor sets all variables to their default values
     */
    public AdvancedSearchParameters() {
        this.criteriaValuesList = new ArrayList<>();
        this.graphElementType = GraphElementType.VERTEX;
        this.allOrAny = "All";
        this.currentSelection = "Ignore";
        this.searchAllGraphs = false;
    }

    /**
     * Constructor that allows assigning of all variables.
     *
     * @param criteriaValuesList
     * @param graphElementType
     * @param allOrAny
     * @param currentSelection
     * @param searchAllGraphs
     */
    public AdvancedSearchParameters(final List<FindCriteriaValues> criteriaValuesList, final GraphElementType graphElementType, String allOrAny, String currentSelection, final boolean searchAllGraphs) {
        this.criteriaValuesList = criteriaValuesList;
        this.graphElementType = graphElementType;
        this.allOrAny = allOrAny;
        this.currentSelection = currentSelection;
        this.searchAllGraphs = searchAllGraphs;
    }

    public AdvancedSearchParameters(final AdvancedSearchParameters parameters) {
        this.copyParameters(parameters);
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
        this.searchAllGraphs = parameters.searchAllGraphs;
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

    /**
     * This checks equality between 2 AdvancedSearchParamter objects. It needs
     * to check every attribute to determine true equality.
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
        AdvancedSearchParameters parameters = (AdvancedSearchParameters) object;

        if (searchAllGraphs == parameters.isSearchAllGraphs()) {
            matches++;
        }
        if (graphElementType.equals(parameters.getGraphElementType())) {
            matches++;
        }
        if (allOrAny.equals(parameters.getAllOrAny())) {
            matches++;
        }
        if (currentSelection == parameters.getCurrentSelection()) {
            matches++;
        }
        int i = 0;
        if (criteriaValuesList.size() == parameters.getCriteriaValuesList().size()) {
            for (FindCriteriaValues values : criteriaValuesList) {
                if (!values.getAttribute().equals(parameters.getCriteriaValuesList().get(i).getAttribute())) {
                    return false;
                }
                if (!values.getAttributeType().equals(parameters.getCriteriaValuesList().get(i).getAttributeType())) {
                    return false;
                }
                if (!values.getFilter().equals(parameters.getCriteriaValuesList().get(i).getFilter())) {
                    return false;
                }

                if (values.getAttributeType().equals(parameters.getCriteriaValuesList().get(i).getAttributeType())) {
                    switch (values.getAttributeType()) {
                        case StringAttributeDescription.ATTRIBUTE_NAME:
                            StringCriteriaValues stringParameterValues = (StringCriteriaValues) values;
                            StringCriteriaValues stringActualValues = (StringCriteriaValues) parameters.getCriteriaValuesList().get(i);

                            if (!stringParameterValues.isIgnoreCase() == stringActualValues.isIgnoreCase()) {
                                return false;
                            }
                            if (!stringParameterValues.isUseList() == stringActualValues.isUseList()) {
                                return false;
                            }
                            if (!stringActualValues.isUseList() && !stringParameterValues.getText().equals(stringActualValues.getText())) {
                                return false;
                            }
                            if (stringActualValues.isUseList() && !stringParameterValues.getTextList().equals(stringActualValues.getTextList())) {
                                return false;
                            }
                            break;
                        case FloatAttributeDescription.ATTRIBUTE_NAME:
                            FloatCriteriaValues floatParameterValues = (FloatCriteriaValues) values;
                            FloatCriteriaValues floatActualValues = (FloatCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (floatParameterValues.getFloatValuePrimary() != floatActualValues.getFloatValuePrimary()) {
                                return false;
                            }
                            if (floatParameterValues.getFloatValueSecondary() != floatActualValues.getFloatValueSecondary()) {
                                return false;
                            }
                            break;
                        case BooleanAttributeDescription.ATTRIBUTE_NAME:
                            BooleanCriteriaValues boolParameterValues = (BooleanCriteriaValues) values;
                            BooleanCriteriaValues boolActualValues = (BooleanCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (boolParameterValues.getBoolValue() != boolActualValues.getBoolValue()) {
                                return false;
                            }
                            break;
                        case ColorAttributeDescription.ATTRIBUTE_NAME:
                            ColourCriteriaValues colourParameterValues = (ColourCriteriaValues) values;
                            ColourCriteriaValues colourActualValues = (ColourCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!colourParameterValues.getColorValue().equals(colourActualValues.getColorValue())) {
                                return false;
                            }
                            break;
                        case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME:
                            DateTimeCriteriaValues dateTimeParameterValues = (DateTimeCriteriaValues) values;
                            DateTimeCriteriaValues dateTimeActualValues = (DateTimeCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!dateTimeParameterValues.getDateTimeStringPrimaryValue().equals(dateTimeActualValues.getDateTimeStringPrimaryValue())) {
                                return false;
                            }
                            if (!dateTimeParameterValues.getDateTimeStringSecondaryValue().equals(dateTimeActualValues.getDateTimeStringSecondaryValue())) {
                                return false;
                            }
                            break;
                        case IconAttributeDescription.ATTRIBUTE_NAME:
                            IconCriteriaValues iconParameterValues = (IconCriteriaValues) values;
                            IconCriteriaValues iconActualValues = (IconCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!iconParameterValues.getIconValue().equals(iconActualValues.getIconValue())) {
                                return false;
                            }
                            break;
                        default:
                            break;
                    }
                }
                i++;
            }

        } else {
            return false;
        }
        return matches == 4;
    }
}

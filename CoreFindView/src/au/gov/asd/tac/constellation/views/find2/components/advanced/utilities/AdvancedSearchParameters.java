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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public AdvancedSearchParameters(final List<FindCriteriaValues> criteriaValuesList, final GraphElementType graphElementType, final String allOrAny, final String currentSelection, final boolean searchAllGraphs) {
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

    /**
     * Gets the criteriaValuesList. This contains a list of all the
     * findCriteriaValues
     *
     * @return
     */
    public List<FindCriteriaValues> getCriteriaValuesList() {
        return criteriaValuesList;
    }

    /**
     * gets the graphElementType of the object.
     *
     * @return
     */
    public GraphElementType getGraphElementType() {
        return graphElementType;
    }

    /**
     * Gets the All or Any string
     *
     * @return
     */
    public String getAllOrAny() {
        return allOrAny;
    }

    /**
     * Gets the currentSelection string
     *
     * @return
     */
    public String getCurrentSelection() {
        return currentSelection;
    }

    /**
     * Gets the isSearchAllGraphs boolean value
     *
     * @return
     */
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
        final AdvancedSearchParameters parameters = (AdvancedSearchParameters) object;

        if (searchAllGraphs == parameters.isSearchAllGraphs()) {
            matches++;
        }
        if (graphElementType.equals(parameters.getGraphElementType())) {
            matches++;
        }
        if (allOrAny.equals(parameters.getAllOrAny())) {
            matches++;
        }
        if (currentSelection.equals(parameters.getCurrentSelection())) {
            matches++;
        }

        // if the size is the same to the new object. continue
        int i = 0;
        if (criteriaValuesList.size() == parameters.getCriteriaValuesList().size()) {
            // for each value in the criteriaValuesList
            for (FindCriteriaValues values : criteriaValuesList) {
                // if the two objects have different attributes return false
                if (!values.getAttribute().equals(parameters.getCriteriaValuesList().get(i).getAttribute())
                        || !values.getAttributeType().equals(parameters.getCriteriaValuesList().get(i).getAttributeType())
                        || !values.getFilter().equals(parameters.getCriteriaValuesList().get(i).getFilter())) {
                    return false;
                }
                // if the two objects attributeType is the same. continue
                if (values.getAttributeType().equals(parameters.getCriteriaValuesList().get(i).getAttributeType())) {
                    // determine the attribute type and handle the comparison appropriately
                    // for the object comparion all values must be identical
                    switch (values.getAttributeType()) {
                        // treat values as strings
                        case StringAttributeDescription.ATTRIBUTE_NAME:
                            StringCriteriaValues stringParameterValues = (StringCriteriaValues) values;
                            StringCriteriaValues stringActualValues = (StringCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (stringParameterValues.isIgnoreCase() != stringActualValues.isIgnoreCase()
                                    || stringParameterValues.isUseList() != stringActualValues.isUseList()
                                    || (!stringActualValues.isUseList() && !stringParameterValues.getText().equals(stringActualValues.getText()))
                                    || (stringActualValues.isUseList() && !stringParameterValues.getTextList().equals(stringActualValues.getTextList()))
                                    || (!stringActualValues.equals(stringParameterValues))
                                    || (!stringActualValues.getText().equals(stringParameterValues.getText()))) {
                                return false;
                            }
                            break;
                        // treat values as floats
                        case FloatAttributeDescription.ATTRIBUTE_NAME:
                            FloatCriteriaValues floatParameterValues = (FloatCriteriaValues) values;
                            FloatCriteriaValues floatActualValues = (FloatCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (floatParameterValues.getFloatValuePrimary() != floatActualValues.getFloatValuePrimary()
                                    || floatParameterValues.getFloatValueSecondary() != floatActualValues.getFloatValueSecondary()) {
                                return false;
                            }
                            break;
                        // treat values as booleans
                        case BooleanAttributeDescription.ATTRIBUTE_NAME:
                            BooleanCriteriaValues boolParameterValues = (BooleanCriteriaValues) values;
                            BooleanCriteriaValues boolActualValues = (BooleanCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (boolParameterValues.getBoolValue() != boolActualValues.getBoolValue()) {
                                return false;
                            }
                            break;
                        // treat values as colors
                        case ColorAttributeDescription.ATTRIBUTE_NAME:
                            ColorCriteriaValues colorParameterValues = (ColorCriteriaValues) values;
                            ColorCriteriaValues colorActualValues = (ColorCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!colorParameterValues.getColorValue().equals(colorActualValues.getColorValue())) {
                                return false;
                            }
                            break;
                        // treat values as dateTimes
                        case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME:
                            DateTimeCriteriaValues dateTimeParameterValues = (DateTimeCriteriaValues) values;
                            DateTimeCriteriaValues dateTimeActualValues = (DateTimeCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!dateTimeParameterValues.getDateTimeStringPrimaryValue().equals(dateTimeActualValues.getDateTimeStringPrimaryValue())
                                    || !dateTimeParameterValues.getDateTimeStringSecondaryValue().equals(dateTimeActualValues.getDateTimeStringSecondaryValue())) {
                                return false;
                            }
                            break;
                        // treat values as icons
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
        /**
         * matches will == 4 if all criteria of object a is the same as object
         * b. It will return false if there is any criteria that doesn't match
         */
        return matches == 4;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.criteriaValuesList);
        hash = 67 * hash + Objects.hashCode(this.graphElementType);
        hash = 67 * hash + Objects.hashCode(this.allOrAny);
        hash = 67 * hash + Objects.hashCode(this.currentSelection);
        hash = 67 * hash + (this.searchAllGraphs ? 1 : 0);
        return hash;
    }

}

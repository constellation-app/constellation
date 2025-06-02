/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.components.advanced.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.StringCriteriaValues;
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
    private String postSearchAction;
    private String searchInLocation;

    /**
     * Default constructor sets all variables to their default values
     */
    public AdvancedSearchParameters() {
        this.criteriaValuesList = new ArrayList<>();
        this.graphElementType = GraphElementType.VERTEX;
        this.allOrAny = "All";
        this.postSearchAction = "Replace Selection";
        this.searchInLocation = "Current Selection";
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
    public AdvancedSearchParameters(final List<FindCriteriaValues> criteriaValuesList, final GraphElementType graphElementType, final String allOrAny, final String postSearchAction, final String searchInLocation) {
        this.criteriaValuesList = criteriaValuesList;
        this.graphElementType = graphElementType;
        this.allOrAny = allOrAny;
        this.postSearchAction = postSearchAction;
        this.searchInLocation = searchInLocation;
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
        this.postSearchAction = parameters.postSearchAction;
        this.searchInLocation = parameters.searchInLocation;
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
    public String getPostSearchAction() {
        return postSearchAction;
    }

    /**
     * Gets the searchInLocation string
     *
     * @return
     */
    public String getSearchInLocation() {
        return searchInLocation;
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

        if (searchInLocation.equals(parameters.getSearchInLocation())) {
            matches++;
        }
        if (graphElementType.equals(parameters.getGraphElementType())) {
            matches++;
        }
        if (allOrAny.equals(parameters.getAllOrAny())) {
            matches++;
        }
        if (postSearchAction.equals(parameters.getPostSearchAction())) {
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
                        case StringAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as strings
                            StringCriteriaValues stringParameterValues = (StringCriteriaValues) values;
                            StringCriteriaValues stringActualValues = (StringCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if ((!stringParameterValues.equals(stringActualValues))
                                    || (stringParameterValues.isIgnoreCase() != stringActualValues.isIgnoreCase())
                                    || (stringParameterValues.isUseList() != stringActualValues.isUseList())
                                    || (!stringActualValues.isUseList() && !stringParameterValues.getText().equals(stringActualValues.getText()))
                                    || (stringActualValues.isUseList() && !stringParameterValues.getTextList().equals(stringActualValues.getTextList()))) {
                                return false;
                            }
                        }
                        case FloatAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as floats
                            FloatCriteriaValues floatParameterValues = (FloatCriteriaValues) values;
                            FloatCriteriaValues floatActualValues = (FloatCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (floatParameterValues.getFloatValuePrimary() != floatActualValues.getFloatValuePrimary()
                                    || floatParameterValues.getFloatValueSecondary() != floatActualValues.getFloatValueSecondary()) {
                                return false;
                            }
                        }
                        case BooleanAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as booleans
                            BooleanCriteriaValues boolParameterValues = (BooleanCriteriaValues) values;
                            BooleanCriteriaValues boolActualValues = (BooleanCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (boolParameterValues.getBoolValue() != boolActualValues.getBoolValue()) {
                                return false;
                            }
                        }
                        case ColorAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as colors
                            ColorCriteriaValues colorParameterValues = (ColorCriteriaValues) values;
                            ColorCriteriaValues colorActualValues = (ColorCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!colorParameterValues.getColorValue().equals(colorActualValues.getColorValue())) {
                                return false;
                            }
                        }
                        case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as dateTimes
                            DateTimeCriteriaValues dateTimeParameterValues = (DateTimeCriteriaValues) values;
                            DateTimeCriteriaValues dateTimeActualValues = (DateTimeCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!dateTimeParameterValues.getDateTimeStringPrimaryValue().equals(dateTimeActualValues.getDateTimeStringPrimaryValue())
                                    || !dateTimeParameterValues.getDateTimeStringSecondaryValue().equals(dateTimeActualValues.getDateTimeStringSecondaryValue())) {
                                return false;
                            }
                        }
                        case IconAttributeDescription.ATTRIBUTE_NAME -> {
                            // treat values as icons
                            IconCriteriaValues iconParameterValues = (IconCriteriaValues) values;
                            IconCriteriaValues iconActualValues = (IconCriteriaValues) parameters.getCriteriaValuesList().get(i);
                            if (!iconParameterValues.getIconValue().equals(iconActualValues.getIconValue())) {
                                return false;
                            }
                        }
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
        hash = 67 * hash + Objects.hashCode(this.postSearchAction);
        hash = 67 * hash + Objects.hashCode(this.searchInLocation);
        return hash;
    }

}

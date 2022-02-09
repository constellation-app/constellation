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
package au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues;

/**
 * This Class is for the DateTimeCriteriaValues which are created from a
 * DateTimeCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class DateTimeCriteriaValues extends FindCriteriaValues {

    private final String dateTimeStringPrimary;
    private final String dateTimeStringSecondary;

    //Main Constructor only containing one dateTimeValue
    public DateTimeCriteriaValues(final String attributeType, final String attribute, final String filter, final String dateTimeStringPrimary) {
        super(attributeType, attribute, filter);
        this.dateTimeStringPrimary = dateTimeStringPrimary;
        this.dateTimeStringSecondary = "";
    }

    //Second Contructor that allows an additional parameter for another DateTimeValue
    //This is used for "In Between" searches
    public DateTimeCriteriaValues(final String attributeType, final String attribute, final String filter, final String dateTimeStringPrimary, final String dateTimeStringSecondary) {
        super(attributeType, attribute, filter);
        this.dateTimeStringPrimary = dateTimeStringPrimary;
        this.dateTimeStringSecondary = dateTimeStringSecondary;
    }

    /**
     * Gets the Primary DateTimeStrinValue
     *
     * @return
     */
    public String getDateTimeStringPrimaryValue() {
        return dateTimeStringPrimary;
    }

    /**
     * Gets the Secondary DateTimeStrinValue
     *
     * @return
     */
    public String getDateTimeStringSecondaryValue() {
        return dateTimeStringSecondary;
    }

}

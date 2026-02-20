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
package au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues;

/**
 * This Class is for the BooleanCriteriaValues which are created from a
 * BooleanCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class BooleanCriteriaValues extends FindCriteriaValues {

    private final boolean boolValue;

    public BooleanCriteriaValues(final String attributeType, final String attribute, final String filter, final boolean boolValue) {
        super(attributeType, attribute, filter);
        this.boolValue = boolValue;
    }

    /**
     * Gets the current boolean value of this booleanCriteriaValues Object
     *
     * @return boolValue
     */
    public boolean getBoolValue() {
        return boolValue;
    }

}

/*
 * Copyright 2010-2024 Australian Signals Directorate
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
 * This Class is for the FloatCriteriaValues which are created from a
 * FloatCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class FloatCriteriaValues extends FindCriteriaValues {

    private final float floatValuePrimary;
    private final float floatValueSecondary;

    // first constructor takes one float value
    public FloatCriteriaValues(final String attributeType, final String attribute, final String filter, final float floatValuePrimary) {
        super(attributeType, attribute, filter);
        this.floatValuePrimary = floatValuePrimary;
        this.floatValueSecondary = 0;
    }

    // second constructor takes two float values
    public FloatCriteriaValues(final String attributeType, final String attribute, final String filter, final float floatValuePrimary, final float floatValueSecondary) {
        super(attributeType, attribute, filter);
        this.floatValuePrimary = floatValuePrimary;
        this.floatValueSecondary = floatValueSecondary;
    }

    /**
     * Gets the floatValuePrimary
     *
     * @return
     */
    public float getFloatValuePrimary() {
        return floatValuePrimary;
    }

    /**
     * Gets the floatValueSecondary
     *
     * @return
     */
    public float getFloatValueSecondary() {
        return floatValueSecondary;
    }

}

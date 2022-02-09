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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;

/**
 * This Class is for the ColourCriteriaValues which are created from a
 * ColourCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class ColourCriteriaValues extends FindCriteriaValues {

    private final ConstellationColor color;

    public ColourCriteriaValues(final String attributeType, final String attribute, final String filter, final ConstellationColor color) {
        super(attributeType, attribute, filter);
        this.color = color;
    }

    /**
     * Gets the current color value of this ColourCriteriaValues Object
     *
     * @return color
     */
    public ConstellationColor getColorValue() {
        return color;
    }

}

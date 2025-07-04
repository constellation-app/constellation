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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Objects;

/**
 * This Class is for the ColorCriteriaValues which are created from a
 ColorCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class ColorCriteriaValues extends FindCriteriaValues {

    private final ConstellationColor color;

    public ColorCriteriaValues(final String attributeType, final String attribute, final String filter, final ConstellationColor color) {
        super(attributeType, attribute, filter);
        this.color = color;
    }

    /**
     * Gets the current color value of this ColorCriteriaValues Object
     *
     * @return color
     */
    public ConstellationColor getColorValue() {
        return color;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.color);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ColorCriteriaValues other = (ColorCriteriaValues) obj;
        return Objects.equals(this.color, other.color);
    }
}

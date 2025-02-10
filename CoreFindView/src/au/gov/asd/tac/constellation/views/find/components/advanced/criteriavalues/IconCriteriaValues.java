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

import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;

/**
 * This Class is for the IconCriteriaValues which are created from a
 * IconCriteriaPanel
 *
 * @author Atlas139mkm
 */
public class IconCriteriaValues extends FindCriteriaValues {

    private final ConstellationIcon icon;

    public IconCriteriaValues(final String attributeType, final String attribute, final String filter, final ConstellationIcon icon) {
        super(attributeType, attribute, filter);
        this.icon = icon;
    }

    /**
     * Gets the IconCriteriaValues icon
     *
     * @return
     */
    public ConstellationIcon getIconValue() {
        return icon;
    }

}

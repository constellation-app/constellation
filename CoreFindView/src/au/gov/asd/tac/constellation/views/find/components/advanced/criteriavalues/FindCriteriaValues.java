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
package au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues;

import java.util.Objects;

/**
 * This Class is the parent class for all the criteriaValueObjects
 *
 * @author Atlas139mkm
 */
public class FindCriteriaValues {

    private String attributeType;
    private String attribute;
    private String filter;

    public FindCriteriaValues(final String attributeType, final String attribute, final String filter) {
        this.attributeType = attributeType;
        this.attribute = attribute;
        this.filter = filter;
    }

    /**
     * Gets the type of attribute the criteriaValue is. Eg "String"
     *
     * @return
     */
    public String getAttributeType() {
        return attributeType;
    }

    /**
     * Gets the attribute of the criteriaValue Eg "Identifier"
     *
     * @return
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Gets the filter of the criteriaValue. Eg "Starts With" for string
     *
     * @return
     */
    public String getFilter() {
        return filter;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.attributeType);
        hash = 71 * hash + Objects.hashCode(this.attribute);
        hash = 71 * hash + Objects.hashCode(this.filter);
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

        final FindCriteriaValues other = (FindCriteriaValues) obj;
        if (!Objects.equals(this.attributeType, other.attributeType) || !Objects.equals(this.attribute, other.attribute)) {
            return false; 
        }
        
        return Objects.equals(this.filter, other.filter);
    }
}

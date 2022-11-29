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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 * Information about an attribute as separate form its existence on a particular
 * graph.
 *
 * @author twilight_sparkle
 */
public class AttributePrototype {

    public static final AttributePrototype getBlankPrototype(final GraphElementType elementType) {
        return new AttributePrototype("", "", elementType, "string", null);
    }

    private final GraphElementType elementType;
    private final String attributeName;
    private final String attributeDescription;
    private final String dataType;
    private final Object defaultValue;

    public AttributePrototype(final String name, final String description, final GraphElementType elementType, final String dataType, final Object defaultValue) {
        this.attributeName = name;
        this.attributeDescription = description;
        this.elementType = elementType;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @return the attributeDescription
     */
    public String getAttributeDescription() {
        return attributeDescription;
    }

    /**
     * @return the type
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @return the defaultValue
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
}

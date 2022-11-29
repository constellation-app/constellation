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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;

/**
 * A NewAttribute specifies an attribute that does not exist on the destination
 * graph but should be created in the process or importing the data.
 *
 * @author sirius
 */
public class NewAttribute implements Attribute {

    private final GraphElementType elementType;
    private final String attributeType;
    private final String name;
    private final String description;
    private int id = ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN;

    private static final String CANNOT_SET_VALUES = "You cannot set values on a NewAttribute";

    public NewAttribute(final Attribute original) {
        this(original.getElementType(), original.getAttributeType(), original.getName(), original.getDescription());
    }

    public NewAttribute(final GraphElementType elementType, final String type,
            final String label, final String description) {
        this.elementType = elementType;
        this.attributeType = type;
        this.name = label;
        this.description = description;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public GraphElementType getElementType() {
        return elementType;
    }

    @Override
    public void setElementType(final GraphElementType elementType) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public String getAttributeType() {
        return attributeType;
    }

    @Override
    public void setAttributeType(final String attributeType) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public void setDefaultValue(final Object value) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public Class<? extends AttributeDescription> getDataType() {
        return null;
    }

    @Override
    public void setDataType(final Class<? extends AttributeDescription> dataType) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }

    @Override
    public GraphAttributeMerger getAttributeMerger() {
        return null;
    }

    @Override
    public void setAttributeMerger(final GraphAttributeMerger attributeMerger) {
        throw new UnsupportedOperationException(CANNOT_SET_VALUES);
    }
}

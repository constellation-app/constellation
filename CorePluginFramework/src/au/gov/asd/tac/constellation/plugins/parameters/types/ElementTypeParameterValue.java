/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.Objects;

/**
 * Element Type Parameter Value
 *
 * @author cygnus_x-1
 */
public class ElementTypeParameterValue extends ParameterValue {

    private GraphElementType elementType;

    public ElementTypeParameterValue() {
        this.elementType = null;
    }

    public ElementTypeParameterValue(final GraphElementType elementType) {
        this.elementType = elementType;
    }

    public GraphElementType getGraphElementType() {
        return elementType;
    }

    @Override
    public String validateString(final String s) {
        try {
            GraphElementType.getValue(s);
        } catch (final IllegalArgumentException ex) {
            return String.format("%s is not a valid element type", s);
        }
        return null;
    }

    @Override
    public boolean setStringValue(final String s) {
        final GraphElementType stringElementType = GraphElementType.getValue(s);
        final boolean equal = Objects.equals(stringElementType, elementType);
        if (!equal) {
            elementType = stringElementType;
        }
        return !equal;
    }

    @Override
    public Object getObjectValue() {
        return elementType;
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o instanceof GraphElementType) {
            final GraphElementType type = (GraphElementType) o;
            final boolean equal = Objects.equals(type, elementType);
            if (!equal) {
                elementType = type;
                return true;
            }
        }
        return false;
    }

    @Override
    protected ParameterValue createCopy() {
        return new ElementTypeParameterValue(elementType);
    }

    @Override
    public String toString() {
        return elementType == null ? "No Value" : elementType.getShortLabel();
    }

    @Override
    public int hashCode() {
        return 41 * 5 + Objects.hashCode(this.elementType);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ElementTypeParameterValue other = (ElementTypeParameterValue) obj;
        return this.elementType == other.elementType;
    }

}

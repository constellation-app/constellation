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

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.Objects;

/**
 * Graph Attribute Parameter Value
 *
 * @author cygnus_x-1
 */
public class GraphAttributeParameterValue extends ParameterValue implements Comparable<GraphAttributeParameterValue> {

    private GraphAttribute attribute;

    public GraphAttributeParameterValue() {
        this.attribute = null;
    }

    public GraphAttributeParameterValue(final GraphAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public String validateString(final String s) {
        throw new UnsupportedOperationException("Cannot set GraphAttributeParameterValue using a String");
    }

    @Override
    public boolean setStringValue(final String s) {
        throw new UnsupportedOperationException("Cannot set GraphAttributeParameterValue using a String");
    }

    @Override
    public Object getObjectValue() {
        return attribute;
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o instanceof GraphAttribute) {
            final GraphAttribute objectAttribute = (GraphAttribute) o;
            final boolean equal = Objects.equals(objectAttribute, attribute);
            if (!equal) {
                attribute = objectAttribute;
            }
            return !equal;
        }
        return false;
    }

    @Override
    protected ParameterValue createCopy() {
        return new GraphAttributeParameterValue(attribute);
    }

    @Override
    public String toString() {
        return attribute == null ? "No Value" : String.format("%s (%s)", attribute.getName(), attribute.getElementType().getShortLabel());
    }

    @Override
    public int compareTo(GraphAttributeParameterValue o) {
        final GraphAttribute other = (GraphAttribute) o.getObjectValue();

        // transaction attributes before vertex attributes
        if (attribute.getElementType() == GraphElementType.TRANSACTION && other.getElementType() == GraphElementType.VERTEX) {
            return -1;
        } else if (attribute.getElementType() == GraphElementType.VERTEX && other.getElementType() == GraphElementType.TRANSACTION) {
            return 1;
        } else {
            // uppercase attribute names before lowercase attribute names
            if (attribute.getName().matches("[A-Z]{1}.*") && other.getName().matches("[a-z]{1}.*")) {
                return -1;
            } else if (attribute.getName().matches("[a-z]{1}.*") && other.getName().matches("[A-Z]{1}.*")) {
                return 1;
            } else {
                // alphabetical
                return attribute.getName().compareTo(other.getName());
            }
        }
    }
}

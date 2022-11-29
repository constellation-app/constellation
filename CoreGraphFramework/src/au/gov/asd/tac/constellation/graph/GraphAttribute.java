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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import java.io.Serializable;
import java.util.Objects;

/**
 * An GraphAttribute is the implementation of that Attribute interface that can
 * be used by a client to gather all information about an attribute into an
 * object for later use.
 *
 * @author sirius
 */
public class GraphAttribute implements Attribute, Serializable {

    private int id;
    private GraphElementType elementType;
    private String attributeType;
    private String name;
    private String description;
    private Object defaultValue;
    private Class<? extends AttributeDescription> dataType;
    private GraphAttributeMerger attributeMerger;

    public GraphAttribute(final int id, final GraphElementType elementType, final String attributeType,
            final String name, final String description, final Object defaultValue,
            final Class<? extends AttributeDescription> dataType) {
        this(id, elementType, attributeType, name, description, defaultValue, dataType, null);
    }

    public GraphAttribute(final int id, final GraphElementType elementType, final String attributeType,
            final String name, final String description, final Object defaultValue,
            final Class<? extends AttributeDescription> dataType, final GraphAttributeMerger attributeMerger) {
        this.id = id;
        this.elementType = elementType;
        this.attributeType = attributeType;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.attributeMerger = attributeMerger;
    }

    /**
     * This constructor provides a convenient way for providers of the
     * DefinedGraphAttributes service to create their defined attributes. The
     * resulting attributes are not used by a Graph, they are useful merely for
     * providing attribute definitions.
     *
     * @param elementType the attribute element type.
     * @param attributeType the attribute type.
     * @param name the attribute label.
     * @param description the attribute description.
     */
    public GraphAttribute(final GraphElementType elementType, final String attributeType,
            final String name, final String description) {
        this.id = Graph.NOT_FOUND;
        this.elementType = elementType;
        this.attributeType = attributeType;
        this.name = name;
        this.description = description;
        this.defaultValue = null;
        this.dataType = null;
        this.attributeMerger = null;
    }

    public GraphAttribute(final GraphAttribute original) {
        this.id = original.id;
        this.elementType = original.elementType;
        this.attributeType = original.attributeType;
        this.name = original.name;
        this.description = original.description;
        this.defaultValue = original.defaultValue;
        this.dataType = original.dataType;
        this.attributeMerger = original.attributeMerger;
    }

    /**
     * Create an attribute definition using the attribute id
     *
     * Note that the attribute id required is the attribute created from
     * <pre>
     * GraphWriteMethods.addAttribute()
     * </pre> and not from
     * <pre>
     * GraphReadMethos.getAttribute()
     * </pre> because we want the attribute id and not the position
     *
     * @param graph
     * @param attribute The attribute id
     */
    public GraphAttribute(final GraphReadMethods graph, final int attribute) {
        this.id = attribute;
        this.elementType = graph.getAttributeElementType(attribute);
        this.attributeType = graph.getAttributeType(attribute);
        this.name = graph.getAttributeName(attribute);
        this.description = graph.getAttributeDescription(attribute);
        this.defaultValue = graph.getAttributeDefaultValue(attribute);
        this.dataType = graph.getAttributeDataType(attribute);
        this.attributeMerger = graph.getAttributeMerger(attribute);
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
        this.elementType = elementType;
    }

    @Override
    public String getAttributeType() {
        return attributeType;
    }

    @Override
    public void setAttributeType(final String attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(final Object defaultvalue) {
        this.defaultValue = defaultvalue;
    }

    @Override
    public Class<? extends AttributeDescription> getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(final Class<? extends AttributeDescription> dataType) {
        this.dataType = dataType;
    }

    @Override
    public GraphAttributeMerger getAttributeMerger() {
        return attributeMerger;
    }

    @Override
    public void setAttributeMerger(final GraphAttributeMerger attributeMerger) {
        this.attributeMerger = attributeMerger;
    }

    @Override
    public String toString() {
        return String.format("ImmutableAttribute[id=%d;type=%s;label=%s;description=%s;elementType=%s;defaultValue=%s]", id, attributeType, name, description, elementType, defaultValue);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.attributeType);
        hash = 59 * hash + this.id;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.description);
        hash = 59 * hash + Objects.hashCode(this.elementType);
        hash = 59 * hash + Objects.hashCode(this.dataType);
        hash = 59 * hash + Objects.hashCode(this.defaultValue);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphAttribute other = (GraphAttribute) obj;
        if (!Objects.equals(this.attributeType, other.attributeType)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.elementType != other.elementType) {
            return false;
        }
        if (!Objects.equals(this.dataType, other.dataType)) {
            return false;
        }
        return Objects.equals(this.defaultValue, other.defaultValue);
    }
}

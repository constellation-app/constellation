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
package au.gov.asd.tac.constellation.graph.schema.attribute;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import java.util.Objects;

/**
 * A SchemaAttribute is a class for defining graph attributes which belong with
 * one or more {@link Schema} in a convenient manner. This class is built to
 * work seamlessly with the graph framework to build the attribute it describes.
 *
 * @author cygnus_x-1
 */
public class SchemaAttribute implements Comparable<SchemaAttribute> {

    private final GraphElementType elementType;
    private final String attributeType;
    private final String name;
    private final String description;
    private final Object defaultValue;
    private final String attributeMergerId;
    private final String regex;
    private final String format;
    private final GraphIndexType indexType;
    private final boolean isMultiValue;
    private final boolean isLabel;
    private final boolean isDecorator;
    private final boolean create;

    private SchemaAttribute(final GraphElementType elementType, final String attributeType, final String name, 
            final String description, final Object defaultvalue, final String attributeMergerId, final String regex, 
            final String format, final GraphIndexType indexType, final boolean isMultiValue, final boolean isLabel,
            final boolean isDecorator, final boolean create) {
        this.elementType = elementType;
        this.attributeType = attributeType;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultvalue;
        this.attributeMergerId = attributeMergerId;
        this.regex = regex;
        this.format = format;
        this.indexType = indexType;
        this.isMultiValue = isMultiValue;
        this.isLabel = isLabel;
        this.isDecorator = isDecorator;
        this.create = create;
    }

    /**
     * Get the {@link GraphElementType} of this SchemaAttribute.
     *
     * @return a {@link GraphElementType} representing the type of graph element
     * this {@link SchemaAttribute} corresponds to.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * A {@link String} representing the type of this SchemaAttribute. This
     * value should match the return value of
     * {@link AttributeDescription#getName} for the relevant
     * {@link AttributeDescription} implementation.
     *
     * @return a {@link String} representing the type of this SchemaAttribute.
     */
    public String getAttributeType() {
        return attributeType;
    }

    /**
     * The name of this SchemaAttribute.
     *
     * @return a {@link String} representing the name of this SchemaAttribute.
     */
    public String getName() {
        return name;
    }

    /**
     * A description of this SchemaAttribute.
     *
     * @return a {@link String} representing a description for this
     * SchemaAttribute.
     */
    public String getDescription() {
        return description;
    }

    /**
     * An {@link Object} which holds the default value for this SchemaAttribute.
     *
     * @return an {@link Object} representing a the default value of this
     * SchemaAttribute.
     */
    public Object getDefault() {
        return defaultValue;
    }

    /**
     * Returns that (@link AttributeMerger} for this schema attribute.
     *
     * @return that (@link AttributeMerger} for this schema attribute.
     */
    public String getAttributeMergerId() {
        return attributeMergerId;
    }

    /**
     * Get a regular expression for validating a value assigned to this
     * SchemaAttribute.
     *
     * @return A {@link String} representing a regular expression.
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the format in which to display a value of this SchemaAttribute. This
     * can be used to provide a custom format for displaying the attribute value
     * as a {@link String}.
     *
     * @return A {@link String} representing the displayable format of this
     * SchemaAttribute.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Get the {@link GraphIndexType} of this SchemaAttribute. This specifies
     * how the attribute is indexed in terms of storage in memory (and
     * consequently how efficiently values of this attribute can be looked up).
     *
     * @return a {@link GraphIndexType} representing how this SchemaAttribute is
     * indexed.
     */
    public GraphIndexType getIndexType() {
        return indexType;
    }

    /**
     * Get a boolean value representing whether this SchemaAttribute supports
     * storing multiple values or not.
     *
     * @return A boolean value representing whether this SchemaAttribute
     * supports storing multiple values.
     */
    public boolean isMultiValue() {
        return isMultiValue;
    }

    /**
     * Get a boolean value representing whether this SchemaAttribute should be
     * used as a label.
     *
     * @return A boolean value representing whether this SchemaAttribute should
     * be used as a label.
     */
    public boolean isLabel() {
        return isLabel;
    }

    /**
     * Get a boolean value representing whether this SchemaAttribute should be
     * used as a decorator. Obviously, this will only affect vertex attributes.
     *
     * @return A boolean value representing whether this SchemaAttribute should
     * be used as a decorator.
     */
    public boolean isDecorator() {
        return isDecorator;
    }

    /**
     * Get a boolean value representing whether this SchemaAttribute is created
     * by default for a {@link Schema} which has registered this
     * SchemaAttribute, or whether it is simply made available for creation.
     *
     * @return A boolean value representing whether this SchemaAttribute is
     * created by default.
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * A convenient method of retrieving this SchemaAttribute from a
     * {@link Graph}.
     *
     * @param graph The {@link GraphReadMethods} from which to retrieve this
     * SchemaAttribute.
     * @return An int value representing the id of this SchemaAttribute.
     */
    public int get(final GraphReadMethods graph) {
        return graph.getAttribute(elementType, name);
    }

    /**
     * A convenient method for creating this SchemaAttribute on a {@link Graph}.
     * <p>
     * If the attribute already exists on the given graph, then the id of that
     * attribute will simply be returned.
     * <p>
     * If the attribute does not exist on the given graph, and the graph has no
     * schema, then the attribute will be added to the graph and the id of that
     * new attribute will be returned.
     * <p>
     * If the attribute does not exist on the given graph, but is registered
     * with the {@link Schema} of the graph, then the Schema will be tasked with
     * creating the attribute and the id of that new attribute will be returned.
     * <p>
     * If the attribute does not exist on the given graph, and is not registered
     * with the {@link Schema} of the graph, then the attribute will be created
     * and returned.
     *
     * @param graph The {@link GraphWriteMethods} on which to create this
     * SchemaAttribute.
     * @return An int value representing the id of this SchemaAttribute.
     */
    public int ensure(final GraphWriteMethods graph) {
        return ensure(graph, false);
    }

    /**
     * A convenient method for creating this SchemaAttribute on a {@link Graph}.
     * <p>
     * If the attribute already exists on the given graph, then the id of that
     * attribute will simply be returned.
     * <p>
     * If the attribute does not exist on the given graph, and the graph has no
     * schema, then the attribute will be added to the graph and the id of that
     * new attribute will be returned.
     * <p>
     * If the attribute does not exist on the given graph, but is registered
     * with the {@link Schema} of the graph, then the Schema will be tasked with
     * creating the attribute and the id of that new attribute will be returned.
     * <p>
     * If the attribute does not exist on the given graph, and is not registered
     * with the {@link Schema} of the graph, then Graph.NOT_FOUND will be
     * returned.
     *
     * @param graph The {@link GraphWriteMethods} on which to create this
     * SchemaAttribute.
     * @param boundBySchema if true, and this {@link SchemaAttribute} is not
     * registered to the {@link Schema} of the current graph, then this method
     * will return {@link GraphConstants#NOT_FOUND}, otherwise the attribute
     * will be created regardless
     *
     * @return An int value representing the id of this SchemaAttribute.
     */
    public int ensure(final GraphWriteMethods graph, final boolean boundBySchema) {
        if (graph.getSchema() == null) {
            int attribute = get(graph);
            if (!boundBySchema && attribute == Graph.NOT_FOUND) {
                attribute = graph.addAttribute(getElementType(), getAttributeType(), getName(), getDescription(), 
                        getDefault(), getAttributeMergerId());
                graph.setAttributeIndexType(attribute, getIndexType());
            }
            return attribute;
        }
        return graph.getSchema().getFactory().ensureAttribute(graph, this, boundBySchema);
    }

    /**
     * Validate the given value against the regular expression of this
     * SchemaAttribute. Returns true if the value is valid or if there is no
     * regular expression specified, otherwise returns false indicating the
     * given value failed validation.
     *
     * @param value The value to validate against this SchemaAttribute.
     * @return true if the specified value was successfully validated.
     */
    public boolean validate(final String value) {
        return regex == null || value.matches(regex);
    }

    /**
     * Compare this SchemaAttribute to an {@link Attribute} object to determine
     * if they are equivalent.
     *
     * @param other the {@link Attribute} object to test for equivalence.
     * @return
     */
    public boolean equivalent(final Attribute other) {
        return other != null 
                && elementType.equals(other.getElementType())
                && attributeType.equals(other.getAttributeType())
                && Objects.equals(name, other.getName())
                && Objects.equals(description, other.getDescription())
                && Objects.equals(defaultValue, other.getDefaultValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (elementType == null ? 0 : elementType.hashCode());
        result = prime * result + (attributeType == null ? 0 : attributeType.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (regex == null ? 0 : regex.hashCode());
        result = prime * result + (format == null ? 0 : format.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final SchemaAttribute other = (SchemaAttribute) o;
        return elementType.equals(other.elementType)
                && attributeType.equals(other.attributeType)
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(defaultValue, other.defaultValue)
                && Objects.equals(regex, other.regex)
                && Objects.equals(format, other.format);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final SchemaAttribute o) {
        return name.compareTo(o.name);
    }

    /**
     * A Builder class for creating a new SchemaAttribute.
     */
    public static class Builder {

        private final GraphElementType elementType;
        private final String attributeType;
        private final String name;
        private String description = null;
        private Object defaultValue = null;
        private String attributeMergerId;
        private String regex = null;
        private String format = null;
        private GraphIndexType indexType = GraphIndexType.NONE;
        private boolean multiValue = false;
        private boolean isLabel = false;
        private boolean isDecorator = false;
        private boolean create = false;

        /**
         * Instantiate a new SchemaAttribute builder with the specified
         * elementType, attributeType, and name. This is the minimal information
         * required to create a new attribute.
         *
         * @param elementType The {@link GraphElementType} the attribute belongs
         * to.
         * @param attributeType The type of data the attribute holds, this
         * should correspond to an {@link AttributeDescription}.
         * @param name The name of this attribute.
         */
        public Builder(final GraphElementType elementType, final String attributeType, final String name) {
            this.elementType = elementType;
            this.attributeType = attributeType;
            this.name = name;
        }

        /**
         * Specify a description explaining what this SchemaAttribute is.
         *
         * @param description The description of this SchemaAttribute
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setDescription(final String description) {
            this.description = description;
            return this;
        }

        /**
         * Set a default value for this attribute. The default will be returned
         * whenever no value is set for this SchemaAttribute.
         *
         * @param defaultValue An {@link Object} holding the default value for
         * this SchemaAttribute.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setDefaultValue(final Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets the (@link AttributeMerger} for this attribute.
         *
         * @param attributeMergerId the (@link AttributeMerger} for this
         * attribute.
         * @return this
         */
        public Builder setAttributeMergerId(final String attributeMergerId) {
            this.attributeMergerId = attributeMergerId;
            return this;
        }

        /**
         * Specify a regular expression for which values for this
         * SchemaAttribute must match in order to be considered valid.
         *
         * @param regex A {@link String} representing a regular expression.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setRegex(final String regex) {
            this.regex = regex;
            return this;
        }

        /**
         * Specify a format for this SchemaAttribute. This is required for
         * certain types of attributes (as specified by that types
         * {@link AttributeDescription}).
         *
         * @param format a {@link String} representing a format.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setFormat(final String format) {
            this.format = format;
            return this;
        }

        /**
         * Set a {@link GraphIndexType} for this SchemaAttribute. This specifies
         * how the attribute is indexed in terms of storage in memory (and
         * consequently how efficiently values of this attribute can be looked
         * up).
         *
         * @param indexType a {@link GraphIndexType}.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setIndexType(final GraphIndexType indexType) {
            this.indexType = indexType;
            return this;
        }

        /**
         * Specify whether this SchemaAttribute can hold multiple values.
         *
         * @param multiValue A boolean value representing whether this
         * SchemaAttribute is capable of holding multiple values.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setMultiValue(final boolean multiValue) {
            this.multiValue = multiValue;
            return this;
        }

        /**
         * Specify whether this SchemaAttribute should be used as a label.
         *
         * @param isLabel A boolean value representing whether this
         * SchemaAttribute should be used as a label.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setLabel(final boolean isLabel) {
            this.isLabel = isLabel;
            return this;
        }

        /**
         * Specify whether this SchemaAttribute should be used as a decorator.
         * This property will only affect vertex attributes.
         *
         * @param isDecorator A boolean value representing whether this
         * SchemaAttribute should be used as a decorator.
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder setDecorator(final boolean isDecorator) {
            this.isDecorator = isDecorator;
            return this;
        }

        /**
         * Specify whether this SchemaAttribute will be automatically created
         * for any new graph using a {@link Schema} which registers this
         * SchemaAttribute. The creation of this SchemaAttribute will be done
         * via {@link #ensure}.
         *
         * @return The Builder object for this SchemaAttribute.
         */
        public Builder create() {
            create = true;
            return this;
        }

        /**
         * Finalise and create the SchemaAttribute defined by this Builder.
         *
         * @return A new SchemaAttribute based on the configuration of this
         * Builder object.
         */
        public SchemaAttribute build() {
            return new SchemaAttribute(elementType, attributeType, name, description, defaultValue,
                    attributeMergerId, regex, format, indexType, multiValue, isLabel, isDecorator, create);
        }
    }
}

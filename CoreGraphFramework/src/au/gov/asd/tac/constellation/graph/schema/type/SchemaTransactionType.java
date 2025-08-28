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
package au.gov.asd.tac.constellation.graph.schema.type;

import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An abstract class representing the type of a transaction. The type of a
 * transaction belongs to (and is aware of) the hierarchical ontology in which
 * it exists. This class provides functionality for traversing and inspecting a
 * type's immediate and wider ontology.
 *
 * @author cygnus_x-1
 */
public final class SchemaTransactionType extends SchemaElementType<SchemaTransactionType> implements Comparable<SchemaTransactionType> {

    private static final SchemaTransactionType UNKNOWN = new SchemaTransactionType(
            "Unknown",
            "A transaction representing a relationship which is not currently part of the analytic schema",
            ConstellationColor.CLOUDS,
            LineStyle.SOLID,
            true, null, null, null, false
    );

    public static SchemaTransactionType unknownType() {
        return UNKNOWN;
    }

    private final LineStyle style;
    private final Boolean directed;

    /**
     * Constructor for SchemaTransactionType. All properties of a
     * SchemaTransactionType (with the exception of the incomplete flag) can
     * only ever be set here, and can never be modified.
     *
     * @param name The name of this SchemaTransactionType.
     * @param description A description for this SchemaTransactionType.
     * @param color The color transactions of this SchemaTransactionType will
     * appear by default.
     * @param style The style transactions of this SchemaTransactionType will
     * inherit by default.
     * @param directed A flag indicating whether a transaction of this
     * SchemaTransactionType should be directed or not.
     * @param superType A SchemaTransactionType representing the super-type of
     * this SchemaTransactionType in the wider hierarchical ontology. If this is
     * set to null (ie. there is no super-type), then this SchemaTransactionType
     * will be assigned itself as its super-type.
     * @param overridenType A SchemaTransactionType which this
     * SchemaTransactionType will override in the wider hierarchical ontology.
     * This should be set to null if it doesn't apply.
     * @param properties A {@link Map} of {@link String} keys to {@link Object}
     * values which allows for storage of arbitrary properties of this
     * SchemaTransactionType.
     * @param incomplete A flag indicating that this SchemaTransactionType is
     * not final. If this is true, the {@link Schema} is given the opportunity
     * to modify this SchemaTransactionType or replace it altogether based on
     * other attributes of the transaction this SchemaTransactionType is
     * assigned to using {@link Schema#completeTransaction}.
     */
    public SchemaTransactionType(final String name, final String description,
            final ConstellationColor color, final LineStyle style, final Boolean directed,
            final SchemaTransactionType superType, final SchemaTransactionType overridenType,
            final Map<String, String> properties, final boolean incomplete) {
        super(name, description, color, superType, overridenType, properties, incomplete);
        this.style = style;
        this.directed = directed;
    }

    /**
     * Get the default line style for transactions of this
     * SchemaTransactionType.
     *
     * @return A {@link ConstellationLineStyle} representing the default style
     * to use for transactions of this SchemaTransactionType.
     */
    public final LineStyle getStyle() {
        return style;
    }

    /**
     * A flag indicating whether transactions of this SchemaTransactionType
     * should be rendered as directed or undirected transactions.
     *
     * @return True if transactions of this SchemaTransactionType should be
     * directed, otherwise false.
     */
    public final Boolean isDirected() {
        return directed;
    }

    /**
     * Build the hierarchy of this SchemaTransactionType.
     */
    public final void buildHierarchy() {
        if (getOverridenType() != null) {
            SchemaTransactionTypeUtilities.getTypes().forEach(type -> {
                if (type != null && type.getSuperType() == getOverridenType()) {
                    type.superType = this;
                }
            });
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.name);
        hash = 43 * hash + Objects.hashCode(this.description);
        hash = 43 * hash + Objects.hashCode(this.color);
        hash = 43 * hash + Objects.hashCode(this.style);
        hash = 43 * hash + Objects.hashCode(this.directed);
        hash = 43 * hash + (superType == this ? 0 : Objects.hashCode(this.superType));
        hash = 43 * hash + Objects.hashCode(this.properties);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SchemaTransactionType other = (SchemaTransactionType) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.color, other.color)
                && Objects.equals(this.style, other.style)
                && Objects.equals(this.directed, other.directed)
                && (this.superType == this ? other.superType == other : Objects.equals(this.superType, other.superType))
                && Objects.equals(this.properties, other.properties);
    }

    @Override
    public int compareTo(final SchemaTransactionType type) {
        return this.name.toLowerCase().compareTo(type.name.toLowerCase());
    }

    /**
     * Gets the default 'unknown' type belonging to the same ontology as this
     * type.
     *
     * @return the default 'unknown' type belonging to the same ontology as this
     * type.
     */
    @Override
    public SchemaTransactionType getUnknownType() {
        return SchemaTransactionType.unknownType();
    }

    @Override
    public SchemaTransactionType copy() {
        return new SchemaTransactionType.Builder(this, null)
                .build();
    }

    @Override
    public SchemaTransactionType rename(final String name) {
        return new SchemaTransactionType.Builder(this, name)
                .build();
    }

    /**
     * This class provides a methods to build a new SchemaTransactionType.
     */
    public static class Builder {

        protected final String name;
        protected String description;
        protected ConstellationColor color;
        protected LineStyle style;
        protected Boolean directed;
        protected SchemaTransactionType superType;
        protected SchemaTransactionType overridenType = null;
        protected Map<String, String> properties = new HashMap<>();
        protected boolean incomplete;

        public Builder(final String name) {
            this.name = name;
        }

        public Builder(final SchemaTransactionType type, final String name) {
            this(type, name, false);
        }

        public Builder(final SchemaTransactionType type, final String name, final boolean overrideType) {
            this.name = name != null ? name : type.getName();
            this.description = type.getDescription();
            this.color = type.getColor();
            this.style = type.getStyle();
            this.directed = type.isDirected();
            this.superType = ((SchemaTransactionType) type.getSuperType()).equals(type)
                    ? null : (SchemaTransactionType) type.getSuperType();
            if (overrideType) {
                this.overridenType = type;
            }
            this.properties = type.getProperties();
            this.incomplete = type.isIncomplete();
        }

        public Builder setDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder setColor(final ConstellationColor color) {
            this.color = color;
            return this;
        }

        public Builder setStyle(final LineStyle style) {
            this.style = style;
            return this;
        }

        public Builder setDirected(final Boolean directed) {
            this.directed = directed;
            return this;
        }

        public Builder setSuperType(final SchemaTransactionType superType) {
            this.superType = superType;
            return this;
        }

        public Builder setOverridenType(final SchemaTransactionType overridenType) {
            this.overridenType = overridenType;
            return this;
        }

        public Builder setProperty(final String propertyKey, final String propertyValue) {
            this.properties.put(propertyKey, propertyValue);
            return this;
        }

        public Builder setProperties(final Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder setIncomplete(final boolean incomplete) {
            this.incomplete = incomplete;
            return this;
        }

        public SchemaTransactionType build() {
            ConstellationColor newColor = color;
            LineStyle newStyle = style;
            Boolean newDirected = directed;

            if (color == null) {
                newColor = superType != null ? superType.color : UNKNOWN.color;
            }

            if (style == null) {
                newStyle = superType != null ? superType.style : UNKNOWN.style;
            }

            if (directed == null) {
                newDirected = superType != null ? superType.directed : UNKNOWN.directed;
            }

            return new SchemaTransactionType(
                    name,
                    description,
                    newColor,
                    newStyle,
                    newDirected,
                    superType,
                    overridenType,
                    properties,
                    incomplete);
        }
    }
}

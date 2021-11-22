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
package au.gov.asd.tac.constellation.graph.schema.type;

import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An abstract class representing the type of a vertex. The type of a vertex
 * belongs to (and is aware of) the hierarchical ontology in which it exists.
 * This class provides functionality for traversing and inspecting a type's
 * immediate and wider ontology.
 *
 * @author cygnus_x-1
 */
public final class SchemaVertexType extends SchemaElementType<SchemaVertexType> implements Comparable<SchemaVertexType> {

    private static final SchemaVertexType UNKNOWN = new SchemaVertexType(
            "Unknown",
            "A node representing a type which is not currently part of the analytic schema",
            ConstellationColor.GREY,
            DefaultIconProvider.UNKNOWN,
            DefaultIconProvider.FLAT_SQUARE,
            null, null, null, null, null, false
    );

    public static SchemaVertexType unknownType() {
        return UNKNOWN;
    }

    private final ConstellationIcon foregroundIcon;
    private final ConstellationIcon backgroundIcon;
    private final Pattern detectionRegex;
    private final Pattern validationRegex;

    /**
     * Constructor for SchemaVertexType. All properties of a SchemaVertexType
     * (with the exception of the incomplete flag) can only ever be set here,
     * and can never be modified.
     *
     * @param name The name of this SchemaVertexType.
     * @param description A description for this SchemaVertexType.
     * @param color The color vertices of this SchemaVertexType will appear by
     * default.
     * @param foregroundIcon The foreground icon vertices of this
     * SchemaVertexType will use by default.
     * @param backgroundIcon The background icon vertices of this
     * SchemaVertexType will use by default.
     * @param detectionRegex A regular expression which can be used to detect
     * vertices of this type.
     * @param validationRegex A regular expression which can be used to vertices
     * of this type are valid.
     * @param superType A SchemaVertexType representing the super-type of this
     * SchemaVertexType in the wider hierarchical ontology. If this is set to
     * null (ie. there is no super-type), then this SchemaVertexType will be
     * assigned itself as its super-type.
     * @param overridenType A SchemaVertexType which this SchemaVertexType will
     * override in the wider hierarchical ontology. This should be set to null
     * if it doesn't apply.
     * @param properties A {@link Map} of {@link String} keys to {@link String}
     * values which allows for storage of arbitrary properties of this
     * SchemaVertexType.
     * @param incomplete A flag indicating that this SchemaVertexType is not
     * final. If this is true, the {@link Schema} is given the opportunity to
     * modify this SchemaVertexType or replace it altogether based on other
     * attributes of the vertex this SchemaVertexType is assigned to using
     * {@link Schema#completeVertex}.
     */
    public SchemaVertexType(final String name, final String description,
            final ConstellationColor color, final ConstellationIcon foregroundIcon,
            final ConstellationIcon backgroundIcon, final Pattern detectionRegex,
            final Pattern validationRegex, final SchemaVertexType superType,
            final SchemaVertexType overridenType, final Map<String, String> properties,
            final boolean incomplete) {
        super(name, description, color, superType, overridenType, properties, incomplete);
        this.foregroundIcon = foregroundIcon;
        this.backgroundIcon = backgroundIcon;
        this.detectionRegex = detectionRegex;
        this.validationRegex = validationRegex;
    }

    /**
     * Get a {@link String} representing the name of the foreground icon that
     * will be used for this SchemaVertexType by default.
     *
     * @return A {@link String} representing the name of the foreground icon to
     * use for vertices of this SchemaVertexType.
     */
    public final ConstellationIcon getForegroundIcon() {
        return foregroundIcon;
    }

    /**
     * Get a {@link String} representing the name of the background icon that
     * will be used for this SchemaVertexType by default.
     *
     * @return A {@link String} representing the name of the background icon to
     * use for vertices of this SchemaVertexType.
     */
    public final ConstellationIcon getBackgroundIcon() {
        return backgroundIcon;
    }

    /**
     * Get a {@link Pattern} representing the regular expression that specifies
     * the detection pattern that data of this SchemaVertexType must match.
     *
     * @return A {@link Pattern} representing the detection regular expression.
     */
    public final Pattern getDetectionRegex() {
        return detectionRegex;
    }

    /**
     * Get a {@link Pattern} representing the regular expression that specifies
     * the validation pattern that data of this SchemaVertexType must match.
     *
     * @return A {@link Pattern} representing the validation regular expression.
     */
    public final Pattern getValidationRegex() {
        return validationRegex;
    }

    /**
     * Build the hierarchy of this SchemaTransactionType.
     */
    public final void buildHierarchy() {
        if (getOverridenType() != null) {
            SchemaVertexTypeUtilities.getTypes().forEach(type -> {
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
        hash = 43 * hash + Objects.hashCode(this.foregroundIcon);
        hash = 43 * hash + Objects.hashCode(this.backgroundIcon);
        hash = 43 * hash + Objects.hashCode(this.detectionRegex != null ? this.detectionRegex.pattern() : null);
        hash = 43 * hash + Objects.hashCode(this.validationRegex != null ? this.validationRegex.pattern() : null);
        hash = 43 * hash + (superType == this ? 0 : Objects.hashCode(this.superType));
        hash = 43 * hash + Objects.hashCode(this.properties);
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
        final SchemaVertexType other = (SchemaVertexType) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.color, other.color)
                && Objects.equals(this.foregroundIcon, other.foregroundIcon)
                && Objects.equals(this.backgroundIcon, other.backgroundIcon)
                && Objects.equals(this.detectionRegex != null ? this.detectionRegex.pattern() : null, other.detectionRegex != null ? other.detectionRegex.pattern() : null)
                && Objects.equals(this.validationRegex != null ? this.validationRegex.pattern() : null, other.validationRegex != null ? other.validationRegex.pattern() : null)
                && (this.superType == this ? other.superType == other : Objects.equals(this.superType, other.superType))
                && Objects.equals(this.properties, other.properties);
    }

    @Override
    public int compareTo(final SchemaVertexType type) {
        return this.name.toLowerCase().compareTo(type.name.toLowerCase());
    }

    @Override
    public SchemaVertexType getUnknownType() {
        return UNKNOWN;
    }

    @Override
    public SchemaVertexType copy() {
        return new SchemaVertexType.Builder(this, null).build();
    }

    @Override
    public SchemaVertexType rename(final String name) {
        return new SchemaVertexType.Builder(this, name).build();
    }

    public static class Builder {

        protected final String name;
        protected String description;
        protected ConstellationColor color;
        protected ConstellationIcon foregroundIcon;
        protected ConstellationIcon backgroundIcon;
        protected Pattern detectionRegex;
        protected Pattern validationRegex;
        protected SchemaVertexType superType;
        protected SchemaVertexType overridenType = null;
        protected Map<String, String> properties = new HashMap<>();
        protected boolean incomplete;

        public Builder(final String name) {
            this.name = name;
        }

        public Builder(final SchemaVertexType type, final String name) {
            this(type, name, false);
        }

        public Builder(final SchemaVertexType type, final String name, final boolean overrideType) {
            if (name != null) {
                this.name = name;
            } else {
                this.name = type.getName();
            }
            this.description = type.getDescription();
            this.color = type.getColor();
            this.foregroundIcon = type.getForegroundIcon();
            this.backgroundIcon = type.getBackgroundIcon();
            this.detectionRegex = type.getDetectionRegex();
            this.validationRegex = type.getValidationRegex();
            this.superType = type.getSuperType().equals(type)
                    ? null : (SchemaVertexType) type.getSuperType();
            if (overrideType) {
                this.overridenType = type;
            }
            this.properties = new HashMap<>(type.getProperties());
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

        public Builder setForegroundIcon(final ConstellationIcon foregroundIcon) {
            this.foregroundIcon = foregroundIcon;
            return this;
        }

        public Builder setBackgroundIcon(final ConstellationIcon backgroundIcon) {
            this.backgroundIcon = backgroundIcon;
            return this;
        }

        public Builder setDetectionRegex(final Pattern regex) {
            this.detectionRegex = regex;
            return this;
        }

        public Builder setValidationRegex(final Pattern regex) {
            this.validationRegex = regex;
            return this;
        }

        public Builder setSuperType(final SchemaVertexType superType) {
            this.superType = superType;
            return this;
        }

        public Builder setOverridenType(final SchemaVertexType overridenType) {
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

        /**
         * Build a vertex type.
         * <p>
         * For the color, foreground, background and validation regex properties
         * the value is set based on the following order: defined value -> super
         * types property value -> UNKNOWN types property value.
         *
         * @return
         */
        public SchemaVertexType build() {
            if (color == null) {
                color = superType != null ? superType.color : UNKNOWN.color;
            }

            if (foregroundIcon == null) {
                foregroundIcon = superType != null ? superType.foregroundIcon : UNKNOWN.getForegroundIcon();
            }

            if (backgroundIcon == null) {
                backgroundIcon = superType != null ? superType.backgroundIcon : UNKNOWN.getBackgroundIcon();
            }

            return new SchemaVertexType(
                    name,
                    description,
                    color,
                    foregroundIcon,
                    backgroundIcon,
                    detectionRegex,
                    validationRegex,
                    superType,
                    overridenType,
                    properties,
                    incomplete
            );
        }
    }
}

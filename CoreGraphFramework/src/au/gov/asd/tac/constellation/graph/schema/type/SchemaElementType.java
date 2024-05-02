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
package au.gov.asd.tac.constellation.graph.schema.type;

import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @param <T>
 *
 * @author cygnus_x-1
 */
public abstract class SchemaElementType<T extends SchemaElementType<?>> {

    public static final String HIERARCHY_SEPARATOR_CHARACTER = SeparatorConstants.PERIOD;

    protected final String name;
    protected final String description;
    protected final ConstellationColor color;
    protected SchemaElementType<T> superType;
    protected final SchemaElementType<T> overridenType;
    protected final Map<String, String> properties;
    protected boolean incomplete;
    protected final String hierarchy;

    /**
     * Constructor for SchemaElementType. All properties of a SchemaElementType
     * (with the exception of the incomplete flag) can only ever be set here,
     * and can never be modified.
     *
     * @param name The name of this SchemaElementType.
     * @param description A description for this SchemaElementType.
     * @param color The color elements of this SchemaElementType will appear by
     * default.
     * @param superType A SchemaElementType representing the super-type of this
     * SchemaElementType in the wider hierarchical ontology. If this is set to
     * null (ie. there is no super-type), then this SchemaElementType will be
     * assigned itself as its super-type.
     * @param overridenType A SchemaElementType which this SchemaElementType
     * will override in the wider hierarchical ontology. This should be set to
     * null if it doesn't apply.
     * @param properties A {@link Map} of {@link String} keys to {@link String}
     * values which allows for storage of arbitrary properties of this
     * SchemaElementType.
     * @param incomplete A flag indicating that this SchemaElementType is not
     * final. If this is true, the {@link Schema} is given the opportunity to
     * modify this SchemaElementType or replace it altogether based on other
     * attributes of the element this SchemaElementType is assigned to.
     */
    protected SchemaElementType(final String name, final String description, final ConstellationColor color,
            final SchemaElementType<T> superType, final SchemaElementType<T> overridenType,
            final Map<String, String> properties, final boolean incomplete) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.superType = superType == null ? this : superType;
        this.overridenType = overridenType;
        this.properties = properties == null ? Collections.emptyMap() : properties;
        this.incomplete = incomplete;
        this.hierarchy = getHierachy();
    }

    /**
     * Get the name of this SchemaElementType.
     *
     * @return A {@link String} representing the name of this SchemaElementType.
     */
    public final String getName() {
        return name;
    }

    /**
     * Get a description for this SchemaElementType..
     *
     * @return A {@link String} representing a description for this
     * SchemaElementType.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Get the default color for elements of this SchemaElementType.
     *
     * @return A {@link ConstellationColor} representing the default color of
     * elements of this SchemaElementType.
     */
    public final ConstellationColor getColor() {
        return color;
    }

    /**
     * Get the parent of this SchemaElementType in its hierarchy.
     *
     * @return A SchemaElementType representing the parent of this
     * SchemaElementType.
     */
    public final SchemaElementType<T> getSuperType() {
        return superType;
    }

    /**
     * Get the type overridden by this SchemaElementType.
     *
     * @return A SchemaElementType representing the type this SchemaElementType
     * overrides, or null if there isn't one.
     */
    public final SchemaElementType<T> getOverridenType() {
        return overridenType;
    }

    /**
     * A flag indicating whether this SchemaElementType is a top-level type in
     * its hierarchy or not.
     *
     * @return True if this SchemaElementType is a top-level type, false
     * otherwise.
     */
    public final boolean isTopLevelType() {
        return getTopLevelType() == this;
    }

    /**
     * Get the top-level type of this SchemaElementType in its hierarchy.
     *
     * @return A SchemaElementType representing the top-level type of this
     * SchemaElementType.
     */
    public final SchemaElementType<T> getTopLevelType() {
        return getSuperType() != this ? getSuperType().getTopLevelType() : this;
    }

    /**
     * Check if this SchemaElementType has the requested non-standard property
     * registered.
     *
     * @param propertyName A {@link String} representing the name of a
     * non-standard property.
     * @return True if the requested non-standard property exists, false
     * otherwise.
     */
    public final boolean hasProperty(final String propertyName) {
        return properties.containsKey(propertyName);
    }

    /**
     * Get the value assigned to the requested non-standard property of this
     * SchemaElementType.
     *
     * @param propertyName A {@link String} representing the name of a
     * non-standard property.
     * @return An {@link String} representing the value of the requested
     * non-standard property.
     */
    public final String getProperty(final String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Get all non-standard properties of this SchemaElementType.
     *
     * @return A {@link Map} of all non-standard properties of this
     * SchemaElementType where property names are mapped to their values for
     * this SchemaElementType.
     */
    public final Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Check if this SchemaElementType is a descendant of the specified
     * SchemaElementType in its hierarchy.
     *
     * @param type The SchemaElementType to check.
     * @return True is this SchemaElementType is a sub-type of the specified
     * SchemaElementType, false otherwise.
     */
    public final boolean isSubTypeOf(final SchemaElementType<?> type) {
        SchemaElementType<?> current;
        SchemaElementType<?> next = this;
        do {
            current = next;
            next = current.getSuperType();
            if (current == type) {
                return true;
            }
        } while (current != next);
        return overridenType != null && overridenType.isSubTypeOf(type);
    }

    /**
     * Get a {@link String} representing the hierarchy of this
     * SchemaElementType.
     *
     * @return A {@link String} representing the hierarchy of this
     * SchemaElementType.
     */
    public final String getHierachy() {
        final StringBuilder hierarchyAsString = new StringBuilder();
        hierarchyAsString.append(this.getName());

        SchemaElementType<?> currentType = this;
        SchemaElementType<?> nextType = currentType.getSuperType();
        while (nextType != currentType) {
            hierarchyAsString.insert(0, HIERARCHY_SEPARATOR_CHARACTER);
            hierarchyAsString.insert(0, nextType.getName());
            currentType = nextType;
            nextType = nextType.getSuperType();
        }

        return hierarchyAsString.toString();
    }

    /**
     * Get a flag indicating that this SchemaElementType should be completed by
     * a {@link Schema}.
     *
     * @return True is this SchemaElementType should be completed by a
     * {@link Schema}, false otherwise.
     */
    public final boolean isIncomplete() {
        return incomplete;
    }

    @Override
    public String toString() {
        return hierarchy;
    }

    /**
     * Get the 'unknown' type for this SchemaElementType. This is a type which
     * must be specified for all {@link Schema} implementations and is used as
     * the default/fallback type.
     *
     * @return A SchemaElementType representing the 'unknown' type.
     */
    public abstract T getUnknownType();

    /**
     * Create an exact copy of this SchemaElementType.
     *
     * @return A new SchemaElementType which is an exact copy of this
     * SchemaElementType.
     */
    public abstract T copy();

    /**
     * Create a copy of this SchemaElementType with a different name.
     *
     * @param name A {@link String} representing the new name.
     * @return A new SchemaElementType which is a copy of this SchemaElementType
     * but with a new name.
     */
    public abstract T rename(final String name);
}

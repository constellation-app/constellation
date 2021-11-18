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
package au.gov.asd.tac.constellation.graph.schema.attribute;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import static au.gov.asd.tac.constellation.graph.GraphElementType.TRANSACTION;
import static au.gov.asd.tac.constellation.graph.GraphElementType.VERTEX;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of utilities for interrogation of all available
 * {@link SchemaAttribute} objects.
 *
 * @author cygnus_x-1
 */
public class SchemaAttributeUtilities {

    private static final Collection<SchemaAttribute> SCHEMA_VERTEX_ATTRIBUTES = new ArrayList<>();
    private static final Collection<SchemaAttribute> SCHEMA_TRANSACTION_ATTRIBUTES = new ArrayList<>();
    
    private SchemaAttributeUtilities() {
        throw new IllegalStateException("Utility class");
    }

    private static synchronized void buildAttributes() {
        if (SCHEMA_VERTEX_ATTRIBUTES.isEmpty() || SCHEMA_TRANSACTION_ATTRIBUTES.isEmpty()) {
            SchemaConceptUtilities.getAttributes().forEach((conceptClass, schemaAttributes) -> {
                schemaAttributes.forEach(schemaAttribute -> {
                    switch (schemaAttribute.getElementType()) {
                        case VERTEX:
                            SCHEMA_VERTEX_ATTRIBUTES.add(schemaAttribute);
                            break;
                        case TRANSACTION:
                            SCHEMA_TRANSACTION_ATTRIBUTES.add(schemaAttribute);
                            break;
                        default:
                            break;
                    }
                });
            });
        }
    }

    /**
     * Get all {@link SchemaAttribute} objects for the specified
     * {@link GraphElementType}.
     *
     *
     * @param elementType The element type
     * @return A {@link List} of all discovered {@link SchemaAttribute}.
     */
    @SuppressWarnings("unchecked") // unchecked cast error cause by Empty set : this is fine.
    public static Collection<SchemaAttribute> getAttributes(final GraphElementType elementType) {
        buildAttributes();
        switch (elementType) {
            case VERTEX:
                return Collections.unmodifiableCollection(SCHEMA_VERTEX_ATTRIBUTES);
            case TRANSACTION:
                return Collections.unmodifiableCollection(SCHEMA_TRANSACTION_ATTRIBUTES);
            default:
                return Collections.emptySet();
        }
    }

    /**
     * Get all {@link SchemaAttribute} from the SchemaConcept of the specified
     * {@link Class}.
     *
     * @param elementType The element type
     * @param fromConcept The SchemaConcept from which to retrieve
     * {@link SchemaAttribute} objects.
     * @return A {@link List} of all discovered {@link SchemaAttribute}.
     */
    public static Collection<SchemaAttribute> getAttributes(final GraphElementType elementType, Class<? extends SchemaConcept> fromConcept) {
        if (fromConcept == null) {
            return getAttributes(elementType);
        }

        final Set<Class<? extends SchemaConcept>> conceptList = new HashSet<>();
        conceptList.add(fromConcept);
        return getAttributes(elementType, conceptList);
    }

    /**
     * Find all {@link SchemaAttribute} instances held by the specified List of
     * SchemaConcept instances, removing any overridden types along the way.
     *
     * @param elementType The element type
     * @param fromConcepts A {@link List} of {@link Class} objects for the set
     * of SchemaConcept from which you want held {@link SchemaAttribute}
     * objects.
     * @return A {@link Collection} of {@link SchemaAttribute}.
     */
    public static Collection<SchemaAttribute> getAttributes(final GraphElementType elementType, Set<Class<? extends SchemaConcept>> fromConcepts) {
        if (fromConcepts == null) {
            return getAttributes(elementType);
        }

        // Add all the types from all the concepts that match the desired concept classes
        final Set<SchemaAttribute> attributes = new HashSet<>();
        SchemaConceptUtilities.getAttributes().forEach((conceptClass, schemaAttributes) -> {
            if (fromConcepts.contains(conceptClass)) {
                schemaAttributes.forEach(schemaAttribute -> {
                    if (elementType.equals(schemaAttribute.getElementType())) {
                        attributes.add(schemaAttribute);
                    }
                });
            }
        });

        return Collections.unmodifiableCollection(attributes);
    }

    /**
     * Get a {@link SchemaAttribute} held by a registered {@link SchemaConcept}
     * by name. Note that if more than one type exists with the specified name,
     * then one will be chosen arbitrarily.
     *
     * @param elementType The element type
     * @param name A {@link String} representing the name of the
     * {@link SchemaAttribute} you wish to find.
     * @return A {@link SchemaAttribute} with the specified name if it could be
     * found, otherwise null.
     */
    public static SchemaAttribute getAttribute(final GraphElementType elementType, String name) {
        return getAttribute(elementType, name, null);
    }

    /**
     * Get a {@link SchemaAttribute} held by the {@link SchemaConcept} of the
     * specified {@link Class} by name. Note that if more than one type exists
     * in the specified concepts with the specified name, then one will be
     * chosen arbitrarily.
     *
     * @param elementType The element type
     * @param name A {@link String} representing the name of the
     * {@link SchemaAttribute} you wish to find.
     * @param fromConcept A {@link Class} object describing the
     * {@link SchemaConcept} you wish to search against.
     * @return A {@link SchemaAttribute} with the specified name if it could be
     * found, otherwise null.
     */
    public static SchemaAttribute getAttribute(final GraphElementType elementType, String name, Class<? extends SchemaConcept> fromConcept) {
        if (name == null) {
            return null;
        }

        for (SchemaAttribute schemaAttribute : getAttributes(elementType, fromConcept)) {
            if (schemaAttribute.getName().equals(name)
                    || schemaAttribute.toString().equals(name)) {
                return schemaAttribute;
            }
        }

        return null;
    }

    /**
     * Checks if a given {@link SchemaAttribute} has been discovered.
     *
     * @param attribute The {@link SchemaAttribute} to look for.
     * @return True if the {@link SchemaAttribute} was found, false otherwise.
     */
    public static boolean containsType(final SchemaAttribute attribute) {
        return getAttributes(attribute.getElementType()).stream()
                .anyMatch(schemaAttribute -> schemaAttribute.equals(attribute));
    }
}

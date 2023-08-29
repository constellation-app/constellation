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

import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of utilities for interrogation of all available
 * {@link SchemaTransactionType} objects.
 *
 * @author cygnus_x-1
 */
public class SchemaTransactionTypeUtilities {

    private static final Collection<SchemaTransactionType> CUSTOM_TRANSACTION_TYPES = new ArrayList<>();

    private static final Map<Set<Class<? extends SchemaConcept>>, Collection<SchemaTransactionType>> SCHEMA_TRANSACTION_TYPE_CACHE = new HashMap<>();

    private static final Set<Class<? extends SchemaConcept>> GET_ALL_TYPES = null;

    private SchemaTransactionTypeUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    public static SchemaTransactionType getDefaultType() {
        return SchemaConceptUtilities.getDefaultTransactionType();
    }

    /**
     * Get all {@link SchemaTransactionType}.
     *
     * @return A {@link List} of all discovered {@link SchemaTransactionType}.
     */
    public static Collection<SchemaTransactionType> getTypes() {
        return getTypes(GET_ALL_TYPES);
    }

    /**
     * Get all {@link SchemaTransactionType}s from the {@link SchemaConcept} of
     * the specified {@link Class}.
     *
     * @param fromConcept the concept that the SchemaTransactionType will be
     * loaded from.
     * @return A {@link List} of all discovered {@link SchemaTransactionType}.
     */
    public static Collection<SchemaTransactionType> getTypes(final Class<? extends SchemaConcept> fromConcept) {
        final Set<Class<? extends SchemaConcept>> conceptList;
        if (fromConcept == null) {
            conceptList = GET_ALL_TYPES;
        } else {
            conceptList = new HashSet<>();
            conceptList.add(fromConcept);
        }

        return getTypes(conceptList);
    }

    /**
     * Find all {@link SchemaTransactionType} instances held by the specified
     * List of {@link SchemaConcept} instances, removing any overridden types
     * along the way.
     *
     * @param fromConcepts A {@link List} of {@link Class} objects for the set
     * of {@link SchemaConcept} from which you want held
     * {@link SchemaTransactionType} objects.
     * @return A {@link Collection} of {@link SchemaTransactionType}.
     */
    public static Collection<SchemaTransactionType> getTypes(final Set<Class<? extends SchemaConcept>> fromConcepts) {
        if (!SCHEMA_TRANSACTION_TYPE_CACHE.containsKey(fromConcepts)) {
            final List<SchemaTransactionType> transactionTypes = new ArrayList<>();

            // Add all types from all concepts that match the desired concept classes
            SchemaConceptUtilities.getTransactionTypes().forEach((conceptClass, schemaTransactionTypes) -> {
                if (fromConcepts == GET_ALL_TYPES || fromConcepts.contains(conceptClass)) {
                    transactionTypes.addAll(schemaTransactionTypes);
                }
            });

            // Add all types from child concepts that match the desired concept classes
            final Set<Class<? extends SchemaConcept>> childConcepts = SchemaConceptUtilities.getChildConcepts(fromConcepts)
                    .stream().map(concept -> concept.getClass()).collect(Collectors.toSet());
            SchemaConceptUtilities.getTransactionTypes().forEach((conceptClass, schemaTransactionTypes) -> {
                if (childConcepts.contains(conceptClass)) {
                    transactionTypes.addAll(schemaTransactionTypes);
                }
            });

            // Remove any types that are overriden by concepts
            SchemaConceptUtilities.getConcepts().forEach(concept -> {
                if ((fromConcepts == GET_ALL_TYPES || fromConcepts.contains(concept.getClass())) && concept.getOverwrittenSchemaTransactionTypes() != null) {
                    transactionTypes.removeAll(concept.getOverwrittenSchemaTransactionTypes());
                }
            });

            // add custom types if no concept is specified
            if (fromConcepts == GET_ALL_TYPES) {
                transactionTypes.addAll(CUSTOM_TRANSACTION_TYPES);
            }

            SCHEMA_TRANSACTION_TYPE_CACHE.put(fromConcepts, transactionTypes);

        }

        return Collections.unmodifiableCollection(SCHEMA_TRANSACTION_TYPE_CACHE.get(fromConcepts));
    }

    /**
     * Get a {@link SchemaTransactionType} held by a registered
     * {@link SchemaConcept} by name. Note that if more than one type exists
     * with the specified name, then one will be chosen arbitrarily.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaTransactionType} you wish to find.
     * @return A {@link SchemaTransactionType} with the specified name if it
     * could be found, otherwise the default {@link SchemaTransactionType} as
     * returned by {@link SchemaConceptUtilities#getDefaultTransactionType()}.
     */
    public static SchemaTransactionType getType(final String name) {
        return getType(name, null);
    }

    /**
     * Get a {@link SchemaTransactionType} held by a registered
     * {@link SchemaConcept} by name. Note that if more than one type exists in
     * the specified concepts with the specified name, then one will be chosen
     * arbitrarily.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaTransactionType} you wish to find.
     * @param fromConcept A {@link Class} object describing the
     * {@link SchemaConcept} you wish to search against.
     * @return A {@link SchemaTransactionType} with the specified name if it
     * could be found, otherwise the default {@link SchemaTransactionType}.
     */
    public static SchemaTransactionType getType(final String name, final Class<? extends SchemaConcept> fromConcept) {
        if (name == null) {
            return getDefaultType();
        }

        for (SchemaTransactionType schemaTransactionType : getTypes(fromConcept)) {
            if (schemaTransactionType.getName().equals(name)
                    || schemaTransactionType.toString().equals(name)) {
                return schemaTransactionType;
            }
        }

        return getDefaultType();
    }

    /**
     * Get a {@link SchemaTransactionType} held by a registered
     * {@link SchemaConcept} by name if it exists or create a new type with it's
     * name set to the given String and set the incomplete flag.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaTransactionType} you wish to find.
     * @return A {@link SchemaTransactionType} with the specified name if it
     * could be found, otherwise a new {@link SchemaTransactionType} as returned
     * with the name.
     */
    public static SchemaTransactionType getTypeOrBuildNew(final String name) {
        final SchemaTransactionType defaultType = SchemaTransactionTypeUtilities.getDefaultType();
        if (name.equals(defaultType.getName())) {
            return defaultType;
        }

        SchemaTransactionType type = SchemaTransactionTypeUtilities.getType(name);
        if (type.equals(defaultType)) {
            String hierarchicalName = name;
            int lastHSCPos = hierarchicalName.lastIndexOf(SchemaElementType.HIERARCHY_SEPARATOR_CHARACTER);
            boolean foundMatch = false;
            SchemaTransactionType ancestorType = null;
            while (lastHSCPos > -1 && !foundMatch) {
                ancestorType = SchemaTransactionTypeUtilities.getType(hierarchicalName.substring(0, lastHSCPos));
                if (!ancestorType.equals(defaultType)) {
                    foundMatch = true;
                }
                hierarchicalName = hierarchicalName.substring(0, lastHSCPos);
                lastHSCPos = hierarchicalName.lastIndexOf(SchemaElementType.HIERARCHY_SEPARATOR_CHARACTER);
            }
            if (foundMatch) {
                type = new SchemaTransactionType.Builder(ancestorType, name).build();
            } else {
                type = new SchemaTransactionType.Builder(defaultType, name).setIncomplete(true).build();
            }
        }

        return type;
    }

    /**
     * Checks if a given {@link SchemaTransactionType} has been discovered by
     * SchemaConcept.
     *
     * @param type The {@link SchemaTransactionType} to look for.
     * @return True if the {@link SchemaTransactionType} was found, false
     * otherwise.
     */
    public static boolean containsType(final SchemaTransactionType type) {
        return getTypes().stream().anyMatch(schemaTransactionType -> schemaTransactionType.equals(type));
    }

    /**
     * Checks if a type with a given name has been discovered by SchemaConcept.
     *
     * @param name The name of a type to look for.
     * @return True if a {@link SchemaTransactionType} was found, false
     * otherwise.
     */
    public static boolean containsTypeName(final String name) {
        return getTypes().stream().anyMatch(t -> t.getName().equals(name));
    }

    /**
     * Add a custom type.
     *
     * @param type The new type.
     * @param replace If a type with the same name exists, replace it with this
     * type if true, else throw an exception.
     */
    public static void addCustomType(final SchemaTransactionType type, final boolean replace) {
        final boolean exists = containsTypeName(type.getName());
        if (exists) {
            if (replace) {
                final Collection<SchemaTransactionType> types = getTypes();
                for (final SchemaTransactionType existingType : types) {
                    if (existingType.getName().equals(type.getName())) {
                        types.remove(existingType);
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException(String.format("A transaction type with name %s already exists", type.getName()));
            }
        }

        CUSTOM_TRANSACTION_TYPES.add(type);

        // a custom type was added and the cache is out of date, remove the entry
        SCHEMA_TRANSACTION_TYPE_CACHE.remove(GET_ALL_TYPES);
    }
}

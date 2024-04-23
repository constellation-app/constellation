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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

/**
 * A collection of utilities for interrogation of all available
 * {@link SchemaVertexType} objects.
 *
 * @author cygnus_x-1
 */
public class SchemaVertexTypeUtilities {

    private static final Logger LOGGER = Logger.getLogger(SchemaVertexTypeUtilities.class.getName());

    private static final Collection<SchemaVertexType> CUSTOM_VERTEX_TYPES = new ArrayList<>();

    private static final Map<Set<Class<? extends SchemaConcept>>, Collection<SchemaVertexType>> SCHEMA_VERTEX_TYPE_CACHE = new HashMap<>();

    private static final Set<Class<? extends SchemaConcept>> GET_ALL_TYPES = null;
    
    private SchemaVertexTypeUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static SchemaVertexType getDefaultType() {
        return SchemaConceptUtilities.getDefaultVertexType();
    }

    /**
     * Get all {@link SchemaVertexType}.
     *
     * {@link SchemaVertexType} objects.
     *
     * @return A {@link List} of all discovered {@link SchemaVertexType}.
     */
    public static Collection<SchemaVertexType> getTypes() {
        return getTypes(GET_ALL_TYPES);
    }

    /**
     * Get all {@link SchemaVertexType} from the SchemaConcept of the specified
     * {@link Class}.
     *
     * @param fromConcept The SchemaConcept from which to retrieve
     * {@link SchemaVertexType} objects.
     * @return A {@link List} of all discovered {@link SchemaVertexType}.
     */
    public static Collection<SchemaVertexType> getTypes(final Class<? extends SchemaConcept> fromConcept) {
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
     * Find all {@link SchemaVertexType} instances held by the specified List of
     * SchemaConcept instances, removing any overridden types along the way.
     *
     * @param fromConcepts A {@link List} of {@link Class} objects for the set
     * of SchemaConcept from which you want held {@link SchemaVertexType}
     * objects.
     * @return A {@link Collection} of {@link SchemaVertexType}.
     */
    public static Collection<SchemaVertexType> getTypes(final Set<Class<? extends SchemaConcept>> fromConcepts) {
        if (!SCHEMA_VERTEX_TYPE_CACHE.containsKey(fromConcepts)) {
            final List<SchemaVertexType> vertexTypes = new ArrayList<>();

            // Add all types from concepts that match the desired concept classes
            SchemaConceptUtilities.getVertexTypes().forEach((conceptClass, schemaVertexTypes) -> {
                if (fromConcepts == GET_ALL_TYPES || fromConcepts.contains(conceptClass)) {
                    vertexTypes.addAll(schemaVertexTypes);
                }
            });

            // Add all types from child concepts that match the desired concept classes
            final Set<Class<? extends SchemaConcept>> childConcepts = SchemaConceptUtilities.getChildConcepts(fromConcepts)
                    .stream().map(concept -> concept.getClass()).collect(Collectors.toSet());
            SchemaConceptUtilities.getVertexTypes().forEach((conceptClass, schemaVertexTypes) -> {
                if (childConcepts.contains(conceptClass)) {
                    vertexTypes.addAll(schemaVertexTypes);
                }
            });

            // Remove any types that are explicitly overriden by concepts
            SchemaConceptUtilities.getConcepts().forEach(concept -> {
                if ((fromConcepts == GET_ALL_TYPES || fromConcepts.contains(concept.getClass())) && concept.getOverwrittenSchemaVertexTypes() != null) {
                    vertexTypes.removeAll(concept.getOverwrittenSchemaVertexTypes());
                }
            });

            // add custom types if no concept is specified
            if (fromConcepts.equals(GET_ALL_TYPES)) {
                vertexTypes.addAll(CUSTOM_VERTEX_TYPES);
            }

            SCHEMA_VERTEX_TYPE_CACHE.put(fromConcepts, vertexTypes);
        }

        return Collections.unmodifiableCollection(SCHEMA_VERTEX_TYPE_CACHE.get(fromConcepts));
    }

    /**
     * Get a {@link SchemaVertexType} held by a registered {@link SchemaConcept}
     * by name. Note that if more than one type exists with the specified name,
     * then one will be chosen arbitrarily.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaVertexType} you wish to find.
     * @return A {@link SchemaVertexType} with the specified name if it could be
     * found, otherwise the default {@link SchemaVertexType} as returned by
     * {@link SchemaConceptUtilities#getDefaultVertexType()}.
     */
    public static SchemaVertexType getType(final String name) {
        return getType(name, null);
    }

    /**
     * Get a {@link SchemaVertexType} held by the {@link SchemaConcept} of the
     * specified {@link Class} by name. Note that if more than one type exists
     * in the specified concepts with the specified name, then one will be
     * chosen arbitrarily.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaVertexType} you wish to find.
     * @param fromConcept A {@link Class} object describing the
     * {@link SchemaConcept} you wish to search against.
     * @return A {@link SchemaVertexType} with the specified name if it could be
     * found, otherwise the default {@link SchemaVertexType}.
     */
    public static SchemaVertexType getType(final String name, final Class<? extends SchemaConcept> fromConcept) {
        if (name == null) {
            return getDefaultType();
        }

        for (final SchemaVertexType schemaVertexType : getTypes(fromConcept)) {
            if (schemaVertexType.getName().equals(name) || schemaVertexType.toString().equals(name)) {
                return schemaVertexType;
            }
        }

        return getDefaultType();
    }

    /**
     * Get a {@link SchemaVertexType} held by a registered {@link SchemaConcept}
     * by name if it exists or create a new type with it's name set to the given
     * String and set the incomplete flag.
     *
     * @param name A {@link String} representing the name of the
     * {@link SchemaVertexType} you wish to find.
     * @return A {@link SchemaVertexType} with the specified name if it could be
     * found, otherwise a new {@link SchemaVertexType} as returned with the
     * name.
     */
    public static SchemaVertexType getTypeOrBuildNew(final String name) {
        final SchemaVertexType defaultType = SchemaVertexTypeUtilities.getDefaultType();
        if (name.equals(defaultType.getName())) {
            return defaultType;
        }

        SchemaVertexType type = SchemaVertexTypeUtilities.getType(name);
        if (type.equals(defaultType)) {
            String hierarchicalName = name;
            int lastHSCPos = hierarchicalName.lastIndexOf(SchemaElementType.HIERARCHY_SEPARATOR_CHARACTER);
            boolean foundMatch = false;
            SchemaVertexType ancestorType = null;
            while (lastHSCPos > -1 && !foundMatch) {
                ancestorType = SchemaVertexTypeUtilities.getType(hierarchicalName.substring(0, lastHSCPos));
                if (!ancestorType.equals(defaultType)) {
                    foundMatch = true;
                }
                hierarchicalName = hierarchicalName.substring(0, lastHSCPos);
                lastHSCPos = hierarchicalName.lastIndexOf(SchemaElementType.HIERARCHY_SEPARATOR_CHARACTER);
            }
            if (foundMatch) {
                type = new SchemaVertexType.Builder(ancestorType, name).build();
            } else {
                type = new SchemaVertexType.Builder(defaultType, name).setIncomplete(true).build();
            }
        }

        return type;
    }

    /**
     * Checks if a given {@link SchemaVertexType} has been discovered.
     *
     * @param type The {@link SchemaVertexType} to look for.
     * @return True if the {@link SchemaVertexType} was found, false otherwise.
     */
    public static boolean containsType(final SchemaVertexType type) {
        return getTypes().stream().anyMatch(schemaVertexType -> schemaVertexType.equals(type));
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
     * Identifies any {@link SchemaVertexType} of those held by registered
     * {@link SchemaConcept} instances detection regular expression matches the
     * specified identifier. If more than one are found, then all matching types
     * will be returned in the order they were discovered. If none are found,
     * then null will be returned.
     *
     * @param identifier A {@link String} representing an identifier you wish to
     * match a {@link SchemaVertexType} against.
     * @return A {@link List} of {@link SchemaVertexType} which matched.
     */
    public static List<SchemaVertexType> matchVertexTypes(final String identifier) {
        return matchVertexTypes(identifier, null);
    }

    /**
     * Identifies any {@link SchemaVertexType} of those held by the
     * {@link SchemaConcept} of the specified {@link Class} whose detection
     * regular expression matches the specified identifier. If more than one are
     * found, then all matching types will be returned in the order they were
     * discovered. If none are found, then null will be returned.
     *
     * @param identifier A {@link String} representing an identifier you wish to
     * match a {@link SchemaVertexType} against.
     * @param fromConcept A {@link Class} object describing the
     * {@link SchemaConcept} you wish to search against.
     * @return A {@link List} of {@link SchemaVertexType} which matched.
     */
    public static List<SchemaVertexType> matchVertexTypes(final String identifier, final Class<? extends SchemaConcept> fromConcept) {
        final List<SchemaVertexType> candidateTypes = new ArrayList<>();

        getTypes(fromConcept).forEach(schemaVertexType -> {
            final Pattern regex = schemaVertexType.getDetectionRegex();
            if (regex != null) {
                final Matcher matcher = regex.matcher(identifier).useAnchoringBounds(true);
                if (matcher.matches()) {
                    candidateTypes.add(schemaVertexType);
                }
            }
        });

        final Set<SchemaVertexType> eliminatedTypes = new HashSet<>();
        candidateTypes.forEach(cadidateType -> {
            if (!eliminatedTypes.contains(cadidateType)
                    && !cadidateType.isTopLevelType()
                    && candidateTypes.contains(cadidateType.getSuperType())) {
                eliminatedTypes.add((SchemaVertexType) cadidateType.getSuperType());
            }
        });

        candidateTypes.removeAll(eliminatedTypes);

        if (candidateTypes.size() > 1) {
            LOGGER.log(Level.WARNING, "Multiple types matched identifier {0}: {1}", new Object[]{identifier, candidateTypes.toString()});
        }

        return Collections.unmodifiableList(candidateTypes);
    }

    /**
     * Given some text as a {@link String}, find any components of that text
     * which match against the most specific detection regular expression of all
     * {@link SchemaVertexType} for all registered {@link SchemaConcept}.
     *
     * @param text A {@link String} representing text from which to find
     * {@link SchemaVertexType} matches.
     * @return A {@link List} of {@link ExtractedVertexType} representing
     * matched types in the text.
     */
    public static List<ExtractedVertexType> extractVertexTypes(final String text) {
        return extractVertexTypes(text, null);
    }

    /**
     * Given some text as a {@link String}, find any components of that text
     * which match against the most specific detection regular expression of all
     * {@link SchemaVertexType} for all registered {@link SchemaConcept}.
     *
     * @param text A {@link String} representing text from which to find
     * {@link SchemaVertexType} matches.
     * @param previouslyExtracted A list {@link ExtractedVertexType> previously
     * extracted from this text.
     * @return A {@link List} of {@link ExtractedVertexType} representing
     * matched types in the text.
     */
    public static List<ExtractedVertexType> extractVertexTypes(final String text, final List<ExtractedVertexType> previouslyExtracted) {
        final List<ExtractedVertexType> extractedTypes = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(previouslyExtracted)) {
            extractedTypes.addAll(previouslyExtracted);
        }

        getTypes().forEach(schemaVertexType -> {
            final Pattern regex = schemaVertexType.getDetectionRegex();
            if (regex != null && text != null) {
                final Matcher matcher = regex.matcher(text);
                while (matcher.find()) {
                    final String identifier = matcher.group();
                    if (identifier.length() > 0) {
                        final ExtractedVertexType currentExtractedType = new ExtractedVertexType(identifier, schemaVertexType, text, matcher.start(), matcher.end());
                        final List<ExtractedVertexType> deficientResults = new ArrayList<>();
                        boolean isDeficientResult = false;
                        for (final ExtractedVertexType extractedType : extractedTypes) {
                            if (currentExtractedType.compareTo(extractedType) < 0) {
                                isDeficientResult = true;
                                break;
                            } else if (currentExtractedType.compareTo(extractedType) > 0) {
                                deficientResults.add(extractedType);
                            }
                        }
                        if (!isDeficientResult) {
                            extractedTypes.removeAll(deficientResults);
                            extractedTypes.add(currentExtractedType);
                        }
                    }
                }
            }
        });

        return Collections.unmodifiableList(extractedTypes);
    }

    /**
     * Add a custom type.
     *
     * @param type The new type.
     * @param replace If a type with the same name exists, replace it with this
     * type if true, else throw an exception.
     */
    public static void addCustomType(final SchemaVertexType type, final boolean replace) {
        final boolean exists = containsTypeName(type.getName());
        if (exists) {
            if (replace) {
                final Collection<SchemaVertexType> types = getTypes();
                for (final SchemaVertexType existingType : types) {
                    if (existingType.getName().equals(type.getName())) {
                        types.remove(existingType);
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException(String.format("A vertex type with name %s already exists", type.getName()));
            }
        }

        CUSTOM_VERTEX_TYPES.add(type);

        // a custom type was added and the cache is out of date, remove the entry
        SCHEMA_VERTEX_TYPE_CACHE.remove(GET_ALL_TYPES);
    }

    /**
     * A class which describes a {@link SchemaVertexType} whose detection
     * regular expression was matched against some given text. This class has
     * the ability to compare itself to other extracted schema types based on
     * specificity.
     */
    public static class ExtractedVertexType implements Comparable<ExtractedVertexType> {

        private final String identifier;
        private final SchemaVertexType type;
        private final String matchedText;
        private final int startIndex;
        private final int endIndex;

        public ExtractedVertexType(final String identifier, final SchemaVertexType type,
                final String matchedText, final int startIndex, final int endIndex) {
            this.identifier = identifier;
            this.type = type;
            this.matchedText = matchedText;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getIdentifier() {
            return identifier;
        }

        public SchemaVertexType getType() {
            return type;
        }

        public String getMatchedText() {
            return matchedText;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        @Override
        public int compareTo(final ExtractedVertexType other) {
            if (startIndex >= other.startIndex && endIndex <= other.endIndex) {
                return -1;
            }
            if (startIndex <= other.startIndex && endIndex >= other.endIndex) {
                return 1;
            }
            return 0;
        }
    }
}

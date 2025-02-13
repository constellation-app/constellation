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
package au.gov.asd.tac.constellation.graph.schema.concept;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * A collection of utilities for interrogation of all available
 * {@link SchemaConcept} objects.
 *
 * @author cygnus_x-1
 */
public class SchemaConceptUtilities {

    private static SchemaConcept defaultConcept = null;
    private static Collection<SchemaConcept> schemaConcepts = null;
    private static Map<Class<? extends SchemaConcept>, Collection<SchemaAttribute>> schemaAttributes = null;
    private static Map<Class<? extends SchemaConcept>, Collection<SchemaVertexType>> schemaVertexTypes = null;
    private static Map<Class<? extends SchemaConcept>, Collection<SchemaTransactionType>> schemaTransactionTypes = null;

    private SchemaConceptUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Get the highest priority (ie. lowest {@link ServiceProvider} 'position'
     * value) SchemaConcept using {@link Lookup}.
     *
     * @return The highest priority SchemaConcept.
     */
    public static final synchronized SchemaConcept getDefaultConcept() {
        if (defaultConcept == null) {
            defaultConcept = Lookup.getDefault().lookup(SchemaConcept.class);
        }
        return defaultConcept;
    }

    /**
     * Returns the collection of all registered {@link SchemaConcept} objects.
     *
     * @return the collection of all registered {@link SchemaConcept} objects.
     */
    public static final synchronized Collection<SchemaConcept> getConcepts() {
        if (schemaConcepts == null) {
            final Collection<? extends SchemaConcept> concepts = Lookup.getDefault().lookupAll(SchemaConcept.class);
            schemaConcepts = Collections.unmodifiableCollection(concepts);
        }

        return schemaConcepts;
    }

    /**
     * Returns a set of {@link SchemaConcept} objects representing all children
     * of the specified concept class.
     *
     * @param schemaConceptClass the {@link SchemaConcept} class from which to
     * get children.
     * @return a set of {@link SchemaConcept} objects.
     */
    public static final Set<SchemaConcept> getChildConcepts(final Class<? extends SchemaConcept> schemaConceptClass) {
        final Set<SchemaConcept> childConcepts = new HashSet<>();

        getConcepts().forEach(concept -> {
            if (concept.getParents() != null && concept.getParents().contains(schemaConceptClass)) {
                childConcepts.add(concept);
                childConcepts.addAll(getChildConcepts(concept.getClass()));
            }
        });

        return childConcepts;
    }

    /**
     * Returns a set of {@link SchemaConcept} objects representing all children
     * of the specified concept classes.
     *
     * @param schemaConceptClasses the set of {@link SchemaConcept} classes from
     * which to get children.
     * @return a set of {@link SchemaConcept} objects.
     */
    public static final Set<SchemaConcept> getChildConcepts(final Set<Class<? extends SchemaConcept>> schemaConceptClasses) {
        final Set<SchemaConcept> childConcepts = new HashSet<>();
        if (schemaConceptClasses != null) {
            schemaConceptClasses.forEach(schemaConceptClass -> childConcepts.addAll(getChildConcepts(schemaConceptClass)));
        }

        return childConcepts;
    }

    /**
     * Returns a collection of all registered {@link SchemaAttribute}s.
     *
     * @return a collection of all registered {@link SchemaAttribute}s.
     */
    public static final synchronized Map<Class<? extends SchemaConcept>, Collection<SchemaAttribute>> getAttributes() {
        if (schemaAttributes == null) {
            final Map<Class<? extends SchemaConcept>, Collection<SchemaAttribute>> attributes = new HashMap<>();
            getConcepts().forEach(concept -> {
                if (concept.getSchemaAttributes() != null) {
                    attributes.put(concept.getClass(), new ArrayList<>(concept.getSchemaAttributes()));
                }
            });

            schemaAttributes = Collections.unmodifiableMap(attributes);
        }

        return schemaAttributes;
    }

    /**
     * Get the {@link SchemaConcept}s that define the specified
     * {@link SchemaAttribute}.
     *
     * @param schemaAttribute A {@link SchemaAttribute}.
     *
     * @return The {@link SchemaConcept}s which provided the specified
     * {@link SchemaAttribute}.
     */
    public static final Collection<SchemaConcept> getAttributeConcepts(final SchemaAttribute schemaAttribute) {
        final List<SchemaConcept> concepts = new ArrayList<>();
        getConcepts().forEach(concept -> {
            if (concept.getSchemaAttributes() != null && concept.getSchemaAttributes().contains(schemaAttribute)) {
                concepts.add(concept);
            }
        });

        return Collections.unmodifiableCollection(concepts);
    }

    /**
     * Find all SchemaVertexTypeProvider instances using {@link Lookup}, and
     * return them ordered by priority (ie. lowest {@link ServiceProvider}
     * 'position' value).
     *
     * @return A {@link Collection} of SchemaVertexTypeProvider.
     */
    public static final synchronized Map<Class<? extends SchemaConcept>, Collection<SchemaVertexType>> getVertexTypes() {
        if (schemaVertexTypes == null) {
            // using LinkedHashMap to preserve Schema order priority
            final Map<Class<? extends SchemaConcept>, Collection<SchemaVertexType>> vertexTypes = new LinkedHashMap<>();
            getConcepts().forEach(concept -> {
                if (concept.getSchemaVertexTypes() != null) {
                    vertexTypes.put(concept.getClass(), new ArrayList<>(concept.getSchemaVertexTypes()));
                }
            });

            schemaVertexTypes = Collections.unmodifiableMap(vertexTypes);
        }

        return schemaVertexTypes;
    }

    /**
     * Returns the {@link SchemaConcept}s that define the specified
     * SchemaVertexType.
     *
     * @param schemaVertexType the SchemaVertexType to query.
     *
     * @return the {@link SchemaConcept}s that define the specified
     * SchemaVertexType.
     */
    public static final Collection<SchemaConcept> getVertexTypeConcepts(final SchemaVertexType schemaVertexType) {
        final List<SchemaConcept> concepts = new ArrayList<>();
        getConcepts().forEach(concept -> {
            if (concept.getSchemaVertexTypes() != null && concept.getSchemaVertexTypes().contains(schemaVertexType)) {
                concepts.add(concept);
            }
        });

        return Collections.unmodifiableCollection(concepts);
    }

    /**
     * Returns the default SchemaVertexType.
     *
     * @return the default SchemaVertexType.
     */
    public static final SchemaVertexType getDefaultVertexType() {
        return getDefaultConcept().getDefaultSchemaVertexType();
    }

    /**
     * Find all SchemaTransactionTypeProvider instances using {@link Lookup},
     * and return them ordered by priority (ie. lowest {@link ServiceProvider}
     * 'position' value).
     *
     * @return A {@link Collection} of SchemaTransactionTypeProvider.
     */
    public static final synchronized Map<Class<? extends SchemaConcept>, Collection<SchemaTransactionType>> getTransactionTypes() {
        if (schemaTransactionTypes == null) {
            // using LinkedHashMap to preserve Schema order priority
            final Map<Class<? extends SchemaConcept>, Collection<SchemaTransactionType>> transactionTypes = new LinkedHashMap<>();
            getConcepts().forEach(concept -> {
                if (concept.getSchemaTransactionTypes() != null) {
                    transactionTypes.put(concept.getClass(), new ArrayList<>(concept.getSchemaTransactionTypes()));
                }
            });

            schemaTransactionTypes = Collections.unmodifiableMap(transactionTypes);
        }

        return schemaTransactionTypes;
    }

    /**
     * Returns the {@link SchemaConcept}s that define the specified
     * {@link SchemaTransactionType}.
     *
     * @param schemaTransactionType the {@link SchemaTransactionType} to query.
     *
     * @return the {@link SchemaConcept}s that define the specified
     * {@link SchemaTransactionType}.
     */
    public static final Collection<SchemaConcept> getTransactionTypeConcepts(final SchemaTransactionType schemaTransactionType) {
        final List<SchemaConcept> concepts = new ArrayList<>();
        getConcepts().forEach(concept -> {
            if (concept.getSchemaTransactionTypes() != null && concept.getSchemaTransactionTypes().contains(schemaTransactionType)) {
                concepts.add(concept);
            }
        });

        return Collections.unmodifiableCollection(concepts);
    }

    /**
     * Returns the default {@link SchemaTransactionType}.
     *
     * @return the default {@link SchemaTransactionType}.
     */
    public static final SchemaTransactionType getDefaultTransactionType() {
        return getDefaultConcept().getDefaultSchemaTransactionType();
    }
}

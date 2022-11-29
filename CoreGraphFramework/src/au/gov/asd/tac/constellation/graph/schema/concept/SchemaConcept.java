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
package au.gov.asd.tac.constellation.graph.schema.concept;

import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SchemaConcept stores the various conceptually related attributes and types
 * which a {@link Schema} might be interested in knowing about.
 *
 * @author cygnus_x-1
 */
public abstract class SchemaConcept {

    /**
     * A SchemaConcept for elements required by the default views in
     * CONSTELLATION.
     */
    @ServiceProvider(service = SchemaConcept.class)
    public static class ConstellationViewsConcept extends SchemaConcept {

        @Override
        public String getName() {
            return String.format("%s Views", BrandingUtilities.APPLICATION_NAME);
        }

        @Override
        public Set<Class<? extends SchemaConcept>> getParents() {
            final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
            parentSet.add(SchemaConcept.class);
            return Collections.unmodifiableSet(parentSet);
        }
    }

    /**
     * Get a {@link String} representing the name of this SchemaConcept. This
     * name could be used both for internally for references, as well as
     * externally for GUI elements.
     *
     * @return A {@link String} representing the name of this SchemaConcept.
     */
    public abstract String getName();

    /**
     * Get the {@link Set} of SchemaConcept classes which act as parents to this
     * SchemaConcept. If one or more parent SchemaConcept is registered to a
     * {@link SchemaFactory}, then this SchemaConcept will automatically be
     * registered too.
     *
     * @return A {@link Set} of {@link Class} objects which are the parents of
     * this SchemaConcept.
     */
    public abstract Set<Class<? extends SchemaConcept>> getParents();

    /**
     * Get the {@link Collection} of {@link SchemaAttribute} this
     * SchemaAttributeProvider holds.
     *
     * @return a {@link Collection} of {@link SchemaAttribute}.
     */
    public Collection<SchemaAttribute> getSchemaAttributes() {
        return Collections.emptyList();
    }

    /**
     * Get a priority ordered {@link List} of vertex types. This ordering is
     * specified by the order {@link SchemaVertexType} objects have been added
     * to this SchemaVertexTypeProvider.
     *
     * @return An ordered list of {@link SchemaVertexType}.
     */
    public List<SchemaVertexType> getSchemaVertexTypes() {
        return Collections.emptyList();
    }

    /**
     * Get a {@link Collection} of any {@link SchemaVertexType} which is
     * overridden by a {@link SchemaVertexType} held by this
     * SchemaVertexTypeProvider.
     *
     * @return A {@link Collection} of overwritten {@link SchemaVertexType}.
     */
    public Collection<SchemaVertexType> getOverwrittenSchemaVertexTypes() {
        return Collections.emptyList();
    }

    /**
     * Get the {@link SchemaVertexType} to default to, usually the 'Unknown'
     * type.
     *
     * @return A {@link SchemaVertexType} representing the default type.
     */
    public SchemaVertexType getDefaultSchemaVertexType() {
        return null;
    }

    /**
     * Get a priority ordered {@link List} of transaction types. This ordering
     * is specified by the order {@link SchemaTransactionType} objects have been
     * added to this SchemaTransactionTypeProvider.
     *
     * @return An ordered list of {@link SchemaTransactionType}.
     */
    public List<SchemaTransactionType> getSchemaTransactionTypes() {
        return Collections.emptyList();
    }

    /**
     * Get a {@link Collection} of any {@link SchemaTransactionType} which is
     * overridden by a {@link SchemaTransactionType} held by this
     * SchemaTransactionTypeProvider.
     *
     * @return A {@link Collection} of overwritten
     * {@link SchemaTransactionType}.
     */
    public Collection<SchemaTransactionType> getOverwrittenSchemaTransactionTypes() {
        return Collections.emptyList();
    }

    /**
     * Get the {@link SchemaTransactionType} to default to, usually the
     * 'Unknown' type.
     *
     * @return A {@link SchemaTransactionType} representing the default type.
     */
    public SchemaTransactionType getDefaultSchemaTransactionType() {
        return null;
    }
}

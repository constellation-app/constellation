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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.mergers.BitwiseOrGraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Attribute provider for attributes specific to the Layers View.
 *
 * @author aldebaran30701
 */
@ServiceProvider(service = SchemaConcept.class)
public class LayersConcept extends SchemaConcept {

    private static final String CONCEPT_CLASS_LITERAL = "Concept class";

    @Override
    public String getName() {
        return "Layers";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.ConstellationViewsConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class GraphAttribute {

        private GraphAttribute() {
            throw new IllegalStateException(CONCEPT_CLASS_LITERAL);
        }

        public static final SchemaAttribute LAYER_MASK_SELECTED = new SchemaAttribute.Builder(GraphElementType.GRAPH, LongAttributeDescription.ATTRIBUTE_NAME, "layer_bitmask_selected")
                .setDescription("The layers currently enabled for display")
                .setDefaultValue(1)
                .create()
                .build();
    }

    public static class VertexAttribute {

        private VertexAttribute() {
            throw new IllegalStateException(CONCEPT_CLASS_LITERAL);
        }

        public static final SchemaAttribute LAYER_MASK = new SchemaAttribute.Builder(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME, "layer_mask")
                .setDescription("Bitmask identifying the layers this vertex belongs to")
                .setDefaultValue(1)
                .setAttributeMergerId(BitwiseOrGraphAttributeMerger.ID)
                .create()
                .build();
        public static final SchemaAttribute LAYER_VISIBILITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "layer_visibility")
                .setDescription("The visibility of the vertex given the layers it belongs to")
                .setDefaultValue(1.0F)
                .create()
                .build();
    }

    public static class TransactionAttribute {

        private TransactionAttribute() {
            throw new IllegalStateException(CONCEPT_CLASS_LITERAL);
        }

        public static final SchemaAttribute LAYER_MASK = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME, "layer_mask")
                .setDescription("Bitmask identifying the layers this transaction belongs to")
                .setDefaultValue(1)
                .setAttributeMergerId(BitwiseOrGraphAttributeMerger.ID)
                .create()
                .build();
        public static final SchemaAttribute LAYER_VISIBILITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "layer_visibility")
                .setDescription("The visibility of the transaction given the layers it belongs to")
                .setDefaultValue(1.0F)
                .create()
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(GraphAttribute.LAYER_MASK_SELECTED);
        schemaAttributes.add(VertexAttribute.LAYER_MASK);
        schemaAttributes.add(VertexAttribute.LAYER_VISIBILITY);
        schemaAttributes.add(TransactionAttribute.LAYER_MASK);
        schemaAttributes.add(TransactionAttribute.LAYER_VISIBILITY);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}

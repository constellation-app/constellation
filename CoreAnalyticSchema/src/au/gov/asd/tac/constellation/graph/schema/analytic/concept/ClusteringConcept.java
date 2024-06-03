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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.HierarchicalStateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.KTrussStateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class ClusteringConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Clustering";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class MetaAttribute {

        public static final SchemaAttribute K_TRUSS_CLUSTERING_STATE = new SchemaAttribute.Builder(GraphElementType.META, KTrussStateAttributeDescription.ATTRIBUTE_NAME, KTrussStateAttributeDescription.ATTRIBUTE_NAME)
                .build();
        public static final SchemaAttribute HIERARCHICAL_CLUSTERING_STATE = new SchemaAttribute.Builder(GraphElementType.META, HierarchicalStateAttributeDescription.ATTRIBUTE_NAME, HierarchicalStateAttributeDescription.ATTRIBUTE_NAME)
                .build();
    }

    public static class VertexAttribute {

        public static final SchemaAttribute NAMED_CLUSTER = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Cluster.Named")
                .setDescription("The named cluster this node belongs to")
                .setDefaultValue("no cluster")
                .build();
        public static final SchemaAttribute K_TRUSS_CLUSTER = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.KTruss.Highest")
                .setDescription("The highest k-truss this node belongs to")
                .build();
        public static final SchemaAttribute K_TRUSS_COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.KTruss.Color")
                .setDescription("The color of the k-truss this node belongs to")
                .build();
        public static final SchemaAttribute HIERARCHICAL_CLUSTER = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.Hierarchical")
                .setDescription("The hierarchical cluster this node belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute HIERARCHICAL_COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Hierarchical.Color")
                .setDescription("The color of the hierarchical cluster this node belongs to")
                .build();
        public static final SchemaAttribute CHINESE_WHISPERS_CLUSTER = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers")
                .setDescription("The chinese whispers cluster this node belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute CHINESE_WHISPERS_COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers.Color")
                .setDescription("The chinese whispers cluster color")
                .build();
        public static final SchemaAttribute INFOMAP_CLUSTER = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.Infomap")
                .setDescription("The Infomap cluster this node belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute INFOMAP_COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Infomap.Color")
                .setDescription("The Infomap cluster color")
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute NAMED_CLUSTER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Cluster.Named")
                .setDescription("The named cluster this transaction belongs to")
                .setDefaultValue("no cluster")
                .build();
        public static final SchemaAttribute K_TRUSS_CLUSTER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.KTruss.Highest")
                .setDescription("The highest k-truss this transaction belongs to")
                .build();
        public static final SchemaAttribute K_TRUSS_COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.KTruss.Color")
                .setDescription("The color of the k-truss this transaction belongs to")
                .build();
        public static final SchemaAttribute HIERARCHICAL_CLUSTER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.Hierarchical")
                .setDescription("The hierarchical cluster this transaction belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute HIERARCHICAL_COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Hierarchical.Color")
                .setDescription("The color of the hierarchical cluster this transaction belongs to")
                .build();
        public static final SchemaAttribute CHINESE_WHISPERS_CLUSTER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers")
                .setDescription("The chinese whispers cluster this transaction belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute CHINESE_WHISPERS_COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers.Color")
                .setDescription("The chinese whispers cluster color")
                .build();
        public static final SchemaAttribute INFOMAP_CLUSTER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerAttributeDescription.ATTRIBUTE_NAME, "Cluster.Infomap")
                .setDescription("The Infomap cluster this node belongs to")
                .setDefaultValue(-1)
                .build();
        public static final SchemaAttribute INFOMAP_COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Infomap.Color")
                .setDescription("The Infomap cluster color")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(MetaAttribute.K_TRUSS_CLUSTERING_STATE);
        schemaAttributes.add(MetaAttribute.HIERARCHICAL_CLUSTERING_STATE);
        schemaAttributes.add(VertexAttribute.NAMED_CLUSTER);
        schemaAttributes.add(VertexAttribute.K_TRUSS_CLUSTER);
        schemaAttributes.add(VertexAttribute.K_TRUSS_COLOR);
        schemaAttributes.add(VertexAttribute.HIERARCHICAL_CLUSTER);
        schemaAttributes.add(VertexAttribute.HIERARCHICAL_COLOR);
        schemaAttributes.add(VertexAttribute.CHINESE_WHISPERS_CLUSTER);
        schemaAttributes.add(VertexAttribute.CHINESE_WHISPERS_COLOR);
        schemaAttributes.add(VertexAttribute.INFOMAP_CLUSTER);
        schemaAttributes.add(VertexAttribute.INFOMAP_COLOR);
        schemaAttributes.add(TransactionAttribute.NAMED_CLUSTER);
        schemaAttributes.add(TransactionAttribute.K_TRUSS_CLUSTER);
        schemaAttributes.add(TransactionAttribute.K_TRUSS_COLOR);
        schemaAttributes.add(TransactionAttribute.HIERARCHICAL_CLUSTER);
        schemaAttributes.add(TransactionAttribute.HIERARCHICAL_COLOR);
        schemaAttributes.add(TransactionAttribute.CHINESE_WHISPERS_CLUSTER);
        schemaAttributes.add(TransactionAttribute.CHINESE_WHISPERS_COLOR);
        schemaAttributes.add(TransactionAttribute.INFOMAP_CLUSTER);
        schemaAttributes.add(TransactionAttribute.INFOMAP_COLOR);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}

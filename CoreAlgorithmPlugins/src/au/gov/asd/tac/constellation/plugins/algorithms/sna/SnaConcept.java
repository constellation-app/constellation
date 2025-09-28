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
package au.gov.asd.tac.constellation.plugins.algorithms.sna;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
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
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class SnaConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Social Network Analysis";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class GraphAttribute {

        public static final SchemaAttribute DENSITY = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Density")
                .setDescription("The density of the graph - |E|/((|V|)*(|V|-1))")
                .build();
        public static final SchemaAttribute DIAMETER = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Diameter")
                .setDescription("The maximum hop-distance between any node and its most distant, reachable node")
                .build();
        public static final SchemaAttribute AVERAGE_DISTANCE = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Average Path Distance")
                .setDescription("The average hop-distance between pairs of nodes on the graph")
                .build();
        public static final SchemaAttribute RADIUS = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Radius")
                .setDescription("The minimum hop-distance between any node and its most distant, reachable node")
                .build();
        public static final SchemaAttribute AVERAGE_DEGREE = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "AverageDegree")
                .setDescription("The average degree of the graph")
                .build();
        public static final SchemaAttribute AVERAGE_IN_DEGREE = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "AverageInDegree")
                .setDescription("The average in-degree of the graph")
                .build();
        public static final SchemaAttribute AVERAGE_OUT_DEGREE = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "AverageOutDegree")
                .setDescription("The average out-degree of the graph")
                .build();
        public static final SchemaAttribute CLUSTERING_COEFFICIENT = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "ClusteringCoefficient")
                .setDescription("The global clustering coefficient ((3*Triangle.Count)/(Number of Connected Vertex Triplets))")
                .build();
        public static final SchemaAttribute COMPONENT_COUNT = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Count.Components")
                .setDescription("The number of components on the graph")
                .build();
        public static final SchemaAttribute TRIANGLE_COUNT = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Count.Triangles")
                .setDescription("The number of triangles on the graph")
                .build();
        public static final SchemaAttribute TRIPLET_COUNT = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "Count.Triplets")
                .setDescription("The number of triplets on the graph")
                .build();
    }

    public static class VertexAttribute {

        public static final SchemaAttribute DEGREE_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Degree")
                .build();
        public static final SchemaAttribute IN_DEGREE_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.InDegree")
                .build();
        public static final SchemaAttribute OUT_DEGREE_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.OutDegree")
                .build();
        public static final SchemaAttribute BETWEENNESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Betweenness")
                .build();
        public static final SchemaAttribute IN_BETWEENNESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.InBetweenness")
                .build();
        public static final SchemaAttribute OUT_BETWEENNESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.OutBetweenness")
                .build();
        public static final SchemaAttribute FLOW_BETWEENNESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.FlowBetweenness")
                .build();
        public static final SchemaAttribute CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Closeness")
                .build();
        public static final SchemaAttribute IN_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.InCloseness")
                .build();
        public static final SchemaAttribute OUT_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.OutCloseness")
                .build();
        public static final SchemaAttribute HARMONIC_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.HarmonicCloseness")
                .build();
        public static final SchemaAttribute IN_HARMONIC_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.InHarmonicCloseness")
                .build();
        public static final SchemaAttribute OUT_HARMONIC_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.OutHarmonicCloseness")
                .build();
        public static final SchemaAttribute HITS_CENTRALITY_AUTHORITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Hits.Authority")
                .build();
        public static final SchemaAttribute HITS_CENTRALITY_HUB = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Hits.Hub")
                .build();
        public static final SchemaAttribute FLOW_CLOSENESS_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.FlowCloseness")
                .build();
        public static final SchemaAttribute INFORMATION_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Information")
                .build();
        public static final SchemaAttribute EIGENVECTOR_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Eigenvector")
                .build();
        public static final SchemaAttribute KATZ_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Katz")
                .build();
        public static final SchemaAttribute PAGERANK_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Pagerank")
                .build();
        public static final SchemaAttribute REACH_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.Reach")
                .build();
        public static final SchemaAttribute OUT_REACH_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.OutReach")
                .build();
        public static final SchemaAttribute IN_REACH_CENTRALITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Centrality.InReach")
                .build();
        public static final SchemaAttribute ECCENTRICITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "Eccentricity")
                .build();
        public static final SchemaAttribute CLUSTERING_COEFFICIENT = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "ClusteringCoefficient")
                .build();
        public static final SchemaAttribute CONNECTIVITY_DEGREE = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "ConnectivityDegree")
                .build();
        public static final SchemaAttribute COMPONENT_SIZE = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "ComponentSize")
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute COSINE_SIMILARITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.Cosine")
                .build();
        public static final SchemaAttribute DICE_SIMILARITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.Dice")
                .build();
        public static final SchemaAttribute JACCARD_INDEX = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.Jaccard")
                .build();
        public static final SchemaAttribute COMMON_NEIGHBOURS = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.CommonNeighbours")
                .build();
        public static final SchemaAttribute RESOURCE_ALLOCATION_INDEX = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.ResourceAllocationIndex")
                .build();
        public static final SchemaAttribute ADAMIC_ADAR_INDEX = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.AdamicAdarIndex")
                .build();
        public static final SchemaAttribute PREFERENTIAL_ATTACHMENT = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.PreferentialAttachment")
                .build();
        public static final SchemaAttribute RATIO_OF_RECIPROCITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "RatioOfReciprocity")
                .build();
        public static final SchemaAttribute MULTIPLEXITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Multiplexity")
                .build();
        public static final SchemaAttribute EFFECTIVE_RESISTANCE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "EffectiveResistance")
                .build();
        public static final SchemaAttribute LEVENSHTEIN_DISTANCE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "Similarity.LevenshteinDistance")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(GraphAttribute.DENSITY);
        schemaAttributes.add(GraphAttribute.DIAMETER);
        schemaAttributes.add(GraphAttribute.AVERAGE_DISTANCE);
        schemaAttributes.add(GraphAttribute.RADIUS);
        schemaAttributes.add(GraphAttribute.AVERAGE_DEGREE);
        schemaAttributes.add(GraphAttribute.AVERAGE_IN_DEGREE);
        schemaAttributes.add(GraphAttribute.AVERAGE_OUT_DEGREE);
        schemaAttributes.add(GraphAttribute.CLUSTERING_COEFFICIENT);
        schemaAttributes.add(GraphAttribute.COMPONENT_COUNT);
        schemaAttributes.add(GraphAttribute.TRIANGLE_COUNT);
        schemaAttributes.add(GraphAttribute.TRIPLET_COUNT);
        schemaAttributes.add(VertexAttribute.DEGREE_CENTRALITY);
        schemaAttributes.add(VertexAttribute.IN_DEGREE_CENTRALITY);
        schemaAttributes.add(VertexAttribute.OUT_DEGREE_CENTRALITY);
        schemaAttributes.add(VertexAttribute.BETWEENNESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.IN_BETWEENNESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.OUT_BETWEENNESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.FLOW_BETWEENNESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.IN_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.OUT_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.HARMONIC_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.IN_HARMONIC_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.OUT_HARMONIC_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.HITS_CENTRALITY_AUTHORITY);
        schemaAttributes.add(VertexAttribute.HITS_CENTRALITY_HUB);
        schemaAttributes.add(VertexAttribute.FLOW_CLOSENESS_CENTRALITY);
        schemaAttributes.add(VertexAttribute.INFORMATION_CENTRALITY);
        schemaAttributes.add(VertexAttribute.EIGENVECTOR_CENTRALITY);
        schemaAttributes.add(VertexAttribute.KATZ_CENTRALITY);
        schemaAttributes.add(VertexAttribute.PAGERANK_CENTRALITY);
        schemaAttributes.add(VertexAttribute.REACH_CENTRALITY);
        schemaAttributes.add(VertexAttribute.OUT_REACH_CENTRALITY);
        schemaAttributes.add(VertexAttribute.IN_REACH_CENTRALITY);
        schemaAttributes.add(VertexAttribute.ECCENTRICITY);
        schemaAttributes.add(VertexAttribute.CLUSTERING_COEFFICIENT);
        schemaAttributes.add(VertexAttribute.CONNECTIVITY_DEGREE);
        schemaAttributes.add(VertexAttribute.COMPONENT_SIZE);
        schemaAttributes.add(TransactionAttribute.COSINE_SIMILARITY);
        schemaAttributes.add(TransactionAttribute.DICE_SIMILARITY);
        schemaAttributes.add(TransactionAttribute.JACCARD_INDEX);
        schemaAttributes.add(TransactionAttribute.COMMON_NEIGHBOURS);
        schemaAttributes.add(TransactionAttribute.RESOURCE_ALLOCATION_INDEX);
        schemaAttributes.add(TransactionAttribute.ADAMIC_ADAR_INDEX);
        schemaAttributes.add(TransactionAttribute.PREFERENTIAL_ATTACHMENT);
        schemaAttributes.add(TransactionAttribute.RATIO_OF_RECIPROCITY);
        schemaAttributes.add(TransactionAttribute.MULTIPLEXITY);
        schemaAttributes.add(TransactionAttribute.EFFECTIVE_RESISTANCE);
        schemaAttributes.add(TransactionAttribute.LEVENSHTEIN_DISTANCE);

        return Collections.unmodifiableCollection(schemaAttributes);
    }
}

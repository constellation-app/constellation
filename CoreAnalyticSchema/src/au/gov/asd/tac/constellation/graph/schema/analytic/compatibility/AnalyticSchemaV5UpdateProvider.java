/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Update provider to ensure that previous attributes using the spelling "colour" are updated
 * to the new spelling "color" to be consistent throughout the application
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV5UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 5;

    private static final String K_TRUSS_COLOUR = "Cluster.KTruss.Colour";
    private static final String HIERARCHICAL_COLOUR = "Cluster.Hierarchical.Colour";
    private static final String CHINESE_WHISPERS_COLOUR = "Cluster.ChineseWhispers.Colour";
    private static final String INFOMAP_COLOUR = "Cluster.Infomap.Colour";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV4UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {

        // VERTEX ATTRIBUTES

        // Retrieve the attributes IDs of the previous vertex attributes
        final int oldKTrussColourAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, K_TRUSS_COLOUR);
        final int oldHierarchicalColourAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, HIERARCHICAL_COLOUR);
        final int oldChineseWhispersColourAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, CHINESE_WHISPERS_COLOUR);
        final int oldInformapColourAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, INFOMAP_COLOUR);

        // Retrieve the new values for the vertex attributes
        final int newKTrussColorAttributeId = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.get(graph);
        final int newHierarchicalColorAttributeId = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.get(graph);
        final int newChineseWhispersColorAttributeId = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_COLOR.get(graph);
        final int newInfomapColorAttributeId = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.ensure(graph);

        // Go through the vertexs
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vertexId = graph.getVertex(vertex);

            // K Truss
            final SchemaAttribute oldKTruss = graph.getObjectValue(oldKTrussColourAttributeIdVertex, vertexId);
            if (oldKTruss != null) {
                graph.setObjectValue(newKTrussColorAttributeId, vertexId, ClusteringConcept.VertexAttribute.K_TRUSS_COLOR);
            }

            // Hierarchical
            final SchemaAttribute oldHierarchical = graph.getObjectValue(oldHierarchicalColourAttributeIdVertex, vertexId);
            if (oldHierarchical != null) {
                graph.setObjectValue(newHierarchicalColorAttributeId, vertexId,ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR);
            }

            // Chinese Whispers
            final SchemaAttribute oldChineseWhispers = graph.getObjectValue(oldChineseWhispersColourAttributeIdVertex, vertexId);
            if (oldChineseWhispers != null) {
                graph.setObjectValue(newChineseWhispersColorAttributeId, vertexId, ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_COLOR);
            }

            // Infomap
            final SchemaAttribute oldInfomap = graph.getObjectValue(oldInformapColourAttributeIdVertex, vertexId);
            if (oldInfomap != null) {
                graph.setObjectValue(newInfomapColorAttributeId, vertexId, ClusteringConcept.VertexAttribute.INFOMAP_COLOR);
            }
        }

        // TRANSACTION ATTRIBUTES

        // Retrieve the attributes IDs of the previous transaction attributes
        final int oldKTrussColourAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, K_TRUSS_COLOUR);
        final int oldHierarchicalColourAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, HIERARCHICAL_COLOUR);
        final int oldChineseWhispersColourAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, CHINESE_WHISPERS_COLOUR);
        final int oldInformapColourAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, INFOMAP_COLOUR);

        // Retrieve the new values for the transaction attributes
        final int newKTrussColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.K_TRUSS_COLOR.get(graph);
        final int newHierarchicalColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.HIERARCHICAL_COLOR.get(graph);
        final int newChineseWhispersColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.CHINESE_WHISPERS_COLOR.get(graph);
        final int newInfomapColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.INFOMAP_COLOR.ensure(graph);

        // Go through the transactions
        for (int transaction = 0; transaction < graph.getTransactionCount(); transaction++) {
            final int transactionId = graph.getTransaction(transaction);

            // K Truss
            final SchemaAttribute oldKTruss = graph.getObjectValue(oldKTrussColourAttributeIdTransaction, transactionId);
            if (oldKTruss != null) {
                graph.setObjectValue(newKTrussColorAttributeIdTransaction, transactionId, ClusteringConcept.TransactionAttribute.K_TRUSS_COLOR);
            }

            // Hierarchical
            final SchemaAttribute oldHierarchical = graph.getObjectValue(oldHierarchicalColourAttributeIdTransaction, transactionId);
            if (oldHierarchical != null) {
                graph.setObjectValue(newHierarchicalColorAttributeIdTransaction, transactionId,ClusteringConcept.TransactionAttribute.HIERARCHICAL_COLOR);
            }

            // Chinese Whispers
            final SchemaAttribute oldChineseWhispers = graph.getObjectValue(oldChineseWhispersColourAttributeIdTransaction, transactionId);
            if (oldChineseWhispers != null) {
                graph.setObjectValue(newChineseWhispersColorAttributeIdTransaction, transactionId, ClusteringConcept.TransactionAttribute.CHINESE_WHISPERS_COLOR);
            }

            // Infomap
            final SchemaAttribute oldInfomap = graph.getObjectValue(oldInformapColourAttributeIdTransaction, transactionId);
            if (oldInfomap != null) {
                graph.setObjectValue(newInfomapColorAttributeIdTransaction, transactionId, ClusteringConcept.TransactionAttribute.INFOMAP_COLOR);
            }
        }

    }

}

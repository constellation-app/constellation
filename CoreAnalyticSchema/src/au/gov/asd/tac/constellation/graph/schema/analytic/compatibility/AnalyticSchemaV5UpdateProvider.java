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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
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
    private static final String LABEL_PROPAGATION_COLOUR = "Cluster.LabelPropagation.Colour";
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
        final int oldKTrussColorAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, K_TRUSS_COLOUR);
        final int oldHierarchicalColorAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, HIERARCHICAL_COLOUR);
        final int oldLabelPropagationColorAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, LABEL_PROPAGATION_COLOUR);
        final int oldInfomapColorAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, INFOMAP_COLOUR);

        // Go through the vertexs
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vertexId = graph.getVertex(vertex);

            // K Truss
            if (oldKTrussColorAttributeIdVertex != Graph.NOT_FOUND) {
                final int newKTrussColorAttributeId = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.ensure(graph);
                final ConstellationColor oldKTruss = graph.getObjectValue(oldKTrussColorAttributeIdVertex, vertexId);
                graph.setObjectValue(newKTrussColorAttributeId, vertexId, oldKTruss);
            }
            
            // Hierarchical
            if (oldHierarchicalColorAttributeIdVertex != Graph.NOT_FOUND) {
                final int newHierarchicalColorAttributeId = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.ensure(graph);
                final ConstellationColor oldHierarchical = graph.getObjectValue(oldHierarchicalColorAttributeIdVertex, vertexId);
                graph.setObjectValue(newHierarchicalColorAttributeId, vertexId, oldHierarchical); 
            }
            
            // Label Propagation Clustering
            if (oldLabelPropagationColorAttributeIdVertex != Graph.NOT_FOUND) {
                final int newLabelPropagationColorAttributeId = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_COLOR.ensure(graph);
                final ConstellationColor oldLabelPropagation = graph.getObjectValue(oldLabelPropagationColorAttributeIdVertex, vertexId);
                graph.setObjectValue(newLabelPropagationColorAttributeId, vertexId, oldLabelPropagation);    
            }

            // Infomap
            if (oldInfomapColorAttributeIdVertex != Graph.NOT_FOUND) {
                final int newInfomapColorAttributeId = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.ensure(graph);
                final ConstellationColor oldInfomap = graph.getObjectValue(oldInfomapColorAttributeIdVertex, vertexId);
                graph.setObjectValue(newInfomapColorAttributeId, vertexId, oldInfomap);    
            }
        }

        // Remove the old attributes
        if (oldKTrussColorAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldKTrussColorAttributeIdVertex);
        }

        if (oldHierarchicalColorAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldHierarchicalColorAttributeIdVertex);
        }

        if (oldLabelPropagationColorAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldLabelPropagationColorAttributeIdVertex);
        }

        if (oldInfomapColorAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldInfomapColorAttributeIdVertex);
        }

        // TRANSACTION ATTRIBUTES

        // Retrieve the attributes IDs of the previous transaction attributes
        final int oldKTrussColorAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, K_TRUSS_COLOUR);
        final int oldHierarchicalColorAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, HIERARCHICAL_COLOUR);
        final int oldLabelPropagationColorAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, LABEL_PROPAGATION_COLOUR);
        final int oldInfomapColorAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, INFOMAP_COLOUR);

        // Go through the transactions
        for (int transaction = 0; transaction < graph.getTransactionCount(); transaction++) {
            final int transactionId = graph.getTransaction(transaction);

            // K Truss
            if (oldKTrussColorAttributeIdTransaction != Graph.NOT_FOUND) {
                final int newKTrussColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.K_TRUSS_COLOR.ensure(graph);
                final ConstellationColor oldKTruss = graph.getObjectValue(oldKTrussColorAttributeIdTransaction, transactionId);
                graph.setObjectValue(newKTrussColorAttributeIdTransaction, transactionId, oldKTruss);
            }

            // Hierarchical
            if (oldHierarchicalColorAttributeIdTransaction != Graph.NOT_FOUND) {
                final int newHierarchicalColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.HIERARCHICAL_COLOR.ensure(graph);
                final ConstellationColor oldHierarchical = graph.getObjectValue(oldHierarchicalColorAttributeIdTransaction, transactionId);
                graph.setObjectValue(newHierarchicalColorAttributeIdTransaction, transactionId, oldHierarchical);   
            }

            // Label Propagation Clustering/Chinese Whispers
            if (oldLabelPropagationColorAttributeIdTransaction != Graph.NOT_FOUND) {
                final int newLabelPropagationColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.LABEL_PROPAGATION_COLOR.ensure(graph);
                final ConstellationColor oldLabelPropagation = graph.getObjectValue(oldLabelPropagationColorAttributeIdTransaction, transactionId);
                graph.setObjectValue(newLabelPropagationColorAttributeIdTransaction, transactionId, oldLabelPropagation);
            }

            // Infomap
            if (oldInfomapColorAttributeIdTransaction != Graph.NOT_FOUND) {
                final int newInfomapColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.INFOMAP_COLOR.ensure(graph);
                final ConstellationColor oldInfomap = graph.getObjectValue(oldInfomapColorAttributeIdTransaction, transactionId);
                graph.setObjectValue(newInfomapColorAttributeIdTransaction, transactionId, oldInfomap);     
            }
        }

        // Remove the old attributes
        if (oldKTrussColorAttributeIdTransaction != Graph.NOT_FOUND) {
            graph.removeAttribute(oldKTrussColorAttributeIdTransaction);
        }

        if (oldHierarchicalColorAttributeIdTransaction != Graph.NOT_FOUND) {
            graph.removeAttribute(oldHierarchicalColorAttributeIdTransaction);
        }

        if (oldLabelPropagationColorAttributeIdTransaction != Graph.NOT_FOUND) {
            graph.removeAttribute(oldLabelPropagationColorAttributeIdTransaction);
        }

        if (oldInfomapColorAttributeIdTransaction != Graph.NOT_FOUND) {
            graph.removeAttribute(oldInfomapColorAttributeIdTransaction);
        }
    }
}

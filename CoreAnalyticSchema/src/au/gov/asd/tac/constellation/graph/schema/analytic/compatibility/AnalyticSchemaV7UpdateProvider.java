/*
 * Copyright 2010-2026 Australian Signals Directorate
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
 * Update provider to ensure that previous attributes using "Chinese Whispers"
 * are updated to the new term "Label Propagation" to be consistent throughout
 * the application.
 *
 * @author andromeda-224
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV7UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 7;

    private static final String CHINESE_WHISPERS_COLOR = "Cluster.ChineseWhispers.Color";
    private static final String CHINESE_WHISPERS_CLUSTER = "Cluster.ChineseWhispers";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV6UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        // Chinese Whispers ATTRIBUTES

        // Retrieve the attributes IDs of the previous vertex attributes
        final int oldChineseWhispersColorAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, CHINESE_WHISPERS_COLOR);
        final int oldChineseWhispersAttributeIdVertex = graph.getAttribute(GraphElementType.VERTEX, CHINESE_WHISPERS_CLUSTER);

        // Go through the vertexs
        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vertexId = graph.getVertex(vertex);

            // Chinese Whispers
            if (oldChineseWhispersAttributeIdVertex != Graph.NOT_FOUND) {
                final int newLabelPropagationAttributeId = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_CLUSTER.ensure(graph);
                final int oldChineseWhispersCluster = graph.getIntValue(oldChineseWhispersAttributeIdVertex, vertexId);
                graph.setObjectValue(newLabelPropagationAttributeId, vertexId, oldChineseWhispersCluster);
            }

            if (oldChineseWhispersColorAttributeIdVertex != Graph.NOT_FOUND) {
                final int newLabelPropagationColorAttributeId = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_COLOR.ensure(graph);
                final ConstellationColor oldChineseWhispers = graph.getObjectValue(oldChineseWhispersColorAttributeIdVertex, vertexId);
                graph.setObjectValue(newLabelPropagationColorAttributeId, vertexId, oldChineseWhispers);
            }
        }

        // remove the old Chinese Whispers attributes from the graph
        if (oldChineseWhispersAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldChineseWhispersAttributeIdVertex);
        }

        if (oldChineseWhispersColorAttributeIdVertex != Graph.NOT_FOUND) {
            graph.removeAttribute(oldChineseWhispersColorAttributeIdVertex);
        }

        // TRANSACTION ATTRIBUTES
        // Retrieve the attributes IDs of the previous transaction attributes
        final int oldChineseWhispersColorAttributeIdTransaction = graph.getAttribute(GraphElementType.TRANSACTION, CHINESE_WHISPERS_COLOR);
        
        // Go through the transactions
        for (int transaction = 0; transaction < graph.getTransactionCount(); transaction++) {
            final int transactionId = graph.getTransaction(transaction);

            // Chinese Whispers
            if (oldChineseWhispersColorAttributeIdTransaction != Graph.NOT_FOUND) {
                final int newLabelPropagationColorAttributeIdTransaction = ClusteringConcept.TransactionAttribute.LABEL_PROPAGATION_COLOR.ensure(graph);
                final ConstellationColor oldChineseWhispersColor = graph.getObjectValue(oldChineseWhispersColorAttributeIdTransaction, transactionId);
                graph.setObjectValue(newLabelPropagationColorAttributeIdTransaction, transactionId, oldChineseWhispersColor);
            }
        }

        // Remove the old attributes
        if (oldChineseWhispersColorAttributeIdTransaction != Graph.NOT_FOUND) {
            graph.removeAttribute(oldChineseWhispersColorAttributeIdTransaction);
        }
    }
}

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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that the improved type attributes are used on the graph.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV2UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 2;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV1UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        boolean updateVertexKeys = false;

        // update vertex raw attribute
        final int oldVertexRawAttributeId = AnalyticConcept.VertexAttribute.RAW.get(graph);
        final Attribute oldVertexRawAttribute = new GraphAttribute(graph, oldVertexRawAttributeId);
        if (!oldVertexRawAttribute.getAttributeType().equals(RawAttributeDescription.ATTRIBUTE_NAME)) {
            graph.setPrimaryKey(GraphElementType.VERTEX);

            final int newVertexRawAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
            final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final String rawValue = graph.getStringValue(oldVertexRawAttributeId, vertexId);
                graph.setObjectValue(newVertexRawAttribute, vertexId, new RawData(rawValue, null));

                if (graph.getStringValue(vertexIdentifierAttribute, vertexId) == null) {
                    graph.setObjectValue(vertexIdentifierAttribute, vertexId, rawValue);
                }
            }
            graph.removeAttribute(oldVertexRawAttributeId);

            updateVertexKeys = true;
        }

        // update vertex type attribute
        if (AnalyticConcept.VertexAttribute.TYPE.get(graph) == Graph.NOT_FOUND) {
            updateVertexKeys = true;
        }
        final int oldVertexTypeAttributeId = graph.getAttribute(GraphElementType.VERTEX, "Type");
        final Attribute oldVertexTypeAttribute = new GraphAttribute(graph, oldVertexTypeAttributeId);
        if (!oldVertexTypeAttribute.getAttributeType().equals(VertexTypeAttributeDescription.ATTRIBUTE_NAME)) {
            graph.setPrimaryKey(GraphElementType.VERTEX);

            final int newVertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
            final int vertexRawAttributeId = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final String typeValue = graph.getStringValue(oldVertexTypeAttributeId, vertexId);
                final SchemaVertexType type = SchemaVertexTypeUtilities.getType(typeValue);
                graph.setObjectValue(newVertexTypeAttributeId, graph.getVertex(vertexPosition), type);

                final RawData rawValue = graph.getObjectValue(vertexRawAttributeId, graph.getVertex(vertexPosition));
                graph.setObjectValue(vertexRawAttributeId, vertexId, rawValue != null ? new RawData(rawValue.getRawIdentifier(), typeValue) : new RawData(null, null));
            }
            graph.removeAttribute(oldVertexTypeAttributeId);

            updateVertexKeys = true;
        }

        // update vertex keys and complete vertices
        if (updateVertexKeys && graph.getSchema() != null) {
            final List<SchemaAttribute> keyAttributes = graph.getSchema().getFactory().getKeyAttributes(GraphElementType.VERTEX);
            final int[] keyAttributeIds = keyAttributes.stream().map(keyAttribute -> keyAttribute.ensure(graph)).mapToInt(keyAttributeId -> keyAttributeId).toArray();
            graph.setPrimaryKey(GraphElementType.VERTEX, keyAttributeIds);
        }

        boolean updateTransactionKeys = false;

        // update transaction type attribute
        if (AnalyticConcept.TransactionAttribute.TYPE.get(graph) == Graph.NOT_FOUND) {
            updateTransactionKeys = true;
        }
        final int oldTransactionTypeAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, "Type");
        final Attribute oldTransactionTypeAttribute = new GraphAttribute(graph, oldTransactionTypeAttributeId);
        if (!oldTransactionTypeAttribute.getAttributeType().equals(TransactionTypeAttributeDescription.ATTRIBUTE_NAME)) {
            graph.setPrimaryKey(GraphElementType.TRANSACTION);

            final int newTransactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
            for (int transactionPosition = 0; transactionPosition < graph.getTransactionCount(); transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);
                final String typeValue = graph.getStringValue(oldTransactionTypeAttributeId, transactionId);
                final SchemaTransactionType type = SchemaTransactionTypeUtilities.getType(typeValue);
                graph.setObjectValue(newTransactionTypeAttributeId, transactionId, type);
            }
            graph.removeAttribute(oldTransactionTypeAttributeId);

            updateTransactionKeys = true;
        }

        // update transaction datetime attribute
        if (TemporalConcept.TransactionAttribute.DATETIME.get(graph) == Graph.NOT_FOUND) {
            updateTransactionKeys = true;
        }

        // update transaction keys and complete transactions
        if (updateTransactionKeys && graph.getSchema() != null) {
            final List<SchemaAttribute> keyAttributes = graph.getSchema().getFactory().getKeyAttributes(GraphElementType.TRANSACTION);
            final int[] keyAttributeIds = keyAttributes.stream().map(keyAttribute -> keyAttribute.ensure(graph)).mapToInt(keyAttributeId -> keyAttributeId).toArray();
            graph.setPrimaryKey(GraphElementType.TRANSACTION, keyAttributeIds);
        }
    }
}

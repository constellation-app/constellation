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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that the new label and identifier attributes are used in
 * place of the 'name' and 'uniqueid' attributes.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV4UpdateProvider extends SchemaUpdateProvider {

    static final int SCHEMA_VERSION_THIS_UPDATE = 4;

    private static final String LABEL_ATTRIBUTE_NAME = "Name";
    private static final String UNIQUE_ID_ATTRIBUTE_NAME = "UniqueId";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV3UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(StoreGraph graph) {
        boolean updateVertexKeys = false;

        // update vertex identifier attribute
        if (VisualConcept.VertexAttribute.IDENTIFIER.get(graph) == Graph.NOT_FOUND) {
            updateVertexKeys = true;
        }

        // update vertex label attribute
        final int oldVertexLabelAttributeId = graph.getAttribute(GraphElementType.VERTEX, LABEL_ATTRIBUTE_NAME);
        if (oldVertexLabelAttributeId != GraphConstants.NOT_FOUND) {
            graph.setPrimaryKey(GraphElementType.VERTEX);

            final int newVertexLabelAttributeId = VisualConcept.VertexAttribute.LABEL.ensure(graph);
            final int vertexIdentiferAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final String labelValue = graph.getStringValue(oldVertexLabelAttributeId, vertexId);
                graph.setStringValue(newVertexLabelAttributeId, vertexId, labelValue);

                if (graph.getStringValue(vertexIdentiferAttributeId, vertexId) == null) {
                    graph.setStringValue(vertexIdentiferAttributeId, vertexId, labelValue);
                }
            }
            graph.removeAttribute(oldVertexLabelAttributeId);

            updateVertexKeys = true;
        }

        // update vertex keys and complete vertices
        if (updateVertexKeys) {
            final List<SchemaAttribute> keyAttributes = graph.getSchema().getFactory().getKeyAttributes(GraphElementType.VERTEX);
            final int[] keyAttributeIds = keyAttributes.stream().map(keyAttribute -> keyAttribute.ensure(graph)).mapToInt(keyAttributeId -> keyAttributeId).toArray();
            graph.setPrimaryKey(GraphElementType.VERTEX, keyAttributeIds);
        }

        // update vertex labels
        final int vertexBottomLabelsAttributeId = VisualConcept.GraphAttribute.BOTTOM_LABELS.get(graph);
        if (vertexBottomLabelsAttributeId != GraphConstants.NOT_FOUND) {
            final GraphLabel label = new GraphLabel(
                    VisualConcept.VertexAttribute.LABEL.getName(),
                    graph.getSchema().getFactory().getVertexLabelColor());
            graph.setObjectValue(vertexBottomLabelsAttributeId, 0, new GraphLabels(Arrays.asList(label)));
        }

        boolean updateTransactionKeys = false;

        // update transaction identifier attribute
        if (VisualConcept.TransactionAttribute.IDENTIFIER.get(graph) == Graph.NOT_FOUND) {
            updateTransactionKeys = true;
        }
        final int oldTransactionUniqueIdAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, UNIQUE_ID_ATTRIBUTE_NAME);
        if (oldTransactionUniqueIdAttributeId != GraphConstants.NOT_FOUND) {
            graph.setPrimaryKey(GraphElementType.TRANSACTION);

            final int newTransactionIdentifierAttributeId = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
            for (int transactionPosition = 0; transactionPosition < graph.getTransactionCount(); transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);
                final String uniqueIdValue = graph.getStringValue(oldTransactionUniqueIdAttributeId, transactionId);
                graph.setStringValue(newTransactionIdentifierAttributeId, transactionId, uniqueIdValue);
            }
            graph.removeAttribute(oldTransactionUniqueIdAttributeId);

            updateTransactionKeys = true;
        }

        // update transaction label attribute
        final int oldTransactionLabelAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, LABEL_ATTRIBUTE_NAME);
        if (oldTransactionLabelAttributeId != GraphConstants.NOT_FOUND) {
            graph.setPrimaryKey(GraphElementType.TRANSACTION);

            final int newTransactionLabelAttributeId = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
            final int transactionIdentiferAttributeId = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
            for (int transactionPosition = 0; transactionPosition < graph.getTransactionCount(); transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);
                final String labelValue = graph.getStringValue(oldTransactionLabelAttributeId, transactionId);
                graph.setStringValue(newTransactionLabelAttributeId, transactionId, labelValue);

                if (graph.getStringValue(transactionIdentiferAttributeId, transactionId) == null) {
                    graph.setStringValue(transactionIdentiferAttributeId, transactionId, labelValue);
                }
            }
            graph.removeAttribute(oldTransactionLabelAttributeId);

            updateTransactionKeys = true;
        }

        // update transaction keys and complete transactions
        if (updateTransactionKeys) {
            final List<SchemaAttribute> keyAttributes = graph.getSchema().getFactory().getKeyAttributes(GraphElementType.TRANSACTION);
            final int[] keyAttributeIds = keyAttributes.stream().map(keyAttribute -> keyAttribute.ensure(graph)).mapToInt(keyAttributeId -> keyAttributeId).toArray();
            graph.setPrimaryKey(GraphElementType.TRANSACTION, keyAttributeIds);
        }
    }
}

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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * This update ensures that the node_labels_top and node_labels bottom graph
 * attributes appropriately change name to node_top_labels_colours and
 * node_bottom_labels_colours
 *
 * @author Atlas139mkm
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV6UpdateProvider extends SchemaUpdateProvider {

    private static final String GRAPH_BOTTOM_LABELS = "node_labels_bottom";
    private static final String GRAPH_TOP_LABELS = "node_labels_top";
    private static final String GRAPH_TRANSACTION_LABELS = "transaction_labels";

    public static final int SCHEMA_VERSION_THIS_UPDATE = 6;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV5UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {

        // retrieve the attribute Id of both the graph_labels_top/bottom
        final int oldGraphBottomlabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, GRAPH_BOTTOM_LABELS);
        final int oldGraphToplabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, GRAPH_TOP_LABELS);
        final int oldGraphTransactionlabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, GRAPH_TRANSACTION_LABELS);

        // retrieve the new values for the graph_labels_bottom and set it to the
        // new values
        final int newGraphBottomLabelsAttributeId = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final String bottomLabelValue = graph.getStringValue(oldGraphBottomlabelsAttributeId, 0);
        graph.setStringValue(newGraphBottomLabelsAttributeId, 0, bottomLabelValue);

        // retrieve the new values for the graph_labels_top and set it to the
        // new values
        final int newGraphTopLabelsAttributeId = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);
        final String topLabelValue = graph.getStringValue(oldGraphToplabelsAttributeId, 0);
        graph.setStringValue(newGraphTopLabelsAttributeId, 0, topLabelValue);

        // retrieve the new values for the transaction_labels and set it to the
        // new values
        final int newGraphTransactionlabelsAttributeId = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(graph);
        final String transactionLabelValue = graph.getStringValue(oldGraphTransactionlabelsAttributeId, 0);
        graph.setStringValue(newGraphTransactionlabelsAttributeId, 0, transactionLabelValue);

        // remove the old attributes
        graph.removeAttribute(oldGraphBottomlabelsAttributeId);
        graph.removeAttribute(oldGraphToplabelsAttributeId);
        graph.removeAttribute(oldGraphTransactionlabelsAttributeId);

    }

}

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
 * Update provider to ensure that previous attributes using the spelling "colour" are updated
 * to the new spelling "color" to be consistent throughout the application 
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV7UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 7;

    private static final String NODE_BOTTOM_LABELS_COLOURS = "node_bottom_labels_colours";
    private static final String NODE_TOP_LABEL_COLOURS = "node_top_labels_colours";
    private static final String TRANSACTION_LABEL_COLOURS = "transaction_labels_colours";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV6UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {

        // Retrieve the attributes IDs of the previous attributes 
        final int oldGraphBottomlabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, NODE_BOTTOM_LABELS_COLOURS);
        final int oldGraphToplabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, NODE_TOP_LABEL_COLOURS);
        final int oldGraphTransactionlabelsAttributeId = graph.getAttribute(GraphElementType.GRAPH, TRANSACTION_LABEL_COLOURS);

        // retrieve the new values for the graph_labels_bottom and set it to the new values
        final int newGraphBottomLabelsAttributeId = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final String bottomLabelValue = graph.getStringValue(oldGraphBottomlabelsAttributeId, 0);
        graph.setStringValue(newGraphBottomLabelsAttributeId, 0, bottomLabelValue);

        // retrieve the new values for the graph_labels_top and set it to the new values
        final int newGraphTopLabelsAttributeId = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);
        final String topLabelValue = graph.getStringValue(oldGraphToplabelsAttributeId, 0);
        graph.setStringValue(newGraphTopLabelsAttributeId, 0, topLabelValue);

        // retrieve the new values for the transaction_labels and set it to the new values
        final int newGraphTransactionlabelsAttributeId = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(graph);
        final String transactionLabelValue = graph.getStringValue(oldGraphTransactionlabelsAttributeId, 0);
        graph.setStringValue(newGraphTransactionlabelsAttributeId, 0, transactionLabelValue);

        // remove the old attributes
        graph.removeAttribute(oldGraphBottomlabelsAttributeId);
        graph.removeAttribute(oldGraphToplabelsAttributeId);
        graph.removeAttribute(oldGraphTransactionlabelsAttributeId);
    
    }

}

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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility.GraphLabelV0;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility.GraphLabelsAndDecoratorsV0;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that new graph attributes for labels and decorators are
 * defined which allows various visual changes to be made from the Attribute
 * Editor, replacing disparate GUI interfaces.
 *
 * @author twilight_sparkle
 */
@SuppressWarnings("deprecation")
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV1UpdateProvider extends SchemaUpdateProvider {

    static final int SCHEMA_VERSION_THIS_UPDATE = 1;

    private static final String LABELS_AND_DECORATORS_ATTRIBUTE_NAME = "labels";
    private static final String LABELS_TOP_ATTRIBUTE_NAME = "labels_top";
    private static final String LABELS_BOTTOM_ATTRIBUTE_NAME = "labels_bottom";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return UpdateProvider.DEFAULT_VERSION;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        final int bottomLabelsAttribute = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final int decoratorsAttribute = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        final int topLabelsAttribute = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);
        final int transactionLabelsAttribute = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(graph);

        VisualConcept.GraphAttribute.BLAZE_OPACITY.ensure(graph);
        VisualConcept.GraphAttribute.BLAZE_SIZE.ensure(graph);
        VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(graph);
        VisualConcept.GraphAttribute.DRAW_FLAGS.ensure(graph);
        VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
        VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);
        VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD.ensure(graph);
        VisualConcept.GraphAttribute.VISIBILITY_THRESHOLD.ensure(graph);

        final int labelsAndDecoratorsAttrId = graph.getAttribute(GraphElementType.META, LABELS_AND_DECORATORS_ATTRIBUTE_NAME);
        if (labelsAndDecoratorsAttrId != Graph.NOT_FOUND) {
            final GraphLabelsAndDecoratorsV0 labelsAndDecorators = graph.getObjectValue(labelsAndDecoratorsAttrId, 0);

            if (labelsAndDecorators != null) {
                final GraphLabelV0[] bottomLabels = labelsAndDecorators.getBottomLabels();
                List<au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel> newBottomLabels = new ArrayList<>();
                for (GraphLabelV0 bottomLabel : bottomLabels) {
                    newBottomLabels.add(new au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel(bottomLabel.getLabel(), bottomLabel.getColor(), bottomLabel.getRadius()));
                }
                graph.setObjectValue(bottomLabelsAttribute, 0, new GraphLabels(newBottomLabels));

                final GraphLabelV0[] topLabels = labelsAndDecorators.getTopLabels();
                List<au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel> newTopLabels = new ArrayList<>();
                for (GraphLabelV0 topLabel : topLabels) {
                    newTopLabels.add(new au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel(topLabel.getLabel(), topLabel.getColor(), topLabel.getRadius()));
                }
                graph.setObjectValue(topLabelsAttribute, 0, new GraphLabels(newTopLabels));

                final GraphLabelV0[] connectionLabels = labelsAndDecorators.getConnectionLabels();
                List<au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel> newConnectionLabels = new ArrayList<>();
                for (GraphLabelV0 connectionLabel : connectionLabels) {
                    newConnectionLabels.add(new au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel(connectionLabel.getLabel(), connectionLabel.getColor(), connectionLabel.getRadius()));
                }
                graph.setObjectValue(transactionLabelsAttribute, 0, new GraphLabels(newConnectionLabels));

                final String[] decoratorAttributes = labelsAndDecorators.getDecoratorLabels();
                graph.setObjectValue(decoratorsAttribute, 0, new VertexDecorators(
                        updateDecoratorAttr(decoratorAttributes[0]),
                        updateDecoratorAttr(decoratorAttributes[1]),
                        updateDecoratorAttr(decoratorAttributes[2]),
                        updateDecoratorAttr(decoratorAttributes[3])
                ));
            }

            graph.removeAttribute(labelsAndDecoratorsAttrId);
        }

        final int labelsTopAttrId = graph.getAttribute(GraphElementType.GRAPH, LABELS_TOP_ATTRIBUTE_NAME);
        if (labelsTopAttrId != Graph.NOT_FOUND) {
            final String[] labelsTop = graph.getStringValue(labelsTopAttrId, 0).split(",");
            final List<au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel> newTopLabels = new ArrayList<>();
            for (String topLabel : labelsTop) {
                newTopLabels.add(new au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel(topLabel, ConstellationColor.LIGHT_BLUE, 1));
            }
            graph.setObjectValue(topLabelsAttribute, 0, new GraphLabels(newTopLabels));
            graph.removeAttribute(labelsTopAttrId);
        }

        final int labelsBottomAttrId = graph.getAttribute(GraphElementType.GRAPH, LABELS_BOTTOM_ATTRIBUTE_NAME);
        if (labelsBottomAttrId != Graph.NOT_FOUND) {
            final String[] labelsBottom = graph.getStringValue(labelsBottomAttrId, 0).split(",");
            final List<au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel> newBottomLabels = new ArrayList<>();
            for (String bottomLabel : labelsBottom) {
                newBottomLabels.add(new au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel(bottomLabel, ConstellationColor.LIGHT_BLUE, 1));
            }
            graph.setObjectValue(bottomLabelsAttribute, 0, new GraphLabels(newBottomLabels));
            graph.removeAttribute(labelsBottomAttrId);
        }
    }

    private static String updateDecoratorAttr(final String attr) {
        return StringUtils.isBlank(attr) || "null".equals(attr) ? null : attr;
    }
}

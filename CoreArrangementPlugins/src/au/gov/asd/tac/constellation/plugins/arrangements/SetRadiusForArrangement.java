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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * A Utility to set an attribute on the graph called 'lradius' which
 * approximates the distance from the centre of the node to the edge of the
 * longest label.
 * <br>
 * Some arrangements use this attribute rather than the node's radius to work
 * out how to space out nodes. Note that this should be reimplemented as a
 * plugin.
 *
 * @author twilight_sparkle
 */
public class SetRadiusForArrangement {

    private final GraphWriteMethods graph;
    private int[] labelAttrArray;
    private float[] labelSizeArray;
    private final int labelRadiusAttr;
    private final int nodeRadiusAttr;
    // There are approximately 8 characters in a label for each node radius unit
    private static final float CHARACTERS_PER_NODE_RADIUS_UNIT = 8F;

    public SetRadiusForArrangement(final GraphWriteMethods graph) {
        this.graph = graph;
        labelRadiusAttr = VisualConcept.VertexAttribute.LABEL_RADIUS.ensure(graph);
        nodeRadiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
        getLabelAttributes();
    }

    private void getLabelAttributes() {
        final int bottomLabelsAttr = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final int topLabelsAttr = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);

        final GraphLabels bottomLabels = graph.getObjectValue(bottomLabelsAttr, 0);
        final GraphLabels topLabels = graph.getObjectValue(topLabelsAttr, 0);

        labelAttrArray = new int[bottomLabels.getNumberOfLabels() + topLabels.getNumberOfLabels()];
        labelSizeArray = new float[labelAttrArray.length];

        int labelNum = 0;
        for (final GraphLabel label : bottomLabels.getLabels()) {
            labelAttrArray[labelNum] = graph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            labelSizeArray[labelNum++] = label.getSize();
        }
        for (final GraphLabel label : topLabels.getLabels()) {
            labelAttrArray[labelNum] = graph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            labelSizeArray[labelNum++] = label.getSize();
        }
    }

    public void setRadii() {
        final int vxCount = graph.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            setRadius(vxId);
        }
    }

    private void setRadius(final int vxId) {
        // We require the width of the label to be at least 1.5f, which is the radius of the standard node with a little extra padding
        float maxLabelWidth = 1.5F;
        for (int i = 0; i < labelAttrArray.length; i++) {
            if (labelAttrArray[i] < 0) {
                continue;
            }

            final Object obj = graph.getObjectValue(labelAttrArray[i], vxId);

            final int characters = (obj == null || obj.toString() == null) ? 0 : obj.toString().length();

            // Set the current label width, we do not allow room for characters to contribute more than 4 node radius units to the label width since wrapping should be implemented in the future
            final float currentLabelWidth = Math.min(4, (characters / CHARACTERS_PER_NODE_RADIUS_UNIT)) * labelSizeArray[i];
            if (currentLabelWidth > maxLabelWidth) {
                maxLabelWidth = currentLabelWidth;
            }
        }

        final float nodeRadius = graph.getFloatValue(nodeRadiusAttr, vxId);
        graph.setFloatValue(labelRadiusAttr, vxId, maxLabelWidth * nodeRadius);
    }
}

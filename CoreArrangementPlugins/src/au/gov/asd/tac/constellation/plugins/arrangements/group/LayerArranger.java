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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Arrange the graph such that different values of the specified attribute are
 * at different z levels.
 *
 * @author algol
 */
public class LayerArranger implements Arranger {

    private static final String VISIBILITY = "visibility";

    private int attr = Graph.NOT_FOUND;

    private boolean maintainMean = false;

    public void setLevelAttr(final int attr) {
        this.attr = attr;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        if (attr == Graph.NOT_FOUND) {
            throw new IllegalArgumentException("Level attribute not set");
        }

        final boolean isFloat = new GraphAttribute(wg, attr).getAttributeType().equals(FloatAttributeDescription.ATTRIBUTE_NAME);

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int xAttr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
        final int yAttr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0, null);
        final int zAttr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0, null);
        final int x2Attr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int y2Attr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int z2Attr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
        final int visAttr = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, VISIBILITY, VISIBILITY, 2, null);

        // Discover the unique attribute values.
        // Collect the min and max z values along the way.
        final Set<String> values = new HashSet<>();
        float xmin = Float.MAX_VALUE;
        float xmax = -Float.MAX_VALUE;
        float ymin = Float.MAX_VALUE;
        float ymax = -Float.MAX_VALUE;
        float zmin = Float.MAX_VALUE;
        float zmax = -Float.MAX_VALUE;

        float minFloat = Float.MAX_VALUE;
        float maxFloat = -Float.MAX_VALUE;

        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            if (isFloat) {
                final float value = wg.getFloatValue(attr, vxId);
                minFloat = Math.min(value, minFloat);
                maxFloat = Math.max(value, maxFloat);
            } else {
                final String value = wg.getStringValue(attr, vxId);
                values.add(value);
            }

            final float x = wg.getFloatValue(xAttr, vxId);
            final float y = wg.getFloatValue(yAttr, vxId);
            final float z = wg.getFloatValue(zAttr, vxId);
            xmin = Math.min(x, xmin);
            xmax = Math.max(x, xmax);
            ymin = Math.min(y, ymin);
            ymax = Math.max(y, xmax);
            zmin = Math.min(z, zmin);
            zmax = Math.max(z, xmax);
        }

        // Arrange each z around the mean of the existing z values.
        // Levels are a fraction of the width of the graph.
        final float zmean = (zmin + zmax) / 2F;
        final float width = Math.max(xmax - xmin, ymax - ymin);

        if (isFloat) {
            // Set the positions directly based on the attribute value.
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                wg.setFloatValue(x2Attr, vxId, wg.getFloatValue(xAttr, vxId));
                wg.setFloatValue(y2Attr, vxId, wg.getFloatValue(yAttr, vxId));
                wg.setFloatValue(z2Attr, vxId, wg.getFloatValue(zAttr, vxId));

                final float value = wg.getFloatValue(attr, vxId);
                final float norm = (value - minFloat) / (maxFloat - minFloat);
                wg.setFloatValue(zAttr, vxId, (norm - 0.5F) * width + zmean);
                wg.setFloatValue(visAttr, vxId, norm);
            }
        } else {
            // Set the level height to be 5% of the graph width or 4 (twice the default node diameter), whichever is larger.
            final float levelHeight = Math.max(width * 0.05F, 4);

            // Figure out which value belongs in which level.
            final String[] valueArray = values.toArray(new String[values.size()]);
            Arrays.sort(valueArray, (s1, s2) -> {
                if (s1 == null) {
                    return s2 == null ? 0 : -1;
                } else if (s2 == null) {
                    return 1;
                } else {
                    return s1.toLowerCase().compareTo(s2.toLowerCase());
                }
            });
            final Map<String, Integer> attrLevel = new HashMap<>();
            for (int i = 0; i < valueArray.length; i++) {
                attrLevel.put(valueArray[i], i);
            }

            final int levelBase = (int) zmean - valueArray.length / 2;
            final float visBase = 1F / (attrLevel.size() + 1);

            // Finally, set the positions.
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                wg.setFloatValue(x2Attr, vxId, wg.getFloatValue(xAttr, vxId));
                wg.setFloatValue(y2Attr, vxId, wg.getFloatValue(yAttr, vxId));
                wg.setFloatValue(z2Attr, vxId, wg.getFloatValue(zAttr, vxId));

                final String value = wg.getStringValue(attr, vxId);
                final int level = attrLevel.get(value);
                final float z = levelBase + level * levelHeight;
                wg.setFloatValue(zAttr, vxId, z);
                final float vis = (level + 1) * visBase;
                wg.setFloatValue(visAttr, vxId, vis);
            }
        }

        if (maintainMean) {
            final float[] newMean = ArrangementUtilities.getXyzMean(wg);
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                // Don't bother with x and y, they haven't changed.
                wg.setFloatValue(zAttr, vxId, wg.getFloatValue(zAttr, vxId) - newMean[2] + oldMean[2]);
            }
        }

        // Set the transaction visibility to be the minimum visibility of it's two vertices.
        final int txVisAttr = wg.addAttribute(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, VISIBILITY, VISIBILITY, 1, null);
        final int txCount = wg.getTransactionCount();
        for (int position = 0; position < txCount; position++) {
            final int txId = wg.getTransaction(position);

            final int vx0 = wg.getTransactionSourceVertex(txId);
            final int vx1 = wg.getTransactionDestinationVertex(txId);

            final float vis0 = wg.getFloatValue(visAttr, vx0);
            final float vis1 = wg.getFloatValue(visAttr, vx1);

            wg.setFloatValue(txVisAttr, txId, vis0 < vis1 ? vis0 : vis1);
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }
}

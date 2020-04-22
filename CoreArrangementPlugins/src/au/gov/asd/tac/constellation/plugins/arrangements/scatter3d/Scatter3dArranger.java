/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter3d;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.time.ZonedDateTime;
import java.util.BitSet;

/**
 * Arrange the graph in a scatter3d.
 *
 * @author CrucisGamma
 */
public class Scatter3dArranger implements Arranger {

    private final Scatter3dChoiceParameters params;

    /**
     * A scatter3d arrangement with default parameters.
     */
    public Scatter3dArranger() {
        this(Scatter3dChoiceParameters.getDefaultParameters());
    }

    /**
     * Construct new ArrangeInScatter3d instance.
     *
     * @param params Parameters for the arrangement.
     */
    public Scatter3dArranger(final Scatter3dChoiceParameters params) {
        this.params = params;
    }

    @Override
    public void setMaintainMean(final boolean b) {
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {

        // Get/set the x,y,z attributes.
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", null, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", null, null);
        }

        final String xDimension;
        final String yDimension;
        final String zDimension;
        final boolean xLogarithmic;
        final boolean yLogarithmic;
        final boolean zLogarithmic;
        if (params != null) {
            xDimension = params.getXDimension();
            yDimension = params.getYDimension();
            zDimension = params.getZDimension();
            xLogarithmic = params.getLogarithmicX();
            yLogarithmic = params.getLogarithmicY();
            zLogarithmic = params.getLogarithmicZ();
        } else {
            return;
        }

        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

        final int xDim = wg.getAttribute(GraphElementType.VERTEX, xDimension);
        final int yDim = wg.getAttribute(GraphElementType.VERTEX, yDimension);
        final int zDim = wg.getAttribute(GraphElementType.VERTEX, zDimension);
        final int vxCount = wg.getVertexCount();

        float maxX = 0;
        float minX = 0;
        float maxY = 0;
        float minY = 0;
        float maxZ = 0;
        float minZ = 0;
        boolean firstVals = true;

        if (vxCount > 0) {
            final BitSet vertices = GraphUtilities.vertexBits(wg);
            int vxPos = 0;
            final int[] vxOrder = new int[vxCount];
            for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                vxOrder[vxPos++] = vxId;
                vertices.clear(vxId);
            }

            float xVal;
            float yVal;
            float zVal;

            for (int i = 0; i < vxPos; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int vxId = vxOrder[i];

                xVal = getFloatValueFromObject(wg.getObjectValue(xDim, vxId), xLogarithmic);
                yVal = getFloatValueFromObject(wg.getObjectValue(yDim, vxId), yLogarithmic);
                zVal = getFloatValueFromObject(wg.getObjectValue(zDim, vxId), zLogarithmic);

                if (firstVals) {
                    firstVals = false;
                    maxX = xVal;
                    minX = xVal;
                    maxY = yVal;
                    minY = yVal;
                    maxZ = zVal;
                    minZ = zVal;
                } else {
                    minX = Math.min(xVal, minX);
                    maxX = Math.max(xVal, maxX);
                    minY = Math.min(yVal, minY);
                    maxY = Math.max(yVal, maxY);
                    minZ = Math.min(zVal, minZ);
                    maxZ = Math.max(zVal, maxZ);
                }

                wg.setFloatValue(xAttr, vxId, xVal);
                wg.setFloatValue(yAttr, vxId, yVal);
                wg.setFloatValue(zAttr, vxId, zVal);
            }

            // Scale to 0-100
            if (maxX == minX) {
                maxX += 1;
            }
            if (maxY == minY) {
                maxY += 1;
            }
            if (maxZ == minZ) {
                maxZ += 1;
            }

            for (int i = 0; i < vxPos; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int vxId = vxOrder[i];

                xVal = wg.getFloatValue(xAttr, vxId);
                yVal = wg.getFloatValue(yAttr, vxId);
                zVal = wg.getFloatValue(zAttr, vxId);

                xVal = 100 * (xVal - minX) / (maxX - minX);
                yVal = 100 * (yVal - minY) / (maxY - minY);
                zVal = 100 * (zVal - minZ) / (maxZ - minZ);

                wg.setFloatValue(xAttr, vxId, xVal);
                wg.setFloatValue(yAttr, vxId, yVal);
                wg.setFloatValue(zAttr, vxId, zVal);
            }
        }
    }

    private float getFinalValue(float value, boolean logarithmic) {
        if (logarithmic) {
            return (float) Math.log10(value);
        }
        return value;
    }

    private float getFloatValueFromObject(Object attributeValue, boolean logarithmic) {
        if (attributeValue == null) {
            return 0.0f;
        }

        if (attributeValue instanceof Float) {
            return getFinalValue((float) attributeValue, logarithmic);
        }

        if (attributeValue instanceof Double) {
            return getFinalValue((float) attributeValue, logarithmic);
        }

        if (attributeValue instanceof String) {
            String val = (String) attributeValue;
            float finalVal = 0.0f;
            float multiplier = 1;
            for (int i = 0; i < val.length(); i++) {
                char ch = val.charAt(i);
                float chVal = (float) ch;
                finalVal += ((float) chVal) * multiplier;
                multiplier /= 26;
            }
            return getFinalValue(finalVal, logarithmic);
        }

        if (attributeValue instanceof Integer) {
            float ret = (Integer) attributeValue;
            return ret;
        }

        if (attributeValue instanceof ConstellationColor) {
            ConstellationColor color = (ConstellationColor) attributeValue;
            float red = color.getRed() / 256;
            float green = color.getGreen() / 256;
            float blue = color.getBlue() / 256;
            return getFinalValue((red + green + blue) * 100, logarithmic);
        }

        if (attributeValue instanceof ZonedDateTime) {
            ZonedDateTime c = (ZonedDateTime) attributeValue;
            float year = c.getYear();
            float month = c.getMonthValue();
            float monthDay = c.getDayOfMonth();
            float hour = c.getHour();
            float minute = c.getMinute();
            return getFinalValue((year - 2010) + month / 12 + monthDay / (366) + hour / (366 * 24) + minute / (366 * 24 * 60), logarithmic);
        }

        if (attributeValue instanceof RawData) {
            String s = ((RawData) attributeValue).toString();
            return getFloatValueFromObject(s, logarithmic);
        }

        return 0.0f;
    }
}

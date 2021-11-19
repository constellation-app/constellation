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
package au.gov.asd.tac.constellation.plugins.arrangements.circle;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;

/**
 *
 * @author algol
 */
public class CircleArranger implements Arranger {

    private boolean maintainMean = false;

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final int vxCount = wg.getVertexCount();
        if (vxCount < 2) {
            // Graphs of size 0 or 1 are already in a circle.
            return;
        }

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(wg);
        radiusSetter.setRadii();

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        // Get required attributes.
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int nradiusAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());

        // Use these if they exist.
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());
        final boolean xyz2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        // Determine the circumference of the circle, given that nodes may have different radii.
        float circleCircumference = 0;
        float maxRadius = 0;
        final float sqrt2 = (float) Math.sqrt(2); // Use radius to the corners rather than the sides.
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float nradius = (nradiusAttr != Graph.NOT_FOUND ? wg.getFloatValue(nradiusAttr, vxId) : 1) * sqrt2;
            circleCircumference += 2F * nradius;
            maxRadius = Math.max(maxRadius, nradius);
        }

        // If there's a really big node, make the circle bigger.
        circleCircumference = Math.max(circleCircumference, 2 * maxRadius * sqrt2 * (float) Math.PI);

        // Now arrange the vertices on the circumference, positioned by their fraction of
        // the space they each take up.
        final float circleRadius = circleCircumference / (2F * (float) Math.PI);
        float angle = 0;
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float nradius = (nradiusAttr != Graph.NOT_FOUND ? wg.getFloatValue(nradiusAttr, vxId) : 1) * sqrt2;

            // What fraction of the circumference is this?
            // And therefore, what is the angle subtended?
            // (if the circumference is 0 then we divide by 1 to avoid dividing by 0 (nradius will be 0 anyway))
            final float arcfrac = (2F * nradius) / (circleCircumference != 0 ? circleCircumference : 1);
            final float arclen = 2F * (float) Math.PI * arcfrac;
            final float subtends = arclen;

            final float positionOnCircle = angle + subtends / 2F;

            // Calculate the x & y position for each vertex.
            final float x = circleRadius * (float) (Math.sin(positionOnCircle));
            final float y = circleRadius * (float) (Math.cos(positionOnCircle));
            final float z = 0;

            wg.setFloatValue(xAttr, vxId, x);
            wg.setFloatValue(yAttr, vxId, y);
            wg.setFloatValue(zAttr, vxId, z);

            if (xyz2) {
                wg.setFloatValue(x2Attr, vxId, x);
                wg.setFloatValue(y2Attr, vxId, y);
                wg.setFloatValue(z2Attr, vxId, z);
            }

            angle += subtends;
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }

    @Override
    public void setMaintainMean(boolean b) {
        maintainMean = b;
    }
}

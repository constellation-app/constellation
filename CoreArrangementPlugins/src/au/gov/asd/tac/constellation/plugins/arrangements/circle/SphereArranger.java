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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;

/**
 * Arrange in a sphere.
 * <p>
 * The arrangement uses the Fibonacci sphere algorithm.
 * <p>
 * The previous x,y,z values are copied to x2,y2,z2.
 *
 * @author algol
 */
public class SphereArranger implements Arranger {

    private boolean maintainMean = false;

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final int vxCount = wg.getVertexCount();
        if (vxCount < 2) {
            return;
        }

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int x2Id = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Id = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Id = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());

        final float rnd = 1F; // Use new Random().nextFloat() * vxCount to add some randomess.
        final float offset = 2F / vxCount;
        final float increment = (float) (Math.PI * (3.0 - Math.sqrt(5)));

        // Make the radius dependent on the number of vertices, with a lower limit.
        //
        final float radius = 8F + (float) (1.0 * Math.sqrt(vxCount));

        for (int position = 0; position < vxCount; position++) {
            final float y = ((position * offset) - 1) + (offset / 2F);
            final float r = (float) Math.sqrt(1.0 - Math.pow(y, 2.0));
            final float phi = ((position + rnd) % vxCount) * increment;
            final float x = (float) Math.cos(phi) * r;
            final float z = (float) Math.sin(phi) * r;

            final int vxId = wg.getVertex(position);

            // Copy the old x,y,z to x2,y2,z2.
            //
            wg.setFloatValue(x2Id, vxId, wg.getFloatValue(xId, vxId));
            wg.setFloatValue(y2Id, vxId, wg.getFloatValue(yId, vxId));
            wg.setFloatValue(z2Id, vxId, wg.getFloatValue(zId, vxId));

            wg.setFloatValue(xId, vxId, x * radius);
            wg.setFloatValue(yId, vxId, y * radius);
            wg.setFloatValue(zId, vxId, z * radius);
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }
}

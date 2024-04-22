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
package au.gov.asd.tac.constellation.plugins.arrangements.random;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.security.SecureRandom;

/**
 *
 * @author algol
 */
public class RandomArranger implements Arranger {

    private final int dimensions;
    private boolean maintainMean = false;

    private final SecureRandom random = new SecureRandom();

    public RandomArranger(final int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int xId = VisualConcept.VertexAttribute.X.get(wg);
        final int yId = VisualConcept.VertexAttribute.Y.get(wg);
        final int zId = VisualConcept.VertexAttribute.Z.get(wg);
        final int x2Id = VisualConcept.VertexAttribute.X2.ensure(wg);
        final int y2Id = VisualConcept.VertexAttribute.Y2.ensure(wg);
        final int z2Id = VisualConcept.VertexAttribute.Z2.ensure(wg);

        final int vxCount = wg.getVertexCount();

        // We want the side to be long enough that a subsequent uncollide doesn't take too long.
        final float side = 4F * (float) Math.sqrt(vxCount);

        final Vector3f xyz = new Vector3f();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            wg.setFloatValue(x2Id, vxId, wg.getFloatValue(xId, vxId));
            wg.setFloatValue(y2Id, vxId, wg.getFloatValue(yId, vxId));
            wg.setFloatValue(z2Id, vxId, wg.getFloatValue(xId, vxId));

            // Arrange in a circle/sphere.
            do {
                xyz.set(0.5F - random.nextFloat(), 0.5F - random.nextFloat(), dimensions == 3 ? 0.5F - random.nextFloat() : 0);
            } while (xyz.getLength() > 0.5F);

            wg.setFloatValue(xId, vxId, side * xyz.getX());
            wg.setFloatValue(yId, vxId, side * xyz.getY());
            wg.setFloatValue(zId, vxId, side * xyz.getZ());
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

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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;

/**
 * Flatten the graph by setting z to zero.
 *
 * @author algol
 */
public class FlattenZField implements Arranger {

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        if (zAttr != Graph.NOT_FOUND) {
            final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());
            final int vxCount = wg.getVertexCount();

            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                if (z2Attr != Graph.NOT_FOUND) {
                    wg.setFloatValue(z2Attr, vxId, wg.getFloatValue(zAttr, vxId));
                }

                wg.setFloatValue(zAttr, vxId, 0);
            }
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // No need to maintain the mean, this arrangement is too simple.
    }
}

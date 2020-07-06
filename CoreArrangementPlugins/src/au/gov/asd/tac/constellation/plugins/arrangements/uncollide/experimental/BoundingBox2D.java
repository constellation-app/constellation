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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 *
 * @author algol
 */
public class BoundingBox2D {

    public static Box2D getBox(final GraphWriteMethods wg) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        final int x2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int y2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int z2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);

        float minx = wg.getFloatValue(xId, wg.getVertex(0));
        float miny = wg.getFloatValue(yId, wg.getVertex(0));
        float maxx = minx;
        float maxy = miny;

        final int vxCount = wg.getVertexCount();
                
        for (int position = 1; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            if (x < minx) {
                minx = x;
            }
            if (x > maxx) {
                maxx = x;
            }
            if (y < miny) {
                miny = y;
            }
            if (y > maxy) {
                maxy = y;
            }
        }

        return new Box2D(minx, miny, maxx, maxy);
    }

    public static class Box2D {

        public final float minx;
        public final float miny;
        public final float maxx;
        public final float maxy;

        public Box2D(final float minx, final float miny, final float maxx, final float maxy) {
            this.minx = minx;
            this.miny = miny;
            this.maxx = maxx;
            this.maxy = maxy;
        }

        @Override
        public String toString() {
            return String.format("[Box2D %f %f %f %f]", minx, miny, maxx, maxy);
        }
    }
}

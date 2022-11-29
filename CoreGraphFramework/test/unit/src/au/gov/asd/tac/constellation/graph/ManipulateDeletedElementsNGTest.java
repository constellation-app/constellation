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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import org.testng.annotations.Test;

/**
 * Manipulate Deleted Elements Test.
 *
 * @author cygnus_x-1
 */
public class ManipulateDeletedElementsNGTest {

    @Test
    public void setIndexedAttributeValueOfDeletedVertexTest() {
        final DualGraph g = new DualGraph(new StoreGraph(), false);

        int attr, v1;

        try {
            // Add an indexed boolean attribute, then add a vertex, then delete that vertex.
            final WritableGraph wg = g.getWritableGraph("add attribute, add/remove vertex", true);
            try {
                attr = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "attr", null, false, null);
                wg.setAttributeIndexType(attr, GraphIndexType.UNORDERED);

                v1 = wg.addVertex();
                wg.removeVertex(v1);
            } finally {
                wg.commit();
            }

            // Set the attribute value of the deleted vertex.
            final WritableGraph wg2 = g.getWritableGraph("set deleted value", true);
            try {
                wg2.setBooleanValue(attr, v1, true);
            } finally {
                wg2.commit();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

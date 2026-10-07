/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class NewTreeArrangerNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Left Intentially Blank
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Left Intentially Blank
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Left Intentially Blank
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Left Intentially Blank
    }

    @Test
    public void testArrange() throws Exception {
        System.out.println("arrange");
        final StoreGraph graph = createTestGraph();
        final float nodeDistance = 10F;
        final int x = VisualConcept.VertexAttribute.X.ensure(graph);
        final int y = VisualConcept.VertexAttribute.Y.ensure(graph);

        final NewTreeArranger instance = new NewTreeArranger(nodeDistance);

        instance.arrange(graph);

        final float vx0x = graph.getFloatValue(x, 0);
        final float vx0y = graph.getFloatValue(y, 0);

        // Assert first circle of neighbour nodes are a certain distance from the center
        for (int i = 1; i < 4; i++) {
            final int vxId = graph.getVertex(i);

            final double distance = Math.sqrt(Math.pow(graph.getFloatValue(x, vxId) - vx0x, 2) + Math.pow(graph.getFloatValue(y, vxId) - vx0y, 2));
            assertEquals(distance, nodeDistance, 0.1F);
        }

        // Assert second circle of neighbour nodes are a certain distance from the center
        for (int i = 4; i < 13; i++) {
            final int vxId = graph.getVertex(i);

            final double distance = Math.sqrt(Math.pow(graph.getFloatValue(x, vxId) - vx0x, 2) + Math.pow(graph.getFloatValue(y, vxId) - vx0y, 2));
            assertEquals(distance, nodeDistance * 2, 0.1F);
        }

    }

    private StoreGraph createTestGraph() {
        final StoreGraph storeGraph = new StoreGraph();

        final int vx0 = storeGraph.addVertex();

        final int vx1 = storeGraph.addVertex();
        final int vx2 = storeGraph.addVertex();
        final int vx3 = storeGraph.addVertex();

        final int vx4 = storeGraph.addVertex();
        final int vx5 = storeGraph.addVertex();
        final int vx6 = storeGraph.addVertex();

        final int vx7 = storeGraph.addVertex();
        final int vx8 = storeGraph.addVertex();
        final int vx9 = storeGraph.addVertex();

        final int vx10 = storeGraph.addVertex();
        final int vx11 = storeGraph.addVertex();
        final int vx12 = storeGraph.addVertex();

        storeGraph.addTransaction(vx0, vx1, true);
        storeGraph.addTransaction(vx0, vx2, true);
        storeGraph.addTransaction(vx0, vx3, true);

        storeGraph.addTransaction(vx1, vx4, true);
        storeGraph.addTransaction(vx1, vx5, true);
        storeGraph.addTransaction(vx1, vx6, true);

        storeGraph.addTransaction(vx2, vx7, true);
        storeGraph.addTransaction(vx2, vx8, true);
        storeGraph.addTransaction(vx2, vx9, true);

        storeGraph.addTransaction(vx3, vx10, true);
        storeGraph.addTransaction(vx3, vx11, true);
        storeGraph.addTransaction(vx3, vx12, true);

        VisualConcept.VertexAttribute.SELECTED.ensure(storeGraph);

        return storeGraph;
    }

}

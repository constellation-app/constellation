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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Flatten Z Test.
 *
 * @author altair
 */
public class FlattenZNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5;
    private int txId1, txId2, txId3;
    private int vAttrId, tAttrId;
    private StoreGraph graph;

    public FlattenZNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private void generateData() throws InterruptedException {
        graph = new StoreGraph();
        attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        if (attrX == Graph.NOT_FOUND) {
            fail();
        }

        attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        if (attrY == Graph.NOT_FOUND) {
            fail();
        }

        attrZ = VisualConcept.VertexAttribute.Z.ensure(graph);
        if (attrZ == Graph.NOT_FOUND) {
            fail();
        }

        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        if (vAttrId == Graph.NOT_FOUND) {
            fail();
        }

        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        if (tAttrId == Graph.NOT_FOUND) {
            fail();
        }

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setFloatValue(attrZ, vxId1, 1.0f);
        graph.setBooleanValue(vAttrId, vxId1, false);
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, 5.0f);
        graph.setFloatValue(attrY, vxId2, 1.0f);
        graph.setFloatValue(attrZ, vxId2, 2.0f);
        graph.setBooleanValue(vAttrId, vxId2, false);
        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 1.0f);
        graph.setFloatValue(attrY, vxId3, 5.0f);
        graph.setFloatValue(attrZ, vxId3, 3.0f);
        graph.setBooleanValue(vAttrId, vxId3, false);
        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, 5.0f);
        graph.setFloatValue(attrY, vxId4, 5.0f);
        graph.setFloatValue(attrZ, vxId4, 4.0f);
        graph.setBooleanValue(vAttrId, vxId4, false);
        vxId5 = graph.addVertex();
        graph.setFloatValue(attrX, vxId5, 10.0f);
        graph.setFloatValue(attrY, vxId5, 10.0f);
        graph.setFloatValue(attrZ, vxId5, 5.0f);
        graph.setBooleanValue(vAttrId, vxId5, false);

        graph.addTransaction(vxId1, vxId2, false);
        graph.addTransaction(vxId1, vxId3, false);
        graph.addTransaction(vxId2, vxId4, true);
        graph.addTransaction(vxId4, vxId2, true);
    }

    @Test
    public void flattenZTest() throws InterruptedException, PluginException {
        generateData();

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            float value = graph.getFloatValue(attrZ, txId);
            assertTrue(String.format("Vertex [%d] should have non-zero z value", txId), (0.0f != value));
        }

        PluginExecution.withPlugin(ArrangementPluginRegistry.FLATTEN_Z).executeNow(graph);

        for (int i = 0; i < graph.getVertexCount(); i++) {
            final int txId = graph.getVertex(i);
            float value = graph.getFloatValue(attrZ, txId);
            assertTrue(String.format("Vertex [%d] should have zero z value", txId), (0.0f == value));
        }
    }
}

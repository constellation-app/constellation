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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import java.io.IOException;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class BoxSelectionPluginNGTest extends ConstellationTest {

    BoxSelectionPlugin boxSelect;
    Camera camera;
    float[] box; // left, right, top, bottom in camera coordinates
    float left = (float) -0.1;
    float right = (float) 0.1;
    float top = (float) 0.1;
    float bottom = (float) -0.1;

    private int attrX, attrY;
    private int vxId1, vxId2, vxId3, vxId4;
    private int txId1, txId2;
    private int vSelectedAttrId, tSelectedAttrId;
    private StoreGraph graph;

    public BoxSelectionPluginNGTest() {
        createNewBox();
        camera = new Camera();
        boxSelect = new BoxSelectionPlugin(true, true, camera, box);
    }

    private void createNewBox() {
        box = new float[4];
        box[0] = left;
        box[1] = right;
        box[2] = top;
        box[3] = bottom;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph();

        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);

        vSelectedAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        tSelectedAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of edit method, of class BoxSelectionPlugin.
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException, IOException {
        System.out.println("edit");

        // test with 0 mix ratio in every boolean value arrangement
        camera = new Camera();
        camera.setMixRatio(0);
        runBoxSelectTests(true, true);
        runBoxSelectTests(true, false);
        runBoxSelectTests(false, true);
        runBoxSelectTests(false, false);

        // Test with 1 mix ratio in every boolean value arrangement
        camera = new Camera();
        camera.setMixRatio(1);
        runBoxSelectTests(true, true);
        runBoxSelectTests(true, false);
        runBoxSelectTests(false, true);
        runBoxSelectTests(false, false);

        // test with 0 mix ratio and VxVisibility in every boolean value arrangement
        VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        camera = new Camera();
        camera.setMixRatio(0);
        runBoxSelectTests(true, true);
        runBoxSelectTests(true, false);
        runBoxSelectTests(false, true);
        runBoxSelectTests(false, false);
    }

    // Reduce code duplication by allowing one method to call 4 methods with the specified parameters.
    public void runBoxSelectTests(final boolean isAdd, final boolean isToggle) throws InterruptedException, PluginException, IOException {
        testBoxSelectWithNothingSelected(isAdd, isToggle);
        testBoxSelectWithOneVxSelected(isAdd, isToggle);
        testBoxSelectWithTwoVxOneTxSelected(isAdd, isToggle);
        testBoxSelectWithTwoVxOneTxSecondaryAttributesSelected(isAdd, isToggle);
    }

    public void testBoxSelectWithNothingSelected(final boolean isAdd, final boolean isToggle) throws InterruptedException, PluginException, IOException {
        // Test selecting nothing
        // no element should be within the box
        left = -0.56f;
        right = -0.54f;
        top = 0.268f;
        bottom = 0.244f;

        createNewBox();

        boxSelect = new BoxSelectionPlugin(isAdd, isToggle, camera, box);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -1.0f);
        graph.setFloatValue(attrY, vxId2, -1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId2, false);

        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 3.0f);
        graph.setFloatValue(attrY, vxId3, 3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId3, false);

        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, -3.0f);
        graph.setFloatValue(attrY, vxId4, -3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId4, false);

        // Add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId4, false);

        graph.setBooleanValue(tSelectedAttrId, txId1, false);
        graph.setBooleanValue(tSelectedAttrId, txId2, false);

        // Test all selected attributes are false before execution
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));

        // Run plugin
        PluginExecution.withPlugin(boxSelect).executeNow(graph);

        // Test selected node values
        assertFalse("Vx Visibility After", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility After", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility After", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility After", graph.getBooleanValue(vSelectedAttrId, vxId4));

        // Test selected transaction values
        assertFalse("Tx Visibility After", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility After", graph.getBooleanValue(tSelectedAttrId, txId2));
    }

    public void testBoxSelectWithOneVxSelected(final boolean isAdd, final boolean isToggle) throws InterruptedException, PluginException, IOException {
        // test box without transaction
        // vxId1 should be within a box 0.06, 0.13, 0.126, 0.054
        left = 0.06f;
        right = 0.13f;
        top = 0.126f;
        bottom = 0.054f;

        createNewBox();
        boxSelect = new BoxSelectionPlugin(isAdd, isToggle, camera, box);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -1.0f);
        graph.setFloatValue(attrY, vxId2, -1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId2, false);

        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 3.0f);
        graph.setFloatValue(attrY, vxId3, 3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId3, false);

        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, -3.0f);
        graph.setFloatValue(attrY, vxId4, -3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId4, false);

        // Add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId4, false);

        graph.setBooleanValue(tSelectedAttrId, txId1, false);
        graph.setBooleanValue(tSelectedAttrId, txId2, false);

        // Test all selected attributes are false before execution
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));

        // Run plugin
        PluginExecution.withPlugin(boxSelect).executeNow(graph);

        // Test selected node values
        assertTrue("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));
    }

    public void testBoxSelectWithTwoVxOneTxSelected(final boolean isAdd, final boolean isToggle) throws InterruptedException, PluginException, IOException {
        // 2 nodes and one transaction are to be selected.
        // vx1 and vx2, plus tx1
        // vx1 and vx2 should be within a box -0.1, 0.1, 0.1, -0.1
        left = (float) -0.1;
        right = (float) 0.1;
        top = (float) 0.1;
        bottom = (float) -0.1;

        createNewBox();
        boxSelect = new BoxSelectionPlugin(isAdd, isToggle, camera, box);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -1.0f);
        graph.setFloatValue(attrY, vxId2, -1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId2, false);

        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 3.0f);
        graph.setFloatValue(attrY, vxId3, 3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId3, false);

        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, -3.0f);
        graph.setFloatValue(attrY, vxId4, -3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId4, false);

        // Add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId4, false);

        graph.setBooleanValue(tSelectedAttrId, txId1, false);
        graph.setBooleanValue(tSelectedAttrId, txId2, false);

        // Test all selected attributes are false before execution
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));

        // Run plugin
        PluginExecution.withPlugin(boxSelect).executeNow(graph);

        // Test selected node values
        assertTrue("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));
    }

    /**
     * Run a box select plugin test
     *
     * @param isAdd
     * @param isToggle
     * @throws InterruptedException
     * @throws PluginException
     * @throws IOException
     */
    public void testBoxSelectWithTwoVxOneTxSecondaryAttributesSelected(final boolean isAdd, final boolean isToggle) throws InterruptedException, PluginException, IOException {
        // test box with transaction and with x2 y2 and z2
        // vxId1 and vxId2  + tx1 should be within a box -0.1, 0.1, 0.1, -0.1
        left = -0.1f;
        right = 0.1f;
        top = 0.1f;
        bottom = -0.1f;

        createNewBox();
        boxSelect = new BoxSelectionPlugin(isAdd, isToggle, camera, box);

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, -1.0f);
        graph.setFloatValue(attrY, vxId2, -1.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId2, false);

        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 3.0f);
        graph.setFloatValue(attrY, vxId3, 3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId3, false);

        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, -3.0f);
        graph.setFloatValue(attrY, vxId4, -3.0f);
        graph.setBooleanValue(vSelectedAttrId, vxId4, false);

        // Add transactions
        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId4, false);

        graph.setBooleanValue(tSelectedAttrId, txId1, false);
        graph.setBooleanValue(tSelectedAttrId, txId2, false);

        // Add x2, y2, z2 attributes
        VisualConcept.VertexAttribute.X2.ensure(graph);
        VisualConcept.VertexAttribute.Y2.ensure(graph);
        VisualConcept.VertexAttribute.Z2.ensure(graph);

        // Test all selected attributes are false before execution
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));

        // Run plugin
        PluginExecution.withPlugin(boxSelect).executeNow(graph);

        // Test selected node values
        assertTrue("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId1));
        assertTrue("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId2));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId3));
        assertFalse("Vx Visibility", graph.getBooleanValue(vSelectedAttrId, vxId4));

        assertTrue("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId1));
        assertFalse("Tx Visibility", graph.getBooleanValue(tSelectedAttrId, txId2));
    }
}

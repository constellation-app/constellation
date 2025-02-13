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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Quality Control Auto Vetter Test
 *
 * @author arcturus
 */
public class QualityControlAutoVetterNGTest {

    private Graph graph;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of updateQualityControlState method, of class
     * QualityControlAutoVetter.
     */
    @Test
    public void testUpdateQualityControlStateWithNoGraph() {
        graph = null;
        final QualityControlState stateBefore = QualityControlAutoVetter.getInstance().getQualityControlState();
        QualityControlAutoVetter.updateQualityControlState(graph);
        final QualityControlState stateAfter = QualityControlAutoVetter.getInstance().getQualityControlState();

        assertEquals(stateBefore, stateAfter);
    }

    /**
     * Test of getInstance method, of class QualityControlAutoVetter.
     */
    @Test
    public void testGetInstance() {
        final QualityControlAutoVetter instance1 = QualityControlAutoVetter.getInstance();
        final QualityControlAutoVetter instance2 = QualityControlAutoVetter.getInstance();
        assertEquals(instance1, instance2);
    }

    // Testing init with no graph open - commented out as TopComponent launches GUI panels which don't execute in the test environment
    @Test
    public void testInit() {
        QualityControlAutoVetter.destroyInstance();
        final QualityControlAutoVetter instance = QualityControlAutoVetter.getInstance();
        instance.init();
        assertNull(instance.getCurrentGraph());
        assertEquals(instance.getlastGlobalModCount(), (long) 0);
    }

    @Test
    public void testInitWithRefresh() {
        QualityControlAutoVetter.destroyInstance();
        final QualityControlAutoVetter instance = QualityControlAutoVetter.getInstance();
        instance.initWithRefresh(true);
        assertNull(instance.getCurrentGraph());
        assertEquals(instance.getlastGlobalModCount(), (long) 0);

    }

    // Test the adding and removing of observers and their behaviour to trigger the methods of the interface.
    @Test
    public void testAddRemoveObserver() throws InterruptedException {
        // add observer of the button state
        final TestObserver observer = new TestObserver();
        QualityControlAutoVetter.getInstance().addObserver(observer);

        // Check initial status
        assertFalse(observer.getCanRunStatus());

        QualityControlAutoVetter.updateQualityControlState(null);

        // Sleep until after pluginExecution thread has returned
        Thread.sleep(1000);

        // Check observer status
        assertTrue(observer.getCanRunStatus());

        // Reset status and recheck
        observer.setCanRunStatus(false);
        assertFalse(observer.getCanRunStatus());

        // Remove observer
        QualityControlAutoVetter.getInstance().removeObserver(observer);

        // Run update state
        QualityControlAutoVetter.updateQualityControlState(null);

        // Sleep until after pluginExecution thread has returned
        Thread.sleep(1000);

        // As it's not an observer it should remain false.
        assertFalse(observer.getCanRunStatus());
    }

    // Test the adding and removing of listeners and their behaviour to trigger the methods of the interface.
    @Test
    public void testAddRemoveListener() {
        // add observer of the button state
        final TestListener listener = new TestListener();
        QualityControlAutoVetter.getInstance().addListener(listener);

        // Check initial status
        assertFalse(listener.getStateChangedStatus());

        // Set the state changed
        QualityControlAutoVetter.getInstance().setQualityControlState(new QualityControlState(null, new ArrayList<>(), new ArrayList<>()));

        // Check observer status - Should have changed to true as the state has changed
        assertTrue(listener.getStateChangedStatus());

        // Reset status and recheck
        listener.setStateChangedStatus(false);
        assertFalse(listener.getStateChangedStatus());

        // Remove observer
        QualityControlAutoVetter.getInstance().removeListener(listener);

        // Set the state changed
        QualityControlAutoVetter.getInstance().setQualityControlState(new QualityControlState(null, new ArrayList<>(), new ArrayList<>()));

        // As it's not an observer it should remain false.
        assertFalse(listener.getStateChangedStatus());
    }

    // Test if multiple buttonlisteners get fired correctly within the update state.
    @Test
    public void testUpdateQualityControlState() throws InterruptedException {
        graph = null;

        // add observer1 of the button state
        QualityControlAutoVetter.destroyInstance();
        final TestObserver observer1 = new TestObserver();
        QualityControlAutoVetter.getInstance().addObserver(observer1);

        // add observer1 of the button state
        final TestObserver observer2 = new TestObserver();
        QualityControlAutoVetter.getInstance().addObserver(observer2);

        // Check initial status
        assertFalse(observer1.getCanRunStatus());
        assertFalse(observer2.getCanRunStatus());

        QualityControlAutoVetter.updateQualityControlState(graph);

        // Sleep until after pluginExecution thread has returned
        Thread.sleep(1000);

        // Check updated status
        assertTrue(observer1.getCanRunStatus());
        assertTrue(observer2.getCanRunStatus());
    }

    @Test
    public void testGraphChangedNoGraph() {
        QualityControlAutoVetter.destroyInstance();
        QualityControlAutoVetter instance = QualityControlAutoVetter.getInstance();

        assertNull(instance.getCurrentGraph());
        assertEquals(instance.getlastGlobalModCount(), (long) 0);
        assertEquals(instance.getlastCameraModCount(), (long) 0);

        QualityControlAutoVetter.getInstance().graphChanged(null);

        // check that no attribute mod counts have unnecesarily changed.
        assertNull(instance.getCurrentGraph());
        assertEquals(instance.getlastGlobalModCount(), (long) 0);
        assertEquals(instance.getlastCameraModCount(), (long) 0);
    }

    // Test commented out as TopComponent launches GUI panels which don't execute in the test environment
    @Test
    public void testGraphChangedWithGraph() throws InterruptedException {
        QualityControlAutoVetter.destroyInstance();
        QualityControlAutoVetter instance = QualityControlAutoVetter.getInstance();

        assertNull(instance.getCurrentGraph());
        assertEquals(instance.getlastGlobalModCount(), (long) 0);

        // Open a new graph
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        // Add camera attribute
        final WritableGraph wg = graph.getWritableGraph("TEST", true);
        try {
            final int cameraAttrId = VisualConcept.GraphAttribute.CAMERA.ensure(wg);
            // Change camera attribute
            final Camera camera = new Camera();
            camera.setVisibilityLow(0.67f);
            wg.setObjectValue(cameraAttrId, 0, camera);
        } finally {
            wg.commit();
        }

        // Sleep until after pluginExecution thread has returned
        Thread.sleep(1000);

        // Set the current graph
        instance.newActiveGraph(graph);

        QualityControlAutoVetter.getInstance().graphChanged(null);

        assertNotEquals(instance.getlastGlobalModCount(), (long) 0);
        assertNotEquals(instance.getlastCameraModCount(), (long) 0);

    }
}

class TestObserver implements QualityControlAutoVetterListener {

    protected boolean canRun = false;

    public TestObserver() {
        // Intentionally left blank - Test.
    }

    public boolean getCanRunStatus() {
        return canRun;
    }

    public void setCanRunStatus(final boolean canRun) {
        this.canRun = canRun;
    }

    @Override
    public void qualityControlRuleChanged(boolean canRun) {
        this.canRun = canRun;
    }
}

class TestListener implements QualityControlListener {

    protected boolean stateChanged = false;
    QualityControlState state;

    public TestListener() {
        // Intentionally left blank - Test.
    }

    public boolean getStateChangedStatus() {
        return stateChanged;
    }

    public void setStateChangedStatus(final boolean stateChanged) {
        this.stateChanged = stateChanged;
    }

    @Override
    public void qualityControlChanged(final QualityControlState state) {
        if (this.state != state) {
            stateChanged = true;
        }
        this.state = state;
    }
}

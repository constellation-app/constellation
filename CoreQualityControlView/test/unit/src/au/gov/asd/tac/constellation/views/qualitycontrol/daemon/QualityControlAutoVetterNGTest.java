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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
public class QualityControlAutoVetterNGTest {
    
    public QualityControlAutoVetterNGTest() {
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

//
//    /**
//     * Test of newActiveGraph method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testNewActiveGraph() {
//        System.out.println("newActiveGraph");
//        Graph graph = null;
//        QualityControlAutoVetter instance = null;
//        instance.newActiveGraph(graph);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of graphChanged method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testGraphChanged() {
//        System.out.println("graphChanged");
//        GraphChangeEvent event = null;
//        QualityControlAutoVetter instance = null;
//        instance.graphChanged(event);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of updateQualityControlState method, of class QualityControlAutoVetter.
     */
    @Test
    public void testUpdateQualityControlStateWithNoGraph() {
        final Graph graph = null;
        QualityControlAutoVetter.updateQualityControlState(graph);
    }

//    /**
//     * Test of getQualityControlState method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testGetQualityControlState() {
//        System.out.println("getQualityControlState");
//        QualityControlAutoVetter instance = null;
//        QualityControlState expResult = null;
//        QualityControlState result = instance.getQualityControlState();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setQualityControlState method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testSetQualityControlState() {
//        System.out.println("setQualityControlState");
//        QualityControlState state = null;
//        QualityControlAutoVetter instance = null;
//        instance.setQualityControlState(state);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addListener method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testAddListener() {
//        System.out.println("addListener");
//        QualityControlListener listener = null;
//        QualityControlAutoVetter instance = null;
//        instance.addListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of invokeListener method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testInvokeListener() {
//        System.out.println("invokeListener");
//        QualityControlListener listener = null;
//        QualityControlAutoVetter instance = null;
//        instance.invokeListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeListener method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testRemoveListener() {
//        System.out.println("removeListener");
//        QualityControlListener listener = null;
//        QualityControlAutoVetter instance = null;
//        instance.removeListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addObserver method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testAddObserver() {
//        System.out.println("addObserver");
//        QualityControlAutoVetterListener buttonListener = null;
//        QualityControlAutoVetter instance = null;
//        instance.addObserver(buttonListener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeObserver method, of class QualityControlAutoVetter.
//     */
//    @Test
//    public void testRemoveObserver() {
//        System.out.println("removeObserver");
//        QualityControlAutoVetterListener buttonListener = null;
//        QualityControlAutoVetter instance = null;
//        instance.removeObserver(buttonListener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of getInstance method, of class QualityControlAutoVetter.
     */
    @Test
    public void testGetInstance() {
        final QualityControlAutoVetter instance1 = QualityControlAutoVetter.getInstance();
        final QualityControlAutoVetter instance2 = QualityControlAutoVetter.getInstance();
        assertEquals(instance1, instance2);
    }
    
}

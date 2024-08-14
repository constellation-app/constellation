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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class DualGraphNGTest {
    
   @Test
    public void testGarbageCollection() {
        System.out.println("testGarbageCollection");
        final int numInstances = 5000;
        final String id = "id";

        try (MockedStatic<MemoryManager> mockMemoryManager = Mockito.mockStatic(MemoryManager.class, Mockito.CALLS_REAL_METHODS)) {
            for (int i = 0; i < numInstances; i++) {
                // Create graph and immediately overwrite its reference
                DualGraph graph = new DualGraph(null);
                assertEquals(graph.getClass(), DualGraph.class);
                graph = null;
                assertNull(graph);
            }

            // Hint garbage collection
            System.gc();

            // Verify instances were made, in this case 3 times numInstances as two lockingStoreGrapghs are made per instance
            mockMemoryManager.verify(() -> MemoryManager.newObject(any()), times(numInstances * 3));

            // Verify there are no remaining instances, because for some reason finalizeObject() verification doesn't work
            final MemoryManager.ClassStats stats = MemoryManager.getObjectCounts().get(DualGraph.class);
            assertEquals(stats.getCurrentCount(), 0);
        }
    }
//    /**
//     * Test of getId method, of class DualGraph.
//     */
//    @Test
//    public void testGetId() {
//        System.out.println("getId");
//        DualGraph instance = null;
//        String expResult = "";
//        String result = instance.getId();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSchema method, of class DualGraph.
//     */
//    @Test
//    public void testGetSchema() {
//        System.out.println("getSchema");
//        DualGraph instance = null;
//        Schema expResult = null;
//        Schema result = instance.getSchema();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addGraphChangeListener method, of class DualGraph.
//     */
//    @Test
//    public void testAddGraphChangeListener() {
//        System.out.println("addGraphChangeListener");
//        GraphChangeListener listener = null;
//        DualGraph instance = null;
//        instance.addGraphChangeListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeGraphChangeListener method, of class DualGraph.
//     */
//    @Test
//    public void testRemoveGraphChangeListener() {
//        System.out.println("removeGraphChangeListener");
//        GraphChangeListener listener = null;
//        DualGraph instance = null;
//        instance.removeGraphChangeListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getReadableGraph method, of class DualGraph.
//     */
//    @Test
//    public void testGetReadableGraph() {
//        System.out.println("getReadableGraph");
//        DualGraph instance = null;
//        ReadableGraph expResult = null;
//        ReadableGraph result = instance.getReadableGraph();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWritableGraph method, of class DualGraph.
//     */
//    @Test
//    public void testGetWritableGraph_String_boolean() throws Exception {
//        System.out.println("getWritableGraph");
//        String name = "";
//        boolean significant = false;
//        DualGraph instance = null;
//        WritableGraph expResult = null;
//        WritableGraph result = instance.getWritableGraph(name, significant);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWritableGraph method, of class DualGraph.
//     */
//    @Test
//    public void testGetWritableGraph_3args() throws Exception {
//        System.out.println("getWritableGraph");
//        String name = "";
//        boolean significant = false;
//        Object editor = null;
//        DualGraph instance = null;
//        WritableGraph expResult = null;
//        WritableGraph result = instance.getWritableGraph(name, significant, editor);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWritableGraphNow method, of class DualGraph.
//     */
//    @Test
//    public void testGetWritableGraphNow_String_boolean() {
//        System.out.println("getWritableGraphNow");
//        String name = "";
//        boolean significant = false;
//        DualGraph instance = null;
//        WritableGraph expResult = null;
//        WritableGraph result = instance.getWritableGraphNow(name, significant);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getWritableGraphNow method, of class DualGraph.
//     */
//    @Test
//    public void testGetWritableGraphNow_3args() {
//        System.out.println("getWritableGraphNow");
//        String name = "";
//        boolean significant = false;
//        Object editor = null;
//        DualGraph instance = null;
//        WritableGraph expResult = null;
//        WritableGraph result = instance.getWritableGraphNow(name, significant, editor);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setUndoManager method, of class DualGraph.
//     */
//    @Test
//    public void testSetUndoManager() {
//        System.out.println("setUndoManager");
//        UndoManager undoManager = null;
//        DualGraph instance = null;
//        instance.setUndoManager(undoManager);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}

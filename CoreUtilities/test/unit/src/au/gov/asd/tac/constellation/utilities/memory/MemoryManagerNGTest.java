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
package au.gov.asd.tac.constellation.utilities.memory;

import au.gov.asd.tac.constellation.utilities.memory.MemoryManager.ClassStats;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class MemoryManagerNGTest {
    
    TestMemoryManagerListener testListener;
    
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
        testListener = new TestMemoryManagerListener();
        MemoryManager.addMemoryManagerListener(testListener);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        MemoryManager.reset();
    }

    /**
     * Test of newObject method, of class MemoryManager.
     */
    @Test
    public void testNewObject() {
        System.out.println("newObject");
        
        final String testString1 = "Test String";
        final String testString2 = "Another Test String";
        final Integer testInteger = 1;
        
        MemoryManager.newObject(testString1.getClass());
        Map<Class<?>, ClassStats> objectCount = MemoryManager.getObjectCounts();
        final ClassStats string1Stats = objectCount.get(testString1.getClass());
        assertEquals(string1Stats.getCurrentCount(), 1);
        assertEquals(string1Stats.getMaxCount(), 1);
        assertEquals(string1Stats.getTotalCount(), 1);
        assertEquals(testListener.getObjectCount(), 1);
        
        MemoryManager.newObject(testString2.getClass());
        objectCount = MemoryManager.getObjectCounts();
        final ClassStats string2Stats = objectCount.get(testString2.getClass());
        assertEquals(string2Stats.getCurrentCount(), 2);
        assertEquals(string2Stats.getMaxCount(), 2);
        assertEquals(string2Stats.getTotalCount(), 2);
        assertEquals(testListener.getObjectCount(), 2);
        
        MemoryManager.newObject(testInteger.getClass());
        objectCount = MemoryManager.getObjectCounts();
        final ClassStats integerStats = objectCount.get(testInteger.getClass());
        assertEquals(integerStats.getCurrentCount(), 1);
        assertEquals(integerStats.getMaxCount(), 1);
        assertEquals(integerStats.getTotalCount(), 1);
        assertEquals(testListener.getObjectCount(), 3);
    }

    /**
     * Test of finalizeObject method, of class MemoryManager.
     */
    @Test
    public void testFinalizeObject() {
        System.out.println("finalizeObject");
        
        final String testString = "Test String";
        MemoryManager.newObject(testString.getClass());
        Map<Class<?>, ClassStats> objectCount = MemoryManager.getObjectCounts();
        ClassStats stringStats = objectCount.get(testString.getClass());
        assertEquals(stringStats.getCurrentCount(), 1);
        assertEquals(stringStats.getMaxCount(), 1);
        assertEquals(stringStats.getTotalCount(), 1);
        assertEquals(testListener.getObjectCount(), 1);
        
        MemoryManager.finalizeObject(testString.getClass());
        objectCount = MemoryManager.getObjectCounts();
        stringStats = objectCount.get(testString.getClass());
        assertEquals(stringStats.getCurrentCount(), 0);
        assertEquals(stringStats.getMaxCount(), 1);
        assertEquals(stringStats.getTotalCount(), 1);
        assertEquals(testListener.getObjectCount(), 0);
        
        // nothing should change as there are no objects to remove
        MemoryManager.finalizeObject(testString.getClass());
        objectCount = MemoryManager.getObjectCounts();
        stringStats = objectCount.get(testString.getClass());
        assertEquals(stringStats.getCurrentCount(), 0);
        assertEquals(stringStats.getMaxCount(), 1);
        assertEquals(stringStats.getTotalCount(), 1);
        assertEquals(testListener.getObjectCount(), 0);
    }

    /**
     * Test of addMemoryManagerListener method, of class MemoryManager.
     */
    @Test
    public void testAddMemoryManagerListener() {
        System.out.println("addMemoryManagerListener");
        
        final MemoryManagerListener listener1 = new TestMemoryManagerListener();
        final MemoryManagerListener listener2 = new TestMemoryManagerListener();
        
        MemoryManager.addMemoryManagerListener(listener1);
        assertEquals(MemoryManager.getListeners().size(), 2); //including the one added at method set up
        MemoryManager.addMemoryManagerListener(listener1);
        assertEquals(MemoryManager.getListeners().size(), 2);
        MemoryManager.addMemoryManagerListener(listener2);
        assertEquals(MemoryManager.getListeners().size(), 3);
        MemoryManager.addMemoryManagerListener(null);
        assertEquals(MemoryManager.getListeners().size(), 3);
    }

    /**
     * Test of removeMemoryManagerListener method, of class MemoryManager.
     */
    @Test
    public void testRemoveMemoryManagerListener() {
        System.out.println("removeMemoryManagerListener");
        
        final MemoryManagerListener listener = new TestMemoryManagerListener();
        MemoryManager.addMemoryManagerListener(listener);
        
        assertEquals(MemoryManager.getListeners().size(), 2); //including the one added at method set up
        MemoryManager.removeMemoryManagerListener(null);
        assertEquals(MemoryManager.getListeners().size(), 2);
        MemoryManager.removeMemoryManagerListener(listener);
        assertEquals(MemoryManager.getListeners().size(), 1);
    }
    
    private static class TestMemoryManagerListener implements MemoryManagerListener {
        
        private int objectCount = 0;

        public int getObjectCount() {
            return objectCount;
        }
        
        @Override
        public void newObject(final Class<?> c) {
            objectCount++;
        }

        @Override
        public void finalizeObject(final Class<?> c) {
            if (objectCount > 0) {
                objectCount--;
            }
        }       
    }
}

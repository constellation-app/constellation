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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class MultiMapNGTest {
    
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
     * Test of put method, of class MultiMap.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        
        final MultiMap<Integer, Integer> instance = new MultiMap<>((d1, d2) -> d2 - d1);
        
        assertEquals(instance.size(), 0);
        
        instance.put(0, 0);
        assertEquals(instance.size(), 1);
        
        final List<Integer> result1 = instance.get(0);
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0), (Integer) 0);
        
        // put an entry in an existing key
        instance.put(0, 3);
        assertEquals(instance.size(), 2);
        
        final List<Integer> result2 = instance.get(0);
        assertEquals(result2.size(), 2);
        assertEquals(result2.get(0), (Integer) 0);
        assertEquals(result2.get(1), (Integer) 3);
        
        // put an entry in a non-existing key
        instance.put(4, 2);
        assertEquals(instance.size(), 3);
        
        // confirm the list for key 0 remains unchanged
        final List<Integer> result31 = instance.get(0);
        assertEquals(result31.size(), 2);
        
        final List<Integer> result32 = instance.get(4);
        assertEquals(result32.size(), 1);
        assertEquals(result32.get(0), (Integer) 2);
    }

    /**
     * Test of hasNext method, of class MultiMap.MultiMapIterator.
     */
    @Test
    public void testHasNext() {
        System.out.println("hasNext");
        
        final MultiMap<Integer, Integer> instance = new MultiMap<>((d1, d2) -> d2 - d1);
        instance.put(0, 0);
        instance.put(0, 3);
        instance.put(4, 2);
        
        final Iterator<Entry<Integer, Integer>> iter = instance.entrySet().iterator();
        
        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }
    
    /**
     * Test of next method, of class MultiMap.MultiMapIterator.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        
        final MultiMap<Integer, Integer> instance = new MultiMap<>((d1, d2) -> d2 - d1);
        instance.put(0, 0);
        instance.put(0, 3);
        instance.put(4, 2);
        
        final Iterator<Entry<Integer, Integer>> iter = instance.entrySet().iterator();
        
        Entry<Integer, Integer> result = iter.next();
        assertEquals(result.getKey(), (Integer) 4);
        assertEquals(result.getValue(), (Integer) 2);
        
        result = iter.next();
        assertEquals(result.getKey(), (Integer) 0);
        assertEquals(result.getValue(), (Integer) 0);
        
        result = iter.next();
        assertEquals(result.getKey(), (Integer) 0);
        assertEquals(result.getValue(), (Integer) 3);
    }
}

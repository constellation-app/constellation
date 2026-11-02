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

import java.util.ArrayList;
import java.util.List;
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
public class ResizerNGTest {

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
     * Test of resize method, of class Resizer. Make list larger.
     */
    @Test
    public void testResizeLarger() {
        System.out.println("resizeLarger");
        
        final List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(4);
        intList.add(6);
        intList.add(2);
        
        Resizer.resize(intList, 6, 3);
        
        final List<Integer> expResult = new ArrayList<>();
        expResult.add(1);
        expResult.add(4);
        expResult.add(6);
        expResult.add(2);
        expResult.add(3);
        expResult.add(3);
        
        assertEquals(intList, expResult);
    }
    
    /**
     * Test of resize method, of class Resizer. Make list smaller.
     */
    @Test
    public void testResizeSmaller() {
        System.out.println("resizeSmaller");
        
        final List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(4);
        intList.add(6);
        intList.add(2);
        
        Resizer.resize(intList, 2, 3);
        
        final List<Integer> expResult = new ArrayList<>();
        expResult.add(1);
        expResult.add(4);
        
        assertEquals(intList, expResult);
    }
}

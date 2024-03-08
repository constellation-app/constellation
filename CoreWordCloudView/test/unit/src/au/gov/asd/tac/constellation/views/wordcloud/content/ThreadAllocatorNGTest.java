/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ThreadAllocator
 * 
 * @author Delphinus8821
 */
public class ThreadAllocatorNGTest extends ConstellationTest {
    
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

    /**
     * Test of calculateNumberOfThreads method, of class ThreadAllocator.
     */
    @Test
    public void testCalculateNumberOfThreads() {
        System.out.println("calculateNumberOfThreads");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final int expResult = 0;
        final int result = instance.calculateNumberOfThreads();
        assertEquals(result, expResult);
    }

    /**
     * Test of nextAdaptor method, of class ThreadAllocator.
     */
    @Test
    public void testNextAdaptor() {
        System.out.println("nextAdaptor");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final ThreadedPhraseAdaptor expResult = null;
        final ThreadedPhraseAdaptor result = instance.nextAdaptor();
        assertEquals(result, expResult);
    }

    /**
     * Test of hasMore method, of class ThreadAllocator.
     */
    @Test
    public void testHasMore() {
        System.out.println("hasMore");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final boolean expResult = true;
        final boolean result = instance.hasMore();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNumAllocated method, of class ThreadAllocator.
     */
    @Test
    public void testGetNumAllocated() {
        System.out.println("getNumAllocated");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final int expResult = 0;
        final int result = instance.getNumAllocated();
        assertEquals(result, expResult);
    }

    /**
     * Test of getLowerPos method, of class ThreadAllocator.
     */
    @Test
    public void testGetLowerPos() {
        System.out.println("getLowerPos");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final int expResult = 0;
        final int result = instance.getLowerPos();
        assertEquals(result, expResult);
    }

    /**
     * Test of getWorkload method, of class ThreadAllocator.
     */
    @Test
    public void testGetWorkload() {
        System.out.println("getWorkload");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final int expResult = -1;
        final int result = instance.getWorkload();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNumberOfThreads method, of class ThreadAllocator.
     */
    @Test
    public void testGetNumberOfThreads() {
        System.out.println("getNumberOfThreads");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final int expResult = -1;
        final int result = instance.getNumberOfThreads();
        assertEquals(result, expResult);
    }

    /**
     * Test of resetThreadAllocation method, of class ThreadAllocator.
     */
    @Test
    public void testResetThreadAllocation() {
        System.out.println("resetThreadAllocation");
        final int numberOfElements = 2;
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        final ThreadAllocator result = instance.resetThreadAllocation(numberOfElements);
        assertEquals(result.numberOfElements, numberOfElements);
    }

    /**
     * Test of indicateAllocated method, of class ThreadAllocator.
     */
    @Test
    public void testIndicateAllocated() {
        System.out.println("indicateAllocated");
        final ThreadAllocator instance = new ThreadAllocatorImpl();
        instance.indicateAllocated();
        assertEquals(1, instance.numAllocated);
    }

    /**
     * Test of buildThreadAllocator method, of class ThreadAllocator.
     */
    @Test
    public void testBuildThreadAllocator() {
        System.out.println("buildThreadAllocator");
        final int maxThreads = 2;
        final int maxElementsPerThread = 2;
        final int numOfElements = 4;
        final AdaptorFactory adaptorConnector = null;
        final ThreadAllocator result = ThreadAllocator.buildThreadAllocator(maxThreads, maxElementsPerThread, numOfElements, adaptorConnector);
        assertEquals(result.numberOfElements, numOfElements);

    }
    
 public class ThreadAllocatorImpl extends ThreadAllocator {

        @Override
        public int calculateNumberOfThreads() {
            return 0;
        }

        @Override
        public ThreadedPhraseAdaptor nextAdaptor() {
            return null;
        }
    }
    
}

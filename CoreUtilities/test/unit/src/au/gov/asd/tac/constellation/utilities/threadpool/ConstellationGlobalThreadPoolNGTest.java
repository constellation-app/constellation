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
package au.gov.asd.tac.constellation.utilities.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class ConstellationGlobalThreadPoolNGTest {

    private static final Logger LOGGER = Logger.getLogger(ConstellationGlobalThreadPoolNGTest.class.getName());
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
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
     * Test of getThreadPool method, of class ConstellationGlobalThreadPool.
     *
     * Making sure the thread pool object has only one instance being created
     */
    @Test
    public void testGetThreadPool() {
        System.out.println("getThreadPool");
        ConstellationGlobalThreadPool c1 = ConstellationGlobalThreadPool.getThreadPool();
        ConstellationGlobalThreadPool c2 = ConstellationGlobalThreadPool.getThreadPool();
        assertEquals(c1, c2);
    }

    /**
     * Test of getScheduledExecutorService method, of class
     * ConstellationGlobalThreadPool.
     *
     * Making sure the scheduled executor service object has only one instance
     * being created
     */
    @Test
    public void testGetScheduledExecutorService() {
        System.out.println("getScheduledExecutorService");
        ScheduledExecutorService s1 = ConstellationGlobalThreadPool.getThreadPool().getScheduledExecutorService();
        ScheduledExecutorService s2 = ConstellationGlobalThreadPool.getThreadPool().getScheduledExecutorService();
        assertEquals(s1, s2);
    }

    /**
     * Test of getFixedThreadPool method, of class
     * ConstellationGlobalThreadPool.
     *
     * The fixed thread pool object also only has one instance which is being
     * tested here
     */
    @Test
    public void testGetFixedThreadPool() {
        System.out.println("getFixedThreadPool");
        ExecutorService f1 = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool();
        ExecutorService f2 = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool();
        assertEquals(f1, f2);
    }

    /**
     * Test of getCachedThreadPool method, of class
     * ConstellationGlobalThreadPool.
     *
     * Same purpose as the tests above
     */
    @Test
    public void testGetCachedThreadPool() {
        System.out.println("getCachedThreadPool");
        ExecutorService e1 = ConstellationGlobalThreadPool.getThreadPool().getCachedThreadPool();
        ExecutorService e2 = ConstellationGlobalThreadPool.getThreadPool().getCachedThreadPool();
        assertEquals(e1, e2);
    }
}

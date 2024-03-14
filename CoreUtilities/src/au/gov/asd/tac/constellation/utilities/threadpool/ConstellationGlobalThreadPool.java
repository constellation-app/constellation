/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class holds global objects for the various thread pools needed by the
 * application
 *
 * @author altair1673
 */
public class ConstellationGlobalThreadPool {

    private static ConstellationGlobalThreadPool threadPool = null;
    private ScheduledExecutorService scheduledExecutorService = null;
    private ExecutorService fixedThreadPool = null;
    private ExecutorService cachedThreadPool = null;

    private ConstellationGlobalThreadPool() {
    }

    /**
     * Static function to only get one instance of this object
     *
     * @return an instance of this class
     */
    public static ConstellationGlobalThreadPool getThreadPool() {
        if (threadPool == null) {
            threadPool = new ConstellationGlobalThreadPool();
        }

        return threadPool;

    }

    /**
     * Instantiates exactly 1 ScheduledExecutorService
     *
     * @return a ScheduledExecutorService
     */
    public ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(5);
        }

        return scheduledExecutorService;
    }

    /**
     * Instantiates exactly 1 FixedThreadPool containing all available threads
     *
     * @return a FixedThreadPool objects
     */
    public ExecutorService getFixedThreadPool() {
        if (fixedThreadPool == null) {
            fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        
        return fixedThreadPool;
    }
    
    /**
     * Instantiates exactly 1 FixedThreadPool containing all available threads.
     * This fixed thread pool will not be referenced by this utility class.
     * It is the responsibility fo the calling class to monitor and shutdown this ExecutrService as needed
     *
     * @param poolName
     * @return a FixedThreadPool objects
     */
    public ExecutorService getFixedThreadPool(final String poolName) {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ConstellationThreadFactory(poolName));
    }

    /**
     * Creates only 1 CachedThreadPool
     *
     * @return a CachedThreadPool object
     */
    public ExecutorService getCachedThreadPool() {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }

        return cachedThreadPool;
    }
}

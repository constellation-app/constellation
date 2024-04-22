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
package au.gov.asd.tac.constellation.utilities.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The MemoryManager records statistics on the creation and deletion of
 * instances of participating classes. This is mainly of use to developers
 * interested in detecting memory leaks. It is up to a specific class to send
 * new and finalize information to the MemoryManager.
 *
 * @author sirius
 */
public class MemoryManager {

    private static final Map<Class<?>, ClassStats> OBJECT_COUNTS = new HashMap<>();

    // The listeners currently registered
    private static final List<MemoryManagerListener> LISTENERS = new ArrayList<>();
    
    /**
     * Registers that a new instance of the specified class has been created.
     *
     * @param c an instance of this class has been created.
     */
    public static void newObject(final Class<?> c) {
        synchronized (OBJECT_COUNTS) {
            ClassStats stats = OBJECT_COUNTS.get(c);
            if (stats == null) {
                stats = new ClassStats();
                OBJECT_COUNTS.put(c, stats);
            }

            stats.currentCount++;
            stats.totalCount++;
            stats.maxCount = Math.max(stats.maxCount, stats.currentCount);
        }

        synchronized (LISTENERS) {
            LISTENERS.stream().forEach(listener -> listener.newObject(c));
        }
    }

    /**
     * Registers that an instance of the specified class has been finalized.
     *
     * @param c an instance of this class has been finalized.
     */
    public static void finalizeObject(final Class<?> c) {
        synchronized (OBJECT_COUNTS) {
            ClassStats stats = OBJECT_COUNTS.get(c);
            if (stats == null) {
                stats = new ClassStats();
                OBJECT_COUNTS.put(c, stats);
            }

            if (stats.currentCount > 0) {
                stats.currentCount--;
            }
        }

        synchronized (LISTENERS) {
            LISTENERS.stream().forEach(listener -> listener.finalizeObject(c));
        }
    }

    /**
     * Returns the ClassStats for all registered classes. The returned Map is a
     * copy meaning that it can be mutated as required with out effecting the
     * MemoryManager.
     *
     * @return the ClassStats for all registered classes.
     */
    public static Map<Class<?>, ClassStats> getObjectCounts() {
        synchronized (OBJECT_COUNTS) {
            return new HashMap<>(OBJECT_COUNTS);
        }
    }
    
    /**
     * Returns a copy of all the currently registered listeners.
     * 
     * @return all the registered listeners
     */
    protected static List<MemoryManagerListener> getListeners() {
        synchronized (LISTENERS) {
            return new ArrayList<>(LISTENERS);
        }
    }
    
    /**
     * Adds a new listener to this MemoryManager.
     *
     * @param listener the listener to be added.
     */
    public static void addMemoryManagerListener(final MemoryManagerListener listener) {
        synchronized (LISTENERS) {
            if (listener != null && !LISTENERS.contains(listener)) {
                LISTENERS.add(listener);
            }
        }
    }

    /**
     * Removes a listener from this MemoryManager.
     *
     * @param listener the listener to be removed.
     */
    public static void removeMemoryManagerListener(final MemoryManagerListener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }
    
    /**
     * Clears the object counts and listeners. 
     * NOTE: This function is only added to enable proper unit testing of this class and probably shouldn't be used otherwise
     */
    protected static void reset() {
        synchronized (OBJECT_COUNTS) {
            OBJECT_COUNTS.clear();
        }
        
        synchronized (LISTENERS) {
            LISTENERS.clear();
        }
    }  
    
    public static class ClassStats {

        private int currentCount = 0;
        private int maxCount = 0;
        private int totalCount = 0;

        public int getCurrentCount() {
            return currentCount;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }
}

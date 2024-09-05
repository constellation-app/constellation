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
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
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
}

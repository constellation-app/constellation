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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.WritableGraph;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 * Nested Locking Test.
 *
 * @author twilight_sparkle
 */
public class NestedLockingNGTest {

    @Test
    public void unmodifiedRollbackControlTest() throws InterruptedException {
        final DualGraph g = new DualGraph(null);
        // Get the first write lock, modify the graph, then commit
        final WritableGraph wg2 = g.getWritableGraph("2", true);
        wg2.addVertex();
        wg2.commit();

        // Get the second write lock and immediately rollback
        final WritableGraph wg3 = g.getWritableGraph("3", true);
        final long modCount = wg3.getGlobalModificationCounter();
        wg3.rollBack();

        // Check that the modcount prior to rollingback the second write lock is the same as the current modcount in a new lock.
        final WritableGraph wg4 = g.getWritableGraph("4", true);
        assertEquals(modCount, wg4.getGlobalModificationCounter());
        wg4.commit();
    }

    @Test
    public void unmodifiedRollbackNestedTest() {
        final DualGraph g = new DualGraph(null);
        try {
            // Get an outer write lock
            final WritableGraph wg1 = g.getWritableGraph("1", true);

            // Get the first inner write lock, modify the graph, then commit
            final WritableGraph wg2 = g.getWritableGraph("2", true);
            wg2.addVertex();
            wg2.commit();

            // Get the second inner write lock and immediately rollback
            final WritableGraph wg3 = g.getWritableGraph("3", true);
            final long modCount = wg3.getGlobalModificationCounter();
            wg3.rollBack();

            // Commit the outer write lock
            wg1.commit();

            // Check that the modcount prior to rollingback the second inner write lock is the same as the current modcount in a new lock.
            final WritableGraph wg4 = g.getWritableGraph("4", true);
            assertEquals(modCount, wg4.getGlobalModificationCounter());
            wg4.commit();
        } catch (InterruptedException ex) {
            assertTrue("Locking Interrupted", false);
        }
    }

    @Test
    public void modifiedRollbackControlTest() throws InterruptedException {
        final DualGraph g = new DualGraph(null);
        // Get the first write lock, modify the graph, then commit
        final WritableGraph wg2 = g.getWritableGraph("2", true);
        wg2.addVertex();
        wg2.commit();

        // Get the second write lock, modify the graph, then rollback
        final WritableGraph wg3 = g.getWritableGraph("3", true);
        final long modCount = wg3.getGlobalModificationCounter();
        wg3.addVertex();
        wg3.rollBack();

        // Check that the modcount prior to modifying the second write lock is the same as the current modcount in a new lock.
        final WritableGraph wg4 = g.getWritableGraph("4", true);
        assertEquals(modCount, wg4.getGlobalModificationCounter());
        wg4.commit();
    }

    @Test
    public void modifiedRollbackNestedTest() {
        final DualGraph g = new DualGraph(null);
        try {
            // Get an outer write lock
            final WritableGraph wg1 = g.getWritableGraph("1", true);

            // Get the first inner write lock, modify the graph, then commit
            final WritableGraph wg2 = g.getWritableGraph("2", true);
            wg2.addVertex();
            wg2.commit();

            // Get the second inner write lock, modify the graph, then rollback
            final WritableGraph wg3 = g.getWritableGraph("3", true);
            final long modCount = wg3.getGlobalModificationCounter();
            wg3.addVertex();
            wg3.rollBack();

            // Commit the outer write lock
            wg1.commit();

            // Check that the modcount prior to modifying the second inner write lock is the same as the current modcount in a new lock.
            final WritableGraph wg4 = g.getWritableGraph("4", true);
            assertEquals(modCount, wg4.getGlobalModificationCounter());
            wg4.commit();
        } catch (InterruptedException ex) {
            assertTrue("Locking Interrupted", false);
        }
    }

    @Test
    public void unmodifiedCommitControlTest() throws InterruptedException {
        final DualGraph g = new DualGraph(null);
        // Get the first write lock, modify the graph, then commit
        final WritableGraph wg2 = g.getWritableGraph("2", true);
        wg2.addVertex();
        wg2.commit();

        // Get the second write lock and immediately commit
        final WritableGraph wg3 = g.getWritableGraph("3", true);
        final long modCount = wg3.getGlobalModificationCounter();
        wg3.commit();

        // Check that the modcount prior to commiting the second write lock is the same as the current modcount in a new lock.
        final WritableGraph wg4 = g.getWritableGraph("4", true);
        assertEquals(modCount, wg4.getGlobalModificationCounter());
        wg4.commit();
    }

    @Test
    public void unmodifiedCommitNestedTest() {
        final DualGraph g = new DualGraph(null);
        try {
            // Get an outer write lock
            final WritableGraph wg1 = g.getWritableGraph("1", true);

            // Get the first inner write lock, modify the graph, then commit
            final WritableGraph wg2 = g.getWritableGraph("2", true);
            wg2.addVertex();
            wg2.commit();

            // Get the second inner write lock and immediately commit
            final WritableGraph wg3 = g.getWritableGraph("3", true);
            final long modCount = wg3.getGlobalModificationCounter();
            wg3.commit();

            // Commit the outer write lock
            wg1.commit();

            // Check that the modcount prior to commiting the second inner write lock is the same as the current modcount in a new lock.
            final WritableGraph wg4 = g.getWritableGraph("4", true);
            assertEquals(modCount, wg4.getGlobalModificationCounter());
            wg4.commit();
        } catch (InterruptedException ex) {
            assertTrue("Locking Interrupted", false);
        }
    }

    @Test
    public void modifiedCommitControlTest() throws InterruptedException {
        final DualGraph g = new DualGraph(null);
        // Get the first write lock, modify the graph, then commit
        final WritableGraph wg2 = g.getWritableGraph("2", true);
        wg2.addVertex();
        wg2.commit();

        // Get the second write lock, modify the graph, then commit
        final WritableGraph wg3 = g.getWritableGraph("3", true);
        wg3.addVertex();
        final long modCount = wg3.getGlobalModificationCounter();
        wg3.commit();

        // Check that the modcount prior to commiting the second write lock is the same as the current modcount in a new lock.
        final WritableGraph wg4 = g.getWritableGraph("4", true);
        assertEquals(modCount, wg4.getGlobalModificationCounter());
        wg4.commit();
    }

    @Test
    public void modifiedCommitNestedTest() {
        final DualGraph g = new DualGraph(null);
        try {
            // Get an outer write lock
            final WritableGraph wg1 = g.getWritableGraph("1", true);

            // Get the first inner write lock, modify the graph, then commit
            final WritableGraph wg2 = g.getWritableGraph("2", true);
            wg2.addVertex();
            wg2.commit();

            // Get the second inner write lock, modify the graph, then commit
            final WritableGraph wg3 = g.getWritableGraph("3", true);
            wg3.addVertex();
            final long modCount = wg3.getGlobalModificationCounter();
            wg3.commit();

            // Commit the outer write lock
            wg1.commit();

            // Check that the modcount prior to commiting the second inner write lock is the same as the current modcount in a new lock.
            final WritableGraph wg4 = g.getWritableGraph("4", true);
            assertEquals(modCount, wg4.getGlobalModificationCounter());
            wg4.commit();
        } catch (InterruptedException ex) {
            assertTrue("Locking Interrupted", false);
        }
    }
}

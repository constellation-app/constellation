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
package au.gov.asd.tac.constellation.plugins;

/**
 * A PluginSynchronizer synchronizes the running of multiple plugins on
 * different threads. Plugins can divide their execution into a number of life
 * cycle stages and the PluginSynchronizer ensures that the corresponding life
 * cycle stage of all plugins has finished before the next stage can begin for
 * any plugin in the group.
 *
 * @author sirius
 */
public class PluginSynchronizer {

    private final Object lock = new Object();

    private volatile int remainingPluginCount;
    private volatile int outstandingPluginCount;
    private volatile int currentGateNumber = 0;

    /**
     * Create a new PluginSynchronizer for a group of plugins of the specified
     * size.
     *
     * @param pluginCount the number of plugins in the group to be synchronized.
     */
    public PluginSynchronizer(int pluginCount) {
        remainingPluginCount = outstandingPluginCount = pluginCount;
    }

    /**
     * Called by plugins to wait for other plugins in the group at a specified
     * gate/life cycle stage. Plugin execution will wait until all plugins in
     * the group are also waiting at this gate.
     *
     * @param gate the gate to wait at.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void waitForGate(int gate) throws InterruptedException {
        synchronized (lock) {
            for (int g = currentGateNumber; g <= gate; g++) {

                // If this is the last thread to wait for this gate...
                if (--outstandingPluginCount == 0) {
                    currentGateNumber++;
                    outstandingPluginCount = remainingPluginCount;
                    lock.notifyAll();

                    // Else wait for the remaining threads
                } else {
                    while (currentGateNumber == g && outstandingPluginCount > 0) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ex) {
                            remainingPluginCount--;
                            throw ex;
                        }
                    }
                }
            }
        }
    }

    /**
     * Called by plugins to indicate that they have finished execution. This
     * will mean that they are no longer waited for at any gate.
     */
    public void finished() {
        synchronized (lock) {
            remainingPluginCount--;
            if (--outstandingPluginCount == 0) {
                currentGateNumber++;
                outstandingPluginCount = remainingPluginCount;
                lock.notifyAll();
            }
        }
    }
}

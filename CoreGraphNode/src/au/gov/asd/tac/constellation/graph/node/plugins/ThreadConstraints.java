/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;

/**
 * constraints for threads
 *
 * @author sirius
 */
public class ThreadConstraints {

    private static final ThreadLocal<ThreadConstraints> THREAD_LOCAL = new ThreadLocal<>();

    private PluginReport currentReport;
    private boolean alwaysSilent;
    private int silentCount;

    public static ThreadConstraints getConstraints() {
        ThreadConstraints constraints = THREAD_LOCAL.get();
        if (constraints == null) {
            constraints = new ThreadConstraints();
            THREAD_LOCAL.set(constraints);
        }
        return constraints;
    }

    public PluginReport getCurrentReport() {
        return currentReport;
    }

    public void setCurrentReport(PluginReport currentReport) {
        this.currentReport = currentReport;
    }

    public boolean isAlwaysSilent() {
        return alwaysSilent;
    }

    public void setAlwaysSilent(final boolean alwaysSilent) {
        this.alwaysSilent = alwaysSilent;
    }

    public int getSilentCount() {
        return silentCount;
    }

    public void setSilentCount(final int silentCount) {
        this.silentCount = silentCount;
    }
    
    /**
     * THREAD_LOCAL should be cleaned up when no longer in use.
     * This method should be used when each thread is completed to 
     * ensure that the THREAD_LOCAL field is cleaned up.
     * This assists with garbage collection.
     */
    public void unload() {
        THREAD_LOCAL.remove();
    }
}

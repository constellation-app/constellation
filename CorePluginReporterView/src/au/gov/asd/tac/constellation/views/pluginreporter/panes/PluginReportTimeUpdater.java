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
package au.gov.asd.tac.constellation.views.pluginreporter.panes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * The PluginReportTimeUpdater is responsible for updating the timer on each plugin report to accurately reflect the
 * amount of time that plugin has been running.
 *
 * @author sirius
 */
public class PluginReportTimeUpdater {

    private static final Set<PluginReportPane> ACTIVE_REPORTS = new LinkedHashSet<>();
    private static final String PLUGIN_REPORTER_THREAD_NAME = "Plugin Reporter Time Updater";

    private static TimerThread timer = null;

    private static boolean isUpdating = false;
    private static final List<PluginReportPane> REPORTS_TO_REMOVE_CACHE = new ArrayList<>();

    private PluginReportTimeUpdater() {
        throw new IllegalStateException("Utility class");
    }

    private static final Runnable UPDATE_PANES = () -> {
        synchronized (ACTIVE_REPORTS) {
            isUpdating = true;
            Set<PluginReportPane> panesToUpdate = new HashSet<>(ACTIVE_REPORTS);
            for (PluginReportPane pane : panesToUpdate) {
                pane.updateTime();
            }
            isUpdating = false;
            for (PluginReportPane pane : REPORTS_TO_REMOVE_CACHE) {
                removePluginReport(pane);
            }
        }
    };

    private static class TimerThread extends Thread {

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        final Runnable updatePanesRunnable = new Runnable() {
            public void run() {
                Platform.runLater(UPDATE_PANES);
            }
        };

        @Override
        public void run() {
            setName(PLUGIN_REPORTER_THREAD_NAME);
            // Run every 1 second with no initial delay
            executor.scheduleAtFixedRate(updatePanesRunnable, 0, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Add a plugin report to the set of plugin reports that will have their timers updated by this
     * PluginReportTimeUpdater.
     *
     * @param pane the PluginReportPane to add.
     */
    public static void addPluginReport(PluginReportPane pane) {
        synchronized (ACTIVE_REPORTS) {
            if (ACTIVE_REPORTS.add(pane) && timer == null) {
                timer = new TimerThread();
                timer.start();
            }
        }
    }

    /**
     * Remove a plugin report from the set of plugin reports that will have their timers updated by this
     * PluginReportTimeUpdater.
     *
     * @param pane the PluginReportPane to remove.
     */
    public static void removePluginReport(PluginReportPane pane) {
        synchronized (ACTIVE_REPORTS) {
            if (isUpdating) {
                REPORTS_TO_REMOVE_CACHE.add(pane);
                return;
            }
            if (ACTIVE_REPORTS.remove(pane) && ACTIVE_REPORTS.isEmpty()) {
                timer.interrupt();
                timer = null;
            }
        }
    }
}

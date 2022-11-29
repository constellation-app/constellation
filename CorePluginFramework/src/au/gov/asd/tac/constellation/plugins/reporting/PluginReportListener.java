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
package au.gov.asd.tac.constellation.plugins.reporting;

/**
 * A PluginReportListener is registered against one or more PluginReports and is
 * notified whenever that PluginReport changes.
 */
public interface PluginReportListener {

    /**
     * A PluginReport that this listener is registered with has changed.
     *
     * @param pluginReport the PluginReport that has changed.
     */
    public void pluginReportChanged(PluginReport pluginReport);

    /**
     * A PluginReport that this listener is registered with has had a new child
     * plugin report added.
     *
     * @param parentReport the parent PluginReport that has gained a child
     * PluginReport.
     * @param childReport the PluginReport that was added as a child.
     */
    public void addedChildReport(PluginReport parentReport, PluginReport childReport);
}

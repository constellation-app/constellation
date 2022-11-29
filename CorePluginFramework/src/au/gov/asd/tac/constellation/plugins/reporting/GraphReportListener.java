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
 * A listener that gets alerted whenever a new plugin report is created.
 *
 * All reports for all graphs are sent to each listener so it is up to the
 * listener to filter out reports for a particular graph if that is what is
 * required.
 *
 * @author sirius
 */
public interface GraphReportListener {

    /**
     * Called by the plugin reporter framework to advertise that a new
     * {@link PluginReport} has been created.
     *
     * @param pluginReport the newly created {@link PluginReport}.
     */
    public void newPluginReport(PluginReport pluginReport);

}

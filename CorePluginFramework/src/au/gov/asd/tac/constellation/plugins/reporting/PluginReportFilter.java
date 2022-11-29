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
 * A PluginReportFilter allows the user to see only a subset of all the
 * available plugin reports for a graph.
 *
 * @author sirius
 */
public interface PluginReportFilter {

    /**
     * Called by the plugin reporter framework for each plugin report to
     * determine if that plugin report should be used.
     *
     * @param report the PluginReport to be filtered.
     *
     * @return true if the given PluginReport should be used.
     */
    public boolean includePluginReport(PluginReport report);
}

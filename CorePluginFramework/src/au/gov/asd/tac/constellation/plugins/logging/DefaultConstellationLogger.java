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
package au.gov.asd.tac.constellation.plugins.logging;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * A default implementation of a ConstellationLogger that ignores all logging
 * messages by default. You can set {@link DefaultConstellationLogger#VERBOSE}
 * to true to enable logging to standard out.
 *
 * @author sirius
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationLogger.class, position = Integer.MAX_VALUE)
public class DefaultConstellationLogger implements ConstellationLogger {

    private static final Logger LOGGER = Logger.getLogger(DefaultConstellationLogger.class.getName());

    @Override
    public void applicationStarted() {
        LOGGER.log(Level.FINE, "Application Started");
    }

    @Override
    public void applicationStopped() {
        LOGGER.log(Level.FINE, "Application Stopped");
    }

    @Override
    public void viewStarted(final TopComponent view) {
        LOGGER.log(Level.FINE, "View Started: {0}", view.getName());
    }

    @Override
    public void viewStopped(final TopComponent view) {
        LOGGER.log(Level.FINE, "View Stopped: {0}", view.getName());
    }

    @Override
    public void viewInfo(final TopComponent view, final String info) {
        LOGGER.log(Level.FINE, "View Info: {0}: {1}", new Object[]{view.getName(), info});
    }

    @Override
    public void pluginStarted(final Plugin plugin, final PluginParameters parameters, final Graph graph) {
        LOGGER.log(Level.FINE, "Plugin Started: {0}", plugin.getName());
    }

    @Override
    public void pluginStopped(final Plugin plugin, final PluginParameters parameters) {
        LOGGER.log(Level.FINE, "Plugin Stopped: {0}", plugin.getName());
    }

    @Override
    public void pluginInfo(final Plugin plugin, final String info) {
        LOGGER.log(Level.FINE, "Plugin Info: {0}: {1}", new Object[]{plugin.getName(), info});
    }

    @Override
    public void pluginError(final Plugin plugin, final Throwable error) {
        LOGGER.log(Level.FINE, "Plugin Error: {0}: {1}", new Object[]{plugin.getName(), error.getMessage()});
    }

    @Override
    public void pluginProperties(final Plugin plugin, final Properties properties) {
        LOGGER.log(Level.FINE, "Plugin Properties: {0}: {1}", new Object[]{plugin.getName(), properties});
    }
}

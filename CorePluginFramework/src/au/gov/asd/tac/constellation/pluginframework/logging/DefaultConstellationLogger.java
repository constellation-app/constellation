/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.pluginframework.logging;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * A default implementation of a ConstellationLogger that ignores all logging
 * messages. This class exists because the framework expects that the logger is
 * not null and this class provides a no-op implementation that provides a null
 * equivalent.
 *
 * @author sirius
 */
@ServiceProvider(service = ConstellationLogger.class, position = Integer.MAX_VALUE)
public class DefaultConstellationLogger implements ConstellationLogger {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultConstellationLogger.class.getName());

    @Override
    public void applicationStarted() {
        LOGGER.log(Level.INFO, "Application Started");
    }

    @Override
    public void applicationStopped() {
        LOGGER.log(Level.INFO, "Application Stopped");
    }

    @Override
    public void viewOpened(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Opened: {0}", view.getName());
    }

    @Override
    public void viewClosed(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Closed: {0}", view.getName());
    }
    
    @Override
    public void viewShowing(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Showing: {0}", view.getName());
    }
    
    @Override
    public void viewHidden(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Hidden: {0}", view.getName());
    }
    
    @Override
    public void viewActivated(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Activated: {0}", view.getName());
    }
    
    @Override
    public void viewDeactivated(final TopComponent view) {
        LOGGER.log(Level.INFO, "View Deactivated: {0}", view.getName());
    }

    @Override
    public void pluginStarted(final Plugin plugin, final PluginParameters parameters, final Graph graph) {
        LOGGER.log(Level.INFO, "Plugin Started: {0}", plugin.getName());
    }

    @Override
    public void pluginStopped(final Plugin plugin, final PluginParameters parameters) {
        LOGGER.log(Level.INFO, "Plugin Stopped: {0}", plugin.getName());
    }

    @Override
    public void pluginInfo(final Plugin plugin, final String info) {
        LOGGER.log(Level.INFO, "Plugin Info: {0}\n\t{1}", new Object[]{plugin.getName(), info});
    }

    @Override
    public void pluginError(final Plugin plugin, final Throwable error) {
        LOGGER.log(Level.INFO, "Plugin Error: {0}\n\t{1}", new Object[]{plugin.getName(), error.getMessage()});
    }

    @Override
    public void pluginProperties(final Plugin plugin, final Properties properties) {
        LOGGER.log(Level.INFO, "Plugin Properties: {0}\n\t{1}", new Object[]{plugin.getName(), properties.toString()});
    }
}

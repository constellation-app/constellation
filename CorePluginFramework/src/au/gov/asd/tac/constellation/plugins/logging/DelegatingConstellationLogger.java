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
package au.gov.asd.tac.constellation.plugins.logging;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * This ConstellationLogger holds a number of other ConstellationLoggers and
 * relays the information it receives to each of these loggers.
 *
 * @author arcturus
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationLogger.class, position = 0)
public class DelegatingConstellationLogger implements ConstellationLogger {

    private List<ConstellationLogger> loggers = null;

    private synchronized void init() {
        if (loggers == null) {
            loggers = new ArrayList<>(Lookup.getDefault().lookupAll(ConstellationLogger.class));
            loggers.remove(this);
        }
    }

    @Override
    public void applicationStarted() {
        init();
        loggers.forEach(logger -> logger.applicationStarted());
    }

    @Override
    public void applicationStopped() {
        init();
        loggers.forEach(logger -> logger.applicationStopped());
    }

    @Override
    public void viewStarted(final TopComponent view) {
        init();
        loggers.forEach(logger -> logger.viewStarted(view));
    }

    @Override
    public void viewStopped(final TopComponent view) {
        init();
        loggers.forEach(logger -> logger.viewStopped(view));
    }

    @Override
    public void viewInfo(final TopComponent view, final String info) {
        init();
        loggers.forEach(logger -> logger.viewInfo(view, info));
    }

    @Override
    public void pluginStarted(final Plugin plugin, final PluginParameters parameters, final Graph graph) {
        init();
        loggers.forEach(logger -> logger.pluginStarted(plugin, sanitiseParameters(parameters), graph));
    }

    @Override
    public void pluginStopped(final Plugin plugin, final PluginParameters parameters) {
        init();
        loggers.forEach(logger -> logger.pluginStopped(plugin, sanitiseParameters(parameters)));
    }

    @Override
    public void pluginInfo(final Plugin plugin, final String info) {
        init();
        loggers.forEach(logger -> logger.pluginInfo(plugin, info));
    }

    @Override
    public void pluginError(final Plugin plugin, final Throwable error) {
        init();
        loggers.forEach(logger -> logger.pluginError(plugin, error));
    }

    @Override
    public void pluginProperties(final Plugin plugin, final Properties properties) {
        init();
        loggers.forEach(logger -> logger.pluginProperties(plugin, properties));
    }

    /**
     * Sanitise parameters, including removing passwords.
     *
     * @param parameters The parameters to sanitise.
     * @return a sanitised copy of the parameters.
     */
    private static PluginParameters sanitiseParameters(final PluginParameters parameters) {
        if (parameters == null) {
            return null;
        }

        final PluginParameters sanitisedParameters = parameters.copy();
        final Map<String, PluginParameter<?>> parametersMap = sanitisedParameters.getParameters();
        parametersMap.keySet().stream()
                .filter(key -> (parametersMap.get(key).getType().getId().equals(PasswordParameterType.ID)))
                .forEachOrdered(key -> parametersMap.get(key).setStringValue("*******"));

        return sanitisedParameters;
    }
}

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
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.PasswordParameterType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * This ConstellationLogger holds a number of other ConstellationLoggers and
 * relays the information it receives to each of these loggers.
 *
 * @author arcturus
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
    public void applicationStart() {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.applicationStart();
        }
    }

    @Override
    public void applicationStop() {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.applicationStop();
        }
    }

    @Override
    public void pluginStart(final Graph graph, final Plugin plugin, final PluginParameters parameters) {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.pluginStart(graph, plugin, sanitiseParameters(parameters));
        }
    }

    @Override
    public void pluginStop(final Plugin plugin, final PluginParameters parameters) {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.pluginStop(plugin, sanitiseParameters(parameters));
        }
    }

    @Override
    public void pluginInfo(final Plugin plugin, final String info) {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.pluginInfo(plugin, info);
        }
    }

    @Override
    public void pluginError(final Plugin plugin, final Throwable error) {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.pluginError(plugin, error);
        }
    }

    @Override
    public void pluginProperties(final Plugin plugin, final Properties properties) {
        init();
        for (ConstellationLogger logger : loggers) {
            logger.pluginProperties(plugin, properties);
        }
    }

    /**
     * Sanitise the parameters like hashing out passwords
     *
     * @param parameters The parameters to sanitise
     * @return A sanitised copy of the parameters
     */
    private static PluginParameters sanitiseParameters(final PluginParameters parameters) {
        if (parameters == null) {
            return null;
        }

        final PluginParameters sanitisedParameters = parameters.copy();

        final Map<String, PluginParameter<?>> parametersMap = sanitisedParameters.getParameters();
        for (final String key : parametersMap.keySet()) {
            if (parametersMap.get(key).getType().getId().equals(PasswordParameterType.ID)) {
                parametersMap.get(key).setStringValue("*****");
            }
        }

        return sanitisedParameters;
    }
}

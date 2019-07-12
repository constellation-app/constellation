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
import org.openide.util.lookup.ServiceProvider;

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

    @Override
    public void applicationStart() {
    }

    @Override
    public void applicationStop() {
    }

    @Override
    public void pluginStart(final Graph graph, final Plugin plugin, final PluginParameters parameters) {
    }

    @Override
    public void pluginStop(final Plugin plugin, final PluginParameters parameters) {
    }

    @Override
    public void pluginInfo(final Plugin plugin, final String info) {
    }

    @Override
    public void pluginError(final Plugin plugin, final Throwable error) {
    }

    @Override
    public void pluginProperties(final Plugin plugin, final Properties properties) {
    }
}

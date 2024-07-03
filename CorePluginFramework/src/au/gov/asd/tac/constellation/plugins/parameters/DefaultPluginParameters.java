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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.Plugin;
import java.util.HashMap;
import java.util.Map;

/**
 * DefaultPluginParameters provides copies of the parameters created by a
 * specified plugin before they have been altered in any way. Instead of asking
 * a plugin to directly create parameters, parameters should be created by
 * asking DefaultPluginParameters for a copy. In this way, it prevents a plugin
 * from creating different parameters during the life of a Constellation
 * session.
 *
 * @author sirius
 */
public class DefaultPluginParameters {

    private static final Map<Class<? extends Plugin>, PluginParameters> DEFAULT_PARAMETER_IDS = new HashMap<>();

    public static PluginParameters getDefaultParameters(final Plugin plugin) {
        final PluginParameters parameters;
        if (!DEFAULT_PARAMETER_IDS.containsKey(plugin.getClass())) {
            parameters = plugin.createParameters();
            if (parameters != null) {
                parameters.lock();
            }
            DEFAULT_PARAMETER_IDS.put(plugin.getClass(), parameters);
        } else {
            parameters = DEFAULT_PARAMETER_IDS.get(plugin.getClass());
        }
        return parameters == null ? null : parameters.copy();
    }
}

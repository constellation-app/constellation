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
package au.gov.asd.tac.constellation.plugins.parameters;

import java.util.Map;

/**
 * A PluginParameterController controls interaction between a group of
 * PluginParameters in a PluginParameterPane. It is registered as a listener on
 * the pane and gets notified when ever the master parameter changes.
 *
 * @author sirius
 */
@FunctionalInterface
public interface PluginParameterController {

    /**
     * Notify a master controller that the master parameter has changed,
     * supplying all of the parameters.
     *
     * @param master The parameter that has changed.
     * @param parameters All of the plugin parameters.
     * @param change The kind of change.
     */
    public void parameterChanged(final PluginParameter<?> master,
            final Map<String, PluginParameter<?>> parameters, final ParameterChange change);
}

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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Data access view global parameters.
 * <p>
 * Individual modules can define global parameters in the data access view.
 * Parameters have positions that allow them to be visually ordered: a parameter
 * with position 10 will come before a parameter with position 20.
 * <p>
 * Implementations of this class should provide a public static String for each
 * parameter it defines; plugins that use the global parameter can reference it
 * via that String.
 *
 * @author algol
 */
public abstract class GlobalParameters {

    private static PluginParameters globalParameters = null;

    /**
     * The global parameters defined by this class.
     * <p>
     * The initial values of the parameters should also be set. The previous
     * values can be used if they are present.
     *
     * @param previous Previous parameters containing values: may be null if
     * there are no previous parameters.
     *
     * @return The global parameters defined by this class.
     */
    public abstract List<PositionalPluginParameter> getParameterList(final PluginParameters previous);

    /**
     * Perform post-processing on the complete PluginParameters instance.
     * <p>
     * This will typically be something like parameters.addMasterController().
     *
     * @param parameters The complete global PluginParameters instance.
     */
    public void postProcess(final PluginParameters parameters) {
    }

    /**
     * Gather the global parameters and return them in the correct order.
     *
     * @param previous Parameters from a previous global parameter pane; may be
     * null if this is the first pane.
     *
     * @return A PluginParameters ordered by ascending position.
     */
    public static PluginParameters getParameters(final PluginParameters previous) {
        if (globalParameters == null) {
            final List<PositionalPluginParameter> globalParametersList = new ArrayList<>();
            final Collection<? extends GlobalParameters> globalParametersObjects = Lookup.getDefault().lookupAll(GlobalParameters.class);
            globalParametersObjects.stream().forEach(globalParametersObject -> {
                globalParametersList.addAll(globalParametersObject.getParameterList(previous));
            });
            Collections.sort(globalParametersList);

            globalParameters = new PluginParameters();
            globalParametersList.stream().forEach(globalParameter -> {
                globalParameters.addParameter(globalParameter.getParameter());
            });

            globalParametersObjects.stream().forEach(globalParametersObject -> {
                globalParametersObject.postProcess(globalParameters);
            });
        }

        return globalParameters;
    }

    /**
     * Read a key / value data file from the specified file resource and return
     * an ordered Map.
     *
     * @param cls Class associated with the resource.
     * @param name Resource name.
     * @param container The container to store the data
     *
     */
    protected static void readDataToMap(final Class cls, final String name, final Map<String, String> container) {
        try {
            final InputStreamReader in = new InputStreamReader(cls.getResourceAsStream(name), StandardCharsets.UTF_8.name());
            try (final BufferedReader reader = new BufferedReader(in)) {
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (!line.isEmpty() && !line.startsWith("#")) {
                        final int ix = line.indexOf(',');
                        container.put(line.substring(0, ix), line.substring(ix + 1));
                    }
                }
            }
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * A PluginParameter with a position.
     */
    protected static class PositionalPluginParameter implements Comparable<PositionalPluginParameter> {

        private final PluginParameter parameter;
        private final int position;

        public PositionalPluginParameter(final PluginParameter parameter, final int position) {
            this.parameter = parameter;
            this.position = position;
        }

        public PluginParameter getParameter() {
            return parameter;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public int compareTo(final PositionalPluginParameter o) {
            return Integer.compare(position, o.position);
        }
    }
}

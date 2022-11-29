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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

    private static final Logger LOGGER = Logger.getLogger(GlobalParameters.class.getName());
    
    private static PluginParameters globalParameters = null;

    /**
     * The global parameters defined by this class.
     * <p/>
     * The initial values of the parameters should also be set. The previous
     * values can be used if they are present.
     *
     * @param previous previous parameters containing values: may be null if
     *     there are no previous parameters
     *
     * @return the global parameters defined by this class
     */
    public abstract List<PositionalPluginParameter> getParameterList(final PluginParameters previous);

    /**
     * Perform post-processing on the complete PluginParameters instance.
     * <p/>
     * This will typically be something like parameters.addMasterController().
     *
     * @param parameters the complete global PluginParameters instance
     */
    public void postProcess(final PluginParameters parameters) {
    }

    /**
     * Gather the global parameters and return them in the correct order.
     *
     * @param previous parameters from a previous global parameter pane; may be
     *     null if this is the first pane.
     *
     * @return a {@link PluginParameters} ordered by ascending position.
     */
    public static PluginParameters getParameters(final PluginParameters previous) {
        if (globalParameters == null) {
            final Collection<? extends GlobalParameters> globalParametersObjects =
                    Lookup.getDefault().lookupAll(GlobalParameters.class);
            
            globalParameters = new PluginParameters();
            globalParametersObjects.stream()
                    .map(globalParametersObject -> globalParametersObject.getParameterList(previous))
                    .flatMap(Collection::stream)
                    .sorted()
                    .map(PositionalPluginParameter::getParameter)
                    .forEach(parameter -> globalParameters.addParameter(parameter));

            globalParametersObjects.stream().forEach(globalParametersObject -> 
                globalParametersObject.postProcess(globalParameters)
            );
        }

        return globalParameters;
    }

    /**
     * Read a key/value data file from the specified file resource and populate
     * the passed map. The key/value data is separated by commas. Blank lines
     * and lines beginning with a hash are ignored.
     *
     * @param cls class associated with the resource
     * @param name name of the resource
     * @param container the map to store the loaded data in
     *
     */
    protected static void readDataToMap(final Class<?> cls,
                                        final String name,
                                        final Map<String, String> container) {
        try {
            IOUtils.readLines(cls.getResourceAsStream(name), StandardCharsets.UTF_8).stream()
                    .filter(StringUtils::isNotBlank)
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> {
                        final int ix = line.indexOf(',');
                        container.put(line.substring(0, ix), line.substring(ix + 1));
                    });
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read data properly", ex);
        }
    }

    /**
     * Clears the global parameters by setting the variable to null. This is primarily
     * for unit testing purposes and should not be used by actual code.
     */
    protected static void clearGlobalParameters() {
        globalParameters = null;
    }
    
    /**
     * A PluginParameter with a position. When being sorted, the position variable
     * is used to determine the plugins position.
     */
    protected static class PositionalPluginParameter implements Comparable<PositionalPluginParameter> {
        private final PluginParameter<?> parameter;
        private final int position;

        public PositionalPluginParameter(final PluginParameter<?> parameter, final int position) {
            this.parameter = parameter;
            this.position = position;
        }

        public PluginParameter<?> getParameter() {
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

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
package au.gov.asd.tac.constellation.views.dataaccess.api;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Basic POJO representing the serialized data access modules user preferences
 * for one tab on the query phase pane.
 *
 * @author formalhaunt
 */
public final class DataAccessUserPreferences {
    private static final String IS_ENABLED = "__is_enabled__";
    private static final String PLUGIN_PARAMETER_KEY_FORMAT = "%s.%s";

    /**
     * Generates a plugin property name for the JSON output that represents if the
     * plugin is enabled. If the plugin class has a simple name of {@code MyPlugin}
     * then the property name will be {@code MyPlugin.__is_enabled__}.
     */
    public static final BiFunction<Class<? extends Plugin>, String, String> PLUGIN_PARAMETER_KEY_GENERATOR =
            (pluginClass, parameterName) -> String.format(PLUGIN_PARAMETER_KEY_FORMAT,
                    pluginClass.getSimpleName(), parameterName);
    
    @JsonProperty(value = "global")
    private Map<String, String> globalParameters;
    
    @JsonProperty(value = "plugins")
    private Map<String, String> pluginParameters;

    /**
     * Create a new {@link DataAccessUserPreferences}.
     */
    public DataAccessUserPreferences() {
        
    }
    
    /**
     * Create a new {@link DataAccessUserPreferences} based on the passed
     * {@link QueryPhasePane}.
     *
     * @param pane the query phase pane to build the data access user preferences from
     */
    public DataAccessUserPreferences(final QueryPhasePane pane) {
        // Extract the global parameters
        globalParameters = pane.getGlobalParametersPane().getParams().getParameters().entrySet().stream()
                .map(param -> new AbstractMap.SimpleImmutableEntry<>(param.getKey(), param.getValue().getStringValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        pluginParameters = new HashMap<>();
        pane.getDataAccessPanes().stream()
                .filter(dataAccessPane -> dataAccessPane.isQueryEnabled())
                .forEach(dataAccessPane -> {
                    // Add the enabled parameter to the plugin parameters
                    pluginParameters.put(
                            PLUGIN_PARAMETER_KEY_GENERATOR.apply(dataAccessPane.getPlugin().getClass(), IS_ENABLED),
                            "true"
                    );
                    
                    // As long as the data access pane parameters were not mentioned in
                    // the global parameters or the parameter is a password type parameter
                    // then add them to the plugin parameters map
                    final PluginParameters parameters = dataAccessPane.getParameters();
                    if (parameters != null) {
                        parameters.getParameters().entrySet().stream()
                                .filter(param -> !PasswordParameterType.ID.equals(param.getValue().getType().getId())
                                        && !globalParameters.containsKey(param.getKey()))
                                .forEach(param -> pluginParameters.put(
                                        param.getKey(), param.getValue().getStringValue()
                                ));
                    }
                
                });
    }
    
    public Map<String, String> getGlobalParameters() {
        return globalParameters;
    }

    public void setGlobalParameters(final Map<String, String> globalParameters) {
        this.globalParameters = globalParameters;
    }

    public Map<String, String> getPluginParameters() {
        return pluginParameters;
    }

    public void setPluginParameters(final Map<String, String> pluginParameters) {
        this.pluginParameters = pluginParameters;
    }
    
    /**
     * Convert the contents of the {@link #pluginParameters} map to a per-plugin
     * map. The plugin map has the following format
     * 
     * <pre><code>
     *      pluginA.param1: value1
     *      pluginA.param2: value2
     *      pluginB.param1: value3
     *      pluginC.param1: value4
     * </code></pre>
     * 
     * The above will be converted to
     * 
     * <pre><code>
     *      pluginA:
     *           pluginA.param1: value1
     *           pluginA.param2: value2
     *      pluginB:
     *           pluginB.param1: value3
     *      pluginC:
     *           pluginC.param1: value4
     * </code></pre>
     * 
     * @return the generated per plugin parameter map
     */
    @JsonIgnore
    public Map<String, Map<String, String>> toPerPluginParamMap() {
        return getPluginParameters().entrySet().stream()
                .map(entry -> {
                    final String name = entry.getKey();
                    final String value = entry.getValue();

                    final int index = name.indexOf('.');
                    if (index != -1) {
                        final String plugin = name.substring(0, index);
                        
                        final Map<String, String> pluginParams = new HashMap<>();
                        pluginParams.put(name, value);
                        
                        return new AbstractMap.SimpleImmutableEntry<>(plugin, pluginParams);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (params1, params2) -> {
                            // Don't need to worry about key clashes as they values
                            // were originally in a map guaranteeing uniquness
                            final Map<String, String> m = new HashMap<>();
                            m.putAll(params1);
                            m.putAll(params2);
                            return m;
                        }
                ));
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataAccessUserPreferences rhs = (DataAccessUserPreferences) o;

        return new EqualsBuilder()
                .append(getGlobalParameters(), rhs.getGlobalParameters())
                .append(getPluginParameters(), rhs.getPluginParameters())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getGlobalParameters())
                .append(getPluginParameters())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("globalParameters", getGlobalParameters())
                .append("pluginParameters", getPluginParameters())
                .toString();
    }
    
    /**
     * Get the enabled JSON property name for the passed plugin class.
     *
     * @param pluginClass the class of the plugin
     * @return the generated JSON enabled property name
     * @see #PLUGIN_PARAMETER_KEY_GENERATOR
     */
    @JsonIgnore
    public static String getEnabledPluginKey(final Class<? extends Plugin> pluginClass) {
        return PLUGIN_PARAMETER_KEY_GENERATOR.apply(pluginClass, IS_ENABLED);
    }
    
}

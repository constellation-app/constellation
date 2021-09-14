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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessConcept;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessState;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessUserPreferences;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;

/**
 * Save and load data access view plugin parameters.
 * <p>
 * Parameters are saved using their names as keys. Therefore, having two plugins
 * with the same parameter names is a bad idea. Parameter names should be
 * qualified with their simple class name: using the fully qualified name
 * (including the package) would cause saved parameters to be useless if classes
 * are refactored into different packages. Note that refactoring the class name
 * will also break a saved file, but we'll take our chances.
 *
 * @author algol
 */
public class ParameterIOUtilities {
    private static final String DATA_ACCESS_DIR = "DataAccessView";

    /**
     * Load the data access graph state and update the data access view.
     * <p>
     * Currently only looking at string parameter types and only shows the first
     * step if it exists.
     *
     * @param dap The data access pane
     * @param graph The active graph to load the state from
     */
    public static void loadDataAccessState(final DataAccessPane dap, final Graph graph) {
        if (graph != null && dap.getCurrentTab() != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.get(rg);
                if (dataAccessStateAttribute != Graph.NOT_FOUND) {
                    final DataAccessState dataAccessState = rg.getObjectValue(dataAccessStateAttribute, 0);
                    if (dataAccessState != null && dataAccessState.getState().size() > 0) {
                        // TODO: support multiple tabs (not just first one in state) and not introduce memory leaks
                        final Map<String, String> tabState = dataAccessState.getState().get(0);
                        final Tab step = dap.getCurrentTab().getTabPane().getTabs().get(0);
                        final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();
                        pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().stream().forEach(param -> {
                            final PluginParameter<?> pp = param.getValue();
                            final String paramvalue = tabState.get(param.getKey());
                            if (paramvalue != null) {
                                pp.setStringValue(paramvalue);
                            }
                        });
                    }
                }
            } finally {
                rg.release();
            }
        }
    }

    /**
     * Save the data access state to the graph.
     * <p>
     * Currently only global parameters are saved.
     *
     * @param tabs The TabPane
     * @param graph The active graph to save the state to
     */
    public static void saveDataAccessState(final TabPane tabs, final Graph graph) {
        if (graph != null) {
            // buildId the data access state object
            final DataAccessState dataAccessState = new DataAccessState();
            for (final Tab step : tabs.getTabs()) {
                dataAccessState.newTab();
                final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();
                for (final Map.Entry<String, PluginParameter<?>> param : pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet()) {
                    final String id = param.getKey();
                    final PluginParameter<?> pp = param.getValue();
                    final String value = pp.getStringValue();
                    if (value != null) {
                        dataAccessState.add(id, value);
                    }
                }
            }

            // save the state onto the graph
            WritableGraph wg = null;
            try {
                wg = graph.getWritableGraph("Update Data Access State", true);
                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.ensure(wg);
                wg.setObjectValue(dataAccessStateAttribute, 0, dataAccessState);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            } finally {
                if (wg != null) {
                    wg.commit();
                }
            }
        }
    }

    /**
     * Saves the global and plugin parameters from the passed tabs that belong to
     * the {@link DataAccessPane}. The parameters will be saved to a JSON file as
     * an array with each element representing one tab.
     *
     * @param tabs the tabs to extract the global and plugin parameters from
     * @see JsonIO#saveJsonPreferences(Optional, ObjectMapper, Object) 
     */
    public static void saveParameters(final TabPane tabs) {
        final List<DataAccessUserPreferences> dataAccessUserPreferenceses = new ArrayList<>();

        String queryName = null;
        for (final Tab step : tabs.getTabs()) {
            final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();

            // Translate the plugin pane to the JSON format
            final DataAccessUserPreferences preferences = new DataAccessUserPreferences(pluginPane);

            // Remember the first non-null, non-blank query name.
            if (queryName == null
                    && preferences.getGlobalParameters().containsKey(
                            CoreGlobalParameters.QUERY_NAME_PARAMETER_ID
                    )
                    && StringUtils.isNotBlank(preferences.getGlobalParameters().get(
                            CoreGlobalParameters.QUERY_NAME_PARAMETER_ID
                    ))) {
                queryName = preferences.getGlobalParameters().get(
                        CoreGlobalParameters.QUERY_NAME_PARAMETER_ID
                );
            }

            dataAccessUserPreferenceses.add(preferences);
        }

        // Only save if the query name parameter is present
        if (queryName != null) {
            JsonIO.saveJsonPreferences(Optional.of(DATA_ACCESS_DIR), new ObjectMapper(), dataAccessUserPreferenceses);
        }
    }

    /**
     * Loads global and plugin parameters from a JSON file into the passed {@link DataAccessPane}.
     * If the JSON is loaded then all existing tabs will be removed and then new tabs added
     * for each entry in the loaded JSON array.
     * 
     * @param dataAccessPane the pane to load the JSON parameter file into
     * @see JsonIO#loadJsonPreferences(Optional, TypeReference) 
     */
    public static void loadParameters(final DataAccessPane dataAccessPane) {
        final List<DataAccessUserPreferences> loadedParameters = JsonIO
                .loadJsonPreferences(
                        Optional.of(DATA_ACCESS_DIR),
                        new TypeReference<List<DataAccessUserPreferences>>() {}
                );
        
        if (loadedParameters != null) {
            dataAccessPane.removeTabs();

            loadedParameters.forEach(loadedParameter -> {
                final QueryPhasePane pluginPane = dataAccessPane.newTab();

                // If an existing global parameter is in the JSON then update it,
                // otherwise ignore it
                pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().stream()
                        .filter(param -> loadedParameter.getGlobalParameters().containsKey(param.getKey()))
                        .forEach(param -> {
                            param.getValue().setStringValue(
                                    loadedParameter.getGlobalParameters().get(param.getKey())
                            );
                        });

                // Groups all the parameters in to the plugin groups. Common parameters
                // are based on the plugin name that is before the first '.' in the key values
                final Map<String, Map<String, String>> ppmap = loadedParameter.toPerPluginParamMap();

                pluginPane.getDataAccessPanes().stream()
                        // Plugins are disabled by defult. Only load and enable from
                        // the JSON if the JSON contains data for this plugin and it's
                        // enabled.
                        .filter(pane -> loadedParameter.getPluginParameters().containsKey(getEnabledPluginKey(pane)) 
                                && Boolean.valueOf(loadedParameter.getPluginParameters().get(getEnabledPluginKey(pane))))
                        .forEach(pane -> pane.setParameterValues(
                                ppmap.get(pane.getPlugin().getClass().getSimpleName())
                        ));
            });
        }
    }
    
    /**
     * Generates the JSON 'enabled' property name for the plugin associated to the
     * passed {@link DataSourceTitledPane}.
     *
     * @param pane the pane that contains the plugin
     * @return the generated property name
     */
    private static String getEnabledPluginKey(final DataSourceTitledPane pane) {
        return DataAccessUserPreferences.getEnabledPluginKey(pane.getPlugin().getClass());
    }
}

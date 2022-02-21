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

import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessUserPreferences;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang3.StringUtils;

/**
 * Save and load data access view plugin parameters. The parameters are saved
 * in JSON format on disk.
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
public class DataAccessParametersIoProvider {
    private static final String DATA_ACCESS_DIR = "DataAccessView";

    /**
     * Private constructor to prevent external initialization.
     */
    private DataAccessParametersIoProvider() {
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
            // Translate the plugin pane to the JSON format
            final DataAccessUserPreferences preferences = new DataAccessUserPreferences(
                    DataAccessTabPane.getQueryPhasePane(step)
            );

            final Label tabCaption = (Label) step.getGraphic();
            final Label defaultCaption = (Label) tabCaption.getGraphic();
            preferences.setStepCaption(!StringUtils.isBlank(tabCaption.getText()) ? tabCaption.getText() : defaultCaption.getText());

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
            JsonIO.saveJsonPreferences(Optional.of(DATA_ACCESS_DIR), dataAccessUserPreferenceses);
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
            dataAccessPane.getDataAccessTabPane().removeTabs();

            loadedParameters.forEach(loadedParameter -> {
                final QueryPhasePane pluginPane = dataAccessPane.getDataAccessTabPane().newTab(loadedParameter.getStepCaption());

                // If an existing global parameter is in the JSON then update it,
                // otherwise ignore it
                pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().stream()
                        .filter(param -> loadedParameter.getGlobalParameters().containsKey(param.getKey()))
                        .forEach(param -> 
                            param.getValue().setStringValue(
                                    loadedParameter.getGlobalParameters().get(param.getKey())
                            )
                        );

                // Groups all the parameters in to the plugin groups. Common parameters
                // are based on the plugin name that is before the first '.' in the key values

                pluginPane.getDataAccessPanes().stream()
                        // Plugins are disabled by defult. Only load and enable from
                        // the JSON if the JSON contains data for this plugin and it's
                        // enabled.
                        .filter(pane ->
                                loadedParameter.getPluginParameters().containsKey(pane.getPlugin().getClass().getSimpleName())
                                        && loadedParameter.getPluginParameters().get(pane.getPlugin().getClass().getSimpleName()).containsKey(getEnabledPluginKey(pane))
                                        && Boolean.valueOf(
                                                loadedParameter.getPluginParameters().get(pane.getPlugin().getClass().getSimpleName()).get(
                                                        getEnabledPluginKey(pane)
                                                ))
                        )
                        .forEach(pane -> pane.setParameterValues(
                                loadedParameter.getPluginParameters().get(pane.getPlugin().getClass().getSimpleName())
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

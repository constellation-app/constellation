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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessUserPreferences;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author mimosa2
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DataAccessParametersIoProviderNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(DataAccessParametersIoProviderNGTest.class.getName());

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @Test
    public void saveParameters_query_present() {
        final Map<String, String> tab1GlobalParams = Map.of(
                "param1", "tab1_param1_value",
                "CoreGlobalParameters.query_name", "my query"
        );

        final Map<String, String> tab2GlobalParams = Map.of(
                "param2", "tab2_param2_value"
        );

        saveParameters(tab1GlobalParams, tab2GlobalParams, true);
    }

    @Test
    public void saveParameters_no_query_name() {
        final Map<String, String> tab1GlobalParams = Map.of(
                "param1", "tab1_param1_value"
        );

        final Map<String, String> tab2GlobalParams = Map.of(
                "param2", "tab2_param2_value"
        );

        saveParameters(tab1GlobalParams, tab2GlobalParams, true);
    }

    @Test
    public void saveParameters_query_name_blank() {
        final Map<String, String> tab1GlobalParams = Map.of(
                "param1", "tab1_param1_value",
                "CoreGlobalParameters.query_name", ""
        );

        final Map<String, String> tab2GlobalParams = Map.of(
                "param2", "tab2_param2_value"
        );

        saveParameters(tab1GlobalParams, tab2GlobalParams, true);
    }

    @Test
    public void loadParameters() throws IOException {
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        
        final QueryPhasePane tab1 = mock(QueryPhasePane.class);
        final QueryPhasePane tab2 = mock(QueryPhasePane.class);
        when(dataAccessTabPane.newTab(anyString())).thenReturn(tab1).thenReturn(tab2);
        
        final GlobalParametersPane globalParametersPane1 = mock(GlobalParametersPane.class); 
        final GlobalParametersPane globalParametersPane2 = mock(GlobalParametersPane.class); 
        when(tab1.getGlobalParametersPane()).thenReturn(globalParametersPane1);
        when(tab2.getGlobalParametersPane()).thenReturn(globalParametersPane2);

        // By adding the settings bit here, it forces mockito to generate two different
        // classes. Otherwise they would be two different objects but have the same class name
        final Plugin plugin1 = mock(Plugin.class, withSettings().extraInterfaces(Comparable.class));
        final Plugin plugin2 = mock(Plugin.class, withSettings().extraInterfaces(Serializable.class));

        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        when(dataSourceTitledPane1.getPlugin()).thenReturn(plugin1);

        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        when(dataSourceTitledPane2.getPlugin()).thenReturn(plugin2);

        when(tab1.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
        when(tab2.getDataAccessPanes()).thenReturn(List.of());

        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        when(pluginParameter1.getId()).thenReturn("param1");

        final PluginParameter pluginParameter2 = mock(PluginParameter.class);
        when(pluginParameter2.getId()).thenReturn("param2");

        final PluginParameter pluginParameter3 = mock(PluginParameter.class);
        when(pluginParameter3.getId()).thenReturn("param3");

        final PluginParameter pluginParameter4 = mock(PluginParameter.class);
        when(pluginParameter4.getId()).thenReturn("param4");

        final PluginParameters globalPluginParameters1 = new PluginParameters();
        globalPluginParameters1.addParameter(pluginParameter1);
        globalPluginParameters1.addParameter(pluginParameter2);
        globalPluginParameters1.addParameter(pluginParameter3);

        final PluginParameters globalPluginParameters2 = new PluginParameters();
        globalPluginParameters2.addParameter(pluginParameter3);
        globalPluginParameters2.addParameter(pluginParameter4);

        when(globalParametersPane1.getParams()).thenReturn(globalPluginParameters1);
        when(globalParametersPane2.getParams()).thenReturn(globalPluginParameters2);

        try (final MockedStatic<JsonIO> jsonIOStaticMock = Mockito.mockStatic(JsonIO.class)) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final String json = IOUtils.toString(
                    new FileInputStream(getClass().getResource("resources/preferences.json").getPath()),
                    StandardCharsets.UTF_8
            );

            // We do not know the mockito plugin names ahead of time so substitute them in now
            final StringSubstitutor substitutor = new StringSubstitutor(
                    Map.of(
                            "INSERT_PLUGIN1_NAME", plugin1.getClass().getSimpleName(),
                            "INSERT_PLUGIN2_NAME", plugin2.getClass().getSimpleName()
                    )
            );
            final List<DataAccessUserPreferences> preferences = objectMapper.readValue(
                    substitutor.replace(json), new TypeReference<List<DataAccessUserPreferences>>() {});

            jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences(eq(Optional.of("DataAccessView")), any(TypeReference.class)))
                    .thenReturn(preferences);

            DataAccessParametersIoProvider.loadParameters(dataAccessPane);
        }
        
        verify(dataAccessTabPane, times(2)).newTab(anyString());
        
        // tab1 global parameters
        verify(pluginParameter1).setStringValue("tab1_param1_value");
        verify(pluginParameter3).setStringValue(null);
        
        // tab1 plugin parameters - only plugin1 because plugin2 is disabled
        verify(dataSourceTitledPane1).setParameterValues(Map.of(
                plugin1.getClass().getSimpleName() + "." + "__is_enabled__", "true",
                plugin1.getClass().getSimpleName() + "." + "param1", "plugin1_param1_value"
        ));

        // tab2 global parameters
        verify(pluginParameter4).setStringValue("tab2_param4_value");

        // tab2 plugin parameters
        verify(dataSourceTitledPane2, times(0)).setParameterValues(anyMap());
    }

    /**
     * Verifies the save parameter method saves only if the query name parameter
     * is present in the global parameters and is not blank. This will simulate two
     * tabs and verify the correct calls are made to serialize the data.
     *
     * @param tab1GlobalParams the global parameters to be populated in tab 1
     * @param tab2GlobalParams the global parameters to be populated in tab 2
     * @param isSaveExpected true if a call to save the JSON is expected to be made, false otherwise
     */
    public void saveParameters(final Map<String, String> tab1GlobalParams,
                               final Map<String, String> tab2GlobalParams,
                               final boolean isSaveExpected) {
        final TabPane tabPane = mock(TabPane.class);

        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList(tab1, tab2));

        final ScrollPane scrollPane1 = mock(ScrollPane.class);
        final ScrollPane scrollPane2 = mock(ScrollPane.class);
        when(tab1.getContent()).thenReturn(scrollPane1);
        when(tab2.getContent()).thenReturn(scrollPane2);

        when(tab1.getGraphic()).thenReturn(new Label("Step caption 1"));
        when(tab2.getGraphic()).thenReturn(new Label("Step caption 2"));

        final QueryPhasePane queryPhasePane1 = mock(QueryPhasePane.class);
        final QueryPhasePane queryPhasePane2 = mock(QueryPhasePane.class);
        when(scrollPane1.getContent()).thenReturn(queryPhasePane1);
        when(scrollPane2.getContent()).thenReturn(queryPhasePane2);

        // This will apply the correct stubbing to the created mocks. Uses the passed
        // query phase pane to determine what stubs to set.
        final MockedConstruction.MockInitializer<DataAccessUserPreferences> mockInitializer = 
                (mock, cntxt) -> {
                    final QueryPhasePane pane = (QueryPhasePane) cntxt.arguments().get(0);

                    if (pane.equals(queryPhasePane1)) {
                        when(mock.getGlobalParameters())
                                .thenReturn(
                                        tab1GlobalParams
                                );
                        return;
                    } else if (pane.equals(queryPhasePane2)) {
                        when(mock.getGlobalParameters())
                                .thenReturn(
                                        tab2GlobalParams
                                );
                        return;
                    }

                    throw new RuntimeException("Unknown QueryPhasePane passed to DataAccessUserPreferences constructor");
                };

        try (
                final MockedStatic<JsonIO> jsonIOStaticMock = Mockito.mockStatic(JsonIO.class);
                final MockedConstruction<DataAccessUserPreferences> mockedPrefConstruction = 
                        Mockito.mockConstruction(DataAccessUserPreferences.class, mockInitializer);
            ) {
            DataAccessParametersIoProvider.saveParameters(tabPane);
            
            if (isSaveExpected) {
                jsonIOStaticMock.verify(() -> JsonIO.saveJsonPreferences(
                        eq(Optional.of("DataAccessView")),
                        eq(mockedPrefConstruction.constructed())
                ));
            } else {
                jsonIOStaticMock.verifyNoInteractions();
            }
        }
    }
}

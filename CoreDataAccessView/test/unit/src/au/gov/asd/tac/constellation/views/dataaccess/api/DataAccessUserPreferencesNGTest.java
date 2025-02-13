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
package au.gov.asd.tac.constellation.views.dataaccess.api;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessUserPreferencesNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(DataAccessUserPreferencesNGTest.class.getName());

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
    public void equality() {
        EqualsVerifier.forClass(DataAccessUserPreferences.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void initWithPane() {
        final QueryPhasePane tab = mock(QueryPhasePane.class);
        
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class); 
        when(tab.getGlobalParametersPane()).thenReturn(globalParametersPane);
        
        final Plugin plugin1 = mock(Plugin.class);
        
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        when(dataSourceTitledPane1.getPlugin()).thenReturn(plugin1);
        when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(true);

        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(false);
        
        when(tab.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
        
        final PluginParameterType passwordType = mock(PluginParameterType.class);
        when(passwordType.getId()).thenReturn("password");

        final PluginParameterType stringType = mock(PluginParameterType.class);
        when(stringType.getId()).thenReturn("string");

        // Plugin params will be in global and plugin but because it is in global
        // it should not be included in the plugin section
        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        when(pluginParameter1.getId()).thenReturn("param1");
        when(pluginParameter1.getStringValue()).thenReturn("value1");
        when(pluginParameter1.getType()).thenReturn(stringType);

        final PluginParameter pluginParameter2 = mock(PluginParameter.class);
        when(pluginParameter2.getId()).thenReturn("param2");
        when(pluginParameter2.getStringValue()).thenReturn("value2");
        when(pluginParameter2.getType()).thenReturn(stringType);

        final PluginParameter pluginParameter3 = mock(PluginParameter.class);
        when(pluginParameter3.getId()).thenReturn("param3");
        when(pluginParameter3.getStringValue()).thenReturn("value3");
        when(pluginParameter3.getType()).thenReturn(stringType);

        // Param 4 is of password type and should not be added
        final PluginParameter pluginParameter4 = mock(PluginParameter.class);
        when(pluginParameter4.getId()).thenReturn("param4");
        when(pluginParameter4.getStringValue()).thenReturn("value4");
        when(pluginParameter4.getType()).thenReturn(passwordType);

        // Set the global parameters
        final PluginParameters globalPluginParameters1 = new PluginParameters();
        globalPluginParameters1.addParameter(pluginParameter1);
        globalPluginParameters1.addParameter(pluginParameter2);

        // Set the parameters for plugin1
        final PluginParameters plugin1Parameters = new PluginParameters();
        plugin1Parameters.addParameter(pluginParameter1);
        plugin1Parameters.addParameter(pluginParameter3);
        plugin1Parameters.addParameter(pluginParameter4);

        when(globalParametersPane.getParams()).thenReturn(globalPluginParameters1);
        when(dataSourceTitledPane1.getParameters()).thenReturn(plugin1Parameters);

        final DataAccessUserPreferences preferences = new DataAccessUserPreferences(tab);

        final DataAccessUserPreferences expectedPreferences = new DataAccessUserPreferences();
        expectedPreferences.setGlobalParameters(
                Map.of(
                        "param1", "value1",
                        "param2", "value2"
                )
        );
        final Map<String, Map<String, String>> examplePluginParameters = new HashMap<>();
        examplePluginParameters.put(plugin1.getClass().getSimpleName(), Map.of(
                        plugin1.getClass().getSimpleName() + "." + "__is_enabled__", "true",
                        "param3", "value3"
                ));
        expectedPreferences.setPluginParameters(examplePluginParameters);

        assertEquals(preferences, expectedPreferences);
    }
}

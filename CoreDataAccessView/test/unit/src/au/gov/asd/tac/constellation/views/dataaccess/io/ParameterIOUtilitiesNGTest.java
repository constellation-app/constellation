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
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mimosa2
 */
public class ParameterIOUtilitiesNGTest {

    private static MockedStatic<JsonIO> jsonIOStaticMock;
    private static MockedStatic<NbPreferences> nbPreferencesStaticMock;
    private static MockedStatic<ApplicationPreferenceKeys> applicationPrefKeysStaticMock;

    public ParameterIOUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
        jsonIOStaticMock = Mockito.mockStatic(JsonIO.class);
        nbPreferencesStaticMock = Mockito.mockStatic(NbPreferences.class);
        applicationPrefKeysStaticMock = Mockito.mockStatic(ApplicationPreferenceKeys.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.hideStage();
        jsonIOStaticMock.close();
        nbPreferencesStaticMock.close();
        applicationPrefKeysStaticMock.close();
    }

    /**
     * Test of saveDataAccessState method, of class ParameterIOUtilities.
     */
    @Test
    public void testsaveDataAccessState() throws Exception {
        System.out.println("testsaveDataAccessState");

        // mock Tab
        final Tab tab = mock(Tab.class);

        ObservableList<Tab> observableArrayList
                = FXCollections.observableArrayList(tab);

        // mock TabPane
        final TabPane tabPane = mock(TabPane.class);
        when(tabPane.getTabs()).thenReturn(observableArrayList);

        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);
        final PluginParameter pluginParameter = mock(PluginParameter.class);

        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);
        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(pluginParameters);
        when(pluginParameter.getStringValue()).thenReturn("something");

        final String someKey = "someKey";
        final Map<String, PluginParameter<?>> map = Map.of(someKey, pluginParameter);
        when(pluginParameters.getParameters()).thenReturn(map);

        // mock graph
        final Graph graph = mock(Graph.class);
        final WritableGraph wGraph = mock(WritableGraph.class);
        when(graph.getWritableGraph("Update Data Access State", true)).thenReturn(wGraph);

        ParameterIOUtilities.saveDataAccessState(tabPane, graph);

        final DataAccessState expectedTab = new DataAccessState();
        expectedTab.newTab();
        expectedTab.add("someKey", "something");

        assertEquals(expectedTab.getState().size(), 1);
        verify(wGraph).setObjectValue(0, 0, expectedTab);

    }

    /**
     * Test of saveParameters method, of class ParameterIOUtilities.
     */
    @Test
    public void testsaveParameters() throws IOException {
        System.out.println("testsaveParameters");

        nbPreferencesStaticMock.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class))
                .thenReturn(null);
        applicationPrefKeysStaticMock.when(() -> ApplicationPreferenceKeys.getUserDir(null))
                .thenReturn(System.getProperty("java.io.tmpdir"));

        // mock Tab
        final Tab tab = mock(Tab.class);

        ObservableList<Tab> observableArrayList
                = FXCollections.observableArrayList(tab);

        // mock TabPane
        final TabPane tabPane = mock(TabPane.class);
        when(tabPane.getTabs()).thenReturn(observableArrayList);

        final ScrollPane scrollPane = mock(ScrollPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);
        final PluginParameter pluginParameter = mock(PluginParameter.class);

        // global parameters
        final String someKey = "CoreGlobalParameters.query_name";
        final String someValue = "query_value";

        when(tab.getContent()).thenReturn(scrollPane);
        when(scrollPane.getContent()).thenReturn(queryPhasePane);
        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(pluginParameters);
        when(pluginParameter.getStringValue()).thenReturn(someValue);

        final Map<String, PluginParameter<?>> map = Map.of(someKey, pluginParameter);
        when(pluginParameters.getParameters()).thenReturn(map);

        ParameterIOUtilities.saveParameters(tabPane);

        final ObjectMapper objectMapper = new ObjectMapper();
        final ArrayNode expectedJsonTree = (ArrayNode) objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/plugin-preferences.json").getPath())
        );

        Optional<String> dirName = Optional.of("DataAccessView");
        jsonIOStaticMock.verify(() -> JsonIO.saveJsonPreferences(
                eq(dirName),
                any(ObjectMapper.class),
                eq(expectedJsonTree)
        ));
    }
}

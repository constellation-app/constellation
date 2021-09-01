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
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.hideStage();
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

        final DataAccessState expectedTab = new DataAccessState();
        expectedTab.newTab();
        expectedTab.add("someKey", "something");

        // mock graph
        final Graph graph = mock(Graph.class);
        final WritableGraph wGraph = mock(WritableGraph.class);
        when(graph.getWritableGraph("Update Data Access State", true)).thenReturn(wGraph);

        ParameterIOUtilities.saveDataAccessState(tabPane, graph);

        assertEquals(expectedTab.getState().size(), 1);
        verify(wGraph).setObjectValue(0, 0, expectedTab);

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.dataaccess.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessConcept;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author mimosa2
 */
public class ParameterIOUtilitiesNGTest {

//    private DataAccessState dataAccessState;
    public ParameterIOUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
//        dataAccessState = new DataAccessState();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of saveDataAccessState method, of class ParameterIOUtilities.
     */
    @Test
    public void testsaveDataAccessState() throws Exception {
//    System.out.println("testsaveDataAccessState");
        new JFXPanel();

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

        final String someKey = "someKey";
        final Map<String, PluginParameter<?>> map = Map.of(someKey, pluginParameter);
        when(pluginParameters.getParameters()).thenReturn(map);

        final DataAccessState expectedTab = new DataAccessState();
        expectedTab.newTab();

        for (final Map.Entry<String, PluginParameter<?>> param : map.entrySet()) {
            final String id = param.getKey();
            final PluginParameter<?> pp = param.getValue();
            final String value = pp.getStringValue();
            if (value != null) {
                expectedTab.add(id, value);
            }
        }
//
        // mock graph
        final Graph graph = mock(Graph.class);
        final WritableGraph wGraph = mock(WritableGraph.class);
        when(graph.getWritableGraph("Update Data Access State", true)).thenReturn(wGraph);

//        final String key = "testKey";
//        final String value = "testValue";
//
//        final DataAccessState expectedTab = new DataAccessState();
//        expectedTab.newTab();
//        expectedTab.newTab();
//        expectedTab.add(key, value);
        final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.ensure(wGraph);

        assertEquals(expectedTab.getState().size(), 1);
        verify(wGraph).setObjectValue(dataAccessStateAttribute, 0, expectedTab);

    }

}

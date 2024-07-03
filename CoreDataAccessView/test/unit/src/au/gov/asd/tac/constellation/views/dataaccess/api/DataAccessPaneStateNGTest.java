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

import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import org.python.google.common.base.Stopwatch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessPaneStateNGTest {

    private static final String CURRENT_GRAPH_ID = "currentGraphId";
    private static final String GRAPH_ID = "graphId";

    // Mock of nbPreferences is created to make LookupPluginsTask behave correctly
    private static MockedStatic<NbPreferences> nbPreferencesStatic;
    private static final Preferences preferenceMock = mock(Preferences.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
            nbPreferencesStatic.when(() -> NbPreferences.forModule(DataAccessViewPreferenceKeys.class)).thenReturn(preferenceMock);

            when(preferenceMock.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
            when(preferenceMock.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
        } catch (Exception e) {
            System.out.println("Error creating static mock of NbPreferences");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        nbPreferencesStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Reset the current graph status
        DataAccessPaneState.clearState();
    }

    @Test
    public void getPlugins() throws InterruptedException, ExecutionException {
        // This call could also be sub 1 second. Depends when the first use of
        // DataAccessPaneState is. But running this test in isolation, this will
        // definitely be greater than 1 second and the second call will be sub second
        assertNotNull(DataAccessPaneState.getPlugins());

        // Verify second call is non null and fast
        final Stopwatch stopwatch = Stopwatch.createStarted();
        assertNotNull(DataAccessPaneState.getPlugins());
        assertTrue(stopwatch.stop().elapsed(TimeUnit.SECONDS) < 1);
    }

    @Test
    public void getPluginsWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        // This call could also be sub 1 second. Depends when the first use of
        // DataAccessPaneState is. But running this test in isolation, this will
        // definitely be greater than 1 second and the second call will be sub second
        assertNotNull(DataAccessPaneState.getPlugins(10, TimeUnit.SECONDS));

        // Verify second call is non null and fast
        final Stopwatch stopwatch = Stopwatch.createStarted();
        assertNotNull(DataAccessPaneState.getPlugins(10, TimeUnit.SECONDS));
        assertTrue(stopwatch.stop().elapsed(TimeUnit.SECONDS) < 1);
    }

    @Test
    public void currentGraphId() {
        assertEquals(DataAccessPaneState.getCurrentGraphId(), null);
        DataAccessPaneState.setCurrentGraphId(CURRENT_GRAPH_ID);
        assertEquals(DataAccessPaneState.getCurrentGraphId(), CURRENT_GRAPH_ID);
    }

    @Test
    public void queriesRunning() {
        // current graphId is null
        assertFalse(DataAccessPaneState.isQueriesRunning());

        // current graphId is not null but initial state is no queries running
        DataAccessPaneState.setCurrentGraphId(CURRENT_GRAPH_ID);
        assertFalse(DataAccessPaneState.isQueriesRunning());

        // current graphId is not null and queries set to running
        DataAccessPaneState.setQueriesRunning(true);
        assertTrue(DataAccessPaneState.isQueriesRunning());

        // passed graphId is null
        assertFalse(DataAccessPaneState.isQueriesRunning(null));

        // passed graphId is not null but is not current graphId so
        // initial state is used which is no queries running
        assertFalse(DataAccessPaneState.isQueriesRunning(GRAPH_ID));

        // passed graphId is not null and queries set to running
        DataAccessPaneState.setQueriesRunning(GRAPH_ID, true);
        assertTrue(DataAccessPaneState.isQueriesRunning(GRAPH_ID));
    }

    @Test
    public void executeButtonIsGo() {
        // current graphId is null
        assertFalse(DataAccessPaneState.isExecuteButtonIsGo());

        // current graphId is not null but initial state is execute button is go
        DataAccessPaneState.setCurrentGraphId(CURRENT_GRAPH_ID);
        assertTrue(DataAccessPaneState.isExecuteButtonIsGo());

        // current graphId is not null and execute button set to not go
        DataAccessPaneState.updateExecuteButtonIsGo(false);
        assertFalse(DataAccessPaneState.isExecuteButtonIsGo());

        // passed graphId is null
        assertFalse(DataAccessPaneState.isExecuteButtonIsGo(null));

        // passed graphId is not null but is not current graphId so
        // initial state is used which is execute button is go
        assertTrue(DataAccessPaneState.isExecuteButtonIsGo(GRAPH_ID));

        // passed graphId is not null and execute button set to not go
        DataAccessPaneState.updateExecuteButtonIsGo(GRAPH_ID, false);
        assertFalse(DataAccessPaneState.isExecuteButtonIsGo(GRAPH_ID));
    }

    @Test
    public void runningPluginsCurrentGraph() {
        // no current graph Id
        assertTrue(DataAccessPaneState.getRunningPlugins().isEmpty());

        // cannot add with no current graph Id
        try {
            DataAccessPaneState.addRunningPlugin(
                    CompletableFuture.completedFuture(null), "pluginName"
            );
            fail("No current graph ID set, this should have thrown an exception.");
        } catch (IllegalStateException ex) {
            assertEquals("Cannot add running plugin. Graph ID is null.", ex.getMessage());
        }

        DataAccessPaneState.setCurrentGraphId(CURRENT_GRAPH_ID);

        // initial state
        assertTrue(DataAccessPaneState.getRunningPlugins().isEmpty());

        final CompletableFuture runningPluginFuture = CompletableFuture.completedFuture(null);

        // add a running plugin
        DataAccessPaneState.addRunningPlugin(
                runningPluginFuture, "pluginName"
        );

        // verify the added plugin is there
        assertEquals(
                DataAccessPaneState.getRunningPlugins(),
                Map.of(runningPluginFuture, "pluginName")
        );

        // remove all running plugins
        DataAccessPaneState.removeAllRunningPlugins();

        // verify there are no running plugins
        assertTrue(DataAccessPaneState.getRunningPlugins().isEmpty());
    }

    @Test
    public void runningPluginsSpecifiedGraph() {
        // no current graph Id
        assertTrue(DataAccessPaneState.getRunningPlugins(null).isEmpty());

        // cannot add with no current graph Id
        try {
            DataAccessPaneState.addRunningPlugin(
                    null, CompletableFuture.completedFuture(null), "pluginName"
            );
            fail("No graph ID set, this should have thrown an exception.");
        } catch (IllegalStateException ex) {
            assertEquals("Cannot add running plugin. Graph ID is null.", ex.getMessage());
        }

        // initial state
        assertTrue(DataAccessPaneState.getRunningPlugins(GRAPH_ID).isEmpty());

        final CompletableFuture runningPluginFuture = CompletableFuture.completedFuture(null);

        // add a running plugin
        DataAccessPaneState.addRunningPlugin(
                GRAPH_ID, runningPluginFuture, "pluginName"
        );

        // verify the added plugin is there
        assertEquals(
                DataAccessPaneState.getRunningPlugins(GRAPH_ID),
                Map.of(runningPluginFuture, "pluginName")
        );

        // remove all running plugins
        DataAccessPaneState.removeAllRunningPlugins(GRAPH_ID);

        // verify there are no running plugins
        assertTrue(DataAccessPaneState.getRunningPlugins(GRAPH_ID).isEmpty());
    }

    @Test
    public void dataAccessPluginComparator() {
        try (final MockedStatic<DataAccessPluginType> dataAccessPluginTypeMockedStatic
                = Mockito.mockStatic(DataAccessPluginType.class)) {
            dataAccessPluginTypeMockedStatic.when(DataAccessPluginType::getTypeWithPosition)
                    .thenReturn(
                            Map.of(
                                    "type1", 1,
                                    "type2", 2,
                                    "type3", 2,
                                    "type4", 3
                            )
                    );

            final DataAccessPaneState.DataAccessPluginComparator comparator
                    = new DataAccessPaneState.DataAccessPluginComparator();

            final DataAccessPlugin plugin1 = mock(DataAccessPlugin.class);
            final DataAccessPlugin plugin2 = mock(DataAccessPlugin.class);

            // TYPE POSITION EQUAL
            when(plugin1.getType()).thenReturn("type2");
            when(plugin2.getType()).thenReturn("type3");

            when(plugin1.getPosition()).thenReturn(1);
            when(plugin2.getPosition()).thenReturn(2);

            assertTrue(comparator.compare(plugin1, plugin2) < 0);

            when(plugin1.getPosition()).thenReturn(2);
            when(plugin2.getPosition()).thenReturn(1);

            assertTrue(comparator.compare(plugin1, plugin2) > 0);

            when(plugin1.getPosition()).thenReturn(1);
            when(plugin2.getPosition()).thenReturn(1);

            assertTrue(comparator.compare(plugin1, plugin2) == 0);

            // TYPE POSITION NOT EQUAL
            when(plugin1.getType()).thenReturn("type1");
            when(plugin2.getType()).thenReturn("type4");

            assertTrue(comparator.compare(plugin1, plugin2) < 0);

            when(plugin1.getType()).thenReturn("type4");
            when(plugin2.getType()).thenReturn("type1");

            assertTrue(comparator.compare(plugin1, plugin2) > 0);
        }
    }

}

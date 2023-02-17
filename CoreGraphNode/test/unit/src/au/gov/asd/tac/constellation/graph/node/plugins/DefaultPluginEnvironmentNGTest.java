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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Default Plugin Environment Test
 *
 * @author arcturus
 */
public class DefaultPluginEnvironmentNGTest {

    private static final Logger LOGGER = Logger.getLogger(DefaultPluginEnvironmentNGTest.class.getName());

    private static MockedConstruction<DefaultPluginInteraction> interactionMocks;

    public DefaultPluginEnvironmentNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }

        interactionMocks = Mockito.mockConstruction(DefaultPluginInteraction.class, (mock, cnxt) -> {
            // Should probably have something in here checking the constructor vars

        });
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        interactionMocks.close();

        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of executePluginLater method, of class DefaultPluginEnvironment.
     */
    @Test
    public void testExecutePluginLaterWithNullAsync() throws ExecutionException, InterruptedException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);
        List<Future<?>> async = null;

        final ExecutorService executorService = mock(ExecutorService.class);
        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginLaterWithAsyncThrowsInterruptedException() throws ExecutionException, InterruptedException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);

        Future<Object> mockedFuture = mock(Future.class);
        List<Future<?>> async = Arrays.asList(mockedFuture);
        when(mockedFuture.get()).thenThrow(InterruptedException.class);

        final ExecutorService executorService = mock(ExecutorService.class);
        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginLaterWithAsyncThrowsExecutionException() throws ExecutionException, InterruptedException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);

        Future<Object> mockedFuture = mock(Future.class);
        List<Future<?>> async = Arrays.asList(mockedFuture);
        when(mockedFuture.get()).thenThrow(ExecutionException.class);

        final ExecutorService executorService = mock(ExecutorService.class);
        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginLaterThrowsInterruptedException() throws ExecutionException, InterruptedException, PluginException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);
        List<Future<?>> async = null;
        InterruptedException interruptedException = mock(InterruptedException.class);
        final ExecutorService executorService = mock(ExecutorService.class);

        doThrow(interruptedException)
                .when(plugin)
                .run(any(PluginGraphs.class), any(PluginInteraction.class), any(PluginParameters.class));

        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginLaterThrowsPluginException() throws ExecutionException, InterruptedException, PluginException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);
        List<Future<?>> async = null;
        PluginException pluginException = mock(PluginException.class);
        final ExecutorService executorService = mock(ExecutorService.class);

        doThrow(pluginException)
                .when(plugin)
                .run(any(PluginGraphs.class), any(PluginInteraction.class), any(PluginParameters.class));

        when(pluginException.getNotificationLevel()).thenReturn(PluginNotificationLevel.FATAL);

        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginLaterThrowsRuntimeException() throws ExecutionException, InterruptedException, PluginException {
        System.out.println("executePluginLater");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);
        List<Future<?>> async = null;
        RuntimeException runtimeException = mock(RuntimeException.class);
        final ExecutorService executorService = mock(ExecutorService.class);

        doThrow(runtimeException)
                .when(plugin)
                .run(any(PluginGraphs.class), any(PluginInteraction.class), any(PluginParameters.class));

        DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();

            return CompletableFuture.completedFuture(null);
        });

        Object expResult = null;
        Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        Object result = future.get();
        assertEquals(result, expResult);
    }

    /**
     * Test of executePluginNow method, of class DefaultPluginEnvironment.
     */
    @Test
    public void testExecutePluginNow() throws Exception {
        System.out.println("executePluginNow");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        Object expResult = null;
        Object result = instance.executePluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test
    public void testExecutePluginNowWithNullGraph() throws Exception {
        System.out.println("executePluginNow");
        Graph graph = null;
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        Object expResult = null;
        Object result = instance.executePluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = PluginException.class)
    public void testExecutePluginNowThrowsPluginException() throws Exception {
        System.out.println("executePluginNow");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        PluginException pluginException = mock(PluginException.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;

        doThrow(pluginException)
                .when(plugin)
                .run(any(PluginGraphs.class), any(PluginInteraction.class), any(PluginParameters.class));

        when(pluginException.getNotificationLevel()).thenReturn(PluginNotificationLevel.FATAL);

        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        instance.executePluginNow(graph, plugin, parameters, interactive);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testExecutePluginNowThrowsRuntimeException() throws Exception {
        System.out.println("executePluginNow");
        Graph graph = mock(Graph.class);
        Plugin plugin = mock(Plugin.class);
        RuntimeException runtimeException = mock(RuntimeException.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;

        doThrow(runtimeException)
                .when(plugin)
                .run(any(PluginGraphs.class), any(PluginInteraction.class), any(PluginParameters.class));

        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        instance.executePluginNow(graph, plugin, parameters, interactive);
    }

    /**
     * Test of executeEditPluginNow method, of class DefaultPluginEnvironment.
     */
    @Test
    public void testExecuteEditPluginNow() throws Exception {
        System.out.println("executeEditPluginNow");
        GraphWriteMethods graph = mock(GraphWriteMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        Object expResult = null;
        Object result = instance.executeEditPluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = InterruptedException.class)
    public void testExecuteEditPluginNowThrowsInterruptedException() throws Exception {
        System.out.println("executeEditPluginNow");
        GraphWriteMethods graph = mock(GraphWriteMethods.class);
        Plugin plugin = mock(Plugin.class);
        InterruptedException interruptedException = mock(InterruptedException.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;

        doThrow(interruptedException)
                .when(plugin)
                .run(any(GraphWriteMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();

        Object expResult = null;
        Object result = instance.executeEditPluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = PluginException.class)
    public void testExecuteEditPluginNowThrowsPluginException() throws Exception {
        System.out.println("executeEditPluginNow");
        GraphWriteMethods graph = mock(GraphWriteMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginException pluginException = mock(PluginException.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;

        doThrow(pluginException)
                .when(plugin)
                .run(any(GraphWriteMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        when(pluginException.getNotificationLevel()).thenReturn(PluginNotificationLevel.FATAL);

        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        instance.executeEditPluginNow(graph, plugin, parameters, interactive);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testExecuteEditPluginNowThrowsRuntimeException() throws Exception {
        System.out.println("executeEditPluginNow");
        GraphWriteMethods graph = mock(GraphWriteMethods.class);
        Plugin plugin = mock(Plugin.class);
        RuntimeException runtimeException = mock(RuntimeException.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;

        doThrow(runtimeException)
                .when(plugin)
                .run(any(GraphWriteMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        instance.executeEditPluginNow(graph, plugin, parameters, interactive);
    }

    /**
     * Test of executeReadPluginNow method, of class DefaultPluginEnvironment.
     */
    @Test
    public void testExecuteReadPluginNow() throws Exception {
        System.out.println("executeReadPluginNow");
        GraphReadMethods graph = mock(GraphReadMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();
        Object expResult = null;
        Object result = instance.executeReadPluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = InterruptedException.class)
    public void testExecuteReadPluginNowThrowsInterruptedException() throws Exception {
        System.out.println("executeReadPluginNow");
        GraphReadMethods graph = mock(GraphReadMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        InterruptedException interruptedException = mock(InterruptedException.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();

        doThrow(interruptedException)
                .when(plugin)
                .run(any(GraphReadMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        Object expResult = null;
        Object result = instance.executeReadPluginNow(graph, plugin, parameters, interactive);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = PluginException.class)
    public void testExecuteReadPluginNowThrowsPluginException() throws Exception {
        System.out.println("executeReadPluginNow");
        GraphReadMethods graph = mock(GraphReadMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        PluginException pluginException = mock(PluginException.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();

        doThrow(pluginException)
                .when(plugin)
                .run(any(GraphReadMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        when(pluginException.getNotificationLevel()).thenReturn(PluginNotificationLevel.FATAL);

        instance.executeReadPluginNow(graph, plugin, parameters, interactive);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testExecuteReadPluginNowThrowsRuntimeException() throws Exception {
        System.out.println("executeReadPluginNow");
        GraphReadMethods graph = mock(GraphReadMethods.class);
        Plugin plugin = mock(Plugin.class);
        PluginParameters parameters = mock(PluginParameters.class);
        RuntimeException runtimeException = mock(RuntimeException.class);
        boolean interactive = false;
        DefaultPluginEnvironment instance = new DefaultPluginEnvironment();

        doThrow(runtimeException)
                .when(plugin)
                .run(any(GraphReadMethods.class), any(PluginInteraction.class), any(PluginParameters.class));

        instance.executeReadPluginNow(graph, plugin, parameters, interactive);
    }
      
    /**
     * Test coverage for DefaultPluginEnvironment where parent and current ThreadConstraints
     * may or may not have plugin reports defined.
     */
    @Test
    public void testExecutePluginLaterWithAndWithoutParentReports() throws ExecutionException, InterruptedException {
        System.out.println("TESTING: executePluginLater with and without parent reports and pre-existing reports");
        final Graph graph = mock(Graph.class);

        final ExecutorService executorService = mock(ExecutorService.class);
        final DefaultPluginEnvironment instance = spy(new DefaultPluginEnvironment());
        doReturn(executorService).when(instance).getPluginExecutor();
        when(executorService.submit(any(Callable.class))).thenAnswer(iom -> {
            final Callable callable = iom.getArgument(0);
            callable.call();
            return CompletableFuture.completedFuture(null);
        });
        final PluginParameters parameters = mock(PluginParameters.class);
        final boolean interactive = false;
        final PluginSynchronizer synchronizer = mock(PluginSynchronizer.class);
        final List<Future<?>> async = null;

        final Plugin plugin = new SimplePlugin("simple_plugin_test") {
            @Override
            protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
                System.out.println("Simple plugin executing ... done");
            }
        };
        final MockedStatic<GraphReportManager> grm = Mockito.mockStatic(GraphReportManager.class);
        final GraphReport testGraphReport = spy(new GraphReport("test_report1"));
        grm.when(() -> GraphReportManager.getGraphReport(any())).thenReturn(testGraphReport);
        final PluginReport testPluginReport = spy(new PluginReport(testGraphReport, plugin));
        doReturn(testPluginReport).when(testGraphReport).addPluginReport((any()));
        final String[] myTags = {"tag_1", "tag_2"};
        doReturn(myTags).when(testPluginReport).getTags();
        
        final ThreadConstraints currentConstraints = spy(new ThreadConstraints());
        final MockedStatic<ThreadConstraints> trc = Mockito.mockStatic(ThreadConstraints.class);
        trc.when(() -> ThreadConstraints.getConstraints()).thenReturn(currentConstraints);
        when(currentConstraints.getCurrentReport()).thenReturn(testPluginReport, testPluginReport, null, testPluginReport, testPluginReport, null, null, null);

        final Object expResult = null;
        System.out.println("TEST settings: parent plugin report defined, and current thread plugin report defined.");
        final Future future = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        final Object result = future.get();
        assertEquals(result, expResult);
        
        System.out.println("TEST settings: parent plugin report NULL, and current thread plugin report defined");
        final Future future2 = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        final Object result2 = future2.get();
        assertEquals(result2, expResult);

        System.out.println("TEST settings: parent plugin report defined, and current thread plugin report NULL");
        final Future future3 = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        final Object result3 = future3.get();
        assertEquals(result3, expResult);

        System.out.println("TEST settings: parent plugin report NULL, and current thread plugin report NULL");
        final Future future4 = instance.executePluginLater(graph, plugin, parameters, interactive, async, synchronizer);
        final Object result4 = future4.get();
        assertEquals(result4, expResult);

        verify(currentConstraints, atLeast(8)).getCurrentReport();
        System.out.println("TEST COMPLETE");
    }    
}

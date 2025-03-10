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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javax.swing.Icon;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.openide.awt.NotificationDisplayer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class WaitForQueriesToCompleteTaskNGTest {
    
    private static final String GRAPH_ID = "graphId";
    
    private static MockedStatic<DataAccessPaneState> paneStateMockedStatic;
    private static MockedStatic<Platform> platformMockedStatic;
    private static MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic;
    
    private DataAccessPane dataAccessPane;
    
    private WaitForQueriesToCompleteTask waitForQueriesToCompleteTask;

    @BeforeClass
    public static void setUpClass() throws Exception {
        paneStateMockedStatic = Mockito.mockStatic(DataAccessPaneState.class);
        platformMockedStatic = Mockito.mockStatic(Platform.class);
        notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        paneStateMockedStatic.close();
        platformMockedStatic.close();
        notificationDisplayerMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessPane = mock(DataAccessPane.class);
        
        waitForQueriesToCompleteTask = new WaitForQueriesToCompleteTask(dataAccessPane, GRAPH_ID);
        
        platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                .thenAnswer(iom -> {
                    final Runnable runnable = iom.getArgument(0);

                    runnable.run();

                    return null;
                });
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        paneStateMockedStatic.reset();
        platformMockedStatic.reset();
        notificationDisplayerMockedStatic.reset();
    }
    
    @Test
    public void run() {
        paneStateMockedStatic.when(() -> DataAccessPaneState.getRunningPlugins(GRAPH_ID))
                .thenReturn(
                        Map.of(
                                CompletableFuture.supplyAsync(() -> "I'm Complete"), "plugin1",
                                CompletableFuture.supplyAsync(() -> "I'm Complete"), "plugin2"
                        )
                );
        paneStateMockedStatic.when(DataAccessPaneState::getCurrentGraphId).thenReturn(GRAPH_ID);

        waitForQueriesToCompleteTask.run();

        paneStateMockedStatic.verify(() -> DataAccessPaneState.setQueriesRunning(GRAPH_ID, false));
        paneStateMockedStatic.verify(() -> DataAccessPaneState.removeAllRunningPlugins(GRAPH_ID));
        verify(dataAccessPane).update();
    }
    
    @Test
    public void run_not_on_current_graph() {
        paneStateMockedStatic.when(() -> DataAccessPaneState.getRunningPlugins(GRAPH_ID))
                .thenReturn(
                        Map.of(
                                CompletableFuture.supplyAsync(() -> "I'm Complete"), "plugin1",
                                CompletableFuture.supplyAsync(() -> "I'm Complete"), "plugin2"
                        )
                );
        paneStateMockedStatic.when(DataAccessPaneState::getCurrentGraphId).thenReturn("currentGraphId");

        waitForQueriesToCompleteTask.run();

        paneStateMockedStatic.verify(() -> DataAccessPaneState.setQueriesRunning(GRAPH_ID, false));
        paneStateMockedStatic.verify(() -> DataAccessPaneState.removeAllRunningPlugins(GRAPH_ID));
        verify(dataAccessPane, never()).update();
    }
    
    @Test
    public void run_plugin_exception() {
        paneStateMockedStatic.when(() -> DataAccessPaneState.getRunningPlugins(GRAPH_ID))
                .thenReturn(
                        Map.of(
                                CompletableFuture.supplyAsync(() -> "I'm Complete"), "plugin1",
                                CompletableFuture.supplyAsync(() -> {throw new RuntimeException("I am an error");}), "plugin2"
                        )
                );
        paneStateMockedStatic.when(DataAccessPaneState::getCurrentGraphId).thenReturn(GRAPH_ID);
        
        final NotificationDisplayer notificationDisplayer = mock(NotificationDisplayer.class);
        notificationDisplayerMockedStatic.when(NotificationDisplayer::getDefault)
                .thenReturn(notificationDisplayer);
        doReturn(null).when(notificationDisplayer)
                .notify(anyString(), any(Icon.class), anyString(), isNull(), eq(NotificationDisplayer.Priority.HIGH));

        waitForQueriesToCompleteTask.run();

        verify(notificationDisplayer).notify(
                "Data Access Plug-In 'plugin2' Errored. Did NOT Finish.",
                UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                "I am an error",
                null,
                NotificationDisplayer.Priority.HIGH
        );
        
        paneStateMockedStatic.verify(() -> DataAccessPaneState.setQueriesRunning(GRAPH_ID, false));
        paneStateMockedStatic.verify(() -> DataAccessPaneState.removeAllRunningPlugins(GRAPH_ID));
        verify(dataAccessPane).update();
    }
}

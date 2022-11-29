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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javax.swing.Icon;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
public class ShowDataAccessPluginTaskNGTest {
    private static final String PLUGIN_NAME = "pluginName";
    
    private static MockedStatic<DataAccessUtilities> dataAccessUtilsMockedStatic;
    private static MockedStatic<DataAccessTabPane> dataAccessTabPaneMockedStatic;
    private static MockedStatic<Platform> platformMockedStatic;
    private static MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic;
    
    private DataAccessPane dataAccessPane;
    private NotificationDisplayer notificationDisplayer;
    
    private ShowDataAccessPluginTask showDataAccessPluginTask;

    @BeforeClass
    public static void setUpClass() throws Exception {
        dataAccessUtilsMockedStatic = Mockito.mockStatic(DataAccessUtilities.class);
        dataAccessTabPaneMockedStatic = Mockito.mockStatic(DataAccessTabPane.class);
        platformMockedStatic = Mockito.mockStatic(Platform.class);
        notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        dataAccessUtilsMockedStatic.close();
        dataAccessTabPaneMockedStatic.close();
        platformMockedStatic.close();
        notificationDisplayerMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessPane = mock(DataAccessPane.class);
        
        showDataAccessPluginTask = new ShowDataAccessPluginTask(PLUGIN_NAME);
        
        dataAccessUtilsMockedStatic.when(DataAccessUtilities::getDataAccessPane)
                .thenReturn(dataAccessPane);
        
        platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                .thenAnswer(iom -> {
                    final Runnable runnable = iom.getArgument(0);

                    runnable.run();

                    return null;
                });
        
        notificationDisplayer = mock(NotificationDisplayer.class);
        notificationDisplayerMockedStatic.when(NotificationDisplayer::getDefault)
                .thenReturn(notificationDisplayer);
        when(notificationDisplayer.notify(anyString(), any(Icon.class), anyString(), isNull()))
                .thenReturn(null);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        dataAccessUtilsMockedStatic.reset();
        dataAccessTabPaneMockedStatic.reset();
        platformMockedStatic.reset();
        notificationDisplayerMockedStatic.reset();
    }
    
    @Test
    public void run() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);
        
        dataAccessTabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(currentTab))
                .thenReturn(queryPhasePane);
        
        showDataAccessPluginTask.run();
        
        verify(queryPhasePane).expandPlugin(PLUGIN_NAME);
    }
    
    @Test
    public void run_data_access_pane_null() {
        dataAccessUtilsMockedStatic.when(DataAccessUtilities::getDataAccessPane)
                .thenReturn(null);
        
        showDataAccessPluginTask.run();
        
        verify(notificationDisplayer).notify(
                "Data Access View",
                UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                "Please open the Data Access view and create a step.",
                null);
    }
    
    @Test
    public void run_current_tab_null() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(null);
        
        showDataAccessPluginTask.run();
        
        verify(notificationDisplayer).notify(
                "Data Access View",
                UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                "Please create a step in the Data Access view.",
                null);
    }
}

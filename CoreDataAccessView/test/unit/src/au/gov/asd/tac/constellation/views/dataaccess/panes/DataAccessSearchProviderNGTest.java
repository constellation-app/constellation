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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.qs.QuickSearchUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.ShowDataAccessPluginTask;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import javafx.scene.control.Tab;
import javax.swing.Icon;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DataAccessSearchProviderNGTest extends ConstellationTest {

    SearchResponse response;
    SearchRequest request;
    DataAccessPane daPane;
    DataAccessTabPane datPane;
    QueryPhasePane qpPane;

    protected NotificationDisplayer notifDisplayer;

    private static final Icon WARNING_ICON = UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor());

    @BeforeMethod
    public void setUpMethod() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Check usage is correct before moving on
        validateMockitoUsage();
    }

    /**
     * Test of evaluate method, of class DataAccessSearchProvider. Pass in an
     * unmatchable search string
     */
    @Test
    public void testEvaluateFail() {
        System.out.println("evaluate fail");

        // Creating mocks
        daPane = mock(DataAccessPane.class);
        datPane = mock(DataAccessTabPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessTabPane> mockedStatic = Mockito.mockStatic(DataAccessTabPane.class)) {
            mockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(Mockito.any(Tab.class)))
                    .thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getDataAccessTabPane()).thenReturn(datPane);
            when(datPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock the request text to be an unmatchable string
        when(request.getText()).thenReturn("nothignshouldmatchthisstring");
        when(response.addResult(Mockito.any(), Mockito.anyString())).thenReturn(true);
        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was never called.
        // This should mean that no plugin name matched the input
        verify(response, never()).addResult(Mockito.any(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class DataAccessSearchProvider. Pass in an
     * unmatchable search string
     */
    @Test
    public void testEvaluateNull() {
        System.out.println("evaluate null");

        // Creating mocks
        daPane = mock(DataAccessPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessTabPane> mockedStatic = Mockito.mockStatic(DataAccessTabPane.class)) {
            mockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(Mockito.any(Tab.class)))
                    .thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getDataAccessTabPane()).thenReturn(datPane);
            when(datPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock the request text to be null
        when(request.getText()).thenReturn(null);
        when(response.addResult(Mockito.any(), Mockito.anyString())).thenReturn(true);
        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was never called.
        // This should mean that no plugin name matched the input
        verify(response, never()).addResult(Mockito.any(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class DataAccessSearchProvider.
     */
    @Test
    public void testEvaluateSuccess() {
        System.out.println("evaluate success");

        // Creating mocks
        daPane = mock(DataAccessPane.class);
        datPane = mock(DataAccessTabPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessTabPane> mockedStatic = Mockito.mockStatic(DataAccessTabPane.class)) {
            mockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(Mockito.any(Tab.class)))
                    .thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getDataAccessTabPane()).thenReturn(datPane);
            when(datPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock request to return the selected text
        when(request.getText()).thenReturn("Select");

        // Return a valid response when results are added that match the expected
        when(response.addResult(Mockito.any(), Mockito.eq(QuickSearchUtilities.CIRCLED_D + "  Select Top N"))).thenReturn(true);
        when(response.addResult(Mockito.any(), Mockito.eq(QuickSearchUtilities.CIRCLED_D + "  Select All"))).thenReturn(true);

        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was called on the correct plugins
        verify(response, times(1)).addResult(Mockito.any(), Mockito.eq(QuickSearchUtilities.CIRCLED_D + "  Select Top N"));
        verify(response, times(1)).addResult(Mockito.any(), Mockito.eq(QuickSearchUtilities.CIRCLED_D + "  Select All"));
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////  Start of PluginDisplayer Tests  ///////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Test of run method, of class PluginDisplayer.
     */
    @Test
    public void testRun1() {
        System.out.println("testRun1 testing query phase pane was successfully returned");

        // Setting up mocks
        daPane = mock(DataAccessPane.class);
        datPane = mock(DataAccessTabPane.class);
        qpPane = mock(QueryPhasePane.class);

        // Do nothing when the plugin is called to expand
        // May never get here with the code existing in the Platform thread.
        doNothing().when(qpPane).expandPlugin(Mockito.eq("SelectTopN"));

        // Mock the static method getQueryPhasePane to return the mocked QueryPhasePane
        try (MockedStatic<DataAccessTabPane> mockedStatic = Mockito.mockStatic(DataAccessTabPane.class)) {
            mockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(Mockito.any(Tab.class)))
                    .thenReturn(qpPane);

            // Return a new tab when the DataAccessPane mock is prompted for getCurrentTab
            when(daPane.getDataAccessTabPane()).thenReturn(datPane);
            when(datPane.getCurrentTab()).thenReturn(new Tab());

            // Mock the static method getDataAccessPane and return the mocked DataAccessPane
            try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
                mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);

                try {
                    ShowDataAccessPluginTask pd = new ShowDataAccessPluginTask("SelectTopN");
                    pd.run();
                } catch (final IllegalStateException ex) {
                    // Catch the exception which happens when executing Platform thread in standalone tests
                    // Fail for any other exception
                }

                // Verify that getCurrentTab was succcessfully called.
                verify(datPane, times(1)).getCurrentTab();

                // Verify that getQueryPhasePane was succcessfully called.
                mockedStatic.verify(() -> DataAccessTabPane.getQueryPhasePane(Mockito.any()));
            }
        }
    }

    /**
     * Test of run method, of class PluginDisplayer. Testing when tab is null
     */
    @Test
    public void testRun2() {
        System.out.println("testRun2 testing null tab returned");

        // Setting up mocks
        daPane = mock(DataAccessPane.class);
        datPane = mock(DataAccessTabPane.class);
        notifDisplayer = mock(NotificationDisplayer.class);
        final Notification notif = new Notification() {
            @Override
            public void clear() {
                // do nothing, used for testing.
            }
        };

        // Setting up a mock for DataAccessUtilities to return null when fetching the pane.
        try (MockedStatic<DataAccessUtilities> mockedStatic1 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getDataAccessTabPane()).thenReturn(datPane);
            when(datPane.getCurrentTab()).thenReturn(null);
            mockedStatic1.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);

            // Mock the static method getDefault() to return the mock of NotificationDisplayer
            try (MockedStatic<NotificationDisplayer> mockedStatic3 = Mockito.mockStatic(NotificationDisplayer.class)) {
                when(notifDisplayer.notify("Data Access view", WARNING_ICON, "Please create a step in the Data Access view.", null)).thenReturn(notif);
                mockedStatic3.when(() -> NotificationDisplayer.getDefault()).thenReturn(notifDisplayer);

                ShowDataAccessPluginTask pd = new ShowDataAccessPluginTask("SelectTopN");
                pd.run();

                // Verify that the current tab was attempted to be retrieved
                verify(datPane, times(1)).getCurrentTab();

                // verify notificationdisplayer was called with correct string
                verify(notifDisplayer, times(1)).notify(Mockito.anyString(), Mockito.eq(WARNING_ICON), Mockito.eq("Please create a step in the Data Access view."), Mockito.eq(null));
            }
        }
    }

    /**
     * Test of run method, of class PluginDisplayer. Testing when daPane is null
     */
    @Test
    public void testRun3() {
        System.out.println("testRun3 null data access pane");

        // Setting up mocks
        notifDisplayer = mock(NotificationDisplayer.class);
        final Notification notif = new Notification() {
            @Override
            public void clear() {
                // do nothing, used for testing.
            }
        };

        // Setting up a mock for DataAccessUtilities to return null when fetching the pane.
        try (MockedStatic<DataAccessUtilities> mockedStatic1 = Mockito.mockStatic(DataAccessUtilities.class)) {
            mockedStatic1.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(null);

            // Mock the static method getDefault() to return the mock of NotificationDisplayer
            try (MockedStatic<NotificationDisplayer> mockedStatic3 = Mockito.mockStatic(NotificationDisplayer.class)) {
                when(notifDisplayer.notify("Data Access view", WARNING_ICON, "Please open the Data Access view and create a step.", null)).thenReturn(notif);
                mockedStatic3.when(() -> NotificationDisplayer.getDefault()).thenReturn(notifDisplayer);

                ShowDataAccessPluginTask pd = new ShowDataAccessPluginTask("SelectTopN");
                pd.run();

                // verify notificationdisplayer was called with correct string
                verify(notifDisplayer, times(1)).notify(Mockito.anyString(), Mockito.eq(WARNING_ICON), Mockito.eq("Please open the Data Access view and create a step."), Mockito.eq(null));
            }
        }
    }
}

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
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessSearchProvider.PluginDisplayer;
import javafx.scene.control.Tab;
import org.mockito.Mock;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class DataAccessSearchProviderNGTest {

    @Mock
    SearchResponse response;

    @Mock
    SearchRequest request;

    @Mock
    DataAccessPane daPane;

    @Mock
    QueryPhasePane qpPane;

    protected NotificationDisplayer notifDisplayer;

    public DataAccessSearchProviderNGTest() {
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
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
        MockitoAnnotations.initMocks(this);
        daPane = mock(DataAccessPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessPane> mockedStatic = Mockito.mockStatic(DataAccessPane.class)) {
            mockedStatic.when(() -> DataAccessPane.getQueryPhasePane(Mockito.any())).thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock the request text to be an unmatchable string
        when(request.getText()).thenReturn("nothignshouldmatchthisstring");
        when(response.addResult(Mockito.anyObject(), Mockito.anyString())).thenReturn(true);
        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was never called.
        // This should mean that no plugin name matched the input
        verify(response, never()).addResult(Mockito.anyObject(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class DataAccessSearchProvider. Pass in an
     * unmatchable search string
     */
    @Test
    public void testEvaluateNull() {
        System.out.println("evaluate null");

        // Creating mocks
        MockitoAnnotations.initMocks(this);
        daPane = mock(DataAccessPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessPane> mockedStatic = Mockito.mockStatic(DataAccessPane.class)) {
            mockedStatic.when(() -> DataAccessPane.getQueryPhasePane(Mockito.any())).thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock the request text to be null
        when(request.getText()).thenReturn(null);
        when(response.addResult(Mockito.anyObject(), Mockito.anyString())).thenReturn(true);
        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was never called.
        // This should mean that no plugin name matched the input
        verify(response, never()).addResult(Mockito.anyObject(), Mockito.anyString());
    }

    /**
     * Test of evaluate method, of class DataAccessSearchProvider.
     */
    @Test
    public void testEvaluateSuccess() {
        System.out.println("evaluate success");

        // Creating mocks
        MockitoAnnotations.initMocks(this);
        daPane = mock(DataAccessPane.class);
        qpPane = mock(QueryPhasePane.class);
        request = mock(SearchRequest.class);
        response = mock(SearchResponse.class);

        // Create mock of DataAccessPane to return the query phase pane mock
        try (MockedStatic<DataAccessPane> mockedStatic = Mockito.mockStatic(DataAccessPane.class)) {
            mockedStatic.when(() -> DataAccessPane.getQueryPhasePane(Mockito.any())).thenReturn(qpPane);
        }

        // Create mock of DataAccessUtilities to return the data access pane mock
        // Mock of DataAccessPane will return a blank new tab when called
        try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getCurrentTab()).thenReturn(new Tab());
            mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);
        }

        // Mock request to return the selected text
        when(request.getText()).thenReturn("Select");

        // Return a valid response when results are added that match the expected
        when(response.addResult(Mockito.anyObject(), Mockito.eq("Select Top N"))).thenReturn(true);
        when(response.addResult(Mockito.anyObject(), Mockito.eq("Select All"))).thenReturn(true);

        DataAccessSearchProvider instance = new DataAccessSearchProvider();
        instance.evaluate(request, response);

        // Verify that addResult was called on the correct plugins
        verify(response, times(1)).addResult(Mockito.anyObject(), Mockito.eq("Select Top N"));
        verify(response, times(1)).addResult(Mockito.anyObject(), Mockito.eq("Select All"));
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
        MockitoAnnotations.initMocks(this);
        daPane = mock(DataAccessPane.class);
        qpPane = mock(QueryPhasePane.class);

        // Do nothing when the plugin is called to expand
        // May never get here with the code existing in the Platform thread.
        doNothing().when(qpPane).expandPlugin(Mockito.eq("SelectTopN"));

        // Mock the static method getQueryPhasePane to return the mocked QueryPhasePane
        try (MockedStatic<DataAccessPane> mockedStatic = Mockito.mockStatic(DataAccessPane.class)) {
            mockedStatic.when(() -> DataAccessPane.getQueryPhasePane(Mockito.anyObject())).thenReturn(qpPane);

            // Return a new tab when the DataAccessPane mock is prompted for getCurrentTab
            when(daPane.getCurrentTab()).thenReturn(new Tab());

            // Mock the static method getDataAccessPane and return the mocked DataAccessPane
            try (MockedStatic<DataAccessUtilities> mockedStatic2 = Mockito.mockStatic(DataAccessUtilities.class)) {
                mockedStatic2.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);

                try {
                    PluginDisplayer pd = new PluginDisplayer("SelectTopN");
                    pd.run();
                } catch (final IllegalStateException ex) {
                    // Catch the exception which happens when executing Platform thread in standalone tests
                    // Fail for any other exception
                }

                // Verify that getCurrentTab was succcessfully called.
                verify(daPane, times(1)).getCurrentTab();

                // Verify that getQueryPhasePane was succcessfully called.
                mockedStatic.verify(() -> DataAccessPane.getQueryPhasePane(Mockito.anyObject()));
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
        MockitoAnnotations.initMocks(this);
        daPane = mock(DataAccessPane.class);
        notifDisplayer = mock(NotificationDisplayer.class);
        final Notification notif = new Notification() {
            @Override
            public void clear() {
                // do nothing, used for testing.
            }
        };

        // Setting up a mock for DataAccessUtilities to return null when fetching the pane.
        try (MockedStatic<DataAccessUtilities> mockedStatic1 = Mockito.mockStatic(DataAccessUtilities.class)) {
            when(daPane.getCurrentTab()).thenReturn(null);
            mockedStatic1.when(() -> DataAccessUtilities.getDataAccessPane()).thenReturn(daPane);

            // Mock the static method getDefault() to return the mock of NotificationDisplayer
            try (MockedStatic<NotificationDisplayer> mockedStatic3 = Mockito.mockStatic(NotificationDisplayer.class)) {
                when(notifDisplayer.notify("Data Access view", UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()), PluginDisplayer.STEP_STRING, null)).thenReturn(notif);
                mockedStatic3.when(() -> NotificationDisplayer.getDefault()).thenReturn(notifDisplayer);

                PluginDisplayer pd = new PluginDisplayer("SelectTopN");
                pd.run();

                // Verify that the current tab was attempted to be retrieved
                verify(daPane, times(1)).getCurrentTab();

                // verify notificationdisplayer was called with correct string
                verify(notifDisplayer, times(1)).notify(Mockito.anyString(), Mockito.eq(UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor())), Mockito.eq(PluginDisplayer.STEP_STRING), Mockito.eq(null));
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
        MockitoAnnotations.initMocks(this);
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
                when(notifDisplayer.notify("Data Access view", UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()), PluginDisplayer.DAV_STEP_STRING, null)).thenReturn(notif);
                mockedStatic3.when(() -> NotificationDisplayer.getDefault()).thenReturn(notifDisplayer);

                PluginDisplayer pd = new PluginDisplayer("SelectTopN");
                pd.run();

                // verify notificationdisplayer was called with correct string
                verify(notifDisplayer, times(1)).notify(Mockito.anyString(), Mockito.eq(UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor())), Mockito.eq(PluginDisplayer.DAV_STEP_STRING), Mockito.eq(null));
            }
        }
    }
}

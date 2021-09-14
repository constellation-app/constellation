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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewTopComponent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessUtilitiesNGTest {

    private static MockedStatic<SwingUtilities> swingUtilitiesStaticMock;
    private static MockedStatic<WindowManager> windowManagerStaticMock;
    private static MockedStatic<DataAccessUtilities> dataAccessUtilitiesStaticMock;

    public DataAccessUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        swingUtilitiesStaticMock = Mockito.mockStatic(SwingUtilities.class);
        windowManagerStaticMock = Mockito.mockStatic(WindowManager.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        swingUtilitiesStaticMock.close();
        windowManagerStaticMock.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        swingUtilitiesStaticMock.reset();
        windowManagerStaticMock.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testGetDataAccessPaneCalledByEventDispatchThread() {
        final WindowManager windowManager = mock(WindowManager.class);
        final DataAccessViewTopComponent topComponent = mock(DataAccessViewTopComponent.class);
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(topComponent);
        when(topComponent.isOpened()).thenReturn(false);
        when(topComponent.getDataAccessPane()).thenReturn(dataAccessPane);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        verify(topComponent, times(1)).open();
        verify(topComponent, times(1)).requestVisible();

        assertSame(actual, dataAccessPane);
    }

    @Test
    public void testGetDataAccessPaneCalledByEventDispatchThreadTopComponentNull() {
        final WindowManager windowManager = mock(WindowManager.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(true);

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(null);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertNull(actual);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThread() {
        final WindowManager windowManager = mock(WindowManager.class);
        final DataAccessViewTopComponent topComponent = mock(DataAccessViewTopComponent.class);
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);

        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);

        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenAnswer(invocation -> {
                    final Runnable r = invocation.getArgument(0);
                    r.run();
                    return null;
                });

        windowManagerStaticMock.when(WindowManager::getDefault).thenReturn(windowManager);

        when(windowManager.findTopComponent(DataAccessViewTopComponent.class.getSimpleName())).thenReturn(topComponent);
        when(topComponent.isOpened()).thenReturn(false);
        when(topComponent.getDataAccessPane()).thenReturn(dataAccessPane);

        DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertSame(actual, dataAccessPane);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThreadError() {
        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);
        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenThrow(new InvocationTargetException(new RuntimeException("Something Bad")));

        final DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertNull(actual);
    }

    @Test
    public void testGetDataAccessPaneNotCalledByEventDispatchThreadInterruptError() {
        swingUtilitiesStaticMock.when(SwingUtilities::isEventDispatchThread).thenReturn(false);
        swingUtilitiesStaticMock.when(() -> SwingUtilities.invokeAndWait(any(Runnable.class)))
                .thenThrow(new InterruptedException());

        final DataAccessPane actual = DataAccessUtilities.getDataAccessPane();

        assertTrue(Thread.interrupted());
    }
}

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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class ScreenWindowsHelperNGTest {

    final int testPointXY = 10;
    final int testSizeWidthHeight = 100;

    @Test
    public void testGetMainWindowCentrePointNull() {
        //Test with no windows, should return null
        Assert.assertTrue(ScreenWindowsHelper.getMainWindowCentrePoint() == null);
    }

    @Test
    public void testGetMainWindowCentrePointMainWindow() {

        // Test method with mocked Window and dimensions
        final Window returnedWindow = mock(Window.class);
        final Dimension testDimension = new Dimension(testSizeWidthHeight,
                testSizeWidthHeight);

        when(returnedWindow.getName()).thenReturn("MainWindow");
        when(returnedWindow.getX()).thenReturn(testPointXY);
        when(returnedWindow.getY()).thenReturn(testPointXY);
        when(returnedWindow.getSize()).thenReturn(testDimension);

        try (final MockedStatic<Window> mockedWindowStatic = Mockito.mockStatic(Window.class)) {
            mockedWindowStatic.when(Window::getWindows).thenReturn(new Window[]{returnedWindow});

            Point testReturnedPoint = ScreenWindowsHelper.getMainWindowCentrePoint();
            Assert.assertEquals((int) testReturnedPoint.getX(), testPointXY + (testSizeWidthHeight / 2));
            Assert.assertEquals((int) testReturnedPoint.getY(), testPointXY + (testSizeWidthHeight / 2));
        }
    }

    @Test
    public void testGetMainWindowCentrePointNotMainWindow() {

        // test the case when it's not main window, should return null
        final Window returnedWindow = mock(Window.class);
        final Dimension testDimension = new Dimension(testSizeWidthHeight,
                testSizeWidthHeight);

        when(returnedWindow.getX()).thenReturn(testPointXY);
        when(returnedWindow.getY()).thenReturn(testPointXY);
        when(returnedWindow.getSize()).thenReturn(testDimension);
        when(returnedWindow.getName()).thenReturn("TestWindow");

        try (final MockedStatic<Window> mockedWindowStatic = Mockito.mockStatic(Window.class)) {
            mockedWindowStatic.when(Window::getWindows).thenReturn(new Window[]{returnedWindow});

            Point testReturnedPoint = ScreenWindowsHelper.getMainWindowCentrePoint();
            Assert.assertEquals(testReturnedPoint, null);
        }
    }

    @Test
    public void testGetMainWindowCentrePointMockedPoint() {

        // Test method with mocked point
        final Point point = mock(Point.class);
        try (final MockedStatic<ScreenWindowsHelper> screenWindowsHelperStatic = Mockito.mockStatic(ScreenWindowsHelper.class)) {
            screenWindowsHelperStatic.when(ScreenWindowsHelper::getMainWindowCentrePoint).thenReturn(point);
            Assert.assertEquals(ScreenWindowsHelper.getMainWindowCentrePoint(), point);
        }
    }
}

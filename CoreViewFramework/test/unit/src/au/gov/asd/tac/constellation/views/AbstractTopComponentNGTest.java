/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views;

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Test class of AbstractTopComponent.
 *
 * @author sol695510
 */
public class AbstractTopComponentNGTest {

    /**
     * Test of createFloatingSize method, of class AbstractTopComponent.
     */
    @Test
    public void testCreateFloatingSize() {
        System.out.println("createFloatingSize");

        final AbstractTopComponent instance = new TestTopComponent();

        final Frame landscapeWindowMock = mock(Frame.class);
        when(landscapeWindowMock.getX()).thenReturn(0);
        when(landscapeWindowMock.getY()).thenReturn(0);
        when(landscapeWindowMock.getWidth()).thenReturn(1920);
        when(landscapeWindowMock.getHeight()).thenReturn(1080);

        final Frame portraitWindowMock = mock(Frame.class);
        when(portraitWindowMock.getX()).thenReturn(0);
        when(portraitWindowMock.getY()).thenReturn(0);
        when(portraitWindowMock.getWidth()).thenReturn(1080);
        when(portraitWindowMock.getHeight()).thenReturn(1920);

        // Test for bottom opening floating top components.
        final Dimension actual = instance.createFloatingSize(landscapeWindowMock, "output");
        final Dimension expected = new Dimension(
                Math.round(landscapeWindowMock.getWidth() * 0.8F),
                Math.round(landscapeWindowMock.getHeight() * 0.3F)
        );

        assertEquals(actual, expected);

        // Test for side opening floating top components when the Constellation window is landscape.
        final Dimension landscapeActual = instance.createFloatingSize(landscapeWindowMock, "");
        final Dimension landscapeExpected = new Dimension(
                Math.round(landscapeWindowMock.getWidth() * 0.3F),
                Math.round(landscapeWindowMock.getHeight() * 0.8F)
        );

        assertEquals(landscapeActual, landscapeExpected);

        // Test for side opening floating top components when the Constellation window is portait.
        final Dimension portraitActual = instance.createFloatingSize(portraitWindowMock, "");
        final Dimension portraitExpected = new Dimension(
                Math.round(portraitWindowMock.getWidth() * 0.3F),
                Math.round(portraitWindowMock.getHeight() * 0.8F)
        );

        assertEquals(portraitActual, portraitExpected);
    }

    /**
     * Test of createFloatingLocation method, of class AbstractTopComponent.
     */
    @Test
    public void testCreateFloatingLocation() {
        System.out.println("createFloatingLocation");

        final AbstractTopComponent instance = new TestTopComponent();

        final AbstractTopComponent instance1 = new TestTopComponent();
        final AbstractTopComponent instance2 = new TestTopComponent();
        final AbstractTopComponent instance3 = new TestTopComponent();
        final AbstractTopComponent instance4 = new TestTopComponent();
        final AbstractTopComponent instance5 = new TestTopComponent();

        final Frame mainWindowMock = mock(Frame.class);
        when(mainWindowMock.getX()).thenReturn(0);
        when(mainWindowMock.getY()).thenReturn(0);
        when(mainWindowMock.getWidth()).thenReturn(1920);
        when(mainWindowMock.getHeight()).thenReturn(1080);

        final Window thisWindowMock = mock(Window.class);
        when(thisWindowMock.getX()).thenReturn(0);
        when(thisWindowMock.getY()).thenReturn(0);
        when(thisWindowMock.getWidth()).thenReturn(100);
        when(thisWindowMock.getHeight()).thenReturn(100);

        final Registry registryMock = mock(Registry.class);
        when(registryMock.getOpened()).thenReturn(Set.of(instance1, instance2, instance3, instance4, instance5));

        final WindowManager windowManagerMock = mock(WindowManager.class);

        try (MockedStatic<TopComponent> topComponentMockedStatic = mockStatic(TopComponent.class); MockedStatic<WindowManager> windowManagerMockedStatic = mockStatic(WindowManager.class)) {
            topComponentMockedStatic.when(TopComponent::getRegistry).thenReturn(registryMock);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManagerMock);

            // The first floating top component opened.
            when(windowManagerMock.isTopComponentFloating(instance1)).thenReturn(Boolean.FALSE);
            when(windowManagerMock.isTopComponentFloating(instance2)).thenReturn(Boolean.FALSE);
            when(windowManagerMock.isTopComponentFloating(instance3)).thenReturn(Boolean.FALSE);
            when(windowManagerMock.isTopComponentFloating(instance4)).thenReturn(Boolean.FALSE);
            when(windowManagerMock.isTopComponentFloating(instance5)).thenReturn(Boolean.FALSE);

            final Point firstLocation = new Point(
                    thisWindowMock.getX(),
                    thisWindowMock.getY()
            );

            final Point firstActual = instance.createFloatingLocation(mainWindowMock, thisWindowMock);
            final Point firstExpected = firstLocation;

            assertEquals(firstActual, firstExpected);

            // A random position floating top component opened.
            final Random random = new Random();

            final boolean boolean1 = random.nextBoolean();
            final boolean boolean2 = random.nextBoolean();
            final boolean boolean3 = random.nextBoolean();
            final boolean boolean4 = random.nextBoolean();
            final boolean boolean5 = random.nextBoolean();

            final int trueCount = Collections.frequency(List.of(boolean1, boolean2, boolean3, boolean4, boolean5), true);

            when(windowManagerMock.isTopComponentFloating(instance1)).thenReturn(boolean1);
            when(windowManagerMock.isTopComponentFloating(instance2)).thenReturn(boolean2);
            when(windowManagerMock.isTopComponentFloating(instance3)).thenReturn(boolean3);
            when(windowManagerMock.isTopComponentFloating(instance4)).thenReturn(boolean4);
            when(windowManagerMock.isTopComponentFloating(instance5)).thenReturn(boolean5);

            final Point randomLocation = new Point(
                    thisWindowMock.getX() + ((mainWindowMock.getWidth() / 50) * trueCount),
                    thisWindowMock.getY() + ((mainWindowMock.getHeight() / 50) * trueCount)
            );

            final Point randomActual = instance.createFloatingLocation(mainWindowMock, thisWindowMock);
            final Point randomExpected = randomLocation;

            assertEquals(randomActual, randomExpected);

            // The sixth floating top component opened.
            when(windowManagerMock.isTopComponentFloating(instance1)).thenReturn(Boolean.TRUE);
            when(windowManagerMock.isTopComponentFloating(instance2)).thenReturn(Boolean.TRUE);
            when(windowManagerMock.isTopComponentFloating(instance3)).thenReturn(Boolean.TRUE);
            when(windowManagerMock.isTopComponentFloating(instance4)).thenReturn(Boolean.TRUE);
            when(windowManagerMock.isTopComponentFloating(instance5)).thenReturn(Boolean.TRUE);

            final Point sixthLocation = new Point(
                    thisWindowMock.getX(),
                    thisWindowMock.getY()
            );

            final Point sixthActual = instance.createFloatingLocation(mainWindowMock, thisWindowMock);
            final Point sixthExpected = sixthLocation;

            assertEquals(sixthActual, sixthExpected);
        }
    }

    /**
     * Implementation of AbstractTopComponent for testing.
     */
    public class TestTopComponent extends AbstractTopComponent {

        @Override
        public void initContent() {
            // Intentionally left blank.
        }

        @Override
        public Object createContent() {
            return null;
        }

        @Override
        public Tuple<String, Boolean> getDefaultFloatingInfo() {
            return null;
        }

        @Override
        public String getModeName() {
            return "";
        }
    }
}

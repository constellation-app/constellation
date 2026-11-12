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

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
package au.gov.asd.tac.constellation.views.timeline.components;

import au.gov.asd.tac.constellation.views.timeline.TimelinePanel;
import au.gov.asd.tac.constellation.views.timeline.TimelineTopComponent;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.ScrollEvent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class TimelineChartNGTest {

    private static final Logger LOGGER = Logger.getLogger(TimelineChartNGTest.class.getName());

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of performZoom method, of class TimelineChart.
     */
    @Test
    public void testPerformZoomInvalidScrollEvent() {
        System.out.println("performZoom with invalid scroll event");

        final ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        when(mockScrollEvent.getEventType()).thenReturn(ScrollEvent.ANY);

        final TimelinePanel parent = mock(TimelinePanel.class);
        final Axis<Number> xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        final double mouseX = 0.0;

        final TimelineChart instance = new TimelineChart(parent, xAxis, yAxis);
        instance.performZoom(mockScrollEvent, mouseX);

        verify(mockScrollEvent, times(1)).getEventType();
        verify(mockScrollEvent, times(0)).getDeltaY();
        verify(parent, times(0)).getCoordinator();
    }

    /**
     * Test of performZoom method, of class TimelineChart.
     */
    @Test
    public void testPerformZoomPositiveDelta() {
        System.out.println("performZoom with positive delta");

        final ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        final double scrollDelta = 1D;
        when(mockScrollEvent.getEventType()).thenReturn(ScrollEvent.SCROLL);
        when(mockScrollEvent.getDeltaY()).thenReturn(scrollDelta);

        final TimelinePanel parent = mock(TimelinePanel.class);
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        when(parent.getCoordinator()).thenReturn(coordinator);

        final Axis<Number> xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        final double mouseX = 0.0;
        final double lower = 0D;
        final double upper = 10D;

        final TimelineChart instance = new TimelineChart(parent, xAxis, yAxis);
        instance.setExtents(lower, upper);
        instance.performZoom(mockScrollEvent, mouseX);

        verify(mockScrollEvent, times(1)).getEventType();
        verify(mockScrollEvent, times(1)).getDeltaY();
        verify(parent, times(1)).getCoordinator();
    }

    @Test
    public void testPerformZoomNegativeDelta() {
        System.out.println("performZoom with negative delta");

        final ScrollEvent mockScrollEvent = mock(ScrollEvent.class);
        final double scrollDelta = -1D;
        when(mockScrollEvent.getEventType()).thenReturn(ScrollEvent.SCROLL);
        when(mockScrollEvent.getDeltaY()).thenReturn(scrollDelta);

        final TimelinePanel parent = mock(TimelinePanel.class);
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        when(parent.getCoordinator()).thenReturn(coordinator);

        final Axis<Number> xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        final double mouseX = 0.0;
        final double lower = 0D;
        final double upper = 10D;

        final TimelineChart instance = new TimelineChart(parent, xAxis, yAxis);
        instance.setExtents(lower, upper);
        instance.performZoom(mockScrollEvent, mouseX);

        verify(mockScrollEvent, times(1)).getEventType();
        verify(mockScrollEvent, times(1)).getDeltaY();
        verify(parent, times(1)).getCoordinator();
    }

}

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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.time.ZoneId;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class TimelinePanelNGTest {

    private static final Logger LOGGER = Logger.getLogger(TimelinePanelNGTest.class.getName());

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
     * Test of getCoordinator method, of class TimelinePanel.
     */
    @Test
    public void testGetCoordinator() {
        System.out.println("getCoordinator");
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instance = new TimelinePanel(coordinator);

        assertEquals(coordinator, instance.getCoordinator());
    }

    /**
     * Test of setExclusionState method, of class TimelinePanel.
     */
    @Test
    public void testSetExclusionState() {
        System.out.println("setExclusionState");

        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instance = new TimelinePanel(coordinator);

        instance.setExclusionState(2);
        instance.setExclusionState(1);
        instance.setExclusionState(0);

        verify(coordinator, times(1)).setExclusionState(2);
        verify(coordinator, times(1)).setExclusionState(1);
        verify(coordinator, times(1)).setExclusionState(0);
    }

    /**
     * Test of updateTimeline method, of class TimelinePanel.
     */
    @Test
    public void testUpdateTimeline() {
        System.out.println("updateTimeline");
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instanceSpy = spy(new TimelinePanel(coordinator));

        final GraphReadMethods mockGraph = mock(GraphReadMethods.class);
        final ZoneId mockZoneId = ZoneId.systemDefault();

        assertEquals(coordinator, instanceSpy.getCoordinator());

        assertNull(instanceSpy.getUpdateTimelineThread());

        instanceSpy.updateTimeline(mockGraph, false, mockZoneId);

        assertNotNull(instanceSpy.getUpdateTimelineThread());

        instanceSpy.clearTimelineData();
    }
}

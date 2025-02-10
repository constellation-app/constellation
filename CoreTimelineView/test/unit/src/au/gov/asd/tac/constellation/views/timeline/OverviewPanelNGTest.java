/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class OverviewPanelNGTest {

    private static final Logger LOGGER = Logger.getLogger(OverviewPanelNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of clearHistogram method, of class OverviewPanel.
     */
    @Test
    public void testClearHistogram() {
        System.out.println("clearHistogram");

        // Mocked variables
        final TimelineTopComponent mockTopComponent = mock(TimelineTopComponent.class);

        final OverviewPanel instance = new OverviewPanel(mockTopComponent);

        // Assert not null before clearing
        assertNotNull(instance.getHistogramData());
        instance.clearHistogram();

        // Should be null now
        assertNull(instance.getHistogramData());
    }

    /**
     * Test of clearHistogram method, of class OverviewPanel.
     */
    @Test
    public void testClearHistogramIsPartialClear() {
        System.out.println("clearHistogram with args");
        final boolean isPartialClear = false;

        // Mocked variables
        final TimelineTopComponent mockTopComponent = mock(TimelineTopComponent.class);

        final OverviewPanel instance = new OverviewPanel(mockTopComponent);

        // Assert not null before clearing
        assertNotNull(instance.getHistogramData());
        instance.clearHistogram(isPartialClear);

        // Should be null now
        assertNull(instance.getHistogramData());
    }

}

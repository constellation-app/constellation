/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.pluginreporter.panes;

import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for PluginReporterPane
 * 
 * @author Delphinus8821
 */
public class PluginReportPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(PluginReportPaneNGTest.class.getName());

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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of toString method, of class PluginReportPane.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);
        
        when(pluginReport.getPluginName()).thenReturn("plugin name");
        String expResult = "plugin name";
        String result = instance.toString();
        assertEquals(result, expResult);
    }

    /**
     * Test of getPluginReport method, of class PluginReportPane.
     */
    @Test
    public void testGetPluginReport() {
        System.out.println("getPluginReport");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);

        PluginReport result = instance.getPluginReport();
        assertEquals(result, pluginReport);
    }

    /**
     * Test of updateTime method, of class PluginReportPane.
     */
    @Test
    public void testUpdateTimeHour() {
        System.out.println("updateTime");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);

        when(pluginReport.getStartTime()).thenReturn((long)5000000);
        when(pluginReport.getStopTime()).thenReturn((long)10000000);

        instance.updateTime();
        String time = instance.getTimeLabel().getText();

        assertEquals(time, "1h23m");
    }

    /**
     * Test of updateTime method, of class PluginReportPane.
     */
    @Test
    public void testUpdateTimeMinute() {
        System.out.println("updateTime");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);

        when(pluginReport.getStartTime()).thenReturn((long)500000);
        when(pluginReport.getStopTime()).thenReturn((long)1000000);

        instance.updateTime();
        String time = instance.getTimeLabel().getText();

        assertEquals(time, "8m20s");
    }

    /**
     * Test of updateTime method, of class PluginReportPane.
     */
    @Test
    public void testUpdateTimeSecond() {
        System.out.println("updateTime");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);

        when(pluginReport.getStartTime()).thenReturn((long)50000);
        when(pluginReport.getStopTime()).thenReturn((long)100000);

        instance.updateTime();
        String time = instance.getTimeLabel().getText();

        assertEquals(time, "50s");
    }

    /**
     * Test of updateTime method, of class PluginReportPane.
     */
    @Test
    public void testUpdateTimeMilli() {
        System.out.println("updateTime");
        final PluginReporterPane pluginReporterPane = mock(PluginReporterPane.class);
        final PluginReport pluginReport = mock(PluginReport.class);
        final PluginReportPane instance = new PluginReportPane(pluginReporterPane, pluginReport, null, null);
        
        when(pluginReport.getStartTime()).thenReturn((long)500);
        when(pluginReport.getStopTime()).thenReturn((long)1000);

        instance.updateTime();
        String time = instance.getTimeLabel().getText();

        assertEquals(time, "0.500s");

        // under 100 milli seconds
        when(pluginReport.getStartTime()).thenReturn((long)50);
        when(pluginReport.getStopTime()).thenReturn((long)100);

        instance.updateTime();
        time = instance.getTimeLabel().getText();

        assertEquals(time, "0.050s");

        // under 10 milliseconds 
        when(pluginReport.getStartTime()).thenReturn((long)5);
        when(pluginReport.getStopTime()).thenReturn((long)10);

        instance.updateTime();
        time = instance.getTimeLabel().getText();

        assertEquals(time, "0.005s");
    }
}

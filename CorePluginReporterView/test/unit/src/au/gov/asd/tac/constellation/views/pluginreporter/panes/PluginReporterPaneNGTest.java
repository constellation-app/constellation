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
package au.gov.asd.tac.constellation.views.pluginreporter.panes;

import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportFilter;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.collections.ListChangeListener;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.util.NbPreferences;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for PluginReporterPane
 *
 * @author Delphinus8821
 */
public class PluginReporterPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(PluginReporterPaneNGTest.class.getName());

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
     * Test of onChanged method, of class PluginReporterPane.
     */
    @Test
    public void testOnChanged() {
        System.out.println("onChanged");
        final String key = "filteredTags";
        final Preferences prefs = mock(Preferences.class);
        final String returnValue = "LOW LEVEL";

        final ListChangeListener.Change<? extends String> c = null;
        final PluginReporterPane instance = new PluginReporterPane();

        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(PluginReporterPane.class))).thenReturn(prefs);

            instance.onChanged(c);

            verify(prefs, times(1)).put(Mockito.eq(key), Mockito.eq(returnValue));
        }
    }

    /**
     * Test of setPluginReportFilter method, of class PluginReporterPane.
     */
    @Test
    public void testSetPluginReportFilter() {
        System.out.println("setPluginReportFilter");
        final PluginReportFilter pluginReportFilter = (PluginReport report) -> true;
        final PluginReporterPane instance = new PluginReporterPane();
        
        instance.setPluginReportFilter(pluginReportFilter);
        assertEquals(instance.getPluginReportFilter(), pluginReportFilter);
    }

    /**
     * Test of setGraphReport method, of class PluginReporterPane.
     */
    @Test
    public void testSetGraphReport() {
        System.out.println("setGraphReport");
        final String graphId = "graphId";
        final GraphReport graphReport = new GraphReport(graphId);
        final PluginReporterPane instance = new PluginReporterPane();
        instance.setGraphReport(graphReport);

        assertEquals(instance.getGraphReport(), graphReport);
    }
}

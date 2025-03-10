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
package au.gov.asd.tac.constellation.views.analyticview.export;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticExportUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TableView;
import org.apache.commons.io.IOUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for AnalyticExportToCsvFilePlugin
 *
 * @author Delphinus8821
 */
public class AnalyticExportToCsvFilePluginNGTest {

    private static final Logger LOGGER = Logger.getLogger(AnalyticExportToCsvFilePluginNGTest.class.getName());
    
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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of execute method, of class AnalyticExportToCsvFilePlugin.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExecute() throws IOException, InterruptedException, PluginException {
        final TableView<ScoreResult.ElementScore> table = mock(TableView.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);

        File tmpFile = null;
        try ( MockedStatic<AnalyticExportUtilities> tableViewUtilsMockedStatic = Mockito.mockStatic(AnalyticExportUtilities.class)) {
            tmpFile = File.createTempFile("constellationTest", ".csv");
            final String csv = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

            tableViewUtilsMockedStatic.when(() -> AnalyticExportUtilities.getTableData(table, true)).thenReturn(csv);

            final AnalyticExportToCsvFilePlugin plugin = new AnalyticExportToCsvFilePlugin(tmpFile, table);
            plugin.execute(null, pluginInteraction, null);

            final String outputtedFile = new String(IOUtils.toByteArray(new FileInputStream(tmpFile)), StandardCharsets.UTF_8);

            assertEquals(csv, outputtedFile);
            assertEquals(plugin.getName(), "Analytic View: Export to CSV");

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
}

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
package au.gov.asd.tac.constellation.views.tableview.plugins;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import org.apache.commons.io.IOUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ExportToCsvFilePluginNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(ExportToCsvFilePluginNGTest.class.getName());

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

    @Test
    public void exportCSV() throws IOException, InterruptedException, PluginException {
        final TableView<ObservableList<String>> table = mock(TableView.class);
        final Pagination pagination = mock(Pagination.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);

        File tmpFile = null;
        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            tmpFile = File.createTempFile("constellationTest", ".csv");
            final String csv = "COLUMN_1,COLUMN_2\nrow1Column1,row1Column2\nrow2Column1,row2Column2\n";

            tableViewUtilsMockedStatic.when(() -> TableViewUtilities.getTableData(table, pagination, true, true)).thenReturn(csv);

            final ExportToCsvFilePlugin plugin = new ExportToCsvFilePlugin(tmpFile, table, pagination, true);
            plugin.execute(null, pluginInteraction, null);

            final String outputtedFile = new String(IOUtils.toByteArray(new FileInputStream(tmpFile)), StandardCharsets.UTF_8);

            assertEquals(csv, outputtedFile);
            assertEquals(plugin.getName(), "Table View: Export to Delimited File");
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
}

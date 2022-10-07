/*
 * Copyright 2010-2022 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for AnalyticExportToExcelFilePlugin
 *
 * @author Delphinus8821
 */
public class AnalyticExportToExcelFilePluginNGTest {

    private static final Logger LOGGER = Logger.getLogger(AnalyticExportToExcelFilePluginNGTest.class.getName());

    public AnalyticExportToExcelFilePluginNGTest() {
    }

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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of execute method, of class AnalyticExportToExcelFilePlugin.
     * @throws java.lang.Exception
     */
    @Test
    public void testExecute() throws Exception {
        final String sheetName = "My Sheet";

        final TableView<ScoreResult.ElementScore> table = mock(TableView.class);
        final PluginInteraction pluginInteraction = mock(PluginInteraction.class);
        final Callback<Integer, Node> callback = mock(Callback.class);

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("constellationTest", ".xls");

            when(callback.call(anyInt())).thenReturn(table);

            final TableColumn<ScoreResult.ElementScore, ? extends Object> column1 = mock(TableColumn.class);
            final TableColumn<ScoreResult.ElementScore, ? extends Object> column2  = mock(TableColumn.class);

            when(column1.getText()).thenReturn("COLUMN_1");
            when(column2.getText()).thenReturn("COLUMN_2");

            when(column1.isVisible()).thenReturn(true);
            when(column2.isVisible()).thenReturn(true);

            final ObservableList<TableColumn<ScoreResult.ElementScore, ? extends Object>> columns = FXCollections.observableArrayList();
            columns.addAll(column1, column2);

            when(table.getColumns()).thenReturn(columns);

            doReturn(FXCollections.observableList(List.of(FXCollections.observableList(List.of("row1Column1", "row1Column2", "row1InvisibleColumn3")))))
                    .when(table).getItems();

            final AnalyticExportToExcelFilePlugin exportToExcel = new AnalyticExportToExcelFilePlugin(tmpFile, table, sheetName);

            exportToExcel.execute(null, pluginInteraction, null);

            // Due to date/times etc. no two files are the same at the byte level
            // So open the saved file, iterating over it, generating a CSV that can
            // be verified.
            final String csvInFile = generateCsvFromExcelFile(tmpFile, sheetName);
            final String expected = "COLUMN_1,COLUMN_2\n";

            assertEquals(expected, csvInFile);
            assertEquals("Analytic View: Export to Excel", exportToExcel.getName());

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }

    }

    /**
     * Open the given Excel file and read it, generating a CSV from its contents.
     *
     * @param file the Excel file to read
     * @param sheetName the sheet name in the Excel file to read
     * @return the contents of the Excel file in CSV format
     * @throws IOException if there is a problem opening the Excel file
     */
    private String generateCsvFromExcelFile(final File file, final String sheetName) throws IOException {
        final Workbook wb = WorkbookFactory.create(new FileInputStream(file));
        final Sheet sheet = wb.getSheet(sheetName);

        final StringBuilder output = new StringBuilder();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            final Row row = sheet.getRow(i);
            output.append(
                    IntStream.range(0, row.getLastCellNum())
                            .mapToObj(cellId -> row.getCell(cellId).toString())
                            .collect(Collectors.joining(","))
            );
            output.append("\n");
        }

        return output.toString();
    }
}

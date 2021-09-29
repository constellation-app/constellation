/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview2.components;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ExportFileChooserNGTest {

    private ExportFileChooser exportFileChooser;

    public ExportFileChooserNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        exportFileChooser = spy(new ExportFileChooser("My Export", ".csv", "A description"));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void init() throws IOException {
        final JFileChooser chooser = exportFileChooser.getFileChooser().createFileChooser();

        assertEquals("My Export", chooser.getDialogTitle());
        assertEquals("A description", chooser.getFileFilter().getDescription());

        final File lowercaseCsvFile = File.createTempFile("export", ".csv");
        final File uppdercaseCsvFile = File.createTempFile("export", ".CSV");
        final File nonCsvFile = File.createTempFile("export", ".xslx");

        assertFalse(chooser.getFileFilter().accept(new File("/tmp")));
        assertFalse(chooser.getFileFilter().accept(nonCsvFile));
        assertTrue(chooser.getFileFilter().accept(lowercaseCsvFile));
        assertTrue(chooser.getFileFilter().accept(uppdercaseCsvFile));

        nonCsvFile.delete();
        lowercaseCsvFile.delete();
        uppdercaseCsvFile.delete();
    }

    @Test
    public void openExportFileChooser() {
        verifyFileSelectionChecks("/tmp/other/export/TEST.CSV", "/tmp/other/export/TEST.CSV");
        verifyFileSelectionChecks("/tmp/other/export/TEST.cSv", "/tmp/other/export/TEST.cSv");
        verifyFileSelectionChecks("/tmp/other/export/TEST", "/tmp/other/export/TEST.csv");
        verifyFileSelectionChecks(null, null);
    }

    /**
     * Verifies the expected corrections (if any) to the provided file path.
     *
     * @param selectedAbsolutePath the original file path that is checked
     * @param expectedAbsolutePath the expected file path with corrections, or
     * the same as the original if no corrections are expected
     */
    private void verifyFileSelectionChecks(final String selectedAbsolutePath,
            final String expectedAbsolutePath) {
        final FileChooserBuilder fileChooser = mock(FileChooserBuilder.class);

        doReturn(fileChooser).when(exportFileChooser).getFileChooser();

        final File file = mock(File.class);

        when(fileChooser.showSaveDialog()).thenReturn(file);

        when(file.getAbsolutePath()).thenReturn(selectedAbsolutePath);

        assertEquals(expectedAbsolutePath,
                exportFileChooser.openExportFileChooser().getAbsolutePath());

    }
}

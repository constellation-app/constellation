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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.plugins.importexport.ImportPane;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.openide.NotifyDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class DelimitedImportControllerNGTest {
    private static MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic;
    private DelimitedImportController delimitedImportController;

    private ImportPane importPaneMocked;
    private DelimitedSourcePane delimitedSourcePaneMocked;

    private final List<File> filesToValidate = new ArrayList<>();



    public DelimitedImportControllerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        notifyDisplayerMockedStatic = Mockito.mockStatic(NotifyDisplayer.class);

        notifyDisplayerMockedStatic.when(() -> NotifyDisplayer.displayAndWait(any(NotifyDescriptor.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        notifyDisplayerMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        delimitedImportController = new DelimitedImportController();

        importPaneMocked = mock(ImportPane.class);
        delimitedSourcePaneMocked = mock(DelimitedSourcePane.class);

        doNothing().when(delimitedSourcePaneMocked).removeFile(any(File.class));
        doReturn(delimitedSourcePaneMocked).when(importPaneMocked).getSourcePane();
        delimitedImportController.setImportPane(importPaneMocked);

        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());
        filesToValidate.add(file);
        final File fileValid = new File(this.getClass().getResource("./resources/testCSV-SameStructure.csv").getFile());
        filesToValidate.add(fileValid);
        final File fileDifferentColumns = new File(this.getClass().getResource("./resources/testCSV-differentColumns-sameColumnCount.csv").getFile());
        filesToValidate.add(fileDifferentColumns);
        final File fileDifferentStructure = new File(this.getClass().getResource("./resources/testCSV-differentStructure.csv").getFile());
        filesToValidate.add(fileDifferentStructure);

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        filesToValidate.clear();
    }

    /**
     * Test of getColumnHeaders method, of class DelimitedImportController.
     */
    @Test
    public void testGetColumnHeaders() {
        System.out.println("getColumnHeaders");
        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());
        // final DelimitedImportController instance = new DelimitedImportController();
        final List<String> expResult = Arrays.asList("Source$name", "Source@Type", "ab cd", "Transaction.Time", "name");
        final List result = delimitedImportController.getColumnHeaders(file);
        assertEquals(result, expResult);
    }

    /**
     * Test of validateFileStructure method, of class DelimitedImportController
     * with no files.
     */
    @Test
    public void testValidateFileStructure_emptyFiles() {
        System.out.println("validateFileStructure");
        final List<File> filesToValidate = new ArrayList<>();
        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());

        delimitedImportController.setFiles(filesToValidate, file);
        assertTrue(delimitedImportController.getFiles().isEmpty());
        List<File> invalidFiles = delimitedImportController.validateFileStructure(filesToValidate);
        assertTrue(invalidFiles.isEmpty());
        assertTrue(delimitedImportController.getFiles().isEmpty());
    }

    /**
     * Test of validateFileStructure method, of class DelimitedImportController
     * with a single file.
     */
    @Test
    public void testValidateFileStructure_singleFile() {
        System.out.println("validateFileStructure");
        final List<File> filesToValidate = new ArrayList<>();
        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());
        filesToValidate.add(file);

        delimitedImportController.setFiles(filesToValidate, file);
        assertEquals(delimitedImportController.getFiles().size(), 1);
        List<File> invalidFiles = delimitedImportController.validateFileStructure(filesToValidate);
        assertTrue(invalidFiles.isEmpty());
        assertEquals(delimitedImportController.getFiles().size(), 1);
    }

    /**
     * Test of validateFileStructure method, of class DelimitedImportController,
     * with filesIncludeHeaders enabled.
     */
    @Test
    public void testValidateFileStructure_filesIncludeHeaders_enabled() {
        System.out.println("validateFileStructure");
        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());

        delimitedImportController.setFiles(filesToValidate, file);
        assertEquals(delimitedImportController.getFiles().size(), 4);
        List<File> invalidFiles = delimitedImportController.validateFileStructure(filesToValidate);

        assertEquals(invalidFiles.size(), 2);
        assertEquals(delimitedImportController.getFiles().size(), 2);
    }

    /**
     * Test of validateFileStructure method, of class DelimitedImportController
     * with filesIncludeHeaders disabled.
     */
    @Test
    public void testValidateFileStructure_filesIncludeHeaders_disabled() {
        System.out.println("validateFileStructure");
        final File file = new File(this.getClass().getResource("./resources/testCSV.csv").getFile());

        delimitedImportController.setfilesIncludeHeaders(false);
        delimitedImportController.setFiles(filesToValidate, file);
        assertEquals(delimitedImportController.getFiles().size(), 4);
        List<File> invalidFiles = delimitedImportController.validateFileStructure(filesToValidate);

        assertEquals(invalidFiles.size(), 1);
        assertEquals(delimitedImportController.getFiles().size(), 3);
    }

}

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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for DelimitedSourcePane.
 *
 * @author sol695510
 */
public class DelimitedSourcePaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(DelimitedSourcePaneNGTest.class.getName());

    public DelimitedSourcePaneNGTest() {
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
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of removeFile method, of class DelimitedSourcePane.
     */
    @Test
    public void testRemoveFile() {
        System.out.println("testRemoveFile");
    }

    /**
     * Test of getDelimitedImportFileChooser method, of class
     * DelimitedSourcePane.
     *
     * @throws IOException
     */
    @Test
    public void testGetDelimitedImportFileChooser() throws IOException {
        System.out.println("testGetDelimitedImportFileChooser");

        final String fileChooserTitle = "Import";

        final DelimitedSourcePane instance = new DelimitedSourcePane(mock(DelimitedImportController.class));
        final JFileChooser fileChooser = instance.getDelimitedImportFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 0);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.accept(fileMock), true);
    }

    /**
     * Test of update method, of class DelimitedSourcePane.
     */
    @Test
    public void testUpdate() {
        System.out.println("testUpdate");
    }
}

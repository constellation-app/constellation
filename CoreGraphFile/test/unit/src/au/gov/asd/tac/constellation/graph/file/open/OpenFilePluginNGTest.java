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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.JFileChooser;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for OpenFilePlugin.
 *
 * @author sol695510
 */
public class OpenFilePluginNGTest {

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<OpenFile> openFileStaticMock;

    public OpenFilePluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileChooserStaticMock = mockStatic(FileChooser.class);
        openFileStaticMock = mockStatic(OpenFile.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
        openFileStaticMock.close();
    }

    /**
     * Test of read method, of class OpenFilePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("testRead");

        final OpenFilePlugin instance = new OpenFilePlugin();
        final File file = new File("test.star");
        final List<File> files = new ArrayList<>();

        final SecureRandom random = new SecureRandom();
        final int numberOfFiles = random.nextInt(5) + 1;

        for (int i = numberOfFiles; i > 0; i--) {
            files.add(file);
        }

        final Optional<List<File>> optionalFiles = Optional.ofNullable(files);

        fileChooserStaticMock.when(()
                -> FileChooser.openMultiDialog(any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFiles));

        openFileStaticMock.when(() -> OpenFile.openFile(file, -1)).thenCallRealMethod();

        instance.read(null, null, null);

        openFileStaticMock.verify(()
                -> OpenFile.openFile(file, -1), times(numberOfFiles));
    }

    /**
     * Test of getOpenFileChooser method, of class OpenFilePlugin.
     *
     * @throws IOException
     */
    @Test
    public void testGetOpenFilePluginChooser() throws IOException {
        System.out.println("testGetOpenFilePluginChooser");

        final String fileChooserTitle = "Open";
        final String fileChooserDescription = "Constellation Files ("
                + FileExtensionConstants.STAR + ", "
                + FileExtensionConstants.NEBULA + ")";

        final OpenFilePlugin instance = new OpenFilePlugin();
        final JFileChooser fileChooser = instance.getOpenFileChooser().createFileChooser();

        // Ensure file chooser is constructed correctly.
        assertEquals(fileChooser.getDialogTitle(), fileChooserTitle);
        assertEquals(fileChooser.getChoosableFileFilters().length, 1);
        assertEquals(fileChooser.getChoosableFileFilters()[0].getDescription(), fileChooserDescription);

        // If file is invalid and does not end with correct extension.
        final File file1 = File.createTempFile("fileInvalid", ".invalid");
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file1), false);

        // If files do not exist.
        final File file2 = new File("/invalidPath/fileStar" + FileExtensionConstants.STAR);
        final File file3 = new File("/invalidPath/fileNebula" + FileExtensionConstants.NEBULA);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file2), false);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file3), false);

        // If files exist, are valid and end with correct extensions.
        final File file4 = File.createTempFile("fileStar", FileExtensionConstants.STAR);
        final File file5 = File.createTempFile("fileNebula", FileExtensionConstants.NEBULA);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file4), true);
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(file5), true);

        // If file is a directory.
        final File fileMock = mock(File.class);
        doReturn("directory").when(fileMock).getName();
        doReturn(false).when(fileMock).isFile();
        doReturn(true).when(fileMock).isDirectory();
        assertEquals(fileChooser.getChoosableFileFilters()[0].accept(fileMock), true);

        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file4.toPath());
        Files.deleteIfExists(file5.toPath());
    }
}

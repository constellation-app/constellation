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

import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
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

    private static final Logger LOGGER = Logger.getLogger(OpenFilePluginNGTest.class.getName());

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<OpenFile> openFileStaticMock;

    public OpenFilePluginNGTest() {
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
        fileChooserStaticMock = Mockito.mockStatic(FileChooser.class);
        openFileStaticMock = Mockito.mockStatic(OpenFile.class);
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
                -> FileChooser.openMultiDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFiles));

        openFileStaticMock.when(() -> OpenFile.openFile(file, -1)).thenCallRealMethod();

        instance.read(null, null, null);

        openFileStaticMock.verify(()
                -> OpenFile.openFile(file, -1), times(numberOfFiles));
    }
}

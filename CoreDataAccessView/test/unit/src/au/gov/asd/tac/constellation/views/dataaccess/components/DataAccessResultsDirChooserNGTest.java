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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessResultsDirChooserNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessResultsDirChooserNGTest.class.getName());

    private static MockedStatic<FileChooser> fileChooserStaticMock;
    private static MockedStatic<DataAccessPreferenceUtilities> DataAccessPreferenceUtilitiesMock;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileChooserStaticMock = Mockito.mockStatic(FileChooser.class);
        DataAccessPreferenceUtilitiesMock = Mockito.mockStatic(DataAccessPreferenceUtilities.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
        DataAccessPreferenceUtilitiesMock.close();
    }

    /**
     * Test of openAndSaveToPreferences method, of class
     * DataAccessResultsDirChooser.
     */
    @Test
    public void testOpenAndSaveToPreferences() {
        System.out.println("testOpenAndSaveToPreferences");

        final DataAccessResultsDirChooser instance = new DataAccessResultsDirChooser();

        final String title = "Folder to save data access results to";
        final File savedDirectory = FileChooser.DEFAULT_DIRECTORY;
        final FileNameExtensionFilter filter = null;

        final File file = new File("testFolder");

        final Optional<File> optionalFile = Optional.ofNullable(file);

        fileChooserStaticMock.when(()
                -> FileChooser.getBaseFileChooserBuilder(
                        title,
                        savedDirectory,
                        filter))
                .thenCallRealMethod();

        fileChooserStaticMock.when(()
                -> FileChooser.openOpenDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        DataAccessPreferenceUtilitiesMock.when(()
                -> DataAccessPreferenceUtilities.setDataAccessResultsDir(file.getAbsoluteFile()))
                .thenCallRealMethod();

        DataAccessPreferenceUtilitiesMock.when(()
                -> DataAccessPreferenceUtilities.getDataAccessResultsDir())
                .thenReturn(file);

        instance.openAndSaveToPreferences();

        DataAccessPreferenceUtilitiesMock.verify(()
                -> DataAccessPreferenceUtilities.setDataAccessResultsDir(file.getAbsoluteFile()), times(1));
    }

    @Test
    public void open_called_on_fx_ui_thread() {
        Platform.runLater(() -> {
            try {
                new DataAccessResultsDirChooser().openAndSaveToPreferences();
                fail("Should have thrown an illegal state exception");
            } catch (IllegalStateException ex) {
                // Do Nothing
            }
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void open_called_on_awt_ui_thread() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            try {
                new DataAccessResultsDirChooser().openAndSaveToPreferences();
                fail("Should have thrown an illegal state exception");
            } catch (IllegalStateException ex) {
                // Do Nothing
            }
        });
    }
}

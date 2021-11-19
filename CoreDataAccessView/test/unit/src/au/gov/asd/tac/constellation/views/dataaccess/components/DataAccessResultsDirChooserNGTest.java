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

import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessResultsDirChooserNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessResultsDirChooserNGTest.class.getName());

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

    @Test
    public void init() {
        final DataAccessResultsDirChooser chooser = spy(new DataAccessResultsDirChooser());
        assertNotNull(chooser.getDataAccesssResultsFileChooser());

        final FileChooserBuilder fileChooser = mock(FileChooserBuilder.class);
        doReturn(fileChooser).when(chooser).getDataAccesssResultsFileChooser();

        final File expectedSelectedDir = new File("/tmp");
        when(fileChooser.showSaveDialog()).thenReturn(expectedSelectedDir);

        try (final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic
                = Mockito.mockStatic(DataAccessPreferenceUtilities.class)) {
            final File selectedDir = chooser.openAndSaveToPreferences();

            assertEquals(selectedDir, expectedSelectedDir);
            prefUtilsMockedStatic.verify(() -> DataAccessPreferenceUtilities
                    .setDataAccessResultsDir(expectedSelectedDir));
        }
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

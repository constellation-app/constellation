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
package au.gov.asd.tac.constellation.utilities.gui.filechooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class FileChooserNGTest {

    private static final Logger LOGGER = Logger.getLogger(FileChooserNGTest.class.getName());

    private static MockedStatic<SwingUtilities> swingUtilsMockedStatic;
    private static MockedStatic<Platform> platformMockedStatic;
    private static MockedStatic<CompletableFuture> completableFutureMockedStatic;
    private ShowFileChooserDialog showFileChooserDialog = mock(ShowFileChooserDialog.class);

    private File file;

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }

        swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class);
        platformMockedStatic = Mockito.mockStatic(Platform.class);
        completableFutureMockedStatic = Mockito.mockStatic(CompletableFuture.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        swingUtilsMockedStatic.close();
        platformMockedStatic.close();
        completableFutureMockedStatic.close();

        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        completableFutureMockedStatic.when(() -> CompletableFuture.completedFuture(
                ArgumentMatchers.<Optional<File>>any())
        )
                .then(iom -> {
                    final Optional<File> suppliedFile = iom.getArgument(0);

                    final CompletableFuture<Optional<File>> completableFuture = mock(CompletableFuture.class);
                    doReturn(suppliedFile).when(completableFuture).get();

                    return completableFuture;
                });

        completableFutureMockedStatic.when(() -> CompletableFuture.completedFuture(any(Supplier.class)))
                .thenAnswer(iom -> {
                    final Supplier<Optional<File>> supplier = iom.getArgument(0);

                    final CompletableFuture<Optional<File>> completableFuture = mock(CompletableFuture.class);
                    doReturn(supplier.get()).when(completableFuture).get();

                    return completableFuture;
                });

        file = mock(File.class);

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        swingUtilsMockedStatic.reset();
        platformMockedStatic.reset();
        completableFutureMockedStatic.reset();
    }

    @Test
    public void openFileDialog() throws ExecutionException, InterruptedException {
        final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);

        CompletableFuture<Optional<File>> completableOptional = mock(CompletableFuture.class);
        doReturn(List.of(file)).when(completableOptional).get();

        CompletableFuture<Optional<List<File>>> optionalFiles = mock(CompletableFuture.class);
        doReturn(file).when(optionalFiles).get();

        when(FileChooser.openSaveDialog(fileChooserBuilder)).thenReturn(completableOptional);
        when(FileChooser.openOpenDialog(fileChooserBuilder)).thenReturn(completableOptional);
        when(FileChooser.openMultiDialog(fileChooserBuilder)).thenReturn(optionalFiles);

        reset(fileChooserBuilder);
        openSingleFileDialog(
                true,
                false,
                fileChooserBuilder,
                FileChooserMode.SAVE,
                () -> FileChooser.openSaveDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openSingleFileDialog(
                false,
                true,
                fileChooserBuilder,
                FileChooserMode.SAVE,
                () -> FileChooser.openSaveDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openSingleFileDialog(
                false,
                false,
                fileChooserBuilder,
                FileChooserMode.SAVE,
                () -> FileChooser.openSaveDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openSingleFileDialog(
                true,
                false,
                fileChooserBuilder,
                FileChooserMode.OPEN,
                () -> FileChooser.openOpenDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openSingleFileDialog(
                false,
                true,
                fileChooserBuilder,
                FileChooserMode.OPEN,
                () -> FileChooser.openOpenDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openSingleFileDialog(
                false,
                false,
                fileChooserBuilder,
                FileChooserMode.OPEN,
                () -> FileChooser.openOpenDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openMultiFileDialog(
                true,
                false,
                fileChooserBuilder,
                FileChooserMode.MULTI,
                () -> FileChooser.openMultiDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openMultiFileDialog(
                false,
                true,
                fileChooserBuilder,
                FileChooserMode.MULTI,
                () -> FileChooser.openMultiDialog(fileChooserBuilder)
        );

        reset(fileChooserBuilder);
        openMultiFileDialog(
                false,
                false,
                fileChooserBuilder,
                FileChooserMode.MULTI,
                () -> FileChooser.openMultiDialog(fileChooserBuilder)
        );
    }

    /**
     * Tests the open file dialog method for selecting a single file. Verifies
     * that the correct methods are called on the correct threads.
     *
     * @param isFxThread true if this is meant to be running on the FX thread,
     * false otherwise
     * @param isSwingThread true if this is meant to be running on the Swing
     * thread, false otherwise
     * @param fileChooserBuilder the file chooser builder to be passed in
     * @param fileDialogMode the file dialog mode that the file chooser will be
     * opened with
     * @param runner a supplier that calls the file chooser method to be tested,
     * returning the result
     * @throws InterruptedException if the thread is interrupted whilst getting
     * the file chooser result
     * @throws ExecutionException if there is an issue getting the file chooser
     * result
     */
    public void openSingleFileDialog(final boolean isFxThread,
            final boolean isSwingThread,
            final FileChooserBuilder fileChooserBuilder,
            final FileChooserMode fileDialogMode,
            final Supplier<CompletableFuture<Optional<File>>> runner) throws InterruptedException, ExecutionException {
        try (
                final MockedConstruction<ShowFileChooserDialog> showFileMockedConstruction
                = Mockito.mockConstruction(
                        ShowFileChooserDialog.class,
                        (mock, cnxt) -> {
                            assertEquals(cnxt.arguments(), List.of(fileChooserBuilder, fileDialogMode));
                        });) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(isSwingThread);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(isFxThread);

            assertSame(runner.get().get(), file);
            assertEquals(showFileMockedConstruction.constructed().size(), 1);

        }
    }

    /**
     * Tests the open file dialog method for selecting multiple files. Verifies
     * that the correct methods are called on the correct threads.
     *
     * @param isFxThread true if this is meant to be running on the FX thread,
     * false otherwise
     * @param isSwingThread true if this is meant to be running on the Swing
     * thread, false otherwise
     * @param fileChooserBuilder the file chooser builder to be passed in
     * @param fileDialogMode the file dialog mode that the file chooser will be
     * opened with
     * @param runner a supplier that calls the file chooser method to be tested,
     * returning the result
     * @throws InterruptedException if the thread is interrupted whilst getting
     * the file chooser result
     * @throws ExecutionException if there is an issue getting the file chooser
     * result
     */
    public void openMultiFileDialog(final boolean isFxThread,
            final boolean isSwingThread,
            final FileChooserBuilder fileChooserBuilder,
            final FileChooserMode fileDialogMode,
            final Supplier<CompletableFuture<Optional<List<File>>>> runner) throws InterruptedException, ExecutionException {
        try (
                final MockedConstruction<ShowFileChooserDialog> showFileMockedConstruction
                = Mockito.mockConstruction(
                        ShowFileChooserDialog.class,
                        (mock, cnxt) -> {
                            assertEquals(cnxt.arguments(), List.of(fileChooserBuilder, fileDialogMode));
                        });) {
            swingUtilsMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(isSwingThread);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(isFxThread);

            assertEquals(runner.get().get(), file);
            assertEquals(showFileMockedConstruction.constructed().size(), 1);
        }
    }
}

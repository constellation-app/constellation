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
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Wrapper for opening file choosers built with the {@link FileChooserBuilder}. There
 * are a couple of issues on macOS that require this to be done in a rather specific
 * way to ensure that the dialog is displayed and the UI does not hang.
 *
 * @author formalhaunt
 */
public class FileChooser {
    
    /**
     * Private constructor to prevent external initialization.
     */
    private FileChooser() {
    }
    
    /**
     * Construct a file chooser using the passed builder and opens it as a save
     * dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request
     * in a async future call that does a recursive call back to this method. This
     * ensures that the UI thread does not get into a race condition where the
     * UI thread becomes blocked.
     * 
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file or
     *     an empty optional if the user selects cancel
     */
    public static CompletableFuture<Optional<File>> openSaveDialog(final FileChooserBuilder fileChooserBuilder) {
        return CompletableFuture.completedFuture(
                openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.SAVE));
    }
    
    /**
     * Construct a file chooser using the passed builder and opens it as a open
     * dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request
     * in a async future call that does a recursive call back to this method. This
     * ensures that the UI thread does not get into a race condition where the
     * UI thread becomes blocked.
     * 
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file or
     *     an empty optional if the user selects cancel
     */
    public static CompletableFuture<Optional<File>> openOpenDialog(final FileChooserBuilder fileChooserBuilder) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Wrap the open call in a completable future so this happens on another
            // thread and return immediately
            return CompletableFuture.supplyAsync(() ->
                    openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.OPEN));
        } else {
            // This is not the UI thread so just call the open dialog method
            return CompletableFuture.completedFuture(
                    openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.OPEN)
            );
        }
    }
    
    /**
     * Construct a file chooser using the passed builder and opens it as a multi
     * dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request
     * in a async future call that does a recursive call back to this method. This
     * ensures that the UI thread does not get into a race condition where the
     * UI thread becomes blocked.
     * 
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file(s) or
     *     an empty optional if the user selects cancel
     */
    public static CompletableFuture<Optional<List<File>>> openMultiDialog(final FileChooserBuilder fileChooserBuilder) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Wrap the open call in a completable future so this happens on another
            // thread and return immediately
            return CompletableFuture.supplyAsync(() ->
                    openFileDialog(fileChooserBuilder, FileChooserMode.MULTI));
        } else {
            // This is not the UI thread so just call the open dialog method
            return CompletableFuture.completedFuture(
                    openFileDialog(fileChooserBuilder, FileChooserMode.MULTI)
            );
        }
    }
    
    /**
     * Construct a file chooser using the passed builder and opens it as a open
     * or save dialog depending on the passed mode. This method will block the
     * calling thread until the user selects a file or presses cancel.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @param fileDialogMode the type of file chooser dialog to open, save, open or multi
     * @return the selected file(s) or an empty optional if the user selects cancel
     */
    private static Optional<List<File>> openFileDialog(final FileChooserBuilder fileChooserBuilder,
                                                       final FileChooserMode fileDialogMode) {
        final ShowFileChooserDialog showDialog = new ShowFileChooserDialog(
                fileChooserBuilder, fileDialogMode);
        showDialog.run();

        return showDialog.getSelectedFiles();
    }
    
    /**
     * Opens a file dialog with the selected mode and returns the first selected file
     * or an empty optional if the user selected cancel.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @param fileDialogMode the type of file chooser dialog to open, save, open or multi
     * @return the first selected file or an empty optional if the user selects cancel
     * @see #openFileDialog(FileChooserBuilder, FileChooserMode) 
     */
    private static Optional<File> openFileDialogAndGetFirstFile(final FileChooserBuilder fileChooserBuilder,
                                                                final FileChooserMode fileDialogMode) {
        final Optional<List<File>> selectedFiles = openFileDialog(fileChooserBuilder, fileDialogMode);
        
        if (selectedFiles.isPresent() && !selectedFiles.get().isEmpty()) {
            return Optional.of(selectedFiles.get().get(0));
        }
        return Optional.empty();
    }
}

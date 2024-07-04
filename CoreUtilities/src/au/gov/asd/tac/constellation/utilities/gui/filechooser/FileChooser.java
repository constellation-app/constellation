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
package au.gov.asd.tac.constellation.utilities.gui.filechooser;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Wrapper for opening file choosers built with the {@link FileChooserBuilder}. There are a couple of issues on macOS
 * that require this to be done in a rather specific way to ensure that the dialog is displayed and the UI does not
 * hang.
 *
 * @author formalhaunt
 */
public class FileChooser {

    private static final Logger LOGGER = Logger.getLogger(FileChooser.class.getName());

    /**
     * Private constructor to prevent external initialization.
     */
    private FileChooser() {
    }

    /**
     * Immediately construct a file chooser using the passed builder and opens it as a save dialog.
     * <p/>
     * This is intended to be called when we already know we are in the EDT / UI Thread This ensures correct program
     * flow, with dialogs shown in the correct order, specifically for a special case where dialogs are cascaded into
     * more dialogs and need an immediate result from the new dialogs
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file or an empty optional if the user selects
     * cancel
     */
    public static CompletableFuture<Optional<File>> openImmediateSaveDialog(final FileChooserBuilder fileChooserBuilder) {
        return CompletableFuture.completedFuture(openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.SAVE));
    }

    /**
     * Construct a file chooser using the passed builder and opens it as a save dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request in a async future call that does a recursive
     * call back to this method. This ensures that the UI thread does not get into a race condition where the UI thread
     * becomes blocked.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file or an empty optional if the user selects
     * cancel
     */
    public static CompletableFuture<Optional<File>> openSaveDialog(final FileChooserBuilder fileChooserBuilder) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Wrap the open call in a completable future so this happens on another
            // thread and return immediately
            return CompletableFuture.supplyAsync(() -> openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.SAVE));
        } else {
            // This is not the UI thread so just call the open dialog method
            return CompletableFuture.completedFuture(openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.SAVE));
        }
    }

    /**
     * Construct a file chooser using the passed builder and opens it as a open dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request in a async future call that does a recursive
     * call back to this method. This ensures that the UI thread does not get into a race condition where the UI thread
     * becomes blocked.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file or an empty optional if the user selects
     * cancel
     */
    public static CompletableFuture<Optional<File>> openOpenDialog(final FileChooserBuilder fileChooserBuilder) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Wrap the open call in a completable future so this happens on another
            // thread and return immediately
            return CompletableFuture.supplyAsync(() -> openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.OPEN));
        } else {
            // This is not the UI thread so just call the open dialog method
            return CompletableFuture.completedFuture(openFileDialogAndGetFirstFile(fileChooserBuilder, FileChooserMode.OPEN));
        }
    }

    /**
     * Construct a file chooser using the passed builder and opens it as a multi dialog.
     * <p/>
     * If this was called from a UI thread then this will wrap the request in a async future call that does a recursive
     * call back to this method. This ensures that the UI thread does not get into a race condition where the UI thread
     * becomes blocked.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @return a {@link CompletableFuture} that will return the selected file(s) or an empty optional if the user
     * selects cancel
     */
    public static CompletableFuture<Optional<List<File>>> openMultiDialog(final FileChooserBuilder fileChooserBuilder) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Wrap the open call in a completable future so this happens on another
            // thread and return immediately
            return CompletableFuture.supplyAsync(() -> openFileDialog(fileChooserBuilder, FileChooserMode.MULTI));
        } else {
            // This is not the UI thread so just call the open dialog method
            return CompletableFuture.completedFuture(openFileDialog(fileChooserBuilder, FileChooserMode.MULTI));
        }
    }

    /**
     * Construct a file chooser using the passed builder and opens it as a open or save dialog depending on the passed
     * mode. This method will block the calling thread until the user selects a file or presses cancel.
     *
     * @param fileChooserBuilder the file chooser builder to build and display
     * @param fileDialogMode the type of file chooser dialog to open, save, open or multi
     * @return the selected file(s) or an empty optional if the user selects cancel
     */
    private static Optional<List<File>> openFileDialog(final FileChooserBuilder fileChooserBuilder,
            final FileChooserMode fileDialogMode) {
        final ShowFileChooserDialog showDialog = new ShowFileChooserDialog(fileChooserBuilder, fileDialogMode);

        // Check if the calling thread is able to run this
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            showDialog.run();
        } else {
            try {
                // Make a request to open the file chooser dialog on the UI thread
                EventQueue.invokeAndWait(showDialog);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.WARNING, "Thread displaying the file chooser was interrupted.", ex);
                Thread.currentThread().interrupt();
            } catch (final InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, "Error occurred during selection in file chooser.", ex);
            }
        }

        return showDialog.getSelectedFiles();
    }

    /**
     * Opens a file dialog with the selected mode and returns the first selected file or an empty optional if the user
     * selected cancel.
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

    /**
     * Function for displaying "Are you sure?" dialog when user attempts to save over existing file.
     *
     * @param selection the files selected by the user when trying to save
     * @param fileExtension the file extension associated with this FileChooserBuilder
     * @return user response, indicating if user wants to overwrite exisitng file
     */
    private static boolean approver(final File[] selection, final String fileExtension) {
        // Show dialog box if file already exists when saving
        final String extension = FilenameUtils.getExtension(selection[0].toString());
        final String filepath = extension.isEmpty() ? selection[0].toString() + fileExtension : selection[0].toString();
        final File file = new File(filepath);

        if (file.exists()) {
            final int response = JOptionPane.showConfirmDialog(null,
                    "The file " + file.getName() + " already exists. Do you want to replace the existing file?",
                    "Overwrite file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            // Overwrite if user chose yes
            return response == JOptionPane.YES_OPTION;
        }
        // Otherwise file didn't already exist
        return true;
    }

    /**
     * Creates a FileChooserBuilder with a file filter, File filter accepts if either: file is a normal file and either:
     * filename ends with given file extension, or file is a directory
     *
     * @param title the title of the FileChooserBuilder
     * @param fileExtension the file extension associated with this FileChooserBuilder.
     * @param filterDescription the description of the file filter
     * @param warnOverwrite true if user will be warned of overwriting a file
     * @return the constructed FileChooserBuilder
     */
    public static FileChooserBuilder createFileChooserBuilder(final String title, final String fileExtension, final String filterDescription, final boolean warnOverwrite) {
        final FileChooserBuilder builder = createFileChooserBuilder(title)
                .setAcceptAllFileFilterUsed(false)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, fileExtension)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return filterDescription;
                    }
                });

        if (warnOverwrite) {
            builder.setSelectionApprover((final File[] selection) -> approver(selection, fileExtension));
        }
        return builder;
    }

    /**
     * Creates a FileChooserBuilder with a file filter, File filter accepts if either: file is a normal file and either:
     * filename ends with given file extension, or file is a directory
     *
     * @param title the title of the FileChooserBuilder
     * @param fileExtension the file extension associated with this FileChooserBuilder.
     * @param filterDescription the description of the file filter
     * @return the constructed FileChooserBuilder
     */
    public static FileChooserBuilder createFileChooserBuilder(final String title, final String fileExtension, final String filterDescription) {
        return createFileChooserBuilder(title, fileExtension, filterDescription, false);
    }

    /**
     * Creates a FileChooserBuilder with a file filter, File filter accepts if either: file is a normal file and either:
     * filename ends with given file extension, or file is a directory
     *
     * @param title the title of the FileChooserBuilder
     * @param fileExtension the file extension associated with this FileChooserBuilder.
     * @param filterDescription the description of the file filter
     * @return the constructed FileChooserBuilder
     */
    public static FileChooserBuilder createFileChooserBuilder(final String title, final String fileExtension) {
        return createFileChooserBuilder(title, fileExtension, fileExtension, false);
    }

    /**
     * Creates a FileChooserBuilder with no file filter
     *
     * @param title the title of the FileChooserBuilder
     * @return the constructed FileChooserBuilder
     */
    public static FileChooserBuilder createFileChooserBuilder(final String title) {
        return new FileChooserBuilder(title)
                .setTitle(title)
                .setFilesOnly(true);
    }
}

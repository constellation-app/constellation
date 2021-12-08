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
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Provides a file chooser dialog that allows the user to select the directory
 * for data access results to be saved into.
 *
 * @author formalhaunt
 */
public class DataAccessResultsDirChooser {

    private static File savedDirectory = FileChooser.DEFAULT_DIRECTORY;

    private static final String TITLE = "Folder to save data access results to";

    /**
     * Opens the directory chooser and waits for the user to provide input.
     * Saves the selected directory to the data access preferences and returns
     * it.
     * <p/>
     * This assumes that it is not called in the UI event thread and runs the
     * dialog call in a separate thread.
     *
     * @return the selected directory or null if the user selects cancel
     * @throws IllegalStateException if the method is called using a UI event
     * thread
     */
    public File openAndSaveToPreferences() {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Attempted to open data access results dir on UI thread.");
        }

        FileChooser.openOpenDialog(getDataAccesssResultsFileChooser()).thenAccept(optionalFolder -> optionalFolder.ifPresent(selectedFolder -> {
            savedDirectory = FileChooser.REMEMBER_OPEN_AND_SAVE_LOCATION ? selectedFolder : FileChooser.DEFAULT_DIRECTORY;
            DataAccessPreferenceUtilities.setDataAccessResultsDir(selectedFolder.getAbsoluteFile());
        }));

        return DataAccessPreferenceUtilities.getDataAccessResultsDir();
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getDataAccesssResultsFileChooser() {
        return FileChooser.getBaseFileChooserBuilder(TITLE, savedDirectory, null)
                .setDirectoriesOnly(true);
    }
}

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
import org.openide.filesystems.FileChooserBuilder;

/**
 * Provides a file chooser dialog that allows the user to select the directory
 * for data access results to be saved into.
 *
 * @author formalhaunt
 */
public class DataAccessResultsDirChooser {
    private static final String TITLE = "Folder to save data access results to";
    private static final String FILE_CHOOSER_GROUP = "dataAccessResultsDir";
    
    private final FileChooserBuilder fileChooser;
  
    /**
     * Creates a new data access results directory chooser.
     */
    public DataAccessResultsDirChooser() {
        fileChooser = new FileChooserBuilder(FILE_CHOOSER_GROUP)
                .setTitle(TITLE)
                .setDirectoriesOnly(true);
    }
    
    /**
     * Opens the directory chooser and waits for the user to provide input. Saves
     * the selected dialog to the data access preferences and returns it.
     *
     * @return the selected directory or null if the user selects cancel
     */
    public File openAndSaveToPreferences() {
        final File selectedDir = getFileChooser().showSaveDialog();
        
        DataAccessPreferenceUtilities.setDataAccessResultsDir(selectedDir);
        
        return selectedDir;
    }
    
    /**
     * Gets the data access results directory chooser.
     *
     * @return the data access results directory file chooser
     */
    public FileChooserBuilder getFileChooser() {
        return fileChooser;
    }
}

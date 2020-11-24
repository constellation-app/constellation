/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.genericjsonio;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;

/**
 * Common functionality allowing JSON preferences to be saved/loaded.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class JsonIO {

    private static final Logger LOGGER = Logger.getLogger(JsonIO.class.getName());
    private static final String FILE_EXT = ".json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());
    private static String currentDir = "";  // Stores directory used by load dialog for reuse in delete call
    private static String currentPrefix = "";  // Stores prefix used by load dialog for reuse in delete call

    /**
     * Private constructor to hide implicit public one.
     */
    private JsonIO() {
        throw new IllegalStateException("Invalid call to private default constructor");
    }

    /**
     * Save the supplied JSON data in a file, within an allocated subdirectory
     * of the users configuration directory. The filename can optionally be
     * prefixed with a string, which can be used to categorize it. Refer to
     * further comments inline.
     *
     * @param saveDir Directory name within users directory to save the
     * configuration file to
     * @param mapper ObjectMapper tied to the JSON object being written to file
     * @param rootNode The root node of the JSON object being written
     * @param filePrefix Ignored if blank, if not, a string to prefix the
     * filename with.
     *
     */
    public static void saveJsonPreferences(String saveDir, ObjectMapper mapper, ArrayNode rootNode, String filePrefix) {
        // Normal operation sees an empty filePrefix string supplied. In this case, the name of the file will contain
        // only the filename entered by the user. If however a non-empty filePrefix is supplied, then the filename
        // privided by the user will be prefixed with this.
        // This is MOST useful when a dedicated directory exists for similar 'type' config files, like the example
        // directory structure below:
        // <user-dir>
        //     +-- tableconfigs
        //              +--- transaction_config1.json
        //              +--- transaction_config2.json
        //              +--- transaction_config3.json
        //              +--- vertex_configa.json
        //              +--- vertex_configb.json
        //              +--- vertex_configc.json
        //
        // The above structure could have been contructed with multiple calls to saveJsonPreferences all using
        // tableconfigs as the value for saveDir, three using 'transaction_' for filePrefix and the other three
        // using 'vertex_'.
        // This functionality is integrated with loadJsonPreferences such that if a filePrefix is supplied in the
        // loadJsonPreferences call then only files that contain the prefix are offered to the user to load. Hence,
        // it allows a form of config file filtering.
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final File prefDir = new File(ApplicationPreferenceKeys.getUserDir(prefs), saveDir);

        // Create containing directory if it doesn't exist, wnsure it was successful.
        if (!prefDir.exists()) {
            prefDir.mkdir();
        }
        if (!prefDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", prefDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

        // Configure the mapper
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);

        // Obtain a filename from the user. If no filename is supplied, and the user didn't hit cancel, then
        // generate a filename for them based on username and timestamp. If cancel was hit, no more processing
        // is required.
        Tuple<Boolean, String> preferenceNameDetails = JsonIODialog.getName();
        if (!preferenceNameDetails.getFirst()) {
            // Cancel was pressed, lets exit straight away - nothing to do here
            return;
        }
        String fileName = preferenceNameDetails.getSecond();
        if (fileName.isEmpty()) {
            // User didn't enter anyhting but hit OK ... this is a trigger to auto generate a filename
            fileName = String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(Instant.now()));
        }

        // At this point ensure the filename isn't all whitespace, if it is, the user may have entered multiple spaces
        // or similar which is a bad filename.
        if (fileName.trim().length() > 0) {
            // Append filePrefix - it may well be empty, in which case the final fileName is unchanged.
            fileName = filePrefix.concat(fileName);

            // Configure JSON mapper settings
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);

            // Create the file and write its contents. IF the file already exists, confirm from the user that they
            // wish to continue (and overwrite the existing file).
            final File f = new File(prefDir, FilenameEncoder.encode(fileName + FILE_EXT));
            boolean go = true;
            if (f.exists()) {
                final String msg = String.format("'%s' already exists. Do you want to overwrite it?", fileName);
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Preference file exists");
                alert.setContentText(msg);
                final Optional<ButtonType> option = alert.showAndWait();
                go = option.isPresent() && option.get() == ButtonType.OK;
            }

            if (go) {
                try {
                    mapper.writeValue(f, rootNode);
                    StatusDisplayer.getDefault().setStatusText(String.format("Preference saved to %s.", f.getPath()));
                } catch (IOException ex) {
                    final String msg = String.format("Can't save table view preference: %s", ex.getMessage());
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        } else {
            NotifyDisplayer.display("There must be a valid preference name.", NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    /**
     * Wrapper around base saveJsonPreferences method which sets an empty prefix
     * string meaning no prefix is supplied.
     *
     * @param saveDir Directory name within users directory to save the
     * configuration file to
     * @param mapper ObjectMapper tied to the JSON object being written to file
     * @param rootNode The root node of the JSON object being written
     */
    public static void saveJsonPreferences(String saveDir, ObjectMapper mapper, ArrayNode rootNode) {
        saveJsonPreferences(saveDir, mapper, rootNode, "");
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory. If filePrefix was provided as a non empty string, then only
     * files prefixed with this value are available to the user to load.
     *
     * @param loadDir the directory location of the JSON files as a string
     * @param filePrefix if not blank a prefix string is pre-pended to the
     * beginning of the filename this can be used to create categorized JSON
     * files with forced name groupings
     *
     * @return The JsonNode of the selected preference or null if nothing is
     * selected
     */
    public static JsonNode loadJsonPreferences(final String loadDir, String filePrefix) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File prefDir = new File(userDir, loadDir);
        final String[] names;
        final ObjectMapper mapper = new ObjectMapper();

        // Store the load directory/prefix so that they can be used by deleteJsonPreference
        currentDir = loadDir;
        currentPrefix = filePrefix;

        // Check the supplied directory for any files, if filePrefix was supplied, only files
        // containing the prefix are returned. Return a list of filenames for the user to select from.
        if (prefDir.isDirectory()) {
            names = prefDir.list((File dir, String name) -> {
                if (filePrefix.isEmpty()) {
                    return name.toLowerCase().endsWith(FILE_EXT);
                }
                return (name.toLowerCase().startsWith(filePrefix) && name.toLowerCase().endsWith(FILE_EXT));
            });
        } else {
            // Nothing to select from - return an empty list
            names = new String[0];
        }

        // chop off ".json" from the filenames
        for (int i = 0; i < names.length; i++) {
            final String nextName = FilenameEncoder.decode(names[i].substring(0, names[i].length() - 5));
            if (StringUtils.isNotBlank(nextName)) {
                names[i] = nextName;
                // Hide any file prefix which the user didn't see when saving
                if (!filePrefix.isEmpty()) {
                    names[i] = names[i].substring(filePrefix.length());
                }
            }
        }

        // Allow user to select a filename from the crafted list using the dialog.
        String fileName = JsonIODialog.getSelection(names);
        if (fileName != null) {
            try {
                // Reconsitute filename to include any file prefix
                if (!filePrefix.isEmpty()) {
                    fileName = filePrefix.concat(fileName);
                }
                return mapper.readTree(new File(prefDir, FilenameEncoder.encode(fileName) + FILE_EXT));
            } catch (IOException ex) {
                final String errorMsg = "An error occured reading file ".concat(FilenameEncoder.encode(fileName) + FILE_EXT);
                LOGGER.log(Level.WARNING, errorMsg, ex);
            }
        }
        return null;
    }

    /**
     * *
     * Allow user to select a preference file to load from the supplied
     * directory.
     *
     * @param loadDir the directory location of the JSON files as a string
     *
     * @return The JsonNode of the selected preference or null if nothing is
     * selected
     */
    public static JsonNode loadJsonPreferences(final String loadDir) {
        return loadJsonPreferences(loadDir, "");
    }

    /**
     * Deletes the selected JSON file from disk, and hence from the selection
     * dialog
     *
     * @param filenameToDelete name of file to delete
     */
    public static void deleteJsonPreference(final String filenameToDelete) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File prefDir = new File(userDir, currentDir);

        if (filenameToDelete != null) {
            final String encodedFilename = FilenameEncoder.encode(currentPrefix.concat(filenameToDelete)) + FILE_EXT;

            // Loop through files in preference directory looking for one matching selected item
            // delete it when found
            if (prefDir.isDirectory()) {
                for (File file : prefDir.listFiles()) {
                    if (file.getName().equals(encodedFilename)) {
                        boolean result = file.delete();
                        if (!result) {
                            final String msg = String.format("Failed to delete file %s from disk", file.getName());
                            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                        }
                        break;
                    }
                }
            }
        }
    }
}

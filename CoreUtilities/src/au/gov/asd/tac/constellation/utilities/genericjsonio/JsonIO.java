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
package au.gov.asd.tac.constellation.utilities.genericjsonio;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.keyboardshortcut.KeyboardShortcutSelectionResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private static final String PREFERENCE_FILE_EXISTS_ALERT_TITLE = "Preference File Exists";
    private static final String PREFERENCE_FILE_EXISTS_ALERT_ERROR_MSG_FORMAT
            = "'%s' already exists. Do you want to overwrite it?";

    private static final String PREFERENCE_FILE_SAVED_MSG_FORMAT
            = "Preference saved to %s.";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Private constructor to hide implicit public one.
     */
    private JsonIO() {
        throw new IllegalStateException("Invalid call to private default constructor");
    }

    /**
     * Save the supplied JSON data in a file, within an allocated subdirectory
     * of the users configuration directory. The filename can optionally be
     * prefixed with a string, which can be used to categorize it.
     * <p/>
     * Normal operation sees an empty filePrefix string supplied. In this case,
     * the name of the file will contain only the filename entered by the user.
     * <p/>
     * If however a non-empty filePrefix is supplied, then the filename provided
     * by the user will be prefixed with this.
     * <p/>
     * This is MOST useful when a dedicated directory exists for similar 'type'
     * configuration files, like the example below:
     *
     * <pre><code>
     * user-dir
     *   +-- tableconfigs
     *   +--- transaction_config1.json
     *   +--- transaction_config2.json
     *   +--- transaction_config3.json
     *   +--- vertex_configa.json
     *   +--- vertex_configb.json
     *   +--- vertex_configc.json
     * </code></pre>
     *
     * The above structure could have been constructed with multiple calls to
     * {@link #saveJsonPreferences(String, ObjectMapper, ArrayNode, String)} all
     * using 'tableconfigs' as the value for {@code saveDir}, three using
     * 'transaction_' for {@code filePrefix} and the other three using
     * 'vertex_'.
     * <p/>
     * This functionality is integrated with
     * {@link #loadJsonPreferences(String, String)} such that if a
     * {@code filePrefix} is supplied in the
     * {@link #loadJsonPreferences(String, String)} call then only files that
     * contain the prefix are offered to the user to load. Hence, it allows a
     * form of configuration file filtering.
     *
     * @param saveDir directory name within the users directory to save the
     * preference file to or empty if it is to be save at the top level
     * @param mapper configured object mapper to write serialize the root node
     * @param rootNode the root object representing the preferences to be
     * written
     * @param filePrefix prefix to be pre-pended to the file name the user
     * @param keyboardShortcut
     * provides or empty if no prefix to be provided
     *
     */
    public static void saveJsonPreferences(final Optional<String> saveDir,
            final Optional<String> filePrefix,
            final Object rootNode,
            final ObjectMapper mapper,
            Optional<Boolean> keyboardShortcut) {
        final File preferenceDirectory = getPrefereceFileDirectory(saveDir);

        // If the preference directory cannot be accessed then return
        if (!preferenceDirectory.isDirectory()) {
            NotifyDisplayer.display(
                    String.format("Can't create preference directory '%s'.", preferenceDirectory),
                    NotifyDescriptor.ERROR_MESSAGE
            );

            return;
        }

         //Record keyboard shortcut
        Optional<String> ks = Optional.empty();
        if (keyboardShortcut.isPresent() && keyboardShortcut.get()) {
           ks = getDefaultKeyboardShortcut(preferenceDirectory);
        }
        
        // Ask the user to provide a file name        
        Optional<String> userInputWithKs = Optional.empty();
        Optional<String> userInputWithoutKs = Optional.empty();
        
        if(keyboardShortcut.isPresent() && keyboardShortcut.get()) {
           Optional<KeyboardShortcutSelectionResult> ksResult = JsonIODialog.getPreferenceFileName(keyboardShortcut, ks, preferenceDirectory);
           if(ksResult.isPresent()) {
               if(Objects.isNull(ksResult.get().getFileName())) {
                   return;
               }
               
               userInputWithKs = Optional.ofNullable(ksResult.get().getFileName());
               ks = Optional.of(ksResult.get().getKeyboardShortcut());
               
           } else {
               return;
           }
        } else {
            userInputWithoutKs = JsonIODialog.getPreferenceFileName();
        }
                
        final Optional<String> userInput = (keyboardShortcut.isPresent() && keyboardShortcut.get()) ? userInputWithKs : userInputWithoutKs;

        // Cancel was pressed. So stop the save.
        if (userInput.isEmpty()) {
            return;
        }

        // If the user hit ok but provided an empty string, then generate one
        final String fileName = StringUtils.isBlank(userInput.get())
                ? String.format(
                        "%s at %s",
                        System.getProperty("user.name"),
                        TIMESTAMP_FORMAT.format(Instant.now())
                )
                : userInput.get();

        // Pre-pend filePrefix
        final String prefixedFileName = filePrefix.orElse("").concat(fileName);       

        //Record keyboard shortcut
        
        /* Optional<String>  ks = Optional.empty();
        if (keyboardShortcut.get() != null && keyboardShortcut.get().booleanValue()) {
           ks = getDefaultKeyboardShortcut(preferenceDirectory);
           if (ks.isEmpty()) {
               //Ask for user defined shortcut
               ks = JsonIODialog.getKeyboardShortcut(preferenceDirectory);
           }           
        } */
        
        final String fileNameWithKeyboardShortcut = ks.orElse("").concat(" " + prefixedFileName);
        
        final File preferenceFile = new File(
                preferenceDirectory,
                FilenameEncoder.encode(fileNameWithKeyboardShortcut + FileExtensionConstants.JSON)
        );

        boolean go = true;

        // If the file exist, ask the user if they want to overwrite
        if (preferenceFile.exists()) {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(PREFERENCE_FILE_EXISTS_ALERT_TITLE);
            alert.setContentText(String.format(
                    PREFERENCE_FILE_EXISTS_ALERT_ERROR_MSG_FORMAT,
                    fileNameWithKeyboardShortcut
            ));

            final Optional<ButtonType> option = alert.showAndWait();
            go = option.isPresent() && option.get() == ButtonType.OK;
        }

        if (go) {
            try {
                // Configure JSON mapper settings
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);

                mapper.writeValue(preferenceFile, rootNode);

                StatusDisplayer.getDefault().setStatusText(
                        String.format(
                                PREFERENCE_FILE_SAVED_MSG_FORMAT,
                                preferenceFile.getPath()
                        )
                );
            } catch (final IOException ex) {
                NotifyDisplayer.display(
                        String.format("Can't save preference file: %s", ex.getMessage()),
                        NotifyDescriptor.ERROR_MESSAGE
                );
            }
        }
    }

    private static Optional<String> getDefaultKeyboardShortcut(File preferenceDirectory) {
        
        for(int index = 1; index <= 5; index++) {
            
            var fi = index;
            FilenameFilter filenameFilter = (d, s) -> {
             return s.startsWith("Ctrl "+ fi);
            };
            
            if(ArrayUtils.isEmpty(preferenceDirectory.list(filenameFilter))) {
                return Optional.of("Ctrl "+index);
            }
        } 
         
         return Optional.empty();
    }
    /**
     * Save the supplied JSON data in a file, within an allocated subdirectory
     * of the users configuration directory.
     *
     * @param saveDir directory name within the users directory to save the
     * configuration file to or empty if it is to be save at the top level
     * @param mapper configured object mapper to write serialize the root node
     * @param rootNode the root object representing the preferences to be
     * written
     * @see #saveJsonPreferences(Optional, ObjectMapper, ArrayNode, Optional)
     */
    public static void saveJsonPreferences(final Optional<String> saveDir,
            final Object rootNode,
            final ObjectMapper mapper) {
        saveJsonPreferences(saveDir, Optional.empty(), rootNode, mapper, Optional.empty());
    }

    /**
     * Save the supplied JSON data in a file, within an allocated subdirectory
     * of the users configuration directory.
     *
     * @param saveDir directory name within the users directory to save the
     * configuration file to or empty if it is to be save at the top level
     * @param rootNode the root object representing the preferences to be
     * written
     * @see #saveJsonPreferences(Optional, ObjectMapper, ArrayNode, Optional)
     */
    public static void saveJsonPreferences(final Optional<String> saveDir,
            final Object rootNode) {
        saveJsonPreferences(saveDir, Optional.empty(), rootNode, OBJECT_MAPPER, Optional.empty());
    }
    
    public static void saveJsonPreferencesWithKeyboardShortcut(final Optional<String> saveDir,
            final Object rootNode) {
        saveJsonPreferences(saveDir, Optional.empty(), rootNode, OBJECT_MAPPER, Optional.of(true));
    }

    /**
     * Save the supplied JSON data in a file, within an allocated subdirectory
     * of the users configuration directory.
     *
     * @param saveDir directory name within the users directory to save the
     * configuration file to or empty if it is to be save at the top level
     * @param filePrefix prefix to be pre-pended to the file name the user
     * provides or empty if no prefix to be provided
     * @param rootNode the root object representing the preferences to be
     * written
     * @see #saveJsonPreferences(Optional, ObjectMapper, ArrayNode, Optional)
     */
    public static void saveJsonPreferences(final Optional<String> saveDir,
            final Optional<String> filePrefix,
            final Object rootNode) {
        saveJsonPreferences(saveDir, filePrefix, rootNode, OBJECT_MAPPER, Optional.empty());
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory. If filePrefix was provided, then only files prefixed with this
     * value are available to the user to load.
     *
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @param filePrefix prefix that is expected to be pre-pended to the file
     * name of the preference file being loaded or empty if no prefix filter is
     * required
     * @return the processed JSON of the selected preference file or null if
     * nothing is selected
     * @see #loadJsonPreferences(Optional, Optional, Function)
     * @deprecated use {@link #loadJsonPreferences(Optional, Optional, Class)}
     * instead
     */
    @Deprecated(since = "2.4")
    public static JsonNode loadJsonPreferences(final Optional<String> loadDir,
            final Optional<String> filePrefix) {
        return loadJsonPreferences(loadDir, filePrefix, file -> {
            try {
                return OBJECT_MAPPER.readTree(file);
            } catch (final IOException ioe) {
                LOGGER.log(
                        Level.WARNING,
                        String.format(
                                "An error occured reading file %s",
                                file.getName()
                        ),
                        ioe
                );
            }
            return null;
        });
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory.
     *
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @return the processed JSON of the selected preference file or null if
     * nothing is selected
     * @see #loadJsonPreferences(Optional, Optional, Function)
     * @deprecated use {@link #loadJsonPreferences(Optional, Class)} instead
     */
    @Deprecated(since = "2.4")
    public static JsonNode loadJsonPreferences(final Optional<String> loadDir) {
        return loadJsonPreferences(loadDir, Optional.empty());
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory. If filePrefix was provided, then only files prefixed with this
     * value are available to the user to load.
     *
     * @param <T> the class that the JSON file to be loaded will be in
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @param filePrefix prefix that is expected to be pre-pended to the file
     * name of the preference file being loaded or empty if no prefix filter is
     * required
     * @param expectedFormat the type representing the JSON in the file to be
     * loaded
     * @param objectMapper the object mapper to perform the de-serialization
     * @return the de-serialized JSON in the requested format
     * @see #loadJsonPreferences(Optional, Optional, Function)
     */
    public static <T> T loadJsonPreferences(final Optional<String> loadDir,
            final Optional<String> filePrefix,
            final TypeReference<T> expectedFormat,
            final ObjectMapper objectMapper) {
        return loadJsonPreferences(loadDir, filePrefix, file -> {
            try {
                return objectMapper.readValue(file, expectedFormat);
            } catch (final IOException ioe) {
                LOGGER.log(
                        Level.WARNING,
                        String.format(
                                "An error occured reading file %s",
                                file.getName()
                        ),
                        ioe
                );
            }
            return null;
        });
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory.
     *
     * @param <T> the class that the JSON file to be loaded will be in
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @param expectedFormat the type representing the JSON in the file to be
     * loaded
     * @return the de-serialized JSON in the requested format
     * @see #loadJsonPreferences(Optional, Optional, Function)
     */
    public static <T> T loadJsonPreferences(final Optional<String> loadDir,
            final TypeReference<T> expectedFormat) {
        return loadJsonPreferences(loadDir, Optional.empty(), expectedFormat, OBJECT_MAPPER);
    }

     public static <T> T loadJsonPreferencesWithFilePrefix(final Optional<String> loadDir,Optional<String> filePrefix,
            final TypeReference<T> expectedFormat) {
         
         return loadJsonPreferencesForFile(loadDir, filePrefix, file -> {
            try {
                return OBJECT_MAPPER.readValue(file, expectedFormat);
            } catch (final IOException ioe) {
                LOGGER.log(
                        Level.WARNING,
                        String.format(
                                "An error occured reading file %s",
                                file.getName()
                        ),
                        ioe
                );
            }
            return null;
        });      
        
    }
     
     protected static <T> T loadJsonPreferencesForFile(final Optional<String> loadDir,
            final Optional<String> filePrefix,
            final Function<File, T> deserializationFunction) {
        final File preferenceDirectory = getPrefereceFileDirectory(loadDir);

        // List the files in the supplied directory that have the required file extension
        // and if filePrefix was supplied, start with the provided prefix.
        final String[] names;
        if (preferenceDirectory.isDirectory()) {
            names = preferenceDirectory.list((File dir, String name)
                    -> StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.JSON)
                    && (filePrefix.isEmpty() || StringUtils.startsWithIgnoreCase(name, filePrefix.get()))
            );
        } else {
            // Nothing to select from - return an empty array
            names = new String[0];
        }

        // Remove the prefix and suffix from the names and pass to the selection dialog        
        final Optional<String> selectedFileName =ArrayUtils.isEmpty(names) ? Optional.empty() : Optional.of(names[0]);

        // Re-add the prefix and suffix, then serialize the preferences to the file
        if (selectedFileName.isPresent()) {            
            return deserializationFunction.apply(new File(
                            preferenceDirectory,
                            FilenameEncoder.encode(selectedFileName.get())
                    )
            );
        }
        return null;
    }   
    /**
     * Allow user to select a preference file to load from the supplied
     * directory. If filePrefix was provided, then only files prefixed with this
     * value are available to the user to load.
     *
     * @param <T> the class that the JSON file to be loaded will be in
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @param filePrefix prefix that is expected to be pre-pended to the file
     * name of the preference file being loaded or empty if no prefix filter is
     * required
     * @param expectedFormat the type representing the JSON in the file to be
     * loaded
     * @return the de-serialized JSON in the requested format
     * @see #loadJsonPreferences(Optional, Optional, Function)
     */
    public static <T> T loadJsonPreferences(final Optional<String> loadDir,
            final Optional<String> filePrefix,
            final TypeReference<T> expectedFormat) {
        return loadJsonPreferences(loadDir, filePrefix, expectedFormat, OBJECT_MAPPER);
    }

    /**
     * Deletes the selected JSON file from disk.
     *
     * @param filename name of file to delete
     * @param loadDir directory name within the users directory to remove the
     * preference file from or empty if it is to be removed at the top level
     * @param filePrefix prefix that is expected to be pre-pended to the file
     * name of the preference file being deleted or empty if no prefix filter is
     * required
     */
    public static void deleteJsonPreference(final String filename,
            final Optional<String> loadDir,
            final Optional<String> filePrefix) {
        final File preferenceDirectory = getPrefereceFileDirectory(loadDir);

        if (filename != null) {
            // Re-add the prefix and extension
            final File fileToDelete = new File(
                    preferenceDirectory,
                    FilenameEncoder.encode(filePrefix.orElse("").concat(filename)) + FileExtensionConstants.JSON
            );

            // Attempt to delete
            try {
                Files.deleteIfExists(fileToDelete.toPath());
            } catch (final SecurityException | IOException ex) {
                NotifyDisplayer.display(
                        String.format("Failed to delete file %s from disk", fileToDelete.getName()),
                        NotifyDescriptor.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Allow user to select a preference file to load from the supplied
     * directory. If filePrefix was provided, then only files prefixed with this
     * value are available to the user to load.
     *
     * @param loadDir directory name within the users directory to load the
     * preference file from or empty if it is to be loaded at the top level
     * @param filePrefix prefix that is expected to be pre-pended to the file
     * name of the preference file being loaded or empty if no prefix filter is
     * required
     * @param deserializationFunction a function that take the file to be loaded
     * and de-serializes the JSON in the required class
     * @return the processed JSON of the selected preference file or null if
     * nothing is selected
     */
    protected static <T> T loadJsonPreferences(final Optional<String> loadDir,
            final Optional<String> filePrefix,
            final Function<File, T> deserializationFunction) {
        final File preferenceDirectory = getPrefereceFileDirectory(loadDir);

        // List the files in the supplied directory that have the required file extension
        // and if filePrefix was supplied, start with the provided prefix.
        final String[] names;
        if (preferenceDirectory.isDirectory()) {
            names = preferenceDirectory.list((File dir, String name)
                    -> StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.JSON)
                    && (filePrefix.isEmpty() || StringUtils.startsWithIgnoreCase(name, filePrefix.get()))
            );
        } else {
            // Nothing to select from - return an empty array
            names = new String[0];
        }

        // Remove the prefix and suffix from the names and pass to the selection dialog
        final int filePrefixLength = filePrefix.orElse("").length();
        final Optional<String> selectedFileName = JsonIODialog.getSelection(
                Arrays.stream(names)
                        .map(name -> FilenameEncoder.decode(name.substring(0, name.length() - 5)))
                        .filter(StringUtils::isNotBlank)
                        .map(name -> name.substring(filePrefixLength))
                        .collect(Collectors.toList()),
                loadDir,
                filePrefix
        );

        // Re-add the prefix and suffix, then serialize the preferences to the file
        if (selectedFileName.isPresent()) {
            final String prefixedFilename = filePrefix.orElse("")
                    .concat(selectedFileName.get());
            return deserializationFunction.apply(new File(
                            preferenceDirectory,
                            FilenameEncoder.encode(prefixedFilename) + FileExtensionConstants.JSON
                    )
            );
        }
        return null;
    }  
    
     
    /**
     * Gets the preference file directory and appends the passed sub directory
     * path to it. If the complete directory path is not present, it will
     * attempt to create it.
     *
     * @param subDirectory the directory path to append to the system
     * preferences directory path
     * @return a file with the passed sub directory appended to the system
     * preferences directory path
     */
    protected static File getPrefereceFileDirectory(final Optional<String> subDirectory) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);

        final File preferenceDirectory = new File(userDir, subDirectory.orElse(""));

        if (!preferenceDirectory.exists()) {
            preferenceDirectory.mkdir();
        }

        return preferenceDirectory;
    }
}

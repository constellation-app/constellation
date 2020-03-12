/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.GenericJsonIO;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
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
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * Load and save Json files to and from a specified directory
 * 
 * 
 * @author formalhaut69
 */
public class JsonIO {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());
    private static String CURRENT_DIR = "";
    
    
    /**
     * Encode the file name and save the Json File in the specified directory
     *
     * @param JsonDirectory the directory location of the Json files as a string
     * @param mapper A JSON document to store everything in
     * @param rootNode an array of Json objects
     *
     */
    public static void saveJsonPreferences(String JsonDirectory, ObjectMapper mapper, ArrayNode rootNode) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File JsonFileLocation = new File(userDir, JsonDirectory);
        
        if (!JsonFileLocation.exists()) {
            JsonFileLocation.mkdir();
        }
        
        if (!JsonFileLocation.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", JsonFileLocation);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        
        String fileName = JsonIODialog.getName();
        if(fileName.equals("")){
            fileName = String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(Instant.now()));
        }
        if(!fileName.equals(" ")){
            if (fileName != null){
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
                final File f = new File(JsonFileLocation, encode(fileName + ".json"));
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
                final NotifyDescriptor nd = new NotifyDescriptor.Message("There must be a valid preference name.", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
        
    }
    
    /**
     * Encode a String so it can be used as a filename.
     *
     * @param s The String to be encoded.
     *
     * @return The encoded String.
     */
    public static String encode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (final char c : s.toCharArray()) {
            if (isValidFileCharacter(c)) {
                b.append(c);
            } else {
                b.append(String.format("_%04x", (int) c));
            }
        }

        return b.toString();
    } 
    
   /**
     * Decode a String that has been encoded by {@link encode(String)}.
     *
     * @param s The String to be decoded.
     *
     * @return The decoded String.
     */
    static String decode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '_') {
                b.append(c);
            } else {
                final String hex = s.substring(i + 1, Math.min(i + 5, s.length()));
                if (hex.length() == 4) {
                    try {
                        final int value = Integer.parseInt(hex, 16);
                        b.append((char) value);
                        i += 4;
                    } catch (final NumberFormatException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return b.toString();
    }
   
   static boolean isValidFileCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == '-' || c == '.';
    }
   
   /**
     * Decode the file names and load the selected Json file
     *
     * @param JsonDirectory the directory location of the Json files as a string
     *
     * @return The JsonNode of the selected preference or null if nothing is selected
     */
    public static JsonNode loadJsonPreferences(String JsonDirectory) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File JsonFileLocation = new File(userDir, JsonDirectory);
        final String[] names;
        final ObjectMapper mapper = new ObjectMapper();
        
        CURRENT_DIR = JsonDirectory;
        
        
       if (JsonFileLocation.isDirectory()) {
            names = JsonFileLocation.list((File dir, String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        } 
       
        //chop off ".json" from the filenames
        for (int i = 0; i < names.length; i++) {
            names[i] = decode(names[i].substring(0, names[i].length() - 5));
        }
        
        //Get name from the dialog window
        final String JsonFileName = JsonIODialog.getSelection(names);
        if(JsonFileName != null){
            try {
                return mapper.readTree(new File(JsonFileLocation, encode(JsonFileName) + ".json"));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
        
    }
    
    /**
     * Deletes the selected Json file
     *
     * @param JsonDirectory the directory location of the Json files as a string
     *
     */
    static void deleteJsonPreference(String selectedItem) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File JsonFileLocation = new File(userDir, CURRENT_DIR);
        
        if (JsonFileLocation.isDirectory()) {
            for(File file : JsonFileLocation.listFiles()){
                if(file.getName().equals(selectedItem + ".json")){
                    file.delete();
                    break;
                }
            }
        }
    }
    
}

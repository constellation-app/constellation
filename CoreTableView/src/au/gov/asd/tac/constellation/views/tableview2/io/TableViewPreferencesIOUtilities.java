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
package au.gov.asd.tac.constellation.views.tableview2.io;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * Save and Load TableView preferences
 * 
 * @author formalhaut69
 */
public class TableViewPreferencesIOUtilities {
    
    private static final String TABLE_VIEW_PREF_DIR = "TableViewPreferences";
    private static final String COLUMN_ORDER_PREF_OBJECT = "ColumnOrderPreference";
    private static final String SORT_BY_COLUMN_OBJECT = "SortByColumnObject";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());
    
    
    /**
     * Save the current order of the table view as a JSON file
     *
     * @param the table View's table
     *
     */
    public static void savePreferences(TableView<ObservableList<String>> table) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File tableViewPreferencesDirectory = new File(userDir, TABLE_VIEW_PREF_DIR);
        final ObservableList<TableColumn<ObservableList<String>, ?>> columns = table.getColumns();
        
        if (!tableViewPreferencesDirectory.exists()) {
            tableViewPreferencesDirectory.mkdir();
        }
        
        if (!tableViewPreferencesDirectory.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", tableViewPreferencesDirectory);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        
        // A JSON document to store everything in;
        // an array of objects where each array element is a tab, and the objects are the parameters in each tab.
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode rootNode = mapper.createArrayNode();
        final ObjectNode tabNode = rootNode.addObject();
        final ObjectNode columnOrderPrefObject = tabNode.putObject(COLUMN_ORDER_PREF_OBJECT);
        final ObjectNode sortByColumnNode = tabNode.putObject(SORT_BY_COLUMN_OBJECT);
        ArrayNode columnOrderArrayNode = columnOrderPrefObject.putArray("columnOrderArray");
        
        
        int i = 0;
        while(i < columns.size() && columns.get(i).isVisible()){
            columnOrderArrayNode.add(columns.get(i).getText());
            i++;
        }
        
        if(!table.getSortOrder().isEmpty()){
            sortByColumnNode.put(table.getSortOrder().get(0).getText(), table.getSortOrder().get(0).getSortType().name());
        } else {
            //The table isn't being sorted by any column so don't save anything
           sortByColumnNode.put("NO SORT BY COLUMN", "");
        }
        
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        
        String fileName = TableViewPreferencesDialog.getTableViewPreferenceName();
        if(fileName.equals("")){
            fileName = String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(Instant.now()));
        }
        
        if (fileName != null) {
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
            final File f = new File(tableViewPreferencesDirectory, encode(fileName + ".json"));
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
   
    /**
     * Load in the preferences from the JSON file and re-order the table 
     *
     * @param the table View's table
     *
     */
    public static void loadPreferences(TableView<ObservableList<String>> table) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File tableViewPreferencesDirectory = new File(userDir, TABLE_VIEW_PREF_DIR);
        final String[] names;
        
       if (tableViewPreferencesDirectory.isDirectory()) {
            names = tableViewPreferencesDirectory.list((File dir, String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        } 
       
        //chop off ".json" from the filenames
        // Chop off ".json".
        for (int i = 0; i < names.length; i++) {
            names[i] = decode(names[i].substring(0, names[i].length() - 5));
        }
        
        final String queryName = TableViewPreferencesDialog.getTableViewPreferences(names);
        
        if (queryName != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode root = mapper.readTree(new File(tableViewPreferencesDirectory, encode(queryName)));
                
                for (final JsonNode step : root) {
                    final JsonNode columnOrderPreference = step.get(COLUMN_ORDER_PREF_OBJECT);
                    final JsonNode JsonColumnOrderArray = columnOrderPreference.get("columnOrderArray");
                    final JsonNode sortByColumnPreference = step.get(SORT_BY_COLUMN_OBJECT);
                    final String sortByColumn = sortByColumnPreference.fieldNames().next();
                    ArrayList<TableColumn<ObservableList<String>, ?>> newColumnOrder = new ArrayList<>();
                    
                    for (final JsonNode JsonSavedColumn : JsonColumnOrderArray) {
                        TableColumn<ObservableList<String>, ?> copy;
                        for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
                            if(column.getText().equals(JsonSavedColumn.textValue())){
                                copy = column;
                                copy.setVisible(true);
                                newColumnOrder.add(copy);
                            }
                        }
                        table.getColumns().removeIf(tc -> tc.getText().equals(JsonSavedColumn.textValue()));
                    }
                    
                    //set all the other columns to not visible
                    for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
                        newColumnOrder.add(column);
                        column.setVisible(false);
                    }
                    table.getColumns().setAll(newColumnOrder);
                    table.getSortOrder().clear();
                    for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
                        if(column.getText().equals(sortByColumn)){
                            table.getSortOrder().add(column);
                        }
                    }
                }
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
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

}

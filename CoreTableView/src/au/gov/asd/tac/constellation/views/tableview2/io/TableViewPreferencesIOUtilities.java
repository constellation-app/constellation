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

import au.gov.asd.tac.constellation.functionality.GenericJsonIO.JsonIO;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
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
     */
    public static void savePreferences(final TableView<ObservableList<String>> table) {
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
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode rootNode = mapper.createArrayNode();
        final ObjectNode global = rootNode.addObject();
        final ObjectNode columnOrderPrefObject = global.putObject(COLUMN_ORDER_PREF_OBJECT);
        final ObjectNode sortByColumnNode = global.putObject(SORT_BY_COLUMN_OBJECT);
        ArrayNode columnOrderArrayNode = columnOrderPrefObject.putArray("columnOrderArray");

        int i = 0;
        while (i < columns.size() && columns.get(i).isVisible()) {
            columnOrderArrayNode.add(columns.get(i).getText());
            i++;
        }

        if (!table.getSortOrder().isEmpty()) {
            sortByColumnNode.put(table.getSortOrder().get(0).getText(), table.getSortOrder().get(0).getSortType().name());
        } else {
            // the table isn't being sorted by any column so don't save anything
            sortByColumnNode.put("NO SORT BY COLUMN", "");
        }

        JsonIO.saveJsonPreferences(TABLE_VIEW_PREF_DIR, mapper, rootNode);

    }

    /**
     * Load in the preferences from the JSON file and re-order the table
     *
     * @param the table View's table
     */
    public static void loadPreferences(TableView<ObservableList<String>> table) {
        final JsonNode root = JsonIO.loadJsonPreferences(TABLE_VIEW_PREF_DIR);
        if (root != null) {
            for (final JsonNode step : root) {
                final JsonNode columnOrderPreference = step.get(COLUMN_ORDER_PREF_OBJECT);
                final JsonNode JsonColumnOrderArray = columnOrderPreference.get("columnOrderArray");
                final JsonNode sortByColumnPreference = step.get(SORT_BY_COLUMN_OBJECT);
                final String sortByColumn = sortByColumnPreference.fieldNames().next();
                ArrayList<TableColumn<ObservableList<String>, ?>> newColumnOrder = new ArrayList<>();

                for (final JsonNode JsonSavedColumn : JsonColumnOrderArray) {
                    TableColumn<ObservableList<String>, ?> copy;
                    for (TableColumn<ObservableList<String>, ?> column : table.getColumns()) {
                        if (column.getText().equals(JsonSavedColumn.textValue())) {
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
                    if (column.getText().equals(sortByColumn)) {
                        table.getSortOrder().add(column);
                    }
                }
            }
        }
    }
}

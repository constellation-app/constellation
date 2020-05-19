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
package au.gov.asd.tac.constellation.views.tableview2.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 * Save and Load TableView preferences.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class TableViewPreferencesIOUtilities {

    private static final String TABLE_VIEW_PREF_DIR = "TableViewPreferences";
    private static final String COLUMN_ORDER_NODE = "ColumnOrder";
    private static final String COLUMN_SORT_NODE = "SortByColumn";
    private static final String VERTEX_FILE_PREFIX = "vertex-";
    private static final String TRANSACTION_FILE_PREFIX = "transaction-";

    /**
     * Private constructor to hide implicit public one.
     */
    private TableViewPreferencesIOUtilities() {
        throw new IllegalStateException("Invalid call to private default constructor");
    }

    /**
     * Save details of the currently displayed tables displayed columns and
     * their order as well as details of any sorting being performed on it. The
     * user will be prompted for a name to save the configuration file as which
     * will be appended to a tag indicating the type of content being displayed
     * in the table.
     *
     * @param tableType Indication of whether the table is displaying in vertex
     * of transaction mode.
     * @param table the tables content.
     */
    public static void savePreferences(GraphElementType tableType, final TableView<ObservableList<String>> table) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File prefDir = new File(userDir, TABLE_VIEW_PREF_DIR);
        String filePrefix = (tableType == GraphElementType.VERTEX ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX);
        final ObservableList<TableColumn<ObservableList<String>, ?>> columns = table.getColumns();

        // Ensure preferences directory exists.
        if (!prefDir.exists()) {
            prefDir.mkdir();
        }
        if (!prefDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", prefDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        // Create the core structure of the JSON object containing nodes for the key characteristics
        // of the graph that are being saved
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode rootNode = mapper.createArrayNode();
        final ObjectNode global = rootNode.addObject();
        final ArrayNode colOrderArrayNode = global.putArray(COLUMN_ORDER_NODE);
        final ObjectNode colSortNode = global.putObject(COLUMN_SORT_NODE);

        // Populate elements of JSON structure based on supplied graph information
        int i = 0;
        while (i < columns.size() && columns.get(i).isVisible()) {
            colOrderArrayNode.add(columns.get(i).getText());
            i++;
        }

        // Store details of the column being sorted by, and its direction if sorting has been enabled.
        // The node will cotain a name/value pair, the name representing the column name, the value
        // representing the direction.
        if (!table.getSortOrder().isEmpty()) {
            colSortNode.put(table.getSortOrder().get(0).getText(), table.getSortOrder().get(0).getSortType().name());
        } else {
            // the table isn't being sorted by any column so don't save anything
            colSortNode.put("", "");
        }
        JsonIO.saveJsonPreferences(TABLE_VIEW_PREF_DIR, mapper, rootNode, filePrefix);
    }

    /**
     * Load in the preferences from the JSON file and re-order the table
     *
     * @param tableType Indication of whether the table is displaying in vertex
     * of transaction mode.
     *
     * @return A Tuple containing: ordered list of table columns (1) and second
     * Tuple (2) containing details of sort column (1) and sort order (2).
     */
    public static Tuple<ArrayList<String>, Tuple<String, TableColumn.SortType>> getPreferences(GraphElementType tableType) {
        String filePrefix = (tableType == GraphElementType.VERTEX ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX);
        final JsonNode root = JsonIO.loadJsonPreferences(TABLE_VIEW_PREF_DIR, filePrefix);
        final ArrayList<String> colOrder = new ArrayList<>();
        String sortColumn = "";
        TableColumn.SortType sortType = TableColumn.SortType.ASCENDING;

        if (root != null) {
            for (final JsonNode step : root) {
                final JsonNode colOrderArrayNode = step.get(COLUMN_ORDER_NODE);
                final JsonNode colSortNode = step.get(COLUMN_SORT_NODE);

                // Extract column order details
                for (final JsonNode columnNode : colOrderArrayNode) {
                    colOrder.add(columnNode.textValue());
                }

                // Extract sort order details
                sortColumn = colSortNode.fieldNames().next();
                if (colSortNode.get(sortColumn).asText().equals("DESCENDING")) {
                    sortType = TableColumn.SortType.DESCENDING;
                }
            }
        }
        return new Tuple<>(colOrder, new Tuple<>(sortColumn, sortType));
    }
}

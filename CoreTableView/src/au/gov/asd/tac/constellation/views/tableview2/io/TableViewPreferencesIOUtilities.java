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
package au.gov.asd.tac.constellation.views.tableview2.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 * Save and Load TableView preferences.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class TableViewPreferencesIOUtilities {
    private static final Logger LOGGER = Logger.getLogger(TableViewPreferencesIOUtilities.class.getName());
    
    private static final String TABLE_VIEW_PREF_DIR = "TableViewPreferences";
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
    public static void savePreferences(final GraphElementType tableType, final TableView<ObservableList<String>> table,
            final int pageSize) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File prefDir = new File(userDir, TABLE_VIEW_PREF_DIR);
        final String filePrefix = tableType == GraphElementType.VERTEX ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX;

        // Ensure preferences directory exists.
        if (!prefDir.exists()) {
            prefDir.mkdir();
        }
        if (!prefDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", prefDir);
            NotifyDisplayer.display(msg, NotifyDescriptor.ERROR_MESSAGE);
            return;
        }
        
        final TablePreferences tablePreferences = new TablePreferences();
        
        tablePreferences.setColumnOrder(
                table.getColumns().stream()
                        .filter(column -> column.isVisible())
                        .map(column -> column.getText())
                        .collect(Collectors.toList())
        );

        tablePreferences.setMaxRowsPerPage(pageSize);
        
        if (!table.getSortOrder().isEmpty()) {
            tablePreferences.setSortByColumn(
                    Map.of(
                            table.getSortOrder().get(0).getText(),
                            table.getSortOrder().get(0).getSortType()
                    )
            );
        }
        
        final ObjectMapper mapper = new ObjectMapper();
        
        JsonIO.saveJsonPreferences(TABLE_VIEW_PREF_DIR, mapper,
                mapper.valueToTree(List.of(tablePreferences)), filePrefix);
    }

    /**
     * Load in the preferences from the JSON file and re-order the table
     *
     * @param tableType Indication of whether the table is displaying in vertex
     * of transaction mode.
     * @param defaultPageSize The page size to load in if the preference being
     * loaded in doesn't have one.
     *
     * @return A Tuple containing: ordered list of table columns (1) and second
     * Tuple (2) containing details of sort column (1) and sort order (2).
     */
    public static TablePreferences getPreferences(final GraphElementType tableType,
            final int defaultPageSize) {
        final String filePrefix = (tableType == GraphElementType.VERTEX ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX);
        
        try {
            final JsonNode root = JsonIO.loadJsonPreferences(TABLE_VIEW_PREF_DIR, filePrefix);

            final TablePreferences tablePreferences;
            if (root == null) {
                tablePreferences = new TablePreferences();
            } else {
                final ObjectMapper mapper = new ObjectMapper();
                tablePreferences = mapper
                        .treeToValue(((ArrayNode) root).get(0), TablePreferences.class);
            }
            
            if (tablePreferences.getMaxRowsPerPage() == null) {
                tablePreferences.setMaxRowsPerPage(defaultPageSize);
            }
            
            return tablePreferences;
        } catch (IOException ex) {
            final String errorMsg = "An error occured converting preference file contents into "
                    + "the expected object";
            LOGGER.log(Level.WARNING, errorMsg, ex);
            return null;
        }
    }
}

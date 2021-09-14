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
import au.gov.asd.tac.constellation.views.tableview2.api.UserTablePreferences;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 * Save and Load table preferences.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class TableViewPreferencesIoProvider {
    private static final Logger LOGGER = Logger.getLogger(TableViewPreferencesIoProvider.class.getName());
    
    private static final String TABLE_VIEW_PREF_DIR = "TableViewPreferences";
    
    private static final String VERTEX_FILE_PREFIX = "vertex-";
    private static final String TRANSACTION_FILE_PREFIX = "transaction-";

    private TableViewPreferencesIoProvider() {
    }

    /**
     * Saves details of the table's current preferences. Persists the preferences
     * as JSON to the local disk.
     *
     * @param tableElementType the current table setting specifying which element
     *     type to display
     * @param table the current table
     * @param pageSize the current table page size
     */
    public static void savePreferences(final GraphElementType tableElementType,
                                       final TableView<ObservableList<String>> table,
                                       final int pageSize) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File prefDir = new File(userDir, TABLE_VIEW_PREF_DIR);
        final String filePrefix = tableElementType == GraphElementType.VERTEX
                ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX;

        // Ensure preferences directory exists.
        if (!prefDir.exists()) {
            prefDir.mkdir();
        }
        if (!prefDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", prefDir);
            NotifyDisplayer.display(msg, NotifyDescriptor.ERROR_MESSAGE);
            return;
        }
        
        final UserTablePreferences tablePreferences = new UserTablePreferences();
        
        tablePreferences.setColumnOrder(
                table.getColumns().stream()
                        .filter(column -> column.isVisible())
                        .map(column -> column.getText())
                        .collect(Collectors.toList())
        );

        tablePreferences.setMaxRowsPerPage(pageSize);
        
        if (!table.getSortOrder().isEmpty()) {
            tablePreferences.setSortByColumn(
                    ImmutablePair.of(
                            table.getSortOrder().get(0).getText(),
                            table.getSortOrder().get(0).getSortType()
                    )
            );
        }
        
        final ObjectMapper mapper = new ObjectMapper();
        
        JsonIO.saveJsonPreferences(Optional.of(TABLE_VIEW_PREF_DIR), mapper,
                mapper.valueToTree(List.of(tablePreferences)), Optional.of(filePrefix));
    }

    /**
     * Load in the preferences from the JSON file into the
     * {@link UserTablePreferences} POJO.
     *
     * @param tableElementType the current table setting specifying which element
     *     type to display
     *
     * @return a populated {@link UserTablePreferences} from the local file
     */
    public static UserTablePreferences getPreferences(final GraphElementType tableElementType) {
        final String filePrefix = tableElementType == GraphElementType.VERTEX
                ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX;
        
        try {
            final JsonNode root = JsonIO.loadJsonPreferences(Optional.of(TABLE_VIEW_PREF_DIR), Optional.of(filePrefix));

            final UserTablePreferences tablePreferences;
            if (root == null) {
                tablePreferences = new UserTablePreferences();
            } else {
                final ObjectMapper mapper = new ObjectMapper()
                        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
                
                tablePreferences = mapper
                        .treeToValue(((ArrayNode) root).get(0), UserTablePreferences.class);
            }
            
            // TODO This needs to be removed once all old versions are deprecated.
            // This is purely here for backward compatibility.
            if (ImmutablePair.of("", null).equals(tablePreferences.getSort())) {
                tablePreferences.setSortByColumn(ImmutablePair.of("", TableColumn.SortType.ASCENDING));
            }
            
            return tablePreferences;
        } catch (final IOException ex) {
            final String errorMsg = "An error occured converting preference file contents into "
                    + "the expected object";
            LOGGER.log(Level.WARNING, errorMsg, ex);
            return null;
        }
    }
}

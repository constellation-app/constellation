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
package au.gov.asd.tac.constellation.views.tableview.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Save and Load table preferences.
 *
 * @author formalhaut69
 * @author serpens24
 */
public class TableViewPreferencesIoProvider {

    private static final String TABLE_VIEW_PREF_DIR = "TableViewPreferences";

    private static final String VERTEX_FILE_PREFIX = "vertex-";
    private static final String TRANSACTION_FILE_PREFIX = "transaction-";

    /**
     * Private constructor to prevent instantiation.
     */
    private TableViewPreferencesIoProvider() {
    }

    /**
     * Saves details of the table's current preferences. Persists the
     * preferences as JSON to the local disk.
     *
     * @param tableElementType the current table setting specifying which
     * element type to display
     * @param table the current table
     * @param pageSize the current table page size
     */
    public static void savePreferences(final GraphElementType tableElementType,
            final TableView<ObservableList<String>> table,
            final int pageSize) {
        final String filePrefix = tableElementType == GraphElementType.VERTEX
                ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX;

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

        JsonIO.saveJsonPreferences(Optional.of(TABLE_VIEW_PREF_DIR), Optional.of(filePrefix),
                List.of(tablePreferences));
    }

    /**
     * Load in the preferences from the JSON file into the
     * {@link UserTablePreferences} POJO.
     *
     * @param tableElementType the current table setting specifying which
     * element type to display
     *
     * @return a populated {@link UserTablePreferences} from the local file
     */
    public static UserTablePreferences getPreferences(final GraphElementType tableElementType) {
        final String filePrefix = tableElementType == GraphElementType.VERTEX
                ? VERTEX_FILE_PREFIX : TRANSACTION_FILE_PREFIX;

        final List<UserTablePreferences> root = JsonIO.loadJsonPreferences(
                Optional.of(TABLE_VIEW_PREF_DIR),
                Optional.of(filePrefix),
                new TypeReference<List<UserTablePreferences>>() {
        }
        );

        return root == null ? new UserTablePreferences() : root.get(0);
    }
}

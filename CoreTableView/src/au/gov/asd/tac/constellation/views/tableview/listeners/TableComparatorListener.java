/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview.listeners;

import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import java.util.Comparator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 * Notified when a sort event happens causing the rows in the table to be
 * re-ordered.
 *
 * @author formalhaunt
 */
public class TableComparatorListener implements ChangeListener<Comparator<? super ObservableList<String>>> {

    private final TablePane tablePane;

    /**
     * Creates a new table comparator listener.
     *
     * @param tablePane the pane containing the table view
     */
    public TableComparatorListener(final TablePane tablePane) {
        this.tablePane = tablePane;
    }

    /**
     * Updates the table pagination based on the new sort order. Uses the flag
     * {@link ActiveTableReference#sortingListenerActive} in order to prevent
     * more than one pagination update from happening at any one time due to a
     * multiple sort changes.
     *
     * @param observable not used, can be null
     * @param oldValue not used, can be null
     * @param newValue not used, can be null
     * @see TableSortTypeListener
     * @see ChangeListener#changed(javafx.beans.value.ObservableValue,
     * java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(final ObservableValue<? extends Comparator<? super ObservableList<String>>> observable,
            final Comparator<? super ObservableList<String>> oldValue, 
            final Comparator<? super ObservableList<String>> newValue) {
        if (!tablePane.getActiveTableReference().isSortingListenerActive()) {
            tablePane.getActiveTableReference().setSortingListenerActive(true);
            tablePane.getActiveTableReference()
                    .updatePagination(tablePane.getActiveTableReference().getUserTablePreferences().getMaxRowsPerPage(), 
                            tablePane);

            tablePane.getActiveTableReference().setSortingListenerActive(false);
        }
    }

}

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
package au.gov.asd.tac.constellation.views.tableview2.listeners;

import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 * Notified when a sort event happens causing the rows in the table to be re-ordered.
 *
 * @author formalhaunt
 */
public class TableComparatorListener implements ChangeListener<Comparator<? super ObservableList<String>>> {
    
    private final TableViewPane tablePane;
    
    /**
     * Creates a new table comparator listener.
     *
     * @param tablePane the pane containing the table view
     */
    public TableComparatorListener(final TableViewPane tablePane) {
        this.tablePane = tablePane;
    }
    
    /**
     * Updates the table pagination based on the new sort order. Uses the flag
     * {@link TableService#sortingListenerActive} in order to prevent more than
     * one pagination update from happening at any one time due to a multiple
     * sort changes.
     * 
     * @param observable not used, can be null
     * @param oldValue not used, can be null
     * @param newValue not used, can be null
     * @see TableSortTypeListener
     * @see ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(final ObservableValue<? extends Comparator<? super ObservableList<String>>> observable,
                        final Comparator<? super ObservableList<String>> oldValue,
                        final Comparator<? super ObservableList<String>> newValue) {
        if (!tablePane.getTableService().isSortingListenerActive()) {
            tablePane.getTableService().setSortingListenerActive(true);
            tablePane.getTableService().updatePagination(
                    tablePane.getTableService().getTablePreferences().getMaxRowsPerPage()
            );
            
            Platform.runLater(() -> tablePane.setCenter(tablePane.getTableService().getPagination()));
            
            tablePane.getTableService().setSortingListenerActive(false);
        }
    }
    
}

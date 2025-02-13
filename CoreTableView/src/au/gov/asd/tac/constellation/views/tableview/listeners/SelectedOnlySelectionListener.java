/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Listens for table selection events and updates the
 * {@link ActiveTableReference#selectedOnlySelectedRows} set with the new
 * selections. These events are only handled though if the table
 * <b>IS</b> in "Selected Only" mode.
 *
 * @author formalhaunt
 */
public class SelectedOnlySelectionListener implements ListChangeListener {

    private final Table table;

    /**
     * Creates a new selected only selection lister.
     *
     * @param table the table the listener will be attached to
     */
    public SelectedOnlySelectionListener(final Table table) {
        this.table = table;
    }

    /**
     * Updates the {@link ActiveTableReference#selectedOnlySelectedRows} set
     * with the currently selected rows in the table. This listener does not
     * affect the selection in the graph.
     * <p/>
     * The listener is only active if the current table state is not null and
     * the table <b>IS</b> in "Selected Only" mode. Selections on the table have
     * no effect on the graph whilst in this mode.
     *
     * @param change not used, can be null
     * @see TableSelectionListener
     * @see
     * ListChangeListener#onChanged(javafx.collections.ListChangeListener.Change)
     */
    @Override
    public void onChanged(final Change change) {
        if (getTableViewTopComponent().getCurrentState() != null 
                && getTableViewTopComponent().getCurrentState().isSelectedOnly()) {
            final ObservableList<ObservableList<String>> rows = table.getTableView().getItems();
            rows.forEach(row -> {
                if (table.getTableView().getSelectionModel().getSelectedItems().contains(row)) {
                    getActiveTableReference().getSelectedOnlySelectedRows().add(row);
                } else if (getActiveTableReference().getSelectedOnlySelectedRows().contains(row)) {
                    // remove the row from selected items as it's no longer selected in the table
                    getActiveTableReference().getSelectedOnlySelectedRows().remove(row);
                }
            });
        }
    }

    /**
     * Convenience method for accessing the table view top component.
     *
     * @return the table view top component
     */
    private TableViewTopComponent getTableViewTopComponent() {
        return table.getParentComponent().getParentComponent();
    }

    /**
     * Convenience method for accessing the active table reference.
     *
     * @return the active table reference
     */
    private ActiveTableReference getActiveTableReference() {
        return table.getParentComponent().getActiveTableReference();
    }
}

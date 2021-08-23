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

import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import java.util.Set;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 *
 * @author formalhaunt
 */
public class SelectedOnlySelectionListener implements ListChangeListener {

    private final TableViewTopComponent tableTopComponent;
    private final TableView<ObservableList<String>> table;
    private final Set<ObservableList<String>> selectedOnlySelectedRows;
    
    public SelectedOnlySelectionListener(final TableViewTopComponent tableTopComponent,
                                         final TableView<ObservableList<String>> table,
                                         final Set<ObservableList<String>> selectedOnlySelectedRows) {
        this.tableTopComponent = tableTopComponent;
        this.table = table;
        this.selectedOnlySelectedRows = selectedOnlySelectedRows;
    }
    
    @Override
    public void onChanged(final Change change) {
        if (tableTopComponent.getCurrentState() != null
                && tableTopComponent.getCurrentState().isSelectedOnly()) {
            final ObservableList<ObservableList<String>> rows = table.getItems();
            rows.forEach(row -> {
                if (table.getSelectionModel().getSelectedItems().contains(row)) {
                    selectedOnlySelectedRows.add(row);
                } else if (selectedOnlySelectedRows.contains(row)) {
                    // remove the row from selected items as it's no longer selected in the table
                    selectedOnlySelectedRows.remove(row);
                } else {
                    // Do nothing
                }
            });
        }
    }
    
}

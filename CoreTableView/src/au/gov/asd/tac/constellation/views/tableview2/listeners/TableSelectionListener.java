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

import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Listens for table selection events and updates the graph with the new selections
 * if the table <b>IS NOT</b> in "Selected Only" mode.
 *
 * @author formalhaunt
 */
public class TableSelectionListener implements ChangeListener<ObservableList<String>> {

    private final TableViewTopComponent tableTopComponent;
    private final TableView<ObservableList<String>> tableView;
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;
    
    /**
     * Creates a new table selection listener.
     *
     * @param tableTopComponent component holding the table state
     * @param tableView the {@link TableView} that the listener is linked to
     * @param rowToElementIdIndex maps the table row to the graph element ID
     */
    public TableSelectionListener(final TableViewTopComponent tableTopComponent,
                                  final TableView<ObservableList<String>> tableView,
                                  final Map<ObservableList<String>, Integer> rowToElementIdIndex) {
        this.tableTopComponent = tableTopComponent;
        this.tableView = tableView;
        this.rowToElementIdIndex = rowToElementIdIndex;
    }
    
    /**
     * Updates the graph with the current table selection.
     * <p/>
     * This listener is only active if the current table state is not null and the
     * table <b>IS NOT</b> in "Selected Only" mode. If the table is in "Selected Only" mode
     * then selections made to the table will have no effect on the graph.
     *
     * @param observable not used, can be null
     * @param oldValue not used, can be null
     * @param newValue not used, can be null
     * @see SelectedOnlySelectionListener
     * @see ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(final ObservableValue<? extends ObservableList<String>> observable,
                        final ObservableList<String> oldValue,
                        final ObservableList<String> newValue) {
        if (tableTopComponent.getCurrentState() != null
                && !tableTopComponent.getCurrentState().isSelectedOnly()) {
            TableViewUtilities.copySelectionToGraph(tableView, rowToElementIdIndex,
                    tableTopComponent.getCurrentState().getElementType(), tableTopComponent.getCurrentGraph());
        }
    }
    
}

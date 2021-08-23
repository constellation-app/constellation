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
 *
 * @author formalhaunt
 */
public class TableSelectionListener implements ChangeListener<ObservableList<String>> {

    private final TableViewTopComponent tableTopComponent;
    private final TableView<ObservableList<String>> tableView;
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;
    
    public TableSelectionListener(final TableViewTopComponent tableTopComponent,
                                  final TableView<ObservableList<String>> tableView,
                                  final Map<ObservableList<String>, Integer> rowToElementIdIndex) {
        this.tableTopComponent = tableTopComponent;
        this.tableView = tableView;
        this.rowToElementIdIndex = rowToElementIdIndex;
    }
    
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

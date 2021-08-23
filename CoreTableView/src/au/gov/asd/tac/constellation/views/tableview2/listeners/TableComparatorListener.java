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
import au.gov.asd.tac.constellation.views.tableview2.service.PreferenceService;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

/**
 *
 * @author formalhaunt
 */
public class TableComparatorListener implements ChangeListener<Comparator<? super ObservableList<String>>> {
    
    private final TableViewPane tablePane;
    private final TableService tableService;
    private final PreferenceService preferenceService;
    
    public TableComparatorListener(final TableViewPane tablePane,
                                   final TableService tableService,
                                   final PreferenceService preferenceService) {
        this.tablePane = tablePane;
        this.tableService = tableService;
        this.preferenceService = preferenceService;
    }
    
    @Override
    public void changed(final ObservableValue<? extends Comparator<? super ObservableList<String>>> observable,
                        final Comparator<? super ObservableList<String>> oldValue,
                        final Comparator<? super ObservableList<String>> newValue) {
        if (!tableService.isSortingListenerActive()) {
            tableService.setSortingListenerActive(true);
            tableService.updatePagination(preferenceService.getMaxRowsPerPage());
            Platform.runLater(() -> {
                tablePane.setCenter(tableService.getPagination());
            });
            tableService.setSortingListenerActive(false);
        }
    }
    
}

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
package au.gov.asd.tac.constellation.views.analyticview.visualisation;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.analyticview.export.AnalyticExportResultsMenu;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticData;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult.ResultListener;
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractTableTranslator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author cygnus_x-1
 * @param <C>
 */
public class TableVisualisation<C extends AnalyticData> extends InternalVisualisation implements ResultListener<C> {

    private final AbstractTableTranslator<? extends AnalyticResult<?>, C> translator;
    private VBox visualisation;
    private final TextField tableFilter;
    private final TableView<C> table;
    private final Map<String, TableColumn<C, Object>> tableColumns = new HashMap<>();
    private ListChangeListener<C> currentListener = null;

    public TableVisualisation(final AbstractTableTranslator<? extends AnalyticResult<?>, C> translator) {
        this.translator = translator;
        this.table = new TableView<>();
        this.visualisation = new VBox();

        this.tableFilter = new TextField();
        tableFilter.setPromptText("Type here to filter results: ");
        tableFilter.setStyle("-fx-prompt-text-fill: gray;");

        final AnalyticExportResultsMenu menu = new AnalyticExportResultsMenu(table);
        menu.init();
        final HBox optionsPanel = new HBox();
        optionsPanel.getChildren().addAll(tableFilter, menu.getExportButton());
        HBox.setHgrow(tableFilter, Priority.ALWAYS);

        table.setPlaceholder(new Label("No results"));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setId("table-visualisation");
        table.setPadding(new Insets(5));

        visualisation.getChildren().addAll(optionsPanel, table);
    }

    public void addColumn(final String columnName, final int percentWidth) {
        final TableColumn<C, Object> column = new TableColumn<>(columnName);
        tableColumns.put(columnName, column);

        column.prefWidthProperty().bind(table.widthProperty().multiply(percentWidth / 100.0));

        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(translator.getCellData(cellData.getValue(), columnName)));

        column.setCellFactory(columnData -> new TableCell<C, Object>() {
            @Override
            public void updateItem(final Object item, final boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(translator.getCellText(this.getTableRow().getItem(), item, columnName));
                    final ConstellationColor color = translator.getCellColor(this.getTableRow().getItem(), item, columnName);
                    this.setBackground(new Background(new BackgroundFill(color.getJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        });

        column.setSortable(true);
        table.getColumns().add(column);
    }

    public void populateTable(final List<C> items) {
        final ObservableList<C> tableData = FXCollections.observableArrayList(items);

        final FilteredList<C> filteredData = new FilteredList<>(tableData, predicate -> true);
        filteredData.addListener((Change<? extends C> change) -> table.refresh());
        tableFilter.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(item -> {
            if (StringUtils.isBlank(newValue)) {
                return true;
            }

            final String lowerCaseFilter = newValue.toLowerCase();
            return item.getIdentifier().toLowerCase().contains(lowerCaseFilter);
        }));

        final SortedList<C> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.setItems(sortedData);
    }

    public List<C> getSelectedItems() {
        return table.getSelectionModel().getSelectedItems();
    }

    public void setSelectionModelListener(final ListChangeListener<C> listener) {
        if (currentListener != null) {
            table.getSelectionModel().getSelectedItems().removeListener(currentListener);
        }
        if (listener != null) {
            table.getSelectionModel().getSelectedItems().addListener(listener);
        }
        currentListener = listener;
    }

    @Override
    public String getName() {
        return "Table";
    }

    @Override
    public Node getVisualisation() {
        return visualisation;
    }
    
    @Override
    public void setVisualisation(final Node visualisation) {
        this.visualisation = (VBox) visualisation;
    }

    @Override
    public void resultChanged(final List<C> selectedItems, final List<C> ignoredItems) {
        // remove the selection change listener
        final ListChangeListener<C> listener = currentListener;
        setSelectionModelListener(null);

        // add items from the ignored list which are currently selected
        final int[] selectionIndices = new int[selectedItems.size() + ignoredItems.size()];
        if (!ignoredItems.isEmpty()) {
            final List<C> currentSelection = table.getSelectionModel().getSelectedItems();
            ignoredItems.forEach(item -> {
                if (currentSelection.contains(item)) {
                    selectionIndices[selectedItems.size() + ignoredItems.indexOf(item)] = table.getItems().indexOf(item);
                }
            });
        }

        // add all items from the selected list
        if (!selectedItems.isEmpty()) {
            selectedItems.forEach(item -> selectionIndices[selectedItems.indexOf(item)] = table.getItems().indexOf(item));
        }

        // clear the table selection and then make the new selection
        table.getSelectionModel().clearSelection();
        if (selectionIndices.length > 0) {
            table.getSelectionModel().selectIndices(selectionIndices[0], selectionIndices);
        }

        // add the selection change listener back
        setSelectionModelListener(listener);
    }
    
    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        } else if (this == object || getClass() == object.getClass()) {
            return true;
        }
        return false;
    }
    
    @Override 
    public int hashCode() {
        return Objects.hash(this.getClass());
    }    
}

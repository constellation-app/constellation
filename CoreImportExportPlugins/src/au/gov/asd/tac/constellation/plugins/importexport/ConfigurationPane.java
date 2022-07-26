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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.plugins.importexport.model.TableRow;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * The ConfigurationPane is a UI element that displays a sample of the imported
 * data and allows the user to assign graph attributes to columns in the data.
 *
 * @author sirius
 */
public class ConfigurationPane extends AnchorPane {

    private static final double TAB_ANCHOR_POS = 5.0;
    private static final double RUN_BUTTON_ANCHOR_POS = 10.0;
    private static final int DOUBLE_CLICK_AMT = 2;

    private static final Image ADD_IMAGE = UserInterfaceIconProvider.ADD.buildImage(16, Color.BLACK);

    protected final ImportController importController;
    protected final TabPane tabPane;
    private final String helpText;


    public ConfigurationPane(final ImportController importController, final String helpText) {
        this.importController = importController;
        this.helpText = helpText;

        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        setMinSize(0, 0);

        // Add the tab pane that will hold a tab for each run
        tabPane = new TabPane();
        tabPane.setMaxHeight(Double.MAX_VALUE);
        tabPane.setSide(Side.TOP);
        AnchorPane.setTopAnchor(tabPane, TAB_ANCHOR_POS);
        AnchorPane.setLeftAnchor(tabPane, TAB_ANCHOR_POS);
        AnchorPane.setRightAnchor(tabPane, TAB_ANCHOR_POS);
        AnchorPane.setBottomAnchor(tabPane, TAB_ANCHOR_POS);
        getChildren().add(tabPane);

        // Create a button to allow the user to add a new tab (RunPane).
        Button newRunButton = new Button("", new ImageView(ADD_IMAGE));
        newRunButton.setOnAction(event -> importController.createNewRun());
        AnchorPane.setTopAnchor(newRunButton, RUN_BUTTON_ANCHOR_POS);
        AnchorPane.setRightAnchor(newRunButton, RUN_BUTTON_ANCHOR_POS);
        getChildren().add(newRunButton);

        ImportSingleton.getDefault().getClearDataFlag().addListener((observable, oldData, newData) -> clearSelectedPane());

        // Add a single run to start with
        createTab();
    }

    protected final Tab createTab() {

        // Create a unique label for the new tab
        int runNumber = 0;
        boolean unique;
        do {
            runNumber++;
            final String label = "Run " + runNumber;
            unique = true;
            for (final Tab tab : tabPane.getTabs()) {
                final Label tabLabel = (Label) tab.getGraphic();
                if (label.equals(tabLabel.getText())) {
                    unique = false;
                    break;
                }
            }
        } while (!unique);
        final Label label = new Label("Run " + runNumber);

        final Tab tab = new Tab();
        tab.setGraphic(label);

        tab.setOnClosed(event -> importController.updateDisplayedAttributes());

        label.setOnMouseClicked(event -> labelClickEvent(tab, label, event));

        // Add the tab
        tabPane.getTabs().add(tab);

        // Bring the new tab to the front
        tabPane.getSelectionModel().select(tab);

        // Create the run pane - store the name of the associated configuration pane tab
        final RunPane runPane = new RunPane(importController, helpText, label.getText());
        
        tab.setContent(runPane);

        return tab;
    }

    private void labelClickEvent(final Tab tab, final Label label, final MouseEvent event) {
        if (event.getClickCount() == DOUBLE_CLICK_AMT) {
            final TextField field = new TextField(label.getText());
            field.setOnAction(e -> {
                label.setText(field.getText());
                tab.setGraphic(label);
            });
            field.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue,
                    Boolean newValue) -> {
                if (!newValue) {
                    label.setText(field.getText());
                    tab.setGraphic(label);
                    
                    // Ensure runPane is updated to store the updated name (corresponding to the configuration pane tab
                    // name) which is used when generating summary details to user.
                    final RunPane runPane = (RunPane) tab.getContent();
                    runPane.setPaneName(label.getText());
                }
            });
            tab.setGraphic(field);
            field.selectAll();
            field.requestFocus();
        }
    }

    public void createNewRun(final Map<String, Attribute> vertexAttributes,
            final Map<String, Attribute> transactionAttributes, final Set<Integer> keys,
            final String[] columns, final List<String[]> data) {
        final Tab tab = createTab();
        RunPane runPane = (RunPane) tab.getContent();
        runPane.requestLayout();

        Platform.runLater(() -> {
            RunPane runPane1 = (RunPane) tab.getContent();
            runPane1.setDisplayedAttributes(vertexAttributes, transactionAttributes, keys);
            runPane1.setSampleData(columns, createTableRows(data));
        });
    }

    /**
     * Set the configuration pane to display the specified column headers and
     * sample data rows.
     *
     * @param columnLabels Column header labels.
     * @param currentData Rows of sample data.
     */
    public void setSampleData(final String[] columnLabels, final List<String[]> currentData) {
        tabPane.getTabs().stream().map(tab -> (RunPane) tab.getContent()).forEachOrdered(runPane -> {
            runPane.setSampleData(columnLabels, createTableRows(currentData));
            runPane.refreshDataView();
        });
    }

    /**
     * Clears the run pane that is currently selected
     */
    protected void clearSelectedPane() {
        if (tabPane.getSelectionModel().getSelectedItem() != null) {
            final RunPane currentSelected = (RunPane) tabPane.getSelectionModel().getSelectedItem().getContent();

            final String[] columns = {};

            currentSelected.setSampleData(columns, createTableRows(FXCollections.observableArrayList()));
        }
    }

    private static ObservableList<TableRow> createTableRows(final List<String[]> data) {
        final ObservableList<TableRow> rows = FXCollections.observableArrayList();
        final int rowCount = Math.min(101, data.size());
        for (int row = 0; row < rowCount; row++) {
            rows.add(new TableRow(row, data.get(row)));
        }
        return rows;
    }

    /**
     * A List&lt;ImportDefinition&gt; where each list element corresponds to a
     * RunPane tab.
     *
     * @return A List&lt;ImportDefinition&gt; where each list element
     * corresponds to a RunPane tab.
     */
    public List<ImportDefinition> createDefinitions(final boolean isFilesIncludeHeadersEnabled) {
        List<ImportDefinition> definitions = new ArrayList<>(tabPane.getTabs().size());

        for (Tab tab : tabPane.getTabs()) {
            RunPane runPane = (RunPane) tab.getContent();
            definitions.add(runPane.createDefinition(isFilesIncludeHeadersEnabled ? 1 : 0));
        }

        return Collections.unmodifiableList(definitions);
    }

    public void deleteAttribute(final Attribute attribute) {
        for (final Tab tab : tabPane.getTabs()) {
            final RunPane runPane = (RunPane) tab.getContent();
            runPane.deleteAttribute(attribute);
        }
    }

    public void setDisplayedAttributes(final Map<String, Attribute> vertexAttributes,
            final Map<String, Attribute> transactionAttributes, final Set<Integer> keys) {
        for (final Tab tab : tabPane.getTabs()) {
            final RunPane runPane = (RunPane) tab.getContent();
            runPane.setDisplayedAttributes(vertexAttributes, transactionAttributes, keys);
            runPane.setAttributePaneHeight();
        }
    }

    /**
     * Returns a combined collection of all attributes that have been allocated
     * to a column in any run.
     *
     * @return a combined collection of all attributes that have been allocated
     * to a column in any run.
     */
    public Collection<Attribute> getAllocatedAttributes() {
        final List<Attribute> allocatedAttributes = new ArrayList<>();
        for (final Tab tab : tabPane.getTabs()) {
            final RunPane runPane = (RunPane) tab.getContent();
            allocatedAttributes.addAll(runPane.getAllocatedAttributes());
        }

        return allocatedAttributes;
    }

    void update(final List<ImportDefinition> definitions) {
        // First create a new RunPane for each ImportDefinition...
        // (This tends to involve Platform.runLater() so let them be queued.)
        tabPane.getTabs().clear();

        definitions.forEach(_item -> importController.createNewRun());

        // ...then configure each RunPane.
        // (This will queue waiting for the RunPane creations.)
        Platform.runLater(() -> {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            for (int ix = 0; ix < definitions.size(); ix++) {
                final ImportDefinition id = definitions.get(ix);
                final RunPane runPane = (RunPane) tabs.get(ix).getContent();

                runPane.update(id);
            }
        });
    }

    public void clearFilters() {
        tabPane.getTabs().stream().map(tab -> (RunPane) tab.getContent()).forEachOrdered(runPane -> runPane.clearFilters());
    }

    /**
     * Check whether the configuration pane has queried data.
     */
    public boolean hasDataQueried() {
        return tabPane.getTabs().stream().map(tab -> (RunPane) tab.getContent()).anyMatch(runPane -> runPane.hasDataQueried());
    }
}

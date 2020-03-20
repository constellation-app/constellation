/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.TableRow;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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

    private final ImportController importController;
    private final TabPane tabPane;

    private static final Image ADD_IMAGE = UserInterfaceIconProvider.ADD.buildImage(16, Color.BLACK);

    public ConfigurationPane(final ImportController importController) {
        this.importController = importController;

        setMaxHeight(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        setMinSize(0, 0);

        // Add the tab pane that will hold a tab for each run
        tabPane = new TabPane();
        tabPane.setMaxHeight(Double.MAX_VALUE);
        tabPane.setSide(Side.TOP);
        AnchorPane.setTopAnchor(tabPane, 5.0);
        AnchorPane.setLeftAnchor(tabPane, 5.0);
        AnchorPane.setRightAnchor(tabPane, 5.0);
        AnchorPane.setBottomAnchor(tabPane, 5.0);
        getChildren().add(tabPane);

        // Create a button to allow the user to add a new tab (RunPane).
        Button newRunButton = new Button("", new ImageView(ADD_IMAGE));
        newRunButton.setOnAction((ActionEvent event) -> {
            importController.createNewRun();
        });
        AnchorPane.setTopAnchor(newRunButton, 10.0);
        AnchorPane.setRightAnchor(newRunButton, 10.0);
        getChildren().add(newRunButton);

        // Add a single run to start with
        createTab();
    }

    private Tab createTab() {

        // Create a unique label for the new tab
        int runNumber = 0;
        boolean unique;
        do {
            String label = "Run " + ++runNumber;
            unique = true;
            for (Tab tab : tabPane.getTabs()) {
                Label tabLabel = (Label) tab.getGraphic();
                if (label.equals(tabLabel.getText())) {
                    unique = false;
                    break;
                }
            }
        } while (!unique);
        final Label label = new Label("Run " + runNumber);

        final Tab tab = new Tab();
        tab.setGraphic(label);

        tab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                importController.updateDisplayedAttributes();
            }
        });

        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    final TextField field = new TextField(label.getText());
                    field.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            label.setText(field.getText());
                            tab.setGraphic(label);
                        }
                    });
                    field.focusedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                            if (!newValue) {
                                label.setText(field.getText());
                                tab.setGraphic(label);
                            }
                        }
                    });
                    tab.setGraphic(field);
                    field.selectAll();
                    field.requestFocus();
                }
            }
        });

        // Add the tab
        tabPane.getTabs().add(tab);

        // Bring the new tab to the front
        tabPane.getSelectionModel().select(tab);

        // Create the run pane
        RunPane runPane = new RunPane(importController);

        tab.setContent(runPane);

        return tab;
    }

    public void createNewRun(final Map<String, Attribute> vertexAttributes, final Map<String, Attribute> transactionAttributes, final Set<Integer> keys, final String[] columns, final List<String[]> data) {
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
        for (final Tab tab : tabPane.getTabs()) {
            final RunPane runPane = (RunPane) tab.getContent();
            runPane.setSampleData(columnLabels, createTableRows(currentData));
        }
    }

    private static ObservableList<TableRow> createTableRows(List<String[]> data) {
        ObservableList<TableRow> rows = FXCollections.observableArrayList();
        final int rowCount = Math.min(101, data.size());
        for (int row = 1; row < rowCount; row++) {
            rows.add(new TableRow(row - 1, data.get(row)));
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
    public List<ImportDefinition> createDefinitions() {
        List<ImportDefinition> definitions = new ArrayList<>(tabPane.getTabs().size());

        for (Tab tab : tabPane.getTabs()) {
            RunPane runPane = (RunPane) tab.getContent();
            definitions.add(runPane.createDefinition());
        }

        return Collections.unmodifiableList(definitions);
    }

    public void deleteAttribute(Attribute attribute) {
        for (Tab tab : tabPane.getTabs()) {
            RunPane runPane = (RunPane) tab.getContent();
            runPane.deleteAttribute(attribute);
        }
    }

    public void setDisplayedAttributes(Map<String, Attribute> vertexAttributes, Map<String, Attribute> transactionAttributes, Set<Integer> keys) {
        for (Tab tab : tabPane.getTabs()) {
            RunPane runPane = (RunPane) tab.getContent();
            runPane.setDisplayedAttributes(vertexAttributes, transactionAttributes, keys);
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
        List<Attribute> allocatedAttributes = new ArrayList<>();
        for (Tab tab : tabPane.getTabs()) {
            RunPane runPane = (RunPane) tab.getContent();
            allocatedAttributes.addAll(runPane.getAllocatedAttributes());
        }

        return allocatedAttributes;
    }

    void update(final List<ImportDefinition> definitions) {
        // First create a new RunPane for each ImportDefinition...
        // (This tends to involve Platform.runLater() so let them be queued.)
        tabPane.getTabs().clear();
        for (final ImportDefinition impdef : definitions) {
            importController.createNewRun();
        }

        // ...then configure each RunPane.
        // (This will queue waiting for the RunPane creations.)
        Platform.runLater(() -> {
            final ObservableList<Tab> tabs = tabPane.getTabs();
            for (int ix = 0; ix < definitions.size(); ix++) {
                final ImportDefinition id = definitions.get(ix);
                final RunPane runPane = (RunPane) tabs.get(ix).getContent();

                runPane.update(id, importController.getKeys());
            }
        });
    }
}

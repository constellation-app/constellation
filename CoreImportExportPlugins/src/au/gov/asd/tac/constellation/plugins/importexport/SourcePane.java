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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_PREF_SIZE;

/**
 * The SourcePane provides the UI necessary to allow the user to specify where
 * the imported data should come from. This is typically done by selecting a
 * file.
 *
 * @author sirius
 */
public abstract class SourcePane extends GridPane {

    private static final int GRAPHCOMBO_COLUMN_INDEX = 1;
    private static final int GRAPHCOMBO_ROW_INDEX = 4;
    private static final int GRAPHCOMBO_COLUMN_SPAN = 2;
    private static final int GRAPHCOMBO_ROW_SPAN = 1;

    private static final int GAP = 10;
    private static final Insets GRIDPANE_PADDING = new Insets(5);

    protected final ComboBox<ImportDestination<?>> graphComboBox;
    protected final Pane parametersPane = new Pane();
    protected final ColumnConstraints column0Constraints = new ColumnConstraints();
    protected final ColumnConstraints column1Constraints = new ColumnConstraints();
    protected final ColumnConstraints column2Constraints = new ColumnConstraints();
    protected final ImportController importController;

    protected SourcePane(final ImportController importController) {
        this.importController = importController;
        graphComboBox = new ComboBox<>();
        updateDestinationGraphCombo();
        GridPane.setConstraints(graphComboBox, GRAPHCOMBO_COLUMN_INDEX, GRAPHCOMBO_ROW_INDEX, GRAPHCOMBO_COLUMN_SPAN,
                GRAPHCOMBO_ROW_SPAN, HPos.LEFT, VPos.TOP);

        setMinHeight(USE_PREF_SIZE);
        setMinWidth(0);
        setPadding(GRIDPANE_PADDING);
        setHgap(GAP);
        setVgap(GAP);

        column0Constraints.setHgrow(Priority.NEVER);
        column1Constraints.setHgrow(Priority.ALWAYS);
        column1Constraints.setMinWidth(0);
        column2Constraints.setHgrow(Priority.NEVER);
        getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
    }

    public abstract void update(final ImportController importController);

    public void setParameters(final PluginParameters parameters) {
        parametersPane.getChildren().clear();
        if (parameters != null) {
            final PluginParametersPane pluginParametersPane = PluginParametersPane.buildPane(parameters, null);
            parametersPane.getChildren().add(pluginParametersPane);
        }
    }

    /**
     * Return the selected destination
     *
     * @return ImportDestination destination
     */
    public final <D> ImportDestination<D> getDestination() {
        return (ImportDestination<D>) graphComboBox.getSelectionModel().getSelectedItem();
    }

    /**
     * Helper method to update the destination graph combo box. Also used for
     * value loading when this pane is being initialized.
     */
    public final void updateDestinationGraphCombo() {
        // previousDestinationObject is the previously selected item in the combobox
        final ImportDestination<?> previousDestinationObject = graphComboBox.getSelectionModel().getSelectedItem();
        final ObservableList<ImportDestination<?>> destinations = FXCollections.observableArrayList();

        final Map<String, Graph> graphs = GraphManager.getDefault().getAllGraphs();
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        ImportDestination<?> defaultDestination = null;
        for (final Graph graph : graphs.values()) {
            final GraphDestination destination = new GraphDestination(graph);
            destinations.add(destination);
            if (graph == activeGraph) {
                defaultDestination = destination;
            }
        }

        final Map<String, SchemaFactory> schemaFactories = SchemaFactoryUtilities.getSchemaFactories();
        for (final SchemaFactory schemaFactory : schemaFactories.values()) {
            final SchemaDestination destination = new SchemaDestination(schemaFactory);
            destinations.add(destination);
            if (defaultDestination == null) {
                defaultDestination = destination;
            }
        }
        // resets the combo box when set correctly.
        graphComboBox.setItems(destinations);
        graphComboBox.setOnAction((final ActionEvent t) -> importController.setDestination(graphComboBox.getSelectionModel().getSelectedItem()));
        // Select null triggers the combobox to update to the correct value for
        // some unknown reason. Removal will mean that the combobox will
        // not keep it's state when a graph event occurs
        // ClearSelection() did not work to fix this.
        graphComboBox.getSelectionModel().select(null);
        importController.setDestination(previousDestinationObject != null ? previousDestinationObject
                : defaultDestination);
    }
}

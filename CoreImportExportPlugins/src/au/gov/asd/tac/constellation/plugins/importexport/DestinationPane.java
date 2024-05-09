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
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

/**
 * The DestinationPane displays the UI that allows the user to specify where the
 * imported graph elements should be placed. At present, there are 2 options:
 * into a currently existing graph, or into a new graph based on a specified
 * schema.
 *
 * @author sirius
 */
public class DestinationPane extends GridPane {

    private static final Insets GRIDPANE_PADDING = new Insets(5);
    private static final int GAP = 10;

    private final ImportController importController;

    public DestinationPane(final ImportController importController) {
        this.importController = importController;

        setMaxWidth(Double.MAX_VALUE);

        setPadding(GRIDPANE_PADDING);
        setHgap(GAP);
        setVgap(GAP);

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

        final ComboBox<ImportDestination<?>> graphComboBox = new ComboBox<>();
        graphComboBox.setItems(destinations);
        graphComboBox.setOnAction((final ActionEvent t)
                -> DestinationPane.this.importController.setDestination(graphComboBox.getSelectionModel().getSelectedItem()));
        graphComboBox.getSelectionModel().select(defaultDestination);
        GridPane.setConstraints(graphComboBox, 0, 0);

        getChildren().addAll(graphComboBox);
    }
}

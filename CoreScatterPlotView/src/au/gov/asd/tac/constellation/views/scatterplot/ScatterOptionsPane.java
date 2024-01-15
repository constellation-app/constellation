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
package au.gov.asd.tac.constellation.views.scatterplot;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.scatterplot.axis.AxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.axis.CategoryAxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.axis.LogarithmicAxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.axis.NumberAxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotStateWriter;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * The GUI component of the Scatter Plot displaying the options which determine
 * what the ScatterChartPane will display.
 *
 * @author cygnus_x-1
 * @author antares
 */
public class ScatterOptionsPane extends BorderPane {

    protected static final Map<String, AxisBuilder<?>> VALID_TYPES_X = new HashMap<>();
    protected static final Map<String, AxisBuilder<?>> VALID_TYPES_Y = new HashMap<>();
    protected static final AxisBuilder<?> DEFAULT_AXIS_BUILDER = new CategoryAxisBuilder();
    private static final String[] NUMBER_TYPE_STRINGS = {"float", "float_or_null", "integer", "integer_or_null", "long", "long_or_null"};

    static {
        for (final AxisBuilder<?> axisBuilder : Lookup.getDefault().lookupAll(AxisBuilder.class)) {
            axisBuilder.getTypes().forEach(type -> {
                VALID_TYPES_X.put(type, axisBuilder);
                VALID_TYPES_Y.put(type, axisBuilder);
            });
        }
    }

    private final ScatterPlotPane scatterPlot;
    private final FlowPane optionsPane;
    private final ToolBar optionsToolBar;
    private final ChoiceBox<String> elementTypeComboBox;
    private final ComboBox<Attribute> xAttributeComboBox;
    private final ComboBox<Attribute> yAttributeComboBox;
    private final ToggleButton selectedOnlyButton;
    private final ToggleButton logarithmicAxisX;
    private final ToggleButton logarithmicAxisY;
    private final Button helpButton;

    private Callback<ListView<Attribute>, ListCell<Attribute>> cellFactory;
    private boolean isUpdating = false;

    public ScatterOptionsPane(ScatterPlotPane parent) {
        this.scatterPlot = parent;

        this.cellFactory = (ListView<Attribute> p) -> new ListCell<Attribute>() {
            @Override
            public void updateItem(Attribute item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setText(item.getName());
                }
            }
        };

        this.elementTypeComboBox = new ChoiceBox<>();
        elementTypeComboBox.getItems().addAll(GraphElementType.VERTEX.getShortLabel(), GraphElementType.TRANSACTION.getShortLabel());
        elementTypeComboBox.getSelectionModel().select(GraphElementType.VERTEX.getShortLabel());
        elementTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating) {
                return;
            }

            try {
                ScatterPlotState state = new ScatterPlotState(scatterPlot.getScatterPlot().getState());

                if (newValue.equals(GraphElementType.VERTEX.getShortLabel())) {
                    state.setElementType(GraphElementType.VERTEX);
                } else if (newValue.equals(GraphElementType.TRANSACTION.getShortLabel())) {
                    state.setElementType(GraphElementType.TRANSACTION);
                } else {
                    ScatterPlotErrorDialog.create("Invalid element type!");
                }

                state.setXAttribute(null);
                state.setYAttribute(null);

                PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
            } catch (InterruptedException e) {
                ScatterPlotErrorDialog.create("Error updating element type: " + e.getMessage());
            }
        });

        this.xAttributeComboBox = new ComboBox<>();
        xAttributeComboBox.setCellFactory(cellFactory);
        xAttributeComboBox.setButtonCell(cellFactory.call(null));
        xAttributeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating) {
                return;
            }

            try {
                ScatterPlotState state = new ScatterPlotState(scatterPlot.getScatterPlot().getState());
                state.setXAttribute(newValue);
                PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
            } catch (InterruptedException e) {
                ScatterPlotErrorDialog.create("Error updating x attribute: " + e.getMessage());
            }
        });

        this.yAttributeComboBox = new ComboBox<>();
        yAttributeComboBox.setCellFactory(cellFactory);
        yAttributeComboBox.setButtonCell(cellFactory.call(null));
        yAttributeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating) {
                return;
            }

            try {
                ScatterPlotState state = new ScatterPlotState(scatterPlot.getScatterPlot().getState());
                state.setYAttribute(newValue);
                PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
            } catch (InterruptedException e) {
                ScatterPlotErrorDialog.create("Error updating y attribute: " + e.getMessage());
            }
        });

        this.selectedOnlyButton = new ToggleButton("Selected Only");
        selectedOnlyButton.selectedProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (isUpdating) {
                        return;
                    }

                    try {
                        ScatterPlotState state = new ScatterPlotState(scatterPlot.getScatterPlot().getState());

                        state.setSelectedOnly(newValue);

                        PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
                    } catch (InterruptedException e) {
                        ScatterPlotErrorDialog.create("Error updating selected only: " + e.getMessage());
                    }
                });

        this.logarithmicAxisX = new ToggleButton("Log Scale X-Axis");
        logarithmicAxisX.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating) {
                return;
            }

            if (newValue) {
                for (String numberType : NUMBER_TYPE_STRINGS) {
                    VALID_TYPES_X.put(numberType, new LogarithmicAxisBuilder());
                }
            } else { // oldValue will be true
                for (String numberType : NUMBER_TYPE_STRINGS) {
                    VALID_TYPES_X.put(numberType, new NumberAxisBuilder());
                }
            }

            try {
                final ScatterPlotState state = scatterPlot.getScatterPlot().getState();

                PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
            } catch (InterruptedException e) {
                ScatterPlotErrorDialog.create("Error updating number axis scale change: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });

        this.logarithmicAxisY = new ToggleButton("Log Scale Y-Axis");
        logarithmicAxisY.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdating) {
                return;
            }

            if (newValue) {
                for (String numberType : NUMBER_TYPE_STRINGS) {
                    VALID_TYPES_Y.put(numberType, new LogarithmicAxisBuilder());
                }
            } else { // oldValue will be true
                for (String numberType : NUMBER_TYPE_STRINGS) {
                    VALID_TYPES_Y.put(numberType, new NumberAxisBuilder());
                }
            }

            try {
                final ScatterPlotState state = scatterPlot.getScatterPlot().getState();

                PluginExecution.withPlugin(new ScatterPlotStateWriter(state)).executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
            } catch (InterruptedException e) {
                ScatterPlotErrorDialog.create("Error updating number axis scale change: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });

        final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
        helpButton = new Button("", helpImage);
        helpButton.setOnAction(event
                -> new HelpCtx(this.getClass().getPackage().getName()).display());
        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");

        this.optionsToolBar = new ToolBar();
        optionsToolBar.getItems().addAll(elementTypeComboBox, xAttributeComboBox, yAttributeComboBox, selectedOnlyButton, logarithmicAxisX, logarithmicAxisY, helpButton);

        this.optionsPane = new FlowPane();
        optionsPane.setAlignment(Pos.CENTER);
        optionsPane.setHgap(10);
        optionsPane.getChildren().add(optionsToolBar);

        this.setId("scatter-options-pane");
        this.setPadding(new Insets(5));
        this.setCenter(optionsToolBar);
    }

    /**
     * Refresh the state of the ScatterOptionsPane based on the given
     * ScatterPlotState.
     *
     * @param state the ScatterPlotState.
     */
    protected void refreshOptions(ScatterPlotState state) {
        if (scatterPlot.getScatterPlot().getCurrentGraph() == null || state == null) {
            return;
        }

        Platform.runLater(() -> {
            isUpdating = true;

            final ObservableList<Attribute> attributes = FXCollections.observableArrayList();

            ReadableGraph readableGraph = scatterPlot.getScatterPlot().getCurrentGraph().getReadableGraph();
            try {
                final int attributeCount = readableGraph.getAttributeCount(state.getElementType());
                for (int attributePosition = 0; attributePosition < attributeCount; attributePosition++) {
                    final int attributeId = readableGraph.getAttribute(state.getElementType(), attributePosition);
                    final Attribute attribute = new GraphAttribute(readableGraph, attributeId);
                    attributes.add(attribute);
                }
            } finally {
                readableGraph.release();
            }

            FXCollections.sort(attributes, (attribute1, attribute2) -> attribute1.getName().compareTo(attribute2.getName()));

            xAttributeComboBox.setItems(attributes);
            yAttributeComboBox.setItems(attributes);

            elementTypeComboBox.getSelectionModel().select(state.getElementType().getShortLabel());
            xAttributeComboBox.getSelectionModel().select(state.getXAttribute());
            yAttributeComboBox.getSelectionModel().select(state.getYAttribute());
            selectedOnlyButton.setSelected(state.isSelectedOnly());

            isUpdating = false;
        });
    }

    /**
     * Reset the state of the ScatterOptionsPane based on the given
     * ScatterPlotState.
     */
    protected void resetOptions() {
        Platform.runLater(() -> {
            elementTypeComboBox.getSelectionModel().select(GraphElementType.VERTEX.getShortLabel());
            xAttributeComboBox.getItems().clear();
            yAttributeComboBox.getItems().clear();
            selectedOnlyButton.setSelected(false);
        });
    }

    /**
     * Disable all options within this ScatterOptionsPane.
     */
    protected void disableOptions() {
        Platform.runLater(() -> {
            optionsToolBar.setDisable(true);
            optionsPane.setDisable(true);
        });
    }

    /**
     * Enable all options within this ScatterOptionsPane.
     */
    protected void enableOptions() {
        Platform.runLater(() -> {
            optionsToolBar.setDisable(false);
            optionsPane.setDisable(false);
        });
    }
}

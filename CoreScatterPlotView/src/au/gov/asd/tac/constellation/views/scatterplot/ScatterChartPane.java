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
package au.gov.asd.tac.constellation.views.scatterplot;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectionMode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * The GUI component of the Scatter Plot displaying the actual scatter plot
 * chart.
 *
 * @author cygnus_x-1
 * @author antares
 */
public class ScatterChartPane extends BorderPane {

    private static final String SCATTER_PLOT_THREAD_NAME = "Scatter Plot";
    private final ScatterPlotPane scatterPlot;
    private ScatterChart<?, ?> scatterChart;
    private final Rectangle selection;
    private final Tooltip tooltip;
    private final Set<ScatterData> currentData;
    private final Set<ScatterData> currentSelectedData;
    private double selectionXOrigin = 0.0;
    private double selectionYOrigin = 0.0;
    private double mouseDistanceFromXOrigin = 0.0;
    private double mouseDistanceFromYOrigin = 0.0;
    private boolean isSelecting = false;

    /**
     * Handle all mouse events on the scatter plot chart.
     */
    private final EventHandler<Event> mouseHandler = new EventHandler<Event>() {
        @Override
        public void handle(final Event t) {
            if (t instanceof MouseEvent me) {
                final double mouseX = me.getX();
                final double mouseY = me.getY();

                // set constants
                final double bufferWidth = 5;
                final double bufferHeight = 5;

                // Mouse cursor enters chart
                if (!me.isPrimaryButtonDown() && me.getEventType().equals(MouseEvent.MOUSE_ENTERED)) {
                    scatterChart.setCursor(Cursor.CROSSHAIR);
                
                // Mouse moved to new position within chart
                } else if (!me.isPrimaryButtonDown() && me.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                    // create buffer around mouse position
                    selectionXOrigin = mouseX;
                    selectionYOrigin = mouseY;
                    selection.setVisible(false);
                    selection.setLayoutX(selectionXOrigin - bufferWidth / 2);
                    selection.setLayoutY(selectionYOrigin - bufferHeight / 2);
                    selection.setWidth(bufferWidth);
                    selection.setHeight(bufferHeight);

                    // select all data within buffer
                    final Set<ScatterData> selectedData = getSelectionFromChart(selection.getBoundsInLocal());
                    if (!selectedData.isEmpty()) {
                        selectElementsOnChart(currentSelectedData, selectedData);

                        // build tooltip text
                        final StringBuilder tooltipText = new StringBuilder();
                        for (final ScatterData data : selectedData) {
                            tooltipText.append(data.getElementLabel());
                            tooltipText.append(SeparatorConstants.NEWLINE);
                        }
                        tooltip.setText(tooltipText.toString());

                        // add tooltip
                        Tooltip.install(scatterChart, tooltip);
                    } else {
                        selectElementsOnChart(currentSelectedData, null);

                        // remove tooltip
                        Tooltip.uninstall(scatterChart, tooltip);
                    }
                } else if (me.isPrimaryButtonDown() && me.getEventType().equals(MouseEvent.MOUSE_PRESSED)) { // Mouse primary button pressed, selection started
                    selectionXOrigin = mouseX;
                    selectionYOrigin = mouseY;
                    isSelecting = true;

                    // Show selection rectangle
                    selection.setLayoutX(selectionXOrigin);
                    selection.setLayoutY(selectionYOrigin);
                    selection.setWidth(0);
                    selection.setHeight(0);
                    selection.setVisible(true);
                } else if (me.isPrimaryButtonDown() && me.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) { // Mouse dragged with primary button pressed, selection continues
                    mouseDistanceFromXOrigin += selectionXOrigin - mouseX;
                    mouseDistanceFromYOrigin += selectionYOrigin - mouseY;

                    selection.setLayoutX(mouseDistanceFromXOrigin < 0 ? mouseX + mouseDistanceFromXOrigin : mouseX);
                    selection.setWidth(mouseDistanceFromXOrigin < 0 ? -mouseDistanceFromXOrigin : mouseDistanceFromXOrigin);
                    selection.setLayoutY(mouseDistanceFromYOrigin < 0 ? mouseY + mouseDistanceFromYOrigin : mouseY);
                    selection.setHeight(mouseDistanceFromYOrigin < 0 ? -mouseDistanceFromYOrigin : mouseDistanceFromYOrigin);

                    // Update variables based on current mouse pointer position
                    selectionXOrigin = mouseX;
                    selectionYOrigin = mouseY;
                }

                // Selection was made
                if (!me.isPrimaryButtonDown() && isSelecting) {
                    Set<ScatterData> selectedData = null;
                    SelectionMode selectionMode = SelectionMode.REPLACE;

                    // dragged box selection
                    if (selection.getWidth() >= 1 || selection.getHeight() >= 1) {
                        selectedData = getSelectionFromChart(selection.getBoundsInLocal());

                        if (me.isControlDown()) {
                            selectionMode = SelectionMode.INVERT;
                        }
                    } else {
                        // single click selection
                        if (me.getClickCount() == 1) {
                            selection.setLayoutX(selection.getLayoutX() - bufferWidth / 2);
                            selection.setLayoutY(selection.getLayoutY() - bufferHeight / 2);
                            selection.setWidth(bufferWidth);
                            selection.setHeight(bufferHeight);

                            selectedData = getSelectionFromChart(selection.getBoundsInLocal());

                            if (me.isControlDown()) {
                                selectionMode = SelectionMode.INVERT;
                            }
                        } else if (me.getClickCount() == 2) { // double click clear selection
                            selectedData = null;
                        }
                    }

                    // make selection
                    try {
                        selectElementsOnGraph(selectedData, selectionMode);
                    } catch (final InterruptedException e) {
                        ScatterPlotErrorDialog.create("Selection interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }

                    // Hide selection rectangle
                    selection.setVisible(false);
                    selection.setLayoutX(0);
                    selection.setLayoutY(0);
                    selection.setWidth(0);
                    selection.setHeight(0);

                    mouseDistanceFromXOrigin = 0.0;
                    mouseDistanceFromYOrigin = 0.0;
                    isSelecting = false;
                }

                // Consume the event so that no other listeners are interrupted:
                t.consume();
            }
        }
    };

    public ScatterChartPane(final ScatterPlotPane parent) {
        this.scatterPlot = parent;

        final LinearGradient gradient = new LinearGradient(0.0, 0.0, 0.0, 0.75, true, CycleMethod.NO_CYCLE,
                new Stop[]{new Stop(0, Color.LIGHTGRAY), new Stop(1, Color.GRAY.darker())});
        this.selection = new Rectangle(0, 0, 0, 0);
        selection.setFill(Color.WHITE);
        selection.setStroke(Color.BLACK);
        selection.setOpacity(0.4);
        selection.setMouseTransparent(true);
        selection.setStroke(Color.SILVER);
        selection.setStrokeWidth(2D);
        selection.setFill(gradient);
        selection.setSmooth(true);
        selection.setArcWidth(5.0);
        selection.setArcHeight(5.0);

        this.tooltip = new Tooltip();

        this.currentData = Collections.synchronizedSet(new HashSet<>());
        this.currentSelectedData = new HashSet<>();

        this.getChildren().addAll(selection);
        this.setId("scatter-chart-pane");
        this.setPadding(new Insets(5));
    }

    /**
     * Refresh the state of the ScatterChartPane based on the given
     * ScatterPlotState.
     *
     * @param state the ScatterPlotState.
     */
    protected void refreshChart(final ScatterPlotState state) {
        if (scatterPlot.getScatterPlot().getCurrentGraph() == null || state == null || state.getXAttribute() == null 
                || state.getYAttribute() == null) {
            resetChart();
            return;
        }

        Platform.runLater(() -> {
            final ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            scatterPlot.getChartPane().setCenter(progressIndicator);
        });

        final Future<?> pluginFuture = PluginExecution.withPlugin(new ScatterPlotRefresher(state))
                .executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
        final Thread waitingThread = new Thread(() -> {
            try {
                pluginFuture.get();
            } catch (final InterruptedException e) {
                ScatterPlotErrorDialog.create("Error refreshing scatter plot: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (final ExecutionException e) {
                ScatterPlotErrorDialog.create("Error refreshing scatter plot: " + e.getMessage());
            }

            Platform.runLater(() -> {
                scatterPlot.getChartPane().setCenter(scatterChart);
                selection.toFront();
            });
        });
        waitingThread.setName(SCATTER_PLOT_THREAD_NAME);
        waitingThread.start();
    }

    /**
     * Reset the state of the ScatterChartPane based on the given
     * ScatterPlotState.
     */
    protected void resetChart() {
        Platform.runLater(() -> {
            if (scatterChart != null) {
                scatterChart.getData().clear();
                scatterChart.getXAxis().setLabel(null);
                scatterChart.getYAxis().setLabel(null);
            }
        });
    }

    /**
     * Return the current selection from the ScatterChart within this
     * ScatterPlotPane.
     *
     * @param selectionBounds the selection will be based on these bounds.
     * @return the current selection from the ScatterChart within this
     * ScatterPlotPane.
     */
    protected Set<ScatterData> getSelectionFromChart(final Bounds selectionBounds) {
        final Set<ScatterData> selectedData = new HashSet<>();

        if (scatterPlot.getScatterPlot().getCurrentGraph() == null) {
            return selectedData;
        }

        synchronized (currentData) {
            for (final ScatterData data : currentData) {
                final Bounds nodeBounds = data.getData().getNode().getBoundsInLocal();
                if (selection.localToScene(selectionBounds).intersects(data.getData().getNode().localToScene(nodeBounds))) {
                    selectedData.add(data);
                }
            }
        }

        return selectedData;
    }

    /**
     * Return the current selection from the active graph.
     *
     * @param state the ScatterPlotState.
     * @return the current selection from the active graph.
     */
    protected Set<ScatterData> getSelectionFromGraph(final ScatterPlotState state) {
        final Set<ScatterData> selectedData = new HashSet<>();

        if (scatterPlot.getScatterPlot().getCurrentGraph() == null) {
            return selectedData;
        }

        try (final ReadableGraph readableGraph = scatterPlot.getScatterPlot().getCurrentGraph().getReadableGraph()) {
            final int selectedAttribute = readableGraph.getAttribute(state.getElementType(), "selected");
            final int elementCount = state.getElementType().getElementCount(readableGraph);
            for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                final int elementId = state.getElementType().getElement(readableGraph, elementPosition);
                if (readableGraph.getBooleanValue(selectedAttribute, elementId)) {
                    synchronized (currentData) {
                        for (final ScatterData data : currentData) {
                            if (data.getElementId() == elementId) {
                                selectedData.add(data);
                            }
                        }
                    }
                }
            }
        }

        return selectedData;
    }

    /**
     * Update the current selection on the ScatterChart within this
     * ScatterChartPane. This allows a primary selection (which will highlight
     * in red) as well as a secondary selection (which will highlight in
     * yellow).
     *
     * @param primarySelection the set of data objects representing the primary
     * selection.
     * @param secondarySelection the set of data objects representing the
     * secondary selection.
     */
    protected void selectElementsOnChart(final Set<ScatterData> primarySelection, final Set<ScatterData> secondarySelection) {
        final DropShadow primarySelectionShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 20.0, 0.85, 0.0, 0.0);
        final DropShadow secondarySelectionShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.RED, 20.0, 0.85, 0.0, 0.0);

        Platform.runLater(() -> {
            synchronized (currentData) {
                for (final ScatterData data : currentData) {
                    if (secondarySelection != null && secondarySelection.contains(data)) {
                        data.getData().getNode().setEffect(secondarySelectionShadow);
                        continue;
                    }

                    if (primarySelection != null && primarySelection.contains(data)) {
                        data.getData().getNode().setEffect(primarySelectionShadow);
                        continue;
                    }

                    @SuppressWarnings("unchecked") // getData will return data of type number and number
                    final Data<Number, Number> data2 = (Data<Number, Number>) data.getData();
                    final Node dataNode = data2.getNode();
                    dataNode.setEffect(null);
                }
            }
        });

        if (primarySelection != null && !currentSelectedData.equals(primarySelection)) {
            currentSelectedData.clear();
            currentSelectedData.addAll(primarySelection);
        }
    }

    /**
     * Update the current selection on the active graph.
     *
     * @param selectedData
     * @param selectionMode
     * @throws InterruptedException
     */
    private void selectElementsOnGraph(final Set<ScatterData> selectedData, final SelectionMode selectionMode) throws InterruptedException {
        if (scatterPlot.getScatterPlot().getCurrentGraph() == null) {
            return;
        }

        final ScatterPlotState state = scatterPlot.getScatterPlot().getState();

        final BitSet elementIds = new BitSet();
        if (selectedData != null) {
            for (final ScatterData data : selectedData) {
                elementIds.set(data.getElementId());
            }
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.CHANGE_SELECTION)
                .withParameter(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds)
                .withParameter(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID, new ElementTypeParameterValue(state.getElementType()))
                .withParameter(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, selectionMode)
                .executeLater(scatterPlot.getScatterPlot().getCurrentGraph());
    }

    /**
     * Deselect all on both the ScatterChart within this ScatterChartPane and
     * the active graph.
     *
     * @throws InterruptedException if the operation was canceled (interrupted).
     */
    protected void deselectAllElements() throws InterruptedException {
        if (scatterPlot.getScatterPlot().getCurrentGraph() == null) {
            return;
        }

        selectElementsOnGraph(null, SelectionMode.REPLACE);
    }

    /**
     * Refresh the state of the ScatterChartPane based on the given
     * ScatterPlotState.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private class ScatterPlotRefresher extends SimpleReadPlugin {

        private final ScatterPlotState scatterPlotState;

        public ScatterPlotRefresher(final ScatterPlotState scatterPlotState) {
            this.scatterPlotState = scatterPlotState;
        }

        @Override
        protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            Platform.runLater(() -> {
                final Attribute xAttribute = scatterPlotState.getXAttribute();
                final Attribute yAttribute = scatterPlotState.getYAttribute();

                final ChartBuilder<?, ?> chartBuilder = new ChartBuilder<>(ScatterOptionsPane.VALID_TYPES_X.getOrDefault(xAttribute.getAttributeType(), ScatterOptionsPane.DEFAULT_AXIS_BUILDER), 
                        ScatterOptionsPane.VALID_TYPES_Y.getOrDefault(yAttribute.getAttributeType(), ScatterOptionsPane.DEFAULT_AXIS_BUILDER));
                scatterChart = chartBuilder.build(graph, scatterPlotState, currentData, currentSelectedData);

                scatterChart.setOnMouseEntered(mouseHandler);
                scatterChart.setOnMouseMoved(mouseHandler);
                scatterChart.setOnMousePressed(mouseHandler);
                scatterChart.setOnMouseReleased(mouseHandler);
                scatterChart.setOnMouseClicked(mouseHandler);
                scatterChart.setOnMouseDragged(mouseHandler);

                final Set<ScatterData> selectedData = getSelectionFromGraph(scatterPlotState);
                selectElementsOnChart(selectedData, null);

                scatterPlot.getChartPane().setCenter(scatterChart);
            });
        }

        @Override
        public String getName() {
            return "Scatter Plot: Refresh";
        }
    }
}

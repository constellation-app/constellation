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

import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The parent pane holding all the components of the Scatter Plot.
 *
 * @author cygnus_x-1
 */
public class ScatterPlotPane extends BorderPane {

    private final ScatterPlotTopComponent scatterPlot;
    private final ScatterOptionsPane scatterOptions;
    private final ScatterChartPane scatterChart;

    public ScatterPlotPane(final ScatterPlotTopComponent parent) {
        this.scatterPlot = parent;

        this.scatterOptions = new ScatterOptionsPane(ScatterPlotPane.this);
        try {
            scatterOptions.refreshOptions(scatterPlot.getState());
        } catch (InterruptedException e) {
            ScatterPlotErrorDialog.create("Error refreshing scatter plot: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        VBox.setVgrow(scatterOptions, Priority.NEVER);

        this.scatterChart = new ScatterChartPane(ScatterPlotPane.this);
        try {
            scatterChart.refreshChart(scatterPlot.getState());
        } catch (InterruptedException e) {
            ScatterPlotErrorDialog.create("Error refreshing scatter plot: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        VBox.setVgrow(scatterChart, Priority.ALWAYS);

        final VBox paneHolder = new VBox();
        paneHolder.getChildren().addAll(scatterOptions, scatterChart);
        paneHolder.setFillWidth(true);
        setCenter(paneHolder);

        this.setId("scatter-plot-pane");
        this.setPadding(new Insets(5));
    }

    /**
     * Refresh all the components of the ScatterPlotPane.
     *
     * @param state the ScatterPlotState.
     */
    protected void refreshScatterPlot(ScatterPlotState state) {
        scatterOptions.refreshOptions(state);
        scatterChart.refreshChart(state);
    }

    /**
     * Get the top component holding this ScatterPlotPane.
     *
     * @return the ScatterPlotTopComponent.
     */
    protected ScatterPlotTopComponent getScatterPlot() {
        return scatterPlot;
    }

    /**
     * Get the ScatterChartPane held within this ScatterPlotPane.
     *
     * @return the ScatterChartPane held within this ScatterPlotPane.
     */
    protected ScatterChartPane getChartPane() {
        return scatterChart;
    }

    /**
     * Get the ScatterOptionsPane held within this ScatterPlotPane.
     *
     * @return the ScatterOptionsPane held within this ScatterPlotPane.
     */
    protected ScatterOptionsPane getOptionsPane() {
        return scatterOptions;
    }
}

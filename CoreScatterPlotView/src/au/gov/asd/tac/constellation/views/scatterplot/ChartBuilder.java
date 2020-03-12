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
package au.gov.asd.tac.constellation.views.scatterplot;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.scatterplot.axis.AxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import java.util.Set;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

/**
 *
 * @author cygnus_x-1
 */
public class ChartBuilder<X, Y> {

    private final AxisBuilder<X> xAxisBuilder;
    private final AxisBuilder<Y> yAxisBuilder;

    public ChartBuilder(AxisBuilder<X> xAxis, AxisBuilder<Y> yAxis) {
        this.xAxisBuilder = xAxis;
        this.yAxisBuilder = yAxis;
    }

    public ScatterChart build(GraphReadMethods graph, ScatterPlotState state, Set<ScatterData> currentData, Set<ScatterData> currentSelectedData) {
        currentData.clear();
        currentSelectedData.clear();

        XYChart.Series<X, Y> series = new XYChart.Series<>();
        series.setName(state.getXAttribute() + " vs. " + state.getYAttribute());

        final int selectedAttribute = graph.getAttribute(state.getElementType(), VisualConcept.VertexAttribute.SELECTED.getName());
        final int labelAttribute = graph.getAttribute(state.getElementType(), VisualConcept.VertexAttribute.LABEL.getName());

        final int elementCount = state.getElementType().getElementCount(graph);
        for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
            final int elementId = state.getElementType().getElement(graph, elementPosition);
            if (!state.isSelectedOnly() || graph.getBooleanValue(selectedAttribute, elementId)) {
                final String elementLabel = graph.getStringValue(labelAttribute, elementId);
                final X vertexXValue = xAxisBuilder.getValue(graph, state.getElementType(), state.getXAttribute().getId(), elementId);
                final Y vertexYValue = yAxisBuilder.getValue(graph, state.getElementType(), state.getYAttribute().getId(), elementId);
                final XYChart.Data<X, Y> data = new XYChart.Data(vertexXValue, vertexYValue);
                final ScatterData scatterData = new ScatterData(elementId, elementLabel, data);
                currentData.add(scatterData);
                if (graph.getBooleanValue(selectedAttribute, elementId)) {
                    currentSelectedData.add(scatterData);
                }
                series.getData().add(data);
            }
        }

        final Axis<X> xAxis = xAxisBuilder.build();
        xAxis.setLabel(state.getXAttribute().getName());

        final Axis<Y> yAxis = yAxisBuilder.build();
        yAxis.setLabel(state.getYAttribute().getName());

        final ScatterChart scatterChart = new ScatterChart(xAxis, yAxis);
        scatterChart.setLegendVisible(false);
        scatterChart.setHorizontalGridLinesVisible(false);
        scatterChart.setVerticalGridLinesVisible(false);

        scatterChart.getData().clear();
        scatterChart.getData().add(series);

        return scatterChart;
    }

}

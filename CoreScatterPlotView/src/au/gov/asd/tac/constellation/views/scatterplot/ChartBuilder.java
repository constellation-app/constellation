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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.scatterplot.axis.AxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.axis.LogarithmicAxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.axis.NumberAxisBuilder;
import au.gov.asd.tac.constellation.views.scatterplot.state.ScatterPlotState;
import java.util.Collection;
import java.util.function.Predicate;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import org.openide.NotifyDescriptor;

/**
 *
 * @author cygnus_x-1
 */
public class ChartBuilder<X, Y> {

    private AxisBuilder<X> xAxisBuilder;
    private AxisBuilder<Y> yAxisBuilder;
    private final Predicate<XYChart.Data<X, Y>> xNotGreaterThanZero = data -> ((float) data.getXValue() <= 0);
    private final Predicate<XYChart.Data<X, Y>> yNotGreaterThanZero = data -> ((float) data.getYValue() <= 0);
    private static final String INVALIDLOGWARNING = "Warning: Unable to apply log function to values <= 0";

    public ChartBuilder(final AxisBuilder<X> xAxis, final AxisBuilder<Y> yAxis) {
        this.xAxisBuilder = xAxis;
        this.yAxisBuilder = yAxis;
    }

    public ScatterChart<X, Y> build(final GraphReadMethods graph, final ScatterPlotState state, 
            final Collection<ScatterData> currentData, final Collection<ScatterData> currentSelectedData) {
        currentData.clear();
        currentSelectedData.clear();

        final XYChart.Series<X, Y> series = new XYChart.Series<>();
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
                final XYChart.Data<X, Y> data = new XYChart.Data<>(vertexXValue, vertexYValue);
                final ScatterData scatterData = new ScatterData(elementId, elementLabel, data);
                currentData.add(scatterData);
                if (graph.getBooleanValue(selectedAttribute, elementId)) {
                    currentSelectedData.add(scatterData);
                }
                series.getData().add(data);
            }
        }

        if (xAxisBuilder instanceof LogarithmicAxisBuilder
                && series.getData().stream().anyMatch(xNotGreaterThanZero)) { // If it is a logarithmic axis with values <=0 then switch to a NumericAxis instead
            xAxisBuilder = (AxisBuilder<X>) new NumberAxisBuilder();
            NotifyDisplayer.display(INVALIDLOGWARNING, NotifyDescriptor.WARNING_MESSAGE);
        }
        final Axis<X> xAxis = xAxisBuilder.build();
        xAxis.setLabel(state.getXAttribute().getName());

        if (yAxisBuilder instanceof LogarithmicAxisBuilder
                && series.getData().stream().anyMatch(yNotGreaterThanZero)) { // If it is a logarithmic axis with values <=0 then switch to a NumericAxis instead
            yAxisBuilder = (AxisBuilder<Y>) new NumberAxisBuilder();
        }
        final Axis<Y> yAxis = yAxisBuilder.build();
        yAxis.setLabel(state.getYAttribute().getName());

        final ScatterChart<X, Y> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setLegendVisible(false);
        scatterChart.setHorizontalGridLinesVisible(false);
        scatterChart.setVerticalGridLinesVisible(false);

        scatterChart.getData().clear();
        scatterChart.getData().add(series);

        return scatterChart;
    }
}

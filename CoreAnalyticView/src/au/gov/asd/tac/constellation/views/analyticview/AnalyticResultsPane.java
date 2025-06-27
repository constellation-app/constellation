/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.EmptyResult;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.InternalVisualisation;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * The pane holding gui elements related to visualisation of analytic results.
 *
 * @author cygnus_x-1
 */
public class AnalyticResultsPane extends VBox {

    private final BorderPane progressIndicatorPane;
    private final TabPane internalVisualisationPane;
    private final ToolBar graphVisualisationPane;
    private AnalyticResult<?> result;
    private final AnalyticViewController analyticViewController;

    public AnalyticResultsPane(final AnalyticViewController analyticViewController) {

        // the analytic controller
        this.analyticViewController = analyticViewController;

        // the progress indicator pane
        this.progressIndicatorPane = new BorderPane();
        Platform.runLater(() -> {
            final ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            progressIndicatorPane.setCenter(progressIndicator);
        });

        // the internal visualisation pane
        this.internalVisualisationPane = new TabPane();
        internalVisualisationPane.prefWidthProperty().bind(this.widthProperty());

        // the graph visualisation pane
        this.graphVisualisationPane = new ToolBar();
        graphVisualisationPane.prefWidthProperty().bind(this.widthProperty());

        // populate the analytic results pane
        this.getChildren().addAll(internalVisualisationPane, graphVisualisationPane);
        this.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));
    }

    /**
     * Reset to the initial state for the analytic results pane.
     */
    protected final void reset() {
        Platform.runLater(() -> {
            internalVisualisationPane.getTabs().clear();
            graphVisualisationPane.getItems().clear();
        });
    }

    protected final BorderPane getProgressIndicatorPane() {
        return progressIndicatorPane;
    }

    protected final TabPane getInternalVisualisationPane() {
        return internalVisualisationPane;
    }

    protected final ToolBar getGraphVisualisationPane() {
        return graphVisualisationPane;
    }

    public final AnalyticResult<?> getResult() {
        return result;
    }

    protected final void displayResults(final AnalyticQuestion<?> question, final AnalyticResult results,
            final Map<GraphVisualisation, Boolean> graphVisualisations) {
        if (results.getClass().equals(EmptyResult.class) && question != null) {
            result = question.getResult() == null ? new EmptyResult() : question.getResult();
        } else {
            result = results;
        }
        result.setAnalyticViewController(analyticViewController);

        Platform.runLater(() -> {
            internalVisualisationPane.getTabs().clear();

            AnalyticUtilities.getInternalVisualisationTranslators().forEach(translator -> {
                if (translator.getResultType().isAssignableFrom(result.getClass())) {
                    translator.setQuestion(question);
                    translator.setResult(result);
                    final InternalVisualisation internalVisualisation = translator.buildVisualisation();
                    final Tab visualisationTab = new Tab(internalVisualisation.getName());
                    visualisationTab.setClosable(false);
                    visualisationTab.setContent(internalVisualisation.getVisualisation());
                    internalVisualisationPane.getTabs().add(visualisationTab);
                }
            });

            graphVisualisationPane.getItems().clear();
            final Label applyResults = new Label("Apply to Results: ");
            graphVisualisationPane.getItems().add(applyResults);

            AnalyticUtilities.getGraphVisualisationTranslators().forEach(translator -> {
                if (translator.getResultType().isAssignableFrom(result.getClass())) {
                    translator.setQuestion(question);
                    translator.setResult(result);
                    translator.setActive(true);

                    GraphVisualisation graphVisualisation = translator.buildControl();
                    Node visualisationNode = graphVisualisation.getVisualisation();

                    if (graphVisualisations.containsKey(graphVisualisation)) {
                        graphVisualisation.setSelected(graphVisualisations.get(graphVisualisation));
                        for (final Map.Entry<GraphVisualisation, Boolean> visualisation : graphVisualisations.entrySet()) {
                            if (visualisation.getKey().getClass() == graphVisualisation.getClass()) {
                                graphVisualisation = visualisation.getKey();
                                visualisationNode = graphVisualisation.getVisualisation();
                                graphVisualisation.setSelected(visualisation.getValue());
                            }
                        }
                    }

                    graphVisualisationPane.getItems().add(visualisationNode);
                    AnalyticViewController.getDefault().updateGraphVisualisations(graphVisualisation, graphVisualisation.isActive());
                }
            });
        });

        // add the results to the graph state
        AnalyticViewController.getDefault().updateResults(result);
    }
}

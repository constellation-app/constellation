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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.plugins.ThreadConstraints;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.state.AnalyticViewState;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 * The parent pane holding all the components of the Analytic View.
 *
 * @author cygnus_x-1
 */
public class AnalyticViewPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewPane.class.getName());

    private static final String RUN_START_TEXT = "Run";
    private static final String RUN_START_STYLE = "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;";
    private static final String RUN_STOP_TEXT = "Stop";
    private static final String RUN_STOP_STYLE = "-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;";

    private final VBox viewPane;
    private final AnchorPane analyticOptionsPane;
    private final HBox analyticOptionButtons;
    private final Button runButton;

    private final AnalyticConfigurationPane analyticConfigurationPane;
    private final AnalyticResultsPane analyticResultsPane;

    private static boolean running = false;
    private Thread questionThread = null;
    private ThreadConstraints parentConstraints = null;

    public AnalyticViewPane(final AnalyticViewController analyticViewController) {

        // the top level analytic view pane
        this.viewPane = new VBox();
        viewPane.prefWidthProperty().bind(this.widthProperty());

        // the pane allowing analytic view options to be set
        this.analyticOptionsPane = new AnchorPane();
        analyticOptionsPane.prefWidthProperty().bind(viewPane.widthProperty());

        // the pane which displays all visualisations and options relating to the results of an analytic question
        this.analyticResultsPane = new AnalyticResultsPane(analyticViewController);
        analyticResultsPane.prefWidthProperty().bind(viewPane.widthProperty());
        analyticResultsPane.minHeightProperty().bind(viewPane.heightProperty().multiply(0.4));

        // the pane allowing selection and configuration of an analytic question
        this.analyticConfigurationPane = new AnalyticConfigurationPane();
        analyticConfigurationPane.prefWidthProperty().bind(viewPane.widthProperty());

        // the pane holding the analytic option buttons
        this.analyticOptionButtons = new HBox();
        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.setOnAction(event -> new HelpCtx(this.getClass().getName()).display());
        this.runButton = new Button(RUN_START_TEXT);
        runButton.setStyle(RUN_START_STYLE);
        runButton.setOnAction(event -> {
            if (running) {
                // hide results pane
                if (viewPane.getChildren().contains(analyticResultsPane)) {
                    viewPane.getChildren().remove(analyticResultsPane);
                }

                // stop execution of the current analytic question
                if (questionThread != null && questionThread.isAlive()) {
                    questionThread.interrupt();
                }
                running = false;
                setRunButtonMode(true);
            } else {
                setRunButtonMode(false);
                // display results pane
                if (!viewPane.getChildren().contains(analyticResultsPane)) {
                    viewPane.getChildren().add(1, analyticResultsPane);
                }
                // display progress indicator
                analyticResultsPane.getInternalVisualisationPane().getTabs().clear();
                final Tab progressTab = new Tab("Progress");
                progressTab.setClosable(false);
                progressTab.setContent(analyticResultsPane.getProgressIndicatorPane());
                analyticResultsPane.getInternalVisualisationPane().getTabs().add(progressTab);
                // answer the current analytic question and display the results
                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                final SimplePlugin virtualAnalytics = new SimplePlugin("Analytic View - Query Runner") {
                    @Override
                    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                        parentConstraints = ThreadConstraints.getConstraints();
                        questionThread = new Thread(() -> {
                            final ThreadConstraints localConstraints = ThreadConstraints.getConstraints();
                            if (localConstraints.getCurrentReport() == null) {
                                localConstraints.setCurrentReport(parentConstraints.getCurrentReport());
                            }
                            running = true;
                            try {
                                final AnalyticQuestion<?> question = analyticConfigurationPane.answerCurrentQuestion();
                                analyticResultsPane.displayResults(question, null);
                                analyticViewController.updateState(true, analyticConfigurationPane.getPluginList());
                            } catch (final AnalyticException ex) {
                                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                                final AnalyticQuestion<?> question = new AnalyticQuestion<>(analyticConfigurationPane.getCurrentQuestion());
                                question.addException(ex);
                                analyticResultsPane.displayResults(question, null);
                                analyticViewController.updateState(false, analyticConfigurationPane.getPluginList());
                            } finally {
                                running = false;
                                setRunButtonMode(true);
                            }
                        }, "Analytic View: Answer Question");
                        questionThread.start();
                        interaction.notify(PluginNotificationLevel.INFO, " * Working * ");
                    }
                };

                try {
                    PluginExecution.withPlugin(virtualAnalytics).interactively(false).executeNow(activeGraph);
                } catch (final InterruptedException iex) {
                    LOGGER.log(Level.SEVERE, iex.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                } catch (final PluginException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        });
        analyticOptionButtons.getChildren().addAll(helpButton, runButton);

        // populate the analytic options pane
        analyticOptionsPane.getChildren().add(analyticOptionButtons);
        AnchorPane.setRightAnchor(analyticOptionButtons, 5.0);

        // populate the analytic view pane
        viewPane.getChildren().addAll(analyticOptionsPane, analyticConfigurationPane);

        // initialise the top level pane
        this.setCenter(viewPane);
    }

    private void setRunButtonMode(final boolean isRunMode) {
        Platform.runLater(() -> {
            runButton.setText(isRunMode ? RUN_START_TEXT : RUN_STOP_TEXT);
            runButton.setStyle(isRunMode ? RUN_START_STYLE : RUN_STOP_STYLE);
        });
    }

    protected final void reset() {
        Platform.runLater(() -> {
            // hide results pane
            if (viewPane.getChildren().contains(analyticResultsPane)) {
                viewPane.getChildren().remove(analyticResultsPane);
            }
            analyticConfigurationPane.reset();
            analyticResultsPane.reset();
        });
    }

    public final AnalyticConfigurationPane getConfigurationPane() {
        return analyticConfigurationPane;
    }

    public final AnalyticResultsPane getResultsPane() {
        return analyticResultsPane;
    }

    protected final void setIsRunnable(final boolean isRunnable) {
        Platform.runLater(() -> runButton.setDisable(!isRunnable));
    }

    /**
     * Is passed in what should currently be active on the pane according to the state and updates the view to match
     */
    public void updateView(final AnalyticViewState state) {
        reset();
        Platform.runLater(() -> {
            final int activeQuestion = state.getCurrentAnalyticQuestionIndex();
            if (!state.getActiveAnalyticQuestions().isEmpty()) {
                final AnalyticQuestionDescription<?> currentQuestion = state.getActiveAnalyticQuestions().get(activeQuestion);
                analyticConfigurationPane.setCurrentQuestion(currentQuestion);
                final boolean categoriesVisible = state.isCategoriesPaneVisible();
                final List<AnalyticQuestionDescription<?>> activeAnalyticQuestions = state.getActiveAnalyticQuestions();
                final List<List<AnalyticConfigurationPane.SelectableAnalyticPlugin>> activeSelectablePlugins = state.getActiveSelectablePlugins();
                final String activeCategory = state.getActiveCategory();

                // need to update configuration pane UI
                analyticConfigurationPane.updatePanes(categoriesVisible, activeAnalyticQuestions, activeSelectablePlugins, activeCategory);

                // show the current results if there are any
                final AnalyticResult<?> results = state.getResult();
                final boolean resultsVisible = state.isResultsPaneVisible();
                final AnalyticQuestion<?> question = state.getQuestion();

                if (results != null && resultsVisible && question != null && !viewPane.getChildren().contains(analyticResultsPane)) {
                        viewPane.getChildren().add(1, analyticResultsPane);
                        analyticResultsPane.displayResults(question, results);
                }
            }         
        });
    }
}

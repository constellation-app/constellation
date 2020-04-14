/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewTopComponent.AnalyticController;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 * The parent pane holding all the components of the Analytic View.
 *
 * @author cygnus_x-1
 */
public class AnalyticViewPane extends BorderPane {

    private static final String RUN_START_TEXT = "Run";
    private static final String RUN_START_STYLE = "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;";
    private static final String RUN_STOP_TEXT = "Stop";
    private static final String RUN_STOP_STYLE = "-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;";

    private final VBox analyticViewPane;
    private final AnchorPane analyticOptionsPane;
    private final HBox analyticOptionControls;
    private final HBox analyticOptionButtons;
    private final Button runButton;

    private final AnalyticConfigurationPane analyticConfigurationPane;
    private final AnalyticResultsPane analyticResultsPane;

    private boolean running = false;
    private Thread questionThread = null;

    public AnalyticViewPane(final AnalyticController analyticController) {

        // the top level analytic view pane
        this.analyticViewPane = new VBox();
        analyticViewPane.prefWidthProperty().bind(this.widthProperty());

        // the pane allowing analytic view options to be set
        this.analyticOptionsPane = new AnchorPane();
        analyticOptionsPane.prefWidthProperty().bind(analyticViewPane.widthProperty());

        // the pane which displays all visualisations and options relating to the results of an analytic question
        this.analyticResultsPane = new AnalyticResultsPane(analyticController);
        analyticResultsPane.prefWidthProperty().bind(analyticViewPane.widthProperty());
        analyticResultsPane.minHeightProperty().bind(analyticViewPane.heightProperty().multiply(0.4));

        // the pane allowing selection and configuration of an analytic question
        this.analyticConfigurationPane = new AnalyticConfigurationPane();
        analyticConfigurationPane.prefWidthProperty().bind(analyticViewPane.widthProperty());

        // the pane holding the analytic option items
        this.analyticOptionControls = new HBox();
        final MenuBar analyticMenu = new MenuBar();
        final MenuItem saveMenuItem = new MenuItem("Save Question");
        saveMenuItem.setOnAction(event -> {
            // TODO: handle saving analytic questions, then add this to the menu
        });
//        optionsMenu.getItems().add(saveMenuItem);
//        analyticMenu.getMenus().add(optionsMenu);

        final Button addButton = new Button("", new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
        addButton.setOnAction(event -> {
            // TODO: handle adding new analytic view tabs, then add this to the controls
        });
        analyticOptionControls.getChildren().add(analyticMenu); //, addButton);

        // the pane holding the analytic option buttons
        this.analyticOptionButtons = new HBox();
        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16)));
        helpButton.setOnAction(event -> {
            new HelpCtx(this.getClass().getName()).display();
        });
        this.runButton = new Button(RUN_START_TEXT);
        runButton.setStyle(RUN_START_STYLE);
        runButton.setOnAction(event -> {
            if (running) {
                // hide results pane
                if (analyticViewPane.getChildren().contains(analyticResultsPane)) {
                    analyticViewPane.getChildren().remove(analyticResultsPane);
                }

                // stop execution of the current analytic question
                if (questionThread != null && questionThread.isAlive()) {
                    questionThread.interrupt();
                }
                running = false;
                Platform.runLater(() -> {
                    runButton.setText(RUN_START_TEXT);
                    runButton.setStyle(RUN_START_STYLE);
                });
            } else {
                // display results pane
                if (!analyticViewPane.getChildren().contains(analyticResultsPane)) {
                    analyticViewPane.getChildren().add(1, analyticResultsPane);
                }
                // display progress indicator
                analyticResultsPane.getInternalVisualisationPane().getTabs().clear();
                final Tab progressTab = new Tab("Progress");
                progressTab.setClosable(false);
                progressTab.setContent(analyticResultsPane.getProgressIndicatorPane());
                analyticResultsPane.getInternalVisualisationPane().getTabs().add(progressTab);
                // answer the current analytic question and display the results
                Thread answerQuestionThread = new Thread(() -> {
                    Platform.runLater(() -> {
                        runButton.setText(RUN_STOP_TEXT);
                        runButton.setStyle(RUN_STOP_STYLE);
                    });

                    running = true;
                    try {
                        AnalyticQuestion question = analyticConfigurationPane.answerCurrentQuestion();
                        analyticResultsPane.displayResults(question);
                    } catch (AnalyticException ex) {
                        final AnalyticQuestion question = new AnalyticQuestion(analyticConfigurationPane.getCurrentQuestion());
                        question.addException(ex);
                        analyticResultsPane.displayResults(question);
                    } finally {
                        running = false;

                        Platform.runLater(() -> {
                            runButton.setText(RUN_START_TEXT);
                            runButton.setStyle(RUN_START_STYLE);
                        });
                    }
                }, "Analytic View: Answer Question");
                questionThread = answerQuestionThread;
                answerQuestionThread.start();
            }
        });
        analyticOptionButtons.getChildren().addAll(helpButton, runButton);

        // populate the analytic options pane
        analyticOptionsPane.getChildren().addAll(analyticOptionControls, analyticOptionButtons);
        AnchorPane.setLeftAnchor(analyticOptionControls, 5.0);
        AnchorPane.setRightAnchor(analyticOptionButtons, 5.0);
        VBox.setVgrow(analyticMenu, Priority.ALWAYS);

        // populate the analytic view pane
        analyticViewPane.getChildren().addAll(analyticOptionsPane, analyticConfigurationPane);

        // initialise the top level pane
        this.setCenter(analyticViewPane);
    }

    protected final void reset() {
        Platform.runLater(() -> {
            // hide results pane
            if (analyticViewPane.getChildren().contains(analyticResultsPane)) {
                analyticViewPane.getChildren().remove(analyticResultsPane);
            }
            analyticConfigurationPane.reset();
            analyticResultsPane.reset();
        });
    }

    protected final AnalyticConfigurationPane getConfigurationPane() {
        return analyticConfigurationPane;
    }

    protected final AnalyticResultsPane getResultsPane() {
        return analyticResultsPane;
    }

    protected final void setIsRunnable(final boolean isRunnable) {
        Platform.runLater(() -> {
            runButton.setDisable(!isRunnable);
        });
    }
}

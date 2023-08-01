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
package au.gov.asd.tac.constellation.views.pluginreporter.panes;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportFilter;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportListener;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.VBox;

/**
 * A PluginReportPane provides the UI that displays a single PluginReport and
 * its child PluginReports.
 *
 * @author sirius
 */
public class PluginReportPane extends BorderPane implements PluginReportListener {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
    private static final Image REPORT_CONTRACTED_IMAGE = UserInterfaceIconProvider.CHEVRON_RIGHT.buildImage(10);
    private static final Image REPORT_EXPANDED_IMAGE = UserInterfaceIconProvider.CHEVRON_DOWN.buildImage(10);

    private final PluginReporterPane reporterPane;
    private final PluginReport pluginReport;
    private final Set<String> filteredTags;
    private final PluginReportFilter pluginReportFilter;

    private final VBox sequencePane = new VBox(2);
    private final GridPane contentPane = new GridPane();
    private final Label pluginNameLabel = new Label();
    private final ProgressBar pluginProgressBar = new ProgressBar();
    private final Label timeLabel = new Label("3:00");
    private final Label messageLabel = new Label();

    private int nextChild = 0;

    /**
     * Creates a new PluginReportPane for a specified PluginReport and places it
     * is a specified PluginReporterPane.
     *
     * @param reporterPane the PluginReporterPane which will display this
     * PluginReportPane.
     * @param pluginReport the PluginReport that the new PluginReportPane will
     * display.
     * @param filteredTags the current list of tags that are filtering the
     * PluginReporterPane.
     * @param pluginReportFilter the filter that is currently applied to the
     * PluginReporterPane.
     */
    public PluginReportPane(PluginReporterPane reporterPane, PluginReport pluginReport, Set<String> filteredTags, PluginReportFilter pluginReportFilter) {
        this.reporterPane = reporterPane;
        this.pluginReport = pluginReport;
        this.filteredTags = filteredTags;
        this.pluginReportFilter = pluginReportFilter;
        this.pluginReport.addPluginReportListener(this);

        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: transparent;");
        borderPane.setCenter(contentPane);

        ColumnConstraints column0Constraints = new ColumnConstraints();
        column0Constraints.setFillWidth(false);

        ColumnConstraints column1Constraints = new ColumnConstraints();
        column1Constraints.setHgrow(Priority.ALWAYS);
        column1Constraints.setFillWidth(true);

        ColumnConstraints column2Constraints = new ColumnConstraints();
        column2Constraints.setMinWidth(USE_PREF_SIZE);
        column2Constraints.setFillWidth(false);

        contentPane.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);

        contentPane.setStyle("-fx-background-color: transparent;");

        pluginNameLabel.setId("plugin-name");
        contentPane.add(pluginNameLabel, 0, 0);

        pluginProgressBar.setMaxHeight(1);
        GridPane.setHalignment(pluginProgressBar, HPos.RIGHT);
        GridPane.setFillWidth(pluginProgressBar, true);
        contentPane.add(pluginProgressBar, 1, 0);

        GridPane.setFillWidth(timeLabel, true);
        timeLabel.setPadding(new Insets(0, 0, 0, 5));
        timeLabel.setMinWidth(USE_PREF_SIZE);
        contentPane.add(timeLabel, 2, 0);

        BorderPane messageContainer = new BorderPane();
        contentPane.add(messageContainer, 0, 1, 3, 1);

        ToggleButton messageButton = new ToggleButton();
        messageButton.setGraphic(new ImageView(REPORT_CONTRACTED_IMAGE));
        messageButton.setPadding(Insets.EMPTY);
        messageButton.setMaxSize(15, 15);
        messageButton.setMinSize(15, 15);
        messageButton.setOnAction((ActionEvent event) -> {
            if (messageButton.isSelected()) {
                messageLabel.setText(pluginReport.getMessageLog());
                messageLabel.setMaxHeight(Double.MAX_VALUE);
                messageButton.setGraphic(new ImageView(REPORT_EXPANDED_IMAGE));
            } else {
                messageLabel.setText(pluginReport.getMessage());
                messageLabel.setMaxHeight(10);
                messageButton.setGraphic(new ImageView(REPORT_CONTRACTED_IMAGE));
            }
        });
        messageContainer.setLeft(messageButton);

        messageLabel.setId("plugin-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxHeight(10);
        messageLabel.setPadding(new Insets(0, 0, 0, 5));
        messageContainer.setCenter(messageLabel);

        sequencePane.setFillWidth(true);
        sequencePane.setId("pluginReport");
        sequencePane.setPadding(new Insets(3));
        sequencePane.getChildren().add(borderPane);
        setCenter(sequencePane);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem saveToClipboardItem = new MenuItem("Save Details To Clipboard");
        saveToClipboardItem.setOnAction((ActionEvent event) -> saveToClipboard());
        contextMenu.getItems().addAll(saveToClipboardItem);
        setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(PluginReportPane.this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        setOnMouseClicked((MouseEvent event) -> contextMenu.hide());

        if (pluginReport.getStopTime() < 0) {
            PluginReportTimeUpdater.addPluginReport(this);
        }

        update();
    }

    @Override
    public String toString() {
        return pluginReport.getPluginName();
    }

    /**
     * Returns the PluginReport for this pane.
     *
     * @return the PluginReport for this pane.
     */
    public PluginReport getPluginReport() {
        return pluginReport;
    }

    /**
     * Saves a text representation of this PluginReport to the clipboard.
     */
    private void saveToClipboard() {
        CharArrayWriter writer = new CharArrayWriter();
        try (PrintWriter out = new PrintWriter(writer)) {
            out.append("Name: " + pluginReport.getPluginName() + SeparatorConstants.NEWLINE);
            out.append("Description: " + pluginReport.getPluginDescription() + SeparatorConstants.NEWLINE);
            out.append("Message: " + pluginReport.getMessage() + SeparatorConstants.NEWLINE);
            out.append("Tags: " + Arrays.toString(pluginReport.getTags()) + SeparatorConstants.NEWLINE);
            out.append("Start: " + dateFormat.format(new Date(pluginReport.getStartTime())) + SeparatorConstants.NEWLINE);
            out.append("Stop: " + dateFormat.format(new Date(pluginReport.getStopTime())) + SeparatorConstants.NEWLINE);

            if (pluginReport.getError() != null) {
                out.append("Error: " + pluginReport.getError().getMessage() + "\n\n");
                pluginReport.getError().printStackTrace(out);
            }
        }

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(writer.toString());
        clipboard.setContent(content);

        // TODO: can't do this because of circular dependancy
//        ClipboardUtilities.copyToClipboard(writer.toString());
        PluginExecution.withPlugin(new SimplePlugin("Copy To Clipboard") {
            @Override
            protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
                ConstellationLoggerHelper.copyPropertyBuilder(this, writer.toString().length(), ConstellationLoggerHelper.SUCCESS);
            }
        }).executeLater(null);

    }

    /**
     * Updates the UI displayed by this PluginReportPane to reflect the current
     * state of the underlying PluginReport.
     */
    private void update() {

        sequencePane.getStyleClass().clear();
        pluginNameLabel.getStyleClass().clear();
        messageLabel.getStyleClass().clear();

        Throwable error = pluginReport.getError();
        if (error == null) {
            long stopTime = pluginReport.getStopTime();

            // If the plugin is still running
            if (stopTime == -1) {
                sequencePane.getStyleClass().add("running");
                pluginNameLabel.getStyleClass().add(JavafxStyleManager.LIGHT_NAME_TEXT);
                messageLabel.getStyleClass().add(JavafxStyleManager.LIGHT_MESSAGE_TEXT);

                // If the plugin has finished
            } else {
                sequencePane.getStyleClass().add("finished");
                pluginNameLabel.getStyleClass().add(JavafxStyleManager.LIGHT_NAME_TEXT);
                messageLabel.getStyleClass().add(JavafxStyleManager.LIGHT_MESSAGE_TEXT);
            }

            if (pluginReport.getMessage() == null) {
                messageLabel.setVisible(false);
            } else {
                messageLabel.setVisible(true);
                messageLabel.setText(pluginReport.getMessage());
            }

        } else {

            // If the plugin has been cancelled
            if (error instanceof InterruptedException) {
                sequencePane.getStyleClass().add("interrupted");
                pluginNameLabel.getStyleClass().add(JavafxStyleManager.LIGHT_NAME_TEXT);
                messageLabel.getStyleClass().add(JavafxStyleManager.LIGHT_MESSAGE_TEXT);
                messageLabel.setText("Cancelled");

                // If the plugin failed in an expected way
            } else if (error instanceof PluginException) {
                sequencePane.getStyleClass().add("failed");
                pluginNameLabel.getStyleClass().add("darkNameText");
                messageLabel.getStyleClass().add("darkMessageText");

                Writer errorWriter = new CharArrayWriter();
                try (PrintWriter out = new PrintWriter(errorWriter)) {
                    out.append(error.getMessage());
                    out.append("\n\n");
                    error.printStackTrace(out);
                }
                messageLabel.setText(errorWriter.toString());

                // If the plugin failed in an unexpected way
            } else {
                sequencePane.getStyleClass().add("errored");
                pluginNameLabel.getStyleClass().add(JavafxStyleManager.LIGHT_NAME_TEXT);
                messageLabel.getStyleClass().add(JavafxStyleManager.LIGHT_MESSAGE_TEXT);

                Writer errorWriter = new CharArrayWriter();
                try (PrintWriter out = new PrintWriter(errorWriter)) {
                    out.append(error.getMessage());
                    out.append("\n\n");
                    error.printStackTrace(out);
                }
                messageLabel.setText(errorWriter.toString());
            }
        }

        int currentStep = pluginReport.getCurrentStep();
        int totalSteps = pluginReport.getTotalSteps();
        if (currentStep > totalSteps) {
            pluginProgressBar.setVisible(false);
        } else if (totalSteps <= 0) {
            pluginProgressBar.setVisible(true);
            pluginProgressBar.setProgress(-1);
        } else {
            pluginProgressBar.setVisible(true);
            pluginProgressBar.setProgress((double) currentStep / (double) totalSteps);
        }

        updateTime();

        pluginNameLabel.setText(pluginReport.getPluginName());
    }

    /**
     * Updates the timer displayed by this PluginReportPane.
     */
    public void updateTime() {
        long startTime = pluginReport.getStartTime();
        long stopTime = pluginReport.getStopTime();

        if (stopTime >= 0) {
            PluginReportTimeUpdater.removePluginReport(this);
        }

        long time = stopTime < 0 ? (System.currentTimeMillis() - startTime) : (stopTime - startTime);
        timeLabel.setText(convertTime(time));
    }

    /**
     * Updates the UI to reflect any new child plugin reports that have been
     * added to this plugin report since the UI was last updated.
     */
    public void updateChildren() {
        Platform.runLater(() -> {
            synchronized (PluginReportPane.this) {
                while (nextChild < pluginReport.getUChildReports().size()) {
                    PluginReport childReport = pluginReport.getUChildReports().get(nextChild++);

                    BorderPane borderPane = new BorderPane();

                    if ((pluginReportFilter == null || pluginReportFilter.includePluginReport(childReport)) && !childReport.containsAllTags(filteredTags)) {
                        PluginReportPane childPane = new PluginReportPane(reporterPane, childReport, filteredTags, pluginReportFilter);

                        Pane paddingPane = new Pane();
                        paddingPane.setPrefWidth(20);
                        paddingPane.setMinWidth(USE_PREF_SIZE);
                        paddingPane.setMaxWidth(USE_PREF_SIZE);

                        borderPane.setLeft(paddingPane);
                        borderPane.setCenter(childPane);
                        childPane.updateChildren();
                    }

                    sequencePane.getChildren().add(borderPane);
                }
            }
        });
    }

    /**
     * A convenience method to convert a datetime into a intuitive
     * human-readable string.
     */
    private static String convertTime(long time) {
        StringBuilder result = new StringBuilder();
        long hours = time / (60 * 60 * 1000);
        time -= hours * (60 * 60 * 1000);
        long minutes = time / (60 * 1000);
        time -= minutes * (60 * 1000);
        long seconds = time / 1000;
        long millis = time - (seconds * 1000);

        if (hours > 0) {
            result.append(hours);
            result.append('h');
            result.append(minutes);
            result.append('m');
        } else if (minutes > 0) {
            result.append(minutes);
            result.append('m');
            result.append(seconds);
            result.append('s');
        } else if (seconds > 9) {
            result.append(seconds);
            result.append('s');
        } else if (seconds > 0) {
            result.append(seconds);
            result.append('.');
            result.append(millis / 100);
            result.append('s');
        } else {
            result.append('0').append('.');
            if (millis < 100) {
                result.append('0');
            }
            if (millis < 10) {
                result.append('0');
            }
            result.append(millis);
            result.append('s');
        }
        return result.toString();
    }

    @Override
    public void pluginReportChanged(PluginReport pluginReport) {
        Platform.runLater(this::update);
    }

    @Override
    public void addedChildReport(PluginReport parentReport, PluginReport childReport) {
        Platform.runLater(() -> {
            synchronized (reporterPane) {
                reporterPane.updateTags();
            }
        });
        updateChildren();
    }

    /**
     * Remove the plugin report listener to allow the PlguinReportPane to be
     * garbage collected
     */
    public void removeListener() {
        this.pluginReport.removePluginReportListener(this);
    }

    /**
     * Get the time label for unit tests
     * @return timeLabel
     */
    protected Label getTimeLabel() {
        return timeLabel;
    }
}

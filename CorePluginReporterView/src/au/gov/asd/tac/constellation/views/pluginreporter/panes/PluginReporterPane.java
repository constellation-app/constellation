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

import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportFilter;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

/**
 * A PluginReporterPane provides a UI where all PluginReports for a single graph
 * are displayed.
 *
 * @author sirius
 */
public class PluginReporterPane extends BorderPane implements ListChangeListener<String> {

    private static final String FILTERED_TAGS_KEY = "filteredTags";

    private final ToolBar controlToolbar = new ToolBar();

    private final ScrollPane reportBoxScroll;
    private final VBox reportBox = new VBox(2);
    private GraphReport graphReport = null;

    private final ObservableList<String> availableTags = FXCollections.observableArrayList();
    private final MultiChoiceInputField<String> tagFilterMultiChoiceInput = new MultiChoiceInputField<>(availableTags);
    private final Set<String> filteredTags = new HashSet<>();
    private PluginReportFilter pluginReportFilter = null;


    // The height of the report box last time we looked
    // This allows us to see if a change in the vertical scroll
    // bar is due to the user or an increase in the report box height.
    private double reportBoxHeight = -1;

    // This number of reports have already been added to the report box.
    // When an update occurs, we can just start looking from this point.
    private int nextReport = 0;

    // Plugin reports are only displayed if their start times are
    // after this time.
    private long clearTime = -1;

    // Limit the amount of JavaFX PluginReportPane's to keep to prevent
    // the JavaFX thread from running out of memory
    private static final int MAXIMUM_REPORT_PANES = 300;

    public PluginReporterPane() {
    
        // Update filtered tags from preferences
        final Preferences prefs = NbPreferences.forModule(PluginReporterPane.class);
        String filteredTagString = prefs.get(FILTERED_TAGS_KEY, PluginTags.LOW_LEVEL);
        filteredTags.addAll(Arrays.asList(filteredTagString.split(SeparatorConstants.TAB, 0)));

        // The filter drop down
        Label filterLabel = new Label("Filter: ");
        tagFilterMultiChoiceInput.setMaxWidth(Double.MAX_VALUE);
        tagFilterMultiChoiceInput.setMinWidth(50);

        // Group these together so the Toolbar treats them as a unit.
        final HBox filterBox = new HBox(filterLabel, tagFilterMultiChoiceInput);
        filterBox.setAlignment(Pos.BASELINE_LEFT);

        // The clear button
        Button clearButton = new Button("Clear");
        clearButton.setTooltip(new Tooltip("Clear all plugins but show new plugins as they are run"));
        clearButton.setOnAction((ActionEvent event) -> {
            clearTime = System.currentTimeMillis();
            setPluginReportFilter(defaultReportFilter);
        });

        // The show all button
        Button showAllButton = new Button("Show All");
        showAllButton.setTooltip(new Tooltip("Show all plugins that have been run on this graph"));
        showAllButton.setOnAction((ActionEvent event) -> {
            clearTime = -1;
            setPluginReportFilter(defaultReportFilter);
        });

        final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
        Button helpButton = new Button("", helpImage);
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent; -fx-effect: null; ");
        helpButton.setOnAction((ActionEvent event)
                -> new HelpCtx(getClass().getPackage().getName()).display());

        controlToolbar.getItems().addAll(
                filterBox,
                clearButton, showAllButton, helpButton);
        setTop(controlToolbar);

        reportBox.setFillWidth(true);
        reportBox.setPadding(new Insets(2));

        reportBoxScroll = new ScrollPane();
        reportBoxScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        reportBoxScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reportBoxScroll.setContent(reportBox);
        reportBoxScroll.setFitToWidth(true);
        reportBoxScroll.setVvalue(reportBoxScroll.getVmax());

        // Slightly ugly way of keeping the scroll bar at the bottom when new plugin records are added.
        reportBoxScroll.vvalueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double oldReportBoxHeight = reportBoxHeight;
            reportBoxHeight = reportBox.heightProperty().doubleValue();
            if (oldReportBoxHeight < reportBoxHeight && (oldReportBoxHeight < 0 || (oldValue.doubleValue() == reportBoxScroll.getVmax() && newValue.doubleValue() != reportBoxScroll.getVmax()))) {
                reportBoxScroll.setVvalue(reportBoxScroll.getVmax());
            }
        });
        setCenter(reportBoxScroll);
    }

    @Override
    public void onChanged(ListChangeListener.Change<? extends String> c) {
        filteredTags.addAll(tagFilterMultiChoiceInput.getItems());
        filteredTags.removeAll(tagFilterMultiChoiceInput.getCheckModel().getCheckedItems());

        // Save the new filtered tags to preferences
        StringBuilder prefString = new StringBuilder();
        String delimiter = "";
        for (String filteredTag : filteredTags) {
            prefString.append(delimiter);
            delimiter = SeparatorConstants.TAB;
            prefString.append(filteredTag);
        }
        final Preferences prefs = NbPreferences.forModule(PluginReporterPane.class);
        prefs.put(FILTERED_TAGS_KEY, prefString.toString());

        setGraphReport(graphReport);
    }

    public final void setPluginReportFilter(PluginReportFilter pluginReportFilter) {
        this.pluginReportFilter = pluginReportFilter;
        setGraphReport(graphReport);
    }

    public synchronized void addPluginReport(PluginReport pluginReport) {
        if (pluginReport.getGraphReport() == graphReport) {
            updateReports(false);
        }
    }

    /**
     * Update the reports shown in the report box to reflect those recorded
     * against the current graph.
     *
     * @param refresh
     */
    private void updateReports(boolean refresh) {
        Platform.runLater(() -> {
            if (refresh) {
                nextReport = nextReport > MAXIMUM_REPORT_PANES ? nextReport - MAXIMUM_REPORT_PANES : 0;

                // remove the listeners before clearing the PluginReportPane's
                for (int i = 0; i < reportBox.getChildren().size(); i++) {
                    ((PluginReportPane) reportBox.getChildren().get(i)).removeListener();
                }
                reportBox.getChildren().clear();
            }

            if (graphReport != null) {
                while (nextReport < graphReport.getPluginReports().size()) {
                    PluginReport pluginReport = graphReport.getPluginReports().get(nextReport++);
                    if ((pluginReportFilter == null || pluginReportFilter.includePluginReport(pluginReport)) && !pluginReport.containsAllTags(filteredTags)) {
                        PluginReportPane reportPane = new PluginReportPane(this, pluginReport, filteredTags, pluginReportFilter);
                        reportBox.getChildren().add(reportPane);
                        reportPane.updateChildren();
                    }
                }

                // TODO: do a better job here of not adding older reports in the first place. The idea here was to reduce memory so this logic is less useful of adding and removing.
                // remove the oldest one if we have reached the maximum
                final int size = reportBox.getChildren().size();
                if (size > MAXIMUM_REPORT_PANES) {
                    ((PluginReportPane) reportBox.getChildren().get(size - MAXIMUM_REPORT_PANES)).removeListener();
                    reportBox.getChildren().remove(size - MAXIMUM_REPORT_PANES);
                }
            }
            updateTags();
        });
    }

    void updateTags() {
        tagFilterMultiChoiceInput.getCheckModel().getCheckedItems().removeListener(this);
        tagFilterMultiChoiceInput.getCheckModel().clearChecks();
        if (graphReport != null) {
            final List<String> tags = new ArrayList<>(graphReport.getUTags());
            int[] selectedIndices = new int[tags.size()];
            int selectedIndexCount = 0;
            int tagIndex = 0;
            for (String tag : tags) {
                if (!availableTags.contains(tag)) {
                    availableTags.add(tag);
                }
                if (!filteredTags.contains(tag)) {
                    tagIndex = availableTags.indexOf(tag);
                    selectedIndices[selectedIndexCount++] = tagIndex; //AIOOBE = DED.
                }
            }
            tagFilterMultiChoiceInput.getCheckModel().checkIndices(Arrays.copyOfRange(selectedIndices, 0, selectedIndexCount));
        }
        tagFilterMultiChoiceInput.getCheckModel().getCheckedItems().addListener(this);
    }

    public synchronized void setGraphReport(GraphReport graphReport) {
        this.graphReport = graphReport;
        reportBoxHeight = -1;
        updateReports(true);
    }

    private PluginReportFilter defaultReportFilter = new PluginReportFilter() {
        @Override
        public boolean includePluginReport(PluginReport report) {
            return report.getStartTime() > clearTime;
        }
    };

    public synchronized GraphReport getGraphReport() {
        return graphReport;
    }

    public PluginReportFilter getPluginReportFilter() {
        return pluginReportFilter;
    }
}

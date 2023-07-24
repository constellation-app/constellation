/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.json.JsonUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * The parent pane holding all the components of the Quality Control View.
 *
 * @author cygnus_x-1
 */
@Messages({
    "MSG_NotApplicable=◇",
    "MSG_QualtyControlRules=Quality control rules for node %s",
    "MSG_SelectSomething=Select some nodes in your graph to review their data quality.",
    "MSG_NoEntries=There are no rules for this identifier."
})
public final class QualityControlViewPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(QualityControlViewPane.class.getName());

    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static Map<QualityControlRule, QualityCategory> rulePriorities = null;
    private static Map<QualityControlRule, Boolean> ruleEnabledStatuses = null;
    private static final List<ToggleGroup> toggleGroups = new ArrayList<>();
    private static final Map<QualityControlRule, Button> ruleEnableButtons = new HashMap<>();
    private static final JsonFactory FACTORY = new MappingJsonFactory();

    private final TableColumn<QualityControlEvent, QualityControlEvent> identifierColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> typeColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> qualityColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> reasonColumn;
    private final TableView<QualityControlEvent> qualityTable;
    private final FlowPane optionsPane;

    private static final String DISABLE = "Disable";
    private static final String ENABLE = "Enable";
    private static boolean firstClick = true;

    public QualityControlViewPane() {
        readSerializedRulePriorities();
        readSerializedRuleEnabledStatuses();

        qualityTable = new TableView<>();
        qualityTable.focusedProperty().addListener((observable, oldValue, newValue) -> firstClick = true);

        identifierColumn = new TableColumn<>("Identifier");
        identifierColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.25));
        identifierColumn.setComparator((qce1, qce2) -> {
            final String ns1 = qce1.getIdentifier() != null ? qce1.getIdentifier() : "";
            final String ns2 = qce2.getIdentifier() != null ? qce2.getIdentifier() : "";
            return ns1.compareTo(ns2);
        });

        typeColumn = new TableColumn<>("Type");
        typeColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.2));
        typeColumn.setComparator((qce1, qce2) -> {
            final String nr1 = qce1.getType() != null ? qce1.getType() : "";
            final String nr2 = qce2.getType() != null ? qce2.getType() : "";
            return nr1.compareTo(nr2);
        });

        qualityColumn = new TableColumn<>("Category");
        qualityColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.15));
        qualityColumn.setComparator((qce1, qce2) -> QualityControlRule.testPriority(qce1.getCategory(), qce2.getCategory()));
        qualityColumn.setSortType(TableColumn.SortType.DESCENDING);

        reasonColumn = new TableColumn<>("Reasons");
        reasonColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.40));
        reasonColumn.setComparator((qce1, qce2) -> {
            final String nr1 = qce1.getReasons() != null ? qce1.getReasons() : "";
            final String nr2 = qce2.getReasons() != null ? qce2.getReasons() : "";
            return nr1.compareTo(nr2);
        });
        qualityTable.getColumns().add(identifierColumn);
        qualityTable.getColumns().add(typeColumn);
        qualityTable.getColumns().add(qualityColumn);
        qualityTable.getColumns().add(reasonColumn);
        qualityTable.getSortOrder().add(qualityColumn);
        qualityTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        qualityTable.setPlaceholder(wrappedLabel(Bundle.MSG_SelectSomething()));
        setCenter(qualityTable);

        optionsPane = new FlowPane();
        optionsPane.setId("qualitycontrolview-flow-pane");
        optionsPane.setAlignment(Pos.CENTER);
        final Button deleteButton = new Button("Delete From Graph");
        deleteButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new DeleteQualityControlEvents(qualitycontrolEvents))
                    .executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });

        final Button selectButton = new Button("Select On Graph");
        selectButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new SelectQualityControlEvents(qualitycontrolEvents))
                    .executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });

        final Button removeButton = new Button("Deselect On Graph");
        removeButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new DeselectQualityControlEvents(qualitycontrolEvents))
                    .executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });

        final Button zoomButton = new Button("Zoom On Graph");
        zoomButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new ZoomToQualityControlEvents(qualitycontrolEvents))
                    .executeLater(GraphManager.getDefault().getActiveGraph());
        });

        final Button priorityButton = new Button("Category Priority");
        priorityButton.setOnAction(event -> showPriorityDialog());

        // create help button
        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.paddingProperty().set(new Insets(2, 0, 0, 0));
        helpButton.setTooltip(new Tooltip("Display help for Quality Control View"));
        helpButton.setOnAction(event -> new HelpCtx(QualityControlViewTopComponent.class.getName()).display());
        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");

        optionsPane.getChildren().addAll(deleteButton, selectButton, removeButton, zoomButton, priorityButton, helpButton);

        setBottom(optionsPane);

        this.setId("qualitycontrolview-border-pane");
        this.setPadding(new Insets(5));
    }

    public TableView<QualityControlEvent> getQualityTable() {
        return qualityTable;
    }

    /**
     * Refresh the data inside QualityControlView with data from the current graph.
     *
     * @param state The new state to display in the view.
     */
    public void refreshQualityControlView(final QualityControlState state) {
        Platform.runLater(() -> {
            final ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(50, 50);
            setCenter(progress);
        });

        final String graphId = state != null ? state.getGraphId() : null;

        Platform.runLater(() -> {
            // The data is being changed.
            // If there are any selections in the table, they almost certainly have no relevance to the new data.
            // This leads to (for example) 10 things being selected, but only 5 things in the table, with nasty results.
            // Therefore, just clear the selections.
            // We should probably save the selection somewhere so when the user switches back they have the same things selected,
            // but that can be a future feature request.
            qualityTable.getSelectionModel().clearSelection();

            for (final TableColumn column : qualityTable.getColumns()) {
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QualityControlEvent, QualityControlEvent>, ObservableValue<QualityControlEvent>>() {
                    @Override
                    public ObservableValue<QualityControlEvent> call(final TableColumn.CellDataFeatures<QualityControlEvent, QualityControlEvent> p) {
                        return new SimpleObjectProperty<>(p.getValue());
                    }
                });
            }

            identifierColumn.setCellFactory(p -> {
                final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                    @Override
                    public void updateItem(final QualityControlEvent item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getIdentifier());
                            setStyle(qualityStyle(item.getCategory()));
                        }
                    }
                };

                cell.setOnMouseClicked(value -> {
                    if (value.getClickCount() >= 2 && !firstClick) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                    firstClick = false;
                });

                return cell;
            });

            typeColumn.setCellFactory(p -> {
                final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                    @Override
                    public void updateItem(final QualityControlEvent item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getType());
                            setStyle(qualityStyle(item.getCategory()));
                        }
                    }
                };

                cell.setOnMouseClicked(value -> {
                    if (value.getClickCount() >= 2 && !firstClick) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                    firstClick = false;
                });

                return cell;
            });

            qualityColumn.setCellFactory(p -> {
                final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                    @Override
                    public void updateItem(final QualityControlEvent item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(String.valueOf(item.getCategory().name()));
                            setAlignment(Pos.CENTER);
                            setStyle(qualityStyle(item.getCategory()));
                        }
                    }
                };

                cell.setOnMouseClicked(value -> {
                    if (value.getClickCount() >= 2 && !firstClick) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                    firstClick = false;
                });

                return cell;
            });

            reasonColumn.setCellFactory(p -> {
                final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                    @Override
                    public void updateItem(final QualityControlEvent item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getReasons());
                            setStyle(qualityStyle(item.getCategory()));
                        }
                    }
                };

                cell.setOnMouseClicked(value -> {
                    if (value.getClickCount() >= 2 && !firstClick) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                    firstClick = false;
                });

                return cell;
            });

            qualityTable.setItems(state != null
                    ? FXCollections.observableArrayList(state.getQualityControlEvents())
                    : FXCollections.emptyObservableList());

            final String displayName = graphId != null && GraphNode.getGraphNode(graphId) != null
                    ? GraphNode.getGraphNode(graphId).getDisplayName()
                    : "a graph";
            qualityTable.setPlaceholder(wrappedLabel(String.format(Bundle.MSG_SelectSomething(), displayName)));

            setCenter(qualityTable);
        });
    }

    /**
     * Create a javafx style based on the given quality category.
     *
     * @param category the quality category.
     * @return a javafx style based on the given quality value.
     */
    public static String qualityStyle(final QualityCategory category) {
        return qualityStyle(category, 0.75F);
    }

    /**
     * Create a javafx style based on the given quality and alpha values.
     *
     * @param category the quality.
     * @param alpha the alpha value.
     * @return a javafx style based on the given quality and alpha values.
     */
    public static String qualityStyle(final QualityCategory category, final float alpha) {
        final int intensity;
        final String style;
        switch (category) {
            case MINOR:
                style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(90,150,255,%f);", alpha);
                break;
            case MEDIUM:
                style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,215,0,%f);", alpha);
                break;
            case MAJOR:
                intensity = 255 - (255 * QualityControlEvent.MAJOR_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(255,%d,0,%f);", intensity, alpha);
                break;
            case SEVERE:
                intensity = 255 - (255 * QualityControlEvent.SEVERE_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,%d,%d,%f);", intensity, intensity, alpha);
                break;
            case CRITICAL:
                intensity = 255 - (255 * QualityControlEvent.CRITICAL_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(150,%d,%d,%f);", intensity, intensity, alpha);
                break;
            default:
                // DEFAULT case
                style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(0,200,0,%f);", alpha);
                break;
        }
        return style;
    }

    /**
     * Shows a dialog to allow the user to select priorities of each rule.
     */
    private void showPriorityDialog() {
        final ScrollPane rulesScrollPane = new ScrollPane();
        rulesScrollPane.setPrefHeight(240);
        rulesScrollPane.setPrefWidth(SystemUtils.IS_OS_LINUX ? 820 : 700);
        rulesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rulesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final GridPane buttonGrid = new GridPane();
        buttonGrid.prefWidthProperty().bind(rulesScrollPane.widthProperty());
        buttonGrid.setPadding(new Insets(5, 0, 5, 0));

        // Setting headings
        int rowCount = 0;
        final Label ruleLabel = new Label("Rule");
        ruleLabel.setPadding(new Insets(0, 100, 10, 5));
        buttonGrid.add(ruleLabel, 0, rowCount);

        final Label minorLabel = new Label("MINOR");
        minorLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(minorLabel, 1, rowCount);

        final Label mediumLabel = new Label("MEDIUM");
        mediumLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(mediumLabel, 2, rowCount);

        final Label majorLabel = new Label("MAJOR");
        majorLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(majorLabel, 3, rowCount);

        final Label severeLabel = new Label("SEVERE");
        severeLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(severeLabel, 4, rowCount);

        final Label criticalLabel = new Label("CRITICAL");
        criticalLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(criticalLabel, 5, rowCount);
        rowCount++;

        // Setting rule names and buttons
        for (final QualityControlRule rule : Lookup.getDefault().lookupAll(QualityControlRule.class)) {
            final ToggleGroup ruleGroup = new ToggleGroup();
            final Label ruleName = new Label(rule.getName());
            final RadioButton minorButton = new RadioButton();
            final RadioButton mediumButton = new RadioButton();
            final RadioButton majorButton = new RadioButton();
            final RadioButton severeButton = new RadioButton();
            final RadioButton criticalButton = new RadioButton();
            final String resetText = rule.getCategory(0) == getPriorities().get(rule) ? "Reset" : "Reset to " + rule.getCategory(0).name();

            final Button resetButton = new Button(resetText);
            resetButton.setOnAction(event -> {
                switch (rule.getCategory(0)) {
                    case MINOR:
                        minorButton.setSelected(true);
                        break;
                    case MEDIUM:
                        mediumButton.setSelected(true);
                        break;
                    case MAJOR:
                        majorButton.setSelected(true);
                        break;
                    case SEVERE:
                        severeButton.setSelected(true);
                        break;
                    case CRITICAL:
                        criticalButton.setSelected(true);
                        break;
                    default:
                        break;
                }
                resetButton.setText("Reset");
            });

            final Button enableDisableButton = new Button(Boolean.TRUE.equals(getEnablementStatuses().get(rule)) ? DISABLE : ENABLE);
            enableDisableButton.setOnAction(event -> {
                if (DISABLE.equals(enableDisableButton.getText())) {
                    enableDisableButton.setText(ENABLE);
                    ruleName.setTextFill(Color.GREY);
                    minorButton.setDisable(true);
                    mediumButton.setDisable(true);
                    majorButton.setDisable(true);
                    severeButton.setDisable(true);
                    criticalButton.setDisable(true);
                    resetButton.setDisable(true);
                } else {
                    enableDisableButton.setText(DISABLE);
                    ruleName.setTextFill(Color.color(0.196, 0.196, 0.196));
                    minorButton.setDisable(false);
                    mediumButton.setDisable(false);
                    majorButton.setDisable(false);
                    severeButton.setDisable(false);
                    criticalButton.setDisable(false);
                    resetButton.setDisable(false);
                }
            });

            getPriorities().putIfAbsent(rule, rule.getCategory(0));
            // setting the selection based on the current priority
            switch (getPriorities().get(rule)) {
                case MINOR:
                    minorButton.setSelected(true);
                    break;
                case MEDIUM:
                    mediumButton.setSelected(true);
                    break;
                case MAJOR:
                    majorButton.setSelected(true);
                    break;
                case SEVERE:
                    severeButton.setSelected(true);
                    break;
                case CRITICAL:
                    criticalButton.setSelected(true);
                    break;
                default:
                    break;
            }

            if (Boolean.FALSE.equals(getEnablementStatuses().get(rule))) {
                ruleName.setTextFill(Color.GREY);
                minorButton.setDisable(true);
                mediumButton.setDisable(true);
                majorButton.setDisable(true);
                severeButton.setDisable(true);
                criticalButton.setDisable(true);
                resetButton.setDisable(true);
            }

            toggleGroups.add(ruleGroup);
            ruleEnableButtons.put(rule, enableDisableButton);

            GridPane.setHalignment(ruleName, HPos.LEFT);
            GridPane.setHalignment(minorButton, HPos.CENTER);
            GridPane.setHalignment(mediumButton, HPos.CENTER);
            GridPane.setHalignment(majorButton, HPos.CENTER);
            GridPane.setHalignment(severeButton, HPos.CENTER);
            GridPane.setHalignment(criticalButton, HPos.CENTER);
            GridPane.setHalignment(resetButton, HPos.CENTER);
            GridPane.setHalignment(enableDisableButton, HPos.CENTER);

            ruleName.setPadding(new Insets(0, 0, 5, 5));
            minorButton.setPadding(new Insets(0, 10, 5, 10));
            mediumButton.setPadding(new Insets(0, 10, 5, 10));
            majorButton.setPadding(new Insets(0, 10, 5, 10));
            severeButton.setPadding(new Insets(0, 10, 5, 10));
            criticalButton.setPadding(new Insets(0, 10, 5, 10));
            resetButton.setPadding(new Insets(5, 10, 5, 10));
            enableDisableButton.setPadding(new Insets(5, 10, 5, 10));

            minorButton.setUserData(QualityCategory.MINOR);
            mediumButton.setUserData(QualityCategory.MEDIUM);
            majorButton.setUserData(QualityCategory.MAJOR);
            severeButton.setUserData(QualityCategory.SEVERE);
            criticalButton.setUserData(QualityCategory.CRITICAL);

            minorButton.setToggleGroup(ruleGroup);
            mediumButton.setToggleGroup(ruleGroup);
            majorButton.setToggleGroup(ruleGroup);
            severeButton.setToggleGroup(ruleGroup);
            criticalButton.setToggleGroup(ruleGroup);

            ruleGroup.setUserData(rule);

            buttonGrid.add(ruleName, 0, rowCount);
            buttonGrid.add(minorButton, 1, rowCount);
            buttonGrid.add(mediumButton, 2, rowCount);
            buttonGrid.add(majorButton, 3, rowCount);
            buttonGrid.add(severeButton, 4, rowCount);
            buttonGrid.add(criticalButton, 5, rowCount);
            buttonGrid.add(resetButton, 6, rowCount);
            buttonGrid.add(enableDisableButton, 7, rowCount);

            rowCount++;
        }

        rulesScrollPane.setContent(buttonGrid);

        final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Select Rule Priorities", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Select Rule Priorities");
        alert.setHeaderText("Customise the priority of rules");
        alert.getDialogPane().setContent(rulesScrollPane);
        alert.setResizable(true);

        if (alert.showAndWait().get() == ButtonType.OK) {
            for (final ToggleGroup tg : toggleGroups) {
                getPriorities().put((QualityControlRule) tg.getUserData(), (QualityCategory) tg.getSelectedToggle().getUserData());
            }
            for (final Entry<QualityControlRule, Button> entry : ruleEnableButtons.entrySet()) {
                final boolean enabled = DISABLE.equals(entry.getValue().getText());
                getEnablementStatuses().put(entry.getKey(), enabled);
                entry.getKey().setEnabled(enabled);
            }
            QualityControlAutoVetter.getInstance().updateQualityEvents();
            writeSerializedRulePriorities();
            writeSerializedRuleEnabledStatuses();
        }
    }

    /**
     * Display a dialog containing all Rule objects registered with the Quality Control View and which matched for a given QualityControlEvent.
     *
     * @param owner
     * @param qcevent
     */
    private void showRuleDialog(final TableCell<QualityControlEvent, QualityControlEvent> qcevent) {
        if (qcevent.getItem() != null) {
            final int vxId = qcevent.getItem().getVertex();
            final String identifier = qcevent.getItem().getIdentifier();
            final List<Pair<QualityCategory, String>> rules = new ArrayList<>();
            for (final QualityControlRule rule : qcevent.getItem().getRules()) {
                // Hack the name and explanation together to obviate the need for another data structure.
                final String ruleName = rule.getName() + "§" + rule.getDescription();
                final QualityCategory quality = rule.getResults().contains(vxId) ? getPriorities().get(rule) : null;
                if (quality != null) {
                    rules.add(new Pair<>(quality, ruleName));
                }
            }

            Collections.sort(rules, (p1, p2) -> {
                final int compare = QualityControlRule.testPriority(p1.getKey(), p2.getKey());
                return compare == 0 ? p1.getValue().compareTo(p2.getValue()) : compare;
            });

            showRuleDialog(identifier, rules);
        }
    }

    /**
     * Display a dialog containing all Rule objects registered with the Quality Control View and which matched for a given identifier.
     *
     * @param owner The owner Node
     * @param identifier The identifier of the graph node being displayed.
     * @param rules The list of rules measured against this graph node.
     */
    private void showRuleDialog(final String identifier, final List<Pair<QualityCategory, String>> rules) {
        final ScrollPane sp = new ScrollPane();
        sp.setPrefHeight(512);
        sp.setPrefWidth(512);
        sp.setFitToWidth(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final VBox vbox = new VBox();
        vbox.prefWidthProperty().bind(sp.widthProperty());
        vbox.setPadding(Insets.EMPTY);
        for (final Pair<QualityCategory, String> rule : rules) {
            final String[] t = rule.getValue().split("§");

            final String quality = rule.getKey().name();
            final String title = String.format("%s - %s", quality, t[0]);

            final Text content = new Text(t[1]);
            content.wrappingWidthProperty().bind(sp.widthProperty().subtract(16)); // Subtract a random number to avoid the vertical scrollbar.

            final TitledPane tp = new TitledPane(title, content);
            tp.prefWidthProperty().bind(vbox.widthProperty());
            tp.setExpanded(false);
            tp.setWrapText(true);

            vbox.getChildren().add(tp);
        }
        if (CollectionUtils.isEmpty(rules)) {
            final Label noEntriesLabel = new Label(Bundle.MSG_NoEntries());
            vbox.getChildren().add(noEntriesLabel);
        }
        sp.setContent(vbox);

        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(String.format(Bundle.MSG_QualtyControlRules(), identifier));
        alert.getDialogPane().setContent(sp);
        alert.setResizable(true);
        final List<Screen> screens = Screen.getScreensForRectangle(this.getScene().getWindow().getX(), this.getScene().getWindow().getY(),
                this.getScene().getWindow().widthProperty().get(), this.getScene().getWindow().heightProperty().get());
        
        alert.setX((screens.get(0).getVisualBounds().getMinX() + screens.get(0).getVisualBounds().getWidth() / 2) - 250); 
        alert.setY((screens.get(0).getVisualBounds().getMinY() + screens.get(0).getVisualBounds().getHeight() / 2) - 250);
        alert.show();
    }
    
    /**
     * Wrap the text in a javafx label.
     *
     * @param msg
     * @return
     */
    private static Label wrappedLabel(final String msg) {
        final Label label = new Label(msg);
        label.setWrapText(true);

        return label;
    }

    /**
     * Writes the rulePriorities to the preferences object.
     */
    private static void writeSerializedRulePriorities() {
        final String mapAsString = JsonUtilities.getMapAsString(FACTORY, getPriorities());
        if (!mapAsString.isEmpty()) {
            PREFERENCES.put(ApplicationPreferenceKeys.RULE_PRIORITIES, mapAsString);
            try {
                PREFERENCES.flush();
            } catch (final BackingStoreException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Writes the rule enabled statuses to the preferences object.
     */
    private static void writeSerializedRuleEnabledStatuses() {
        final String mapAsString = JsonUtilities.getMapAsString(FACTORY, getEnablementStatuses());
        if (!mapAsString.isEmpty()) {
            PREFERENCES.put(ApplicationPreferenceKeys.RULE_ENABLED_STATUSES, mapAsString);
            try {
                PREFERENCES.flush();
            } catch (final BackingStoreException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Reads the preferences object to load the rulePriorities.
     */
    public static void readSerializedRulePriorities() {
        getPriorities().clear();
        final Map<String, String> priorityStringMap = JsonUtilities.getStringAsMap(FACTORY, PREFERENCES.get(ApplicationPreferenceKeys.RULE_PRIORITIES, ""));
        for (final Entry<String, String> entry : priorityStringMap.entrySet()) {
            getPriorities().put(QualityControlEvent.getRuleByString(entry.getKey()), QualityControlEvent.getCategoryFromString(entry.getValue()));
        }
    }

    /**
     * Reads the preferences object to load the rule enabled statuses.
     */
    public static void readSerializedRuleEnabledStatuses() {
        getEnablementStatuses().clear();
        final Map<String, String> enableStringMap = JsonUtilities.getStringAsMap(FACTORY, PREFERENCES.get(ApplicationPreferenceKeys.RULE_ENABLED_STATUSES, ""));
        for (final Entry<String, String> entry : enableStringMap.entrySet()) {
            final QualityControlRule rule = QualityControlEvent.getRuleByString(entry.getKey());
            final boolean enabled = Boolean.parseBoolean(entry.getValue());
            getEnablementStatuses().put(rule, enabled);
            rule.setEnabled(enabled);
        }
    }

    /**
     * Lazily instantiates the rulePriorities Map and loads it via the lookup
     *
     * @return a Map<QualityControlRule, QualityCategory> of rules mapped to categories
     */
    public static Map<QualityControlRule, QualityCategory> getPriorities() {
        if (MapUtils.isEmpty(rulePriorities)) {
            rulePriorities = new HashMap<>();
            for (final QualityControlRule rule : Lookup.getDefault().lookupAll(QualityControlRule.class)) {
                rulePriorities.put(rule, rule.getCategory(0));
            }
        }
        return rulePriorities;
    }

    public static Map<QualityControlRule, Boolean> getEnablementStatuses() {
        if (MapUtils.isEmpty(ruleEnabledStatuses)) {
            ruleEnabledStatuses = new HashMap<>();
            for (final QualityControlRule rule : Lookup.getDefault().lookupAll(QualityControlRule.class)) {
                ruleEnabledStatuses.put(rule, rule.isEnabled());
            }
        }
        return ruleEnabledStatuses;
    }

    /**
     * Delete nodes in a graph matching rows selected in QualityControlView.
     */
    @PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.DELETE})
    protected static class DeleteQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public DeleteQualityControlEvents(final List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (final QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
                if (qualitycontrolEvent != null) {
                    vertexIds.add(qualitycontrolEvent.getVertex());
                }
            }

            for (final int vertexId : vertexIds) {
                graph.removeVertex(vertexId);
            }
        }

        @Override
        public String getName() {
            return "Quality Control View: Delete";
        }
    }

    /**
     * Selects on the graph only nodes which have a corresponding selected QualityControlEvent.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    protected static class SelectQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public SelectQualityControlEvents(final List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (final QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
                if (qualitycontrolEvent != null) {
                    vertexIds.add(qualitycontrolEvent.getVertex());
                }
            }

            final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
            if (vertexSelectedAttr != Graph.NOT_FOUND) {
                final int vertexCount = graph.getVertexCount();
                for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                    final int vertexId = graph.getVertex(vertexPosition);
                    graph.setBooleanValue(vertexSelectedAttr, vertexId, vertexIds.contains(vertexId));
                }
            }

            final int transactionSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
            if (transactionSelectedAttr != Graph.NOT_FOUND) {
                final int transactionCount = graph.getTransactionCount();
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getTransaction(transactionPosition);
                    graph.setBooleanValue(transactionSelectedAttr, transactionId, false);
                }
            }
        }

        @Override
        public String getName() {
            return "Quality Control View: Select";
        }
    }

    /**
     * Selects on the graph only nodes which do not have a corresponding selected QualityControlEvent.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    protected static class DeselectQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public DeselectQualityControlEvents(final List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (final QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
                if (qualitycontrolEvent != null) {
                    vertexIds.add(qualitycontrolEvent.getVertex());
                }
            }

            final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
            if (vertexSelectedAttribute != Graph.NOT_FOUND) {
                final int vertexCount = graph.getVertexCount();
                for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                    int vertexId = graph.getVertex(vertexPosition);
                    if (vertexIds.contains(vertexId)) {
                        graph.setBooleanValue(vertexSelectedAttribute, vertexId, false);
                    }
                }
            }
        }

        @Override
        public String getName() {
            return "Quality Control View: Deselect";
        }
    }

    /**
     * Zoom the camera of the Graph to the extents of nodes corresponding to any selected QualityControlEvent.
     */
    @PluginInfo(pluginType = PluginType.VIEW, tags = {PluginTags.VIEW})
    private static class ZoomToQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public ZoomToQualityControlEvents(final List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = qualitycontrolEvents;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int[] vertexIds = new int[qualitycontrolEvents.size()];
            for (int index = 0; index < qualitycontrolEvents.size(); index++) {
                vertexIds[index] = qualitycontrolEvents.get(index).getVertex();
            }

            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.ZOOM_TO_VERTICES)
                    .withParameter(ZoomToVerticesPlugin.VERTICES_PARAMETER_ID, vertexIds)
                    .executeNow(graph);
        }

        @Override
        public String getName() {
            return "Quality Control View: Zoom";
        }
    }
}

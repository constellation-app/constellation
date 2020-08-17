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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.*;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openide.util.Exceptions;
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

    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static Map<QualityControlRule, QualityCategory> rulePriorities = null;
    private static final List<ToggleGroup> toggleGroups = new ArrayList<>();
    private static final JsonFactory FACTORY = new MappingJsonFactory();
    public static Lookup lookup = null;

    private final TableColumn<QualityControlEvent, QualityControlEvent> identifierColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> typeColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> qualityColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> reasonColumn;
    private final TableView<QualityControlEvent> qualityTable;
    private final FlowPane optionsPane;

    public QualityControlViewPane() {
        readSerializedRulePriorities();

        qualityTable = new TableView<>();
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
        qualityColumn.setComparator((qce1, qce2) -> {
            return QualityControlRule.testPriority(qce1.getCategory(), qce2.getCategory());
        });
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
        final Button deleteButton = new Button("Delete from Graph");
        deleteButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new DeleteQualityControlEvents(qualitycontrolEvents)).executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });
        optionsPane.getChildren().add(deleteButton);

        final Button selectButton = new Button("Select on graph");
        selectButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new SelectQualityControlEvents(qualitycontrolEvents)).executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });
        optionsPane.getChildren().add(selectButton);

        final Button removeButton = new Button("Deselect on graph");
        removeButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new DeselectQualityControlEvents(qualitycontrolEvents)).executeLater(GraphManager.getDefault().getActiveGraph());
            qualityTable.getSelectionModel().clearSelection();
        });
        optionsPane.getChildren().add(removeButton);

        final Button zoomButton = new Button("Zoom on graph");
        zoomButton.setOnAction(event -> {
            final List<QualityControlEvent> qualitycontrolEvents = qualityTable.getSelectionModel().getSelectedItems();
            PluginExecution.withPlugin(new ZoomToQualityControlEvents(qualitycontrolEvents)).executeLater(GraphManager.getDefault().getActiveGraph());
        });
        optionsPane.getChildren().add(zoomButton);

        final Button priorityButton = new Button("Category Priority");
        priorityButton.setOnAction(event -> {
            showPriorityDialog();
        });
        optionsPane.getChildren().add(priorityButton);

        setBottom(optionsPane);

        this.setId("qualitycontrolview-border-pane");
        this.setPadding(new Insets(5));
    }

    /**
     * Refresh the data inside QualityControlView with data from the current
     * graph.
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

            for (TableColumn column : qualityTable.getColumns()) {
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QualityControlEvent, QualityControlEvent>, ObservableValue<QualityControlEvent>>() {
                    @Override
                    public ObservableValue<QualityControlEvent> call(TableColumn.CellDataFeatures<QualityControlEvent, QualityControlEvent> p) {
                        return new SimpleObjectProperty<>(p.getValue());
                    }
                });
            }

            identifierColumn.setCellFactory((TableColumn<QualityControlEvent, QualityControlEvent> p) -> {
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
                    if (value.getClickCount() == 2) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                });

                return cell;
            });

            typeColumn.setCellFactory((TableColumn<QualityControlEvent, QualityControlEvent> p) -> {
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
                    if (value.getClickCount() == 2) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                });

                return cell;
            });

            qualityColumn.setCellFactory((TableColumn<QualityControlEvent, QualityControlEvent> p) -> {
                final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                    @Override
                    public void updateItem(final QualityControlEvent item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getCategory() == QualityCategory.DEFAULT ? Bundle.MSG_NotApplicable() : String.valueOf(item.getCategory().name()));
                            setAlignment(Pos.CENTER);
                            setStyle(qualityStyle(item.getCategory()));
                        }
                    }
                };

                cell.setOnMouseClicked(value -> {
                    if (value.getClickCount() == 2) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
                });

                return cell;
            });

            reasonColumn.setCellFactory((TableColumn<QualityControlEvent, QualityControlEvent> p) -> {
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
                    if (value.getClickCount() == 2) {
                        @SuppressWarnings("unchecked") //sourceCell will be a Table cell of quality control events which extends from object type
                        final TableCell<QualityControlEvent, QualityControlEvent> sourceCell = (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource();
                        showRuleDialog(sourceCell);
                    }
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
     * Create a javafx style based on the given quality value.
     *
     * @param quality the quality.
     * @return a javafx style based on the given quality value.
     */
    public static String qualityStyle(final QualityCategory category) {
        return qualityStyle(category, 0.75f);
    }

    /**
     * Create a javafx style based on the given quality and alpha values.
     *
     * @param quality the quality.
     * @param alpha the alpha value.
     * @return a javafx style based on the given quality and alpha values.
     */
    public static String qualityStyle(final QualityCategory category, final float alpha) {
        final String whiteText = "-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(%d,%d,255,%f);";
        final int intensity;
        final String style;
        switch (category) {
            case INFO:
                intensity = 255 - (255 * QualityControlEvent.INFO_VALUE) / 100;
                style = String.format(whiteText, intensity, intensity, alpha);
                break;
            case WARNING:
                intensity = 255 - (255 * QualityControlEvent.WARNING_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(%d,%d,255,%f);", intensity, intensity, alpha);
                break;
            case SEVERE:
                intensity = 255 - (255 * QualityControlEvent.SEVERE_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,%d,%d,%f);", intensity, intensity, alpha);
                break;
            case FATAL:
                intensity = 255 - (255 * QualityControlEvent.FATAL_VALUE) / 100;
                style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(0,%d,%d,%f);", intensity, intensity, alpha);
                break;
            default:
                // DEFAULT case
                intensity = 255 - (255 * QualityControlEvent.DEFAULT_VALUE) / 100;
                style = String.format(whiteText, intensity, intensity, alpha);
                break;
        }

        return style;
    }

    /**
     * Shows a dialog to allow the user to select priorities of each rule.
     */
    private static void showPriorityDialog() {
        final ScrollPane rulesScrollPane = new ScrollPane();
        rulesScrollPane.setPrefHeight(240);
        rulesScrollPane.setPrefWidth(700);
        rulesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rulesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final GridPane buttonGrid = new GridPane();
        buttonGrid.prefWidthProperty().bind(rulesScrollPane.widthProperty());
        buttonGrid.setPadding(new Insets(5, 0, 5, 0));

        // Setting headings
        int rowCount = 0;
        final Label ruleLabel = new Label("Rule");
        ruleLabel.setPadding(new Insets(0, 100, 10, 5));
        buttonGrid.add(ruleLabel, 0, rowCount);

        final Label defaultLabel = new Label("DEFAULT");
        defaultLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(defaultLabel, 1, rowCount);

        final Label infoLabel = new Label("INFO");
        infoLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(infoLabel, 2, rowCount);

        final Label warningLabel = new Label("WARNING");
        warningLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(warningLabel, 3, rowCount);

        final Label severeLabel = new Label("SEVERE");
        severeLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(severeLabel, 4, rowCount);

        final Label fatalLabel = new Label("FATAL");
        fatalLabel.setPadding(new Insets(0, 10, 10, 10));
        buttonGrid.add(fatalLabel, 5, rowCount);
        rowCount++;

        // Setting rule names and buttons
        for (final QualityControlRule rule : getLookup().lookupAll(QualityControlRule.class)) {
            final ToggleGroup ruleGroup = new ToggleGroup();
            final Label ruleName = new Label(rule.getName());
            final RadioButton defaultButton = new RadioButton();
            final RadioButton infoButton = new RadioButton();
            final RadioButton warningButton = new RadioButton();
            final RadioButton severeButton = new RadioButton();
            final RadioButton fatalButton = new RadioButton();
            final Button resetButton = new Button("Reset");
            resetButton.setOnAction(event -> {
                switch (rule.getCategory(0)) {
                    case DEFAULT:
                        defaultButton.setSelected(true);
                        break;
                    case INFO:
                        infoButton.setSelected(true);
                        break;
                    case WARNING:
                        warningButton.setSelected(true);
                        break;
                    case SEVERE:
                        severeButton.setSelected(true);
                        break;
                    case FATAL:
                        fatalButton.setSelected(true);
                        break;
                    default:
                        break;
                }
            });

            getPriorities().putIfAbsent(rule, rule.getCategory(0));
            // setting the selection based on the current priority
            switch (getPriorities().get(rule)) {
                case DEFAULT:
                    defaultButton.setSelected(true);
                    break;
                case INFO:
                    infoButton.setSelected(true);
                    break;
                case WARNING:
                    warningButton.setSelected(true);
                    break;
                case SEVERE:
                    severeButton.setSelected(true);
                    break;
                case FATAL:
                    fatalButton.setSelected(true);
                    break;
                default:
                    break;
            }

            toggleGroups.add(ruleGroup);

            GridPane.setHalignment(ruleName, HPos.LEFT);
            GridPane.setHalignment(defaultButton, HPos.CENTER);
            GridPane.setHalignment(infoButton, HPos.CENTER);
            GridPane.setHalignment(warningButton, HPos.CENTER);
            GridPane.setHalignment(severeButton, HPos.CENTER);
            GridPane.setHalignment(fatalButton, HPos.CENTER);
            GridPane.setHalignment(resetButton, HPos.CENTER);

            ruleName.setPadding(new Insets(0, 0, 5, 5));
            defaultButton.setPadding(new Insets(0, 10, 5, 10));
            infoButton.setPadding(new Insets(0, 10, 5, 10));
            warningButton.setPadding(new Insets(0, 10, 5, 10));
            severeButton.setPadding(new Insets(0, 10, 5, 10));
            fatalButton.setPadding(new Insets(0, 10, 5, 10));
            resetButton.setPadding(new Insets(5, 10, 5, 10));

            defaultButton.setUserData(QualityCategory.DEFAULT);
            infoButton.setUserData(QualityCategory.INFO);
            warningButton.setUserData(QualityCategory.WARNING);
            severeButton.setUserData(QualityCategory.SEVERE);
            fatalButton.setUserData(QualityCategory.FATAL);

            defaultButton.setToggleGroup(ruleGroup);
            infoButton.setToggleGroup(ruleGroup);
            warningButton.setToggleGroup(ruleGroup);
            severeButton.setToggleGroup(ruleGroup);
            fatalButton.setToggleGroup(ruleGroup);

            ruleGroup.setUserData(rule);

            buttonGrid.add(ruleName, 0, rowCount);
            buttonGrid.add(defaultButton, 1, rowCount);
            buttonGrid.add(infoButton, 2, rowCount);
            buttonGrid.add(warningButton, 3, rowCount);
            buttonGrid.add(severeButton, 4, rowCount);
            buttonGrid.add(fatalButton, 5, rowCount);
            buttonGrid.add(resetButton, 6, rowCount);

            rowCount++;
        }

        rulesScrollPane.setContent(buttonGrid);

        final Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Select Rule Priorities",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Select Rule Priorities");
        alert.setHeaderText("Customise the priority of rules");
        alert.getDialogPane().setContent(rulesScrollPane);
        alert.setResizable(true);

        if (alert.showAndWait().get() == ButtonType.OK) {
            for (final ToggleGroup tg : toggleGroups) {
                getPriorities().put((QualityControlRule) tg.getUserData(),
                        (QualityCategory) tg.getSelectedToggle().getUserData());
            }
            QualityControlAutoVetter.getInstance().updateQualityEvents();
            writeSerializedRulePriorities();
        }
    }

    /**
     * Display a dialog containing all Rule objects registered with the Quality
     * Control View and which matched for a given QualityControlEvent.
     *
     * @param owner
     * @param qcevent
     */
    private static void showRuleDialog(final TableCell<QualityControlEvent, QualityControlEvent> qcevent) {
        if (qcevent.getItem() != null) {
            final int vxId = qcevent.getItem().getVertex();
            final String identifier = qcevent.getItem().getIdentifier();
            final ArrayList<Pair<QualityCategory, String>> rules = new ArrayList<>();
            for (final QualityControlRule rule : qcevent.getItem().getRules()) {
                // Hack the name and explanation together to obviate the need for another data structure.
                final String ruleName = rule.getName() + "§" + rule.getDescription();
                final QualityCategory quality = rule.getResults().contains(vxId) ? getPriorities().get(rule) : null;
                if (quality != null) {
                    rules.add(new Pair<>(quality, ruleName));
                }
            }

            Collections.sort(rules, (final Pair<QualityCategory, String> p1, final Pair<QualityCategory, String> p2) -> {
                int compare = QualityControlRule.testPriority(p1.getKey(), p2.getKey());
                if (compare == 0) {
                    compare = p1.getValue().compareTo(p2.getValue());
                }

                return compare;
            });

            showRuleDialog(identifier, rules);
        }
    }

    /**
     * Display a dialog containing all Rule objects registered with the Quality
     * Control View and which matched for a given identifier.
     *
     * @param owner The owner Node
     * @param identifier The identifier of the graph node being displayed.
     * @param rules The list of rules measured against this graph node.
     */
    private static void showRuleDialog(final String identifier, final List<Pair<QualityCategory, String>> rules) {
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

            final String quality = rule.getKey() == QualityCategory.DEFAULT ? Bundle.MSG_NotApplicable() : "" + rule.getKey().name();
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
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Reads the preferences object to load the rulePriorities.
     */
    private static void readSerializedRulePriorities() {
        getPriorities().clear();
        final Map<String, String> priorityStringMap = JsonUtilities.getStringAsMap(FACTORY, PREFERENCES.get(ApplicationPreferenceKeys.RULE_PRIORITIES, ""));
        for (Entry<String, String> entry : priorityStringMap.entrySet()) {
            getPriorities().put(QualityControlEvent.getRuleByString(entry.getKey()), QualityControlEvent.getCategoryFromString(entry.getValue()));
        }
    }

    /**
     * Lazily instantiates the rulePriorities Map and loads it via the lookup
     *
     * @return a Map<QualityControlRule, QualityCategory> of rules mapped to
     * categories
     */
    public static Map<QualityControlRule, QualityCategory> getPriorities() {
        if (MapUtils.isEmpty(rulePriorities)) {
            rulePriorities = new HashMap<>();
            for (final QualityControlRule rule : getLookup().lookupAll(QualityControlRule.class)) {
                rulePriorities.put(rule, rule.getCategory(0));
            }
        }
        return rulePriorities;
    }

    /**
     * Lazily instantiates the lookup object
     *
     * @return the cached lookup object
     */
    public static Lookup getLookup() {
        if (lookup == null) {
            lookup = Lookup.getDefault();
        }
        return lookup;
    }

    /**
     * Delete nodes in a graph matching rows selected in QualityControlView.
     */
    private class DeleteQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public DeleteQualityControlEvents(List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
                if (qualitycontrolEvent != null) {
                    vertexIds.add(qualitycontrolEvent.getVertex());
                }
            }

            for (int vertexId : vertexIds) {
                graph.removeVertex(vertexId);
            }
        }

        @Override
        public String getName() {
            return "Quality Control View: Delete";
        }
    }

    /**
     * Selects on the graph only nodes which have a corresponding selected
     * QualityControlEvent.
     */
    private class SelectQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public SelectQualityControlEvents(List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
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
     * Selects on the graph only nodes which do not have a corresponding
     * selected QualityControlEvent.
     */
    private class DeselectQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public DeselectQualityControlEvents(List<QualityControlEvent> qualitycontrolEvents) {
            this.qualitycontrolEvents = new ArrayList<>(qualitycontrolEvents);
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final Set<Integer> vertexIds = new HashSet<>();
            for (QualityControlEvent qualitycontrolEvent : qualitycontrolEvents) {
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
     * Zoom the camera of the Graph to the extents of nodes corresponding to any
     * selected QualityControlEvent.
     */
    private class ZoomToQualityControlEvents extends SimpleEditPlugin {

        private final List<QualityControlEvent> qualitycontrolEvents;

        public ZoomToQualityControlEvents(List<QualityControlEvent> qualitycontrolEvents) {
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

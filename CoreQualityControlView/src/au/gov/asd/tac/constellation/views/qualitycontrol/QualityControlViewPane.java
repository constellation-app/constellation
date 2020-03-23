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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.*;
import au.gov.asd.tac.constellation.graph.interaction.plugins.zoom.ZoomToVerticesPlugin;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import org.openide.util.NbBundle.Messages;

/**
 * The parent pane holding all the components of the Quality Control View.
 *
 * @author cygnus_x-1
 */
@Messages({
    "MSG_NotApplicable=◇",
    "MSG_QualtyControlRules=Quality control rules for node %s",
    "MSG_SelectSomething=Select some nodes in your graph to review their data quality."
})
public final class QualityControlViewPane extends BorderPane {

    private final QualityControlViewTopComponent parent;
    private final TableView<QualityControlEvent> qualityTable;
    private final TableColumn<QualityControlEvent, QualityControlEvent> identifierColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> typeColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> qualityColumn;
    private final TableColumn<QualityControlEvent, QualityControlEvent> reasonColumn;
    private final FlowPane optionsPane;

    public QualityControlViewPane(QualityControlViewTopComponent topComponent) {
        this.parent = topComponent;

        qualityTable = new TableView<>();
        identifierColumn = new TableColumn<>("Identifier");
        identifierColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.35));
        identifierColumn.setComparator((qce1, qce2) -> {
            final String ns1 = qce1.getIdentifier() != null ? qce1.getIdentifier() : "";
            final String ns2 = qce2.getIdentifier() != null ? qce2.getIdentifier() : "";
            return ns1.compareTo(ns2);
        });

        typeColumn = new TableColumn<>("Type");
        typeColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.15));
        typeColumn.setComparator((qce1, qce2) -> {
            final String nr1 = qce1.getType() != null ? qce1.getType() : "";
            final String nr2 = qce2.getType() != null ? qce2.getType() : "";
            return nr1.compareTo(nr2);
        });

        qualityColumn = new TableColumn<>("Score");
        qualityColumn.prefWidthProperty().bind(qualityTable.widthProperty().multiply(0.10));
        qualityColumn.setComparator((qce1, qce2) -> {
            return Integer.compare(qce1.getQuality(), qce2.getQuality());
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

            identifierColumn.setCellFactory(new Callback<TableColumn<QualityControlEvent, QualityControlEvent>, TableCell<QualityControlEvent, QualityControlEvent>>() {
                @Override
                public TableCell<QualityControlEvent, QualityControlEvent> call(TableColumn<QualityControlEvent, QualityControlEvent> p) {
                    final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                        @Override
                        public void updateItem(final QualityControlEvent item, final boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getIdentifier());
                                setStyle(qualityStyle(item.getQuality()));
                            }
                        }
                    };

                    cell.setOnMouseClicked((value) -> {
                        if (value.getClickCount() == 2) {
                            showRuleDialog(qualityTable, (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource());
                        }
                    });

                    return cell;
                }
            });

            typeColumn.setCellFactory(new Callback<TableColumn<QualityControlEvent, QualityControlEvent>, TableCell<QualityControlEvent, QualityControlEvent>>() {
                @Override
                public TableCell<QualityControlEvent, QualityControlEvent> call(TableColumn<QualityControlEvent, QualityControlEvent> p) {
                    final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                        @Override
                        public void updateItem(final QualityControlEvent item, final boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getType());
                                setStyle(qualityStyle(item.getQuality()));
                            }
                        }
                    };

                    cell.setOnMouseClicked((value) -> {
                        if (value.getClickCount() == 2) {
                            showRuleDialog(qualityTable, (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource());
                        }
                    });

                    return cell;
                }
            });

            qualityColumn.setCellFactory(new Callback<TableColumn<QualityControlEvent, QualityControlEvent>, TableCell<QualityControlEvent, QualityControlEvent>>() {
                @Override
                public TableCell<QualityControlEvent, QualityControlEvent> call(TableColumn<QualityControlEvent, QualityControlEvent> p) {
                    final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                        @Override
                        public void updateItem(final QualityControlEvent item, final boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getQuality() == 0 ? Bundle.MSG_NotApplicable() : String.valueOf(item.getQuality()));
                                setAlignment(item.getQuality() == 0 ? Pos.CENTER : Pos.CENTER_RIGHT);
                                setStyle(qualityStyle(item.getQuality()));
                            }
                        }
                    };

                    cell.setOnMouseClicked((value) -> {
                        if (value.getClickCount() == 2) {
                            showRuleDialog(qualityTable, (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource());
                        }
                    });

                    return cell;
                }
            });

            reasonColumn.setCellFactory(new Callback<TableColumn<QualityControlEvent, QualityControlEvent>, TableCell<QualityControlEvent, QualityControlEvent>>() {
                @Override
                public TableCell<QualityControlEvent, QualityControlEvent> call(TableColumn<QualityControlEvent, QualityControlEvent> p) {
                    final TableCell<QualityControlEvent, QualityControlEvent> cell = new TableCell<QualityControlEvent, QualityControlEvent>() {
                        @Override
                        public void updateItem(final QualityControlEvent item, final boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getReasons());
                                setStyle(qualityStyle(item.getQuality()));
                            }
                        }
                    };

                    cell.setOnMouseClicked((value) -> {
                        if (value.getClickCount() == 2) {
                            showRuleDialog(qualityTable, (TableCell<QualityControlEvent, QualityControlEvent>) value.getSource());
                        }
                    });

                    return cell;
                }
            });

            if (state != null) {
                qualityTable.setItems(FXCollections.observableArrayList(state.getQualityControlEvents()));
            }

            final String displayName = graphId != null ? GraphNode.getGraphNode(graphId).getDisplayName() : "a graph";
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
    public static String qualityStyle(final int quality) {
        return qualityStyle(quality, 0.75f);
    }

    /**
     * Create a javafx style based on the given quality and alpha values.
     *
     * @param quality the quality.
     * @param alpha the alpha value.
     * @return a javafx style based on the given quality and alpha values.
     */
    public static String qualityStyle(final int quality, final float alpha) {
        final int intensity = 255 - (255 * quality) / 100;
        final String style;

        if (quality >= 60 && quality < 90) {
            style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(%d,%d,255,%f);", intensity, intensity, alpha);
        } else if (quality >= 95) {
            style = String.format("-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(0,%d,%d,%f);", intensity, intensity, alpha);
        } else if (quality >= 90) {
            style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(255,%d,%d,%f);", intensity, intensity, alpha);
        } else {
            style = String.format("-fx-text-fill: rgb(0,0,0);-fx-background-color: rgba(%d,%d,255,%f);", intensity, intensity, alpha);
        }

        return style;
    }

    /**
     * Display a dialog containing all Rule objects registered with the Quality
     * Control View and which matched for a given QualityControlEvent.
     *
     * @param owner
     * @param qcevent
     */
    private static void showRuleDialog(final Node owner, final TableCell<QualityControlEvent, QualityControlEvent> qcevent) {
        if (qcevent.getItem() != null) {
            final int vxId = qcevent.getItem().getVertex();
            final String identifier = qcevent.getItem().getIdentifier();
            final ArrayList<Pair<Integer, String>> rules = new ArrayList<>();
            for (final QualityControlRule rule : qcevent.getItem().getRules()) {
                // Hack the name and explanation together to obviate the need for another data structure.
                final String ruleName = rule.getName() + "§" + rule.getDescription();
                final int quality = rule.getResults().contains(vxId) ? rule.getQuality(vxId) : 0;
                rules.add(new Pair<>(quality, ruleName));
            }

            Collections.sort(rules, (final Pair<Integer, String> p1, final Pair<Integer, String> p2) -> {
                int compare = Integer.compare(p2.getKey(), p1.getKey());
                if (compare == 0) {
                    compare = p1.getValue().compareTo(p2.getValue());
                }

                return compare;
            });

            showRuleDialog(owner, identifier, rules);
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
    private static void showRuleDialog(final Node owner, final String identifier, final List<Pair<Integer, String>> rules) {
        final ScrollPane sp = new ScrollPane();
        sp.setPrefHeight(512);
        sp.setPrefWidth(512);
        sp.setFitToWidth(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        final VBox vbox = new VBox();
        vbox.prefWidthProperty().bind(sp.widthProperty());
        vbox.setPadding(Insets.EMPTY);
        for (final Pair<Integer, String> rule : rules) {
            final String[] t = rule.getValue().split("§");

            final String quality = rule.getKey() == 0 ? Bundle.MSG_NotApplicable() : "" + rule.getKey();
            final String title = String.format("%s - %s", quality, t[0]);

            final Text content = new Text(t[1]);
            content.wrappingWidthProperty().bind(sp.widthProperty().subtract(16)); // Subtract a random number to avoid the vertical scrollbar.

            final TitledPane tp = new TitledPane(title, content);
            tp.prefWidthProperty().bind(vbox.widthProperty());
            tp.setExpanded(false);
            tp.setWrapText(true);

            vbox.getChildren().add(tp);
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
                    if (vertexIds.contains(vertexId)) {
                        graph.setBooleanValue(vertexSelectedAttr, vertexId, true);
                    } else {
                        graph.setBooleanValue(vertexSelectedAttr, vertexId, false);
                    }
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

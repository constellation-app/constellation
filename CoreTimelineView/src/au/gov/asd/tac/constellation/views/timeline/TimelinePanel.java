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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.timeline.clustering.ClusteringManager;
import au.gov.asd.tac.constellation.views.timeline.clustering.TreeElement;
import au.gov.asd.tac.constellation.views.timeline.clustering.TreeLeaf;
import au.gov.asd.tac.constellation.views.timeline.components.Cluster;
import au.gov.asd.tac.constellation.views.timeline.components.Interaction;
import au.gov.asd.tac.constellation.views.timeline.components.TimelineChart;
import au.gov.asd.tac.constellation.views.timeline.components.Transaction;
import au.gov.asd.tac.constellation.views.timeline.components.Vertex;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author betelgeuse
 */
@Messages({
    "DateTimeLabel=Temporal Attribute:",
    "ShowLabels=Show Node Labels",
    "NodeLabelsCaption=Labels",
    "NoAttributes=<No Valid Attributes>",
    "SelectLabelAttribute=<Select Label Attr>",
    "Lbl_ShowSelectedOnly=Show selected only",
    "ZoomtoSelection=Zoom to Selection",
    "DimNodesLabel=Dim Excluded Nodes",
    "HideNodesLabel=Hide Excluded Nodes",
    "ShowNodesLabel=Show Excluded Nodes"
})
public class TimelinePanel extends Region {

    private static final char ARROW_CHAR = 0x2192;

    private static final int MIN_CLUSTER_WIDTH = 14; // Min width of 14 pixels which matches the min label width.
    private final TimelineTopComponent coordinator;
    private final AnchorPane innerPane = new AnchorPane();
    private final ToolBar toolbar;
    private final Label lowerTime;
    private final Label upperTime;
    private String nodeLabelAttr = null;
    private final ClusteringManager clusteringManager = new ClusteringManager();
    private TimelineChart timeline;
    private ComboBox<String> cmbDatetimeAttributes;
    private ComboBox<String> cmbAttributeNames;
    private ComboBox<String> cmbExcludedNodes;
    private ToggleButton selectedOnlyButton;
    private ToggleButton btnShowLabels;
    private ComboBox<ZoneId> timeZoneComboBox;
    private long expectedvxMod = Long.MIN_VALUE;
    private long expectedtxMod = Long.MIN_VALUE;

    final BorderPane timelinePane = new BorderPane();

    Thread updateTimelineThread = null;

    /*
     If there is no color set for a node or transaction then set it to clouds.
     */
    private static final Color FALLBACK_COLOR = ConstellationColor.CLOUDS.getJavaFXColor();

    /**
     * Constructs a new TimelinePanel, and sets the parent top component as its coordinator.
     *
     * @param coordinator The top component that manages and liaises with both the timeline and histogram components.
     *
     * @see TimelineTopComponent
     */
    public TimelinePanel(final TimelineTopComponent coordinator) {
        // Assign the parent topcomponent:
        this.coordinator = coordinator;

        // Set the layout constraints:
        this.setMaxHeight(Double.MAX_VALUE);
        this.setMinWidth(440D);
        this.setMaxWidth(Double.MAX_VALUE);

        // Create the timeline component:
        timeline = new TimelineChart(this, new NumberAxis(), new NumberAxis());

        // Create all of the components:
        toolbar = createToolBar();
        lowerTime = createExtentLabel();
        lowerTime.textProperty().bind(timeline.lowerTimeExtentProperty());
        upperTime = createExtentLabel();
        upperTime.textProperty().bind(timeline.upperTimeExtentProperty());
        lowerTime.setId("extentLabel");
        upperTime.setId("extentLabel");
        // Create and layout the panels that will hold the components for the timeline:
        doLayout();
    }

    public TimelineTopComponent getCoordinator() {
        return coordinator;
    }

    // <editor-fold defaultstate="collapsed" desc="Layout Layers">
    /**
     * Creates organises the TimelinePanel's layers.
     */
    protected void doLayout() {
        // Layer that contains the timelinechart component:

        timelinePane.setCenter(timeline);
        // The layer that contains the time extent labels:
        final BorderPane labelsPane = new BorderPane();
        BorderPane.setAlignment(lowerTime, Pos.CENTER);
        BorderPane.setAlignment(upperTime, Pos.CENTER);
        BorderPane.setMargin(lowerTime, new Insets(-32.0, -40.0, 0.0, -60.0));
        BorderPane.setMargin(upperTime, new Insets(-32.0, -60.0, 0.0, -40.0));
        labelsPane.setLeft(lowerTime);
        labelsPane.setRight(upperTime);
        labelsPane.setMouseTransparent(true);

        // Layer that combines the newly constructed time-extents with the timeline layer:
        final StackPane stackPane = new StackPane();
        StackPane.setAlignment(labelsPane, Pos.CENTER);
        StackPane.setAlignment(timelinePane, Pos.CENTER);
        stackPane.setPadding(Insets.EMPTY);
        stackPane.getChildren().addAll(timelinePane, labelsPane);

        // Layout the menu bar and the timeline object:
        final VBox vbox = new VBox();
        VBox.setVgrow(toolbar, Priority.NEVER);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        vbox.getChildren().addAll(toolbar, stackPane);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setFillWidth(true);

        // Organise the inner pane:
        AnchorPane.setTopAnchor(vbox, 0D);
        AnchorPane.setBottomAnchor(vbox, 0D);
        AnchorPane.setLeftAnchor(vbox, 0D);
        AnchorPane.setRightAnchor(vbox, 0D);
        innerPane.getChildren().add(vbox);
        innerPane.prefHeightProperty().bind(super.heightProperty());
        innerPane.prefWidthProperty().bind(super.widthProperty());

        // Attach the inner pane to the root:
        this.getChildren().add(innerPane);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Populate and De-Populate Timeline">
    public void populateFromGraph(final GraphReadMethods graph, final String dateTimeAttribute,
            final String nodeLabelAttr, final boolean selectedOnly, final ZoneId zoneId) {
        this.nodeLabelAttr = nodeLabelAttr;

        final TimeExtents te = clusteringManager.generateTree(graph, dateTimeAttribute, selectedOnly);

        if (te != null) {
            final double paddingFactor = 0.05;
            double unadjustedLowerTimeExtent = te.lowerTimeExtent();
            double unadjustedUpperTimeExtent = te.upperTimeExtent();
            if (unadjustedLowerTimeExtent == unadjustedUpperTimeExtent) {
                unadjustedLowerTimeExtent -= 5000;
                unadjustedUpperTimeExtent += 5000;
            }
            final double deltaTimeExtent = unadjustedUpperTimeExtent - unadjustedLowerTimeExtent;
            final double padding = deltaTimeExtent * paddingFactor;
            // This is adding 1% padding on the left and right so that the data is actually visible.
            final double adjustedLowerTimeExtent = unadjustedLowerTimeExtent - padding;
            final double adjustedUpperTimeExtent = unadjustedUpperTimeExtent + padding;
            this.setTimelineExtent(graph, adjustedLowerTimeExtent, adjustedUpperTimeExtent, selectedOnly, zoneId);

        } else {
            this.setTimelineExtent(graph, 0, System.currentTimeMillis(), selectedOnly, zoneId);
        }
    }

    // Wrapper function for updateTimelineWorker, allowing it to be interrupted and restarted
    // updateTimelineWorker can be a lengthy function depending on the amount of data,
    // so allowing it to be interrupted prevents running out of memory issues and overall responsivness
    public void updateTimeline(final GraphReadMethods graph, final boolean selectedOnly, final ZoneId zoneId) {
        // If thread still running, stop and restart
        if (updateTimelineThread != null && updateTimelineThread.isAlive()) {
            updateTimelineThread.interrupt();
            updateTimelineThread = null;
        }

        // At this stage, thread has to be stopped/finished. So start again
        updateTimelineThread = new Thread() {
            @Override
            public void run() {
                updateTimelineWorker(graph, selectedOnly, zoneId);
            }
        };

        updateTimelineThread.start();
    }

    private void updateTimelineWorker(final GraphReadMethods graph, final boolean selectedOnly, final ZoneId zoneId) {
        final ObservableList<XYChart.Data<Number, Number>> listOfNodeItems = FXCollections.observableArrayList();

        // Graph attribute ids:
        final String colorAttrDesc = new ColorAttributeDescription().getName();
        final int colorTransAttr = graph.getAttribute(GraphElementType.TRANSACTION, colorAttrDesc);
        final int colorVertAttr = graph.getAttribute(GraphElementType.VERTEX, colorAttrDesc);
        final int selectedTransAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        final int selectedVertAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int labelVertAttr = graph.getAttribute(GraphElementType.VERTEX, nodeLabelAttr);
        long lowestObservedY = Long.MAX_VALUE;
        long highestObservedY = Long.MIN_VALUE;

        // Check if thread has been interrupted
        if (Thread.interrupted()) {
            return;
        }

        // Helps with out of memory issues
        Platform.runLater(() -> {
            clearTimelineData(); // requires platform runlater
        });

        //for (final TreeElement element : clusteringManager.getElementsToDraw()) {
        final Object[] elementsAsArray = clusteringManager.getElementsToDraw().toArray();
        for (Object elementsAsArray1 : elementsAsArray) {
            final TreeElement element = (TreeElement) elementsAsArray1;

            // Check if thread has been interrupted
            if (Thread.interrupted()) {
                return;
            }
            XYChart.Data<Number, Number> nodeItem = element.getNodeItem();
            if (nodeItem == null) {
                if (element instanceof TreeLeaf leaf) {
                    final int transactionID = leaf.getId();

                    // Get the color for this transaction:
                    ConstellationColor col = ConstellationColor.getColorValue(graph.getStringValue(colorTransAttr, transactionID));
                    Color transColor = col != null ? col.getJavaFXColor() : FALLBACK_COLOR;

                    // Get the selection status for the transaction:
                    final boolean transSelected = graph.getBooleanValue(selectedTransAttr, transactionID);

                    // Get the source and destination vertices, and their respective colors:
                    final int sourceA = graph.getTransactionSourceVertex(transactionID);
                    final int sourceB = graph.getTransactionDestinationVertex(transactionID);

                    // If either vertex not found, skip this loop
                    if (sourceA == -1 || sourceB == -1) {
                        continue;
                    }

                    // Get the color for each vertex:
                    col = ConstellationColor.getColorValue(graph.getStringValue(colorVertAttr, sourceA));
                    final Color sourceAColor = col != null ? col.getJavaFXColor() : FALLBACK_COLOR;
                    col = ConstellationColor.getColorValue(graph.getStringValue(colorVertAttr, sourceB));
                    final Color sourceBColor = col != null ? col.getJavaFXColor() : FALLBACK_COLOR;

                    // Get the selection state for each vertex:
                    final boolean sourceASelected = graph.getBooleanValue(selectedVertAttr, sourceA);
                    final boolean sourceBSelected = graph.getBooleanValue(selectedVertAttr, sourceB);

                    // Get the label for each vertex:
                    String sourceALabel = null;
                    String sourceBLabel = null;
                    if (labelVertAttr != Graph.NOT_FOUND) {
                        sourceALabel = graph.getStringValue(labelVertAttr, sourceA);
                        sourceBLabel = graph.getStringValue(labelVertAttr, sourceB);
                    }

                    boolean isElementSelected = GraphManager.getDefault().isElementSelected();
                    isElementSelected |= sourceASelected || sourceBSelected || transSelected;
                    GraphManager.getDefault().setElementSelected(isElementSelected);

                    // Get the directionality of the transaction:
                    final int directionality = graph.getTransactionDirection(transactionID);

                    final Vertex vertexA;
                    final Vertex vertexB;
                    final Transaction transaction;

                    if (sourceA > sourceB) {
                        vertexA = new Vertex(sourceA, sourceA, sourceALabel, sourceAColor,
                                sourceASelected, transSelected, btnShowLabels.isSelected());
                        vertexB = new Vertex(sourceB, sourceB, sourceBLabel, sourceBColor,
                                sourceBSelected, transSelected, btnShowLabels.isSelected());

                        switch (directionality) {
                            case Graph.DOWNHILL -> {
                                final String label = labelMaker(sourceALabel, ARROW_CHAR, sourceBLabel);
                                transaction = new Transaction(transactionID, transColor, label, Transaction.DIRECTED_UP, transSelected);
                            }
                            case Graph.UPHILL ->
                                throw new IllegalArgumentException("source > dest is always downhill");
                            default -> { // Undirected / Bi-directional
                                final String label = labelMaker(sourceALabel, '-', sourceBLabel);
                                transaction = new Transaction(transactionID, transColor, label, transSelected);
                            }
                        }
                    } else if (sourceA < sourceB) {
                        vertexA = new Vertex(sourceB, sourceB, sourceBLabel, sourceBColor,
                                sourceBSelected, transSelected, btnShowLabels.isSelected());
                        vertexB = new Vertex(sourceA, sourceA, sourceALabel, sourceAColor,
                                sourceASelected, transSelected, btnShowLabels.isSelected());

                        switch (directionality) {
                            case Graph.DOWNHILL ->
                                throw new IllegalArgumentException("source < dest is always uphill");
                            case Graph.UPHILL -> {
                                final String label = labelMaker(sourceALabel, ARROW_CHAR, sourceBLabel);
                                transaction = new Transaction(transactionID, transColor, label, Transaction.DIRECTED_DOWN, transSelected);
                            }
                            default -> { // Undirected / Bi-directional
                                final String label = labelMaker(sourceBLabel, '-', sourceALabel);
                                transaction = new Transaction(transactionID, transColor, label, transSelected);
                            }
                        }
                    } else {
                        // Same source and destination: a loop.
                        vertexA = new Vertex(sourceA, sourceA, sourceALabel, sourceAColor,
                                sourceASelected, transSelected, btnShowLabels.isSelected());
                        vertexB = new Vertex(sourceA, sourceA, sourceALabel, sourceAColor,
                                sourceASelected, transSelected, btnShowLabels.isSelected());
                        transaction = new Transaction(transactionID, transColor, sourceALabel, transSelected);
                    }

                    nodeItem = new XYChart.Data<>(leaf.getDatetime(),
                            (Math.max(sourceA, sourceB) - Math.min(sourceA, sourceB)),
                            new Interaction(vertexA, vertexB, transaction, btnShowLabels.isSelected()));
                } else {
                    nodeItem = new XYChart.Data<>(element.getLowerTimeExtent(), element.getLowerDisplayPos(),
                            new Cluster(element.getLowerTimeExtent(), element.getUpperTimeExtent(),
                                    element.getLowerDisplayPos(), element.getUpperDisplayPos(), element.getCount(),
                                    element.getSelectedCount(), element.anyNodesSelected()));
                }

                element.setNodeItem(nodeItem);
            }
            listOfNodeItems.add(nodeItem);
            lowestObservedY = Math.min(element.getLowerDisplayPos(), lowestObservedY);
            highestObservedY = Math.max(element.getUpperDisplayPos(), highestObservedY);
        }

        // Check if thread has been interrupted
        if (Thread.interrupted()) {
            return;
        }

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setData(listOfNodeItems);
        final long low = lowestObservedY;
        final long high = highestObservedY;
        
        Platform.runLater(() -> {
            timeline.populate(series, low, high, selectedOnly, zoneId); // requires platform runlater
        });
    }

    private static String labelMaker(final String a, final char cxn, final String b) {
        return a != null && b != null ? String.format("%s %s %s", a, cxn, b) : null;
    }

    public void clearTimelineData() {
        // Reset timeline by copying values to new instance
        lowerTime.textProperty().unbind();
        upperTime.textProperty().unbind();

        // Get old extents
        final double lower = getTimelineLowerTimeExtent();
        final double upper = getTimelineUpperTimeExtent();

        timeline = new TimelineChart(this, new NumberAxis(), new NumberAxis());

        // Rebind text
        lowerTime.textProperty().bind(timeline.lowerTimeExtentProperty());
        upperTime.textProperty().bind(timeline.upperTimeExtentProperty());

        timeline.setExtents(lower, upper);
        timelinePane.setCenter(timeline);
    }

    /**
     * Method responsible for removing all content from the <code>TimelineChart</code> component.
     *
     * @see TimelineChart
     */
    public void clearTimeline() {
        clearTimelineData();
        clusteringManager.cleanupVariables();
        GraphManager.getDefault().setElementSelected(false);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Extents">
    protected void setTimelineExtent(final GraphReadMethods graph, final double lowerTimeExtent, final double upperTimeExtent,
            final boolean showSelectedOnly, final ZoneId zoneId) {
        final double millisPerPixel = (upperTimeExtent - lowerTimeExtent) / timeline.getWidth();
        final double pixelsPerTransaction = MIN_CLUSTER_WIDTH * millisPerPixel;

        clusteringManager.filterTree(pixelsPerTransaction, (long) lowerTimeExtent, (long) upperTimeExtent);

        updateTimeline(graph, showSelectedOnly, zoneId);
        timeline.setExtents(lowerTimeExtent, upperTimeExtent);
    }

    protected void setExclusionState(final int exclusionState) {
        switch (exclusionState) {
            case 1 ->
                cmbExcludedNodes.setValue(Bundle.DimNodesLabel());
            case 2 ->
                cmbExcludedNodes.setValue(Bundle.HideNodesLabel());
            default ->
                cmbExcludedNodes.setValue(Bundle.ShowNodesLabel());
        }
    }

    protected void setIsShowingSelectedOnly(final boolean isShowingSelectedOnly) {
        selectedOnlyButton.setSelected(isShowingSelectedOnly);
    }

    protected void setLabelState(final boolean isShowingLabels) {
        btnShowLabels.setSelected(isShowingLabels);
    }

    protected double getTimelineLowerTimeExtent() {
        return timeline.getLowerExtent();
    }

    protected double getTimelineUpperTimeExtent() {
        return timeline.getUpperExtent();
    }

    public boolean isDimOrHideExpected(final long actualVxModCount, final long actualTxModCount) {
        return actualVxModCount <= expectedvxMod && actualTxModCount <= expectedtxMod;
    }

    protected void initExclusionState(final Graph graph, final long lowerTimeExtent, final long upperTimeExtent,
            final int exclusionState) {
        final Plugin initPlugin = clusteringManager.new InitDimOrHidePlugin(lowerTimeExtent, upperTimeExtent, exclusionState, (vxMod, txMod) -> {
            expectedvxMod = vxMod;
            expectedtxMod = txMod;
        });
        PluginExecution.withPlugin(initPlugin).executeLater(graph);
    }

    protected void updateExclusionState(final Graph graph, final long lowerTimeExtent, final long upperTimeExtent,
            final int exclusionState) {
        final Plugin updatePlugin = clusteringManager.new UpdateDimOrHidePlugin(lowerTimeExtent, upperTimeExtent, exclusionState, (vxMod, txMod) -> {
            expectedvxMod = vxMod;
            expectedtxMod = txMod;
        });
        PluginExecution.withPlugin(updatePlugin).executeLater(graph);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Toolbar">
    private ToolBar createToolBar() {
        final ToolBar tb = new ToolBar();

        final Label lblDTAttr = new Label(Bundle.DateTimeLabel() + "  ");

        cmbDatetimeAttributes = new ComboBox<>();
        cmbDatetimeAttributes.setPrefWidth(150.0);
        cmbDatetimeAttributes.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) {
                coordinator.setCurrentDatetimeAttr(newValue);
            }
        });

        // Combobox for attribute names:
        cmbAttributeNames = createComboNodeLabels();

        // Time zone selection combo box
        timeZoneComboBox = new ComboBox<>();
        timeZoneComboBox.setPrefWidth(150.0);

        final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
        ZoneId.getAvailableZoneIds().forEach(id -> timeZones.add(ZoneId.of(id)));
        timeZoneComboBox.setItems(timeZones.sorted(TimeZoneUtilities.ZONE_ID_COMPARATOR));
        final Callback<ListView<ZoneId>, ListCell<ZoneId>> cellFactory = (final ListView<ZoneId> p) -> new ListCell<>() {
            @Override
            protected void updateItem(final ZoneId item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(TimeZoneUtilities.getTimeZoneAsString(item));
                }
            }
        };
        timeZoneComboBox.setCellFactory(cellFactory);
        timeZoneComboBox.setButtonCell(cellFactory.call(null));
        timeZoneComboBox.getSelectionModel().select(TimeZoneUtilities.UTC);
        timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> coordinator.updateTimeZone(n));

        // Combo box for excluded nodes visibility
        cmbExcludedNodes = new ComboBox<>();
        cmbExcludedNodes.setPrefWidth(200.0);

        final ObservableList<String> excludedNodeList = FXCollections.observableArrayList();
        excludedNodeList.add(Bundle.ShowNodesLabel());
        excludedNodeList.add(Bundle.DimNodesLabel());
        excludedNodeList.add(Bundle.HideNodesLabel());

        cmbExcludedNodes.setItems(excludedNodeList);
        cmbExcludedNodes.setValue(excludedNodeList.get(0));
        cmbExcludedNodes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue) && coordinator != null) {
                if (newValue.equals(Bundle.DimNodesLabel())) {
                    coordinator.setExclusionState(1);
                } else if (newValue.equals(Bundle.HideNodesLabel())) {
                    coordinator.setExclusionState(2);
                } else {
                    coordinator.setExclusionState(0);
                }
            }
        });

        // Handle
        btnShowLabels = new ToggleButton(Bundle.ShowLabels());
        btnShowLabels.selectedProperty().addListener((observable, oldValue, newValue) -> coordinator.setIsShowingNodeLabels(newValue));

        final Button btnZoomToSelection = new Button(Bundle.ZoomtoSelection());
        btnZoomToSelection.setOnAction(e -> coordinator.setExtents());

        selectedOnlyButton = new ToggleButton(Bundle.Lbl_ShowSelectedOnly());
        selectedOnlyButton.selectedProperty().addListener((observable, oldValue, newValue) -> coordinator.setIsShowingSelectedOnly(newValue));

        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.WHITE.getJavaColor())));
        helpButton.setTooltip(new Tooltip("Display help for Timeline"));
        helpButton.setOnAction(event -> new HelpCtx(TimelineTopComponent.class.getName()).display());

        final Label spacer1 = new Label("   ");
        final Label spacer2 = new Label("   ");
        final Label spacer3 = new Label("   ");
        final Label spacer4 = new Label("   ");
        final Label spacer5 = new Label("   ");

        // Add all of the components to the menu bar:
        tb.getItems().addAll(
                lblDTAttr, cmbDatetimeAttributes,
                timeZoneComboBox,
                spacer1,
                cmbExcludedNodes,
                spacer2,
                selectedOnlyButton,
                spacer3,
                btnZoomToSelection,
                spacer4,
                btnShowLabels,
                spacer5,
                helpButton, cmbAttributeNames);

        tb.setCursor(Cursor.DEFAULT);

        return tb;
    }

    private ComboBox<String> createComboNodeLabels() {
        final ComboBox<String> newComboBox = new ComboBox<>();
        newComboBox.setPrefWidth(150.0);
        newComboBox.setVisible(false);
        newComboBox.setPromptText(Bundle.SelectLabelAttribute());
        newComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                coordinator.setNodeLabelsAttr(newValue);
            }
        });

        //scroll on button only not drop downlist.
        newComboBox.setOnScroll(t -> {
            if (t.getDeltaY() < 0) {
                newComboBox.getSelectionModel().selectNext();
            } else if (t.getDeltaY() > 0) {
                newComboBox.getSelectionModel().selectPrevious();
            }
        });

        newComboBox.setVisibleRowCount(5);

        return newComboBox;
    }

    public void setNodeLabelAttributes(final List<String> attrList) {
        if (attrList != null) {
            ObservableList<String> attrLabels = FXCollections.observableArrayList(attrList);

            if (!cmbAttributeNames.getItems().equals(attrLabels)) {
                final String current = cmbAttributeNames.getSelectionModel().getSelectedItem();

                cmbAttributeNames.setItems(attrLabels);

                if (attrLabels.contains(current)) {
                    cmbAttributeNames.getSelectionModel().select(current);
                } else {
                    cmbAttributeNames.getSelectionModel().clearSelection();
                    cmbAttributeNames.setPromptText(Bundle.SelectLabelAttribute());
                }
            }
        } else {
            toolbar.getItems().remove(cmbAttributeNames);
            cmbAttributeNames = createComboNodeLabels();
            toolbar.getItems().add(cmbAttributeNames);
            cmbAttributeNames.setPromptText(Bundle.SelectLabelAttribute());
        }
    }

    public void setIsShowingNodeLabelAttributes(final boolean isShowingNodeLabels) {
        cmbAttributeNames.setVisible(isShowingNodeLabels);
        cmbAttributeNames.setDisable(!isShowingNodeLabels);

        btnShowLabels.setSelected(isShowingNodeLabels);
    }

    public void setNodeLabelAttribute(final String attribute) {
        if (attribute == null || !cmbAttributeNames.getItems().contains(attribute)) {
            cmbAttributeNames.getSelectionModel().clearSelection();
            cmbAttributeNames.setPromptText(Bundle.SelectLabelAttribute());
        } else {
            cmbAttributeNames.getSelectionModel().select(
                    cmbAttributeNames.getItems().indexOf(attribute));
        }
    }

    public void setDisabledToolbar(final boolean isDisabled) {
        toolbar.setDisable(isDisabled);
    }

    public void setDateTimeAttributes(final List<String> attrList, final String currentDateTimeAttr) {
        final ObservableList<String> attrs = FXCollections.observableArrayList(attrList);

        cmbDatetimeAttributes.setItems(attrs);
        if (currentDateTimeAttr == null) {
            cmbDatetimeAttributes.getSelectionModel().selectFirst();
        } else {
            cmbDatetimeAttributes.getSelectionModel().select(
                    cmbDatetimeAttributes.getItems().indexOf(currentDateTimeAttr));
        }
    }
    // </editor-fold>

    void setTimeZone(final ZoneId timeZone) {
        timeZoneComboBox.getSelectionModel().select(timeZone);
    }

    public void passScrollEventFromOverviewToChart(final ScrollEvent se) {
        final double midpoint = timeline.getWidth() / 2;
        timeline.performZoom(se, midpoint);
    }
    // <editor-fold defaultstate="collapsed" desc="Time Extent Labels">

    private Label createExtentLabel() {
        final Label newLabel = new Label();

        // Align the text vertically:
        newLabel.setRotate(270D);
        // Fix the dimensions to prevent jittery motion on value changes:
        newLabel.setMinWidth(150D);
        newLabel.setPrefWidth(150D);
        newLabel.setMaxWidth(150D);
        newLabel.setMinHeight(30D);
        newLabel.setPrefHeight(30D);
        newLabel.setMaxHeight(30D);
        newLabel.setAlignment(Pos.CENTER);

        return newLabel;
    }
    // </editor-fold>
}

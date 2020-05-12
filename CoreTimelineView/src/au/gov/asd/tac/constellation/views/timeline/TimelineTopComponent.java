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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LocalDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.awt.BorderLayout;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the CONSTELLATION Timeline UI.
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.views.timeline//Timeline//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TimelineTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/timeline/resources/timeline.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.timeline.TimelineTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 1500),
    @ActionReference(path = "Shortcuts", name = "CS-T"),
    @ActionReference(path = "Toolbars/Views", position = 100)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TimelineAction",
        preferredID = "TimelineTopComponent"
)
@Messages({
    "CTL_TimelineAction=Timeline",
    "CTL_TimelineTopComponent=Timeline",
    "HINT_TimelineTopComponent=The timeline window allows the user to display temporal data pertaining to a given graph.",
    "NoGraph=<No Active Graph>",
    "NoTemporal=<No Temporal Data Present On Current Graph>"
})
public final class TimelineTopComponent extends TopComponent implements LookupListener, GraphChangeListener, UndoRedo.Provider {

    private static final double DEFAULT_DIVIDER_LOCATION = 0.9;
    private static final String LIGHT_THEME = "resources/Style-Container-Light.css";
    private static final String DARK_THEME = "resources/Style-Container-Dark.css";
    private static final String UPDATE_TIMELINE_THREAD_NAME = "Update Timeline from Graph";
    public static final List<String> SUPPORTED_DATETIME_ATTRIBUTE_TYPES = Arrays.asList(
            ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME,
            LocalDateTimeAttributeDescription.ATTRIBUTE_NAME,
            DateAttributeDescription.ATTRIBUTE_NAME);

    private GraphNode graphNode = null;
    private final Lookup.Result<GraphNode> result;
    private TimelineState state;
    private JFXPanel container = new JFXPanel();
    private TimelinePanel timelinePanel;
    private OverviewPanel overviewPanel;
    private SplitPane splitPane;
    private BorderPane noActive;
    private Label lblNoActive;
    private StackPane root;
    private long currentGlobalModificationCount = Long.MIN_VALUE;
    private long currentAttributeModificationCount = Long.MIN_VALUE;
    private long currentStructureModificationCount = Long.MIN_VALUE;
    private long currentTransSelectedModificationCount = Long.MIN_VALUE;
    private long currentVertSelectedModificationCount = Long.MIN_VALUE;
    //private long currentTransDimModificationCount = Long.MIN_VALUE;
    //private long currentVertDimModificationCount = Long.MIN_VALUE;
    //private long currentTransHideModificationCount = Long.MIN_VALUE;
    //private long currentVertHideModificationCount = Long.MIN_VALUE;
    private long currentTemporalAttributeModificationCount = Long.MIN_VALUE;
    private volatile double splitPanePosition = DEFAULT_DIVIDER_LOCATION;
    private List<String> datetimeAttributes;
    private String currentDatetimeAttribute = null;

    public TimelineTopComponent() {
        initComponents();
        setName(Bundle.CTL_TimelineTopComponent());
        setToolTipText(Bundle.HINT_TimelineTopComponent());

        // Attach listener that determines the active graphnode:
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);

        final TimelineTopComponent thisComponent = this;

        // Add the JavaFX container to this topcomponent (this enables JavaFX and Swing interoperability):
        add(container, BorderLayout.CENTER);

        final int height = this.getPreferredSize().height;

        // Populate the jfx container:
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            // No active graph view:
            noActive = new BorderPane();
            lblNoActive = new Label(Bundle.NoGraph());
            lblNoActive.setTextFill(Color.LIGHTGREY);
            BorderPane.setAlignment(lblNoActive, Pos.CENTER);
            noActive.setCenter(lblNoActive);
            
            timelinePanel = new TimelinePanel(thisComponent);
            overviewPanel = new OverviewPanel(thisComponent);
            
            SplitPane.setResizableWithParent(overviewPanel, false);
            SplitPane.setResizableWithParent(timelinePanel, true);
            splitPane = new SplitPane();
            splitPane.setId("hiddenSplitter");
            splitPane.getItems().addAll(timelinePanel, overviewPanel);
            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.getStylesheets().add(TimelineTopComponent.class.getResource(DARK_THEME).toExternalForm());
            splitPane.setMinHeight(height * 0.8);
            splitPane.setVisible(false);
            splitPane.setDisable(true);
            
            root = new StackPane();
            root.getChildren().addAll(noActive, splitPane);
            
            // Create the scene:
            final Scene scene = new Scene(root);
            scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
            
            splitPane.prefHeightProperty().bind(scene.heightProperty());
            splitPane.prefWidthProperty().bind(scene.widthProperty());
            
            // Set the split pane as the javafx scene:
            container.setScene(scene);
            
            // Now that the heights are known, set the position of the splitPane divider:
            splitPane.getDividers().get(0).setPosition(splitPanePosition);
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Timeline and Histogram Extents">
    /**
     * Coordinates extents for both the timeline and the histogram components.
     *
     * @param lowerTimeExtent The earliest time to be shown on the components.
     * @param upperTimeExtent The latest time to be shown on the components.
     */
    public void setExtents(final double lowerTimeExtent, final double upperTimeExtent) {
        state.setLowerTimeExtent(lowerTimeExtent);
        state.setUpperTimeExtent(upperTimeExtent);

        if (graphNode.getGraph() != null) {
            final ReadableGraph graph = graphNode.getGraph().getReadableGraph();
            try {
                timelinePanel.setTimelineExtent(graph, state.getLowerTimeExtent(),
                        state.getUpperTimeExtent(), state.isShowingSelectedOnly(), state.getTimeZone());
            } finally {
                graph.release();
            }

            timelinePanel.updateExclusionState(graphNode.getGraph(),
                    (long) state.getLowerTimeExtent(), (long) state.getUpperTimeExtent(), state.exclusionState());
            overviewPanel.setExtentPOV(state.getLowerTimeExtent(), state.getUpperTimeExtent());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Timeline and Histogram Extents">
    /**
     * Coordinates extents for both the timeline and the histogram components.
     *
     */
    public void setExtents() {

        if (graphNode.getGraph() != null) {

            final ReadableGraph graph = graphNode.getGraph().getReadableGraph();
            final int txCount = graph.getTransactionCount();
            final int txTimAttrId = graph.getAttribute(GraphElementType.TRANSACTION, this.currentDatetimeAttribute);
            final int txSelAttrId = graph.getAttribute(GraphElementType.TRANSACTION,
                    VisualConcept.TransactionAttribute.SELECTED.getName());

            long lowerTimeExtent = Long.MAX_VALUE;
            long upperTimeExtent = Long.MIN_VALUE;
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;

            for (int txID = 0; txID < txCount; txID++) {
                if (graph.getBooleanValue(txSelAttrId, txID)) {
                    lowerTimeExtent = Math.min(graph.getLongValue(txTimAttrId, txID), lowerTimeExtent);
                    upperTimeExtent = Math.max(graph.getLongValue(txTimAttrId, txID), upperTimeExtent);
                }
                minTime = Math.min(graph.getLongValue(txTimAttrId, txID), minTime);
                maxTime = Math.max(graph.getLongValue(txTimAttrId, txID), maxTime);
            }

            if (lowerTimeExtent != Long.MAX_VALUE) {
                state.setLowerTimeExtent(lowerTimeExtent);
            } else {
                state.setLowerTimeExtent(minTime);
            }
            if (upperTimeExtent != Long.MIN_VALUE) {
                state.setUpperTimeExtent(upperTimeExtent);
            } else {
                state.setUpperTimeExtent(maxTime);
            }

            try {
                timelinePanel.setTimelineExtent(graph, state.getLowerTimeExtent(),
                        state.getUpperTimeExtent(), state.isShowingSelectedOnly(), state.getTimeZone());
            } finally {
                graph.release();
            }

            timelinePanel.updateExclusionState(graphNode.getGraph(),
                    (long) state.getLowerTimeExtent(), (long) state.getUpperTimeExtent(), state.exclusionState());
            overviewPanel.setExtentPOV(state.getLowerTimeExtent(), state.getUpperTimeExtent());
        }
    }

    protected double getTimelineLowerTimeExtent() {
        return timelinePanel.getTimelineLowerTimeExtent();
    }

    protected double getTimelineUpperTimeExtent() {
        return timelinePanel.getTimelineUpperTimeExtent();
    }
    // </editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // Ensure that graph manager is listening for graphs:
        GraphManager.getDefault();
        result.addLookupListener(this);
    }

    public void zoomFromOverview(final ScrollEvent se) {
        timelinePanel.passScrollEventFromOverviewToChart(se);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Properties">
    void writeProperties(final java.util.Properties p) {
        // SplitPane position:
        final String pos = Double.toString(splitPane.getDividers().get(0).getPosition());
        p.setProperty("divider-location", pos);
    }

    void readProperties(final java.util.Properties p) {
        splitPanePosition = DEFAULT_DIVIDER_LOCATION;
        final String pos = p.getProperty("divider-location");
        if (pos != null) {
            try {
                splitPanePosition = Double.parseDouble(pos);

                // Ensure that this happens after the initial setup.
                Platform.runLater(() -> {
                    splitPane.getDividers().get(0).setPosition(splitPanePosition);
                });
            } catch (final NumberFormatException ex) {
            }
        }
    }
    // </editor-fold>

    private void setNode(final GraphNode node) {
        // Navigating away from the current graph:
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();

            // As we are navigating away from this graph, save the state and remove the listeners.
            graph.removeGraphChangeListener(this);

            persistStateToGraph();

            // We no longer have a datetime as we are moving away from the current graph:
            currentDatetimeAttribute = null;

            GraphManager.getDefault().setDatetimeAttr(null);

            Platform.runLater(() -> {
                timelinePanel.setExclusionState(0);
                //timelinePanel.setIsShowingNodeLabelAttributes(false);
                timelinePanel.setNodeLabelAttributes(null);
                timelinePanel.setNodeLabelAttribute(null);
                timelinePanel.setTimeZone(TimeZoneUtilities.UTC);
                splitPane.setVisible(false);
                timelinePanel.setDisable(true);
                // Clear charts:
                timelinePanel.clearTimeline();
                overviewPanel.clearHistogram();
            });
        }

        // Moving to a new graph:
        if (node != null) {
            graphNode = node;
            final Graph graph = graphNode.getGraph();

            // Check if there is a previous state on the graph:
            retrieveStateFromGraph();

            // Update the graph change counters:
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                currentGlobalModificationCount = rg.getGlobalModificationCounter();
                currentAttributeModificationCount = rg.getAttributeModificationCounter();
                currentStructureModificationCount = rg.getStructureModificationCounter();

                final int transSelectedAttr = rg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
                final int vertSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                if (transSelectedAttr != Graph.NOT_FOUND) {
                    currentTransSelectedModificationCount = rg.getValueModificationCounter(transSelectedAttr);
                }
                if (vertSelectedAttr != Graph.NOT_FOUND) {
                    currentVertSelectedModificationCount = rg.getValueModificationCounter(vertSelectedAttr);
                }
            } finally {
                rg.release();
            }

            populateFromGraph(graph, true);

            graph.addGraphChangeListener(this);
        } // Moving to nothing:
        else {
            persistStateToGraph();

            graphNode = null;

            // Hide interface:
            hideTimeline(Bundle.NoGraph());
        }
    }

    private void hideTimeline(final String message) {
        Platform.runLater(() -> {
            lblNoActive.setText(message);
            
            timelinePanel.setDisable(true);
            //if visibility is set to false at the constructor, the javafx thread gets stuck in an endless loop under
            //certain conditions (with timeline open, create graph, close graph) so we set opacity to 0 in the constructor so that it is 'invisible'
            splitPane.setVisible(false);
            splitPane.setDisable(true);
            
            // Clear charts:
            timelinePanel.clearTimeline();
            overviewPanel.clearHistogram();
        });
    }

    private void populateFromGraph(final Graph graph, final boolean isFullRefresh) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                if (graph == null) {
                    // with no graph to populate from there's no point continuing
                    return;
                }
                datetimeAttributes = new ArrayList<>();
                final ReadableGraph rg = graph.getReadableGraph();
                try {
                    final int attributeCount = rg.getAttributeCount(GraphElementType.TRANSACTION);
                    for (int i = 0; i < attributeCount; i++) {
                        final int attrID = rg.getAttribute(GraphElementType.TRANSACTION, i);
                        final Attribute attribute = new GraphAttribute(rg, attrID);
                        final String attributeType = attribute.getAttributeType();

                        if (SUPPORTED_DATETIME_ATTRIBUTE_TYPES.contains(attributeType)) {
                            datetimeAttributes.add(attribute.getName());
                        }
                    }

                    if (!datetimeAttributes.isEmpty()) {
                        if (state != null && datetimeAttributes.contains(state.getDateTimeAttr())) {
                            currentDatetimeAttribute = state.getDateTimeAttr();

                            GraphManager.getDefault().setDatetimeAttr(currentDatetimeAttribute);
                        } else {
                            // Set the current datetime to the first found datetime attribute from the graph:
                            currentDatetimeAttribute = datetimeAttributes.get(0);
                            GraphManager.getDefault().setDatetimeAttr(currentDatetimeAttribute);
                        }
                    } else {
                        // There are no datetime attributes on the graph:
                        currentDatetimeAttribute = null;
                        GraphManager.getDefault().setDatetimeAttr(currentDatetimeAttribute);
                        hideTimeline(Bundle.NoTemporal());

                        return;
                    }

                    if (currentDatetimeAttribute != null) {
                        currentTemporalAttributeModificationCount = rg.getValueModificationCounter(rg.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute));
                        // We've calculated everything, so start populating the graph:
                        Platform.runLater(() -> {
                            final ReadableGraph rg1 = graph.getReadableGraph();
                            try {
                                // Clear anything already on the charts:
                                timelinePanel.clearTimeline();
                                overviewPanel.clearHistogram(!isFullRefresh);
                                // Ensure that everything is visible:
                                timelinePanel.setDisable(false);
                                //splitPane.setVisible(true);
                                //if visibility is set to false at the constructor, the javafx thread gets stuck in an endless loop under
                                //certain conditions (with timeline open, create graph, close graph) so we set opacity to 0 in the constructor so that it is 'invisible'
//                                    splitPane.setOpacity(1);
                                splitPane.setVisible(true);
                                splitPane.setDisable(false);
//                                    splitPane.getDividers().get(0).setPosition(splitPanePosition);
                                // Add the datetime attributes:
                                timelinePanel.setDateTimeAttributes(datetimeAttributes, currentDatetimeAttribute);
                                timelinePanel.setTimeZone(state == null ? TimeZoneUtilities.UTC : state.getTimeZone());
                                // Add the label attributes:
                                timelinePanel.setNodeLabelAttributes(GraphManager.getDefault().getVertexAttributeNames());
                                final boolean selectedOnly = state != null && state.isShowingSelectedOnly();
                                timelinePanel.setIsShowingSelectedOnly(selectedOnly);
                                if (state != null && state.getNodeLabelsAttr() != null) {
                                    timelinePanel.setNodeLabelAttribute(state.getNodeLabelsAttr());
                                    timelinePanel.setIsShowingNodeLabelAttributes(state.isShowingNodeLabels());
                                    timelinePanel.populateFromGraph(rg1, currentDatetimeAttribute, state.getNodeLabelsAttr(), selectedOnly, state.getTimeZone());
                                } else {
                                    timelinePanel.populateFromGraph(rg1, currentDatetimeAttribute, null, selectedOnly, state == null ? TimeZoneUtilities.UTC : state.getTimeZone());
                                }
                                overviewPanel.populateHistogram(rg1, currentDatetimeAttribute, getTimelineLowerTimeExtent(), getTimelineUpperTimeExtent(), isFullRefresh, selectedOnly);
                            } finally {
                                rg1.release();
                            }
                            // Restore the dimming state if we have it:
                            if (state != null) //if(state != null && !isFullRefresh)
                            {
                                if (state.getLowerTimeExtent() == 0) {
                                    setExtents(getTimelineLowerTimeExtent(), getTimelineUpperTimeExtent());
                                }
                                timelinePanel.setExclusionState(state.exclusionState());
                            } else {
                                // There is no state, so lets create a new one:
                                state = new TimelineState(getTimelineLowerTimeExtent(), getTimelineUpperTimeExtent(),
                                        0, false, currentDatetimeAttribute, false, null, TimeZoneUtilities.UTC);
                            }
                            setExtents(state.getLowerTimeExtent(), state.getUpperTimeExtent());
                        });
                    }
                } finally {
                    rg.release();
                }
            }
        };
        t.setName(UPDATE_TIMELINE_THREAD_NAME);
        t.start();
    }

    // Wrapper call around populateFromGraph for cases where populateFromGraph is using graphNode.getGraph()
    // as the graph. This wrapper validates that graphNode is non null.
    private void populateFromGraphNode(final boolean isFullRefresh) {
        if (graphNode != null) {
            populateFromGraph(graphNode.getGraph(), isFullRefresh);
        }
    }

    public void setCurrentDatetimeAttr(final String currentDatetimeAttr) {
        this.currentDatetimeAttribute = currentDatetimeAttr;
        if (state != null) {
            state.setDateTimeAttr(currentDatetimeAttr);
        }
        persistStateToGraph();

        GraphManager.getDefault().setDatetimeAttr(currentDatetimeAttr);

        // Clear charts:
        Platform.runLater(() -> {
            timelinePanel.clearTimeline();
            overviewPanel.clearHistogram();
            
            timelinePanel.setExclusionState(0);
        });

        // Call for repopulation:
        populateFromGraphNode(true);
    }

    void updateTimeZone(final ZoneId timeZone) {
        if (state != null && !state.getTimeZone().equals(timeZone)) {
            state.setTimeZone(timeZone);

            persistStateToGraph();

            // Clear charts.
            Platform.runLater(() -> {
                timelinePanel.clearTimeline();
                overviewPanel.clearHistogram();
                timelinePanel.setExclusionState(0);
            });

            populateFromGraphNode(true);
        }
    }

    protected void setExclusionState(final int exclusionState) {
        if (state != null) {
            state.setExclusionState(exclusionState);
            // Enable dimming if just enabled:
            timelinePanel.initExclusionState(graphNode.getGraph(), state.getDateTimeAttr(),
                    (long) state.getLowerTimeExtent(), (long) state.getUpperTimeExtent(), exclusionState);
        }
    }

    protected void setIsShowingSelectedOnly(final boolean isShowingSelectedOnly) {
        if (state != null) {
            state.setIsShowingSelectedOnly(isShowingSelectedOnly);
        }

        persistStateToGraph();

        // Clear charts.
        Platform.runLater(() -> {
            timelinePanel.clearTimeline();
            overviewPanel.clearHistogram();
            timelinePanel.setExclusionState(0);
        });

        // Call for repopulation.
        populateFromGraphNode(true);
    }

    protected void setIsShowingNodeLabels(final boolean isShowingNodeLabels) {
        if (state != null) {
            state.setIsShowingNodeLabels(isShowingNodeLabels);
            persistStateToGraph();

            if (!isShowingNodeLabels || state.getNodeLabelsAttr() != null) {
                populateFromGraphNode(false);
            }

            timelinePanel.setIsShowingNodeLabelAttributes(isShowingNodeLabels);
        }
    }

    protected void setNodeLabelsAttr(final String nodeLabelsAttr) {
        if (state != null) {
            state.setNodeLabelsAttr(nodeLabelsAttr);
            persistStateToGraph();

            // Do only a partial update, ie the timeline and selection area for histogram:
            populateFromGraphNode(false);
        }
    }

    protected String getNodeLabelsAttr() {
        return state.getNodeLabelsAttr();
    }

    private void persistStateToGraph() {
        if (graphNode != null) {
            // Ensure there is a graph to persist state to
            PluginExecution.withPlugin(new TimelineStatePlugin(state)).executeLater(graphNode.getGraph());
        }
    }

    private void retrieveStateFromGraph() {
        final ReadableGraph rg = graphNode.getGraph().getReadableGraph();
        try {
            final int attrID = rg.getAttribute(GraphElementType.META, TimelineConcept.MetaAttribute.TIMELINE_STATE.getName());
            if (attrID != Graph.NOT_FOUND) {
                state = (TimelineState) rg.getObjectValue(attrID, 0);
            } else {
                state = null;
            }
        } finally {
            rg.release();
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        if (graphNode != null) {
            return graphNode.getUndoRedoManager();
        } else {
            return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Graph Listeners">
    /**
     * Listener method called on changes to the graph node.
     *
     * @param ev The corresponding event for changes to the current graph node.
     */
    @Override
    public void resultChanged(final LookupEvent ev) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1
                && activatedNodes[0] instanceof GraphNode) {
            final GraphNode gnode = ((GraphNode) activatedNodes[0]);

            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }

    /**
     * Listener method called on changes to the graph.
     *
     * @param evt The corresponding event for changes to the current graph.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        if (graphNode != null) {
            final ReadableGraph rg = graphNode.getGraph().getReadableGraph();
            try {
                final long oldGlobalModificationCount = currentGlobalModificationCount;
                currentGlobalModificationCount = rg.getGlobalModificationCounter();

                // Continue only if there has been a change:
                if (currentGlobalModificationCount == oldGlobalModificationCount) {
                    return;
                }

                final long oldAttributeModificationCount = currentAttributeModificationCount;
                currentAttributeModificationCount = rg.getAttributeModificationCounter();

                final long oldStructureModificationCount = currentStructureModificationCount;
                currentStructureModificationCount = rg.getStructureModificationCounter();

                // Selected and dimming attributes:
                final long oldTransSelectedModificationCount = currentTransSelectedModificationCount;
                final long oldVertSelectedModificationCount = currentVertSelectedModificationCount;
                final long oldTemporalAttributeModificationCount = currentTemporalAttributeModificationCount;
                final int transSelectedAttr = rg.getAttribute(GraphElementType.TRANSACTION,
                        VisualConcept.TransactionAttribute.SELECTED.getName());
                final int vertSelectedAttr = rg.getAttribute(GraphElementType.VERTEX,
                        VisualConcept.VertexAttribute.SELECTED.getName());
                if (transSelectedAttr != Graph.NOT_FOUND) {
                    currentTransSelectedModificationCount = rg.getValueModificationCounter(transSelectedAttr);
                }
                if (vertSelectedAttr != Graph.NOT_FOUND) {
                    currentVertSelectedModificationCount = rg.getValueModificationCounter(vertSelectedAttr);
                }
                final int temporalAttrId = rg.getAttribute(GraphElementType.TRANSACTION, this.currentDatetimeAttribute);
                if (temporalAttrId != Graph.NOT_FOUND) {
                    currentTemporalAttributeModificationCount = rg.getValueModificationCounter(temporalAttrId);
                }

                /*final int transDimAttr = rg.getAttribute(GraphElementType.TRANSACTION,
                        VisualConcept.TransactionAttribute.DIMMED.getName());
                final int vertDimAttr = rg.getAttribute(GraphElementType.VERTEX,
                        VisualConcept.VertexAttribute.DIMMED.getName());
                final int transHideAttr = rg.getAttribute(GraphElementType.TRANSACTION,
                        VisualConcept.TransactionAttribute.VISIBILITY.getName());
                final int vertHideAttr = rg.getAttribute(GraphElementType.VERTEX,
                        VisualConcept.VertexAttribute.VISIBILITY.getName());
                if (transDimAttr != Graph.NOT_FOUND) {
                    currentTransDimModificationCount = rg.getValueModificationCounter(transDimAttr);
                }
                if (vertDimAttr != Graph.NOT_FOUND) {
                    currentVertDimModificationCount = rg.getValueModificationCounter(vertDimAttr);
                }
                if (transHideAttr != Graph.NOT_FOUND) {
                    currentTransHideModificationCount = rg.getValueModificationCounter(transHideAttr);
                }
                if (vertHideAttr != Graph.NOT_FOUND) {
                    currentVertHideModificationCount = rg.getValueModificationCounter(vertHideAttr);
                }*/
                // Detect graph changes to attributes:
                if (currentAttributeModificationCount != oldAttributeModificationCount) {
                    Platform.runLater(() -> {
                        // Re-populate charts:
                        timelinePanel.setNodeLabelAttributes(GraphManager.getDefault().getVertexAttributeNames());
                        populateFromGraphNode(true);
                    });
                } //Detect value change on the temporal attribute
                else if (currentTemporalAttributeModificationCount != oldTemporalAttributeModificationCount) {
                    populateFromGraphNode(true);
                } // Detect graph structural changes (such as adding and removal of nodes etc):
                else if (currentStructureModificationCount != oldStructureModificationCount) {
                    // Re-populate charts:
                    populateFromGraphNode(true);
                } // Detect changes of selection to transactions or vertices:
                else if (currentTransSelectedModificationCount != oldTransSelectedModificationCount
                        || currentVertSelectedModificationCount != oldVertSelectedModificationCount) {
                    // Do only a partial update, ie the timeline and selection area for histogram:
                    populateFromGraphNode(false);
                } // Detect changes of dim to transactions and vertices:
                /*else if (!timelinePanel.isDimOrHideExpected(currentVertDimModificationCount, currentTransDimModificationCount)) {
                    Platform.runLater(() -> {
                        timelinePanel.setExclusionState(0);
                    });
                }
                else if (!timelinePanel.isDimOrHideExpected(currentVertHideModificationCount, currentTransHideModificationCount)) {
                    Platform.runLater(() -> {
                        timelinePanel.setExclusionState(0);
                    });
                }*/
            } finally {
                rg.release();
            }
        }
    }
    // </editor-fold>
}

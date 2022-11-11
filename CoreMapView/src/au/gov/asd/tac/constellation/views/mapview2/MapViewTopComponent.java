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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.LineMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Screen;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author altair1673
 */
@TopComponent.Description(
        preferredID = "MapViewTopComponent2",
        iconBase = "au/gov/asd/tac/constellation/views/mapview/resources/treasure-map.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 4000),
    @ActionReference(path = "Shortcuts", name = "CA-M")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapView2Action",
        preferredID = "MapViewTopComponent2"
)
@NbBundle.Messages({
    "CTL_MapView2Action=Map View",
    "CTL_MapViewTopComponent2=Map View",
    "HINT_MapViewTopComponent2=Map View"
})

public final class MapViewTopComponent extends JavaFxTopComponent<MapViewPane> {

    private final Logger LOGGER = Logger.getLogger("test");

    public final MapViewPane mapViewPane;
    private final Map<String, AbstractMarker> markers = new HashMap<>();
    private final List<Integer> selectedNodeList = new ArrayList<>();

    private int markerID = 0;

    public MapViewTopComponent() {

        setName(Bundle.CTL_MapViewTopComponent2());
        setToolTipText(Bundle.HINT_MapViewTopComponent2());

        initComponents();
        mapViewPane = new MapViewPane(this);
        initContent();

    }

    /**
     * Sets the top components content to the findViewPane.
     *
     * @return
     */
    @Override
    protected MapViewPane createContent() {
        return mapViewPane;
    }

    /**
     * Sets the css Styling
     *
     * @return
     */
    @Override
    protected String createStyle() {
        return "";
    }


    /**
     * Handles what occurs when the component is opened. This updates the UI to
     * ensure its current, toggles the find view to set it to enabled or
     * disabled based on if a graph is open, focuses the findTextBox for UX
     * quality, ensures the view window is floating. It also sets the size and
     * location of the view to be in the top right of the users screen.
     */
    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        //WindowManager.getDefault().setTopComponentFloating(this, true);
        //mapViewPane.resetContent();
        mapViewPane.setUpMap();
        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(this)).executeLater(getCurrentGraph());

    }

    @Override
    protected void handleComponentClosed() {
        super.handleComponentClosed();
        markers.clear();
        selectedNodeList.clear();
    }

    public int getNewMarkerID() {
        return ++markerID;
    }

    public void setMapImage(Component mapComponent) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jfxContainer.add(mapComponent);
                validate();
            }
        });

        //jfxContainer.setVisible(true);

    }

    public JFXPanel getJfxContainer() {
        return jfxContainer;
    }


    /**
     * When a graph is opened handle toggling the find view disabled state
     *
     * @param graph
     */
    @Override
    protected void handleGraphOpened(final Graph graph) {
        super.handleGraphOpened(graph);
        LOGGER.log(Level.SEVERE, "Graph Has been opened");
        mapViewPane.setUpMap();
        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(this)).executeLater(getCurrentGraph());
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return markers;
    }


    /**
     * When a new graph is created handle updating the UI
     *
     * @param graph
     */
    @Override
    protected void handleNewGraph(final Graph graph) {
        super.handleNewGraph(graph);
        //UpdateUI();
        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(this)).executeLater(getCurrentGraph());
    }

    public void drawMarkerOnMap(double lat, double lon, double scale) {
        mapViewPane.drawMarker(lat, lon, scale);
    }

    public void addNodeId(int markerID, List<Integer> selectedNodes, boolean selectingVertex) {
        selectedNodeList.add(markerID);
        PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodes, selectingVertex)).executeLater(getCurrentGraph());
    }

    public void drawMarkerOnMap() {
        LOGGER.log(Level.SEVERE, "Length of marker: " + markers.size());

        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            mapViewPane.drawMarker(m);
        }

    }


    /**
     * Get the findViewPane
     *
     * @return the findViewPane
     */
    public MapViewPane getMapViewPane() {
        return mapViewPane;
    }

    public MapProvider getDefaultProvider() {
        return mapViewPane.getDefaultProvider();
    }

    public MarkerState getMarkerState() {
        return mapViewPane.getMarkerState();
    }

    public boolean shouldUpdate() {
        return this.needsUpdate();
    }

    public int getContentWidth() {
        return getWidth();
    }

    public void addMarker(String key, AbstractMarker e) {
        markers.put(key, e);
    }

    public int getContentHeight() {
        return getHeight();
    }

    public List<? extends MapProvider> getProviders() {
        return mapViewPane.getProviders();
    }

    public void selectOnGraph(final GraphElementType graphElementType, final Set<Integer> elementIds) {
        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(this)).executeLater(getCurrentGraph());
    }

    public void removeUserMarker(int id) {
        mapViewPane.removeUserMarker(id);
    }

    @PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
    public static class ExtractCoordsFromGraphPlugin extends SimpleReadPlugin {

        private final Logger LOGGER = Logger.getLogger("test");


        private MapViewTopComponent mapViewTopComponent;

        public ExtractCoordsFromGraphPlugin(final MapViewTopComponent topComponent) {
            mapViewTopComponent = topComponent;
        }

        @Override
        public String getName() {
            return "ExtractCoordsFromGraphPlugin";
        }

        @Override
        protected void read(GraphReadMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {

            if (graph != null) {

                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};

                try {
                    for (GraphElementType elementType : elementTypes) {
                        int lonID = GraphConstants.NOT_FOUND;
                        int latID = GraphConstants.NOT_FOUND;

                        int elementCount;

                        switch (elementType) {
                            case VERTEX:
                                lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                                latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                                elementCount = graph.getVertexCount();
                                //LOGGER.log(Level.SEVERE, "Lattitude: " + latID + ", Longitude: " + lonID);

                                //double lon = graph.getDoubleValue(latID, latID)
                                break;
                            case TRANSACTION:
                                lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                                latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                                elementCount = graph.getTransactionCount();
                                break;
                            default:
                                continue;
                        }

                        for (int elementPos = 0; elementPos < elementCount; ++elementPos) {
                            int elementID = -99;

                            switch (elementType) {
                                case VERTEX:
                                    elementID = graph.getVertex(elementPos);

                                    break;
                                case TRANSACTION:
                                    elementID = graph.getTransaction(elementPos);
                                    break;
                                default:
                                    break;
                            }

                            if (lonID != GraphConstants.NOT_FOUND && latID != GraphConstants.NOT_FOUND && elementID != -99 && elementType == GraphElementType.VERTEX) {
                                final float elementLat = graph.getObjectValue(latID, elementID);
                                final float elementLon = graph.getObjectValue(lonID, elementID);
                                PointMarker p = new PointMarker(mapViewTopComponent, mapViewTopComponent.getNewMarkerID(), elementID, (double) elementLat, (double) elementLon, 0.05, 95, 244);
                                String coordinateKey = (double) elementLat + "," + (double) elementLon;
                                if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                                    mapViewTopComponent.addMarker(coordinateKey, p);

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            mapViewTopComponent.mapViewPane.drawMarker(p);
                                        }
                                    });

                                } else {
                                    mapViewTopComponent.getAllMarkers().get(coordinateKey).addNodeID(elementID);
                                }

                                //mapViewTopComponent.drawMarkerOnMap(elementLat, elementLon, 0.05);

                            }

                            if (elementType == GraphElementType.TRANSACTION) {

                                int dateTimeID = TemporalConcept.TransactionAttribute.DATETIME.get(graph);

                                String dateTime = graph.getStringValue(dateTimeID, elementID);

                                if (!dateTime.isBlank()) {
                                    int sourceID = graph.getTransactionSourceVertex(elementID);
                                    int destinationID = graph.getTransactionDestinationVertex(elementID);

                                    final float sourceLat = graph.getObjectValue(latID, sourceID);
                                    final float sourceLon = graph.getObjectValue(lonID, sourceID);

                                    final float destLat = graph.getObjectValue(latID, destinationID);
                                    final float destLon = graph.getObjectValue(lonID, destinationID);

                                    String coordinateKey = (double) sourceLat + "," + (double) sourceLon + "," + (double) destLat + "," + (double) destLon;

                                    LineMarker l = new LineMarker(mapViewTopComponent, mapViewTopComponent.getNewMarkerID(), elementID, (float) sourceLat, (float) sourceLon, (float) destLat, (float) destLon, 0, 149);
                                    if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                                        mapViewTopComponent.addMarker(coordinateKey, l);
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                mapViewTopComponent.mapViewPane.drawMarker(l);
                                            }
                                        });
                                    } else {
                                        mapViewTopComponent.getAllMarkers().get(coordinateKey).addNodeID(elementID);
                                    }
                                }


                            }
                        }

                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "EXCEPTION CAUGHT!!", e);
                    LOGGER.log(Level.SEVERE, e.getMessage());
                }

                //mapViewTopComponent.drawMarkerOnMap();

            }

        }

    }

    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    public static class SelectOnGraphPlugin extends SimpleEditPlugin {

        private List<Integer> selectedNodeList = new ArrayList<Integer>();
        private boolean isSelectingVertex = true;

        public SelectOnGraphPlugin(List<Integer> selectedNodeList, boolean isSelectingVertex) {
            this.selectedNodeList = selectedNodeList;
            this.isSelectingVertex = isSelectingVertex;
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            if (isSelectingVertex) {
                final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
                final int vertexCount = graph.getVertexCount();

                for (int i = 0; i < vertexCount; ++i) {
                    final int vertexID = graph.getVertex(i);
                    graph.setBooleanValue(vertexSelectedAttribute, vertexID, selectedNodeList.contains(vertexID));
                }
            } else {
                final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
                final int transactionCount = graph.getTransactionCount();

                for (int i = 0; i < transactionCount; ++i) {
                    final int transactionID = graph.getTransaction(i);
                    graph.setBooleanValue(transactionSelectedAttribute, transactionID, selectedNodeList.contains(transactionID));
                }
            }
        }

        @Override
        public String getName() {
            return "SelectOnGraphPlugin2";
        }

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}

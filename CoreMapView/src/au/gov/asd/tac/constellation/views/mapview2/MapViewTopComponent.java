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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.GeoShapePolygonMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import java.awt.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ScrollPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component for the MapView
 *
 * @author serpens24
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
    @ActionReference(path = "Menu/Experimental/Views", position = 4000)
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

    private static final Logger LOGGER = Logger.getLogger(MapViewTopComponent.class.getName());

    public static final Object LOCK = new Object();

    // The mapview itself
    private final MapViewPane mapViewPane;
    private final Consumer<Graph> updateMarkers;
    private Graph currentGraph;  // store the currently viewed graph to allow recalcultion of points if map changes

    private int markerID = 0;

    private final MapViewTopComponent self = this;

    public MapViewTopComponent() {

        setName(Bundle.CTL_MapViewTopComponent2());
        setToolTipText(Bundle.HINT_MapViewTopComponent2());


        initComponents();
        mapViewPane = new MapViewPane(this);
        mapViewPane.setUpMap();
        super.initContent();

        updateMarkers = graph -> {
            mapViewPane.clearQuerriesMarkers();
            runExtractCoordsFromGraphPlugin(graph);
        };

        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.LATITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.LONGITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.SHAPE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.TransactionAttribute.LATITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.TransactionAttribute.LONGITUDE, updateMarkers);
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
    }

    @Override
    protected void handleComponentClosed() {

        super.handleComponentClosed();
        mapViewPane.getMap().clearQueriedMarkers();
    }

    public int getNewMarkerID() {
        return ++markerID;
    }

    public void setMapImage(final Component mapComponent) {

        SwingUtilities.invokeLater(() -> {
            jfxContainer.add(mapComponent);
            validate();
        });
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

    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return mapViewPane.getAllMarkers();
    }

    /**
     * Re-extract all coordinates from the graph and determine their position on the map. If map changes, or displayed
     * graph changes markers need recalculation.
     */
    public void recalculateCoords() {
      runExtractCoordsFromGraphPlugin(currentGraph);
    }

    /**
     * When a new graph is created handle updating the UI
     *
     * @param graph
     */
    @Override
    protected void handleNewGraph(final Graph graph) {
        currentGraph = graph;
        super.handleNewGraph(graph);
        recalculateCoords();
    }

    @Override
    protected void handleGraphChange(final GraphChangeEvent event) {
        runExtractCoordsFromGraphPlugin(currentGraph);
    }
    
    public void runExtractCoordsFromGraphPlugin(final Graph graph) {
        if (graph != null) {
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(self)).executeNow(graph);
                    } catch (final PluginException e) {
                        LOGGER.log(Level.SEVERE, "Exception thrown: {0}", e.getMessage());
                    } catch (final InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "Exception thrown: {0}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }).get();

            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown: {0}", ex.getMessage());
                Thread.currentThread().interrupt();

            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown: {0}", ex.getMessage());
            }

            Platform.runLater(() -> {
                mapViewPane.getMap().redrawQueriedMarkers();
                mapViewPane.getMap().refreshMapLayers();
            });
        }
    }

    /**
     * Get the mapViewPane
     *
     * @return the mapViewPane
     */
    public MapViewPane getMapViewPane() {
        return mapViewPane;
    }

    public boolean shouldUpdate() {
        return this.needsUpdate();
    }

    public int getContentWidth() {
        return getWidth();
    }

    /**
     * Adds marker on the map
     *
     * @param key - key which is the coordinate of the marker
     * @param e - the marker itself
     */
    public void addMarker(final String key, final AbstractMarker e) {
        if (getMapViewPane().getMap() != null) {
            getMapViewPane().getMap().addMarkerToHashMap(key, e);
        }
    }

    public int getContentHeight() {
        return getHeight();
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    @PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
    @NbBundle.Messages("ExtractCoordsFromGraphPlugin=Extracts Coordinates from Graph")
    protected static class ExtractCoordsFromGraphPlugin extends SimpleReadPlugin {

        private MapViewTopComponent mapViewTopComponent;

        public ExtractCoordsFromGraphPlugin(final MapViewTopComponent topComponent) {
            mapViewTopComponent = topComponent;
        }

        @Override
        public String getName() {
            return "ExtractCoordsFromGraphPlugin";
        }

        /**
         * Read the graph to extract geo coordinated
         *
         * @param graph - the current graph
         * @param interaction
         * @param parameters
         * @throws InterruptedException
         * @throws PluginException
         */
        @Override
        protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            if (graph != null) {
                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};

                mapViewTopComponent.getMapViewPane().getMap().clearQueriedMarkers();

                for (final GraphElementType elementType : elementTypes) {
                    // Ids for all attributes needed from a single vertext of a graph
                    int lonID = GraphConstants.NOT_FOUND;
                    int latID = GraphConstants.NOT_FOUND;
                    int geoShapeID = GraphConstants.NOT_FOUND;
                    int colourID = GraphConstants.NOT_FOUND;
                    int blazeID = GraphConstants.NOT_FOUND;
                    int overlayID = GraphConstants.NOT_FOUND;
                    int labelAttrID = GraphConstants.NOT_FOUND;
                    int identifierID = GraphConstants.NOT_FOUND;
                    int selectedID = GraphConstants.NOT_FOUND;

                    final int elementCount;

                    switch (elementType) {
                        case VERTEX:
                            // Get IDs
                            lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                            latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                            geoShapeID = SpatialConcept.VertexAttribute.SHAPE.get(graph);
                            colourID = VisualConcept.VertexAttribute.COLOR.get(graph);
                            blazeID = VisualConcept.VertexAttribute.BLAZE.get(graph);
                            overlayID = VisualConcept.VertexAttribute.OVERLAY_COLOR.get(graph);
                            labelAttrID = VisualConcept.VertexAttribute.LABEL.get(graph);
                            identifierID = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
                            selectedID = VisualConcept.VertexAttribute.SELECTED.get(graph);
                            elementCount = graph.getVertexCount();
                            break;
                        case TRANSACTION:
                            lonID = SpatialConcept.TransactionAttribute.LONGITUDE.get(graph);
                            latID = SpatialConcept.TransactionAttribute.LATITUDE.get(graph);
                            selectedID = VisualConcept.VertexAttribute.SELECTED.get(graph);
                            elementCount = graph.getTransactionCount();
                            break;
                        default:
                            continue;
                    }

                    // Loop though every graph element
                    for (int elementPos = 0; elementPos < elementCount; elementPos++) {
                        final int elementID = elementType == GraphElementType.VERTEX ? graph.getVertex(elementPos) : graph.getTransaction(elementPos);
                        boolean processingGeoShape = false;
                        // For all the vertices
                        if (((lonID != GraphConstants.NOT_FOUND && latID != GraphConstants.NOT_FOUND) || geoShapeID != GraphConstants.NOT_FOUND) && elementType == GraphElementType.VERTEX) {
                            // Get lattitude and longitude
                            final Float elementLat = graph.getObjectValue(latID, elementID);
                            final Float elementLon = graph.getObjectValue(lonID, elementID);
                            
                            // Only render elements that are within the geographical bounds of the selected map 
                            if (elementLat == null || elementLon == null || !MapConversions.isPointOnMap(elementLat, elementLon)) {
                                continue;
                            }
                            
                            String elementShape = null;

                            if (geoShapeID != GraphConstants.NOT_FOUND) {
                                elementShape = graph.getObjectValue(geoShapeID, elementID);
                            }

                            if (elementShape != null) {
                                processingGeoShape = true;
                            }

                            if ((elementLat == null || elementLon == null) && StringUtils.isEmpty(elementShape)) {
                                continue;
                            }

                            // Get the colour defined in the nodes color attribute
                            final ConstellationColor attributeColour = graph.getObjectValue(colourID, elementID);
                            ConstellationColor blazeColour = null;
                            ConstellationColor overlayColour = null;
                            String labelAttr = null;
                            String identAttr = null;
                            boolean elementSelected = false;

                            // Get other colours if they are available
                            if (blazeID != GraphConstants.NOT_FOUND) {
                                final String colourStr = graph.getStringValue(blazeID, elementID);
                                if (colourStr != null) {
                                    blazeColour = ConstellationColor.getColorValue(colourStr.split(SeparatorConstants.SEMICOLON)[1]);
                                }
                            }

                            if (overlayID != GraphConstants.NOT_FOUND) {
                                overlayColour = graph.getObjectValue(overlayID, elementID);
                            }

                            // Get label text if they are available
                            if (labelAttrID != GraphConstants.NOT_FOUND) {
                                labelAttr = graph.getStringValue(labelAttrID, elementID);
                            }

                            if (identifierID != GraphConstants.NOT_FOUND) {
                                identAttr = graph.getStringValue(identifierID, elementID);
                            }

                            if (selectedID != GraphConstants.NOT_FOUND) {
                                elementSelected = graph.getBooleanValue(selectedID, elementID);
                            }
                            
                            if (!elementSelected && mapViewTopComponent.getMapViewPane().getMap().getMarkersShowing().contains(AbstractMarker.MarkerType.SELECTED)) {
                                continue;
                            }
                            
                            // Generate a key from the vertex coordinate
                            final String coordinateKey = (double) elementLat + "," + (double) elementLon;

                            if (processingGeoShape) {
                                final JSONObject json = new JSONObject(elementShape);
                                final JSONArray featureList = json.getJSONArray("features");
                                final GeoShapePolygonMarker gsp = new GeoShapePolygonMarker(mapViewTopComponent.getMapViewPane().getMap(), mapViewTopComponent.getNewMarkerID(), elementID);
                                for (int i = 0; i < featureList.length(); ++i) {
                                    final JSONObject latLongObj = featureList.getJSONObject(i).getJSONObject("geometry");
                                    final JSONArray latLongList = latLongObj.getJSONArray("coordinates");

                                    for (int j = 0; j < latLongList.length(); ++j) {
                                        
                                        // This is the euqivalent key for geomarker
                                        final String keyString = latLongList.getJSONArray(j).toString();
                                        if (!mapViewTopComponent.getAllMarkers().containsKey(keyString)) {
                                            gsp.addGeoShape(keyString, elementID);
                                            mapViewTopComponent.addMarker(keyString, gsp);

                                        } else {
                                            final GeoShapePolygonMarker existing = (GeoShapePolygonMarker) mapViewTopComponent.getAllMarkers().get(latLongList.getJSONArray(j).toString());
                                            existing.getGeoShapes().get(latLongList.getJSONArray(j).toString()).getValue().add(elementID);
                                        }
                                    }

                                    final String markerKey = latLongList.getJSONArray(0).toString();
                                    final GeoShapePolygonMarker addedMarker = (GeoShapePolygonMarker) mapViewTopComponent.getAllMarkers().get(markerKey);
                                    addedMarker.setAttributeColour(attributeColour, markerKey);
                                    addedMarker.setBlazeColour(blazeColour, markerKey);
                                    addedMarker.setOverlayColour(overlayColour, markerKey);
                                    addedMarker.setLabelAttr(labelAttr, markerKey);
                                    addedMarker.setIdentAttr(identAttr, markerKey);
                                }
                            } else {
                                
                                // We are processing a PointMarker. Either a PointMarker already exists with corresponding location  coordinateKey)
                                // in which case we re-use this marker, or we are adding a new marker.
                                final PointMarker p;
                                if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                                    // Create a new point marker and add it to the map
                                    p = new PointMarker(mapViewTopComponent.getMapViewPane().getMap(), mapViewTopComponent.getNewMarkerID(), elementID, (double) elementLat, (double) elementLon, attributeColour);
                                    mapViewTopComponent.addMarker(coordinateKey, p);
                                } else {
                                    p = (PointMarker) mapViewTopComponent.getAllMarkers().get(coordinateKey);
                                    if (p.getConnectedNodeIdList().get(0) != elementID) {
                                        p.addNodeID(elementID);
                                    }
                                }
                                if (elementSelected) {
                                    p.select();
                                }
                                
                                // Set colours and labels if they are available
                                p.setBlazeColour(blazeColour);
                                p.setOverlayColour(overlayColour);
                                p.setLabelAttr(labelAttr);
                                p.setIdentAttr(identAttr);
                            }
                        }
                        processingGeoShape = false;
                    }
                }
            }
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

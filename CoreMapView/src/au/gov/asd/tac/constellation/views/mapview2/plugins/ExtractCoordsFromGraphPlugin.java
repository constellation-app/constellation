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
package au.gov.asd.tac.constellation.views.mapview2.plugins;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugin to extract geo-coordinates form graph node
 *
 * @author altair1673
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
@NbBundle.Messages("ExtractCoordsFromGraphPlugin=Extracts Coordinates from Graph")
public class ExtractCoordsFromGraphPlugin extends SimpleReadPlugin {
    private static final Logger LOGGER = Logger.getLogger(ExtractCoordsFromGraphPlugin.class.getName());

    private static final double POINT_MARKER_X_OFFSET = 95;
    private static final double POINT_MARKER_Y_OFFSET = 245;

    private MapViewTopComponent mapViewTopComponent;

    public ExtractCoordsFromGraphPlugin() {

    }

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
                int colourID = GraphConstants.NOT_FOUND;
                int blazeID = GraphConstants.NOT_FOUND;
                int overlayID = GraphConstants.NOT_FOUND;
                int labelAttrID = GraphConstants.NOT_FOUND;
                int identifierID = GraphConstants.NOT_FOUND;

                final int elementCount;

                switch (elementType) {
                    case VERTEX:
                        // Get IDs
                        lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                        latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                        colourID = VisualConcept.VertexAttribute.COLOR.get(graph);
                        blazeID = VisualConcept.VertexAttribute.BLAZE.get(graph);
                        overlayID = VisualConcept.VertexAttribute.OVERLAY_COLOR.get(graph);
                        labelAttrID = VisualConcept.VertexAttribute.LABEL.get(graph);
                        identifierID = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
                        elementCount = graph.getVertexCount();
                        break;
                    case TRANSACTION:
                        lonID = SpatialConcept.TransactionAttribute.LONGITUDE.get(graph);
                        latID = SpatialConcept.TransactionAttribute.LATITUDE.get(graph);
                        elementCount = graph.getTransactionCount();
                        break;
                    default:
                        continue;
                }

                // Loop though every graph element
                for (int elementPos = 0; elementPos < elementCount; elementPos++) {
                    final int elementID = elementType == GraphElementType.VERTEX ? graph.getVertex(elementPos) : graph.getTransaction(elementPos);

                    // For all the vertices
                    if (lonID != GraphConstants.NOT_FOUND && latID != GraphConstants.NOT_FOUND && elementType == GraphElementType.VERTEX) {
                        // Get lattitude and longitude
                        final Float elementLat = graph.getObjectValue(latID, elementID);
                        final Float elementLon = graph.getObjectValue(lonID, elementID);

                        // Get the nodes colour
                        final String elementColour = graph.getStringValue(colourID, elementID);

                        String blazeColour = null;
                        String overlayColour = null;
                        String labelAttr = null;
                        String identAttr = null;

                        // Get other ccolous if they are available
                        if (blazeID != GraphConstants.NOT_FOUND) {
                            blazeColour = graph.getStringValue(blazeID, elementID);
                        }

                        if (overlayID != GraphConstants.NOT_FOUND) {
                            overlayColour = graph.getStringValue(overlayID, elementID);
                        }

                        // Get label text if they are available
                        if (labelAttrID != GraphConstants.NOT_FOUND) {
                            labelAttr = graph.getStringValue(labelAttrID, elementID);
                        }

                        if (identifierID != GraphConstants.NOT_FOUND) {
                            identAttr = graph.getStringValue(identifierID, elementID);
                        }

                        // Generate a key from the vertex coordinate
                        final String coordinateKey = (double) elementLat + "," + (double) elementLon;

                        // If another vertext of the same location hasn't been queried yet
                        if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                            // Create a new point marker and add it to the map
                            final PointMarker p = new PointMarker(mapViewTopComponent.getMapViewPane().getMap(), mapViewTopComponent.getNewMarkerID(), elementID, (double) elementLat, (double) elementLon, 0.05, POINT_MARKER_X_OFFSET, POINT_MARKER_Y_OFFSET, elementColour); //244
                            mapViewTopComponent.addMarker(coordinateKey, p);

                            // Set colours and labels if they are available
                            if (blazeColour != null) {
                                p.setBlazeColour(blazeColour);
                            }

                            if (overlayColour != null) {
                                p.setOverlayColour(overlayColour);
                            }

                            if (labelAttr != null) {
                                p.setLabelAttr(labelAttr);
                            }

                            if (identAttr != null) {
                                p.setIdentAttr(identAttr);
                            }

                        } else {

                            if (blazeColour != null) {
                                ((PointMarker) mapViewTopComponent.getAllMarkers().get(coordinateKey)).setBlazeColour(blazeColour);

                            }

                            if (overlayColour != null) {

                                ((PointMarker) mapViewTopComponent.getAllMarkers().get(coordinateKey)).setOverlayColour(overlayColour);

                            }

                            if (labelAttr != null) {
                                ((PointMarker) mapViewTopComponent.getAllMarkers().get(coordinateKey)).setLabelAttr(labelAttr);
                            }

                            if (identAttr != null) {
                                ((PointMarker) mapViewTopComponent.getAllMarkers().get(coordinateKey)).setIdentAttr(identAttr);
                            }

                            if (mapViewTopComponent.getAllMarkers().get(coordinateKey).getConnectedNodeIdList().get(0) != elementID) {
                                mapViewTopComponent.getAllMarkers().get(coordinateKey).addNodeID(elementID);
                            }

                        }

                    }

                }

            }

        }

    }

}

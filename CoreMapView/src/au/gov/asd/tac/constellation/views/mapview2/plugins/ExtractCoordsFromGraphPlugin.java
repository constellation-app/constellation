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
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
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
import au.gov.asd.tac.constellation.views.mapview2.markers.LineMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author altair1673
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
@NbBundle.Messages("ExtractCoordsFromGraphPlugin=Extracts geographic coordinates from the graph")
public class ExtractCoordsFromGraphPlugin extends SimpleReadPlugin {

    //private final Logger LOGGER = Logger.getLogger("ExtractCoords");
    private static final Logger LOGGER = Logger.getLogger(ExtractCoordsFromGraphPlugin.class.getName());

    private MapViewTopComponent mapViewTopComponent;
    private boolean transactionsOnly;

    public ExtractCoordsFromGraphPlugin() {

    }

    public ExtractCoordsFromGraphPlugin(final MapViewTopComponent topComponent) {
        mapViewTopComponent = topComponent;
    }

    @Override
    public String getName() {
        return "ExtractCoordsFromGraphPlugin";
    }

    @Override
    protected void read(final GraphReadMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {

        if (graph != null) {

            final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};
            //mapViewTopComponent.mapViewPane.getMap().clearListeners();
            mapViewTopComponent.mapViewPane.getMap().clearQueriedMarkers();

            try {
                for (GraphElementType elementType : elementTypes) {
                    int lonID = GraphConstants.NOT_FOUND;
                    int latID = GraphConstants.NOT_FOUND;
                    int colourID = GraphConstants.NOT_FOUND;
                    int blazeID = GraphConstants.NOT_FOUND;
                    int overlayID = GraphConstants.NOT_FOUND;
                    int labelAttrID = GraphConstants.NOT_FOUND;
                    int identifierID = GraphConstants.NOT_FOUND;

                    int elementCount;

                    switch (elementType) {
                        case VERTEX:
                            lonID = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                            latID = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                            colourID = VisualConcept.VertexAttribute.COLOR.get(graph);
                            blazeID = VisualConcept.VertexAttribute.BLAZE.get(graph);
                            overlayID = VisualConcept.VertexAttribute.OVERLAY_COLOR.get(graph);
                            labelAttrID = VisualConcept.VertexAttribute.LABEL.get(graph);
                            identifierID = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
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

                        if (lonID != GraphConstants.NOT_FOUND && latID != GraphConstants.NOT_FOUND && elementType == GraphElementType.VERTEX && elementID != -99) {
                            final float elementLat = graph.getObjectValue(latID, elementID);
                            final float elementLon = graph.getObjectValue(lonID, elementID);

                            final String elementColour = graph.getStringValue(colourID, elementID);

                            String blazeColour = null;
                            String overlayColour = null;
                            String labelAttr = null;
                            String identAttr = null;

                            if (blazeID != GraphConstants.NOT_FOUND) {
                                blazeColour = graph.getStringValue(blazeID, elementID);
                            }

                            if (overlayID != GraphConstants.NOT_FOUND) {
                                overlayColour = graph.getStringValue(overlayID, elementID);
                            }

                            if (labelAttrID != GraphConstants.NOT_FOUND) {
                                labelAttr = graph.getStringValue(labelAttrID, elementID);
                            }

                            if (identifierID != GraphConstants.NOT_FOUND) {
                                identAttr = graph.getStringValue(identifierID, elementID);
                            }

                            //LOGGER.log(Level.SEVERE, "Node colour:" + blazeColour);

                            String coordinateKey = (double) elementLat + "," + (double) elementLon;
                            if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                                PointMarker p = new PointMarker(mapViewTopComponent.mapViewPane.getMap(), mapViewTopComponent.getNewMarkerID(), elementID, (double) elementLat, (double) elementLon, 0.05, 95, 245, elementColour); //244
                                mapViewTopComponent.addMarker(coordinateKey, p);

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

                                //LOGGER.log(Level.SEVERE, "Corrdindate key: " + coordinateKey);
                                /*Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mapViewTopComponent.mapViewPane.drawMarker(p);
                                    }
                                });*/

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

                                if (mapViewTopComponent.getAllMarkers().get(coordinateKey).getIdList().get(0) != elementID) {
                                    mapViewTopComponent.getAllMarkers().get(coordinateKey).addNodeID(elementID);
                                }

                            }

                            //mapViewTopComponent.drawMarkerOnMap(elementLat, elementLon, 0.05);
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

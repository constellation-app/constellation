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
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
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
                            LOGGER.log(Level.SEVERE, "Lattitude: " + latID + ", Longitude: " + lonID);

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
                            PointMarker p = new PointMarker(mapViewTopComponent.mapViewPane.getMap(), mapViewTopComponent.getNewMarkerID(), elementID, (double) elementLat, (double) elementLon, 0.05, 95, 244);
                            String coordinateKey = (double) elementLat + "," + (double) elementLon;
                            if (!mapViewTopComponent.getAllMarkers().keySet().contains(coordinateKey)) {
                                mapViewTopComponent.addMarker(coordinateKey, p);
                                LOGGER.log(Level.SEVERE, "Corrdindate key: " + coordinateKey);
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

                            if (dateTime != null && !dateTime.isBlank()) {
                                int sourceID = graph.getTransactionSourceVertex(elementID);
                                int destinationID = graph.getTransactionDestinationVertex(elementID);

                                final float sourceLat = graph.getObjectValue(latID, sourceID);
                                final float sourceLon = graph.getObjectValue(lonID, sourceID);

                                final float destLat = graph.getObjectValue(latID, destinationID);
                                final float destLon = graph.getObjectValue(lonID, destinationID);

                                String coordinateKey = (double) sourceLat + "," + (double) sourceLon + "," + (double) destLat + "," + (double) destLon;

                                LineMarker l = new LineMarker(mapViewTopComponent.mapViewPane.getMap(), mapViewTopComponent.getNewMarkerID(), elementID, (float) sourceLat, (float) sourceLon, (float) destLat, (float) destLon, 0, 149);
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

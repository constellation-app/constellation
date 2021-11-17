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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ObjectCache;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationMultiFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationClusterMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationLineMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationMarkerFactory;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationMultiMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationPointMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationPolygonMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.geo.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A cache for storing markers and the {@link GraphElement} objects they
 * represent.
 *
 * @author cygnus_x-1
 */
public abstract class MarkerCache extends ObjectCache<ConstellationAbstractMarker, GraphElement> {

    private static final Logger LOGGER = Logger.getLogger(MarkerCache.class.getName());

    private static final int CLUSTER_DISTANCE = 80;
    private static final String MULTIPLE_VALUES = "<Multiple Values>";

    protected final Object lock = new Object();

    public static MarkerCache getDefault() {
        return Lookup.getDefault().lookup(MarkerCache.class);
    }

    public Set<ConstellationAbstractMarker> buildMarkers(final Graph graph, final ConstellationMarkerFactory markerFactory) {
        assert !SwingUtilities.isEventDispatchThread();

        // clear cache (retaining custom markers)
        synchronized (lock) {
            final Set<ConstellationAbstractMarker> customMarkers = getCustomMarkers();
            clear();
            customMarkers.forEach(marker -> add(marker, GraphElement.NON_ELEMENT));

            if (graph != null && markerFactory != null) {
                // update cache (calculate graph markers)
                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};
                final ReadableGraph readableGraph = graph.getReadableGraph();
                try {
                    for (final GraphElementType graphElementType : elementTypes) {
                        final int elementLatitudeAttributeId;
                        final int elementLongitudeAttributeId;
                        final int elementShapeAttributeId;
                        final int elementCount;
                        switch (graphElementType) {
                            case VERTEX:
                                elementLatitudeAttributeId = SpatialConcept.VertexAttribute.LATITUDE.get(readableGraph);
                                elementLongitudeAttributeId = SpatialConcept.VertexAttribute.LONGITUDE.get(readableGraph);
                                elementShapeAttributeId = SpatialConcept.VertexAttribute.SHAPE.get(readableGraph);
                                elementCount = readableGraph.getVertexCount();
                                break;
                            case TRANSACTION:
                                elementLatitudeAttributeId = SpatialConcept.TransactionAttribute.LATITUDE.get(readableGraph);
                                elementLongitudeAttributeId = SpatialConcept.TransactionAttribute.LONGITUDE.get(readableGraph);
                                elementShapeAttributeId = SpatialConcept.TransactionAttribute.SHAPE.get(readableGraph);
                                elementCount = readableGraph.getTransactionCount();
                                break;
                            default:
                                continue;
                        }

                        for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                            final int elementId;
                            switch (graphElementType) {
                                case VERTEX:
                                    elementId = readableGraph.getVertex(elementPosition);
                                    break;
                                case TRANSACTION:
                                    elementId = readableGraph.getTransaction(elementPosition);
                                    break;
                                default:
                                    elementId = GraphConstants.NOT_FOUND;
                                    break;
                            }

                            boolean shapeAdded = false;
                            if (elementShapeAttributeId != GraphConstants.NOT_FOUND) {
                                final String elementShape = readableGraph.getStringValue(elementShapeAttributeId, elementId);
                                if (elementShape != null && Shape.isValidGeoJson(elementShape)) {
                                    final List<ConstellationAbstractFeature> shapes = new ArrayList<>();
                                    try {
                                        final List<ConstellationAbstractFeature> features = GeoJSONReader.loadDataFromJSON(null, elementShape).stream()
                                                .map(FeatureUtilities::convert).collect(Collectors.toList());
                                        shapes.addAll(features);
                                        shapeAdded = true;
                                        markerFactory.createMarkers(shapes).forEach(marker -> {
                                            final GraphElement element = new GraphElement(elementId, graphElementType);
                                            add(marker, element);
                                        });
                                    } catch (Exception ex) {
                                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                                    }
                                }
                            }
                            if (!shapeAdded && elementLatitudeAttributeId != GraphConstants.NOT_FOUND && elementLongitudeAttributeId != GraphConstants.NOT_FOUND) {
                                final Float elementLatitude = readableGraph.getObjectValue(elementLatitudeAttributeId, elementId);
                                final Float elementLongitude = readableGraph.getObjectValue(elementLongitudeAttributeId, elementId);
                                if (elementLatitude != null && elementLongitude != null) {
                                    final Location location = new Location(elementLatitude, elementLongitude);
                                    final ConstellationAbstractFeature point = new ConstellationPointFeature(location);
                                    try {
                                        final ConstellationAbstractMarker marker = markerFactory.createMarker(point);
                                        final GraphElement element = new GraphElement(elementId, graphElementType);
                                        add(marker, element);
                                    } catch (Exception ex) {
                                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    readableGraph.release();
                }
            }
        }

        return getAllMarkers();
    }

    public Set<ConstellationClusterMarker> buildClusters(final UnfoldingMap map, final ConstellationMarkerFactory markerFactory, final MarkerState markerState) {
        assert !SwingUtilities.isEventDispatchThread();

        // clear cache of cluster markers
        final Set<ConstellationAbstractMarker> oldClusterMarkers = cache.keySet().stream()
                .filter(ConstellationClusterMarker.class::isInstance)
                .collect(Collectors.toSet());
        oldClusterMarkers.forEach(marker -> cache.remove(marker));

        final Set<ConstellationClusterMarker> clusterMarkers = new HashSet<>();
        if (markerState.isShowClusterMarkers()) {
            // generate points for clusters
            final Map<DoublePoint, List<ConstellationAbstractMarker>> markerPoints = new HashMap<>();
            cache.keySet().forEach(marker -> {
                final DoublePoint point = new DoublePoint(
                        new double[]{
                            map.getScreenPosition(marker.getLocation()).x,
                            map.getScreenPosition(marker.getLocation()).y});
                if (!markerPoints.containsKey(point)) {
                    markerPoints.put(point, new ArrayList<>());
                }
                markerPoints.get(point).add(marker);
            });

            // calculate new clusters
            final DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<>(CLUSTER_DISTANCE, 0);
            final List<Cluster<DoublePoint>> clusters = clusterer.cluster(markerPoints.keySet());

            // build new cluster markers
            clusters.forEach(cluster -> {
                try {
                    final List<ConstellationAbstractMarker> markersInCluster = cluster.getPoints().stream()
                            .map(markerPoints::get)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    final ConstellationClusterMarker clusterMarker = new ConstellationClusterMarker();
                    clusterMarker.setColor(MarkerUtilities.DEFAULT_CLUSTER_COLOR);
                    clusterMarker.setMarkers(markersInCluster);
                    clusterMarkers.add(clusterMarker);
                    add(clusterMarker, GraphElement.NON_ELEMENT);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }

        return clusterMarkers;
    }

    public void styleMarkers(final Graph graph, final MarkerState markerState) {
        assert !SwingUtilities.isEventDispatchThread();

        // update style based on graph attributes
        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int elementMixColorAttributeId = VisualConcept.GraphAttribute.MIX_COLOR.get(readableGraph);
                final ConstellationColor mixColor = elementMixColorAttributeId != Graph.NOT_FOUND ? readableGraph.getObjectValue(elementMixColorAttributeId, 0) : null;
                synchronized (lock) {
                    forEach((marker, elementList) -> {
                        if (marker != null) {
                            final Set<String> labels = new HashSet<>();
                            final Set<ConstellationColor> colors = new HashSet<>();
                            boolean selected = false;
                            boolean dimmed = false;
                            boolean hidden = false;

                            // get relevent attribute ids
                            int elementVisibilityAttributeId;
                            int elementDimmedAttributeId;
                            int elementSelectedAttributeId;
                            int elementLabelAttributeId;
                            int elementColorAttributeId;
                            for (final GraphElement element : elementList) {
                                switch (element.getType()) {
                                    case VERTEX:
                                        if(markerState.getLabel() == null){
                                            elementLabelAttributeId = GraphConstants.NOT_FOUND;
                                        }else{
                                            elementLabelAttributeId = markerState.getLabel().getVertexAttribute() == null ? GraphConstants.NOT_FOUND
                                                : markerState.getLabel().getVertexAttribute().get(readableGraph);
                                        }
                                        
                                        if(markerState.getColorScheme() == null){
                                            elementColorAttributeId = GraphConstants.NOT_FOUND;
                                        }else{
                                            elementColorAttributeId = markerState.getColorScheme().getVertexAttribute() == null ? GraphConstants.NOT_FOUND
                                                : markerState.getColorScheme().getVertexAttribute().get(readableGraph);
                                        }
                                        elementSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
                                        elementDimmedAttributeId = VisualConcept.VertexAttribute.DIMMED.get(readableGraph);
                                        elementVisibilityAttributeId = VisualConcept.VertexAttribute.VISIBILITY.get(readableGraph);
                                        break;
                                    case TRANSACTION:
                                        if(markerState.getLabel() == null){
                                            elementLabelAttributeId = GraphConstants.NOT_FOUND;
                                        }else{
                                            elementLabelAttributeId = markerState.getLabel().getTransactionAttribute() == null ? GraphConstants.NOT_FOUND
                                                : markerState.getLabel().getTransactionAttribute().get(readableGraph);
                                        }
                                        
                                        if(markerState.getColorScheme() == null){
                                            elementColorAttributeId = GraphConstants.NOT_FOUND;
                                        }else{
                                            elementColorAttributeId = markerState.getColorScheme().getTransactionAttribute() == null ? GraphConstants.NOT_FOUND
                                                : markerState.getColorScheme().getTransactionAttribute().get(readableGraph);
                                        }
                                        
                                        elementSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
                                        elementDimmedAttributeId = VisualConcept.TransactionAttribute.DIMMED.get(readableGraph);
                                        elementVisibilityAttributeId = VisualConcept.TransactionAttribute.VISIBILITY.get(readableGraph);
                                        break;
                                    default:
                                        if (marker.isCustom()) {
                                            colors.add(MarkerUtilities.value(MarkerUtilities.DEFAULT_CUSTOM_COLOR));
                                            selected |= marker.isSelected();
                                            dimmed |= marker.isDimmed();
                                        }
                                        continue;
                                }

                                final int elementId = element.getId();

                                // get label
                                if (elementLabelAttributeId != GraphConstants.NOT_FOUND) {
                                    labels.add(readableGraph.getStringValue(elementLabelAttributeId, elementId));
                                }

                                // get color
                                if (elementColorAttributeId != GraphConstants.NOT_FOUND) {
                                    switch (markerState.getColorScheme()) {
                                        case COLOR:
                                        case OVERLAY:
                                            colors.add(readableGraph.getObjectValue(elementColorAttributeId, elementId));
                                            break;
                                        case BLAZE:
                                            // give the blazes a color and default to the color scheme for non blazed nodes
                                            if (readableGraph.getObjectValue(elementColorAttributeId, elementId) != null) {
                                                colors.add(((Blaze) readableGraph.getObjectValue(elementColorAttributeId, elementId)).getColor());
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                // get selected
                                if (elementSelectedAttributeId != GraphConstants.NOT_FOUND
                                        && readableGraph.getBooleanValue(elementSelectedAttributeId, elementId)) {
                                    selected |= true;
                                }

                                // get dimming
                                if (elementDimmedAttributeId != GraphConstants.NOT_FOUND
                                        && readableGraph.getBooleanValue(elementDimmedAttributeId, elementId)) {
                                    dimmed |= true;
                                }

                                // get visibility
                                if (elementVisibilityAttributeId != GraphConstants.NOT_FOUND
                                        && readableGraph.getFloatValue(elementVisibilityAttributeId, elementId) <= 0) {
                                    hidden |= true;
                                }
                            }

                            // update label
                            if (labels.isEmpty()) {
                                marker.setId(null);
                            } else if (labels.size() == 1) {
                                marker.setId(labels.iterator().next());
                            } else {
                                marker.setId(MULTIPLE_VALUES);
                            }

                            // update color
                            if (colors.isEmpty()) {
                                marker.setColor(MarkerUtilities.DEFAULT_COLOR);
                            } else if (colors.size() == 1) {
                                marker.setColor(MarkerUtilities.color(colors.iterator().next()));
                            } else {
                                marker.setColor(MarkerUtilities.color(mixColor));
                            }

                            // update selection
                            marker.setSelected(selected);

                            // update dimming
                            marker.setDimmed(dimmed);
                            // update visibility
                            if ((markerState.isShowSelectedOnly() && !marker.isSelected())
                                    || (!markerState.isShowPointMarkers() && marker instanceof ConstellationPointMarker)
                                    || (!markerState.isShowLineMarkers() && marker instanceof ConstellationLineMarker)
                                    || (!markerState.isShowPolygonMarkers() && marker instanceof ConstellationPolygonMarker)
                                    || (!markerState.isShowMultiMarkers() && marker instanceof ConstellationMultiMarker)
                                    || (!markerState.isShowClusterMarkers() && marker instanceof ConstellationClusterMarker)) {
                                marker.setHidden(true);
                            } else {
                                marker.setHidden(hidden);
                            }
                        }
                    });
                }
            } finally {
                readableGraph.release();
            }
        }
    }

    public abstract Set<ConstellationAbstractMarker> getAllMarkers();

    public abstract void clearAllMarkers();

    public abstract Set<ConstellationAbstractMarker> getSelectedMarkers();

    public abstract void clearSelectedMarkers();

    public abstract Set<ConstellationAbstractMarker> getCustomMarkers();

    public abstract void clearCustomMarkers();
}

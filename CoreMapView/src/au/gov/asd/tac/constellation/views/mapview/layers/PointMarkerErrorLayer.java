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
package au.gov.asd.tac.constellation.views.mapview.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.utilities.geospatial.Distance.Haversine;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationPointMarker;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapPosition;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * An overlay for visualising the error region associated with point markers.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 1000)
public class PointMarkerErrorLayer extends MapLayer {

    private static final int STROKE_COLOR = MarkerUtilities.color(255, 255, 0, 0);
    private static final int ERROR_REGION_COLOR = MarkerUtilities.color(127, 255, 0, 0);
    private static final float DEFAULT_PRECISION = 0F;

    private int markerCount = 0;

    @Override
    public String getName() {
        return "Point Marker Error Region (Experimental)";
    }

    @Override
    public boolean requiresUpdate() {
        return markerCount != map.getMarkers().size();
    }

    @Override
    public PImage update() {
        if (map.getMarkers().isEmpty()) {
            return null;
        }

        markerCount = map.getMarkers().size();

        final int width = renderer.width - 5;
        final int height = renderer.height - 5;

        // get on screen markers
        final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());
        final List<ConstellationAbstractMarker> markers = renderer.getMarkerCache().getAllMarkers().stream()
                .filter(marker -> {
                    final ScreenPosition markerPosition = map.getScreenPosition(marker.getLocation());
                    return markerPosition != null
                            && markerPosition.x > topLeft.x
                            && markerPosition.y > topLeft.y
                            && markerPosition.x < bottomRight.x
                            && markerPosition.y < bottomRight.y;
                })
                .collect(Collectors.toList());

        // create error region data from markers
        final PGraphics errorRegionImage = renderer.createGraphics(width, height, PConstants.JAVA2D);
        errorRegionImage.beginDraw();
        errorRegionImage.stroke(STROKE_COLOR);
        errorRegionImage.fill(ERROR_REGION_COLOR);

        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int vertexPrecisionAttributeId = SpatialConcept.VertexAttribute.PRECISION.get(readableGraph);
                final int transactionPrecisionAttributeId = SpatialConcept.TransactionAttribute.PRECISION.get(readableGraph);

                for (final ConstellationAbstractMarker marker : markers) {
                    if (!(marker instanceof ConstellationPointMarker)) {
                        continue;
                    }

                    float minimumPrecision = DEFAULT_PRECISION;
                    final Set<GraphElement> elements = renderer.getMarkerCache().get(marker);
                    for (final GraphElement element : elements) {
                        final float elementPrecision;
                        switch (element.getType()) {
                            case VERTEX:
                                if (vertexPrecisionAttributeId != Graph.NOT_FOUND) {
                                    elementPrecision = readableGraph.getFloatValue(vertexPrecisionAttributeId, element.getId());
                                    minimumPrecision = Math.max(elementPrecision, minimumPrecision);
                                } else {
                                    elementPrecision = DEFAULT_PRECISION;
                                    minimumPrecision = DEFAULT_PRECISION;
                                }
                                break;
                            case TRANSACTION:
                                elementPrecision = transactionPrecisionAttributeId != Graph.NOT_FOUND
                                        ? readableGraph.getFloatValue(transactionPrecisionAttributeId, element.getId())
                                        : DEFAULT_PRECISION;
                                break;
                            default:
                                elementPrecision = DEFAULT_PRECISION;
                                break;
                        }
                        minimumPrecision = Math.max(elementPrecision, minimumPrecision);
                    }

                    // don't bother drawing if there isn't a precision
                    if (minimumPrecision == 0) {
                        continue;
                    }

                    final Location errorRegionRadiusLocation = new Location(
                            marker.getLocation().getLat() - Haversine.kilometersToDecimalDegrees(minimumPrecision),
                            marker.getLocation().getLon() - Haversine.kilometersToDecimalDegrees(minimumPrecision));
                    final List<Location> errorRegionLocations = MarkerUtilities.generateCircle(marker.getLocation(), errorRegionRadiusLocation);
                    final List<MapPosition> errorRegionPositions = errorRegionLocations.stream()
                            .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                            .collect(Collectors.toList());
                    errorRegionImage.beginShape();
                    errorRegionPositions.forEach(position -> errorRegionImage.vertex(position.x, position.y));
                    errorRegionImage.endShape(PConstants.CLOSE);
                }
            } finally {
                readableGraph.release();
            }
        }

        errorRegionImage.endDraw();

        return errorRegionImage;
    }
}

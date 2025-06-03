/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * A layer which generates a paths between markers in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class AbstractPathsLayer extends MapLayer {

    private static final int MAX_LINE_WIDTH = 10;
    private static final ConstellationColor SRC_COLOR = ConstellationColor.fromHtmlColor(("#7f0000"));
    private static final ConstellationColor DST_COLOR = ConstellationColor.fromHtmlColor(("#007f00"));
    private static final int N_COLORS = 6;

    private String graphId = "";
    private long structureModCount = -1;
    private int onScreenMarkerCount = 0;

    @Override
    public boolean requiresUpdate() {
        if (drawPathsToOffscreenMarkers()) {

            // check the graph isn't null
            if (graph == null) {
                boolean update = !graphId.isEmpty();
                if (update) {
                    graphId = "";
                    structureModCount = -1;
                }

                return update;
            }

            // check that we have the same graph, and that it hasn't been structurally modified
            try {
                final Tuple<String, Long> graphTuple = graph.readFromGraph(reader -> Tuple.create(reader.getId(), reader.getStructureModificationCounter()));
                if (!graphTuple.getFirst().equals(graphId)) {
                    graphId = graphTuple.getFirst();
                    return true;
                }
                if (graphTuple.getSecond() != structureModCount) {
                    structureModCount = graphTuple.getSecond();
                    return true;
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());
        return onScreenMarkerCount != renderer.getMarkerCache().keys().stream()
                .filter(marker -> {
                    final ScreenPosition markerPosition = map.getScreenPosition(marker.getLocation());
                    return !marker.isHidden()
                            && markerPosition != null
                            && markerPosition.x > topLeft.x
                            && markerPosition.y > topLeft.y
                            && markerPosition.x < bottomRight.x
                            && markerPosition.y < bottomRight.y;
                }).count();
    }

    @Override
    public PImage update() {
        if (graph == null) {
            return null;
        }

        final Set<Marker> onScreenMarkers;

        final List<Tuple<GraphElement, GraphElement>> paths = new ArrayList<>();
        try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
            // update on screen markers, collecting the ids of the vertices involved in valid paths along the way
            final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
            final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());
            onScreenMarkers = renderer.getMarkerCache().keys().stream()
                    .filter(marker -> {
                        final ScreenPosition markerPosition = map.getScreenPosition(marker.getLocation());
                        final boolean onScreen
                                = markerPosition != null
                                && markerPosition.x > topLeft.x
                                && markerPosition.y > topLeft.y
                                && markerPosition.x < bottomRight.x
                                && markerPosition.y < bottomRight.y;

                        if (drawPathsToOffscreenMarkers() || onScreen) {
                            final Set<GraphElement> elementsAtMarker = renderer.getMarkerCache().get(marker);
                            if (elementsAtMarker != null) {
                                elementsAtMarker.forEach(element -> paths.addAll(getPathsForElement(readableGraph, element)));
                            }
                        }

                        return onScreen;
                    }).collect(Collectors.toSet());

            onScreenMarkerCount = onScreenMarkers.size();
        }

        if (onScreenMarkers.isEmpty()) {
            return null;
        }

        final Map<GraphElement, Marker> elementToMarkerCache = new HashMap<>();
        renderer.getMarkerCache().keys().forEach(marker -> renderer.getMarkerCache().get(marker).forEach(element -> elementToMarkerCache.put(element, marker)));

        // set up a color palette
        final int[] palette = Arrays.asList(ConstellationColor.createLinearPalette(N_COLORS, SRC_COLOR, DST_COLOR)).stream()
                .mapToInt(c -> MarkerUtilities.color(c)).toArray();

        final int width = renderer.width - 5;
        final int height = renderer.height - 5;
        final PGraphics pathsImage = renderer.createGraphics(width, height, PConstants.JAVA2D);
        pathsImage.beginDraw();

        // deduplicate paths, storing duplicate counts
        int maxWeight = 1;
        final Map<Tuple<GraphElement, GraphElement>, Integer> dedupedPaths = new HashMap<>();
        for (final Tuple<GraphElement, GraphElement> path : paths) {
            if (dedupedPaths.containsKey(path)) {
                final int weight = dedupedPaths.get(path) + 1;
                if (weight > maxWeight) {
                    maxWeight = weight;
                }
                dedupedPaths.put(path, weight);
            } else {
                dedupedPaths.put(path, 1);
            }
        }

        // draw weighted paths
        final int maxWeightFinal = maxWeight;
        dedupedPaths.forEach((path, weight) -> {
            final Marker sourceMarker = elementToMarkerCache.get(path.getFirst());
            final Marker destinationMarker = elementToMarkerCache.get(path.getSecond());
            final boolean validPath = (drawPathsToOffscreenMarkers()
                    && (onScreenMarkers.contains(sourceMarker) || onScreenMarkers.contains(destinationMarker)))
                    || (onScreenMarkers.contains(sourceMarker) && onScreenMarkers.contains(destinationMarker));

            if (validPath) {
                final Location sourceLocation = sourceMarker != null ? sourceMarker.getLocation() : null;
                final Location destinationLocation = destinationMarker != null ? destinationMarker.getLocation() : null;
                if (sourceLocation != null && destinationLocation != null) {
                    final ScreenPosition sourcePosition = map.getScreenPosition(sourceLocation);
                    final ScreenPosition destinationPosition = map.getScreenPosition(destinationLocation);
                    final float lineWidth = Math.max(maxWeightFinal > MAX_LINE_WIDTH
                            ? (MAX_LINE_WIDTH * (weight / (float) maxWeightFinal)) + 1 : weight + 1, 2);
                    pathsImage.strokeWeight(lineWidth);

                    pathsImage.pushMatrix();
                    pathsImage.translate(sourcePosition.x, sourcePosition.y);
                    pathsImage.rotate(PApplet.atan2((destinationPosition.y - sourcePosition.y), (destinationPosition.x - sourcePosition.x)));

                    final float translatedDestiniationPosition = (float) Math.hypot(destinationPosition.x - sourcePosition.x, destinationPosition.y - sourcePosition.y);
                    drawColoredLine(pathsImage, translatedDestiniationPosition, lineWidth, palette);

                    pathsImage.popMatrix();
                }
            }
        });

        pathsImage.endDraw();
        return pathsImage;
    }

    /**
     * Draw a multi-colored line from (0,0) to (0,d).
     * <p>
     * Arrowheads clutter things up when a lot of them meet at the same place.
     * Instead, we'll draw a multi-colored line to indicate head and tail.
     * <p>
     * Since only vertical lines are drawn, it is assumed that the relevant
     * graphics transformations have been done before this.
     *
     * @param image
     * @param d
     * @param width
     * @param palette
     */
    private static void drawColoredLine(final PGraphics image, final float d, final float width, final int[] palette) {
        image.strokeWeight(width);
        image.strokeCap(PConstants.SQUARE);
        for (int i = 0; i < N_COLORS; i++) {
            final float x0 = i * d / N_COLORS;
            final float y0 = width / 2;
            final float x1 = (i + 1) * d / N_COLORS;
            final float y1 = width / 2;
            image.stroke(palette[i]);
            image.fill(palette[i]);
            image.line(x0, y0, x1, y1);
        }
    }

    public abstract List<Tuple<GraphElement, GraphElement>> getPathsForElement(final ReadableGraph graph, final GraphElement element);

    public abstract boolean drawPathsToOffscreenMarkers();
}

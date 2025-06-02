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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PConstants;
import processing.core.PImage;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 600)
public class ThiessenPolygonsLayer extends MapLayer {

    private int onScreenMarkerCount = 0;

    @Override
    public String getName() {
        return "Thiessen Polygons";
    }

    @Override
    public boolean requiresUpdate() {
        final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());
        return onScreenMarkerCount != map.getMarkers().stream()
                .filter(marker -> {
                    final ScreenPosition markerPosition = marker.getLocation() != null
                            ? map.getScreenPosition(marker.getLocation())
                            : null;
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
        // update on screen markers
        final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());

        final List<Marker> onScreenMarkers = map.getMarkers().stream()
                .filter(marker -> {
                    final ScreenPosition markerPosition = map.getScreenPosition(marker.getLocation());
                    return !marker.isHidden()
                            && markerPosition != null
                            && markerPosition.x > topLeft.x
                            && markerPosition.y > topLeft.y
                            && markerPosition.x < bottomRight.x
                            && markerPosition.y < bottomRight.y;
                })
                .toList();

        onScreenMarkerCount = onScreenMarkers.size();

        if (onScreenMarkers.isEmpty()) {
            return null;
        }

        // map markers to screen positions
        final Map<Marker, ScreenPosition> positionMap = onScreenMarkers.stream()
                .collect(Collectors.toMap(marker -> marker,
                        marker -> map.getScreenPosition(marker.getLocation()),
                        (marker1, marker2) -> marker1));

        // map markers to colors
        final ConstellationColor[] palette = ConstellationColor.createPalette(onScreenMarkerCount);
        final Map<Marker, Integer> paletteMap = new HashMap<>();
        onScreenMarkers.forEach(marker -> paletteMap.put(marker,
                MarkerUtilities.color(palette[onScreenMarkers.indexOf(marker)])));

        final int width = renderer.width - 5;
        final int height = renderer.height - 5;
        final PImage voronoiImage = renderer.createImage(width, height, PConstants.ARGB);
        voronoiImage.loadPixels();

        for (int pixelIndex = 0; pixelIndex < voronoiImage.pixels.length; pixelIndex++) {
            // find the closest marker to this pixel
            final ScreenPosition pixelPosition = pixelPosition(pixelIndex, width, height);
            Marker closestMarker = null;
            for (final Marker marker : onScreenMarkers) {
                if (closestMarker == null) {
                    closestMarker = marker;
                } else {
                    final ScreenPosition markerPosition = positionMap.get(marker);
                    final ScreenPosition closestMarkerPosition = positionMap.get(closestMarker);
                    if (markerPosition != null && closestMarkerPosition != null
                            && euclidianDistance((int) pixelPosition.x, (int) pixelPosition.y, (int) markerPosition.x, (int) markerPosition.y)
                            < euclidianDistance((int) pixelPosition.x, (int) pixelPosition.y, (int) closestMarkerPosition.x, (int) closestMarkerPosition.y)) {
                        closestMarker = marker;
                    }
                }
            }

            // color this pixel based on its closest marker
            if (closestMarker != null && paletteMap.get(closestMarker) != null) {
                voronoiImage.pixels[pixelIndex] = paletteMap.get(closestMarker);
            }
        }

        voronoiImage.updatePixels();
        return voronoiImage;
    }

    private ScreenPosition pixelPosition(final int pixel, final int width, final int height) {
        if (pixel > width * height) {
            throw new IllegalArgumentException("pixel is outside of the image bounds");
        }
        return new ScreenPosition(Math.floorMod(pixel, width), Math.floorDiv(pixel, width));
    }

    private double euclidianDistance(final int x1, final int y1, final int x2, final int y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }
}

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

import au.gov.asd.tac.constellation.utilities.image.GaussianBlur;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.List;
import java.util.stream.Collectors;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * A layer which generates a heatmap over markers in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class AbstractHeatmapLayer extends MapLayer {

    private static final int RADIUS = 16;
    private static final int PASSES = 3;
    private static final int THRESHOLD = 20;
    private static final float SEVERITY = 2F;

    private int onScreenMarkerCount = 0;

    @Override
    public boolean requiresUpdate() {
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
        // update on screen markers
        final ScreenPosition topLeft = map.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = map.getScreenPosition(map.getBottomRightBorder());
        final List<Marker> onScreenMarkers = renderer.getMarkerCache().keys().stream()
                .filter(marker -> {
                    final ScreenPosition markerPosition = map.getScreenPosition(marker.getLocation());
                    return !marker.isHidden()
                            && markerPosition != null
                            && markerPosition.x > topLeft.x
                            && markerPosition.y > topLeft.y
                            && markerPosition.x < bottomRight.x
                            && markerPosition.y < bottomRight.y;
                })
                .collect(Collectors.toList());

        onScreenMarkerCount = onScreenMarkers.size();

        if (onScreenMarkers.isEmpty()) {
            return null;
        }

        // create point image from markers
        final int width = renderer.width - 5;
        final int height = renderer.height - 5;
        final float[] pointImage = new float[width * height];
        onScreenMarkers.forEach(marker -> {
            final ConstellationAbstractMarker constellationMarker = (ConstellationAbstractMarker) marker;
            final ScreenPosition markerPosition = map.getScreenPosition(constellationMarker.getLocation());
            final float markerWeight = getWeight(constellationMarker);
            int markerPosImageElement = (int) markerPosition.y * width + (int) markerPosition.x;
            while (markerPosImageElement >= pointImage.length) {
                markerPosImageElement -= width;
            }
            if (markerPosition != null && markerPosImageElement < pointImage.length) {
                pointImage[markerPosImageElement] = markerWeight;
            }
        });

        // generate gaussian blur around points
        final float[] gaussImage = new float[width * height];
        GaussianBlur.gaussianBlurBox(pointImage, gaussImage,
                width, height, RADIUS, PASSES, GaussianBlur.BoxBlurType.FASTEST);

        final PImage heatmapImage = renderer.createImage(width, height, PConstants.ARGB);
        heatmapImage.loadPixels();
        GaussianBlur.colorise(gaussImage, heatmapImage.pixels, THRESHOLD, SEVERITY);
        heatmapImage.updatePixels();
        return heatmapImage;
    }

    protected abstract float getWeight(final ConstellationAbstractMarker marker);
}

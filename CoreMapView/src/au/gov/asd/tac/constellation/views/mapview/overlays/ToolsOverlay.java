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
package au.gov.asd.tac.constellation.views.mapview.overlays;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.views.mapview.MapViewPluginRegistry;
import static au.gov.asd.tac.constellation.views.mapview.MapViewTileRenderer.LOCK;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature.ConstellationFeatureType;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationShapeFeature;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerCache;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An overlay providing tools for use in the Map View.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapOverlay.class)
public class ToolsOverlay extends MapOverlay {

    private static final Logger LOGGER = Logger.getLogger(ToolsOverlay.class.getName());
    private static final String DISABLED = "Disabled";

    private enum MeasurementSystem {

        IMPERIAL("mi", (start, end) -> Distance.Haversine.estimateDistanceInMiles(start.getLat(), start.getLon(), end.getLat(), end.getLon())),
        METRIC("km", (start, end) -> Distance.Haversine.estimateDistanceInKilometers(start.getLat(), start.getLon(), end.getLat(), end.getLon())),
        NAUTICAL("nmi", (start, end) -> Distance.Haversine.estimateDistanceInNauticalMiles(start.getLat(), start.getLon(), end.getLat(), end.getLon()));

        private final String displayText;
        private final BiFunction<Location, Location, Double> measureFunction;

        private MeasurementSystem(final String displayText, final BiFunction<Location, Location, Double> measureFunction) {
            this.displayText = displayText;
            this.measureFunction = measureFunction;
        }

        public String getDisplayText() {
            return displayText;
        }

        public BiFunction<Location, Location, Double> getMeasureFunction() {
            return measureFunction;
        }
    }

    private MeasurementSystem measureSystem = MeasurementSystem.METRIC;
    private boolean mouseLeftMeasureSystemRegion = true;
    private boolean mouseLeftMeasureToolRegion = true;
    private boolean measureActive = false;
    private boolean measureFinished = false;
    private boolean measurePath = false;
    private final List<Location> measureVertices = new ArrayList<>();
    private boolean measureCircle = false;
    private int measureOriginX = -1;
    private int measureOriginY = -1;
    private int measureDeltaX = -1;
    private int measureDeltaY = -1;

    private boolean mouseLeftDrawToolRegion = true;
    private boolean mouseLeftAddToGraphRegion = true;
    private boolean drawActive = false;
    private boolean drawPolygon = false;
    private boolean drawCircle = false;
    private final List<Location> drawVertices = new ArrayList<>();
    private int drawOriginX = -1;
    private int drawOriginY = -1;
    private int drawDeltaX = -1;
    private int drawDeltaY = -1;

    @Override
    public String getName() {
        return "Tools Overlay";
    }

    @Override
    public float getX() {
        return renderer.getComponent().getX() + renderer.getComponent().getWidth() - 10F - width;
    }

    @Override
    public float getY() {
        return renderer.getComponent().getY() + 10F;
    }

    private Location getMeasureToolStart() {
        if (measureOriginX == -1 && measureOriginY == -1) {
            return null;
        }
        return map.getLocation(measureOriginX, measureOriginY);
    }

    private Location getMeasureToolEnd() {
        if (measureDeltaX == -1 && measureDeltaY == -1) {
            return null;
        }
        return map.getLocation(measureDeltaX, measureDeltaY);
    }

    private Location getDrawToolStart() {
        if (drawOriginX == -1 && drawOriginY == -1) {
            return null;
        }
        return map.getLocation(drawOriginX, drawOriginY);
    }

    private Location getDrawToolEnd() {
        if (drawDeltaX == -1 && drawDeltaY == -1) {
            return null;
        }
        return map.getLocation(drawDeltaX, drawDeltaY);
    }

    @Override
    public void overlay() {
        boolean leftMousePressed = renderer.mouseButton == PConstants.LEFT;

        // draw tool overlay
        renderer.noStroke();
        renderer.fill(BACKGROUND_COLOR);
        renderer.rect(x, y, width, height);

        float yOffset = y + MARGIN;

        // update measure tool state
        final float measureToolX = x + 60;
        final float measureToolWidth = VALUE_BOX_MEDIUM_WIDTH + VALUE_BOX_SHORT_WIDTH;
        final float measureSystemX = x + 60 + VALUE_BOX_MEDIUM_WIDTH + VALUE_BOX_SHORT_WIDTH + (PADDING * 2);
        final float measureSystemWidth = VALUE_BOX_SHORT_WIDTH;
        if (renderer.mouseX > measureToolX && renderer.mouseX < measureToolX + measureToolWidth
                && renderer.mouseY > yOffset && renderer.mouseY < yOffset + VALUE_BOX_HEIGHT) {
            if (leftMousePressed && mouseLeftMeasureToolRegion && !drawActive) {
                measureActive = !measureActive;
                mouseLeftMeasureToolRegion = false;
            }
        } else {
            mouseLeftMeasureToolRegion = true;
        }
        if (!measureActive && renderer.mouseX > measureSystemX && renderer.mouseX < measureSystemX + measureSystemWidth
                && renderer.mouseY > yOffset && renderer.mouseY < yOffset + VALUE_BOX_HEIGHT) {
            if (leftMousePressed && mouseLeftMeasureSystemRegion) {
                final MeasurementSystem[] measurementSystems = MeasurementSystem.values();
                measureSystem = measurementSystems[(measureSystem.ordinal() + 1) % measurementSystems.length];
                mouseLeftMeasureSystemRegion = false;
            }
        } else {
            mouseLeftMeasureSystemRegion = true;
        }

        // draw measure tool
        drawLabel("Measure", x + 60, yOffset);
        drawValue(measureSystem.getDisplayText(), measureSystemX, yOffset, measureSystemWidth, false, false);
        if (drawActive) {
            final Location start = getDrawToolStart();
            final Location end = getDrawToolEnd();
            if (start != null && end != null) {
                final float distance = measureSystem.getMeasureFunction().apply(start, end).floatValue();
                drawValue(String.format("%s", PApplet.nf(distance, 1, 3)), measureToolX, yOffset, measureToolWidth, false, false);
            } else {
                drawValue(DISABLED, measureToolX, yOffset, measureToolWidth, false, false);
            }
        } else if (measureActive) {
            final Location start = getMeasureToolStart();
            final Location end = getMeasureToolEnd();
            if (start != null && end != null) {
                float distance = measureSystem.getMeasureFunction().apply(start, end).floatValue();
                if (measurePath) {
                    for (int i = 1; i < measureVertices.size(); i++) {
                        final Location startVertex = measureVertices.get(i - 1);
                        final Location endVertex = measureVertices.get(i);
                        distance += measureSystem.getMeasureFunction().apply(startVertex, endVertex).floatValue();
                    }
                }
                drawValue(String.format("%s", PApplet.nf(distance, 1, 3)), measureToolX, yOffset, measureToolWidth, false, true);
            } else {
                drawValue("Enabled", measureToolX, yOffset, measureToolWidth, false, true);
            }
        } else {
            drawValue(DISABLED, measureToolX, yOffset, measureToolWidth, false, false);
        }

        // update map based on measure tool state
        if (mouseLeftMeasureToolRegion && measureActive) {
            final int measureToolColor = renderer.color(255, 0, 0, 127);
            renderer.stroke(measureToolColor);
            if (measurePath && measureVertices.size() > 1) {
                for (int i = 1; i < measureVertices.size(); i++) {
                    final ScreenPosition previousVertex = map.getScreenPosition(measureVertices.get(i - 1));
                    final ScreenPosition currentVertex = map.getScreenPosition(measureVertices.get(i));
                    renderer.strokeWeight(5);
                    renderer.point(previousVertex.x, previousVertex.y);
                    renderer.point(currentVertex.x, currentVertex.y);
                    renderer.strokeWeight(2);
                    renderer.line(previousVertex.x, previousVertex.y, currentVertex.x, currentVertex.y);
                }
            } else {
                renderer.strokeWeight(5);
                renderer.point(measureOriginX, measureOriginY);
            }
            if (measureOriginX != -1 && measureOriginY != -1
                    && measureDeltaX != -1 && measureDeltaY != -1) {
                renderer.strokeWeight(5);
                renderer.point(measureDeltaX, measureDeltaY);
                renderer.strokeWeight(2);
                renderer.line(measureOriginX, measureOriginY, measureDeltaX, measureDeltaY);
                if (measureCircle) {
                    final float radius = (float) Math.sqrt(
                            Math.pow((measureDeltaX - measureOriginX), 2)
                            + Math.pow((measureDeltaY - measureOriginY), 2));
                    renderer.noFill();
                    renderer.strokeWeight(2);
                    renderer.ellipseMode(PConstants.RADIUS);
                    renderer.ellipse(measureOriginX, measureOriginY, radius, radius);
                }
            }
        }

        // draw separator
        yOffset += VALUE_BOX_HEIGHT + PADDING * 2;
        drawSeparator(yOffset);
        yOffset += PADDING * 2;

        // update draw tool state
        final float drawToolX = x + 60;
        final float drawToolWidth = VALUE_BOX_MEDIUM_WIDTH + VALUE_BOX_SHORT_WIDTH;
        final float addToGraphX = x + 60 + VALUE_BOX_MEDIUM_WIDTH + VALUE_BOX_SHORT_WIDTH + (PADDING * 2);
        final float addToGraphWidth = VALUE_BOX_SHORT_WIDTH;
        if (renderer.mouseX > drawToolX && renderer.mouseX < drawToolX + drawToolWidth
                && renderer.mouseY > yOffset && renderer.mouseY < yOffset + VALUE_BOX_HEIGHT) {
            if (leftMousePressed && mouseLeftDrawToolRegion && !measureActive) {
                drawActive = !drawActive;
                mouseLeftDrawToolRegion = false;
            }
        } else {
            mouseLeftDrawToolRegion = true;
        }
        if (!drawActive && renderer.mouseX > addToGraphX && renderer.mouseX < addToGraphX + addToGraphWidth
                && renderer.mouseY > yOffset && renderer.mouseY < yOffset + VALUE_BOX_HEIGHT) {
            if (leftMousePressed && mouseLeftAddToGraphRegion) {
                try {
                    final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
                    PluginExecution.withPlugin(MapViewPluginRegistry.COPY_CUSTOM_MARKERS_TO_GRAPH)
                            .executeNow(currentGraph);

                    final MarkerCache markerCache = renderer.getMarkerCache();
                    markerCache.getCustomMarkers().forEach(marker -> {
                        markerCache.get(marker).remove(GraphElement.NON_ELEMENT);
                        marker.setCustom(false);
                    });

                    renderer.updateMarkers(currentGraph, renderer.getMarkerState());
                } catch (final PluginException ex) {
                    LOGGER.log(Level.SEVERE, "Error copying custom markers to graph", ex);
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Error copying custom markers to graph", ex);
                    Thread.currentThread().interrupt();
                }
                mouseLeftAddToGraphRegion = false;
            }
        } else {
            mouseLeftAddToGraphRegion = true;
        }

        // draw draw tool
        drawLabel("Draw", x + 60, yOffset);
        drawValue("+", addToGraphX, yOffset, addToGraphWidth, false, false);
        if (drawActive) {
            drawValue("Enabled", drawToolX, yOffset, drawToolWidth, false, true);

            yOffset += VALUE_BOX_HEIGHT + (PADDING * 4);

            final float drawDescriptionHeight = (VALUE_BOX_HEIGHT * 16) + (PADDING * 2) + 1;
            renderer.noStroke();
            renderer.fill(BACKGROUND_COLOR);
            renderer.rect(x, yOffset - 1, width, drawDescriptionHeight);

            final String drawDescription = " > Click on the map to draw a point marker.\n"
                    + " > Click on the map while holding shift to begin drawing a circle"
                    + "  marker, click again with or without shift to complete it.\n"
                    + " > Click on the map while holding control to begin drawing a polygon"
                    + "  marker, continue clicking while holding control to draw edges,"
                    + "  then release control and click once more to complete it.\n"
                    + " > Click on a drawn marker to remove it.";
            drawInfo(drawDescription, yOffset - (PADDING * 2), width - (MARGIN * 2) - (PADDING * 2), true);
        } else {
            drawValue(DISABLED, drawToolX, yOffset, drawToolWidth, false, false);
        }

        // update map based on draw tool state
        if (mouseLeftDrawToolRegion && drawActive) {
            final int polygonColor = renderer.color(0, 0, 0, 127);
            renderer.stroke(polygonColor);
            renderer.strokeJoin(PConstants.ROUND);
            renderer.strokeCap(PConstants.ROUND);
            if (drawPolygon && drawVertices.size() > 1) {
                for (int i = 1; i < drawVertices.size(); i++) {
                    final ScreenPosition previousVertex = map.getScreenPosition(drawVertices.get(i - 1));
                    final ScreenPosition currentVertex = map.getScreenPosition(drawVertices.get(i));
                    renderer.strokeWeight(5);
                    renderer.point(previousVertex.x, previousVertex.y);
                    renderer.point(currentVertex.x, currentVertex.y);
                    renderer.strokeWeight(2);
                    renderer.line(previousVertex.x, previousVertex.y, currentVertex.x, currentVertex.y);
                }
            } else {
                renderer.strokeWeight(5);
                renderer.point(drawOriginX, drawOriginY);
            }
            if (drawOriginX != -1 && drawOriginY != -1
                    && drawDeltaX != -1 && drawDeltaY != -1) {
                renderer.strokeWeight(5);
                renderer.point(drawDeltaX, drawDeltaY);
                renderer.strokeWeight(2);
                renderer.line(drawOriginX, drawOriginY, drawDeltaX, drawDeltaY);
                if (drawCircle) {
                    final float radius = (float) Math.sqrt(
                            Math.pow((drawDeltaX - drawOriginX), 2)
                            + Math.pow((drawDeltaY - drawOriginY), 2));
                    renderer.noFill();
                    renderer.strokeWeight(2);
                    renderer.ellipseMode(PConstants.RADIUS);
                    renderer.ellipse(drawOriginX, drawOriginY, radius, radius);
                }
            }
        }

        active = measureActive || drawActive;
    }

    @Override
    public void mouseMoved(final MouseEvent event) {

        // update measure line
        if (measureActive && !measureFinished) {
            measureDeltaX = event.getX();
            measureDeltaY = event.getY();
        }

        // update draw line
        if (drawActive) {
            drawDeltaX = event.getX();
            drawDeltaY = event.getY();
        }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        if (event.getButton() == PConstants.LEFT) {

            // draw measure line
            if (mouseLeftMeasureToolRegion && measureActive) {
                if (measureOriginX == -1 && measureOriginY == -1) {
                    if (event.isControlDown()) {
                        measurePath = true;
                        measureVertices.add(map.getLocation(event.getX(), event.getY()));
                    } else if (event.isShiftDown()) {
                        measureCircle = true;
                    } else {
                        // Do nothing
                    }
                    measureFinished = false;
                    measureOriginX = event.getX();
                    measureOriginY = event.getY();
                } else {
                    if (event.isControlDown()) {
                        measureVertices.add(map.getLocation(event.getX(), event.getY()));
                        measureOriginX = event.getX();
                        measureOriginY = event.getY();
                    } else {
                        measureFinished = true;
                        measurePath = false;
                        measureVertices.clear();
                        measureCircle = false;
                        measureOriginX = -1;
                        measureOriginY = -1;
                        measureDeltaX = -1;
                        measureDeltaY = -1;
                    }
                }
            }

            // draw markers
            final List<ConstellationAbstractMarker> hitMarkers;
            synchronized (LOCK) {
                hitMarkers = map.getHitMarkers(event.getX(), event.getY()).stream()
                        .map(ConstellationAbstractMarker.class::cast)
                        .collect(Collectors.toList());
            }
            if (mouseLeftDrawToolRegion && drawActive) {
                final MarkerCache markerCache = renderer.getMarkerCache();
                final Location clickLocation = map.getLocation(event.getX(), event.getY());
                if (hitMarkers.isEmpty()) {
                    if (event.isControlDown()) {
                        drawPolygon = true;
                        drawVertices.add(clickLocation);
                        drawOriginX = event.getX();
                        drawOriginY = event.getY();
                    } else if (event.isShiftDown() && !drawCircle) {
                        drawCircle = true;
                        drawOriginX = event.getX();
                        drawOriginY = event.getY();
                    } else {
                        if (drawPolygon) {
                            final ConstellationShapeFeature clickFeature;
                            if (drawVertices.size() > 2) {
                                clickFeature = new ConstellationShapeFeature(ConstellationFeatureType.POLYGON);
                            } else if (drawVertices.size() == 2) {
                                clickFeature = new ConstellationShapeFeature(ConstellationFeatureType.LINE);
                            } else {
                                return;
                            }
                            drawVertices.forEach(clickFeature::addLocation);
                            renderer.addCustomMarker(clickFeature);
                            drawPolygon = false;
                            drawVertices.clear();
                            drawOriginX = -1;
                            drawOriginY = -1;
                            drawDeltaX = -1;
                            drawDeltaY = -1;
                        } else if (drawCircle) {
                            drawVertices.addAll(MarkerUtilities.generateCircle(
                                    map.getLocation(drawOriginX, drawOriginY),
                                    map.getLocation(drawDeltaX, drawDeltaY)));
                            final ConstellationShapeFeature clickFeature = new ConstellationShapeFeature(ConstellationFeatureType.POLYGON);
                            drawVertices.forEach(clickFeature::addLocation);
                            renderer.addCustomMarker(clickFeature);
                            drawCircle = false;
                            drawVertices.clear();
                            drawOriginX = -1;
                            drawOriginY = -1;
                            drawDeltaX = -1;
                            drawDeltaY = -1;
                        } else {
                            final ConstellationPointFeature clickFeature = new ConstellationPointFeature(clickLocation);
                            renderer.addCustomMarker(clickFeature);
                        }
                    }
                } else {
                    hitMarkers.forEach(hitMarker -> {
                        if (hitMarker.isCustom()) {
                            markerCache.get(hitMarker).remove(GraphElement.NON_ELEMENT);
                            if (markerCache.get(hitMarker).isEmpty()) {
                                map.getMarkers().remove(hitMarker);
                                markerCache.remove(hitMarker);
                            } else {
                                hitMarker.setCustom(false);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseWheel(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        // DO NOTHING
    }
}

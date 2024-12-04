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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/**
 * Parent class to heatmap classes
 *
 * @author altair1673
 */
public abstract class AbstractHeatmapLayer extends AbstractMapLayer {

    // Group to hold all heatmap graphical elements
    protected Group layerGroup;

    private static final double X_OFFSET = -7.1;
    private static final double Y_OFFSET = -12.8;
    
    private static final int RED_THRESHOLD = 1000;
    private static final int ORANGERED_THRESHOLD = 500;
    private static final int ORANGE_THRESHOLD = 100;
    private static final int YELLOW_THRESHOLD = 50;
    private static final int LIME_THRESHOLD = 10;
    private static final int GREEN_THRESHOLD = 5;
    private static final int FADEDGREEN_THRESHOLD = 1;        
    private static final Color RED_MARKER = Color.color(0.3, 0, 0);
    private static final Color ORANGE_MARKER = Color.color(0.27, 0.2, 0);
    private static final Color YELLOW_MARKER = Color.color(0.26, 0.26, 0);
    private static final Color GREEN_MARKER = Color.color(0.18, 0.27, 0);
    private static final Color BLUE_MARKER = Color.color(0, 0, 0.3);
    private static final Color RED = Color.color(1, 0, 0, 0.5);
    private static final Color RED_FADE = Color.color(1, 0.5, 0.5, 0.05);
    private static final Color ORANGE = Color.color(1, 0.5, 0, 0.5);
    private static final Color ORANGE_FADE = Color.color(1, 0.678, 0.367, 0.05);
    private static final Color YELLOW = Color.color(1, 0.918, 0, 0.5);
    private static final Color YELLOW_FADE = Color.color(1, 0.945, 0.367, 0.05);
    private static final Color LIME = Color.color(0.5, 1, 0.35, 0.5);
    private static final Color LIME_FADE = Color.color(0.75, 1, 0.6, 0.05);
    private static final Color BLUE = Color.color(0, 0, 1, 0.3);
    private static final Color BLUE_FADE = Color.color(0.318, 0.318, 1, 0.05);
    private static final Color TRANSPARENT = Color.color(1, 1, 1, 0.0001);

    private final Map<String, Shape> shapeCache = new HashMap<>();
    private final Map<String, Integer> weightCache = new HashMap<>();

    protected AbstractHeatmapLayer(final MapView parent, final int id) {
        super(parent, id);
        layerGroup = new Group();
    }

    /**
     * Set up the heatmap layer
     */
    @Override
    public void setUp() {        
        // Loop through all the markers
        for (final AbstractMarker value : parent.getAllMarkers().values()) {

            // If marker is a point marker
            if (value instanceof PointMarker) {
                final PointMarker marker = (PointMarker) value;
                
                // check if thispoint marker is within another marker's area
                final String nearbyId = idOfNearbyShape(marker);
                final int currentWeight = getWeight(marker);
                
                if (nearbyId != null) {
                    // add the weights
                    final Integer cachedWeight = weightCache.get(nearbyId);
                    final Integer largerWeight = currentWeight > cachedWeight ? currentWeight : cachedWeight;
                    weightCache.put(nearbyId, largerWeight);
                    
                    // get the union of the shapes
                    final Shape cachedShape = shapeCache.get(nearbyId);
                    final Circle circlePoint = new Circle(16/Math.pow(MapView.getMapScale(), 0.85));
                    circlePoint.setCenterX(marker.getX());
                    circlePoint.setCenterY(marker.getY());
                    
                    final Shape combinedShape = Shape.union(cachedShape, circlePoint);
                    combinedShape.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.75, true, CycleMethod.NO_CYCLE, getStopsForWeight(largerWeight)));
                    combinedShape.setOpacity(0.75);
                    shapeCache.put(nearbyId, combinedShape);
                    
                } else {
                    final String currentId = marker.getIdentAttr();
                    weightCache.put(currentId, currentWeight);

                    final Circle initialCircle = new Circle(16/Math.pow(MapView.getMapScale(), 0.85), new RadialGradient(0, 0, 0.5, 0.5, 0.75, true, CycleMethod.NO_CYCLE, getStopsForWeight(currentWeight)));
                    initialCircle.setCenterX(marker.getX());
                    initialCircle.setCenterY(marker.getY());
                    initialCircle.setOpacity(0.75);
                    shapeCache.put(currentId, initialCircle);
                }
            }
        }
        
        for (final String cacheId : shapeCache.keySet()) {
            final Shape currentShape = shapeCache.get(cacheId);
            final Integer cachedWeight = weightCache.get(cacheId);

            final double fontSize = 12/Math.pow(MapView.getMapScale(), 0.86);
            final Text weightText = new Text();
            weightText.setText(" " + cachedWeight + " ");
            final double markerCentreX = currentShape.getBoundsInParent().getCenterX();
            final double markerCentreY = currentShape.getBoundsInParent().getCenterY();            
            weightText.setX(markerCentreX + X_OFFSET/Math.pow(MapView.getMapScale(), 0.85));
            weightText.setY(markerCentreY + Y_OFFSET/Math.pow(MapView.getMapScale(), 0.87));    
            weightText.setFill(getMarkerColor(cachedWeight));
            weightText.setStyle(" -fx-font-size: " + fontSize + ";");

            // Add text to group
            layerGroup.getChildren().add(weightText);
            layerGroup.getChildren().add(currentShape);
        }
    }

    private Stop[] getStopsForWeight(final int weight){
        Stop[] stops;

        if (weight >= RED_THRESHOLD) {
            stops = new Stop[] {new Stop(0, RED), new Stop(0.35, RED), new Stop(0.7, RED_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= ORANGERED_THRESHOLD) {
            stops = new Stop[] {new Stop(0, RED), new Stop(0.2, RED), new Stop(0.45, ORANGE), new Stop(0.75, ORANGE_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= ORANGE_THRESHOLD) {
            stops = new Stop[] {new Stop(0, RED), new Stop(0.35, ORANGE), new Stop(0.5, YELLOW), new Stop(0.75, YELLOW_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= YELLOW_THRESHOLD) {
            stops = new Stop[] {new Stop(0, RED), new Stop(0.1, ORANGE), new Stop(0.2, ORANGE), new Stop(0.5, YELLOW), new Stop(0.75, LIME_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= LIME_THRESHOLD) {
            stops = new Stop[] {new Stop(0, ORANGE), new Stop(0.4, YELLOW), new Stop(0.6, LIME),  new Stop(0.75, LIME_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= GREEN_THRESHOLD) {
            stops = new Stop[] {new Stop(0, YELLOW), new Stop(0.15, YELLOW), new Stop(0.5, LIME),  new Stop(0.75, LIME_FADE), new Stop(1, TRANSPARENT)};
        } else if (weight >= FADEDGREEN_THRESHOLD) {
            stops = new Stop[] {new Stop(0, LIME), new Stop(0.3, LIME),  new Stop(0.6, LIME_FADE), new Stop(1, TRANSPARENT)};
        } else {
            stops = new Stop[] {new Stop(0, BLUE), new Stop(0.5, BLUE_FADE), new Stop(1, TRANSPARENT)};
        }
        return stops;
    }
    
    private Color getMarkerColor(final int weight) {
        if (weight >= ORANGERED_THRESHOLD) {
            return RED_MARKER;
        } else if (weight >= YELLOW_THRESHOLD) {
            return ORANGE_MARKER;
        } else if (weight >= GREEN_THRESHOLD) {
            return YELLOW_MARKER;
        } else if (weight >= FADEDGREEN_THRESHOLD) {
            return GREEN_MARKER;
        } else {
            return BLUE_MARKER;
        }        
    }
    
    private String idOfNearbyShape(final PointMarker p) {
        Circle testShape = new Circle(12/Math.pow(MapView.getMapScale(), 0.85));
        testShape.setCenterX(p.getX());
        testShape.setCenterY(p.getY());
        for (String idKey : shapeCache.keySet()) {
            Shape cachedShape = shapeCache.get(idKey);
            Shape interShape = Shape.intersect(cachedShape, testShape);
            if(interShape.getLayoutBounds().getHeight()>0 && interShape.getLayoutBounds().getWidth()>0) {
                return idKey;
            }            
        }        
        return null;
    }
    
    @Override
    public Group getLayer() {
        return layerGroup;
    }

    public int getWeight(final AbstractMarker marker) {
        return 0;
    }

    public void resetLayer() {
        layerGroup.getChildren().forEach(layerNode -> layerGroup.getChildren().remove(layerNode));
        shapeCache.clear();
        weightCache.clear();
        setUp();
    }
}

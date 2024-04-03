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
package au.gov.asd.tac.constellation.views.mapview.markers;

import au.gov.asd.tac.constellation.utilities.datastructure.ObjectCache;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature.ConstellationFeatureType;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationMultiFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationShapeFeature;
import au.gov.asd.tac.constellation.views.mapview.utilities.FeatureKey;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A factory for creating markers for use in the Map View.
 *
 * @author cygnus_x-1
 */
public class ConstellationMarkerFactory {

    private final ObjectCache<FeatureKey, ConstellationAbstractMarker> featureCache;
    private final HashMap<ConstellationFeatureType, Class<? extends ConstellationAbstractMarker>> featureTypeMap;

    private int defaultColor = MarkerUtilities.DEFAULT_COLOR;
    private int defaultCustomColor = MarkerUtilities.DEFAULT_CUSTOM_COLOR;
    private int defaultHighlightColor = MarkerUtilities.DEFAULT_HIGHLIGHT_COLOR;
    private int defaultSelectColor = MarkerUtilities.DEFAULT_SELECT_COLOR;
    private int defaultStrokeColor = MarkerUtilities.DEFAULT_STROKE_COLOR;
    private int defaultStrokeWeight = MarkerUtilities.DEFAULT_STROKE_WEIGHT;

    public ConstellationMarkerFactory() {
        super();
        this.featureCache = new ObjectCache<>();
        this.featureTypeMap = new HashMap<>();
        featureTypeMap.put(ConstellationFeatureType.POINT, ConstellationPointMarker.class);
        featureTypeMap.put(ConstellationFeatureType.LINE, ConstellationLineMarker.class);
        featureTypeMap.put(ConstellationFeatureType.POLYGON, ConstellationPolygonMarker.class);
        featureTypeMap.put(ConstellationFeatureType.MULTI, ConstellationMultiMarker.class);
        featureTypeMap.put(ConstellationFeatureType.CLUSTER, ConstellationClusterMarker.class);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(final int color) {
        this.defaultColor = color;
    }

    public int getDefaultCustomColor() {
        return defaultCustomColor;
    }

    public void setDefaultCustomColor(final int color) {
        this.defaultCustomColor = color;
    }

    public int getDefaultHighlightColor() {
        return defaultHighlightColor;
    }

    public void setDefaultHighlightColor(final int color) {
        this.defaultHighlightColor = color;
    }

    public int getDefaultSelectColor() {
        return defaultSelectColor;
    }

    public void setDefaultSelectColor(final int color) {
        this.defaultSelectColor = color;
    }

    public int getDefaultStrokeColor() {
        return defaultStrokeColor;
    }

    public void setDefaultStrokeColor(final int color) {
        this.defaultStrokeColor = color;
    }

    public int getDefaultStrokeWeight() {
        return defaultStrokeWeight;
    }

    public void setDefaultStrokeWeight(final int weight) {
        this.defaultStrokeWeight = weight;
    }

    public void setPointClass(final Class<? extends ConstellationAbstractMarker> pointMarkerClass) {
        featureTypeMap.remove(ConstellationFeatureType.POINT);
        featureTypeMap.put(ConstellationFeatureType.POINT, pointMarkerClass);
    }

    public void setLineClass(final Class<? extends ConstellationAbstractMarker> lineMarkerClass) {
        featureTypeMap.remove(ConstellationFeatureType.LINE);
        featureTypeMap.put(ConstellationFeatureType.LINE, lineMarkerClass);
    }

    public void setPolygonClass(final Class<? extends ConstellationAbstractMarker> polygonMarkerClass) {
        featureTypeMap.remove(ConstellationFeatureType.POLYGON);
        featureTypeMap.put(ConstellationFeatureType.POLYGON, polygonMarkerClass);
    }

    public void setMultiClass(final Class<? extends ConstellationAbstractMarker> multiMarkerClass) {
        featureTypeMap.remove(ConstellationFeatureType.MULTI);
        featureTypeMap.put(ConstellationFeatureType.MULTI, multiMarkerClass);
    }

    public void setClusterClass(final Class<? extends ConstellationAbstractMarker> multiMarkerClass) {
        featureTypeMap.remove(ConstellationFeatureType.CLUSTER);
        featureTypeMap.put(ConstellationFeatureType.CLUSTER, multiMarkerClass);
    }

    public List<ConstellationAbstractMarker> createMarkers(final Iterable<ConstellationAbstractFeature> features) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final List<ConstellationAbstractMarker> markers = new ArrayList<>();
        for (final ConstellationAbstractFeature feature : features) {
            markers.add(createMarker(feature));
        }
        return markers;
    }

    public ConstellationAbstractMarker createMarker(final ConstellationAbstractFeature feature) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final ConstellationAbstractMarker marker;
        final FeatureKey key = new FeatureKey(feature);
        if (featureCache.contains(key)) {
            marker = featureCache.getRandom(key);
        } else {
            switch (feature.getType()) {
                case POINT -> marker = createPointMarker((ConstellationPointFeature) feature);
                case LINE -> marker = createLineMarker((ConstellationShapeFeature) feature);
                case POLYGON -> marker = createPolygonMarker((ConstellationShapeFeature) feature);
                case MULTI -> marker = createMultiMarker((ConstellationMultiFeature) feature);
                case CLUSTER -> marker = createClusterMarker((ConstellationMultiFeature) feature);
                default -> {
                    return null;
                }
            }
            featureCache.add(key, marker);
        }
        return marker;
    }

    protected ConstellationAbstractMarker createPointMarker(final ConstellationPointFeature feature) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Class<? extends ConstellationAbstractMarker> markerClass = featureTypeMap.get(feature.getType());
        ConstellationAbstractMarker marker;
        try {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(Location.class, HashMap.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocation(), feature.getProperties());
        } catch (final NoSuchMethodException ex) {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(Location.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocation());
            marker.setProperties(feature.getProperties());
        }
        marker.setColor(defaultColor);
        marker.setCustomColor(defaultCustomColor);
        marker.setHighlightColor(defaultHighlightColor);
        marker.setSelectColor(defaultSelectColor);
        marker.setStrokeColor(defaultStrokeColor);
        marker.setStrokeWeight(defaultStrokeWeight);
        return marker;
    }

    protected ConstellationAbstractMarker createLineMarker(final ConstellationShapeFeature feature) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Class<? extends ConstellationAbstractMarker> markerClass = featureTypeMap.get(feature.getType());
        ConstellationAbstractMarker marker;
        try {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(List.class, HashMap.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocations(), feature.getProperties());
        } catch (final NoSuchMethodException ex) {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(List.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocations());
            marker.setProperties(feature.getProperties());
        }
        marker.setColor(defaultColor);
        marker.setCustomColor(defaultCustomColor);
        marker.setHighlightColor(defaultHighlightColor);
        marker.setSelectColor(defaultSelectColor);
        marker.setStrokeColor(defaultStrokeColor);
        marker.setStrokeWeight(defaultStrokeWeight);
        return marker;
    }

    protected ConstellationAbstractMarker createPolygonMarker(final ConstellationShapeFeature feature) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Class<? extends ConstellationAbstractMarker> markerClass = featureTypeMap.get(feature.getType());
        ConstellationAbstractMarker marker;
        try {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(List.class, HashMap.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocations(), feature.getProperties());
        } catch (NoSuchMethodException ex) {
            final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor(List.class);
            marker = (ConstellationAbstractMarker) markerConstructor.newInstance(feature.getLocations());
            marker.setProperties(feature.getProperties());
        }
        marker.setColor(defaultColor);
        marker.setCustomColor(defaultCustomColor);
        marker.setHighlightColor(defaultHighlightColor);
        marker.setSelectColor(defaultSelectColor);
        marker.setStrokeColor(defaultStrokeColor);
        marker.setStrokeWeight(defaultStrokeWeight);
        return marker;
    }

    protected ConstellationAbstractMarker createMultiMarker(final ConstellationMultiFeature feature) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Class<? extends ConstellationAbstractMarker> markerClass = featureTypeMap.get(feature.getType());
        final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor();
        final ConstellationMultiMarker multiMarker = (ConstellationMultiMarker) markerConstructor.newInstance();
        multiMarker.addMarkers(createMarkers(feature.getFeatures()));
        return multiMarker;
    }

    protected ConstellationAbstractMarker createClusterMarker(final ConstellationMultiFeature feature) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        final Class<? extends ConstellationAbstractMarker> markerClass = featureTypeMap.get(feature.getType());
        final Constructor<? extends ConstellationAbstractMarker> markerConstructor = markerClass.getDeclaredConstructor();
        final ConstellationClusterMarker clusterMarker = (ConstellationClusterMarker) markerConstructor.newInstance();
        clusterMarker.addMarkers(createMarkers(feature.getFeatures()));
        return clusterMarker;
    }
}

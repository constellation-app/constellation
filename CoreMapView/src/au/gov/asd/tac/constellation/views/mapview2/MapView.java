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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.Graph;

import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.CircleMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PolygonMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.asd.tac.constellation.views.mapview2.overlays.AbstractOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.InfoOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.ToolsOverlay;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

/**
 *
 * @author altair1673
 */
public class MapView extends ScrollPane {

    private MapViewPane parent;

    private final StackPane mapStackPane;
    private final List<AbstractMarker> userMarkers = new ArrayList<AbstractMarker>();

    private int drawnMarkerId = 0;
    private boolean drawingCircleMarker = false;
    private boolean drawingPolygonMarker = false;

    public static final double minLong = -169.1110266;
    public static final double maxLong = 190.48712;

    public static final double minLat = -58.488473;
    public static final double maxLat = 83.63001;

    public static final double mapWidth = 1010.33;
    public static final double mapHeight = 1224;

    private Set<AbstractMarker.MarkerType> markersShowing = new HashSet<>();
    private Map<String, AbstractOverlay> overlayMap = new HashMap<>();

    private Canvas mapCanvas;
    private Group countryGroup;
    private Group graphMarkerGroup;
    private Group drawnMarkerGroup;
    private Group polygonMarkerGroup;
    private Group clusterMarkerGroup;
    private Group pointMarkerGroup;

    private static final Logger LOGGER = Logger.getLogger("Test");

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

    private final PointMarker testMarker;
    private final PointMarker testMarker2;

    private final double mapScaleFactor = 1.1;

    private double scale = 1.0;
    //private final SVGPath p = new SVGPath();
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    private CircleMarker circleMarker = null;
    private PolygonMarker polygonMarker = null;

    private ArrayList<ArrayList<Node>> pointMarkerClusters = new ArrayList<ArrayList<Node>>();
    private Set<Node> clusteredPointMarkers = new HashSet<>();

    private boolean drawingMeasureLine = false;
    private Line measureLine = null;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    private final Pane mapGroupHolder = new Pane();


    private final List<AbstractMapLayer> layers = new ArrayList<>();

    private ToolsOverlay toolsOverlay = null;
    private InfoOverlay infoOverlay = null;

    private final Group overlayGroup;
    private final Group layerGroup;

    //private final Region testRegion = new Region();

    public MapView(MapViewPane parent) {
        this.parent = parent;
        LOGGER.log(Level.SEVERE, "In MapView constructor");
        testMarker = new PointMarker(parent.getParentComponent(), -99, -99, 32.764233, 129.872696, 0.05, 95, 244);
        testMarker.setMarkerPosition(mapWidth, mapHeight);

        testMarker2 = new PointMarker(parent.getParentComponent(), -100, -100, 35.011665, 135.768326, 0.05, 95, 244);
        testMarker2.setMarkerPosition(mapWidth, mapHeight);
        //testRegion.setShape(testMarker.getMarker());
        //markersShowing.setValue(new SetChangeListener<AbstractMarker.MarkerType>());

        //testRegion.setTranslateX(200);
        //testRegion.setTranslateY(200);

        countryGroup = new Group();
        graphMarkerGroup = new Group();
        drawnMarkerGroup = new Group();
        polygonMarkerGroup = new Group();
        overlayGroup = new Group();
        layerGroup = new Group();
        clusterMarkerGroup = new Group();
        pointMarkerGroup = new Group();

        pointMarkerGroup.getChildren().addAll(testMarker.getMarker(), testMarker2.getMarker());


        markersShowing.add(AbstractMarker.MarkerType.LINE_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POINT_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POLYGON_MARKER);


        mapCanvas = new Canvas();

        mapGroupHolder.setBackground(Background.fill(new Color(0.722, 0.871, 0.902, 1)));

        countrySVGPaths.clear();
        parseMapSVG();
        LOGGER.log(Level.SEVERE, "Size of country array: " + countrySVGPaths.size());


        mapStackPane = new StackPane();

        mapStackPane.setBorder(Border.EMPTY);


        mapStackPane.getChildren().addAll(mapGroupHolder);
        mapStackPane.setBackground(Background.fill(Color.WHITE));


        mapStackPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                e.consume();

                if (e.getDeltaY() == 0) {
                    return;
                }

                double scaleFactor = (e.getDeltaY() > 0) ? mapScaleFactor : 1 / mapScaleFactor;

                double oldXScale = mapCanvas.getScaleY();
                double oldYScale = mapCanvas.getScaleX();

                double newXScale = oldXScale * scaleFactor;
                double newYScale = oldYScale * scaleFactor;

                double xAdjust = (newXScale / oldXScale) - 1;
                double yAdjust = (newYScale / oldYScale) - 1;

                double moveX = e.getSceneX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
                double moveY = e.getSceneY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

                mapCanvas.setTranslateX(mapCanvas.getTranslateX() - xAdjust * moveX);
                mapCanvas.setTranslateY(mapCanvas.getTranslateY() - yAdjust * moveY);
                mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
                mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);

                mapCanvas.setScaleX(newXScale);
                mapCanvas.setScaleY(newYScale);
                mapStackPane.setScaleX(newXScale);
                mapStackPane.setScaleY(newYScale);

                //testRegion.setScaleX(scaleFactor);
                //testRegion.setScaleY(scaleFactor);

                //graphMarkerGroup.setScaleX(scaleFactor);
                //graphMarkerGroup.setScaleY(scaleFactor);
                calculateClusters();
                clusterMarkerGroup.getChildren().clear();

                pointMarkerClusters.forEach(cluster -> drawClusterMarkers(cluster));
                //testMarker.getMarker().setScaleY(scaleFactor);
            }
        });

        mapStackPane.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            transalateX = node.getTranslateX();
            transalateY = node.getTranslateY();

        });

        mapStackPane.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {


                double scaleX = mapStackPane.getScaleX();
                double scaleY = mapStackPane.getScaleY();

                double canvasScaleX = mapCanvas.getScaleX();
                double canvasScaleY = mapCanvas.getScaleY();

                Node node = (Node) event.getSource();

                node.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX)));
                node.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY)));

                //mapCanvas.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX) / canvasScaleX));
                //mapCanvas.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY) / canvasScaleY));

                event.consume();
            }

        });


        countryGroup.getChildren().clear();
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }

        this.setPannable(true);

        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        setContent(mapStackPane);


        this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);

        mapGroupHolder.setPrefWidth(1010.33);

        mapGroupHolder.setPrefHeight(1224);

        mapGroupHolder.getChildren().add(countryGroup);

        toolsOverlay = new ToolsOverlay(815, 20);
        infoOverlay = new InfoOverlay(20, 20);

        overlayMap.put(MapViewPane.TOOLS_OVERLAY, toolsOverlay);
        overlayMap.put(MapViewPane.INFO_OVERLAY, infoOverlay);

        mapGroupHolder.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                double x = event.getX();
                double y = event.getY();

                if (toolsOverlay.getDrawingEnabled().get() && !toolsOverlay.getMeasureEnabled().get()) {
                    if (event.isShiftDown()) {
                        drawingCircleMarker = true;
                        circleMarker = new CircleMarker(parent.getParentComponent(), drawnMarkerId++, x, y, 0, 100, 100);
                        polygonMarkerGroup.getChildren().add(circleMarker.getUICircle());
                        polygonMarkerGroup.getChildren().add(circleMarker.getUILine());

                    } else if (drawingCircleMarker) {
                        circleMarker.generateCircle();
                        drawnMarkerGroup.getChildren().add(circleMarker.getMarker());
                        userMarkers.add(circleMarker);
                        circleMarker = null;
                        polygonMarkerGroup.getChildren().clear();
                        drawingCircleMarker = false;
                        toolsOverlay.resetMeasureText();
                    } else if (event.isControlDown()) {
                        if (!drawingPolygonMarker && !drawingCircleMarker) {
                            polygonMarker = new PolygonMarker(parent.getParentComponent(), drawnMarkerId++, 0, 0);

                            polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));
                            drawingPolygonMarker = true;
                        } else {
                            polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));
                        }
                        toolsOverlay.resetMeasureText();
                    } else if (drawingPolygonMarker) {
                        drawingPolygonMarker = false;
                        polygonMarker.generatePath();
                        addUserDrawnMarker(polygonMarker);
                        toolsOverlay.resetMeasureText();
                        userMarkers.add(polygonMarker);
                        polygonMarker.endDrawing();
                        polygonMarkerGroup.getChildren().clear();
                    } else if (!drawingPolygonMarker && !drawingCircleMarker) {
                        UserPointMarker marker = new UserPointMarker(parent.getParentComponent(), drawnMarkerId++, x, y, 0.05, 95, -95);
                        marker.setMarkerPosition(0, 0);
                        //drawnMarkerGroup.getChildren().addAll(marker.getMarker());
                        addUserDrawnMarker(marker);
                        //pointMarkerGroup.getChildren().addAll(marker.getMarker());
                        userMarkers.add(marker);
                    }
                } else if (!toolsOverlay.getDrawingEnabled().get() && toolsOverlay.getMeasureEnabled().get()) {

                    //if (event.isPrimaryButtonDown()) {
                        if (!drawingMeasureLine) {
                            LOGGER.log(Level.SEVERE, "Drawing measure line");
                            measureLine = new Line();
                            measureLine.setStroke(Color.RED);
                            //measureLine.setFill(Color.RED);
                            measureLine.setStartX(event.getX());
                            measureLine.setStartY(event.getY());
                            measureLine.setEndX(event.getX());
                            measureLine.setEndY(event.getY());
                            measureLine.setStrokeWidth(1);
                            polygonMarkerGroup.getChildren().add(measureLine);
                            drawingMeasureLine = true;
                        } else {
                            LOGGER.log(Level.SEVERE, "Erasing measure line");
                            polygonMarkerGroup.getChildren().clear();
                            toolsOverlay.resetMeasureText();
                            drawingMeasureLine = false;
                            measureLine = null;
                        }
                    //}
                }
                event.consume();
            }

        });

        mapGroupHolder.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();

                if (infoOverlay != null && infoOverlay.getIsShowing()) {
                    infoOverlay.updateLocation(x, y);
                }

                if (toolsOverlay.getDrawingEnabled().get() && !toolsOverlay.getMeasureEnabled().get()) {
                    if (drawingCircleMarker && circleMarker != null && !drawingPolygonMarker) {

                        circleMarker.setRadius(x, y);
                        circleMarker.setLineEnd(x, y);
                        toolsOverlay.setDistanceText(circleMarker.getCenterX(), circleMarker.getCenterY(), x, y);
                    } else if (drawingPolygonMarker && polygonMarker != null && !drawingCircleMarker) {
                        polygonMarker.setEnd(x, y);
                        toolsOverlay.setDistanceText(polygonMarker.getCurrentLine().getStartX(), polygonMarker.getCurrentLine().getStartY(), polygonMarker.getCurrentLine().getEndX(), polygonMarker.getCurrentLine().getEndY());
                    }
                } else if (toolsOverlay.getMeasureEnabled().get() && !toolsOverlay.getDrawingEnabled().get()) {
                    if (measureLine != null) {
                        measureLine.setEndX(event.getX());
                        measureLine.setEndY(event.getY());

                        toolsOverlay.setDistanceText(measureLine.getStartX(), measureLine.getStartY(), measureLine.getEndX(), measureLine.getEndY());
                    }
                }


                event.consume();
            }

        });

        mapGroupHolder.getChildren().add(graphMarkerGroup);
        mapGroupHolder.getChildren().add(pointMarkerGroup);
        mapGroupHolder.getChildren().add(clusterMarkerGroup);
        mapGroupHolder.getChildren().addAll(drawnMarkerGroup);
        mapGroupHolder.getChildren().addAll(polygonMarkerGroup);
        mapGroupHolder.getChildren().add(overlayGroup);
        mapGroupHolder.getChildren().add(layerGroup);

        overlayGroup.getChildren().addAll(toolsOverlay.getOverlayPane());
        overlayGroup.getChildren().addAll(infoOverlay.getOverlayPane());

        //graphMarkerGroup.getChildren().add(testRegion);
    }

    private void calculateClusters() {
        clusteredPointMarkers.clear();
        pointMarkerClusters.clear();
        for (int i = 0; i < pointMarkerGroup.getChildren().size(); ++i) {

            if (!clusteredPointMarkers.contains(pointMarkerGroup.getChildren().get(i))) {
                clusteredPointMarkers.add(pointMarkerGroup.getChildren().get(i));
                ArrayList<Node> clusterArray = new ArrayList<>();
                clusterArray.add(pointMarkerGroup.getChildren().get(i));
                for (int j = 0; j < pointMarkerGroup.getChildren().size(); ++j) {
                    if (i != j && !clusteredPointMarkers.contains(pointMarkerGroup.getChildren().get(j))) {
                        double distance = getNodeDistance(pointMarkerGroup.getChildren().get(i), pointMarkerGroup.getChildren().get(j));

                        if (distance < 150) {
                            clusteredPointMarkers.add(pointMarkerGroup.getChildren().get(j));
                            clusterArray.add(pointMarkerGroup.getChildren().get(j));
                        }
                    }
                }
                pointMarkerClusters.add(clusterArray);
            }
        }
    }

    private double getNodeDistance(Node n1, Node n2) {
        double x1 = n1.localToScreen(n1.getBoundsInLocal().getCenterX(), n1.getBoundsInLocal().getCenterY()).getX();
        double y1 = n1.localToScreen(n1.getBoundsInLocal().getCenterX(), n1.getBoundsInLocal().getCenterY()).getY();

        double x2 = n2.localToScreen(n2.getBoundsInLocal().getCenterX(), n2.getBoundsInLocal().getCenterY()).getX();
        double y2 = n2.localToScreen(n2.getBoundsInLocal().getCenterX(), n2.getBoundsInLocal().getCenterY()).getY();

        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        //LOGGER.log(Level.SEVERE, "Test X1: " + x1);
        LOGGER.log(Level.SEVERE, "Test Distance: " + distance);
        return distance;
    }

    private void drawClusterMarkers(ArrayList<Node> nodes) {
        if (nodes.isEmpty()) {
            return;
        }

        double MIN_RADIUS = 15;
        Vec3 clusterCenter = new Vec3();
        nodes.forEach(node -> {

            double nodeX = node.getBoundsInParent().getCenterX();
            double nodeY = node.getBoundsInParent().getCenterY();

            clusterCenter.addVector(new Vec3(nodeX, nodeY));
        });
        clusterCenter.divVector(nodes.size());

        double diameter = 0;
        double maxDistance = Double.MIN_VALUE;
        double farthestNode1X = 0;
        double farthestNode1Y = 0;

        double farthestNode2X = 0;
        double farthestNode2Y = 0;
        if (nodes.size() > 0) {
            final Vec3 minPosition = new Vec3(
                    Float.MAX_VALUE, Float.MAX_VALUE);
            final Vec3 maxPosition = new Vec3(
                    Float.MIN_VALUE, Float.MIN_VALUE);
            for (int i = 0; i < nodes.size(); ++i) {
                Node node = nodes.get(i);
                double nodeX = node.getBoundsInParent().getCenterX();
                double nodeY = node.getBoundsInParent().getCenterY();
                for (int j = 0; j < nodes.size(); ++j) {
                    Node node2 = nodes.get(j);
                    if (node != node2) {
                        double node2X = node2.getBoundsInParent().getCenterX();
                        double node2Y = node2.getBoundsInParent().getCenterY();

                        double distance = Math.sqrt(Math.pow(node2X - nodeX, 2) + Math.pow(node2Y - nodeY, 2));

                        if (maxDistance < distance) {
                            maxDistance = distance;
                            farthestNode1X = nodeX;
                            farthestNode1Y = nodeY;
                            farthestNode2X = node2X;
                            farthestNode2Y = node2Y;
                            clusterCenter.x = (nodeX + node2X) / 2;
                            clusterCenter.y = (nodeY + node2Y) / 2;
                        }
                    }
                }

                /*minPosition.x = Math.min(nodeX, minPosition.x);
                minPosition.y = Math.min(nodeY, minPosition.y);
                maxPosition.x = Math.max(nodeX, maxPosition.x);
                maxPosition.y = Math.max(nodeY, maxPosition.y);*/
            }
            //diameter = Math.sqrt(Math.pow((maxPosition.x - minPosition.x), 2)
            //+ Math.pow((maxPosition.y - minPosition.y), 2));

            diameter = Math.sqrt(Math.pow((farthestNode2X - farthestNode1X), 2)
                    + Math.pow((farthestNode2Y - farthestNode1Y), 2));
        }
        double clusterRadius = Math.max((float) diameter / 2, MIN_RADIUS);

        Circle c = new Circle();
        c.setCenterX(clusterCenter.x);
        c.setCenterY(clusterCenter.y);
        c.setRadius(clusterRadius);
        c.setFill(Color.DARKBLUE);
        c.setOpacity(0.6);
        c.setMouseTransparent(true);

        Text numNodes = new Text(clusterCenter.x - 5, clusterCenter.y + 5, "" + nodes.size());
        numNodes.setFill(Color.YELLOW);
        numNodes.setFont(new Font(20));
        numNodes.setMouseTransparent(true);
        //numNodes.setText();

        clusterMarkerGroup.getChildren().add(c);
        clusterMarkerGroup.getChildren().add(numNodes);

    }

    private void addUserDrawnMarker(AbstractMarker marker) {
        if (markersShowing.contains(marker.getType())) {

            if (marker instanceof UserPointMarker) {
                pointMarkerGroup.getChildren().add(marker.getMarker());
                return;
            }

            drawnMarkerGroup.getChildren().addAll(marker.getMarker());
        }
    }

    public void toggleToolsOverlay() {
        LOGGER.log(Level.SEVERE, "toggling tools overlay");
        toolsOverlay.getOverlayPane().setVisible(!toolsOverlay.getOverlayPane().isVisible());
        toolsOverlay.setIsShowing(toolsOverlay.getOverlayPane().isVisible());

    }

    public void toggleOverlay(String overlay, boolean show) {
        if (overlayMap.containsKey(overlay) && overlayMap.get(overlay).getIsShowing() != show) {
            overlayMap.get(overlay).toggleOverlay();
        }
    }

    public void removeUserMarker(int id) {
        for (int i = 0; i < userMarkers.size(); ++i) {
            if (userMarkers.get(i).getMarkerId() == id) {
                AbstractMarker removed = userMarkers.get(i);
                userMarkers.remove(i);
                if (removed instanceof UserPointMarker) {
                    pointMarkerGroup.getChildren().clear();
                    redrawUserMarkers(true);
                } else
                    redrawUserMarkers(false);

                break;
            }
        }

        //redrawUserMarkers();

    }

    private void redrawUserMarkers(boolean pointMarkersOnly) {
        drawnMarkerGroup.getChildren().clear();

        userMarkers.forEach((marker) -> {

            if (markersShowing.contains(marker.getType())) {

                if (marker instanceof UserPointMarker && pointMarkersOnly) {
                    pointMarkerGroup.getChildren().add(marker.getMarker());

                }
                else if (!pointMarkersOnly);
                    drawnMarkerGroup.getChildren().add(marker.getMarker());
            }
        });
    }


    public void addLayer(AbstractMapLayer layer) {
        layer.setUp();
        layers.add(layer);
        layerGroup.getChildren().add(layer.getLayer());
        layer.setIsShowing(true);
    }

    public void removeLayer(int id) {
        layerGroup.getChildren().clear();
        for (int i = 0; i < layers.size(); ++i) {
            if (layers.get(i).getId() == id) {
                layers.remove(i);

            }
        }
        renderLayers();
    }

    public void updateShowingMarkers(AbstractMarker.MarkerType type, boolean adding) {
        if (markersShowing.contains(type) && !adding) {
            markersShowing.remove(type);
        } else if (!markersShowing.contains(type) && adding) {
            markersShowing.add(type);
        }

        redrawUserMarkers(false);
        redrawQueriedMarkers();
    }

    public void redrawQueriedMarkers() {
        graphMarkerGroup.getChildren().clear();
        pointMarkerGroup.getChildren().clear();
        parent.redrawQueriedMarkers();
    }

    private void renderLayers() {
        layerGroup.getChildren().clear();
        layers.forEach(layer -> {
            //layer.setUp();
            layerGroup.getChildren().add(layer.getLayer());
        });
    }


    public Map<String, AbstractMarker> getAllMarkers() {
        return parent.getAllMarkers();
    }

    public Graph getCurrentGraph() {
        return parent.getCurrentGraph();
    }


    /*private double longToX(double longitude, double minLong, double mapWidth, double lonDelta) {
        return (longitude - minLong) * (mapWidth / lonDelta);
    }

    private double latToY(double lattitude, double mapWidth, double mapHeight) {
        lattitude = lattitude * (Math.PI / 180);
        double y = Math.log(Math.tan((Math.PI / 4) + (lattitude / 2)));
        y = (mapHeight / 2) - (mapWidth * y / (2 * Math.PI));

        return y;
    }*/

    public void drawMarker(AbstractMarker marker) {

        if (markersShowing.contains(marker.getType())) {
            marker.setMarkerPosition(mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());

            if (marker instanceof PointMarker) {
                pointMarkerGroup.getChildren().add(marker.getMarker());
                return;
            }
            graphMarkerGroup.getChildren().add(marker.getMarker());
        }

    }

    private void parseMapSVG() {
        countryGroup.getChildren().clear();
        LOGGER.log(Level.SEVERE, "in parse svg map");

        try {
            try (BufferedReader bFileReader = new BufferedReader(new FileReader("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapView\\resources\\MercratorMapView4.txt"))) {
                String path = "";
                String line = "";

                while ((line = bFileReader.readLine()) != null) {
                    line = line.strip();


                    if (line.startsWith("<path")) {
                        int startIndex = line.indexOf("d=");

                        int endIndex = line.indexOf(">");

                        path = line.substring(startIndex + 3, endIndex - 2);

                        SVGPath svgPath = new SVGPath();
                        svgPath.setFill(Color.WHITE);
                        svgPath.setStrokeWidth(5);
                        svgPath.setContent(path);

                        countrySVGPaths.add(svgPath);

                        path = "";
                    }

                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception thrown");
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }



}

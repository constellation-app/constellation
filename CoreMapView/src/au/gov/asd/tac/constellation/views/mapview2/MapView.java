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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.CircleMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.shape.SVGPath;
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
    private final Image mapImage;
    private final ImageView mapDisplay;
    private final List<String> countryDrawings = new ArrayList<String>();
    private final List<AbstractMarker> userMarkers = new ArrayList<AbstractMarker>();

    private int drawnMarkerId = 0;
    private boolean drawingCircleMarker = false;

    public static final double minLong = -169.1110266;
    public static final double maxLong = 190.48712;

    public static final double minLat = -58.488473;
    public static final double maxLat = 83.63001;


    private Canvas mapCanvas;
    private Group countryGroup;
    private Group drawnMarkerGroup;
    private Group polygonMarkerGroup;
    private GraphicsContext gc;
    //private WebEngine webEngine;
    //private WebView webView;
    private static final Logger LOGGER = Logger.getLogger("Test");

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

    private final double mapScaleFactor = 1.1;

    private double scale = 1.0;
    //private final SVGPath p = new SVGPath();
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    private CircleMarker circleMarker = null;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    private final Pane mapGroupHolder = new Pane();

    private double canvasTransalateX;
    private double canvasTransalateY;

    public MapView(MapViewPane parent) {
        this.parent = parent;
        LOGGER.log(Level.SEVERE, "In MapView constructor");

        mapImage = new Image("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapview\\resources\\world-map.jpg");
        countryGroup = new Group();
        drawnMarkerGroup = new Group();
        polygonMarkerGroup = new Group();

        mapCanvas = new Canvas();
        gc = mapCanvas.getGraphicsContext2D();

        mapGroupHolder.setBackground(Background.fill(new Color(0.722, 0.871, 0.902, 1)));

        countrySVGPaths.clear();
        //setUp();
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
            }
        });

        mapStackPane.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            transalateX = node.getTranslateX();
            transalateY = node.getTranslateY();

            canvasTransalateX = mapCanvas.getTranslateX();
            canvasTransalateY = mapCanvas.getTranslateY();
        });

        mapStackPane.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                return;
            }

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

        });


        mapDisplay = new ImageView(mapImage);
        //mapStackPane.getChildren().add(mapCanvas);
        countryGroup.getChildren().clear();
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }

        this.setPannable(true);
        //this.setPrefSize(1009.6727, 665.96301);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //countryGroup.getChildren().add(p);

        setContent(mapStackPane);

        //mapCanvas.scaleXProperty().bind(zoomProperty);
        //mapCanvas.scaleYProperty().bind(zoomProperty);

        this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);
        //drawMarker();
        //draw();
        mapGroupHolder.setPrefWidth(1010.33);
        //mapGroupHolder.setPrefHeight(967.25);
        mapGroupHolder.setPrefHeight(1224);

        mapGroupHolder.getChildren().add(countryGroup);

        /*DayNightLayer dayNightLayer = new DayNightLayer();
        dayNightLayer.setUp();

        mapGroupHolder.getChildren().addAll(dayNightLayer.getLayer());*/
        mapGroupHolder.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                double x = event.getX();
                double y = event.getY();
                
                LOGGER.log(Level.SEVERE, "X circle: " + x + " Y circle: " + y);

                if (event.isShiftDown()) {
                    drawingCircleMarker = true;
                    circleMarker = new CircleMarker(parent.getParentComponent(), drawnMarkerId++, x, y, 0, 100, 100);
                    polygonMarkerGroup.getChildren().add(circleMarker.getUICircle());
                    polygonMarkerGroup.getChildren().add(circleMarker.getUILine());

                } else if (drawingCircleMarker) {
                    circleMarker.generateCircle();
                    drawnMarkerGroup.getChildren().add(circleMarker.getMarker());
                    circleMarker = null;
                    polygonMarkerGroup.getChildren().clear();
                    drawingCircleMarker = false;
                } else {
                    UserPointMarker marker = new UserPointMarker(parent.getParentComponent(), drawnMarkerId++, x, y, 0.05, 95, -95);
                    marker.setMarkerPosition(0, 0);
                    drawnMarkerGroup.getChildren().addAll(marker.getMarker());
                    userMarkers.add(marker);
                }
                event.consume();
            }

        });

        mapGroupHolder.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (drawingCircleMarker && circleMarker != null) {
                    double x = event.getX();
                    double y = event.getY();

                    double distance = Math.sqrt(Math.pow(x - circleMarker.getCenterX(), 2) + Math.pow(y - circleMarker.getCenterY(), 2));

                    circleMarker.setRadius(distance);
                    circleMarker.setLineEnd(x, y);
                }

                event.consume();
            }

        });

        mapGroupHolder.getChildren().addAll(drawnMarkerGroup);
        mapGroupHolder.getChildren().addAll(polygonMarkerGroup);


    }

    public void removeUserMarker(int id) {
        for (int i = 0; i < userMarkers.size(); ++i) {
            if (userMarkers.get(i).getMarkerId() == id) {
                userMarkers.remove(i);
                break;
            }
        }

        redrawUserMarkers();
    }

    private void redrawUserMarkers() {
        drawnMarkerGroup.getChildren().clear();

        userMarkers.forEach((marker) -> {
            drawnMarkerGroup.getChildren().add(marker.getMarker());
        });
    }

    public void toggleHeatmapLayer() {
        /*PopularityHeatmapLayer heatmapLayer = new PopularityHeatmapLayer(this);
        heatmapLayer.setUp();
        mapGroupHolder.getChildren().addAll(heatmapLayer.getLayer());*/
    }

    public void toggleLayer(AbstractMapLayer layer, boolean toggle) {
        if (toggle) {
            layer.setUp();
            mapGroupHolder.getChildren().add(layer.getLayer());
        }
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return parent.getAllMarkers();
    }

    public Graph getCurrentGraph() {
        return parent.getCurrentGraph();
    }

    public void drawMarker(double lattitude, double longitude, double xyScale) {


        double lonDelta = maxLong - minLong;

        //double xScale = mapGroupHolder.getPrefWidth() / (maxLong - minLong);
        //double yScale = mapGroupHolder.getPrefHeight() / (maxLat - minLat);

        //double x = (longitude - minLong) * xScale;

        //double minLatRad = minLat * (Math.PI / 180);

        //double worldMapWidth = ((mapGroupHolder.getPrefWidth() / lonDelta) * 360) / (2 * Math.PI);
        //double yOffset = (worldMapWidth / 2 * Math.log((1 + Math.sin(minLatRad)) / (1 - Math.sin(minLatRad))));
        //double y = mapGroupHolder.getPrefHeight() - ((worldMapWidth * Math.log((1 + Math.sin(lattitudeRad)) / (1 - Math.sin(lattitudeRad)))) - yOffset);
        double x = (longitude - minLong) * (mapGroupHolder.getPrefWidth() / lonDelta);
        double lattitudeRad = lattitude * (Math.PI / 180);

        double y = Math.log(Math.tan((Math.PI / 4) + (lattitudeRad / 2)));
        y = (mapGroupHolder.getPrefHeight() / 2) - (mapGroupHolder.getPrefWidth() * y / (2 * Math.PI));


        x += 95;
        y -= 244;

        LOGGER.log(Level.SEVERE, "x: " + x + " y: " + y);

        //x = 600;
        //y = 600;

        String markerPath = "c-20.89-55.27-83.59-81.74-137-57.59-53.88,24.61-75.7,87.77-47.83,140.71,12.54,23.69,26.47,46.44,39.93,70.12,15.79,27.4,32,55.27,50.16,87.31a101.37,101.37,0,0,1,4.65-9.76c27.86-49.23,56.66-98,84-147.68,14.86-26,16.72-54.8,6-83.12z";
        markerPath = "M " + x + "," + y + " Z " + markerPath;


        //LOGGER.log(Level.SEVERE, markerPath);

        SVGPath marker = new SVGPath();
        marker.setContent(markerPath);
        gc.appendSVGPath(markerPath);
        //marker.translateXProperty().add(4000);
        //marker.translateYProperty().add(5000);
        marker.setStroke(Color.BLACK);
        marker.setFill(Color.RED);
        marker.setScaleX(xyScale);
        marker.setScaleY(xyScale);
        //marker.rotateProperty().set(90.0);

        marker.setOnMouseEntered((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                marker.setFill(Color.ORANGE);
                e.consume();
            }
        }));

        marker.setOnMouseExited((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                marker.setFill(Color.RED);
                e.consume();
            }
        }));

        gc.rect(x, y, 50, 50);

        countryGroup.getChildren().add(marker);
        //drawMarker();
        //draw();
    }

    private double longToX(double longitude, double minLong, double mapWidth, double lonDelta) {
        return (longitude - minLong) * (mapWidth / lonDelta);
    }

    private double latToY(double lattitude, double mapWidth, double mapHeight) {
        lattitude = lattitude * (Math.PI / 180);
        double y = Math.log(Math.tan((Math.PI / 4) + (lattitude / 2)));
        y = (mapHeight / 2) - (mapWidth * y / (2 * Math.PI));

        return y;
    }

    public void drawLine(double lon1, double lat1, double lon2, double lat2) {

        double perthX = longToX(115.857048, minLong, mapGroupHolder.getPrefWidth(), maxLong - minLong);
        double perthY = latToY(-31.953512, mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());

        double sydX = longToX(151.208755, minLong, mapGroupHolder.getPrefWidth(), maxLong - minLong);
        double sydY = latToY(-33.870453, mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());

        perthX += 0;
        perthY -= 149;

        sydY -= 149;

        LOGGER.log(Level.SEVERE, "Y coord is: " + perthY);

        String line = "M " + perthX + ", " + perthY + " Z L " + sydX + "," + sydY + " z";
        SVGPath linePath = new SVGPath();
        linePath.setStroke(Color.BLACK);
        linePath.setStrokeWidth(1);
        linePath.setContent(line);
        countryGroup.getChildren().add(linePath);
    }

    public void drawMarker(AbstractMarker marker) {
        marker.setMarkerPosition(mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());

        countryGroup.getChildren().add(marker.getMarker());

    }

    private void parseMapSVG() {
        countryGroup.getChildren().clear();
        LOGGER.log(Level.SEVERE, "in parse svg map");

        try {
            try (BufferedReader bFileReader = new BufferedReader(new FileReader("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapView\\resources\\MercratorMapView4.txt"))) {
                String path = "";
                String line = "";

                int l = 0;
                //LOGGER.log(Level.SEVERE, "Line: " + ++l);
                while ((line = bFileReader.readLine()) != null) {
                    line = line.strip();
                    //LOGGER.log(Level.SEVERE, "Line:" + line);
                    //for (int i = 0; i < line.length(); ++i) {

                    if (line.startsWith("<path")) {
                        int startIndex = line.indexOf("d=");

                        int endIndex = line.indexOf(">");

                        path = line.substring(startIndex + 3, endIndex - 2);

                        SVGPath svgPath = new SVGPath();
                        svgPath.setFill(Color.WHITE);
                        svgPath.setStrokeWidth(5);
                        svgPath.setContent(path);

                        //LOGGER.log(Level.SEVERE, "Path: " + path);

                        countrySVGPaths.add(svgPath);

                        path = "";
                    }

                    //}
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception thrown");
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }



}

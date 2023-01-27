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
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static au.gov.asd.tac.constellation.views.mapview2.MapViewPane.GEO_JSON;
import static au.gov.asd.tac.constellation.views.mapview2.MapViewPane.GEO_PACKAGE;
import static au.gov.asd.tac.constellation.views.mapview2.MapViewPane.KML;
import static au.gov.asd.tac.constellation.views.mapview2.MapViewPane.SHAPEFILE;

import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.CircleMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarkerBuilder;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PolygonMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.asd.tac.constellation.views.mapview2.overlays.AbstractOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.InfoOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.ToolsOverlay;
import au.gov.asd.tac.constellation.views.mapview2.plugins.SelectOnGraphPlugin;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
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


    private int drawnMarkerId = 0;
    private boolean drawingCircleMarker = false;
    private boolean drawingPolygonMarker = false;

    public static final double minLong = -169.1110266;
    public static final double maxLong = 190.48712;

    public static final double minLat = -58.488473;
    public static final double maxLat = 83.63001;

    public static final double mapWidth = 1010.33;
    public static final double mapHeight = 1224;

    private Map<String, AbstractMarker> markers = new HashMap<>();
    private final List<AbstractMarker> userMarkers = new ArrayList<AbstractMarker>();

    private Set<AbstractMarker.MarkerType> markersShowing = new HashSet<>();
    private Map<String, AbstractOverlay> overlayMap = new HashMap<>();

    private final List<Integer> selectedNodeList = new ArrayList<>();

    //private Canvas mapCanvas;
    private Group countryGroup;
    private Group graphMarkerGroup;
    private Group drawnMarkerGroup;
    private Group polygonMarkerGroup;
    private Group clusterMarkerGroup;
    //public Group pointMarkerGroup;
    public Group hiddenPointMarkerGroup;
    private final Group overlayGroup;
    private final Group layerGroup;
    private final Group pointMarkerTextGroup;
    private final Group thessianMarkersGroup;
    private final Group selectionRectangleGroup;
    private final Group viewPortRectangleGroup;
    private final Group zoomLocationGroup;

    private boolean showingZoomToLocationPane = false;

    private static final Logger LOGGER = Logger.getLogger("MapView");

    Vec3 medianPositionOfMarkers = null;
    private boolean calculatedMedianMarkerPosition = false;

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

    /*private final UserPointMarker testMarker;
    private final UserPointMarker testMarker2;
    private final UserPointMarker testMarker3;
    private final UserPointMarker testMarker4;*/

    private final double mapScaleFactor = 1.1;

    private double scale = 1.0;
    //private final SVGPath p = new SVGPath();
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    private CircleMarker circleMarker = null;
    private PolygonMarker polygonMarker = null;
    private Rectangle selectionRectangle = null;
    private boolean isSelectingMultiple = false;
    private double selectionRectangleX = 0;
    private double selectionRectangleY = 0;
    private ArrayList<ArrayList<Node>> pointMarkerClusters = new ArrayList<ArrayList<Node>>();
    private Set<Node> clusteredPointMarkers = new HashSet<>();

    private boolean drawingMeasureLine = false;
    private Line measureLine = null;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    private int markerID = 0;

    private final Pane mapGroupHolder = new Pane();

    private Rectangle clipRectangle = new Rectangle();
    private final List<AbstractMapLayer> layers = new ArrayList<>();

    private ToolsOverlay toolsOverlay = null;
    private InfoOverlay infoOverlay = null;


    private MapView self = this;

    public ClusterMarkerBuilder clusterMarkerBuilder = null;

    private StringProperty markerColourProperty = new SimpleStringProperty();
    private StringProperty markerTextProperty = new SimpleStringProperty();

    //private final Region testRegion = new Region();

    public MapView(MapViewPane parent) {
        this.parent = parent;
        LOGGER.log(Level.SEVERE, "In MapView constructor");
        /*testMarker = new UserPointMarker(this, 90, 200, 200, 0.05, 0, 0);
        testMarker.setMarkerPosition(mapWidth, mapHeight);

        testMarker2 = new UserPointMarker(this, 91, 200, 300, 0.05, 0, 0);
        testMarker2.setMarkerPosition(mapWidth, mapHeight);

        testMarker3 = new UserPointMarker(this, 91, 150, 250, 0.05, 0, 0);
        testMarker3.setMarkerPosition(mapWidth, mapHeight);

        testMarker4 = new UserPointMarker(this, 91, 250, 250, 0.05, 0, 0);
        testMarker4.setMarkerPosition(mapWidth, mapHeight);

        userMarkers.add(testMarker);
        userMarkers.add(testMarker2);
        userMarkers.add(testMarker3);
        userMarkers.add(testMarker4);*/
        //testRegion.setShape(testMarker.getMarker());
        //markersShowing.setValue(new SetChangeListener<AbstractMarker.MarkerType>());

        //testRegion.setTranslateX(200);
        //testRegion.setTranslateY(200);
        clusterMarkerBuilder = new ClusterMarkerBuilder(this);

        countryGroup = new Group();
        graphMarkerGroup = new Group();
        drawnMarkerGroup = new Group();
        polygonMarkerGroup = new Group();
        overlayGroup = new Group();
        layerGroup = new Group();
        clusterMarkerGroup = new Group();
        //pointMarkerGroup = new Group();
        //drawnMarkerGroup.getChildren().addAll(testMarker.getMarker(), testMarker2.getMarker(), testMarker3.getMarker(), testMarker4.getMarker());
        hiddenPointMarkerGroup = new Group();
        hiddenPointMarkerGroup.setVisible(false);
        pointMarkerTextGroup = new Group();
        thessianMarkersGroup = new Group();
        selectionRectangleGroup = new Group();
        zoomLocationGroup = new Group();
        viewPortRectangleGroup = new Group();


        markersShowing.add(AbstractMarker.MarkerType.LINE_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POINT_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POLYGON_MARKER);


        //mapCanvas = new Canvas();

        mapGroupHolder.setBackground(Background.fill(new Color(0.722, 0.871, 0.902, 1)));

        countrySVGPaths.clear();
        parseMapSVG();
        LOGGER.log(Level.SEVERE, "Size of country array: " + countrySVGPaths.size());


        mapStackPane = new StackPane();

        mapStackPane.setBorder(Border.EMPTY);
        mapStackPane.setBackground(Background.fill(Color.BROWN));


        mapStackPane.getChildren().addAll(mapGroupHolder);
        mapStackPane.setBackground(Background.fill(Color.WHITE));

        clipRectangle.setWidth(parent.getWidth());
        clipRectangle.setHeight(parent.getHeight());
        clipRectangle.setX(0);
        clipRectangle.setY(0);
        clipRectangle.setFill(Color.TRANSPARENT);
        //parent.setClip(clipRectangle);

        mapStackPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                e.consume();
                int nodesOnScreen = 0;
                if (e.getDeltaY() == 0) {
                    return;
                }
                /*if (e.getDeltaY() > 0) {
                    reScaleQueriedMarkers(0.94);
                } else {
                    reScaleQueriedMarkers(1.06);
                }*/
                double scaleFactor = (e.getDeltaY() > 0) ? mapScaleFactor : 1 / mapScaleFactor;

                double oldXScale = mapStackPane.getScaleY();
                double oldYScale = mapStackPane.getScaleX();

                double newXScale = oldXScale * scaleFactor;
                double newYScale = oldYScale * scaleFactor;

                double xAdjust = (newXScale / oldXScale) - 1;
                double yAdjust = (newYScale / oldYScale) - 1;

                double moveX = e.getSceneX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
                double moveY = e.getSceneY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

                mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
                mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);


                mapStackPane.setScaleX(newXScale);
                mapStackPane.setScaleY(newYScale);

                if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
                    updateClusterMarkers();
                }

                for (AbstractMarker m : markers.values()) {
                    if (self.getParent().getBoundsInLocal().contains(m.getMarker().getBoundsInParent())) {
                        ++nodesOnScreen;
                    }
                }

                LOGGER.log(Level.SEVERE, "Nodes on Screen: " + nodesOnScreen);

            }
        });

        mapStackPane.setOnMousePressed(event -> {
            //calculateMedianMarkerPosition();
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            transalateX = mapStackPane.getTranslateX();
            transalateY = mapStackPane.getTranslateY();

        });

        mapStackPane.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {

                Node node = (Node) event.getSource();

                mapStackPane.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX)));
                mapStackPane.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY)));

                //mapCanvas.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX) / canvasScaleX));
                //mapCanvas.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY) / canvasScaleY));

                event.consume();
            }

        });


        countryGroup.getChildren().clear();
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }

        this.setPannable(false);

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

        markerColourProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                LOGGER.log(Level.SEVERE, "Inside markerCOlourProperty change event handler");
                for (Object value : markers.values()) {
                    AbstractMarker m = (AbstractMarker) value;

                    if (m instanceof PointMarker) {
                        PointMarker p = (PointMarker) m;

                        p.changeMarkerColour(newValue);
                        LOGGER.log(Level.SEVERE, "Called changeMarkerColour function on marker: " + p.getMarkerId());
                    }

                    //drawMarker(m);
                }
            }
        });

        markerTextProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                pointMarkerTextGroup.getChildren().clear();
                for (Object value : markers.values()) {
                    AbstractMarker m = (AbstractMarker) value;

                    if (m instanceof PointMarker) {
                        PointMarker p = (PointMarker) m;

                        /*if (newValue.equals(MapViewPane.NO_LABELS)) {
                            pointMarkerTextGroup.getChildren().clear();
                            break;
                        } else */
                        if (newValue.equals(MapViewPane.USE_LABEL_ATTR)) {

                            setPointMarkerText(p.getLabelAttr(), p);
                        } else if (newValue.equals(MapViewPane.USE_IDENT_ATTR)) {
                            setPointMarkerText(p.getIdentAttr(), p);
                        }
                    }

                    //drawMarker(m);
                }
            }
        });

        mapGroupHolder.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {

                        deselectAllMarkers();
                        selectedNodeList.clear();
                        PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodeList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                        LOGGER.log(Level.SEVERE, "Double clicked");
                        event.consume();
                        return;
                    }
                }

                double x = event.getX();
                double y = event.getY();

                if (toolsOverlay.getDrawingEnabled().get() && !toolsOverlay.getMeasureEnabled().get()) {
                    if (event.isShiftDown()) {
                        drawingCircleMarker = true;
                        circleMarker = new CircleMarker(self, drawnMarkerId++, x, y, 0, 100, 100);
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
                        //updateClusterMarkers();
                    } else if (event.isControlDown()) {
                        if (!drawingPolygonMarker && !drawingCircleMarker) {
                            polygonMarker = new PolygonMarker(self, drawnMarkerId++, 0, 0);

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
                        //updateClusterMarkers();
                    } else if (!drawingPolygonMarker && !drawingCircleMarker) {
                        UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 95, -95);
                        marker.setMarkerPosition(0, 0);
                        //drawnMarkerGroup.getChildren().addAll(marker.getMarker());
                        addUserDrawnMarker(marker);
                        //pointMarkerGroup.getChildren().addAll(marker.getMarker());
                        userMarkers.add(marker);
                        updateClusterMarkers();
                    }
                } else if (!toolsOverlay.getDrawingEnabled().get() && toolsOverlay.getMeasureEnabled().get()) {

                    //if (event.isPrimaryButtonDown()) {
                        if (!drawingMeasureLine) {
                            LOGGER.log(Level.SEVERE, "Drawing measure line");
                            measureLine = new Line();
                            measureLine.setStroke(Color.RED);
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

                /*if (isSelectingMultiple) {
                    //double x = event.getX();
                    //double y = event.getY();

                    double width = x - selectionRectangle.getX();
                    double height = y - selectionRectangle.getY();

                    selectionRectangle.setWidth(20);
                    selectionRectangle.setHeight(20);
                }*/


                event.consume();
            }

        });

        mapGroupHolder.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
                isSelectingMultiple = true;
                selectionRectangle = new Rectangle();
                selectionRectangle.setX(event.getX());
                    selectionRectangle.setY(event.getY());
                    selectionRectangleX = event.getX();
                    selectionRectangleY = event.getY();

                selectionRectangle.setFill(Color.GRAY);
                selectionRectangle.setOpacity(0.2);

                selectionRectangleGroup.getChildren().add(selectionRectangle);
                LOGGER.log(Level.SEVERE, "Mouse Pressed");

                    //selectionRectangle.setWidth(20);
                    //selectionRectangle.setHeight(20);
                }

                ///event.consume();
            }
        });

        mapGroupHolder.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isSelectingMultiple) {
                    double x = event.getX();
                    double y = event.getY();

                    double width = 0;
                    double height = 0;

                    if (x >= selectionRectangleX && y <= selectionRectangleY) {
                        width = x - selectionRectangleX;
                        height = selectionRectangleY - y;
                        selectionRectangle.setY(y);
                    } else if (x < selectionRectangleX && y < selectionRectangleY) {
                        width = selectionRectangleX - x;
                        height = selectionRectangleY - y;
                        selectionRectangle.setX(x);
                        selectionRectangle.setY(y);
                    } else if (x >= selectionRectangleX && y >= selectionRectangleY) {
                        width = x - selectionRectangleX;
                        height = y - selectionRectangleY;

                    } else if (x < selectionRectangleX && y > selectionRectangleY) {
                        width = selectionRectangleX - x;
                        height = y - selectionRectangleY;

                        selectionRectangle.setX(x);
                    }

                    selectionRectangle.setWidth(width);
                    selectionRectangle.setHeight(height);
                }
            }
        });

        mapGroupHolder.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (isSelectingMultiple) {
                    selectedNodeList.clear();
                    LOGGER.log(Level.SEVERE, "Top left: " + selectionRectangle.getX() + ", " + selectionRectangle.getY() + " bottom right: " + (selectionRectangle.getX() + selectionRectangle.getWidth()) + ", " + (selectionRectangle.getY() + selectionRectangle.getHeight()));
                    double pointMarkerXOffset = 95.5;
                    double pointMarkerYOffset = 95.5;
                    List<Integer> idList = new ArrayList<>();
                    for (AbstractMarker m : markers.values()) {
                        if (m instanceof PointMarker) {
                            PointMarker p = (PointMarker) m;

                            /*Rectangle r = new Rectangle();
                            r.setWidth(1);
                            r.setHeight(1);
                            r.setX(p.getX() - 96);
                            r.setY(p.getY() + 96);
                            r.setFill(Color.RED);

                            overlayGroup.getChildren().add(r);*/

                            p.deselect();


                            if (selectionRectangle.contains(p.getX() - pointMarkerXOffset, p.getY() + pointMarkerYOffset)) {
                                //LOGGER.log(Level.SEVERE, "X coor of marker: " + p.getX() + " Y coordinate is: " + p.getY());
                                //LOGGER.log(Level.SEVERE, "X coordinate of selection box: " + selectionRectangle.getX() + " Y coordinate of box is: " + selectionRectangle.getY());
                                p.select();

                                idList.addAll(p.getIdList());
                                selectedNodeList.add(p.getMarkerId());
                            }

                        }
                    }

                    PluginExecution.withPlugin(new SelectOnGraphPlugin(idList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                    isSelectingMultiple = false;
                    selectionRectangleGroup.getChildren().clear();
                    LOGGER.log(Level.SEVERE, "Mouse Released");
                }
                // event.consume();
            }
        });

        // Add this feature in later
        /*mapGroupHolder.setOnDragDetected(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {

            }
        });*/

        mapGroupHolder.getChildren().add(hiddenPointMarkerGroup);
        mapGroupHolder.getChildren().add(graphMarkerGroup);
        //mapGroupHolder.getChildren().add(pointMarkerGroup);
        mapGroupHolder.getChildren().addAll(drawnMarkerGroup);
        mapGroupHolder.getChildren().add(clusterMarkerGroup);
        mapGroupHolder.getChildren().addAll(polygonMarkerGroup);
        mapGroupHolder.getChildren().add(overlayGroup);
        //mapStackPane.getChildren().add(overlayGroup);
        mapGroupHolder.getChildren().add(layerGroup);
        mapGroupHolder.getChildren().add(pointMarkerTextGroup);
        mapGroupHolder.getChildren().add(thessianMarkersGroup);

        mapGroupHolder.getChildren().add(zoomLocationGroup);
        overlayGroup.getChildren().addAll(toolsOverlay.getOverlayPane());
        overlayGroup.getChildren().addAll(infoOverlay.getOverlayPane());
        mapGroupHolder.getChildren().add(selectionRectangleGroup);
        mapGroupHolder.getChildren().add(viewPortRectangleGroup);

        Rectangle r = new Rectangle();
        r.setX(0);
        r.setY(0);

        r.setWidth(mapWidth);
        r.setHeight(mapHeight);

        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.RED);
        //viewPortRectangleGroup.getChildren().add(r);
        mapStackPane.getChildren().add(viewPortRectangleGroup);


        //graphMarkerGroup.getChildren().add(testRegion);
        //calculateMedianMarkerPosition();
    }

    private void setPointMarkerText(String markerText, PointMarker p) {
        Text t = new Text(markerText);
        t.setX(p.getX() - 125);
        t.setY(p.getY() + 103);

        pointMarkerTextGroup.getChildren().add(t);
    }

    public void deselectAllMarkers() {
        for (AbstractMarker value : markers.values()) {
            if (value instanceof PointMarker) {
                PointMarker p = (PointMarker) value;
                p.deselect();
            }
        }
    }

    public StringProperty getMarkerColourProperty() {
        return markerColourProperty;
    }

    private void addUserDrawnMarker(AbstractMarker marker) {
        if (markersShowing.contains(marker.getType())) {

            if (marker instanceof ClusterMarker) {
                clusterMarkerGroup.getChildren().add(marker.getMarker());
                return;
            }

            drawnMarkerGroup.getChildren().addAll(marker.getMarker());
        }
    }

    public void removeUserMarker(int id) {
        for (int i = 0; i < userMarkers.size(); ++i) {
            if (userMarkers.get(i).getMarkerId() == id) {

                userMarkers.remove(i);

                break;
            }
        }

        redrawUserMarkers();
        updateClusterMarkers();
    }

    public void toggleOverlay(String overlay, boolean show) {
        if (overlayMap.containsKey(overlay) && overlayMap.get(overlay).getIsShowing() != show) {
            overlayMap.get(overlay).toggleOverlay();
        }
    }

    public int getNewMarkerID() {
        return parent.getNewMarkerID();
    }

    public void addClusterMarkers(List<ClusterMarker> clusters, List<Text> clusterValues) {
        // REMOVE THIS IF STATEMENT
        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
        clusterMarkerGroup.getChildren().clear();

        clusters.forEach(cluster -> {
            addUserDrawnMarker(cluster);
        });

        clusterValues.forEach(numNodes -> {
            clusterMarkerGroup.getChildren().add(numNodes);
            });
        }
    }

    //private List<ClusterMarker> clusterMarkers = new ArrayList<>();

    private void redrawUserMarkers() {
        drawnMarkerGroup.getChildren().clear();
        //hiddenPointMarkerGroup.getChildren().clear();

        userMarkers.forEach((marker) -> {

            if (markersShowing.contains(marker.getType())) {
                
                drawnMarkerGroup.getChildren().add(marker.getMarker());

            }
        });

        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER) && markersShowing.contains(AbstractMarker.MarkerType.POINT_MARKER)) {
            updateClusterMarkers();
        } else
            clusterMarkerGroup.getChildren().clear();
    }

    private void updateClusterMarkers() {
        hiddenPointMarkerGroup.getChildren().clear();

        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            if (m instanceof PointMarker) {
                //m.setMarkerPosition(mapWidth, mapHeight);
                SVGPath p = new SVGPath();
                p.setContent(((PointMarker) m).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        }

        userMarkers.forEach(m -> {
            if (m instanceof UserPointMarker) {
                SVGPath p = new SVGPath();
                p.setContent(((UserPointMarker) m).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        });

        clusterMarkerBuilder.update(hiddenPointMarkerGroup);
        addClusterMarkers(clusterMarkerBuilder.getClusterMarkers(), clusterMarkerBuilder.getClusterValues());
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

        redrawUserMarkers();
        redrawQueriedMarkers();
    }

    public void redrawQueriedMarkers() {
        graphMarkerGroup.getChildren().clear();
        //parent.redrawQueriedMarkers();
        //hiddenPointMarkerGroup.getChildren().clear();

        if (markers.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker map is empty");
        }

        if (markers.values().isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker values is empty");
        }

        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            drawMarker(m);
        }
    }

    public void reScaleQueriedMarkers(double scale) {
        graphMarkerGroup.getChildren().clear();
        //parent.redrawQueriedMarkers();
        //hiddenPointMarkerGroup.getChildren().clear();

        if (markers.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker map is empty");
        }

        if (markers.values().isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker values is empty");
        }

        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            m.getMarker().setScaleX(m.getMarker().getScaleX() * scale);
            m.getMarker().setScaleY(m.getMarker().getScaleY() * scale);

            if (scale < 1) {
                m.getMarker().setLayoutY(m.getMarker().getLayoutY() + (m.getMarker().getScaleY() * 10));
            } else {
                m.getMarker().setLayoutY(m.getMarker().getLayoutY() - (m.getMarker().getScaleY() * 10));
            }

            drawMarker(m);
            //m.getMarker().setLayoutY(m.getY());
        }
    }

    private void renderLayers() {
        layerGroup.getChildren().clear();
        layers.forEach(layer -> {
            //layer.setUp();
            layerGroup.getChildren().add(layer.getLayer());
        });
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return markers;
    }

    public List<AbstractMarker> getAllMarkersAsList() {
        List<AbstractMarker> allMarkers = new ArrayList<>();

        for (AbstractMarker marker : markers.values()) {
            allMarkers.add(marker);
        }

        for (AbstractMarker marker : userMarkers) {
            allMarkers.add(marker);
        }

        return allMarkers;
    }

    public void clearQueriedMarkers() {
        markers.clear();
        //graphMarkerGroup.getChildren().clear();
        //markers = null;
        //markers = new HashMap<>();
        selectedNodeList.clear();
    }

    public void clearAll() {
        clearQueriedMarkers();
        //markers.clear();
        //selectedNodeList.clear();
        userMarkers.clear();
        //mapCanvas = new Canvas();
        //mapCanvas.getChildren().clear();
        countryGroup.getChildren().clear();
        graphMarkerGroup.getChildren().clear();
        drawnMarkerGroup.getChildren().clear();
        polygonMarkerGroup.getChildren().clear();
        clusterMarkerGroup.getChildren().clear();

        hiddenPointMarkerGroup.getChildren().clear();
        overlayGroup.getChildren().clear();
        layerGroup.getChildren().clear();

    }

    public Graph getCurrentGraph() {
        return GraphManager.getDefault().getActiveGraph();
    }

    public void addMarkerIdToSelectedList(int markerID, List<Integer> selectedNodes, boolean selectingVertex) {
        selectedNodeList.add(markerID);
        PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodes, selectingVertex)).executeLater(GraphManager.getDefault().getActiveGraph());

    }

    public void panToAll() {

        int markerCounter = 0;
        double averageX = 0;
        double averageY = 0;

        for (AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker) {
                averageX += (m.getX() - 95.5);
                averageY += (m.getY() + 95.5);
                ++markerCounter;
            }
        }

        for (AbstractMarker m : userMarkers) {
            if (m instanceof UserPointMarker) {
                averageX += m.getX();
                averageY += m.getY();
                ++markerCounter;
            }
        }


        averageX /= markerCounter;
        averageY /= markerCounter;

        Rectangle r = new Rectangle();
            r.setX(averageX);
            r.setY(averageY);

        r.setWidth(5);
        r.setHeight(5);

        r.setFill(Color.RED);
            r.setOpacity(0.25);

        overlayGroup.getChildren().add(r);
        pan(averageX, averageY);
        zoom(averageX, averageY);

    }

    public void panToCenter() {
        double centerX = this.getWidth() / 2;
        double centerY = this.getHeight() / 2;


        mapStackPane.setTranslateX(centerX - mapWidth / 2);
        mapStackPane.setTranslateY(centerY - mapHeight / 2);

    }

    private void pan(double x, double y) {
        Vec3 center = new Vec3(mapWidth / 2, mapHeight / 2);

        Point2D averageMarkerPosition = mapStackPane.localToParent(x, y);

        double parentCenterX = mapStackPane.localToParent(center.x, center.y).getX();
        double parentCenterY = mapStackPane.localToParent(center.x, center.y).getY();

        Vec3 dirVect = new Vec3(parentCenterX - averageMarkerPosition.getX(), parentCenterY - averageMarkerPosition.getY());


        mapStackPane.setTranslateX(mapStackPane.getTranslateX() + dirVect.x);
        mapStackPane.setTranslateY(mapStackPane.getTranslateY() + dirVect.y);
        /*viewPortRectangleGroup.getChildren().clear();
        Rectangle r = new Rectangle();
        r.setX(x - 50);
        r.setY(y - 50);

        r.setWidth(100);
        r.setHeight(100);

        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.RED);

        viewPortRectangleGroup.getChildren().add(r);*/
    }

    public void panToSelection() {
        int markerCounter = 0;
        double averageX = 0;
        double averageY = 0;

        for (AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId())) {
                averageX += (m.getX() - 95.5);
                averageY += (m.getY() + 95.5);

                LOGGER.log(Level.SEVERE, "Marker selected x: " + (m.getX() - 95.5) + " y: " + (m.getY() + 95.5));

                ++markerCounter;
            }
        }

        LOGGER.log(Level.SEVERE, "markers selected: " + selectedNodeList.size());

        averageX /= markerCounter;
        averageY /= markerCounter;

        LOGGER.log(Level.SEVERE, "Average x: " + averageX + " Average y: " + averageY);


        pan(averageX, averageY);
        zoom(averageX, averageY);
    }

    public void zoom(double x, double y) {
        while (true) {
            double scaleFactor = 1.05;

            double oldXScale = mapStackPane.getScaleY();
            double oldYScale = mapStackPane.getScaleX();

            double newXScale = oldXScale * scaleFactor;
            double newYScale = oldYScale * scaleFactor;

            double xAdjust = (newXScale / oldXScale) - 1;
            double yAdjust = (newYScale / oldYScale) - 1;

            double moveX = mapStackPane.localToParent(x, y).getX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
            double moveY = mapStackPane.localToParent(x, y).getY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

            mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
            mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);

            mapStackPane.setScaleX(newXScale);
            mapStackPane.setScaleY(newYScale);

            if (!selectedMarkersInView()) {

                mapStackPane.setTranslateX(mapStackPane.getTranslateX() + xAdjust * moveX);
                mapStackPane.setTranslateY(mapStackPane.getTranslateY() + yAdjust * moveY);

                mapStackPane.setScaleX(oldXScale);
                mapStackPane.setScaleY(oldYScale);
                break;
            }

        }
    }

    private boolean selectedMarkersInView() {
        for (AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId())) {
                double x = (m.getX() - 95.5);
                double y = (m.getY() + 95.5);

                LOGGER.log(Level.SEVERE, "Marker selected x: " + (m.getX() - 95.5) + " y: " + (m.getY() + 95.5));

                if (!parent.getViewPortRectangle().getBoundsInLocal().contains(mapStackPane.localToParent(x, y))) {
                    return false;
                }

            }
        }

        return true;
    }

    public void generateZoomLocationUI() {
        if (!showingZoomToLocationPane) {

            double width = 100;
            double height = 75;

        BorderPane pane = new BorderPane();
            pane.prefWidth(width);
            pane.prefHeight(height);
            pane.minWidth(width);
            pane.minHeight(height);
            pane.maxWidth(width);
            pane.maxHeight(height);

            pane.setBackground(Background.fill(Color.BLACK));

            pane.setTranslateX(mapWidth / 2 - width / 2);
            pane.setTranslateY(mapHeight / 2 - height / 2);

        GridPane topGridPane = new GridPane();
            pane.setCenter(topGridPane);

        Text titleText = new Text("Zoom to Location");
            titleText.setFill(Color.WHITE);

        Button closeButton = new Button();
        closeButton.setText("X");
            closeButton.setTextFill(Color.WHITE);
            //closeButton.prefHeight(6);
            //closeButton.prefWidth(10);
            //closeButton.maxHeight(6);
            //closeButton.maxWidth(10);
            //closeButton.minHeight(6);
            //closeButton.minWidth(10);
            closeButton.setPadding(new Insets(0, 0, 0, 95));
            closeButton.setOnAction(event -> {
                showingZoomToLocationPane = false;
                event.consume();
                zoomLocationGroup.getChildren().clear();
            });

            Text geoTypeLabel = new Text("Geo Type");
            geoTypeLabel.setFill(Color.WHITE);

            ComboBox<String> geoTypeMenu = new ComboBox<>(FXCollections.observableList(Arrays.asList("Coordinate", "Geohash", "MGRS")));
            geoTypeMenu.getSelectionModel().selectFirst();
            Text lattitudeLabel = new Text("Lattitude");
            lattitudeLabel.setFill(Color.AQUA);

            Text longitudeLabel = new Text("Longitude");
            longitudeLabel.setFill(Color.AQUA);

            Text radiusLabel = new Text("Radius");
            radiusLabel.setFill(Color.AQUA);

            TextField lattitudeInput = new TextField();
            lattitudeInput.setBorder(Border.stroke(Color.AQUA));
            //lattitudeInput.minWidth(180);
            /*coordinateInput.minHeight(10);
            coordinateInput.maxWidth(180);
            coordinateInput.maxHeight(10);
            coordinateInput.prefWidth(180);
            coordinateInput.prefHeight(10);
            coordinateInput.setPromptText("Enter a coordinate in decimal degrees (and optionally a radis in kilometers) with components seperated by spaces of commas");*/

            lattitudeInput.setBackground(Background.fill(Color.WHITE));

            TextField longitudeInput = new TextField();
            longitudeInput.setBackground(Background.fill(Color.WHITE));
            longitudeInput.setBorder(Border.stroke(Color.AQUA));

            TextField radiusInput = new TextField();
            radiusInput.setBackground(Background.fill(Color.WHITE));
            radiusInput.setBorder(Border.stroke(Color.AQUA));

            GridPane coordinateGridPane = new GridPane();
            coordinateGridPane.setHgap(10);
            coordinateGridPane.setPadding(new Insets(0, 0, 0, 10));

            coordinateGridPane.add(lattitudeLabel, 0, 0);
            coordinateGridPane.add(longitudeLabel, 1, 0);
            coordinateGridPane.add(radiusLabel, 2, 0);

            coordinateGridPane.add(lattitudeInput, 0, 1);
            coordinateGridPane.add(longitudeInput, 1, 1);
            coordinateGridPane.add(radiusInput, 2, 1);

            Text geoHashLabel = new Text("Base-16 geohash value");
            TextField geoHashInput = new TextField();

            Text mgrsLabel = new Text("MGRS value");
            TextField mgrsInput = new TextField();

            geoTypeMenu.setOnAction(event -> {

                coordinateGridPane.getChildren().clear();
                String selectedItem = geoTypeMenu.getSelectionModel().getSelectedItem();

                if (selectedItem.equals("Coordinate")) {
                    coordinateGridPane.add(lattitudeLabel, 0, 0);
                    coordinateGridPane.add(longitudeLabel, 1, 0);
                    coordinateGridPane.add(radiusLabel, 2, 0);

                    coordinateGridPane.add(lattitudeInput, 0, 1);
                    coordinateGridPane.add(longitudeInput, 1, 1);
                    coordinateGridPane.add(radiusInput, 2, 1);

                } else if (selectedItem.equals("Geohash")) {
                    geoHashLabel.setFill(Color.AQUA);
                    geoHashInput.setBorder(Border.stroke(Color.AQUA));
                    coordinateGridPane.add(geoHashLabel, 0, 0);
                    coordinateGridPane.add(geoHashInput, 0, 1);
                } else if (selectedItem.equals("MGRS")) {
                    mgrsLabel.setFill(Color.AQUA);
                    mgrsInput.setBorder(Border.stroke(Color.AQUA));
                    coordinateGridPane.add(mgrsLabel, 0, 0);
                    coordinateGridPane.add(mgrsInput, 0, 1);
                }


            });


            Button okButton = new Button("OK");
            okButton.setTextFill(Color.WHITE);

            Button cancelButton = new Button("Cancel");
            cancelButton.setTextFill(Color.WHITE);

            cancelButton.setOnAction(event -> {
                showingZoomToLocationPane = false;
                event.consume();
                zoomLocationGroup.getChildren().clear();
            });

            GridPane bottomGridPane = new GridPane();

        topGridPane.add(titleText, 0, 0);
            topGridPane.add(closeButton, 2, 0);
            topGridPane.add(geoTypeLabel, 0, 1);
            topGridPane.add(geoTypeMenu, 1, 1, 2, 1);

            topGridPane.add(coordinateGridPane, 0, 2);



            bottomGridPane.add(okButton, 0, 0);
            bottomGridPane.add(cancelButton, 1, 0);

            topGridPane.add(bottomGridPane, 3, 4);



            zoomLocationGroup.getChildren().add(pane);

            showingZoomToLocationPane = true;
        }
    }


    public ObservableList<Node> getPointMarkersOnMap() {
        return hiddenPointMarkerGroup.getChildren();
    }

    // public void clearListeners() {
        //markerColourProperty = null;
        //markerColourProperty = new SimpleStringProperty();
    //}
    public StringProperty getMarkerTextProperty() {
        return markerTextProperty;
    }

    public void drawMarker(AbstractMarker marker) {

        if (markersShowing.contains(marker.getType())) {
            marker.setMarkerPosition(mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());
            if (!graphMarkerGroup.getChildren().contains(marker.getMarker())) {
                graphMarkerGroup.getChildren().add(marker.getMarker());
            }
        }

        /*if (marker instanceof PointMarker) {
            PointMarker p = (PointMarker) marker;

            Rectangle r = new Rectangle();
            r.setWidth(5);
            r.setHeight(5);

            r.setX(p.getX() - 97);
            r.setY(p.getY() + 93);
            r.setFill(Color.RED);
            graphMarkerGroup.getChildren().addAll(r);
            //thessianMarkersGroup.getChildren().addAll(marker.getMarker());
        }*/

    }


    public void addMarkerToHashMap(String key, AbstractMarker e) {
        markers.put(key, e);
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
                        svgPath.setStrokeWidth(0.025);
                        svgPath.setStroke(Color.BLACK);
                        svgPath.setContent(path);

                        countrySVGPaths.add(svgPath);

                        path = "";
                    }

                }

                //clearQueriedMarkers();
                //redrawQueriedMarkers();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception thrown");
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }



}

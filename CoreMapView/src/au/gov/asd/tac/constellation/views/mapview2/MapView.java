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
import au.gov.asd.tac.constellation.utilities.geospatial.Geohash;
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
import au.gov.tac.constellation.views.mapview2.utillities.MarkerUtilities;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
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
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author altair1673
 */
public class MapView extends ScrollPane {

    private MapViewPane parent;

    private final StackPane mapStackPane;

    // ID of th enext user drawn marker
    private int drawnMarkerId = 0;

    // Flags for the different drawing modes
    private boolean drawingCircleMarker = false;
    private boolean drawingPolygonMarker = false;

    // Furthest lattiude to the east and west
    public static final double minLong = -169.1110266;
    public static final double maxLong = 190.48712;

    // Furthest longitude to the north and south
    public static final double minLat = -58.488473;
    public static final double maxLat = 83.63001;

    public static final double mapWidth = 1010.33;
    public static final double mapHeight = 1224;

    // Two containers that hold queried markers and user drawn markers
    private Map<String, AbstractMarker> markers = new HashMap<>();
    private final List<AbstractMarker> userMarkers = new ArrayList<AbstractMarker>();

    // Set of the types of markers currently showing on screen
    private Set<AbstractMarker.MarkerType> markersShowing = new HashSet<>();

    // Set of overlays that are active at all times
    private Map<String, AbstractOverlay> overlayMap = new HashMap<>();

    private final List<Integer> selectedNodeList = new ArrayList<>();

    // The group that contains all the country graphics
    private Group countryGroup;

    // The two groups that hold the queried and user marker groups
    private Group graphMarkerGroup;

    private Group drawnMarkerGroup;

    // Groups for user drawn polygons, cluster markers and "hidden" point markers used for cluster calculations
    private Group polygonMarkerGroup;
    private Group clusterMarkerGroup;
    public Group hiddenPointMarkerGroup;

    // Groups for the ovelays and layers
    private final Group overlayGroup;
    private final Group layerGroup;

    // Attribute and Identifier text groups for markers
    private final Group pointMarkerTextGroup;

    // Group for thessian polygons
    private final Group thessianMarkersGroup;

    // Group for the rectangle the user drags to select multiple markers
    private final Group selectionRectangleGroup;
    private final Group viewPortRectangleGroup;
    private final Group zoomLocationGroup;

    // Flag for zoom to location menu
    private boolean showingZoomToLocationPane = false;

    private static final Logger LOGGER = Logger.getLogger("MapView");

    Vec3 medianPositionOfMarkers = null;

    // Factor to scale map by when zooming
    private final double mapScaleFactor = 1.1;

    private double scale = 1.0;
    //private final SVGPath p = new SVGPath();

    // The paths for the edges of all the countries
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    // The UI overlys for the shapes the user can draw
    private CircleMarker circleMarker = null;
    private PolygonMarker polygonMarker = null;
    private Rectangle selectionRectangle = null;

    // Flag is the user is selecting multiple markers on screen
    private boolean isSelectingMultiple = false;
    private double selectionRectangleX = 0;
    private double selectionRectangleY = 0;

    private ArrayList<ArrayList<Node>> pointMarkerClusters = new ArrayList<ArrayList<Node>>();
    private Set<Node> clusteredPointMarkers = new HashSet<>();

    // Flag for the line the user draws to smell distance between
    private boolean drawingMeasureLine = false;
    private Line measureLine = null;

    // Panning variables
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    private int markerID = 0;

    // Pane that hold all the groups for allt he different graphical outputs
    private final Pane mapGroupHolder = new Pane();

    private Rectangle clipRectangle = new Rectangle();

    // All the layers are stored here
    private final List<AbstractMapLayer> layers = new ArrayList<>();

    public static ToolsOverlay toolsOverlay = null;
    public static InfoOverlay infoOverlay = null;

    private final double toolOverlayWidth = 815;
    private final double toolsOverlayHeight = 20;

    private final double infoOverlayWidth = 20;
    private final double infoOverlayHeight = 20;

    private MapView self = this;

    public ClusterMarkerBuilder clusterMarkerBuilder = null;

    // Flags for what attribute marker colour and text should be derived from
    private StringProperty markerColourProperty = new SimpleStringProperty();
    private StringProperty markerTextProperty = new SimpleStringProperty();

    //private final Region testRegion = new Region();

    public MapView(MapViewPane parent) {
        this.parent = parent;
        LOGGER.log(Level.SEVERE, "In MapView constructor");

        clusterMarkerBuilder = new ClusterMarkerBuilder(this);

        // Instasntiate all the groups for the graphical outputs
        countryGroup = new Group();
        graphMarkerGroup = new Group();
        drawnMarkerGroup = new Group();
        polygonMarkerGroup = new Group();
        overlayGroup = new Group();
        layerGroup = new Group();
        clusterMarkerGroup = new Group();
        hiddenPointMarkerGroup = new Group();
        hiddenPointMarkerGroup.setVisible(false);
        pointMarkerTextGroup = new Group();
        thessianMarkersGroup = new Group();
        selectionRectangleGroup = new Group();
        zoomLocationGroup = new Group();
        viewPortRectangleGroup = new Group();

        // By default all markers should be showing
        markersShowing.add(AbstractMarker.MarkerType.LINE_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POINT_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POLYGON_MARKER);

        mapGroupHolder.setBackground(Background.fill(new Color(0.722, 0.871, 0.902, 1)));

        // Clear any existing paths and read the SVG paths of countries from the files
        countrySVGPaths.clear();
        parseMapSVG();

        // The stackPane to store
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

        // Scoll to zoom
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

                // Scroll factor is more or less than 1 depending on which way the scroll wheele is scrolled
                double scaleFactor = (e.getDeltaY() > 0) ? mapScaleFactor : 1 / mapScaleFactor;

                // Get the current scale of the map view
                double oldXScale = mapStackPane.getScaleX();
                double oldYScale = mapStackPane.getScaleY();

                // Change the scale based on which way the scroll wheel is turned
                double newXScale = oldXScale * scaleFactor;
                double newYScale = oldYScale * scaleFactor;

                // Calculate how much the map will have to move
                double xAdjust = (newXScale / oldXScale) - 1;
                double yAdjust = (newYScale / oldYScale) - 1;

                // Calculate how much the map will have to move
                double moveX = e.getSceneX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
                double moveY = e.getSceneY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

                // Move the map
                mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
                mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);

                // Scale the map
                mapStackPane.setScaleX(newXScale);
                mapStackPane.setScaleY(newYScale);

                // If cluster markers are showing then update them based on distance between markers
                if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
                    updateClusterMarkers();
                }

                // Update how many markers are on screen
                for (AbstractMarker m : markers.values()) {
                    if (self.getParent().getBoundsInLocal().contains(m.getMarker().getBoundsInParent())) {
                        ++nodesOnScreen;
                    }
                }

            }
        });

        // Panning code
        // Record where the mouse has been pressed
        mapStackPane.setOnMousePressed(event -> {
            //calculateMedianMarkerPosition();

            // Coordinate within scene
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            // Position of map when moise is clicked
            transalateX = mapStackPane.getTranslateX();
            transalateY = mapStackPane.getTranslateY();

        });

        // When user drags the mouse
        mapStackPane.setOnMouseDragged(event -> {
            // Check they are dragging the right mouse button
            if (event.isSecondaryButtonDown()) {

                Node node = (Node) event.getSource();

                // Move the map
                mapStackPane.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX)));
                mapStackPane.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY)));


                event.consume();
            }

        });

        // Clear any country grpahics that already exist within group
        countryGroup.getChildren().clear();

        // Add paths to group to display them on screen
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }

        // Set default pan behaviour to false
        this.setPannable(false);

        // No scroll bars allowed!!
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        setContent(mapStackPane);

        // Center content
        this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);

        // Add the country group the the the pane
        mapGroupHolder.setPrefWidth(mapWidth);
        mapGroupHolder.setPrefHeight(mapHeight);
        mapGroupHolder.getChildren().add(countryGroup);

        // INstantiate overlays
        toolsOverlay = new ToolsOverlay(toolOverlayWidth, toolsOverlayHeight);
        infoOverlay = new InfoOverlay(infoOverlayWidth, infoOverlayHeight);

        // Put overlays in map
        overlayMap.put(MapViewPane.TOOLS_OVERLAY, toolsOverlay);
        overlayMap.put(MapViewPane.INFO_OVERLAY, infoOverlay);

        markerColourProperty.set(parent.DEFAULT_COLOURS);
        // Event listener for what colour point markers should be
        markerColourProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                // Loop through all markers on screen
                for (Object value : markers.values()) {
                    AbstractMarker m = (AbstractMarker) value;

                    // If the marker is a point marker
                    if (m instanceof PointMarker) {
                        PointMarker p = (PointMarker) m;

                        // Change the marker colour
                        p.changeMarkerColour(newValue);

                    }

                    //drawMarker(m);
                }
            }
        });

        // Handler for what type of text to display under the markers
        markerTextProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Clear any existing text
                pointMarkerTextGroup.getChildren().clear();

                // Loop through all the markers
                for (Object value : markers.values()) {
                    AbstractMarker m = (AbstractMarker) value;

                    // If its a point marker change its text
                    if (m instanceof PointMarker) {
                        PointMarker p = (PointMarker) m;

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

        // Event handler when mouse is clicked on the map
        mapGroupHolder.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // If left clicked
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    // if double clicked desekect all marekers from both the map and the graph in consty
                    if (event.getClickCount() == 2) {

                        deselectAllMarkers();
                        selectedNodeList.clear();
                        PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodeList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                        LOGGER.log(Level.SEVERE, "Double clicked");
                        event.consume();
                        return;
                    }
                }

                // Record location of mouse click
                double x = event.getX();
                double y = event.getY();

                // If drawing is enabled and measurment is disabled
                if (toolsOverlay.getDrawingEnabled().get() && !toolsOverlay.getMeasureEnabled().get()) {

                    // If shift is down then display circle drawing
                    if (event.isShiftDown()) {
                        drawingCircleMarker = true;
                        circleMarker = new CircleMarker(self, drawnMarkerId++, x, y, 0, 100, 100);
                        polygonMarkerGroup.getChildren().add(circleMarker.getUICircle());
                        polygonMarkerGroup.getChildren().add(circleMarker.getUILine());

                    } // If user is drawing a circle then add it to the map and clear any UI elements
                    else if (drawingCircleMarker) {
                        circleMarker.generateCircle();
                        drawnMarkerGroup.getChildren().add(circleMarker.getMarker());
                        userMarkers.add(circleMarker);
                        circleMarker = null;
                        polygonMarkerGroup.getChildren().clear();
                        drawingCircleMarker = false;

                        // Reset the distance shown on the tools ovelay
                        toolsOverlay.resetMeasureText();

                    } // If control is down
                    else if (event.isControlDown()) {
                        // if user is not current;y drawing a polygon marker or a circle marker then show UI to draw a polygon on screen
                        if (!drawingPolygonMarker && !drawingCircleMarker) {
                            polygonMarker = new PolygonMarker(self, drawnMarkerId++, 0, 0);

                            polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));
                            drawingPolygonMarker = true;
                        } else {
                            polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));
                        }
                        toolsOverlay.resetMeasureText();
                    } // If the user is drawing a polygon marker then add the polygon to the screen and clear any UI elements
                    else if (drawingPolygonMarker) {
                        drawingPolygonMarker = false;
                        polygonMarker.generatePath();
                        addUserDrawnMarker(polygonMarker);
                        toolsOverlay.resetMeasureText();
                        userMarkers.add(polygonMarker);
                        polygonMarker.endDrawing();
                        polygonMarkerGroup.getChildren().clear();
                    } // If the user is not drawing any type of marker then generate point marker where they clicked
                    else if (!drawingPolygonMarker && !drawingCircleMarker) {
                        UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 95, -95);
                        marker.setMarkerPosition(0, 0);

                        userMarkers.add(marker);
                        updateClusterMarkers();
                    }
                } // If drawing is not enabled but measuring is
                else if (!toolsOverlay.getDrawingEnabled().get() && toolsOverlay.getMeasureEnabled().get()) {

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

                }
                event.consume();
            }

        });

        // If the mouse is moved
        mapGroupHolder.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();

                // Change lattitude and logitude text on info overlay if its showing
                if (infoOverlay != null && infoOverlay.getIsShowing()) {
                    infoOverlay.updateLocation(x, y);
                }

                // If drawing is enabled
                if (toolsOverlay.getDrawingEnabled().get() && !toolsOverlay.getMeasureEnabled().get()) {

                    // if drawing circle marker then change the circle marker UI's radius to the distance between the original click position and the mouse's
                    // current position
                    if (drawingCircleMarker && circleMarker != null && !drawingPolygonMarker) {

                        circleMarker.setRadius(x, y);
                        circleMarker.setLineEnd(x, y);

                        // Change distance text on tools overlay
                        toolsOverlay.setDistanceText(circleMarker.getCenterX(), circleMarker.getCenterY(), x, y);

                    } // If the user is drawing a polygon marker then update the polygon drawing UI
                    else if (drawingPolygonMarker && polygonMarker != null && !drawingCircleMarker) {
                        polygonMarker.setEnd(x, y);

                        // Update distance text
                        toolsOverlay.setDistanceText(polygonMarker.getCurrentLine().getStartX(), polygonMarker.getCurrentLine().getStartY(), polygonMarker.getCurrentLine().getEndX(), polygonMarker.getCurrentLine().getEndY());
                    }
                } // If the user isn't drawing anything but IS measuring distance then update the size of the measurement line
                else if (toolsOverlay.getMeasureEnabled().get() && !toolsOverlay.getDrawingEnabled().get()) {
                    if (measureLine != null) {
                        measureLine.setEndX(event.getX());
                        measureLine.setEndY(event.getY());

                        toolsOverlay.setDistanceText(measureLine.getStartX(), measureLine.getStartY(), measureLine.getEndX(), measureLine.getEndY());
                    }
                }

                event.consume();
            }

        });

        // When mouse is pressed
        mapGroupHolder.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // create the selection rectangle
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

                }

            }
        });

        // When mouse is dragged
        mapGroupHolder.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // If the user is draing a selection rectangle
                if (isSelectingMultiple) {
                    double x = event.getX();
                    double y = event.getY();

                    double width = 0;
                    double height = 0;

                    // Change positiona nd zie of rectangle based on where the user moves their mouse
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

        // When mouse is released
        mapGroupHolder.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // If the user is selecting multiple markers
                if (isSelectingMultiple) {
                    selectedNodeList.clear();

                    double pointMarkerXOffset = 95.5;
                    double pointMarkerYOffset = 95.5;
                    List<Integer> idList = new ArrayList<>();

                    // Loop through all the markers
                    for (AbstractMarker m : markers.values()) {
                        if (m instanceof PointMarker) {
                            PointMarker p = (PointMarker) m;


                            p.deselect();

                            // If marker is within the selection rectangle then select the marker
                            if (selectionRectangle.contains(p.getX() - pointMarkerXOffset, p.getY() + pointMarkerYOffset)) {

                                p.select();

                                idList.addAll(p.getConnectedNodeIdList());
                                selectedNodeList.add(p.getMarkerId());
                            }

                        }
                    }

                    // Select all noded that correspond to selected markers in consty
                    PluginExecution.withPlugin(new SelectOnGraphPlugin(idList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                    isSelectingMultiple = false;
                    selectionRectangleGroup.getChildren().clear();
                    LOGGER.log(Level.SEVERE, "Mouse Released");
                }

            }
        });

        // Add all garphical groups to pane
        mapGroupHolder.getChildren().add(hiddenPointMarkerGroup);
        mapGroupHolder.getChildren().add(graphMarkerGroup);
        mapGroupHolder.getChildren().addAll(drawnMarkerGroup);
        mapGroupHolder.getChildren().add(clusterMarkerGroup);
        mapGroupHolder.getChildren().addAll(polygonMarkerGroup);
        mapGroupHolder.getChildren().add(overlayGroup);
        mapGroupHolder.getChildren().add(layerGroup);
        mapGroupHolder.getChildren().add(pointMarkerTextGroup);
        mapGroupHolder.getChildren().add(thessianMarkersGroup);

        mapGroupHolder.getChildren().add(zoomLocationGroup);
        overlayGroup.getChildren().addAll(toolsOverlay.getOverlayPane());
        overlayGroup.getChildren().addAll(infoOverlay.getOverlayPane());
        mapGroupHolder.getChildren().add(selectionRectangleGroup);
        mapGroupHolder.getChildren().add(viewPortRectangleGroup);

    }

    // Sets text on the map at a specific location
    private void setPointMarkerText(String markerText, PointMarker p) {
        Text t = new Text(markerText);
        t.setX(p.getX() - 125);
        t.setY(p.getY() + 103);

        pointMarkerTextGroup.getChildren().add(t);
    }

    // Deselects all markers
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

    // Add user drawn marker on screen
    private void addUserDrawnMarker(AbstractMarker marker) {
        // Only draw marker is it's type is set to be showing on screen
        if (markersShowing.contains(marker.getType())) {

            if (marker instanceof ClusterMarker) {
                clusterMarkerGroup.getChildren().add(marker.getMarker());
                return;
            }

            drawnMarkerGroup.getChildren().addAll(marker.getMarker());
        }
    }

    // Remove a user drawn marker
    public void removeUserMarker(int id) {
        // Loop through all the user drawn markers and if their id matches with the one passed in
        // to this function then remove them
        for (int i = 0; i < userMarkers.size(); ++i) {
            if (userMarkers.get(i).getMarkerId() == id) {

                userMarkers.remove(i);

                break;
            }
        }

        // Redraw the user markers now that one has been removed
        redrawUserMarkers();
        updateClusterMarkers();
    }

    // Hide/Show diferent map pverlays
    public void toggleOverlay(String overlay, boolean show) {
        if (overlayMap.containsKey(overlay) && overlayMap.get(overlay).getIsShowing() != show) {
            overlayMap.get(overlay).toggleOverlay();
        }
    }

    public int getNewMarkerID() {
        return parent.getNewMarkerID();
    }

    // Add a cluster marker
    public void addClusterMarkers(List<ClusterMarker> clusters, List<Text> clusterValues) {

        // If cluster marker is showing
        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
        clusterMarkerGroup.getChildren().clear();

            // Draw each cluster marker on map
        clusters.forEach(cluster -> {
            addUserDrawnMarker(cluster);
        });

            // Add how many markers are in one cluster
        clusterValues.forEach(numNodes -> {
            clusterMarkerGroup.getChildren().add(numNodes);
            });
        }
    }

    private void redrawUserMarkers() {

        drawnMarkerGroup.getChildren().clear();

        // Redraw all user created markers
        userMarkers.forEach((marker) -> {

            if (markersShowing.contains(marker.getType())) {
                
                drawnMarkerGroup.getChildren().add(marker.getMarker());

            }
        });

        // Cluster markers are showing as well as point markers then update the clister markers
        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER) && markersShowing.contains(AbstractMarker.MarkerType.POINT_MARKER)) {
            updateClusterMarkers();
        } else
            clusterMarkerGroup.getChildren().clear();
    }

    private void updateClusterMarkers() {
        hiddenPointMarkerGroup.getChildren().clear();

        // Add point markers to the hiddenPointMarker groups
        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            if (m instanceof PointMarker) {
                //m.setMarkerPosition(mapWidth, mapHeight);
                SVGPath p = new SVGPath();
                p.setContent(((PointMarker) m).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        }

        // Add user created markers to the hiddenPointMarker group
        userMarkers.forEach(m -> {
            if (m instanceof UserPointMarker) {
                SVGPath p = new SVGPath();
                p.setContent(((UserPointMarker) m).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        });

        // Calculate the clusters
        clusterMarkerBuilder.update(hiddenPointMarkerGroup);

        // Add cluster markers to map
        addClusterMarkers(clusterMarkerBuilder.getClusterMarkers(), clusterMarkerBuilder.getClusterValues());
    }

    // Sets up and adds a layer to the map
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

    // Hide/Shows a type of marker on the screen
    public void updateShowingMarkers(AbstractMarker.MarkerType type, boolean adding) {

        // Add or remove a type of marker
        if (markersShowing.contains(type) && !adding) {
            markersShowing.remove(type);
        } else if (!markersShowing.contains(type) && adding) {
            markersShowing.add(type);
        }

        // Redraw all markers on screen
        redrawUserMarkers();
        redrawQueriedMarkers();
    }

    public List<AbstractMapLayer> getLayers() {
        return layers;
    }

    public void redrawQueriedMarkers() {
        graphMarkerGroup.getChildren().clear();


        if (markers.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker map is empty");
        }

        if (markers.values().isEmpty()) {
            LOGGER.log(Level.SEVERE, "Marker values is empty");
        }

        // Redraw all queried markers
        for (Object value : markers.values()) {
            AbstractMarker m = (AbstractMarker) value;
            drawMarker(m);
        }
    }

    // Scale the size of the markers
    public void reScaleQueriedMarkers(double scale) {
        graphMarkerGroup.getChildren().clear();

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

            // Scale markers up and down based on whether or not the map is being zoomed in or out
            if (scale < 1) {
                m.getMarker().setLayoutY(m.getMarker().getLayoutY() + (m.getMarker().getScaleY() * 10));
            } else {
                m.getMarker().setLayoutY(m.getMarker().getLayoutY() - (m.getMarker().getScaleY() * 10));
            }

            drawMarker(m);

        }
    }

    // Rredraw layers
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
        selectedNodeList.clear();
    }

    public void clearAll() {
        clearQueriedMarkers();

        userMarkers.clear();

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

        // Add the positions of all queried markers
        for (AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker) {
                averageX += (m.getX() - 95.5);
                averageY += (m.getY() + 95.5);
                ++markerCounter;
            }
        }

        // Add the positions of all user markers
        for (AbstractMarker m : userMarkers) {
            if (m instanceof UserPointMarker) {
                averageX += m.getX();
                averageY += m.getY();
                ++markerCounter;
            }
        }

        // find average position
        averageX /= markerCounter;
        averageY /= markerCounter;

        // Pan to location
        pan(averageX, averageY);

        // Zoom to average location
        zoom(averageX, averageY, true);

    }

    // Pan to the center of the component
    public void panToCenter() {
        double centerX = this.getWidth() / 2;
        double centerY = this.getHeight() / 2;


        mapStackPane.setTranslateX(centerX - mapWidth / 2);
        mapStackPane.setTranslateY(centerY - mapHeight / 2);

    }

    // Pan to specific location
    private void pan(double x, double y) {

        Vec3 center = new Vec3(mapWidth / 2, mapHeight / 2);

        Point2D averageMarkerPosition = mapStackPane.localToParent(x, y);

        double parentCenterX = mapStackPane.localToParent(center.x, center.y).getX();
        double parentCenterY = mapStackPane.localToParent(center.x, center.y).getY();

        Vec3 dirVect = new Vec3(parentCenterX - averageMarkerPosition.getX(), parentCenterY - averageMarkerPosition.getY());


        mapStackPane.setTranslateX(mapStackPane.getTranslateX() + dirVect.x);
        mapStackPane.setTranslateY(mapStackPane.getTranslateY() + dirVect.y);
    }

    public void panToSelection() {
        int markerCounter = 0;
        double averageX = 0;
        double averageY = 0;

        // Add coordinates of the all selected markers
        for (AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId())) {
                averageX += (m.getX() - 95.5);
                averageY += (m.getY() + 95.5);


                ++markerCounter;
            }
        }

        // Average location of all selecter markers
        averageX /= markerCounter;
        averageY /= markerCounter;



        pan(averageX, averageY);
        zoom(averageX, averageY, false);
    }

    public void zoom(double x, double y, boolean allMarkers) {
        double scaleFactor = 1.05;
        boolean zoomIn = true;

        // Keep zooming in
        while (true) {

            double oldXScale = mapStackPane.getScaleX();
            double oldYScale = mapStackPane.getScaleY();

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

            // When required markers are no longer in view then zoom out untill they are and break
            if (!selectedMarkersInView(allMarkers) && zoomIn) {
                scaleFactor = 1 / 1.05;
                zoomIn = false;
            } else if (selectedMarkersInView(allMarkers) && !zoomIn)
                break;

        }
    }

    // Calculate if all or selected marker sare in view
    private boolean selectedMarkersInView(boolean allMarkers) {
        for (AbstractMarker m : markers.values()) {
            if ((m instanceof PointMarker && allMarkers) || (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId()) && !allMarkers)) {
                double x = (m.getX() - 100);
                double y = (m.getY() + 80);

                Rectangle r = new Rectangle();
                r.setWidth(10);
                r.setHeight(17.5);
                r.setX(x);
                r.setY(y);

                if (!(parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y)) && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y)) && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y + r.getHeight())) && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y + r.getHeight())))) {
                    return false;
                }

            }
        }

        return true;
    }

    // Create the zoom to location UI
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

            pane.setTranslateX((mapWidth / 2) - 300);
            pane.setTranslateY(mapHeight / 2 - height / 2);

        GridPane topGridPane = new GridPane();
            pane.setCenter(topGridPane);

        Text titleText = new Text("Zoom to Location");
            titleText.setFill(Color.WHITE);

        Button closeButton = new Button();
        closeButton.setText("X");
            closeButton.setTextFill(Color.WHITE);

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

            // Change coordinate UI based on geoType
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

            okButton.setOnAction(event -> {
                String selectedGeoType = geoTypeMenu.getSelectionModel().getSelectedItem();

                // Convert coordinate input to x and y coordinates
                if (selectedGeoType.equals("Coordinate")) {

                    double lattitude = -3000;
                    double longitude = -3000;
                    double radius = -3000;

                    String lattitudeText = lattitudeInput.getText();
                    String longitudeText = longitudeInput.getText();
                    String radiusText = radiusInput.getText();

                    if (!lattitudeText.isBlank() && !lattitudeText.isEmpty() && NumberUtils.isParsable(lattitudeText.strip())) {
                        lattitude = Double.parseDouble(lattitudeText.strip());
                    } else {
                        return;
                    }

                    if (!longitudeText.isBlank() && !longitudeText.isEmpty() && NumberUtils.isParsable(longitudeText.strip())) {
                        longitude = Double.parseDouble(longitudeText.strip());
                    } else {
                        return;
                    }

                    double x = MarkerUtilities.longToX(longitude, minLong, mapWidth, maxLong - minLong);
                    double y = MarkerUtilities.latToY(lattitude, mapWidth, mapHeight) - 149;

                    if (!radiusText.isBlank() && !radiusText.isEmpty() && NumberUtils.isParsable(radiusText.strip())) {
                        radius = Double.parseDouble(radiusText.strip());

                        // draw circle at entered location
                        CircleMarker zoomCircleMarker = new CircleMarker(self, drawnMarkerId++, x, y, radius, 100, 100);

                        zoomCircleMarker.generateCircle();
                        drawnMarkerGroup.getChildren().add(zoomCircleMarker.getMarker());
                        userMarkers.add(zoomCircleMarker);
                    } // If no radius is provided then draw point marker at location
                    else {
                        UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 95, -95);
                        marker.setMarkerPosition(0, 0);

                        addUserDrawnMarker(marker);

                        userMarkers.add(marker);
                    }

                } // Convert mgrs value to x and y
                else if (selectedGeoType.equals("MGRS")) {
                    String mgrs = mgrsInput.getText().strip();

                    if (!mgrs.isBlank() && !mgrs.isEmpty()) {
                        final MGRSCoord coordinate = MGRSCoord.fromString(mgrs, null);
                        double x = MarkerUtilities.longToX(coordinate.getLongitude().degrees, minLong, mapWidth, maxLong - minLong);
                        double y = MarkerUtilities.latToY(coordinate.getLatitude().degrees, mapWidth, mapHeight) - 149;

                        UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 96.05, -96.15);
                        marker.setMarkerPosition(0, 0);

                        addUserDrawnMarker(marker);

                        userMarkers.add(marker);
                    }
                } // Convert geohash value to x and y
                else if (selectedGeoType.equals("Geohash")) {
                    String location = geoHashInput.getText().strip();

                    if (!location.isBlank() && !location.isEmpty()) {
                        final double[] geohashCoordinates = Geohash.decode(location, Geohash.Base.B32);
                        double x = MarkerUtilities.longToX(geohashCoordinates[1] - geohashCoordinates[3], minLong, mapWidth, maxLong - minLong);
                        double y = MarkerUtilities.latToY(geohashCoordinates[0] - geohashCoordinates[2], mapWidth, mapHeight) - 149;

                        UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 95, -95);
                        marker.setMarkerPosition(0, 0);


                        addUserDrawnMarker(marker);

                        userMarkers.add(marker);

                    }
                }

                showingZoomToLocationPane = false;
                event.consume();
                zoomLocationGroup.getChildren().clear();
            });

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

    public List<AbstractMarker> getUserMarkers() {
        return userMarkers;
    }

    public List<Integer> getSelectedNodeList() {
        return selectedNodeList;
    }

    public ObservableList<Node> getPointMarkersOnMap() {
        return hiddenPointMarkerGroup.getChildren();
    }

    public StringProperty getMarkerTextProperty() {
        return markerTextProperty;
    }

    // Draw a user created marker on the screen
    public void drawMarker(AbstractMarker marker) {

        if (markersShowing.contains(marker.getType())) {
            marker.setMarkerPosition(mapGroupHolder.getPrefWidth(), mapGroupHolder.getPrefHeight());
            if (!graphMarkerGroup.getChildren().contains(marker.getMarker())) {
                graphMarkerGroup.getChildren().add(marker.getMarker());
            }
        }

    }

    public Group getGraphMarkerGroup() {
        return graphMarkerGroup;
    }

    public void addMarkerToHashMap(String key, AbstractMarker e) {
        markers.put(key, e);
    }

    public Set<AbstractMarker.MarkerType> getMarkersShowing() {
        return markersShowing;
    }

    // Load the world map
    private void parseMapSVG() {
        countryGroup.getChildren().clear();

        // Read map from file
        try {
            try (BufferedReader bFileReader = new BufferedReader(new FileReader("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapView\\resources\\MercratorMapView4.txt"))) {
                String path = "";
                String line = "";

                // While there is more to read
                while ((line = bFileReader.readLine()) != null) {
                    // Strip the line read in
                    line = line.strip();

                    // Extract the svg path segment from the line
                    if (line.startsWith("<path")) {
                        int startIndex = line.indexOf("d=");

                        int endIndex = line.indexOf(">");

                        path = line.substring(startIndex + 3, endIndex - 2);

                        // Create the SVGPath object and add it to an array
                        SVGPath svgPath = new SVGPath();
                        svgPath.setFill(Color.WHITE);
                        svgPath.setStrokeWidth(0.025);
                        svgPath.setStroke(Color.BLACK);
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

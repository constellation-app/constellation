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

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.utilities.geospatial.Geohash;


import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;

import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.CircleMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.ClusterMarkerBuilder;
import au.gov.asd.tac.constellation.views.mapview2.markers.GeoShapePolygonMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PolygonMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.asd.tac.constellation.views.mapview2.overlays.AbstractOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.InfoOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.OverviewOverlay;
import au.gov.asd.tac.constellation.views.mapview2.overlays.ToolsOverlay;
import au.gov.asd.tac.constellation.views.mapview2.plugins.SelectOnGraphPlugin;
import au.gov.asd.tac.constellation.views.mapview2.utilities.GeoShape;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * This class holds the actual MapView vector graphic and all panes asscoiated
 * with it Its got all of the event handlers for drawing, labels, colours,
 * markers etc... It has all the groups that hold all other graphical elements
 * such as cluster markers, daynight layer and thiessen polygon layer
 *
 * @author altair1673
 */
public class MapView extends ScrollPane {

    private final MapViewPane parent;

    private final StackPane mapStackPane;

    // ID of the next user drawn marker
    private int drawnMarkerId = 0;

    private double pointMarkerGlobalScale = 0.05;

    // Flags for the different drawing modes
    private boolean drawingCircleMarker = false;
    private boolean drawingPolygonMarker = false;

    // Furthest longitude to the east and west
    public static final double MIN_LONG = -180;
    public static final double MAX_LONG = 180;

    // Furthest lattitude to the north and south
    public static final double MIN_LAT = -85.0511;
    public static final double MAX_LAT = 85.0511;

    public static final double MAP_VIEWPORT_WIDTH = 1010;
    public static final double MAP_VIEWPORT_HEIGHT = 1224;

    public static final double MAP_WIDTH = 400;
    public static final double MAP_HEIGHT = 400;

    // Two containers that hold queried markers and user drawn markers
    private Map<String, AbstractMarker> markers = new HashMap<>();
    private final List<AbstractMarker> userMarkers = new ArrayList<>();

    // Set of the types of markers currently showing on screen
    private final Set<AbstractMarker.MarkerType> markersShowing = new HashSet<>();

    // Set of overlays that are active at all times
    private final Map<String, AbstractOverlay> overlayMap = new HashMap<>();

    private final List<Integer> selectedNodeList = new ArrayList<>();

    // The group that contains all the country graphics
    private final Group countryGroup;

    // The two groups that hold the queried and user marker groups
    private final Group graphMarkerGroup;
    private final Group drawnMarkerGroup;

    // Groups for user drawn polygons, cluster markers and "hidden" point markers used for cluster calculations
    private final Group polygonMarkerGroup;
    private final Group clusterMarkerGroup;
    private final Group hiddenPointMarkerGroup;

    public static double MAP_ZOOM_LEVEL = 0;

    // Groups for the ovelays and layers
    private final Group overlayGroup;
    private final Group layerGroup;

    // Attribute and Identifier text groups for markers
    private final Group pointMarkerTextGroup;
    private final List<Pair<AbstractMarker, Text>> markerTextLabels = new ArrayList<>();

    // Group for thessian polygons
    private final Group thessianMarkersGroup;

    // Group for the rectangle the user drags to select multiple markers
    private final Group selectionRectangleGroup;
    private final Group viewPortRectangleGroup;

    // Flag for zoom to location menu
    private boolean showingZoomToLocationPane = false;

    private static final Logger LOGGER = Logger.getLogger(MapView.class.getName());

    Vec3 medianPositionOfMarkers = null;

    // Factor to scale map by when zooming
    private static final double MAP_SCALE_FACTOR = 1.1;

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

    // Flag for the line the user draws to smell distance between
    private boolean drawingMeasureLine = false;
    private Line measureLine = null;

    // Panning variables
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    // Pane that hold all the groups for all the different graphical outputs
    private final Pane mapGroupHolder = new Pane();

    private final Rectangle clipRectangle = new Rectangle();

    // All the layers are stored here
    private final List<AbstractMapLayer> layers = new ArrayList<>();

    public static final ToolsOverlay TOOLS_OVERLAY = new ToolsOverlay(0, 0);
    private static final InfoOverlay INFO_OVERLAY = new InfoOverlay();
    private OverviewOverlay overviewOverlay;

    private static final String COORDINATE = "Coordinate";
    private static final String GEOHASH = "Geohash";
    private static final String MGRS = "MGRS";

    private final MapView self = this;

    private ClusterMarkerBuilder clusterMarkerBuilder = null;

    // Flags for what attribute marker colour and text should be derived from
    private final StringProperty markerColourProperty = new SimpleStringProperty();
    private final StringProperty markerTextProperty = new SimpleStringProperty();

    private final Rectangle enclosingRectangle = new Rectangle();

    public MapView(final MapViewPane parent) {
        this.parent = parent;

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
        viewPortRectangleGroup = new Group();
        final Group enclosingRectangleGroup = new Group();

        // By default all markers should be showing
        markersShowing.add(AbstractMarker.MarkerType.LINE_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POINT_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POLYGON_MARKER);

        mapGroupHolder.setBackground(new Background(new BackgroundFill(new Color(0.722, 0.871, 0.902, 1), null, null)));

        // Clear any existing paths and read the SVG paths of countries from the files
        countrySVGPaths.clear();
        parseMapSVG();

        // The stackPane to store
        mapStackPane = new StackPane();
        mapStackPane.setBorder(Border.EMPTY);
        mapStackPane.setBackground(new Background(new BackgroundFill(Color.BROWN, null, null)));


        mapStackPane.getChildren().addAll(mapGroupHolder);
        mapStackPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        clipRectangle.setWidth(parent.getWidth());
        clipRectangle.setHeight(parent.getHeight());
        clipRectangle.setX(0);
        clipRectangle.setY(0);
        clipRectangle.setFill(Color.TRANSPARENT);

        setScrollEventHandler();

        setMousePressedEventHandler();

        // When user drags the mouse
        mapStackPane.setOnMouseDragged(event -> {
            // Check they are dragging the right mouse button
            if (event.isSecondaryButtonDown()) {
                // Move the map
                mapStackPane.setTranslateX(transalateX + (event.getSceneX() - mouseAnchorX));
                mapStackPane.setTranslateY(transalateY + (event.getSceneY() - mouseAnchorY));
                enclosingRectangle.setTranslateX(enclosingRectangle.getTranslateX() - (event.getSceneX() - mouseAnchorX));
                enclosingRectangle.setTranslateY(enclosingRectangle.getTranslateY() - (event.getSceneY() - mouseAnchorY));
                updateOverviewOverlay();
                event.consume();
            }
        });

        // Clear any country grpahics that already exist within group
        countryGroup.getChildren().clear();

        // Add paths to group to display them on screen
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }
        overviewOverlay = new OverviewOverlay(0, 0, countrySVGPaths);
        enclosingRectangle.setWidth(MAP_VIEWPORT_WIDTH);
        enclosingRectangle.setHeight(MAP_VIEWPORT_HEIGHT);

        enclosingRectangle.setStroke(Color.RED);
        enclosingRectangle.setStrokeWidth(8);
        enclosingRectangle.setFill(Color.TRANSPARENT);
        enclosingRectangle.setMouseTransparent(true);
        enclosingRectangleGroup.getChildren().add(enclosingRectangle);

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
        mapGroupHolder.setPrefWidth(MAP_WIDTH);
        mapGroupHolder.setPrefHeight(MAP_HEIGHT);
        mapGroupHolder.getChildren().add(countryGroup);

        // Put overlays in map
        overlayMap.put(MapViewPane.TOOLS_OVERLAY, TOOLS_OVERLAY);
        overlayMap.put(MapViewPane.INFO_OVERLAY, INFO_OVERLAY);
        overlayMap.put(MapViewPane.OVERVIEW_OVERLAY, overviewOverlay);

        markerColourProperty.set(parent.DEFAULT_COLOURS);
        // Event listener for what colour point markers should be
        markerColourProperty.addListener((observable, oldValue, newValue) -> {
            // Loop through all markers on screen
            for (final AbstractMarker value : markers.values()) {
                value.changeMarkerColour(newValue);
            }
        });

        // Handler for what type of text to display under the markers
        markerTextProperty.addListener((observable, oldValue, newValue) -> {
            renderMarkerText(newValue);
        });

        setMouseClickedEventHandler();

        // If the mouse is moved
        mapGroupHolder.setOnMouseMoved(event -> {
            final double x = event.getX();
            final double y = event.getY();

            // Change lattitude and logitude text on info overlay if its showing
            if (INFO_OVERLAY.isShowing()) {
                INFO_OVERLAY.updateLocation(x, y);
                parent.setLatFieldText(INFO_OVERLAY.getLatText().getText());
                parent.setLonFieldText(INFO_OVERLAY.getLonText().getText());
            }

            // If drawing is enabled
            if (TOOLS_OVERLAY.getDrawingEnabled().get() && !TOOLS_OVERLAY.getMeasureEnabled().get()) {

                // if drawing circle marker then change the circle marker UI's radius to the distance between the original click position and the mouse's
                // current position
                if (drawingCircleMarker && circleMarker != null && !drawingPolygonMarker) {
                    circleMarker.setRadius(x, y);
                    circleMarker.setLineEnd(x, y);

                    // Change distance text on tools overlay
                    TOOLS_OVERLAY.setDistanceText(circleMarker.getCenterX(), circleMarker.getCenterY(), x, y);

                    // If the user is drawing a polygon marker then update the polygon drawing UI
                } else if (drawingPolygonMarker && polygonMarker != null && !drawingCircleMarker) {
                    polygonMarker.setEnd(x, y);

                    // Update distance text
                    TOOLS_OVERLAY.setDistanceText(polygonMarker.getCurrentLine().getStartX(), polygonMarker.getCurrentLine().getStartY(), polygonMarker.getCurrentLine().getEndX(), polygonMarker.getCurrentLine().getEndY());
                }

                // If the user isn't drawing anything but IS measuring distance then update the size of the measurement line
            } else if (TOOLS_OVERLAY.getMeasureEnabled().get() && !TOOLS_OVERLAY.getDrawingEnabled().get() && measureLine != null) {
                measureLine.setEndX(event.getX());
                measureLine.setEndY(event.getY());

                TOOLS_OVERLAY.setDistanceText(measureLine.getStartX(), measureLine.getStartY(), measureLine.getEndX(), measureLine.getEndY());
            }

            event.consume();
        });


        // When mouse is dragged
        mapGroupHolder.setOnMouseDragged(event -> {
            // If the user is drawing a selection rectangle
            if (event.isPrimaryButtonDown()) {
                isSelectingMultiple = true;
                final double x = event.getX();
                final double y = event.getY();

                final double width;
                final double height;

                // Change position and size of rectangle based on where the user moves their mouse
                if (x >= selectionRectangleX && y <= selectionRectangleY) {
                    width = x - selectionRectangleX;
                    height = selectionRectangleY - y;
                    selectionRectangle.setY(y);
                } else if (x < selectionRectangleX && y < selectionRectangleY) {
                    width = selectionRectangleX - x;
                    height = selectionRectangleY - y;
                    selectionRectangle.setX(x);
                    selectionRectangle.setY(y);
                } else if (x >= selectionRectangleX) {
                    width = x - selectionRectangleX;
                    height = y - selectionRectangleY;

                } else {
                    width = selectionRectangleX - x;
                    height = y - selectionRectangleY;

                    selectionRectangle.setX(x);
                }

                selectionRectangle.setWidth(width);
                selectionRectangle.setHeight(height);
            }
        });

        // When mouse is released
        mapGroupHolder.setOnMouseReleased(event -> {
            // If the user is selecting multiple markers
            if (isSelectingMultiple) {
                LOGGER.log(Level.SEVERE, "Selecting multiple");
                selectedNodeList.clear();

                final List<Integer> idList = new ArrayList<>();

                // Loop through all the markers
                for (final AbstractMarker m : markers.values()) {
                    if (m instanceof PointMarker) {
                        final PointMarker p = (PointMarker) m;
                        p.deselect();

                        // If marker is within the selection rectangle then select the marker
                        if (selectionRectangle.contains(p.getX(), p.getY())) {
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

        overlayGroup.getChildren().addAll(INFO_OVERLAY.getOverlayPane());
        mapGroupHolder.getChildren().add(selectionRectangleGroup);
        mapGroupHolder.getChildren().addAll(viewPortRectangleGroup);

    }

    /**
     * Mouse pressed events for stack pane and the mapGroupHolder
     */
    private void setMousePressedEventHandler() {

        // Panning code
        // Record where the mouse has been pressed
        mapStackPane.setOnMousePressed(event -> {
            // Coordinate within scene
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            // Position of map when moise is clicked
            transalateX = mapStackPane.getTranslateX();
            transalateY = mapStackPane.getTranslateY();

            final Rectangle viewPortRect = parent.getViewPortRectangle();

            clipRectangle.setX(viewPortRect.getX());
            clipRectangle.setY(viewPortRect.getY());
            clipRectangle.setWidth(viewPortRect.getWidth());
            clipRectangle.setHeight(viewPortRect.getHeight());

        });

        // When mouse is pressed instantiate multi-select rectangle
        mapGroupHolder.setOnMousePressed(event -> {
            // create the selection rectangle
            if (event.isPrimaryButtonDown()) {
                selectionRectangle = new Rectangle();
                selectionRectangle.setX(event.getX());
                selectionRectangle.setY(event.getY());
                selectionRectangleX = event.getX();
                selectionRectangleY = event.getY();

                selectionRectangle.setFill(Color.GRAY);
                selectionRectangle.setOpacity(0.2);
                selectionRectangleGroup.getChildren().add(selectionRectangle);
            }
        });
    }

    private void updateOverviewOverlay() {
        final Rectangle borderRect = parent.getBorderRectangle();

        final Point2D mapPos = mapStackPane.getParent().getParent().localToParent(mapStackPane.getTranslateX(), mapStackPane.getTranslateY());
        final Point2D viewPortPos = new Point2D(borderRect.getX(), borderRect.getY());
        final Vec3 mapTopRight = new Vec3(mapPos.getX() + mapStackPane.getWidth(), mapPos.getY());
        final Vec3 topRightViewPort = new Vec3(borderRect.getX() + borderRect.getWidth(), borderRect.getY());

        final Point2D screenViewPortTopLeft = borderRect.localToScreen(borderRect.getX(), borderRect.getY());
        final Point2D screenViewPortTopRight = borderRect.localToScreen(borderRect.getX() + borderRect.getWidth(), borderRect.getY());

        final Vec3 topLeftVect = new Vec3((viewPortPos.getX() - mapPos.getX()), viewPortPos.getY() - mapPos.getY());
        final Vec3 topRightVect = new Vec3(topRightViewPort.getX() - mapTopRight.getX(), topRightViewPort.getY() - mapTopRight.getY());

        final Vec3 newTopLeft = new Vec3(clipRectangle.getX() + topLeftVect.getX(), clipRectangle.getY() + topLeftVect.getY());
        final Vec3 newTopRight = new Vec3((clipRectangle.getX() + clipRectangle.getWidth()) + topRightVect.getX(), clipRectangle.getY() + topRightVect.getY());

        final double width = screenViewPortTopRight.getX() - screenViewPortTopLeft.getX();

        overviewOverlay.update(topLeftVect, parent.getBorderRectangle().getWidth());
    }

    /**
     * Event handler for when mouse is clicked on the map
     */
    private void setMouseClickedEventHandler() {

        mapGroupHolder.setOnMouseClicked(event -> {
            // If left clicked
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                // if double clicked desekect all marekers from both the map and the graph in consty
                deselectAllMarkers();
                selectedNodeList.clear();
                PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodeList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                event.consume();
                return;

            }

            // Record location of mouse click
            final double x = event.getX();
            final double y = event.getY();

            // If drawing is enabled and measurment is disabled
            if (TOOLS_OVERLAY.getDrawingEnabled().get() && !TOOLS_OVERLAY.getMeasureEnabled().get()) {

                // If shift is down then display circle drawing
                if (event.isShiftDown()) {
                    drawingCircleMarker = true;
                    circleMarker = new CircleMarker(self, drawnMarkerId++, x, y, 0, 100, 100);
                    polygonMarkerGroup.getChildren().add(circleMarker.getUICircle());
                    polygonMarkerGroup.getChildren().add(circleMarker.getUILine());

                    // If user is drawing a circle then add it to the map and clear any UI elements
                } else if (drawingCircleMarker) {
                    circleMarker.generateCircle();
                    drawnMarkerGroup.getChildren().add(circleMarker.getMarker());
                    userMarkers.add(circleMarker);
                    circleMarker = null;
                    polygonMarkerGroup.getChildren().clear();
                    drawingCircleMarker = false;

                    // Reset the distance shown on the tools ovelay
                    TOOLS_OVERLAY.resetMeasureText();

                    // If control is down
                } else if (event.isControlDown()) {
                    // if user is not current;y drawing a polygon marker or a circle marker then show UI to draw a polygon on screen
                    if (!drawingPolygonMarker) {
                        polygonMarker = new PolygonMarker(self, drawnMarkerId++, 0, 0);

                        drawingPolygonMarker = true;
                    }
                    polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));

                    TOOLS_OVERLAY.resetMeasureText();

                    // If the user is drawing a polygon marker then add the polygon to the screen and clear any UI elements
                } else if (drawingPolygonMarker) {
                    drawingPolygonMarker = false;
                    polygonMarker.generatePath();
                    addUserDrawnMarker(polygonMarker);
                    TOOLS_OVERLAY.resetMeasureText();
                    userMarkers.add(polygonMarker);
                    polygonMarker.endDrawing();
                    polygonMarkerGroup.getChildren().clear();

                    // If the user is not drawing any type of marker then generate point marker where they clicked
                } else {
                    final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, 95, -95);
                    LOGGER.log(Level.SEVERE, "User point marker x: " + x + " User point marker y: " + y);
                    marker.setMarkerPosition(0, 0);
                    addUserDrawnMarker(marker);
                    userMarkers.add(marker);
                    updateClusterMarkers();
                }

                // If drawing is not enabled but measuring is
            } else if (!TOOLS_OVERLAY.getDrawingEnabled().get() && TOOLS_OVERLAY.getMeasureEnabled().get()) {
                if (!drawingMeasureLine) {
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
                    polygonMarkerGroup.getChildren().clear();
                    TOOLS_OVERLAY.resetMeasureText();
                    drawingMeasureLine = false;
                    measureLine = null;
                }

            }
            event.consume();
        });
    }

    private void renderMarkerText(final String option) {
        // Clear any existing text
        pointMarkerTextGroup.getChildren().clear();
        markerTextLabels.clear();
        // Loop through all the markers
        for (final AbstractMarker value : markers.values()) {
            // If its a point marker change its text
            if (value instanceof PointMarker) {
                final PointMarker p = (PointMarker) value;
                if (option.equals(MapViewPane.USE_LABEL_ATTR)) {
                    setPointMarkerText(p.getLabelAttr(), p);
                } else if (option.equals(MapViewPane.USE_IDENT_ATTR)) {
                    setPointMarkerText(p.getIdentAttr(), p);
                }
            } else if (value instanceof GeoShapePolygonMarker) {
                final GeoShapePolygonMarker gsp = (GeoShapePolygonMarker) value;

                if (option.equals(MapViewPane.USE_LABEL_ATTR)) {
                    gsp.getGeoShapes().values().forEach(shapePair -> setPolygonMarkerText(shapePair.getKey().getLabelAttr(), shapePair.getKey()));
                } else if (option.equals(MapViewPane.USE_IDENT_ATTR)) {
                    gsp.getGeoShapes().values().forEach(shapePair -> setPolygonMarkerText(shapePair.getKey().getIdentAttr(), shapePair.getKey()));
                }
            }

        }
    }

    /**
     * Set scroll events
     */
    private void setScrollEventHandler() {
        // Scoll to zoom
        mapStackPane.setOnScroll(e -> {
            e.consume();

            if (e.getDeltaY() == 0) {
                return;
            }

            final Rectangle viewPortRect = parent.getViewPortRectangle();

            clipRectangle.setX(viewPortRect.getX());
            clipRectangle.setY(viewPortRect.getY());
            clipRectangle.setWidth(viewPortRect.getWidth());
            clipRectangle.setHeight(viewPortRect.getHeight());

            // Scroll factor is more or less than 1 depending on which way the scroll wheele is scrolled
            final double scaleFactor = (e.getDeltaY() > 0) ? MAP_SCALE_FACTOR : 1 / MAP_SCALE_FACTOR;

            // Get the current scale of the map view
            final double oldXScale = mapStackPane.getScaleX();
            final double oldYScale = mapStackPane.getScaleY();

            // Change the scale based on which way the scroll wheel is turned
            final double newXScale = oldXScale * scaleFactor;
            final double newYScale = oldYScale * scaleFactor;

            // Calculate how much the map will have to move
            final double xAdjust = (newXScale / oldXScale) - 1;
            final double yAdjust = (newYScale / oldYScale) - 1;
            final double markerScalingRate = 1.1;
            final double labelYOffset = 11;
            if (scaleFactor > 1.0) {
                pointMarkerGlobalScale /= 1.08;
                scaleMarkerText(markerScalingRate, true);
                resizeMarkers(true);
            } else {
                pointMarkerGlobalScale *= 1.08;
                scaleMarkerText(markerScalingRate, false);
                resizeMarkers(false);
            }

            // Calculate how much the map will have to move
            final double moveX = e.getSceneX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
            final double moveY = e.getSceneY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

            // Move the map
            mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
            mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);

            // Scale the map
            mapStackPane.setScaleX(newXScale);
            mapStackPane.setScaleY(newYScale);

            updateOverviewOverlay();

            // If cluster markers are showing then update them based on distance between markers
            if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
                updateClusterMarkers();
            }
        });
    }

    /**
     * Scale the labels below the markers
     *
     * @param markerScalingRate - rate to scale the labels at
     * @param zoomOut - Whether to zoom out
     */
    private void scaleMarkerText(final double markerScalingRate, final boolean zoomOut) {
        markerTextLabels.forEach(textLabel -> {
            if (zoomOut) {
                textLabel.getValue().setScaleX(textLabel.getValue().getScaleX() / markerScalingRate);
                textLabel.getValue().setScaleY(textLabel.getValue().getScaleY() / markerScalingRate);
            } else {
                textLabel.getValue().setScaleX(textLabel.getValue().getScaleX() * markerScalingRate);
                textLabel.getValue().setScaleY(textLabel.getValue().getScaleY() * markerScalingRate);
            }

            double posX = textLabel.getValue().getBoundsInParent().getCenterX();
            double posY = textLabel.getValue().getBoundsInParent().getCenterY();

            if (textLabel.getKey() instanceof PointMarker) {
                textLabel.getValue().setTranslateY(textLabel.getValue().getTranslateY() + (((textLabel.getKey().getMarker().getBoundsInParent().getCenterY() + textLabel.getKey().getMarker().getBoundsInParent().getHeight() / 2) - posY)));
                textLabel.getValue().setTranslateX(textLabel.getValue().getTranslateX() + (((textLabel.getKey().getMarker().getBoundsInParent().getCenterX()) - posX)));
            }

        });
    }

    /**
     * Sets text on the map at a specific location
     *
     * @param markerText - Text to appear below the marker
     * @param p - The marker itself
     */
    private void setPointMarkerText(final String markerText, final PointMarker p) {
        final Text t = new Text(markerText);
        t.setX(p.getMarker().getBoundsInParent().getCenterX() - 20);
        t.setY(p.getMarker().getBoundsInParent().getCenterY() + 20);
        markerTextLabels.add(new Pair(p, t));
        pointMarkerTextGroup.getChildren().add(t);
    }

    private void setPolygonMarkerText(final String markerText, final GeoShape gs) {
        final Text t = new Text(markerText);
        t.setX(gs.getCenterX() - 37);
        t.setY(gs.getCenterY());
        final UserPointMarker tempMarker = new UserPointMarker(this, -99, gs.getCenterX(), gs.getCenterY(), 0.05, 99, 99);
        markerTextLabels.add(new Pair(tempMarker, t));
        pointMarkerTextGroup.getChildren().add(t);
    }

    /**
     * Deselects all markers
     */
    public void deselectAllMarkers() {
        for (final AbstractMarker value : markers.values()) {
            value.deselect();
        }
    }

    public StringProperty getMarkerColourProperty() {
        return markerColourProperty;
    }

    /**
     * Add user drawn marker on screen
     *
     * @param marker - marker to add
     */
    private void addUserDrawnMarker(final AbstractMarker marker) {
        // Only draw marker is it's type is set to be showing on screen
        if (markersShowing.contains(marker.getType())) {

            if (marker instanceof ClusterMarker) {
                clusterMarkerGroup.getChildren().add(marker.getMarker());
                return;
            }

            if (marker instanceof UserPointMarker) {
                final UserPointMarker uMarker = (UserPointMarker) marker;
                if (uMarker.getScale() != pointMarkerGlobalScale) {
                    uMarker.scaleAndReposition(pointMarkerGlobalScale);
                }
            }

            drawnMarkerGroup.getChildren().addAll(marker.getMarker());
        }
    }

    /**
     * Remove a user drawn marker
     *
     * @param id - id of marker to remove
     */
    public void removeUserMarker(final int id) {
        // Loop through all the user drawn markers and if their id matches with the one passed in
        // to this function then remove them
        for (int i = 0; i < userMarkers.size(); i++) {
            if (userMarkers.get(i).getMarkerId() == id) {
                userMarkers.remove(i);
                break;
            }
        }

        // Redraw the user markers now that one has been removed
        redrawUserMarkers();
        updateClusterMarkers();
    }

    /**
     * Hide/Show different map overlays
     *
     * @param overlay - overlay to hide/show
     * @param show - flag
     */
    public void toggleOverlay(final String overlay, final boolean show) {
        if (overlayMap.containsKey(overlay) && overlayMap.get(overlay).isShowing() != show) {
            overlayMap.get(overlay).toggleOverlay();
        }
    }

    public int getNewMarkerID() {
        return parent.getNewMarkerID();
    }

    // Add a cluster marker
    public void addClusterMarkers(final List<ClusterMarker> clusters, final List<Text> clusterValues) {
        // If cluster marker is showing
        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER)) {
            clusterMarkerGroup.getChildren().clear();

            // Draw each cluster marker on map
            clusters.forEach(cluster -> addUserDrawnMarker(cluster));

            // Add how many markers are in one cluster
            clusterValues.forEach(numNodes -> clusterMarkerGroup.getChildren().add(numNodes));
        }
    }

    /**
     * Clears and re-renders all user created markers on screen
     */
    private void redrawUserMarkers() {
        drawnMarkerGroup.getChildren().clear();

        // Redraw all user created markers
        userMarkers.forEach(marker -> addUserDrawnMarker(marker));

        // Cluster markers are showing as well as point markers then update the clister markers
        if (markersShowing.contains(AbstractMarker.MarkerType.CLUSTER_MARKER) && markersShowing.contains(AbstractMarker.MarkerType.POINT_MARKER)) {
            updateClusterMarkers();
        } else {
            clusterMarkerGroup.getChildren().clear();
        }
    }

    /**
     * Reduces number od cluster makers or increases the size of existing ones
     * based on distance between marker
     */
    private void updateClusterMarkers() {
        hiddenPointMarkerGroup.getChildren().clear();

        // Add point markers to the hiddenPointMarker groups
        for (final AbstractMarker value : markers.values()) {

            if (value instanceof PointMarker) {
                final SVGPath p = new SVGPath();
                p.setContent(((PointMarker) value).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        }

        // Add user created markers to the hiddenPointMarker group
        userMarkers.forEach(m -> {
            if (m instanceof UserPointMarker) {
                final SVGPath p = new SVGPath();
                p.setContent(((UserPointMarker) m).getPath());
                hiddenPointMarkerGroup.getChildren().add(p);
            }
        });

        // Calculate the clusters
        clusterMarkerBuilder.update(hiddenPointMarkerGroup);

        // Add cluster markers to map
        addClusterMarkers(clusterMarkerBuilder.getClusterMarkers(), clusterMarkerBuilder.getClusterValues());
    }

    /**
     * Change size of markers based on whether user has zoomed in or out
     *
     * @param zoomIn - check for zoom in/out
     */
    private void resizeMarkers(final boolean zoomIn) {
        markers.values().forEach(abstractMarker -> {
            if (abstractMarker instanceof PointMarker) {
                final PointMarker marker = (PointMarker) abstractMarker;
                if (zoomIn) {
                    marker.scaleAndReposition(marker.getScale() / 1.08);
                } else {
                    marker.scaleAndReposition(marker.getScale() * 1.08);
                }
            }
        });

        userMarkers.forEach(abstractMarker -> {
            if (abstractMarker instanceof UserPointMarker) {
                final UserPointMarker marker = (UserPointMarker) abstractMarker;
                if (zoomIn) {
                    marker.scaleAndReposition(marker.getScale() / 1.08);
                } else {
                    marker.scaleAndReposition(marker.getScale() * 1.08);
                }
            }
        });
    }

    // Sets up and adds a layer to the map
    public void addLayer(final AbstractMapLayer layer) {
        layer.setUp();
        layers.add(layer);
        renderLayers();
        layer.setIsShowing(true);
    }

    public void removeLayer(final int id) {
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).getId() == id) {
                layers.remove(i);
            }
        }
        renderLayers();
    }

    /**
     * Render layers on the map
     */
    private void renderLayers() {
        layerGroup.getChildren().clear();
        layers.forEach(layer -> layerGroup.getChildren().add(layer.getLayer()));
    }

    /**
     * Hide/Shows a type of marker on the screen
     *
     * @param type - the type of marker to show/hide
     * @param adding - flag
     */
    public void updateShowingMarkers(final AbstractMarker.MarkerType type, final boolean adding) {
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

    /**
     * Redraw all markers that have been extracted from the graph
     */
    public void redrawQueriedMarkers() {
        graphMarkerGroup.getChildren().clear();

        if (markers.isEmpty()) {
            LOGGER.log(Level.INFO, "Marker map is empty");
        }

        // Redraw all queried markers
        for (final AbstractMarker value : markers.values()) {
            drawMarker(value);
        }
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return markers;
    }

    /**
     * Put user drawn markers and graph markers in a list
     *
     * @return an array list of all markers on the map
     */
    public List<AbstractMarker> getAllMarkersAsList() {
        final List<AbstractMarker> allMarkers = new ArrayList<>();

        for (final AbstractMarker marker : markers.values()) {
            allMarkers.add(marker);
        }

        for (final AbstractMarker marker : userMarkers) {
            allMarkers.add(marker);
        }

        return allMarkers;
    }

    public void clearQueriedMarkers() {
        markers.clear();
        selectedNodeList.clear();
    }

    /**
     * Selects one or nodes on the graph
     *
     * @param markerID - ID of selected marker
     * @param selectedNodes - Node id's that are represented by the
     * @param selectingVertex - Flag for vertex or transaction selection
     */
    public void addMarkerIdToSelectedList(final int markerID, final List<Integer> selectedNodes, final boolean selectingVertex) {
        selectedNodeList.add(markerID);
        PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodes, selectingVertex)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Pans to all markers
     */
    public void panToAll() {

        int markerCounter = 0;
        double averageX = 0;
        double averageY = 0;

        // Add the positions of all queried markers
        for (final AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker) {
                averageX += m.getX();
                averageY += m.getY();
                ++markerCounter;
            }
        }

        // Add the positions of all user markers
        for (final AbstractMarker m : userMarkers) {
            if (m instanceof UserPointMarker) {
                averageX += m.getX();
                averageY += m.getY();
                ++markerCounter;
            }
        }

        if (markerCounter == 0) {
            return;
        }

        // find average position
        averageX /= markerCounter;
        averageY /= markerCounter;

        // Pan to location
        pan(averageX, averageY);

        // Zoom to average location
        zoom(averageX, averageY, true);

    }

    /**
     * Pan to the center of the component
     */
    public void panToCenter() {
        final double centerX = this.getWidth() / 2;
        final double centerY = this.getHeight() / 2;


        mapStackPane.setTranslateX(centerX - MAP_WIDTH / 2);
        mapStackPane.setTranslateY(centerY - MAP_HEIGHT / 2);

    }

    /**
     * Pan to specific location
     *
     * @param x - coordinate
     * @param y - coordinate
     */
    private void pan(final double x, final double y) {

        final Vec3 center = new Vec3(MAP_WIDTH / 2, MAP_HEIGHT / 2);

        final Point2D averageMarkerPosition = mapStackPane.localToParent(x, y);

        final double parentCenterX = mapStackPane.localToParent(center.getX(), center.getY()).getX();
        final double parentCenterY = mapStackPane.localToParent(center.getX(), center.getY()).getY();

        final Vec3 dirVect = new Vec3(parentCenterX - averageMarkerPosition.getX(), parentCenterY - averageMarkerPosition.getY());


        mapStackPane.setTranslateX(mapStackPane.getTranslateX() + dirVect.getX());
        mapStackPane.setTranslateY(mapStackPane.getTranslateY() + dirVect.getY());
    }

    /**
     * Pans the map to have the average coordinates of all selected nodes to be
     * at the the center of the screen
     */
    public void panToSelection() {
        int markerCounter = 0;
        double averageX = 0;
        double averageY = 0;

        // Add coordinates of the all selected markers
        for (final AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId())) {
                averageX += m.getX();
                averageY += m.getY();
                ++markerCounter;
            }
        }

        if (markerCounter == 0) {
            return;
        }

        // Average location of all selecter markers
        averageX /= markerCounter;
        averageY /= markerCounter;

        pan(averageX, averageY);
        zoom(averageX, averageY, false);
    }

    /**
     * Zoom on a certain point
     *
     * @param x
     * @param y
     * @param allMarkers - If all markers should be visible before zooming is
     * stopped
     */
    public void zoom(final double x, final double y, final boolean allMarkers) {
        double scaleFactor = 1.05;
        boolean zoomIn = true;

        // Keep zooming in
        while (true) {

            if (scaleFactor > 1.0) {
                pointMarkerGlobalScale /= 1.04;
            } else {
                pointMarkerGlobalScale *= 1.04;
            }

            final double oldXScale = mapStackPane.getScaleX();
            final double oldYScale = mapStackPane.getScaleY();

            final double newXScale = oldXScale * scaleFactor;
            final double newYScale = oldYScale * scaleFactor;

            final double xAdjust = (newXScale / oldXScale) - 1;
            final double yAdjust = (newYScale / oldYScale) - 1;

            final double moveX = mapStackPane.localToParent(x, y).getX() - (mapStackPane.getBoundsInParent().getWidth() / 2 + mapStackPane.getBoundsInParent().getMinX());
            final double moveY = mapStackPane.localToParent(x, y).getY() - (mapStackPane.getBoundsInParent().getHeight() / 2 + mapStackPane.getBoundsInParent().getMinY());

            mapStackPane.setTranslateX(mapStackPane.getTranslateX() - xAdjust * moveX);
            mapStackPane.setTranslateY(mapStackPane.getTranslateY() - yAdjust * moveY);

            mapStackPane.setScaleX(newXScale);
            mapStackPane.setScaleY(newYScale);
            redrawQueriedMarkers();

            // When required markers are no longer in view then zoom out untill they are and break
            if (!selectedMarkersInView(allMarkers) && zoomIn) {
                scaleFactor = 1 / 1.05;
                zoomIn = false;
            } else if (selectedMarkersInView(allMarkers) && !zoomIn) {
                break;
            }

        }
    }

    /**
     * Calculate if all or selected markers are in view
     *
     * @param allMarkers
     * @return whether or not markers are in view
     */
    private boolean selectedMarkersInView(final boolean allMarkers) {
        for (final AbstractMarker m : markers.values()) {
            // If All markers need to be shown OR if the current marker is selected and all markers do not need to be shown
            if ((m instanceof PointMarker && allMarkers) || (m instanceof PointMarker && selectedNodeList.contains(m.getMarkerId()) && !allMarkers)) {
                final double x = (m.getX());
                final double y = (m.getY());

                final Rectangle r = new Rectangle();
                r.setWidth(10);
                r.setHeight(17.5);
                r.setX(x);
                r.setY(y);

                return (parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y))
                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y))
                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y + r.getHeight()))
                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y + r.getHeight())));
            }
        }

        return true;
    }

    /**
     * Create the zoom to location UI
     */
    public void generateZoomLocationUI() {
        if (!showingZoomToLocationPane) {
            final Stage stage = new Stage();
            final double width = 650;
            final double height = 180;
            final String textFillStyle = "-fx-text-fill: #FFFFFF;";
            final String backgroundFill = "#111111";

            final BorderPane pane = new BorderPane();
            pane.prefHeight(height);
            pane.minWidth(width);
            pane.minHeight(height);
            pane.maxWidth(width);
            pane.maxHeight(height);

            pane.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#222222").getJavaFXColor(), null, null)));

            final GridPane topGridPane = new GridPane();
            pane.setCenter(topGridPane);

            stage.setTitle("Zoom to Location");

            final Text geoTypeLabel = new Text("   Geo Type");
            geoTypeLabel.setFill(Color.WHITE);

            final ComboBox<String> geoTypeMenu = new ComboBox<>(FXCollections.observableList(Arrays.asList(COORDINATE, GEOHASH, MGRS)));
            geoTypeMenu.getSelectionModel().selectFirst();

            final Text lattitudeLabel = new Text("Lattitude");
            lattitudeLabel.setFill(Color.WHITE);

            final Text longitudeLabel = new Text("Longitude");
            longitudeLabel.setFill(Color.WHITE);

            final Text radiusLabel = new Text("Radius");
            radiusLabel.setFill(Color.WHITE);

            final TextField lattitudeInput = new TextField();
            lattitudeInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
            lattitudeInput.setStyle(textFillStyle);

            lattitudeInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));

            final TextField longitudeInput = new TextField();
            longitudeInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));
            longitudeInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
            longitudeInput.setStyle(textFillStyle);

            final TextField radiusInput = new TextField();
            radiusInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));
            radiusInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
            radiusInput.setStyle(textFillStyle);

            final GridPane coordinateGridPane = new GridPane();
            coordinateGridPane.setHgap(10);
            coordinateGridPane.setPadding(new Insets(0, 0, 0, 10));

            coordinateGridPane.add(lattitudeLabel, 0, 0);
            coordinateGridPane.add(longitudeLabel, 1, 0);
            coordinateGridPane.add(radiusLabel, 2, 0);

            coordinateGridPane.add(lattitudeInput, 0, 1);
            coordinateGridPane.add(longitudeInput, 1, 1);
            coordinateGridPane.add(radiusInput, 2, 1);

            final Text geoHashLabel = new Text("Base-16 geohash value");
            final TextField geoHashInput = new TextField();
            geoHashInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));
            geoHashInput.setStyle(textFillStyle);

            final Text mgrsLabel = new Text("MGRS value");
            final TextField mgrsInput = new TextField();
            mgrsInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));
            mgrsInput.setStyle(textFillStyle);

            // Change coordinate UI based on geoType
            geoTypeMenu.setOnAction(event -> {

                coordinateGridPane.getChildren().clear();
                final String selectedItem = geoTypeMenu.getSelectionModel().getSelectedItem();

                if (selectedItem.equals(COORDINATE)) {
                    coordinateGridPane.add(lattitudeLabel, 0, 0);
                    coordinateGridPane.add(longitudeLabel, 1, 0);
                    coordinateGridPane.add(radiusLabel, 2, 0);

                    coordinateGridPane.add(lattitudeInput, 0, 1);
                    coordinateGridPane.add(longitudeInput, 1, 1);
                    coordinateGridPane.add(radiusInput, 2, 1);

                    pane.setMinWidth(width);
                    pane.setMaxWidth(width);
                    stage.setWidth(width);

                } else if (selectedItem.equals(GEOHASH)) {
                    geoHashLabel.setFill(Color.WHITE);
                    geoHashInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
                    coordinateGridPane.add(geoHashLabel, 0, 0);
                    coordinateGridPane.add(geoHashInput, 0, 1);
                    pane.setMinWidth(420);
                    pane.setMaxWidth(420);
                    stage.setWidth(420);
                } else if (selectedItem.equals(MGRS)) {
                    mgrsLabel.setFill(Color.WHITE);
                    mgrsInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
                    coordinateGridPane.add(mgrsLabel, 0, 0);
                    coordinateGridPane.add(mgrsInput, 0, 1);
                    pane.setMinWidth(420);
                    pane.setMaxWidth(420);
                    stage.setWidth(420);
                }


            });


            final Button okButton = new Button("OK");
            okButton.setTextFill(Color.BLACK);
            okButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#26ED49").getJavaFXColor(), null, null)));

            okButton.setOnMouseEntered(event -> {
                okButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#86ED26").getJavaFXColor(), null, null)));
                okButton.setTextFill(Color.BLACK);
            });
            okButton.setOnMouseExited(event -> {
                okButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#26ED49").getJavaFXColor(), null, null)));
                okButton.setTextFill(Color.BLACK);
            });

            final double zoomCircleMarkerXOffset = 0;
            final double zoomCircleMarkerYOffset = 0;

            final double zoomUserMarkerXOffset = 0;
            final double zoomUserMarkerYOffset = 0;

            final double mgrsZoomUserMarkerXOffset = 0;
            final double mgrsZoomUserMarkerYOffset = 0;

            okButton.setOnAction(event -> {
                String selectedGeoType = geoTypeMenu.getSelectionModel().getSelectedItem();

                // Convert coordinate input to x and y coordinates
                if (selectedGeoType.equals(COORDINATE)) {

                    double lattitude = -3000;
                    double longitude = -3000;
                    double radius = -3000;

                    final String lattitudeText = lattitudeInput.getText();
                    final String longitudeText = longitudeInput.getText();
                    final String radiusText = radiusInput.getText();

                    if (StringUtils.isNotBlank(lattitudeText) && NumberUtils.isParsable(lattitudeText.strip()) && StringUtils.isNotBlank(longitudeText) && NumberUtils.isParsable(longitudeText.strip())) {
                        lattitude = Double.parseDouble(lattitudeText.strip());
                        longitude = Double.parseDouble(longitudeText.strip());
                    } else {
                        showingZoomToLocationPane = false;
                        stage.close();
                        return;
                    }

                    final double x = MarkerUtilities.longToX(longitude, MIN_LONG, MAP_WIDTH, MAX_LONG - MIN_LONG);
                    final double y = MarkerUtilities.latToY(lattitude, MAP_WIDTH, MAP_HEIGHT);

                    if (StringUtils.isNotBlank(radiusText) && NumberUtils.isParsable(radiusText.strip())) {
                        radius = Double.parseDouble(radiusText.strip());

                        // draw circle at entered location
                        final CircleMarker zoomCircleMarker = new CircleMarker(self, drawnMarkerId++, x, y, radius, zoomCircleMarkerXOffset, zoomCircleMarkerYOffset);

                        zoomCircleMarker.generateCircle();
                        drawnMarkerGroup.getChildren().add(zoomCircleMarker.getMarker());
                        userMarkers.add(zoomCircleMarker);

                        // If no radius is provided then draw point marker at location
                    } else {
                        final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, zoomUserMarkerXOffset, zoomUserMarkerYOffset);
                        marker.setMarkerPosition(0, 0);

                        addUserDrawnMarker(marker);

                        userMarkers.add(marker);
                    }

                    // Convert mgrs value to x and y
                } else if (selectedGeoType.equals(MGRS) && StringUtils.isNotBlank(mgrsInput.getText())) {

                    final MGRSCoord coordinate = MGRSCoord.fromString(mgrsInput.getText().strip(), null);
                    double x = MarkerUtilities.longToX(coordinate.getLongitude().degrees, MIN_LONG, MAP_WIDTH, MAX_LONG - MIN_LONG);
                    double y = MarkerUtilities.latToY(coordinate.getLatitude().degrees, MAP_WIDTH, MAP_HEIGHT);

                    UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, mgrsZoomUserMarkerXOffset, mgrsZoomUserMarkerYOffset);
                    marker.setMarkerPosition(0, 0);

                    addUserDrawnMarker(marker);

                    userMarkers.add(marker);

                    // Convert geohash value to x and y
                } else if (selectedGeoType.equals(GEOHASH) && StringUtils.isNotBlank(geoHashInput.getText())) {

                    final double[] geohashCoordinates = Geohash.decode(geoHashInput.getText().strip(), Geohash.Base.B32);
                    final double x = MarkerUtilities.longToX(geohashCoordinates[1] - geohashCoordinates[3], MIN_LONG, MAP_WIDTH, MAX_LONG - MIN_LONG);
                    final double y = MarkerUtilities.latToY(geohashCoordinates[0] - geohashCoordinates[2], MAP_WIDTH, MAP_HEIGHT);

                    final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05, zoomUserMarkerXOffset, zoomUserMarkerYOffset);
                    marker.setMarkerPosition(0, 0);

                    addUserDrawnMarker(marker);
                    userMarkers.add(marker);

                }

                showingZoomToLocationPane = false;
                event.consume();
                stage.close();
            });

            final Button cancelButton = new Button("Cancel");
            cancelButton.setTextFill(Color.BLACK);
            cancelButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#DEC20B").getJavaFXColor(), null, null)));

            cancelButton.setOnMouseEntered(event -> {
                cancelButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#DBA800").getJavaFXColor(), null, null)));
                cancelButton.setTextFill(Color.BLACK);
            });
            cancelButton.setOnMouseExited(event -> {
                cancelButton.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#DEC20B").getJavaFXColor(), null, null)));
                cancelButton.setTextFill(Color.BLACK);
            });

            cancelButton.setOnAction(event -> {
                showingZoomToLocationPane = false;
                event.consume();
                stage.close();
            });

            final HBox topHBox = new HBox(5, geoTypeLabel, geoTypeMenu);

            final GridPane bottomGridPane = new GridPane();

            topGridPane.add(topHBox, 0, 1);
            topGridPane.setHgap(10);
            topGridPane.setVgap(10);

            topGridPane.add(coordinateGridPane, 0, 2);

            bottomGridPane.add(okButton, 0, 0);
            bottomGridPane.add(cancelButton, 1, 0);
            bottomGridPane.setHgap(10);

            topGridPane.add(bottomGridPane, 3, 4);

            final Scene s = new Scene(pane);
            stage.setScene(s);
            stage.setWidth(width);
            stage.setHeight(height);

            if (!stage.isShowing()) {
                final List<Screen> screens = Screen.getScreensForRectangle(this.getScene().getWindow().getX(), this.getScene().getWindow().getY(), this.getScene().getWindow().widthProperty().get(), this.getScene().getWindow().heightProperty().get());

                stage.setX((screens.get(0).getVisualBounds().getMinX() + screens.get(0).getVisualBounds().getWidth() / 2) - width / 2);
                stage.setY((screens.get(0).getVisualBounds().getMinY() + screens.get(0).getVisualBounds().getHeight() / 2) - height / 2);

                stage.show();
                showingZoomToLocationPane = true;
            }
        }
    }

    public List<AbstractMarker> getUserCreatedMarkers() {
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

    /**
     * Draw a marker on the screen
     *
     * @param marker - marker to be added to the map
     */
    public void drawMarker(final AbstractMarker marker) {
        if (markersShowing.contains(marker.getType()) && ((markersShowing.contains(AbstractMarker.MarkerType.SELECTED) && marker.isSelected()) || !markersShowing.contains(AbstractMarker.MarkerType.SELECTED))) {
            marker.setMarkerPosition(MAP_WIDTH, MAP_HEIGHT);
            if (!graphMarkerGroup.getChildren().contains(marker.getMarker())) {
                if (marker instanceof GeoShapePolygonMarker) {
                    final GeoShapePolygonMarker gsp = (GeoShapePolygonMarker) marker;
                    gsp.getGeoShapes().values().forEach(shapePair -> {
                        if (!graphMarkerGroup.getChildren().contains(shapePair.getKey())) {
                            graphMarkerGroup.getChildren().add(shapePair.getKey());
                        }
                    });
                } else {
                    graphMarkerGroup.getChildren().add(marker.getMarker());
                }

                if (marker instanceof PointMarker) {
                    final PointMarker pMarker = (PointMarker) marker;
                    // The line below is to verify the positioning of the marker
                    //graphMarkerGroup.getChildren().add(pMarker.getPosRect());
                    if (pMarker.getScale() != pointMarkerGlobalScale) {
                        pMarker.scaleAndReposition(pointMarkerGlobalScale);
                    }
                }
            }
        }
    }

    public Group getGraphMarkerGroup() {
        return graphMarkerGroup;
    }

    public void testAddingGeoMarker(final Polygon p) {
        Platform.runLater(() -> graphMarkerGroup.getChildren().add(p));
    }

    public void addMarkerToHashMap(final String key, final AbstractMarker e) {
        markers.put(key, e);
    }

    public static InfoOverlay getInfoOverlay() {
        return INFO_OVERLAY;
    }

    public OverviewOverlay getOverviewOverlay() {
        return overviewOverlay;
    }


    public Set<AbstractMarker.MarkerType> getMarkersShowing() {
        return markersShowing;
    }

    /**
     * Load the world map
     */
    private void parseMapSVG() {
        countryGroup.getChildren().clear();

        // Read map from file
        try {

            final File map = ConstellationInstalledFileLocator.locate("modules/ext/data/MercratorMapView6.txt", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain());
            try (final BufferedReader bFileReader = new BufferedReader(new FileReader(map))) {
                String path = "";
                String line = "";

                // While there is more to read
                while ((line = bFileReader.readLine()) != null) {
                    // Strip the line read in
                    line = line.strip();

                    // Extract the svg path segment from the line
                    if (line.startsWith("<path")) {
                        final int startIndex = line.indexOf("d=");

                        final int endIndex = line.indexOf(">");

                        path = line.substring(startIndex + 3, endIndex - 2);

                        // Create the SVGPath object and add it to an array
                        final SVGPath svgPath = new SVGPath();
                        svgPath.setFill(Color.WHITE);
                        svgPath.setStrokeWidth(0.025);
                        svgPath.setStroke(Color.BLACK);
                        svgPath.setContent(path);

                        countrySVGPaths.add(svgPath);

                        path = "";
                    }
                }
            }

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Exception thrown: {0}", e.getMessage());
        }

    }



}

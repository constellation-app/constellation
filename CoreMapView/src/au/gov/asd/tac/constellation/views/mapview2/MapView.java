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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.geospatial.Geohash;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.LocationPathsLayer;
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
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import java.io.BufferedReader;
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
import javafx.geometry.Bounds;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * This class holds the actual MapView vector graphic and all panes associated
 * with it Its got all of the event handlers for drawing, labels, colours,
 * markers etc... It has all the groups that hold all other graphical elements
 * such as cluster markers, daynight layer and thiessen polygon layer
 *
 * @author serpens24
 * @author altair1673
 */
public class MapView extends ScrollPane {

    private static final Logger LOGGER = Logger.getLogger(MapView.class.getName());
    private static final double SCALED_MAP_LINE_WIDTH = 0.065;  // Scaled width of lines on maps (for SVG maps) to account

    private final MapViewPane parent;

    private final StackPane mapStackPane;
    private final Group scrollContent;

    // ID of the next user drawn marker
    private int drawnMarkerId = 0;

    private static double currentScale = 1.0;  // Scale of overall map display

    // Flags for the different drawing modes
    private boolean drawingCircleMarker = false;
    private boolean drawingPolygonMarker = false;

    public static final double EMPTY_BORDER_REGION = 250;

    // TODO: look to remove these constants and instead read lats, longs, and width/height from mapDetails object.
    // Furthest longitude to the east and west
    public static double MIN_LONG = 0;
    public static double MAX_LONG = 0;

    // Furthest latitude to the north and south
    public static double MIN_LAT = 0;
    public static double MAX_LAT = 0;

    // Size of the map
    public static double MAP_WIDTH = 0;
    public static double MAP_HEIGHT = 0;
    public static Insets MAP_OFFSETS = new Insets(0);
    
    private static MapDetails mapDetails = new MapDetails(MapDetails.MapType.SVG, 0, 0, 0, 0, 0, 0, new Insets(0), "", null);
     
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
    private final Group graphMarkerGroup;  // markers sourced from the graph content
    private final Group drawnMarkerGroup;  // markers drawn by user

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

    // Factor to scale map by when zooming
    private static final double MAP_SCALE_FACTOR = 1.25;

    // The paths for the edges of all the countries
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    // The UI overlys for the shapes the user can draw
    private CircleMarker circleMarker = null;
    private PolygonMarker polygonMarker = null;
    private Rectangle selectionRectangle = null;

    // Flag is the user is selecting multiple markers on screen
    private boolean isSelectingMultiple = false;
    private boolean isSelectionMade = false;
    private double selectionRectangleX = 0;
    private double selectionRectangleY = 0;

    // Flag for the line the user draws to smell distance between
    private boolean drawingMeasureLine = false;
    private Line measureLine = null;

    // Pane that hold all the groups for all the different graphical outputs
    private final Pane mapGroupHolder = new Pane();


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

    private final boolean testUsingCanvas = false; // Set this to true to test using the SVGCanvas 
    // or to use SVGImage, which requires the fxsvgImage.jar file to be included in the dependencies
    // *** NOTE ... this functionality is currently BUGGED ***
    // With SVGCanvas, the Netbean Swing interface becomes inoperative when trying to use a SwingNode inside a Javafx component
    // With SVGImage, the image bounds get changed, leading to complex adjustments to synchronise (x,y) co-ordinates 
    //   to exact map locations

    /**
     * Construct MapView object using the map identified by mspDetails.
     * 
     * @param parent The parent view pane.
     * @param mapDetails Details of he map being loaded.
     */
    public MapView(final MapViewPane parent, final MapDetails mapDetails) {
        this.parent = parent;
        this.mapDetails = mapDetails;
        
        LOGGER.setLevel(Level.ALL);
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

        // By default all markers should be showing
        markersShowing.add(AbstractMarker.MarkerType.LINE_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POINT_MARKER);
        markersShowing.add(AbstractMarker.MarkerType.POLYGON_MARKER);

        mapGroupHolder.setBackground(new Background(new BackgroundFill(new Color(0.622, 0.761, 0.782, 1), null, null)));

        // Clear any existing paths and read the SVG paths of countries from the files
        countrySVGPaths.clear();
        if (!testUsingCanvas) {
            parseMapSVG();
            countrySVGPaths.forEach(svgPath -> svgPath.setStrokeWidth(SCALED_MAP_LINE_WIDTH / Math.pow(currentScale, 0.85)));
        }
        
        // The stackPane to store
        mapStackPane = new StackPane();
        mapStackPane.setBorder(Border.EMPTY);
        mapStackPane.setBackground(new Background(new BackgroundFill(Color.BROWN, null, null)));

        mapStackPane.getChildren().addAll(mapGroupHolder);
        mapStackPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        setScrollEventHandler();
        setMousePressedEventHandler();

        // When user drags the mouse
        mapStackPane.setOnMouseDragged(event -> {
// TODO: commented out for now as this is being handled by mapView ScrollPane
//            // Check they are dragging the right mouse button
//            if (event.isSecondaryButtonDown()) {
//                // Move the map      
//                mapStackPane.setTranslateX(translateX + (event.getSceneX() - mouseAnchorX));
//                mapStackPane.setTranslateY(translateY + (event.getSceneY() - mouseAnchorY));
//                enclosingRectangle.setTranslateX(enclosingRectangle.getTranslateX() - (event.getSceneX() - mouseAnchorX));
//                enclosingRectangle.setTranslateY(enclosingRectangle.getTranslateY() - (event.getSceneY() - mouseAnchorY));
//                updateOverviewOverlay();
//                event.consume();
//            }
        });

        // Clear any country grpahics that already exist within group
        countryGroup.getChildren().clear();

        final int outerWidth = (int) (mapDetails.getWidth() + EMPTY_BORDER_REGION*2);
        final int outerHeight = (int) (mapDetails.getHeight() + EMPTY_BORDER_REGION*2);
        if (!testUsingCanvas) {
            // Add paths to group to display them on screen
            for (int i = 0; i < countrySVGPaths.size(); ++i) {
                countryGroup.getChildren().add(countrySVGPaths.get(i));
            }
            final SVGPath outerBorder = new SVGPath();
            outerBorder.setContent("M-%d -%d l %d 0 l 0 %d l -%d 0 l 0 -%d".formatted((int)EMPTY_BORDER_REGION, (int)EMPTY_BORDER_REGION, outerWidth, outerHeight, outerWidth, outerHeight));
            outerBorder.setStrokeWidth(SCALED_MAP_LINE_WIDTH);
            outerBorder.setStroke(Color.TRANSPARENT);
            outerBorder.setFill(Color.TRANSPARENT);
            countryGroup.getChildren().add(outerBorder);
        }

        // Setup mapView ScrollPane to be pannable and have scrollbars
        this.setPannable(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollContent = new Group(mapStackPane);  // wrap mapStackPane in a Group as Group dimensions are understood by 
                                                  // ScrollPane container.
        setContent(scrollContent);

        // TODO: hack for now, would like to remove these (and all references to them). Previously MapView stored all
        // dimensions as public constants and other classes pulled from there. Intention is to instead use MapDetails
        // object
        MIN_LONG = mapDetails.getLeftLon();
        MAX_LONG = mapDetails.getRightLon();
        MIN_LAT = mapDetails.getBottomLat();
        MAX_LAT = mapDetails.getTopLat();
        MAP_WIDTH = mapDetails.getWidth();
        MAP_HEIGHT = mapDetails.getHeight();
        MAP_OFFSETS = mapDetails.getBorderInsets();

        // Center content
        this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);

        // Add the country group the the the pane
        
        if (testUsingCanvas) {
            StackPane canvasPane = new StackPane();
            // code is COMMENTED OUT because the dependency is not included 
            // SVGImage is the class type in the fxsvgImage.jar third party package

            //SVGImage canvasImage = SVGLoader.load(mapDetails.getMapFile());
            //mapGroupHolder.getChildren().add(canvasImage);            
            //canvasImage.parentToLocal(parentBounds)

            canvasPane.setBorder(Border.EMPTY);
            //canvasPane.getChildren().addAll(canvasImage);
            
            setFitToWidth(true);
            setFitToHeight(true);
            canvasPane.setManaged(false);
            mapGroupHolder.getChildren().add(canvasPane);
            
            //SVGImage miniCanvas = SVGLoader.load(mapDetails.getMapFile()); // can't do a regular "copy" of the canvasImage, so using a new instance
            //miniCanvas.setScaleX(0.1);
            //miniCanvas.setScaleY(0.1);
            //overviewOverlay = new OverviewOverlay(mapDetails.getWidth(), mapDetails.getHeight(), null, miniCanvas);
        } else {
            mapGroupHolder.setPrefWidth(mapDetails.getWidth());
            mapGroupHolder.setPrefHeight(mapDetails.getHeight());
            mapGroupHolder.getChildren().add(countryGroup);
            Rectangle clipRectangle = new Rectangle(-EMPTY_BORDER_REGION, -EMPTY_BORDER_REGION, outerWidth, outerHeight);
            mapGroupHolder.setClip(clipRectangle);
            overviewOverlay = new OverviewOverlay(mapDetails.getWidth(), mapDetails.getHeight(), countrySVGPaths, null);
        }

        Bounds laybounds = scrollContent.getBoundsInLocal();
        double adjX = 0.0;
        double adjY = 0.0;
        if (laybounds.getMinX() < 0) {
            adjX = 0 - laybounds.getMinX();
        }
        if (laybounds.getMinY() < 0) {
            adjY = 0 - laybounds.getMinX();
        }
        scrollContent.setLayoutX(adjX);
        scrollContent.setLayoutY(adjY);
        
        MapConversions.setBorderOffsets(mapDetails.getBorderInsets());
        // Initialize the MapConversions class to align with the map being loaded.
        MapConversions.initMapDimensions(mapDetails);

        // Put overlays in map
        overlayMap.put(MapViewPane.TOOLS_OVERLAY, TOOLS_OVERLAY);
        overlayMap.put(MapViewPane.OVERVIEW_OVERLAY, overviewOverlay);

        markerColourProperty.set(parent.DEFAULT_COLOURS);

        // TODO this is triggered from Paintbrush menu, where user can select:
        //           - Default Colours
        //           - Use Color Attribute
        //           - Use Overlay Color
        //           - Use Blaze Color

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

            // Change latitude and longitude text on info overlay if its showing
            Platform.runLater(() -> INFO_OVERLAY.updateLocation(x, y));

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
            if (event.isPrimaryButtonDown() && selectionRectangle != null) {
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
                        if (selectionRectangle != null && selectionRectangle.contains(p.getX(), p.getY())) {
                            p.select();
                            idList.addAll(p.getConnectedNodeIdList());
                            selectedNodeList.add(p.getMarkerId());
                        }
                    }
                }

                // Select all nodes that correspond to selected markers in consty
                PluginExecution.withPlugin(new SelectOnGraphPlugin(idList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
                isSelectingMultiple = false;
                isSelectionMade = true;
            }
        });

        
        /**
         * Catch changes to mapView visible width and trigger overlay update.
         */
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.updateOverviewOverlay(); 
        });
        
        /**
         * Catch changes to mapView visible height and trigger overlay update.
         */
        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.updateOverviewOverlay(); 
        });
        
        /**
         * Catch changes to mapView visible horizontal scroll position and trigger overlay update.
         */
        this.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            this.updateOverviewOverlay(); 
            Platform.runLater(() -> {
                INFO_OVERLAY.updateZoomLabel(scaleSteps());
                INFO_OVERLAY.updateMapScaleText(scaledStepsToRepresent1000km());
            });
        });
        
        /**
         * Catch changes to mapView visible vertical scroll position and trigger overlay update.
         */
        this.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            this.updateOverviewOverlay(); 
            Platform.runLater(() -> {
                INFO_OVERLAY.updateZoomLabel(scaleSteps());
                INFO_OVERLAY.updateMapScaleText(scaledStepsToRepresent1000km());
            });
        });

        // Add all graphical groups to pane
        mapGroupHolder.getChildren().add(hiddenPointMarkerGroup);
        mapGroupHolder.getChildren().add(graphMarkerGroup);
        mapGroupHolder.getChildren().addAll(drawnMarkerGroup);
        mapGroupHolder.getChildren().add(clusterMarkerGroup);
        mapGroupHolder.getChildren().addAll(polygonMarkerGroup);
        mapGroupHolder.getChildren().add(overlayGroup);
        mapGroupHolder.getChildren().add(layerGroup);
        mapGroupHolder.getChildren().add(pointMarkerTextGroup);
        mapGroupHolder.getChildren().add(thessianMarkersGroup);

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
            
            // If a selection is already made, clear it, as this is resetting the selection
            if (isSelectionMade) {
                isSelectionMade = false;
                selectionRectangleGroup.getChildren().clear();
                selectionRectangle = null;
            }

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

    /**
     * Update the contents of the overview overlay screen.
     */
    public void updateOverviewOverlay() {
        
        if (this.getScene() == null) {
            return;
        }
        final double screenX = getScreenCentreX();
        final double screenY = getScreenCentreY();
        final double viewPortWidth = getViewportBounds().getWidth()/currentScale;
        final double viewPortHeight = getViewportBounds().getHeight()/currentScale;

        final double widthRange = MAP_WIDTH - viewPortWidth;
        double posHvalue = (screenX - viewPortWidth/2) / widthRange;
        if (posHvalue < 0) {
            posHvalue = 0;
        }
        if (posHvalue > 1) {
            posHvalue = 1;
        }
        final double targetHvalue = posHvalue;

        final double heightRange = MAP_HEIGHT - viewPortHeight;
        double posVvalue = 0;
        if (heightRange > 0) {
            posVvalue = (screenY - viewPortHeight/2) / heightRange;
            if (posVvalue < 0) {
                posVvalue = 0;
            }
            if (posVvalue > 1) {
                posVvalue = 1;
            }
        }
        final double targetVvalue = posVvalue;
        
        Platform.runLater(() -> overviewOverlay.update(viewPortWidth, viewPortHeight, targetHvalue, targetVvalue));
    }

    /**
     * Event handler for when mouse is clicked on the map
     */
    private void setMouseClickedEventHandler() {

        mapGroupHolder.setOnMouseClicked(event -> {
            // If left clicked
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                // if double clicked deselect all marekers from both the map and the graph in consty
                deselectAllMarkers();
                selectedNodeList.clear();
                PluginExecution.withPlugin(new SelectOnGraphPlugin(selectedNodeList, true)).executeLater(GraphManager.getDefault().getActiveGraph());
  //              event.consume();
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
                    circleMarker = new CircleMarker(self, drawnMarkerId++, x, y, 0);
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
                    // if user is not currently drawing a polygon marker or a circle marker then show UI to draw a polygon on screen
                    if (!drawingPolygonMarker) {
                        polygonMarker = new PolygonMarker(self, drawnMarkerId++);
                        drawingPolygonMarker = true;
                    }
                    polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));

                    TOOLS_OVERLAY.resetMeasureText();

                    // If the user is drawing a polygon marker then add the polygon to the screen and clear any UI elements
                } else if (drawingPolygonMarker) {
                    polygonMarkerGroup.getChildren().add(polygonMarker.addNewLine(x, y));
                    drawingPolygonMarker = false;
                    polygonMarker.generatePath();
                    addUserDrawnMarker(polygonMarker);
                    TOOLS_OVERLAY.resetMeasureText();
                    userMarkers.add(polygonMarker);
                    polygonMarker.endDrawing();
                    polygonMarkerGroup.getChildren().clear();

                    // If the user is not drawing any type of marker then generate point marker where they clicked
                } else if (event.getButton().equals(MouseButton.PRIMARY)) {
                    final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 1);
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
     //       event.consume();
        });
    }

    /**
     * Generate labels for markers based on the selected marker label content type option.
     * @param option String indicating the type of content to be displayed in marker labels.
     */
    private void renderMarkerText(final String option) {
        // Clear any existing text
        pointMarkerTextGroup.getChildren().clear();
        markerTextLabels.clear();
        if (option == null) {
            return;
        }

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

    private void resizeLines(){
        double adjustment = currentScale == 1 ? 1 : 
                currentScale > 1 ? (0.75 * (Math.pow(currentScale, 0.75))) : (1 / Math.pow((2 - currentScale), 2));
        layers.forEach(layer -> {
            if (layer instanceof LocationPathsLayer locLayer) {
                locLayer.scale(adjustment);
            }
        });
    }
    
    /**
     * Adjust the scaling of marker text to take into account the current map scale.
     * This requires that the scale of the marker text to be 1/currentScale to allow them to remain the same size.
     * @param textLabel The Marker/Text pair containing the marker text and linked Marker object. Note that when the
     * marker text is set to be paired with a GeoShape, that the MArker object in the pair is a dummy UserText marker.
     */
     private void resizeMarkerText(Pair<AbstractMarker, Text> textLabel) {
        textLabel.getValue().setScaleX(0.75 +0.25/Math.pow(currentScale, 0.75));
        textLabel.getValue().setScaleY(0.75 +0.25/Math.pow(currentScale, 0.75));
     }
    
     /**
      * Iterate over all marker text labels and resize them to account for current map scaling.
      */
    private void resizeAllText() {
        markerTextLabels.forEach(textLabel -> {
            resizeMarkerText(textLabel);
        });
    }
    
    /**
     * Display marker label text for the given PointMarker.
     *
     * @param markerText - Text to appear below the marker.
     * @param p - The marker itself.
     */
    private void setPointMarkerText(final String markerText, final PointMarker p) {
 
        final Text textMarker = new Text(markerText);
        final double fontSize = 15/Math.pow(MapView.getMapScale(), 0.85);
        textMarker.setStyle(" -fx-font-size: " + fontSize + ";");
        final Pair<AbstractMarker, Text> textLabel = new Pair(p, textMarker);        
        markerTextLabels.add(textLabel);
        pointMarkerTextGroup.getChildren().add(textLabel.getValue());

        final double posX = textLabel.getValue().getBoundsInParent().getCenterX();
        final double posY = textLabel.getValue().getBoundsInParent().getCenterY();
        final double markerCentreX = textLabel.getKey().getMarker().getBoundsInParent().getCenterX();
        final double markerCentreY = textLabel.getKey().getMarker().getBoundsInParent().getCenterY();
        final double markerHeight = textLabel.getKey().getMarker().getBoundsInParent().getHeight();
        if (textLabel.getKey() instanceof PointMarker) {
            textLabel.getValue().setTranslateY(textLabel.getValue().getTranslateY() + (markerCentreY + markerHeight / 2) - (posY - 10)/Math.pow(currentScale, 0.85));
            textLabel.getValue().setTranslateX(textLabel.getValue().getTranslateX() + markerCentreX - (posX + 0.75*markerText.length())/Math.pow(currentScale, 0.85));
        }
    }
    
    /**
     * Display marker label text for the given GeoShape. This GeoShape will represent a GeoShape tied to a
     * GeoShapePolygonMarker, noting that a GeoShapePolygonMarker may contain multiple GeoShapes. The location to
     * display labels attempts to be near the centre of the shape.
     *
     * @param markerText - Text to appear below the marker.
     * @param p - The marker itself.
     */
    private void setPolygonMarkerText(final String markerText, final GeoShape gs) {
        final Text t = new Text(markerText);    
        final double fontSize = 12/Math.pow(MapView.getMapScale(), 0.75);
        t.setStyle(" -fx-font-size: " + fontSize + ";");
        final UserPointMarker tempMarker = new UserPointMarker(this, AbstractMarker.NO_MARKER_ID, gs.getCenterX(), gs.getCenterY(), 0.05);
        
        final Pair<AbstractMarker, Text> textLabel = new Pair(tempMarker,  t); 
        markerTextLabels.add(textLabel);
        pointMarkerTextGroup.getChildren().add(textLabel.getValue());
            
        final double markerCentreX = gs.getBoundsInParent().getCenterX();
        final double markerCentreY = gs.getBoundsInParent().getCenterY();
        final double textWidth = t.getBoundsInParent().getWidth() / Math.pow(currentScale, 0.85);
        t.setX(markerCentreX - textWidth/2);
        t.setY(markerCentreY);
    }

    /**
     * Zoom the map to the given scale updating all layers.
     * 
     * @param scalingFactor The scale to zoom the map to.
     */
    private void scale(final double scalingFactor) {
        final double viewportWidth = getViewportBounds().getWidth();
        final double viewportHeight = getViewportBounds().getHeight();
        final double scaledContentWidth = getContent().getBoundsInLocal().getWidth();
        final double scaledContentHeight = getContent().getBoundsInLocal().getHeight();

        if (viewportWidth > scaledContentWidth && viewportHeight > scaledContentHeight && scalingFactor < currentScale) {
            return;
        }
        
        // Determine the new scale value and line width value. The scaledMapLineWidth value is designed to counteract
        // the value of scaleFactor and ensure that map lines remain at the same width
        currentScale = currentScale * scalingFactor;

        // Scale mapStackPane to the new zoom factor and update map line widths to counter it 
        mapStackPane.setScaleX(currentScale);
        mapStackPane.setScaleY(currentScale);
        if (!testUsingCanvas) {
            countrySVGPaths.forEach(svgPath -> svgPath.setStrokeWidth(SCALED_MAP_LINE_WIDTH/Math.pow(currentScale, 0.85)));
        }
         
        // Resize markers
        resizeMarkers();
        resizeLines();
        resizeAllText();
        refreshMapLayers();
        
        if (INFO_OVERLAY.isShowing()) {
            Platform.runLater(() -> {
                INFO_OVERLAY.updateZoomLabel(scaleSteps()); // y = x ^ n ... 
                INFO_OVERLAY.updateMapScaleText(scaledStepsToRepresent1000km()); // get equivalent scaled x,y coordiantes and calculate the distance
            });
        }
    }
    
    private double getDistancePerXstep() {
        final double currentMapX = getScreenCentreX();
        final double currentMapY = getScreenCentreY();
        final double startLon = MapConversions.mapXToLon(currentMapX);
        final double endLon = startLon + 10;
        final double startLat = MapConversions.mapYToLat(currentMapY);
        final double distance = getDistanceFromLatLonInKm(startLat, startLon, startLat, endLon);
        final double xCoordDiference = MapConversions.lonToMapX(endLon) - currentMapX;
        
        return distance / xCoordDiference;
    }
    
    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
      final double earthRadius = 6371; // Radius of the earth in km
      final double dLat = degToRad(lat2 - lat1);  // degTorad below
      final double dLon = degToRad(lon2 - lon1); 
      final double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) * 
                Math.sin(dLon/2) * Math.sin(dLon/2); 
      final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
      final double d = earthRadius * c; // Distance in km
      return d;
    }
    
    private double degToRad(double degValue) {
        return degValue * (Math.PI / 180);
    }
    
    private double scaledStepsToRepresent1000km() {
        return 1000 * currentScale / getDistancePerXstep();
    }
    
    private int scaleSteps() {
        double tempScale = currentScale;
        int steps = 0;
        if (tempScale >=1) {
            while (tempScale > 1) {
                tempScale /= MAP_SCALE_FACTOR;
                steps++;
            }
        } else {
            while (tempScale < 1) {
                tempScale *= MAP_SCALE_FACTOR;
                steps--;
            }            
        }
        return steps;
    }
    
    /***
     * Perform a zoom in by a zoom factor.
     */
    public void zoomIn() {
        scale(currentScale * MAP_SCALE_FACTOR);
    }
    
    /***
     * Perform a zoom out by a zoom factor.
     */
    public void zoomOut() {
        scale(currentScale / MAP_SCALE_FACTOR);
    }
    
    /**
     * Set scroll events
     */
    private void setScrollEventHandler() {
        // Scoll to zoom
        mapStackPane.setOnScroll(e -> {
            final double scaleAmount = (e.getDeltaY() > 0) ? MAP_SCALE_FACTOR : 1 / MAP_SCALE_FACTOR;
            final double posX = e.getX();
            final double posY = e.getY();
            final double cenX = getScreenCentreX();
            final double cenY = getScreenCentreY();

            final double viewportWidth = getViewportBounds().getWidth();
            final double viewportHeight = getViewportBounds().getHeight();
            final double scaledContentWidth = getContent().getBoundsInLocal().getWidth();
            final double scaledContentHeight = getContent().getBoundsInLocal().getHeight();

            final double deadZoneX = (MAP_WIDTH + 2 * EMPTY_BORDER_REGION) * viewportWidth / (scaledContentWidth * 4.5);
            final double deadZoneY = (MAP_HEIGHT + 2 * EMPTY_BORDER_REGION) * viewportHeight / (scaledContentHeight * 4.5);

            double modX = Math.abs(posX - cenX) > deadZoneX ? ((posX - cenX) > 0 ? ((posX - cenX - deadZoneX/1.5)/3) : ((posX - cenX + deadZoneX/1.5)/3)) : 0;
            double modY = Math.abs(posY - cenY) > deadZoneY ? ((posY - cenY) > 0 ? ((posY - cenY - deadZoneY/1.5)/3) : ((posY - cenY + deadZoneY/1.5)/3)) : 0;

            final double maxModX = (viewportWidth/scaledContentWidth) * (MAP_WIDTH/3); // 1/3 of the viewport screen content area ;
            if (Math.abs(modX) > maxModX) {
                modX = modX > 0 ? maxModX : -maxModX;
            }
            final double maxModY = (viewportHeight/scaledContentHeight) * (MAP_HEIGHT/3); // 1/3 of the viewport screen content area ;
            if (Math.abs(modY) > maxModY) {
                modY = modY > 0 ? maxModY : -maxModY;
            }
            
            double targetX = cenX + modX;
            if (targetX < 0) {
                targetX = 0;
            } else if (targetX > MAP_WIDTH) {
                targetX = MAP_WIDTH;
            }
            double targetY = cenY + modY;
            if (targetY < 0) {
                targetY = 0;
            } else if (targetY > MAP_HEIGHT) {
                targetY = MAP_HEIGHT;
            }

            scale(scaleAmount);
            panToPosition(targetX, targetY);
            
            e.consume();
        });
    }

    public double getScreenCentreX(){
        final double hScrollPos = self.getHvalue();
        final double viewportWidth = getViewportBounds().getWidth();        
        // when hScrollPos is 0%, the centre of the visible screen section is half the width of the viewport window
        // when hScrollPos is 100%, the centre is half the viewport window size subtracted from the full width.        
        final double contentWidth = getContent().getBoundsInLocal().getWidth();        
        final double currentXOffset = viewportWidth/2 + hScrollPos * (contentWidth - viewportWidth);
        final double centreXVal = (MAP_WIDTH + 2 * EMPTY_BORDER_REGION) * currentXOffset/contentWidth - EMPTY_BORDER_REGION;
        return centreXVal;
    }
    
    public double getScreenCentreY(){
        final double vScrollPos = self.getVvalue();
        final double viewportHeight = getViewportBounds().getHeight();        
        final double contentHeight = getContent().getBoundsInLocal().getHeight();
        final double currentYOffset = viewportHeight/2 + vScrollPos * (contentHeight - viewportHeight);
        final double centreYVal = (MAP_HEIGHT + 2 * EMPTY_BORDER_REGION) * currentYOffset/contentHeight - EMPTY_BORDER_REGION;
        return centreYVal;
    }

    /**
     * Deselects all markers.
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
                uMarker.scaleMarker(currentScale == 1 ? 1 : currentScale > 1 ? (0.85 *(Math.pow(currentScale, 0.85))) : (1/(2 - currentScale)/(2 - currentScale)));
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
     * Iterate over all markers and resize them based on current zoom level to ensure they remain constant.
     */
    private void resizeMarkers() {
        // TODO: once all markers have scaleMarker() set up, just call scaleMarker()
        final double adjustment = currentScale == 1 ? 1 : currentScale > 1 ? (0.85 *(Math.pow(currentScale, 0.85))) : (1/(2 - currentScale)/(2 - currentScale));
        markers.values().forEach(abstractMarker -> {
            if (abstractMarker instanceof PointMarker) {
                final PointMarker marker = (PointMarker) abstractMarker;
                marker.scaleMarker(adjustment);
            } else {
                abstractMarker.scaleMarker(adjustment);
            }
        });

        userMarkers.forEach(abstractMarker -> {
            if (abstractMarker instanceof UserPointMarker) {
                final UserPointMarker marker = (UserPointMarker) abstractMarker;
                marker.scaleMarker(adjustment);
            } else {
                abstractMarker.scaleMarker(adjustment);
            }
        });
    }

    public void refreshMapLayers() {
        layers.forEach(layer -> {
            if (layer instanceof AbstractMapLayer amLayer && !(layer instanceof DayNightLayer)) {
                Platform.runLater(() -> {    
                    parent.refreshLayer(amLayer);
                });
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
        parent.redrawQueriedMarkers();
        redrawUserMarkers();
        resizeMarkers();
        resizeLines();
        resizeAllText();
        refreshMapLayers();
    }

    public List<AbstractMapLayer> getLayers() {
        return layers;
    }

    /**
     * Redraw all markers that have been extracted from the graph
     */
    public void redrawQueriedMarkers() {
        graphMarkerGroup.getChildren().clear();
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
     * Selects one or more nodes on the graph
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

        mapStackPane.setTranslateX(centerX - mapDetails.getWidth() / 2);
        mapStackPane.setTranslateY(centerY - mapDetails.getHeight() / 2);
    }

    /**
     * Pan to specific location
     *
     * @param x - coordinate
     * @param y - coordinate
     */
    private void pan(final double x, final double y) {

        final Vec3 center = new Vec3(mapDetails.getWidth() / 2, mapDetails.getHeight() / 2);

        final Point2D averageMarkerPosition = mapStackPane.localToParent(x, y);

        final double parentCenterX = mapStackPane.localToParent(center.getX(), center.getY()).getX();
        final double parentCenterY = mapStackPane.localToParent(center.getX(), center.getY()).getY();

        final Vec3 dirVect = new Vec3(parentCenterX - averageMarkerPosition.getX(), parentCenterY - averageMarkerPosition.getY());


        mapStackPane.setTranslateX(mapStackPane.getTranslateX() + dirVect.getX());
        mapStackPane.setTranslateY(mapStackPane.getTranslateY() + dirVect.getY());
    }
    
    /**
     * Zoom map out to a scale that allows the entire map to be seen.
     */
    public void zoomToAll() {
        // best to simply do a hard reset here.
        currentScale = 1;
        scale(1);
        self.setHvalue(0.5);
        self.setVvalue(0.5);                
    }
    
    /**
     * Zoom the map to the requested scale and centre it on the given lat/long.
     * This function only deals with maps being zoomed further in, to zoom toa selected range from the current graph.
     * Using the function to zoom out would cause errors when the requested scale and centre position would see space to
     * the top or left of the graph.
     * @param requiredScale The requested zoom scale.
     * @param centreLat Latitude to centre map on.
     * @param centreLon Longitude to centre map on.
     */
    private void zoomAndRecenterToSelection(final double requiredScale, final double centreLat, final double centreLon) {
        
        if (requiredScale < this.currentScale) {
            LOGGER.log(Level.WARNING, "Requested zoom scale is less than current zoom scale. Map will not centre neatly");
        }

        // Adjust scale, prior to readjusting centre to ensure all map dimensions are known
        scale(requiredScale);
        
        // Determine the dimensions of the current viewpane (the viewable region in the ScrollPane) and the layoutBounds
        // which returnes the full scrollable region of the map in its scaled state. This means as the map scale
        // increases, the size of layoutBounds increases at the same time. A map 100x100 pizxels at a scale of 2 is
        // represented as 200x200 pixels.
        final Bounds layoutBounds = scrollContent.getLayoutBounds();
        final Bounds viewportBounds = this.getViewportBounds();
        
        // Convert the supplied centre lat/long to X/Y coordinates converted to the current scale to ensure they are in
        // the same units as layoutBounds and viewportBounds. These can then be used to determine required new hValue
        // and vValue.
        final double centreX = MapConversions.lonToMapX(centreLon) * currentScale;
        final double centreY = MapConversions.latToMapY(centreLat) * currentScale;
        
        // Now work out how much the ScrollPane is able to scroll in both dimensions. Given that the ScrollPane doesnt
        // let its content scroll off the pane in either direction, the scrollable range is thefull range of layoutBounds
        // less the dimensions of the viewport
        final double viewportScrollWidth = layoutBounds.getWidth() - viewportBounds.getWidth();
        final double viewportScrollHeight = layoutBounds.getHeight() - viewportBounds.getHeight();
        
        // Determine the top left of the proposed new viewport, given that we want to keep the view centred on the
        // supplied coordinate.
        final double viewportX = centreX - viewportBounds.getWidth()/2;
        final double viewportY = centreY - viewportBounds.getHeight()/2;
        final double targetHvalue = viewportX/viewportScrollWidth;
        final double targetVvalue = viewportY/viewportScrollHeight;
        
        // Now reposition viewport using the hValue/vValue
        this.setHvalue(targetHvalue);
        this.setVvalue(targetVvalue);
    }
    
    /**
     * Zoom the map to the currently made selection.
     */
    public void zoomToSelection() {
        // loop through all markers and use the coordinates of the selected nodes
        // to find the rectangle that contains them all
        int totalFound = 0;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (final AbstractMarker m : markers.values()) {
            if (m instanceof PointMarker pm && pm.isSelected()) {
                totalFound++;
                if (pm.getX() < minX) {
                    minX = pm.getX();
                }
                if (pm.getY() < minY) {
                    minY = pm.getY();
                }
                
                if (pm.getX() > maxX) {
                    maxX = pm.getX();
                }
                if (pm.getY() > maxY) {
                    maxY = pm.getY();
                }
            }
        }
        if (totalFound == 0) {
            return;
        }
        
        minX -= 0.15*EMPTY_BORDER_REGION/Math.sqrt(totalFound);
        minY -= 0.15*EMPTY_BORDER_REGION/Math.sqrt(totalFound);
        maxX += 0.15*EMPTY_BORDER_REGION/Math.sqrt(totalFound);
        maxY += 0.15*EMPTY_BORDER_REGION/Math.sqrt(totalFound);
        
        final double rectangleWidth = maxX - minX;
        final double rectangleHeight = maxY - minY;
        // set the position and scale such that the rectangle is inside the viewport
        final double widthFactor = getViewportBounds().getWidth() / rectangleWidth;
        final double heightFactor = getViewportBounds().getHeight() / rectangleHeight;
        // the ratio of the current width to the selectionArea width is the change of zoom scale required
        final double newScale = Math.min(widthFactor, heightFactor);
        
        currentScale = newScale;
        scale(1);
        final double targetPosX = maxX - (maxX - minX)/2;
        final double targetPosY = maxY - (maxY - minY)/2;
        panToPosition(targetPosX, targetPosY);
    }

    public void panToPosition(final double x, final double y) {
        // How to calculate scrollbar offsets:
        // Accounting for scaling ... visible window fraction is getViewportBound().getWidth() / getContent().getBoundsInLocal().getWidth()
        // sample numbers ... when zoomed in at a scale of 5.44 .... 1045 / 8158
        // to pan to an x co-ordinate on a 1:1 scale with numbers of 1045 / 1500 ... when map x = 375 (on a 1000x999 map)
        // scrollbar calculation for adjusted x = EMPTY_BORDER_REGION + 375 = 625
        //           625 / 1500 = 0.417
        // for the scaled/zoomed in case, the viewport remains a static size, so we need to compensate for that
        // 5.44 * zoom ->   3400 / 8160 = 0.417
        // viewport fraction = 1045 / 8160 = 0.128
        // 0% horizontal scrollbar position has a center point = 1045/2 = 522.5
        // 100% horizontal scrollbar position has a center point = 8160 - 522.5 = 7637.5
        // to move to position 3400 of the full range,
        // you actually need to move 3400 - 522.5 = 2877.5 in a range of 7115
        // the actual scrollbar position becomes 2877.5/7155 = 0.404
        // the theoretical position was 0.417, but the actual position is 0.404

        final double viewportWidthAdjustment = getViewportBounds().getWidth()/2;
        final double viewportHeightAdjustment = getViewportBounds().getHeight()/2;
        final double scaledContentWidth = getContent().getBoundsInLocal().getWidth();
        final double scaledContentHeight = getContent().getBoundsInLocal().getHeight();

        final double scaledContentCenterX = currentScale * (EMPTY_BORDER_REGION + x);
        final double adjustedCenterX = scaledContentCenterX - viewportWidthAdjustment;
        final double scrollEndPosX = scaledContentWidth - viewportWidthAdjustment;
        final double scrollRangeX = scrollEndPosX - viewportWidthAdjustment;
        final double adjustedHpos = adjustedCenterX / scrollRangeX;
        
        final double scaledContentCenterY = currentScale * (EMPTY_BORDER_REGION + y);
        final double adjustedCenterY = scaledContentCenterY - viewportHeightAdjustment;
        final double scrollEndPosY = scaledContentHeight - viewportHeightAdjustment;
        final double scrollRangeY = scrollEndPosY - viewportHeightAdjustment;
        final double adjustedVpos = adjustedCenterY / scrollRangeY;

        self.setHvalue(adjustedHpos);
        self.setVvalue(adjustedVpos);           
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
     * TODO: This code zooming n iteratively until it zooms to the pont that not all markers are visible, then it
     * zooms back out one level to bring everything back into view. can this be done in a better way by calculating
     * map extents and then zooming to a level that covers it all ?
     * 
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

// TODO: rework this to use mapView ScrollPane checks
//                return (parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y))
//                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y))
//                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x, y + r.getHeight()))
//                        && parent.getViewPortRectangle().contains(mapStackPane.localToParent(x + r.getWidth(), y + r.getHeight())));
            }
        }

        return true; // ?? this ALWAYS returns true ! - TODO: investigate/fix this
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

            final Text latitudeLabel = new Text("Latitude");
            latitudeLabel.setFill(Color.WHITE);

            final Text longitudeLabel = new Text("Longitude");
            longitudeLabel.setFill(Color.WHITE);

            final Text radiusLabel = new Text("Radius");
            radiusLabel.setFill(Color.WHITE);

            final TextField latitudeInput = new TextField();
            latitudeInput.setBorder(new Border(new BorderStroke(Color.WHITE, null, null, null)));
            latitudeInput.setStyle(textFillStyle);

            latitudeInput.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor(backgroundFill).getJavaFXColor(), null, null)));

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

            coordinateGridPane.add(latitudeLabel, 0, 0);
            coordinateGridPane.add(longitudeLabel, 1, 0);
            coordinateGridPane.add(radiusLabel, 2, 0);

            coordinateGridPane.add(latitudeInput, 0, 1);
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
                    coordinateGridPane.add(latitudeLabel, 0, 0);
                    coordinateGridPane.add(longitudeLabel, 1, 0);
                    coordinateGridPane.add(radiusLabel, 2, 0);

                    coordinateGridPane.add(latitudeInput, 0, 1);
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

            okButton.setOnAction(event -> {
                String selectedGeoType = geoTypeMenu.getSelectionModel().getSelectedItem();

                // Convert coordinate input to x and y coordinates
                if (selectedGeoType.equals(COORDINATE)) {

                    double latitude = -3000;
                    double longitude = -3000;
                    double radius = -3000;

                    final String latitudeText = latitudeInput.getText();
                    final String longitudeText = longitudeInput.getText();
                    final String radiusText = radiusInput.getText();

                    if (StringUtils.isNotBlank(latitudeText) && NumberUtils.isParsable(latitudeText.strip()) && StringUtils.isNotBlank(longitudeText) && NumberUtils.isParsable(longitudeText.strip())) {
                        latitude = Double.parseDouble(latitudeText.strip());
                        longitude = Double.parseDouble(longitudeText.strip());
                    } else {
                        showingZoomToLocationPane = false;
                        stage.close();
                        return;
                    }

                    final double x = MapConversions.lonToMapX(longitude);
                    final double y = MapConversions.latToMapY(latitude);

                    if (StringUtils.isNotBlank(radiusText) && NumberUtils.isParsable(radiusText.strip())) {
                        radius = Double.parseDouble(radiusText.strip());

                        // draw circle at entered location
                        final CircleMarker zoomCircleMarker = new CircleMarker(self, drawnMarkerId++, x, y, radius);

                        zoomCircleMarker.generateCircle();
                        drawnMarkerGroup.getChildren().add(zoomCircleMarker.getMarker());
                        userMarkers.add(zoomCircleMarker);

                        // If no radius is provided then draw point marker at location
                    } else {
                        final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05);
                        marker.setMarkerPosition(0, 0);

                        addUserDrawnMarker(marker);

                        userMarkers.add(marker);
                    }
                    panToPosition(x, y);

                    // Convert mgrs value to x and y
                } else if (selectedGeoType.equals(MGRS) && StringUtils.isNotBlank(mgrsInput.getText())) {

                    final MGRSCoord coordinate = MGRSCoord.fromString(mgrsInput.getText().strip(), null);
                    double x = MapConversions.lonToMapX(coordinate.getLongitude().degrees);
                    double y = MapConversions.latToMapY(coordinate.getLatitude().degrees);

                    UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05);
                    marker.setMarkerPosition(0, 0);

                    addUserDrawnMarker(marker);

                    userMarkers.add(marker);
                    panToPosition(x, y);

                    // Convert geohash value to x and y
                } else if (selectedGeoType.equals(GEOHASH) && StringUtils.isNotBlank(geoHashInput.getText())) {

                    final double[] geohashCoordinates = Geohash.decode(geoHashInput.getText().strip(), Geohash.Base.B32);
                    double x = MapConversions.lonToMapX(geohashCoordinates[1] - geohashCoordinates[3]);
                    double y = MapConversions.latToMapY(geohashCoordinates[0] - geohashCoordinates[2]);

                    final UserPointMarker marker = new UserPointMarker(self, drawnMarkerId++, x, y, 0.05);
                    marker.setMarkerPosition(0, 0);

                    addUserDrawnMarker(marker);
                    userMarkers.add(marker);
                    panToPosition(x, y);

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
        
        // Determine if the marker should be drawn. This decision depends on several factors. The first check is to
        // ensure that the marker type has been selected for display from the available types. The second factor is to
        // ensure that only selected markers are shown if only SELECTED markers should be displayed.
        if (markersShowing.contains(marker.getType()) && ((markersShowing.contains(AbstractMarker.MarkerType.SELECTED) && marker.isSelected()) || !markersShowing.contains(AbstractMarker.MarkerType.SELECTED))) {
            marker.setMarkerPosition(mapDetails.getWidth(), mapDetails.getHeight());
            if (!graphMarkerGroup.getChildren().contains(marker.getMarker())) {
                
                if (marker instanceof GeoShapePolygonMarker) {
                    // Handle markers that contain GeoJson in the Geo.Shape attribute which is a more complex marker type
                    final GeoShapePolygonMarker gsp = (GeoShapePolygonMarker) marker;
                    gsp.getGeoShapes().values().forEach(shapePair -> {
                        if (!graphMarkerGroup.getChildren().contains(shapePair.getKey())) {
                            graphMarkerGroup.getChildren().add(shapePair.getKey());
                        }
                    });
                } else {
                    graphMarkerGroup.getChildren().add(marker.getMarker());
                }

                marker.scaleMarker(currentScale == 1 ? 1 : currentScale > 1 ? (0.85 *(Math.pow(currentScale, 0.85))) : (1/(2 - currentScale)/(2 - currentScale)));
            }
        }
    }

    public Group getGraphMarkerGroup() {
        return graphMarkerGroup;
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

    /**
     * Return true if the user has selected a region of the map.
     * @return True if the user has selected a region of the map.
     */
    public boolean isSelectionMade() {
        return isSelectionMade;
    }
    
    public Set<AbstractMarker.MarkerType> getMarkersShowing() {
        return markersShowing;
    }

    public double getScaledMapLineWidth() {
        return SCALED_MAP_LINE_WIDTH / Math.pow(currentScale, 0.85);
    }

    /**
     * Return details of current scaling factor (zoom) of the map.
     *
     * @return Current scaling factor (zoom) of the map.
     */
    public double getCurrentScale() {
        return currentScale; 
    }
    
    /**
     * Load the world map
     */
    private void parseMapSVG() {
        countryGroup.getChildren().clear();
        
        // You will need to adequately prepare the map data before it can be processed here.
        // This parser only collects PATH data from the source file.
        // The entire PATH data content needs to be on a single line, and the PATH tag needs to be the first tag of the line.
        // It does not read in polygons, lines, shapes, or anything else from the file.
        // It also doesn't apply any svg internally defined matrix transformations (fixed x and y scaling and offsets to the supplied data).

        // Read map from file
        try {

            try (final BufferedReader bFileReader = new BufferedReader(new FileReader(mapDetails.getMapFile()))) {
                String path = "";
                String line = "";
                boolean firstLineDone = false;

                // While there is more to read
                while ((line = bFileReader.readLine()) != null) {
                    // Strip the line read in
                    line = line.strip().replace("\t", "  ");                    
                    // Extract the svg path segment from the line
                    if (line.startsWith("<path")) {
                        final int startIndex = line.indexOf(" d=") + 4;
                        final int endIndex = line.substring(startIndex).indexOf("\"");

                        if (startIndex > -1 && endIndex > -1) {
                            path = line.substring(startIndex, startIndex + endIndex);
                            // Create the SVGPath object and add it to an array
                            final SVGPath svgPath = new SVGPath();
                            if (!firstLineDone) { // the first line is generally the bounding border rectangle, not map data
                                svgPath.setFill(Color.rgb(128,140,255,0.2));
                                firstLineDone = true;
                            } else {
                                svgPath.setFill(Color.rgb(255,255,255,0.8));
                            }
                            svgPath.setStrokeWidth(getScaledMapLineWidth());
                            svgPath.setStroke(Color.BLACK);
                            svgPath.setContent(path);
                            // to implement matrix transforms : svgPath.getTransforms().add(Transform.affine(-1, 2.4336e-008, -2.4336e-008, -1, 2163.78, 1161.93));
                            countrySVGPaths.add(svgPath);
                        }
                    }
                }
            }

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Exception thrown: {0}", e.getMessage());
        }
    }
    
    public static double getMapScale() {
        return currentScale;
    }
}

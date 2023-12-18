/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.mapview.exporters.GeoJsonExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.GeoPackageExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.KmlExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter.MapExporterWrapper;
import au.gov.asd.tac.constellation.views.mapview.exporters.ShapefileExporter;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ActivityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.EntityPathsLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.LocationPathsLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ThiessenPolygonsLayer2;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MenuButtonCheckCombobox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

/**
 * MapViewPane holds container of the MapView and the entire toolbar All toolbar
 * options are set here and event handlers for toolbar options call functions in
 * MapView class to manipulate the map
 *
 * @author altair1673
 */
public class MapViewPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(MapViewPane.class.getName());

    private final MapViewTopComponent parent;
    public final GridPane toolBarGridPane;
    // Stackpane to hold the map
    private final StackPane parentStackPane;

    // Holds various overlays
    private final AnchorPane anchorPane = new AnchorPane();

    // String for all the menu options
    private static final String MARKER_TYPE_POINT = "Point Markers";
    private static final String MARKER_TYPE_LINE = "Line Markers";
    private static final String MARKER_TYPE_POLYGON = "Polygon Markers";
    private static final String MARKER_TYPE_CLUSTER = "Cluster Markers";
    private static final String SELECTED_ONLY = "Selected Only";
    private static final String ZOOM_ALL = "Zoom to All";
    private static final String ZOOM_SELECTION = "Zoom to Selection";
    private static final String ZOOM_LOCATION = "Zoom to Location";
    private static final String ZOOM_IN = "Zoom In";
    private static final String ZOOM_OUT = "Zoom Out";

    private static final String DAY_NIGHT = "Day / Night";
    private static final String HEATMAP_STANDARD = "Heatmap (Standard)";
    private static final String HEATMAP_POPULARITY = "Heatmap (Popularity)";
    private static final String HEATMAP_ACTIVITY = "Heatmap (Activity)";
    private static final String ENTITY_PATHS = "Entity Paths";
    private static final String LOCATION_PATHS = "Location Paths";
    private static final String THIESSEAN_POLYGONS = "Thiessean Polygons";

    public static final String INFO_OVERLAY = "Info Overlay";
    public static final String TOOLS_OVERLAY = "Tools Overlay";
    public static final String OVERVIEW_OVERLAY = "Overview Overlay";

    public static final String DEFAULT_COLOURS = "Default Colors";
    public static final String USE_COLOUR_ATTR = "Use Color Attribute";
    public static final String USE_OVERLAY_COL = "Use Ovelay Color";
    public static final String USE_BLAZE_COL = "Use Blaze Color";

    public static final String NO_LABELS = "No Labels";
    public static final String USE_LABEL_ATTR = "Use Label Atrtibute";
    public static final String USE_IDENT_ATTR = "Use Identifier Attribute";

    public static final String GEO_JSON = "GeoJSON";
    public static final String GEO_PACKAGE = "GeoPackage";
    public static final String KML = "KML";
    public static final String SHAPEFILE = "Shapefile";

    // All the toolbar UI elements
    private final ChoiceBox mapsDropDown;
    private final MenuButton zoomDropDown;

    private final Button helpButton;
    private final List<String> dropDownOptions = new ArrayList<>();
    private final Label latLabel = new Label("Latitude: ");
    private final Label lonLabel = new Label("Longitude: ");
    private final Label latField = new Label("0.00");
    private final Label lonField = new Label("0.00");

    // Store list of available maps and an object to store the currently selected map.
    private final List<MapDetails> maps = Arrays.asList(
            new MapDetails(MapDetails.MapType.SVG, 1000, 999, 85.0511, -85.0511, -180, 180, "Full World (default)",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/WorldMap1000x999.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain())),
            new MapDetails(MapDetails.MapType.SVG, 1000, 1018, 38, -36, -18, 60, "Africa & Middle East",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/AfricaAndMiddleEast1000x1018.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain())),
            new MapDetails(MapDetails.MapType.SVG, 1000, 1491, 84, -57, -170, 84, "The Americas",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/Americas1000x1491.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain())),
            new MapDetails(MapDetails.MapType.SVG, 1000, 1443, 72, 33, 13, 62, "Europe & UK",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/EuropeAndUK1000x1443.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain())),
            new MapDetails(MapDetails.MapType.SVG, 1000, 1090, 10, -55, 110, 180, "South East Asia",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/SEAsia1000x1090.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain()))
    );
    private MapDetails selectedMap;
    
    private MapView mapView;

    // A map of all the layers
    private int layerId = 0;
    private final Map<String, Integer> layerMap = new HashMap<>();
    
    public MapViewPane(final MapViewTopComponent parentComponent) {
        parent = parentComponent;

        parentStackPane = new StackPane();

        toolBarGridPane = new GridPane();
        toolBarGridPane.setHgap(5);
        toolBarGridPane.setVgap(10);

        latLabel.setFont(Font.font(15));
        lonLabel.setFont(Font.font(15));
        latLabel.setPadding(new Insets(0, 0, 0, 8));

        latField.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        latField.setFont(Font.font(15));
        lonField.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        lonField.setFont(Font.font(15));
        
        // Add map names to dropdown to allow their selection. Select the first (default) entry as the default map to view
        mapsDropDown = new ChoiceBox();
        maps.forEach(map -> {
            mapsDropDown.getItems().add(map.getMapName());
        });
        mapsDropDown.getSelectionModel().selectFirst();
        selectedMap = maps.get(0);
        mapView = new MapView(this, maps.get(mapsDropDown.getSelectionModel().getSelectedIndex()));

        // Set up action to ensure that changing the map selection triggers new map to be loaded and displayed with
        // refreshed content.
        mapsDropDown.setOnAction((event) -> {
            parentStackPane.getChildren().remove(mapView);
            selectedMap = maps.get(mapsDropDown.getSelectionModel().getSelectedIndex());
            mapView = new MapView(this, selectedMap);
            parent.recalculateCoords();
            parentStackPane.getChildren().add(mapView);
        });

        final MenuButtonCheckCombobox layersMenuButton = new MenuButtonCheckCombobox(FXCollections.observableArrayList(DAY_NIGHT, HEATMAP_STANDARD, HEATMAP_POPULARITY, HEATMAP_ACTIVITY, ENTITY_PATHS, LOCATION_PATHS, THIESSEAN_POLYGONS), false, false);
        layersMenuButton.getMenuButton().setTooltip(new Tooltip("Select layers to render over the map in the Map View"));
        layersMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            if (parent.getCurrentGraph() != null) {
                layersMenuButton.getOptionMap().keySet().forEach(key -> addLayer(key, layerId, layersMenuButton.getOptionMap().get(key).isSelected()));
            } else {
                layersMenuButton.revertLastAction();
                NotifyDisplayer.display("Layer options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });
        layersMenuButton.setIcon(parent.getClass().getResource("resources/layers2.png").toString());

        // Add overlays to toolbar
        final MenuButtonCheckCombobox overlaysMenuButton = new MenuButtonCheckCombobox(FXCollections.observableArrayList(INFO_OVERLAY, TOOLS_OVERLAY, OVERVIEW_OVERLAY), false, false);
        overlaysMenuButton.getMenuButton().setTooltip(new Tooltip("Select overlays to render over the map in the Map View"));
        overlaysMenuButton.setIcon(parent.getClass().getResource("resources/overlays.png").toString());
        // Overlay event handler
        overlaysMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            overlaysMenuButton.getOptionMap().keySet().forEach(key -> {
                toggleOverlay(key, overlaysMenuButton.getOptionMap().get(key).isSelected());

                if (key.equals(INFO_OVERLAY) && overlaysMenuButton.getOptionMap().get(key).isSelected() && !toolBarGridPane.getChildren().contains(latLabel)) {
                    toolBarGridPane.add(latLabel, 0, 1);
                    toolBarGridPane.add(latField, 1, 1);
                    toolBarGridPane.add(lonLabel, 2, 1);
                    toolBarGridPane.add(lonField, 3, 1);
                } else if (key.equals(INFO_OVERLAY) && !overlaysMenuButton.getOptionMap().get(key).isSelected()) {
                    toolBarGridPane.getChildren().removeAll(latLabel, latField, lonLabel, lonField);
                } else if (key.equals(OVERVIEW_OVERLAY) && overlaysMenuButton.getOptionMap().get(key).isSelected()) {
                    mapView.updateOverviewOverlay();
                }
            });
        });

        // Zoom menu set up and event handling
        zoomDropDown = new MenuButton();
        zoomDropDown.setGraphic(new ImageView(new Image(parent.getClass().getResource("resources/zoom.png").toString())));
        final MenuItem zoomAll = new MenuItem(ZOOM_ALL);
        final MenuItem zoomSelection = new MenuItem(ZOOM_SELECTION);
        final MenuItem zoomLocation = new MenuItem(ZOOM_LOCATION);
        final MenuItem zoomIn = new MenuItem(ZOOM_IN);
        final MenuItem zoomOut = new MenuItem(ZOOM_OUT);

        zoomDropDown.getItems().addAll(zoomAll, zoomSelection, zoomLocation, zoomIn, zoomOut);
        zoomDropDown.setTooltip(new Tooltip("Zoom based on markers or locations in the Map View"));

        final String zoomError = "Zoom options require a graph to be open!";

        zoomAll.setOnAction(event -> {
            if (parent.getCurrentGraph() != null) {
                mapView.panToCenter();
                mapView.panToAll();
            } else {
                NotifyDisplayer.display(zoomError, NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        zoomSelection.setOnAction(event -> {
            if (parent.getCurrentGraph() != null) {
                mapView.panToCenter();
                mapView.panToSelection();
            } else {
                NotifyDisplayer.display(zoomError, NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        zoomLocation.setOnAction(event -> {
            if (parent.getCurrentGraph() != null) {
                mapView.generateZoomLocationUI();
            } else {
                NotifyDisplayer.display(zoomError, NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        /**
         * Menu item allowing map to be zoomed in by a zoom factor.
         */
        zoomIn.setOnAction(event -> {
            mapView.zoomIn();
        });

        /**
         * Menu item allowing map to be zoomed out by a zoom factor.
         */
        zoomOut.setOnAction(event -> {
            mapView.zoomOut();
        });

        // Menu to show/hide markers        
        final MenuButtonCheckCombobox markerMenuButton = new MenuButtonCheckCombobox(FXCollections.observableArrayList(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_CLUSTER, SELECTED_ONLY), false, false);
        markerMenuButton.setIcon(parent.getClass().getResource("resources/location-pin.png").toString());

        markerMenuButton.getMenuButton().setTooltip(new Tooltip("Choose which markers are displayed in the Map View"));
        markerMenuButton.selectItem(MARKER_TYPE_POINT);
        markerMenuButton.selectItem(MARKER_TYPE_LINE);
        markerMenuButton.selectItem(MARKER_TYPE_POLYGON);
        // Event handler for hiding/showing markers
        markerMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            if (parent.getCurrentGraph() != null) {
                markerMenuButton.getOptionMap().keySet().forEach(key -> mapView.updateShowingMarkers(getMarkerTypeFromString((String) key), markerMenuButton.getOptionMap().get(key).isSelected()));
            } else {
                markerMenuButton.revertLastAction();
                NotifyDisplayer.display("Marker options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        // Marker colour mneu setup and event handling
        final MenuButtonCheckCombobox coloursMenuButton = new MenuButtonCheckCombobox(FXCollections.observableList(Arrays.asList(DEFAULT_COLOURS, USE_COLOUR_ATTR, USE_OVERLAY_COL, USE_BLAZE_COL)), true, false);
        coloursMenuButton.setIcon(parent.getClass().getResource("resources/paint-roller.png").toString());
        coloursMenuButton.selectItem(DEFAULT_COLOURS);
        coloursMenuButton.getMenuButton().setTooltip(new Tooltip("Chose the color scheme for markers displayed in the Map View"));
        coloursMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            if (parent.getCurrentGraph() != null) {
                coloursMenuButton.getOptionMap().keySet().forEach(key -> {
                    if (coloursMenuButton.getOptionMap().get(key).isSelected()) {
                        mapView.getMarkerColourProperty().set(key);
                    }
                });
            } else {
                coloursMenuButton.revertLastAction();
                coloursMenuButton.selectItem(DEFAULT_COLOURS);
                NotifyDisplayer.display("Colour options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        // Marker label menu setup and event handling
        final MenuButtonCheckCombobox labelsMenuButton = new MenuButtonCheckCombobox(FXCollections.observableList(Arrays.asList(NO_LABELS, USE_LABEL_ATTR, USE_IDENT_ATTR)), true, false);
        labelsMenuButton.setIcon(parent.getClass().getResource("resources/price-label.png").toString());
        labelsMenuButton.selectItem(NO_LABELS);
        labelsMenuButton.getMenuButton().setTooltip(new Tooltip("Chose the label for markers displayed in the Map View"));
        labelsMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            if (parent.getCurrentGraph() != null) {
                labelsMenuButton.getOptionMap().keySet().forEach(key -> {
                    if (labelsMenuButton.getOptionMap().get(key).isSelected()) {
                        mapView.getMarkerTextProperty().set(key);
                    }
                });
            } else {
                labelsMenuButton.revertLastAction();
                labelsMenuButton.selectItem(NO_LABELS);
                NotifyDisplayer.display("Label options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        // Export menu setup and eventer handling
        final MenuButtonCheckCombobox exportMenuButton = new MenuButtonCheckCombobox(FXCollections.observableList(Arrays.asList(GEO_JSON, GEO_PACKAGE, KML, SHAPEFILE)), true, true);
        exportMenuButton.getMenuButton().setTooltip(new Tooltip("Export from the Map View"));
        exportMenuButton.setIcon(parent.getClass().getResource("resources/download.png").toString());
        exportMenuButton.getItemClicked().addListener((obs, oldVal, newVal) -> {
            if (parent.getCurrentGraph() != null) {
                exportMenuButton.getOptionMap().keySet().forEach(key -> {
                    if (exportMenuButton.getOptionMap().get(key).isSelected()) {
                        MapExporterWrapper exporterWrapper = null;
                        if (key.equals(GEO_JSON)) {
                            exporterWrapper = new MapExporterWrapper(new GeoJsonExporter());
                        } else if (key.equals(KML)) {
                            exporterWrapper = new MapExporterWrapper(new KmlExporter());
                        } else if (key.equals(SHAPEFILE)) {
                            exporterWrapper = new MapExporterWrapper(new ShapefileExporter());
                        } else if (key.equals(GEO_PACKAGE)) {
                            exporterWrapper = new MapExporterWrapper(new GeoPackageExporter());
                        }

                        PluginExecution
                                .withPlugin(exporterWrapper.getExporter().getPluginReference())
                                .interactively(true)
                                .executeLater(parent.getCurrentGraph());

                        exportMenuButton.getOptionMap().get(key).setSelected(false);
                    }
                });
            } else {
                exportMenuButton.revertLastAction();
                NotifyDisplayer.display("Export options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });

        helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.setOnAction(event -> new HelpCtx(this.getClass().getName()).display());
        helpButton.setTooltip(new Tooltip("Help on using the Map View"));

        toolBarGridPane.add(mapsDropDown, 0, 0);
        toolBarGridPane.add(layersMenuButton.getMenuButton(), 1, 0);
        toolBarGridPane.add(overlaysMenuButton.getMenuButton(), 2, 0);
        toolBarGridPane.add(zoomDropDown, 3, 0);
        toolBarGridPane.add(markerMenuButton.getMenuButton(), 4, 0);
        toolBarGridPane.add(coloursMenuButton.getMenuButton(), 5, 0);
        toolBarGridPane.add(labelsMenuButton.getMenuButton(), 6, 0);
        toolBarGridPane.add(exportMenuButton.getMenuButton(), 7, 0);
        toolBarGridPane.add(helpButton, 8, 0);
        setTop(toolBarGridPane);
    }

    /**
     * Makes sure queried markers in the map are cleared and any markers that
     * are selected are also destroyed
     */
    public void clearQuerriesMarkers() {
        mapView.clearQueriedMarkers();
    }

    /**
     * Hide/Show overlay
     *
     * @param overlay - string representing the overlay
     */
    private void toggleOverlay(final String overlay, final boolean isChecked) {
        mapView.toggleOverlay(overlay, isChecked);
    }

    /**
     *
     * @param markerTypeString
     * @return
     */
    private AbstractMarker.MarkerType getMarkerTypeFromString(final String markerTypeString) {
        switch (markerTypeString) {
            case MARKER_TYPE_POINT:
                return AbstractMarker.MarkerType.POINT_MARKER;
            case MARKER_TYPE_POLYGON:
                return AbstractMarker.MarkerType.POLYGON_MARKER;
            case MARKER_TYPE_LINE:
                return AbstractMarker.MarkerType.LINE_MARKER;
            case MARKER_TYPE_CLUSTER:
                return AbstractMarker.MarkerType.CLUSTER_MARKER;
            case SELECTED_ONLY:
                return AbstractMarker.MarkerType.SELECTED;
            default:
                return AbstractMarker.MarkerType.NO_MARKER;
        }
    }

    public void setLonFieldText(final String longitude) {
        lonField.setText(longitude);
    }

    public void setLatFieldText(final String lattitude) {
        latField.setText(lattitude);
    }

    /**
     * Add/remove layer to the map
     *
     * @param key - key specifying the layer
     * @param id - a new id for the layer if it is going to be working
     * @param isChecked - is the item is checked or not
     */
    private void addLayer(final String key, final int id, final boolean isChecked) {
        if (isChecked && !layerMap.containsKey(key)) {
            mapView.addLayer(getLayerFromKey(key));
            layerMap.put(key, id);
        } else if (!isChecked && layerMap.containsKey(key)) {
            mapView.removeLayer(layerMap.get(key));
            layerMap.remove(key);
        }
    }

    /**
     * Create a map layer based on a key
     *
     * @param key - key specifying which layer to create
     * @return
     */
    private AbstractMapLayer getLayerFromKey(final String key) {
        switch (key) {
            case DAY_NIGHT:
                return new DayNightLayer(mapView, layerId++);
            case HEATMAP_STANDARD:
                return new StandardHeatmapLayer(mapView, layerId++);
            case HEATMAP_POPULARITY:
                return new PopularityHeatmapLayer(mapView, layerId++);
            case HEATMAP_ACTIVITY:
                return new ActivityHeatmapLayer(mapView, layerId++);
            case ENTITY_PATHS:
                return new EntityPathsLayer(mapView, layerId++, mapView.getAllMarkers());
            case LOCATION_PATHS:
                return new LocationPathsLayer(mapView, layerId++, mapView.getAllMarkers());
            case THIESSEAN_POLYGONS:
                return new ThiessenPolygonsLayer2(mapView, layerId++, mapView.getAllMarkersAsList());
            default:
                break;
        }

        return null;
    }

    public MapViewTopComponent getParentComponent()    {
        return parent;
    }

    public int getNewMarkerID() {
        return parent.getNewMarkerID();
    }


    public MapView getMap() {
        return mapView;
    }

    /**
     * Set up the map and view-port rectangle Add components to stack pane and
     * scroll pane
     */
    public void setUpMap() {
        // Create the actual map display area
        parentStackPane.getChildren().add(mapView);

        AnchorPane.setTopAnchor(parentStackPane, 0.0);
        AnchorPane.setRightAnchor(parentStackPane, 0.0);
        AnchorPane.setLeftAnchor(parentStackPane, 0.0);
        AnchorPane.setBottomAnchor(parentStackPane, 0.0);

        AnchorPane.setTopAnchor(mapView.TOOLS_OVERLAY.getOverlayPane(), 5.0);
        AnchorPane.setLeftAnchor(mapView.TOOLS_OVERLAY.getOverlayPane(), 5.0);

        AnchorPane.setBottomAnchor(mapView.getOverviewOverlay().getOverlayPane(), 20.0);
        AnchorPane.setRightAnchor(mapView.getOverviewOverlay().getOverlayPane(), 20.0);

        anchorPane.getChildren().addAll(parentStackPane, mapView.TOOLS_OVERLAY.getOverlayPane(), mapView.getOverviewOverlay().getOverlayPane());
        anchorPane.prefWidthProperty().bind(this.widthProperty());

        Platform.runLater(() -> setCenter(anchorPane));
    }


    public StackPane getParentStackPane() {
        return parentStackPane;
    }

    /**
     * Redraw queried markers
     */
    public void redrawQueriedMarkers() {
        mapView.redrawQueriedMarkers();
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return mapView != null ? mapView.getAllMarkers() : Collections.emptyMap();
    }

    public Graph getCurrentGraph() {
        return parent.getCurrentGraph();
    }

    /**
     * draw marker on the map
     *
     * @param marker - marker to be added
     */
    public void drawMarker(final AbstractMarker marker) {
        if (marker != null && mapView != null) {
            mapView.drawMarker(marker);

        }
    }
}

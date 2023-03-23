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
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.mapview.exporters.GeoJsonExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.GeoPackageExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.KmlExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter.MapExporterWrapper;
import au.gov.asd.tac.constellation.views.mapview.exporters.ShapefileExporter;
import au.gov.asd.tac.constellation.views.mapview.layers.MapLayer;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ActivityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.EntityPathsLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.LocationPathsLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ThiessenPolygonsLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ThiessenPolygonsLayer2;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.controlsfx.control.CheckComboBox;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * MapViewPane holds container of the MapView and the entire toolbar All toolbar
 * options are set here and event handlers for toolbar options call functions in
 * MapView class to manipulate the map
 *
 * @author altair1673
 */
public class MapViewPane extends BorderPane {

    private final MapViewTopComponent parent;
    private final GridPane toolBarGridPane;
    // Stackpane to hold the map
    private final StackPane parentStackPane;

    // Rectangle to repesent the view port
    private final Rectangle viewPortRectangle;
    // String for all the menu options
    private static final String MARKER_TYPE_POINT = "Point Markers";
    private static final String MARKER_TYPE_LINE = "Line Markers";
    private static final String MARKER_TYPE_POLYGON = "Polygon Markers";
    private static final String MARKER_TYPE_MULTI = "Multi-Markers";
    private static final String MARKER_TYPE_CLUSTER = "Cluster Markers";
    private static final String SELECTED_ONLY = "Selected Only";
    private static final String ZOOM_ALL = "Zoom to All";
    private static final String ZOOM_SELECTION = "Zoom to Selection";
    private static final String ZOOM_LOCATION = "Zoom to Location";

    private static final String DAY_NIGHT = "Day / Night";
    private static final String HEATMAP_STANDARD = "Heatmap(Standard)";
    private static final String HEATMAP_POPULARITY = "Heatmap(Popularity)";
    private static final String HEATMAP_ACTIVITY = "Heatmap(Activity)";
    private static final String ENTITY_PATHS = "Entity Paths";
    private static final String LOCATION_PATHS = "Location Paths";
    private static final String THIESSEAN_POLYGONS = "Thiessean Polygons";
    private static final String THIESSEAN_POLYGONS_2 = "Thiessean Polygons 2";

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

    // Map providers
    private final MapProvider defaultProvider;
    private final List<? extends MapProvider> providers;

    // All the toolbar UI elements
    private final ChoiceBox<MapProvider> mapProviderDropDown;
    private final MenuButton zoomDropDown;
    private final CheckComboBox<String> markerDropDown;
    private final CheckComboBox<String> layersDropDown;
    private final CheckComboBox<String> overlaysDropDown;
    private final ChoiceBox<String> colourDropDown;
    private final ChoiceBox<String> markerLabelDropDown;
    private final ComboBox<String> exportDropDown;
    private final Button helpButton;
    private final List<String> dropDownOptions = new ArrayList<>();
    private final Label latLabel = new Label("Lattitude: ");
    private final Label lonLabel = new Label("Longitude: ");
    private final Label latField = new Label("0.00");
    private final Label lonField = new Label("0.00");

    private MapView mapView;

    // A map of all the layers
    private int layerId = 0;
    private final Map<String, Integer> layerMap = new HashMap<>();
    
    public MapViewPane(final MapViewTopComponent parentComponent) {
        parent = parentComponent;

        parentStackPane = new StackPane();
        viewPortRectangle = new Rectangle();

        viewPortRectangle.setMouseTransparent(true);

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

        defaultProvider = Lookup.getDefault().lookup(MapProvider.class);
        providers = new ArrayList<>(Lookup.getDefault().lookupAll(MapProvider.class));

        // get all the map types in string form from the providers
        providers.forEach(p -> dropDownOptions.add(p.toString()));

        // Add the providers to the toolbar
        mapProviderDropDown = new ChoiceBox(FXCollections.observableList(providers));
        mapProviderDropDown.getSelectionModel().selectFirst();
        mapProviderDropDown.setTooltip(new Tooltip("Select a basemap for the Map View"));

        final List<? extends MapLayer> layers = new ArrayList<>(Lookup.getDefault().lookupAll(MapLayer.class));
        setDropDownOptions(layers);

        // Add all the layers to the toolbar
        layersDropDown = new CheckComboBox<>(FXCollections.observableArrayList(DAY_NIGHT, HEATMAP_STANDARD, HEATMAP_POPULARITY, HEATMAP_ACTIVITY, ENTITY_PATHS, LOCATION_PATHS, THIESSEAN_POLYGONS, THIESSEAN_POLYGONS_2));
        layersDropDown.setTitle("Layers");
        layersDropDown.setTooltip(new Tooltip("Select layers to render over the map in the Map View"));
        layersDropDown.setMinWidth(85);
        layersDropDown.setMaxWidth(85);

        // Event handler for selecting different layers to show
        layersDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(final ListChangeListener.Change<? extends String> c) {
                layersDropDown.getItems().forEach(item -> addLayer(item, layerId));
            }
        });

        // Add overlays to toolbar
        overlaysDropDown = new CheckComboBox<>(FXCollections.observableArrayList(INFO_OVERLAY, TOOLS_OVERLAY));
        overlaysDropDown.setTitle("Overlays");
        overlaysDropDown.setTooltip(new Tooltip("Select overlays to render over the map in the Map View"));
        overlaysDropDown.setMinWidth(95);
        overlaysDropDown.setMaxWidth(95);

        // Overlay event handler
        overlaysDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(final ListChangeListener.Change<? extends String> c) {
                overlaysDropDown.getItems().forEach(item -> toggleOverlay(item));
                if (overlaysDropDown.getCheckModel().isChecked(INFO_OVERLAY)) {
                    toolBarGridPane.add(latLabel, 0, 1);
                    toolBarGridPane.add(latField, 1, 1);
                    toolBarGridPane.add(lonLabel, 2, 1);
                    toolBarGridPane.add(lonField, 3, 1);

                } else if (!overlaysDropDown.getCheckModel().isChecked(INFO_OVERLAY)) {
                    toolBarGridPane.getChildren().removeAll(latLabel, latField, lonLabel, lonField);
                }

            }
        });

        // Zoom menu set up and event handling
        zoomDropDown = new MenuButton("Zoom");
        final MenuItem zoomAll = new MenuItem(ZOOM_ALL);
        final MenuItem zoomSelection = new MenuItem(ZOOM_SELECTION);
        final MenuItem zoomLocation = new MenuItem(ZOOM_LOCATION);

        zoomDropDown.getItems().addAll(zoomAll, zoomSelection, zoomLocation);
        zoomDropDown.setTooltip(new Tooltip("Zoom based on markers or locations in the Map View"));


        zoomAll.setOnAction(event -> {
            mapView.panToCenter();
            mapView.panToAll();
        });

        zoomSelection.setOnAction(event -> {
            mapView.panToCenter();
            mapView.panToSelection();
        });

        zoomLocation.setOnAction(event -> mapView.generateZoomLocationUI());

        // Menu to show/hide markers
        markerDropDown = new CheckComboBox<>(FXCollections.observableArrayList(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI, MARKER_TYPE_CLUSTER, SELECTED_ONLY));
        markerDropDown.setTitle("Markers");
        markerDropDown.setTooltip(new Tooltip("Choose which markers are displayed in the Map View"));
        markerDropDown.getCheckModel().check(MARKER_TYPE_POINT);
        markerDropDown.getCheckModel().check(MARKER_TYPE_LINE);
        markerDropDown.getCheckModel().check(MARKER_TYPE_POLYGON);
        markerDropDown.setMinWidth(90);
        markerDropDown.setMaxWidth(90);

        // Event handler for hiding/showing markers
        markerDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(final ListChangeListener.Change<? extends String> c) {
                markerDropDown.getItems().forEach(item -> {

                    if (markerDropDown.getCheckModel().isChecked(item)) {
                        if (item.equals(MARKER_TYPE_POINT)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.POINT_MARKER, true);
                        } else if (item.equals(MARKER_TYPE_LINE)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.LINE_MARKER, true);
                        } else if (item.equals(MARKER_TYPE_POLYGON)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.POLYGON_MARKER, true);
                        } else if (item.equals(MARKER_TYPE_CLUSTER)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.CLUSTER_MARKER, true);
                        } else if (item.equals(SELECTED_ONLY)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.SELECTED, true);
                        }

                        } else {
                        if (item.equals(MARKER_TYPE_POINT)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.POINT_MARKER, false);
                        } else if (item.equals(MARKER_TYPE_LINE)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.LINE_MARKER, false);
                        } else if (item.equals(MARKER_TYPE_POLYGON)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.POLYGON_MARKER, false);
                        } else if (item.equals(MARKER_TYPE_CLUSTER)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.CLUSTER_MARKER, false);
                        } else if (item.equals(SELECTED_ONLY)) {
                            mapView.updateShowingMarkers(AbstractMarker.MarkerType.SELECTED, false);
                        }

                        }

                });

            }
        });

        // Marker colour mneu setup and event handling
        colourDropDown = new ChoiceBox<>(FXCollections.observableList(Arrays.asList(DEFAULT_COLOURS, USE_COLOUR_ATTR, USE_OVERLAY_COL, USE_BLAZE_COL)));
        colourDropDown.getSelectionModel().selectFirst();
        colourDropDown.setTooltip(new Tooltip("Chose the color scheme for markers displayed in the Map View"));

        colourDropDown.setOnAction(event -> mapView.getMarkerColourProperty().set(colourDropDown.getValue()));
        colourDropDown.setMinWidth(120);
        colourDropDown.setMaxWidth(120);

        // Marker label menu setup and event handling
        markerLabelDropDown = new ChoiceBox<>(FXCollections.observableList(Arrays.asList(NO_LABELS, USE_LABEL_ATTR, USE_IDENT_ATTR)));
        markerLabelDropDown.getSelectionModel().selectFirst();
        markerLabelDropDown.setTooltip(new Tooltip("Chose the label for markers displayed in the Map View"));

        markerLabelDropDown.setOnAction(event -> mapView.getMarkerTextProperty().set(markerLabelDropDown.getValue()));
        markerLabelDropDown.setMinWidth(95);
        markerLabelDropDown.setMaxWidth(95);

        // Export menu setup and eventer handling
        exportDropDown = new ComboBox(FXCollections.observableList(Arrays.asList("Export", GEO_JSON, GEO_PACKAGE, KML, SHAPEFILE)));
        exportDropDown.getSelectionModel().selectFirst();

        exportDropDown.setOnAction(event -> {
            if (parent.getCurrentGraph() != null) {
                MapExporterWrapper exporterWrapper = null;

                String selectedItem = exportDropDown.getSelectionModel().getSelectedItem();
                if (selectedItem.equals(GEO_JSON)) {
                    exporterWrapper = new MapExporterWrapper(new GeoJsonExporter());
                } else if (selectedItem.equals(KML)) {
                    exporterWrapper = new MapExporterWrapper(new KmlExporter());
                } else if (selectedItem.equals(SHAPEFILE)) {
                    exporterWrapper = new MapExporterWrapper(new ShapefileExporter());
                } else if (selectedItem.equals(GEO_PACKAGE)) {
                    exporterWrapper = new MapExporterWrapper(new GeoPackageExporter());
                }

                PluginExecution
                        .withPlugin(exporterWrapper.getExporter().getPluginReference())
                        .interactively(true)
                        .executeLater(parent.getCurrentGraph());
            } else {
                NotifyDisplayer.display("Export options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });
        exportDropDown.setTooltip(new Tooltip("Export from the Map View"));
        exportDropDown.setMinWidth(110);
        exportDropDown.setMaxWidth(110);

        helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.setOnAction(event -> new HelpCtx(this.getClass().getName()).display());
        helpButton.setTooltip(new Tooltip("Help on using the Map View"));

        toolBarGridPane.add(mapProviderDropDown, 0, 0);
        toolBarGridPane.add(layersDropDown, 1, 0);
        toolBarGridPane.add(overlaysDropDown, 2, 0);
        toolBarGridPane.add(zoomDropDown, 3, 0);
        toolBarGridPane.add(markerDropDown, 4, 0);
        toolBarGridPane.add(colourDropDown, 5, 0);
        toolBarGridPane.add(markerLabelDropDown, 6, 0);
        toolBarGridPane.add(exportDropDown, 7, 0);
        toolBarGridPane.add(helpButton, 8, 0);
        setTop(toolBarGridPane);
    }

    /**
     * Hide/Show overlay
     *
     * @param overlay - string representing the overlay
     */
    private void toggleOverlay(final String overlay) {
        mapView.toggleOverlay(overlay, overlaysDropDown.getCheckModel().isChecked(overlay));
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
     */
    private void addLayer(final String key, final int id) {
        if (layersDropDown.getCheckModel().getCheckedItems().contains(key) && !layerMap.containsKey(key)) {
            mapView.addLayer(getLayerFromKey(key));
            layerMap.put(key, id);
        } else if (!layersDropDown.getCheckModel().getCheckedItems().contains(key) && layerMap.containsKey(key)) {
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
                return new ThiessenPolygonsLayer(mapView, layerId++, mapView.getAllMarkersAsList());
            case THIESSEAN_POLYGONS_2:
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
        mapView = new MapView(this);
        parentStackPane.getChildren().add(mapView);

        // Set position of "viewport" rectangle
        viewPortRectangle.setX(0);
        viewPortRectangle.setY(0);

        parentStackPane.getChildren().add(mapView.TOOLS_OVERLAY.getOverlayPane());

        parentStackPane.setLayoutX(0);
        parentStackPane.setLayoutY(0);

        viewPortRectangle.setWidth(MapView.MAP_WIDTH);
        viewPortRectangle.setHeight(MapView.MAP_HEIGHT);

        viewPortRectangle.setFill(Color.TRANSPARENT);
        viewPortRectangle.setStroke(Color.TRANSPARENT);

        // Adds the mapView and viewport rect underneath the toolbar
        parentStackPane.getChildren().add(viewPortRectangle);
        Platform.runLater(() -> setCenter(parentStackPane));

    }

    public StackPane getParentStackPane() {
        return parentStackPane;
    }

    public Rectangle getViewPortRectangle() {
        return viewPortRectangle;
    }

    /**
     * Redraw queried markers
     */
    public void redrawQueriedMarkers() {
        mapView.redrawQueriedMarkers();
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        if (mapView != null) {
            return mapView.getAllMarkers();
        }

        return new HashMap<>();
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

    public MapProvider getDefaultProvider() {
        return defaultProvider;
    }


    public List<? extends MapProvider> getProviders() {
        return new ArrayList<>(providers);
    }

    private void setDropDownOptions(final List<?> options) {
        dropDownOptions.clear();
        options.forEach(o -> dropDownOptions.add(o.toString()));
    }
}

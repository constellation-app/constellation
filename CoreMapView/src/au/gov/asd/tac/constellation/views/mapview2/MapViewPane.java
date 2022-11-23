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
import au.gov.asd.tac.constellation.plugins.gui.MultiChoiceInputPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
//import au.gov.asd.tac.constellation.views.mapview2.MapViewTileRenderer;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter.MapExporterWrapper;
import au.gov.asd.tac.constellation.views.mapview.layers.MapLayer;
import au.gov.asd.tac.constellation.views.mapview.overlays.MapOverlay;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import au.gov.asd.tac.constellation.views.mapview2.layers.AbstractMapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.ActivityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.DayNightLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.PopularityHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.layers.StandardHeatmapLayer;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.openide.util.Lookup;

/**
 *
 * @author altair1673
 */
public class MapViewPane extends BorderPane {

    private final MapViewTopComponent parent;
    private final ToolBar toolBar;

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
    private static final String POINT_MARKER_ERROR = "Point Marker Error Region (Experimental)";

    public static final String INFO_OVERLAY = "Info Overlay";
    public static final String TOOLS_OVERLAY = "Tools Overlay";
    public static final String OVERVIEW_OVERLAY = "Overview Overlay";


    private final MapProvider defaultProvider;
    private final List<? extends MapProvider> providers;
    private final List<? extends MapExporter> exporters;
    private final MarkerState markerState = null;

    private final ChoiceBox<MapProvider> mapProviderDropDown;
    private final MenuButton zoomDropDown;
    private final CheckComboBox<String> markerDropDown;
    private final CheckComboBox<String> layersDropDown;
    private final CheckComboBox<String> overlaysDropDown;
    private final ChoiceBox colourDropDown;
    private final ChoiceBox markerLabelDropDown;
    private final ComboBox exportDropDown;
    private final Button helpButton;

    private MapView mapView;

    private int layerId = 0;
    private Map<String, Integer> layerMap = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger("MapViewPane");

    //private final Consumer<Graph> updateMarkers;
    //private au.gov.asd.tac.constellation.views.mapview2.MapViewTileRenderer renderer = null;

    private final List<String> dropDownOptions = new ArrayList<>();

    public MapViewPane(final MapViewTopComponent parentComponent) {
        parent = parentComponent;

        toolBar = new ToolBar();

        defaultProvider = Lookup.getDefault().lookup(MapProvider.class);
        providers = new ArrayList<>(Lookup.getDefault().lookupAll(MapProvider.class));
        exporters = new ArrayList<>(Lookup.getDefault().lookupAll(MapExporter.class));


        providers.forEach((p) -> {
            dropDownOptions.add(p.toString());
        });

        mapProviderDropDown = new ChoiceBox(FXCollections.observableList(providers));
        mapProviderDropDown.getSelectionModel().selectFirst();
        mapProviderDropDown.setTooltip(new Tooltip("Select a basemap for the Map View"));

        final List<? extends MapLayer> layers = new ArrayList<>(Lookup.getDefault().lookupAll(MapLayer.class));
        setDropDownOptions(layers);

        layersDropDown = new CheckComboBox(FXCollections.observableArrayList(DAY_NIGHT, HEATMAP_STANDARD, HEATMAP_POPULARITY, HEATMAP_ACTIVITY, ENTITY_PATHS, LOCATION_PATHS, THIESSEAN_POLYGONS, POINT_MARKER_ERROR));
        layersDropDown.setTitle("Layers");
        layersDropDown.setTooltip(new Tooltip("Select layers to render over the map in the Map View"));

        layersDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                layersDropDown.getItems().forEach(item -> {

                    addLayer(item, layerId);
                });

            }
        });


        overlaysDropDown = new CheckComboBox(FXCollections.observableArrayList(INFO_OVERLAY, OVERVIEW_OVERLAY, TOOLS_OVERLAY));
        overlaysDropDown.setTitle("Overlays");
        overlaysDropDown.setTooltip(new Tooltip("Select overlays to render over the map in the Map View"));

        overlaysDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {

                overlaysDropDown.getItems().forEach(item -> {

                    if (overlaysDropDown.getCheckModel().isChecked(item)) {
                        mapView.toggleOverlay(item, true);
                    } else {
                        mapView.toggleOverlay(item, false);
                    }

                });

            }
        });

        zoomDropDown = new MenuButton("Zoom");
        zoomDropDown.getItems().addAll(new MenuItem(ZOOM_ALL), new MenuItem(ZOOM_SELECTION), new MenuItem(ZOOM_LOCATION));
        zoomDropDown.setTooltip(new Tooltip("Zoom based on markers or locations in the Map View"));

        markerDropDown = new CheckComboBox(FXCollections.observableArrayList(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI, MARKER_TYPE_CLUSTER, SELECTED_ONLY));
        markerDropDown.setTitle("Markers");
        markerDropDown.setTooltip(new Tooltip("Choose which markers are displayed in the Map View"));
        markerDropDown.getCheckModel().check(MARKER_TYPE_POINT);
        markerDropDown.getCheckModel().check(MARKER_TYPE_LINE);
        markerDropDown.getCheckModel().check(MARKER_TYPE_POLYGON);
        //markerDropDown.getCheckModel().check(MARKER_TYPE_MULTI);
        markerDropDown.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
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
                        }
                        }

                });

            }
        });

        colourDropDown = new ChoiceBox<>(FXCollections.observableList(Arrays.asList(MarkerState.MarkerColorScheme.values())));
        colourDropDown.getSelectionModel().selectFirst();
        colourDropDown.setTooltip(new Tooltip("Chose the color scheme for markers displayed in the Map View"));

        markerLabelDropDown = new ChoiceBox<>(FXCollections.observableList(Arrays.asList(MarkerState.MarkerLabel.values())));
        markerLabelDropDown.getSelectionModel().selectFirst();
        markerLabelDropDown.setTooltip(new Tooltip("Chose the label for markers displayed in the Map View"));

        final List<MapExporterWrapper> exporterWrappers = exporters.stream().map(MapExporterWrapper::new).collect(Collectors.toList());
        exportDropDown = new ComboBox(FXCollections.observableList(exporterWrappers));
        exportDropDown.setTooltip(new Tooltip("Export from the Map View"));

        helpButton = new Button("Help");

        toolBar.getItems().addAll(mapProviderDropDown, layersDropDown, overlaysDropDown, zoomDropDown, markerDropDown, colourDropDown, markerLabelDropDown, exportDropDown, helpButton);
        setTop(toolBar);
    }

    private void addLayer(String key, int id) {
        if (layersDropDown.getCheckModel().getCheckedItems().contains(key) && !layerMap.containsKey(key)) {
            LOGGER.log(Level.SEVERE, "Adding layer: " + key);
            mapView.addLayer(getLayerFromKey(key));
            layerMap.put(key, id);
        } else if (!layersDropDown.getCheckModel().getCheckedItems().contains(key) && layerMap.containsKey(key)) {
            LOGGER.log(Level.SEVERE, "Removing layer: " + key);
            mapView.removeLayer(layerMap.get(key));
            layerMap.remove(key);
        }
    }

    private AbstractMapLayer getLayerFromKey(String key) {
        switch (key) {
            case DAY_NIGHT:
                return new DayNightLayer(mapView, layerId++);
            case HEATMAP_STANDARD:
                return new StandardHeatmapLayer(mapView, layerId++);
            case HEATMAP_POPULARITY:
                return new PopularityHeatmapLayer(mapView, layerId++);
            case HEATMAP_ACTIVITY:
                return new ActivityHeatmapLayer(mapView, layerId++);
            default:
                break;
        }

        return null;
    }

    public MapViewTopComponent getParentComponent()    {
        return parent;
    }


    public MapView getMap() {
        return mapView;
    }

    public void setUpMap() {
        mapView = new MapView(this);
        Platform.runLater(() -> {
            setCenter(mapView);

        });

    }

    public void redrawQueriedMarkers() {
        mapView.redrawQueriedMarkers();
    }

    public void toggleHeatmapLayer() {
        //mapView.toggleHeatmapLayer();
    }

    public Map<String, AbstractMarker> getAllMarkers() {
        if (mapView != null) {
            return mapView.getAllMarkers();
        }

        return new HashMap<String, AbstractMarker>();
    }

    private void testEventHandler() {
        LOGGER.log(Level.SEVERE, "test");
    }

    public Graph getCurrentGraph() {
        return parent.getCurrentGraph();
    }

    public void drawMarker(double lat, double lon, double scale) {
        if (mapView != null) {
            //mapView.drawMarker(lat, lon, scale);
        }
    }

    public void drawMarker(AbstractMarker marker) {
        if (marker != null && mapView != null) {
            mapView.drawMarker(marker);

        }
    }

    //public void drawMarker()
    //{
    //}

    public MapProvider getDefaultProvider() {
        return defaultProvider;
    }

    public MarkerState getMarkerState() {
        return markerState;
    }

    public List<? extends MapProvider> getProviders() {
        return providers;
    }

    public void resetContent() {

        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if (renderer != null) {
                    renderer.dispose();
                }

                renderer = new MapViewTileRenderer(parent);
                glComponent = renderer.init();
                parent.setMapImage(glComponent);

                //mapView.setContent((JComponent) glComponent);

                //setCenter(mapView);

            }

        });*/
    }

    private void setDropDownOptions(List<?> options) {
        dropDownOptions.clear();
        options.forEach((o) -> {
            dropDownOptions.add(o.toString());
        });
    }
}

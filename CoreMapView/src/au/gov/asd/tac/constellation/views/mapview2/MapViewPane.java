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

import au.gov.asd.tac.constellation.plugins.gui.MultiChoiceInputPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter.MapExporterWrapper;
import au.gov.asd.tac.constellation.views.mapview.layers.MapLayer;
import au.gov.asd.tac.constellation.views.mapview.overlays.MapOverlay;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckComboBox;
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

    private final List<? extends MapProvider> providers;
    private final List<? extends MapExporter> exporters;

    private final ChoiceBox<MapProvider> mapProviderDropDown;
    private final MenuButton zoomDropDown;
    private final CheckComboBox markerDropDown;
    private final CheckComboBox<MapLayer> layersDropDown;
    private final CheckComboBox<MapOverlay> overlaysDropDown;
    private final ChoiceBox colourDropDown;
    private final ChoiceBox markerLabelDropDown;
    private final ComboBox exportDropDown;
    private final Button helpButton;


    private final List<String> dropDownOptions = new ArrayList<>();

    public MapViewPane(final MapViewTopComponent parentComponent) {
        parent = parentComponent;

        toolBar = new ToolBar();

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

        layersDropDown = new CheckComboBox(FXCollections.observableList(layers));
        layersDropDown.setTitle("Layers");
        layersDropDown.setTooltip(new Tooltip("Select layers to render over the map in the Map View"));

        final List<? extends MapOverlay> overlays = new ArrayList<>(Lookup.getDefault().lookupAll(MapOverlay.class));
        setDropDownOptions(overlays);

        overlaysDropDown = new CheckComboBox(FXCollections.observableList(overlays));
        overlaysDropDown.setTitle("Overlays");
        overlaysDropDown.setTooltip(new Tooltip("Select overlays to render over the map in the Map View"));

        zoomDropDown = new MenuButton("Zoom");
        zoomDropDown.getItems().addAll(new MenuItem(ZOOM_ALL), new MenuItem(ZOOM_SELECTION), new MenuItem(ZOOM_LOCATION));
        zoomDropDown.setTooltip(new Tooltip("Zoom based on markers or locations in the Map View"));

        markerDropDown = new CheckComboBox(FXCollections.observableArrayList(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI, MARKER_TYPE_CLUSTER, SELECTED_ONLY));
        markerDropDown.setTitle("Markers");
        markerDropDown.setTooltip(new Tooltip("Choose which markers are displayed in the Map View"));

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

    private void setDropDownOptions(List<?> options) {
        dropDownOptions.clear();
        options.forEach((o) -> {
            dropDownOptions.add(o.toString());
        });
    }
}

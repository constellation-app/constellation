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
package au.gov.asd.tac.constellation.views.mapview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterController;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.utilities.geospatial.Geohash;
import au.gov.asd.tac.constellation.utilities.geospatial.Mgrs;
import au.gov.asd.tac.constellation.utilities.gui.JDropDownMenu;
import au.gov.asd.tac.constellation.utilities.gui.JMultiChoiceComboBoxMenu;
import au.gov.asd.tac.constellation.utilities.gui.JSingleChoiceComboBoxMenu;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.views.SwingTopComponent;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter;
import au.gov.asd.tac.constellation.views.mapview.exporters.MapExporter.MapExporterWrapper;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature.ConstellationFeatureType;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationShapeFeature;
import au.gov.asd.tac.constellation.views.mapview.layers.MapLayer;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.overlays.MapOverlay;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState.MarkerColorScheme;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState.MarkerLabel;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * The Map View Top Component. This class provides the Map View window and
 * toolbar and handles all interactions with the graph.
 */
@TopComponent.Description(
        preferredID = "MapViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/mapview/resources/map-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.mapview.MapViewTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 700),
    @ActionReference(path = "Shortcuts", name = "CS-M")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapViewAction",
        preferredID = "MapViewTopComponent")
@Messages({
    "CTL_MapViewAction=Map View",
    "CTL_MapViewTopComponent=Map View",
    "HINT_MapViewTopComponent=Map View"})
public final class MapViewTopComponent extends SwingTopComponent<Component> {

    private static final String UPDATE_SELECTION_PLUGIN = "Map View: Update Selection on Graph";
    private static final String MARKER_TYPE_POINT = "Point Markers";
    private static final String MARKER_TYPE_LINE = "Line Markers";
    private static final String MARKER_TYPE_POLYGON = "Polygon Markers";
    private static final String MARKER_TYPE_MULTI = "Multi-Markers";
    private static final String MARKER_TYPE_CLUSTER = "Cluster Markers";
    private static final String SELECTED_ONLY = "Selected Only";
    private static final String ZOOM_ALL = "Zoom to All";
    private static final String ZOOM_SELECTION = "Zoom to Selection";
    private static final String ZOOM_LOCATION = "Zoom to Location";
    private static final String PARAMETER_TYPE = "zoom_to_location.geo_type";
    private static final String PARAMETER_LOCATION = "zoom_to_location.location_id";
    private static final String GEO_TYPE_COORDINATE = "Coordinate";
    private static final String GEO_TYPE_GEOHASH = "Geohash";
    private static final String GEO_TYPE_MGRS = "MGRS";

    private final JToolBar toolBar;
    private final JSingleChoiceComboBoxMenu<MapProvider> mapProviderMenu;
    private final JMultiChoiceComboBoxMenu<MapLayer> layersComboBox;
    private final JMultiChoiceComboBoxMenu<MapOverlay> overlaysComboBox;
    private final JDropDownMenu<String> zoomMenu;
    private final JMultiChoiceComboBoxMenu<String> markerVisibilityComboBox;
    private final JSingleChoiceComboBoxMenu<MarkerColorScheme> markerColorSchemeComboBox;
    private final JSingleChoiceComboBoxMenu<MarkerLabel> markerLabelComboBox;
    private final JDropDownMenu<MapExporterWrapper> exportMenu;
    private final JButton helpButton;
    private MapViewTileRenderer renderer;
    private Component glComponent;

    private final MapProvider defaultProvider;
    private final List<? extends MapProvider> providers;
    private final List<? extends MapExporter> exporters;
    private final MarkerState markerState;
    private int cachedWidth;
    private int cachedHeight;
    private final Consumer<Graph> updateMarkers;

    public MapViewTopComponent() {
        super();

        // initialise top component
        setName(Bundle.CTL_MapViewTopComponent());
        setToolTipText(Bundle.HINT_MapViewTopComponent());
        initComponents();
        initContent();

        // initialise variables
        this.markerState = new MarkerState();
        this.cachedWidth = getWidth();
        this.cachedHeight = getHeight();

        updateMarkers = graph -> renderer.updateMarkers(graph, markerState);

        // lookup map providers
        this.defaultProvider = Lookup.getDefault().lookup(MapProvider.class);
        this.providers = new ArrayList<>(Lookup.getDefault().lookupAll(MapProvider.class));
        this.exporters = new ArrayList<>(Lookup.getDefault().lookupAll(MapExporter.class));

        // initialise toolbar
        this.toolBar = new JToolBar(SwingConstants.HORIZONTAL);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        this.mapProviderMenu = new JSingleChoiceComboBoxMenu(AnalyticIconProvider.MAP.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), providers);
        mapProviderMenu.addSelectionListener(event -> {
            final MapProvider mapProvider = (MapProvider) event.getSource();
            renderer.switchProviders(mapProvider);
            mapProviderMenu.setText(mapProvider.getName());
        });
        mapProviderMenu.setToolTipText("Select a basemap for the Map View");
        toolBar.add(mapProviderMenu);

        final List<? extends MapLayer> layers = new ArrayList<>(Lookup.getDefault().lookupAll(MapLayer.class));
        this.layersComboBox = new JMultiChoiceComboBoxMenu(UserInterfaceIconProvider.MENU.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), layers);
        layersComboBox.addSelectedItems(layers.stream().filter(layer -> layer.isEnabled()).toArray(MapLayer[]::new));
        layersComboBox.addSelectionListener(event -> {
            final MapLayer layer = (MapLayer) event.getSource();
            layer.setEnabled(!layer.isEnabled());
        });
        layersComboBox.setToolTipText("Select layers to render over the map in the Map View");
        toolBar.add(layersComboBox);

        final List<? extends MapOverlay> overlays = new ArrayList<>(Lookup.getDefault().lookupAll(MapOverlay.class));
        this.overlaysComboBox = new JMultiChoiceComboBoxMenu(UserInterfaceIconProvider.TAG.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), overlays);
        overlaysComboBox.addSelectedItems(overlays.stream().filter(overlay -> overlay.isEnabled()).toArray(MapOverlay[]::new));
        overlaysComboBox.addSelectionListener(event -> {
            final MapOverlay overlay = (MapOverlay) event.getSource();
            overlay.setEnabled(!overlay.isEnabled());
        });
        overlaysComboBox.setToolTipText("Select overlays to render over the map in the Map View");
        toolBar.add(overlaysComboBox);

        final List<String> zoomActions = Arrays.asList(ZOOM_ALL, ZOOM_SELECTION, ZOOM_LOCATION);
        this.zoomMenu = new JDropDownMenu<>(UserInterfaceIconProvider.ZOOM_IN.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), zoomActions);
        zoomMenu.addActionListener(event -> {
            if (currentGraph != null) {
                final String zoomAction = (String) event.getSource();
                switch (zoomAction) {
                    case ZOOM_ALL -> renderer.zoomToMarkers(markerState);
                    case ZOOM_SELECTION -> {
                        final MarkerState selectedOnlyState = new MarkerState();
                        selectedOnlyState.setShowSelectedOnly(true);
                        renderer.zoomToMarkers(selectedOnlyState);
                    }
                    case ZOOM_LOCATION -> {
                        final PluginParameters zoomParameters = createParameters();
                        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(ZOOM_LOCATION, zoomParameters);
                        dialog.showAndWait();
                        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
                            @SuppressWarnings("unchecked") //the plugin that comes from PARAMETER_TYPE is of type SingleChoiceParameter
                            final PluginParameter<SingleChoiceParameterValue> parameterType = (PluginParameter<SingleChoiceParameterValue>) zoomParameters.getParameters().get(PARAMETER_TYPE);
                            final String geoType = SingleChoiceParameterType.getChoice(parameterType);
                            final String location = zoomParameters.getStringValue(PARAMETER_LOCATION);
                            zoomLocationBasedOnGeoType(geoType, location);
                        }
                    }
                    default -> {
                    }
                }
            } else {
                NotifyDisplayer.display("Zoom options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });
        zoomMenu.setToolTipText("Zoom based on markers or locations in the Map View");
        toolBar.add(zoomMenu);

        final List<String> markerTypes = Arrays.asList(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI, MARKER_TYPE_CLUSTER, SELECTED_ONLY);
        this.markerVisibilityComboBox = new JMultiChoiceComboBoxMenu<>(UserInterfaceIconProvider.VISIBLE.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), markerTypes);
        markerVisibilityComboBox.addSelectedItems(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI);
        markerVisibilityComboBox.addSelectionListener(event -> {
            final Set<String> visibleMarkerTypes = markerVisibilityComboBox.getSelectedItems();
            markerState.setShowPointMarkers(visibleMarkerTypes.contains(MARKER_TYPE_POINT));
            markerState.setShowLineMarkers(visibleMarkerTypes.contains(MARKER_TYPE_LINE));
            markerState.setShowPolygonMarkers(visibleMarkerTypes.contains(MARKER_TYPE_POLYGON));
            markerState.setShowMultiMarkers(visibleMarkerTypes.contains(MARKER_TYPE_MULTI));
            markerState.setShowClusterMarkers(visibleMarkerTypes.contains(MARKER_TYPE_CLUSTER));
            markerState.setShowSelectedOnly(visibleMarkerTypes.contains(SELECTED_ONLY));
            renderer.updateMarkers(currentGraph, markerState);
        });
        markerVisibilityComboBox.setToolTipText("Choose which markers are displayed in the Map View");
        toolBar.add(markerVisibilityComboBox);

        this.markerColorSchemeComboBox = new JSingleChoiceComboBoxMenu<>(UserInterfaceIconProvider.EDIT.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), Arrays.asList(MarkerColorScheme.values()));
        markerColorSchemeComboBox.setSelectedItem(MarkerColorScheme.DEFAULT);
        markerColorSchemeComboBox.addSelectionListener(event -> {
            final MarkerColorScheme colorScheme = (MarkerColorScheme) event.getSource();
            markerState.setColorScheme(colorScheme);

            if (colorScheme.getVertexAttribute() != null) {
                addAttributeValueChangeHandler(colorScheme.getVertexAttribute(), graph -> renderer.updateMarkers(graph, markerState));
            }
            if (colorScheme.getTransactionAttribute() != null) {
                addAttributeValueChangeHandler(colorScheme.getTransactionAttribute(), graph -> renderer.updateMarkers(graph, markerState));
            }

            renderer.updateMarkers(currentGraph, markerState);
        });
        markerColorSchemeComboBox.setToolTipText("Chose the color scheme for markers displayed in the Map View");
        toolBar.add(markerColorSchemeComboBox);

        this.markerLabelComboBox = new JSingleChoiceComboBoxMenu<>(UserInterfaceIconProvider.LABELS.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), Arrays.asList(MarkerLabel.values()));
        markerLabelComboBox.setSelectedItem(MarkerLabel.DEFAULT);
        markerLabelComboBox.addSelectionListener(event -> {
            final MarkerLabel label = (MarkerLabel) event.getSource();
            markerState.setLabel(label);

            if (label.getVertexAttribute() != null) {
                addAttributeValueChangeHandler(label.getVertexAttribute(), graph -> renderer.updateMarkers(graph, markerState));
            }
            if (label.getTransactionAttribute() != null) {
                addAttributeValueChangeHandler(label.getTransactionAttribute(), graph -> renderer.updateMarkers(graph, markerState));
            }

            renderer.updateMarkers(currentGraph, markerState);
        });
        markerLabelComboBox.setToolTipText("Chose the label for markers displayed in the Map View");
        toolBar.add(markerLabelComboBox);

        final List<MapExporterWrapper> exporterWrappers = exporters.stream().map(MapExporterWrapper::new).collect(Collectors.toList());
        this.exportMenu = new JDropDownMenu<>(UserInterfaceIconProvider.DOWNLOAD.buildIcon(16, ConstellationColor.AZURE.getJavaColor()), exporterWrappers);
        exportMenu.addActionListener(event -> {
            if (currentGraph != null) {
                final MapExporterWrapper exporterWrapper = (MapExporterWrapper) event.getSource();
                PluginExecution
                        .withPlugin(exporterWrapper.getExporter().getPluginReference())
                        .interactively(true)
                        .executeLater(currentGraph);
            } else {
                NotifyDisplayer.display("Export options require a graph to be open!", NotifyDescriptor.INFORMATION_MESSAGE);
            }
        });
        exportMenu.setToolTipText("Export from the Map View");
        toolBar.add(exportMenu);

        this.helpButton = new JButton("", UserInterfaceIconProvider.HELP.buildIcon(16, ConstellationColor.AZURE.getJavaColor()));
        helpButton.addActionListener(event -> new HelpCtx(this.getClass().getName()).display());
        helpButton.setToolTipText("Help on using the Map View");
        toolBar.add(helpButton);

        add(toolBar, BorderLayout.NORTH);

        // top component resize listener
        addComponentListener(new ComponentAdapter() {
            ScheduledFuture<?> scheduledFuture;

            @Override
            //Cancels the previous resize (future) and then performs the latest one every half second
            public void componentResized(ComponentEvent event) {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
                scheduledFuture = ConstellationGlobalThreadPool.getThreadPool().getScheduledExecutorService().schedule(() -> {
                    if (event.getComponent().getWidth() != cachedWidth
                            || event.getComponent().getHeight() != cachedHeight) {
                        cachedWidth = event.getComponent().getWidth();
                        cachedHeight = event.getComponent().getHeight();
                        resetContent();
                    }
                    return null;
                }, 500, TimeUnit.MILLISECONDS);
            }
        });

        // add graph structure listener
        addStructureChangeHandler(updateMarkers);

        // add graph visual listeners
        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, updateMarkers);
        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, updateMarkers);
        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.DIMMED, updateMarkers);
        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.DIMMED, updateMarkers);
        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.VISIBILITY, updateMarkers);
        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.VISIBILITY, updateMarkers);

        // add graph geo listeners
        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.LATITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.LONGITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.VertexAttribute.SHAPE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.TransactionAttribute.LATITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.TransactionAttribute.LONGITUDE, updateMarkers);
        addAttributeValueChangeHandler(SpatialConcept.TransactionAttribute.SHAPE, updateMarkers);

        // add state listeners
        if (markerState.getLabel().getVertexAttribute() != null) {
            addAttributeValueChangeHandler(markerState.getLabel().getVertexAttribute(), updateMarkers);
        }
        if (markerState.getLabel().getTransactionAttribute() != null) {
            addAttributeValueChangeHandler(markerState.getLabel().getTransactionAttribute(), updateMarkers);
        }
        if (markerState.getColorScheme().getVertexAttribute() != null) {
            addAttributeValueChangeHandler(markerState.getColorScheme().getVertexAttribute(), updateMarkers);
        }
        if (markerState.getColorScheme().getTransactionAttribute() != null) {
            addAttributeValueChangeHandler(markerState.getColorScheme().getTransactionAttribute(), updateMarkers);
        }
    }

    private void zoomLocationBasedOnGeoType(final String geoType, final String location) throws AssertionError {
        final ConstellationAbstractMarker marker;
        switch (geoType) {
            case GEO_TYPE_COORDINATE -> {
                final String[] coordinate = location.split("[,\\s]+");
                if (coordinate.length != 2 && coordinate.length != 3) {
                    NotifyDisplayer.display("Invalid coordinate syntax provided, should be comma or space separated", NotifyDescriptor.ERROR_MESSAGE);
                    return;
                }
                final float latitude;
                final float longitude;
                final float radius;
                try {
                    latitude = Float.parseFloat(coordinate[0]);
                    longitude = Float.parseFloat(coordinate[1]);
                    if (coordinate.length == 3) {
                        radius = Float.parseFloat(coordinate[2]);
                    } else {
                        radius = 0;
                    }
                } catch (final NumberFormatException ex) {
                    NotifyDisplayer.display("Invalid coordinate data provided, latitude and longitude should be numbers", NotifyDescriptor.ERROR_MESSAGE);
                    return;
                }
                if (latitude <= -90F || latitude >= 90F) {
                    NotifyDisplayer.display("Invalid coordinate data provided, latitude should be in the range [-90. 90]", NotifyDescriptor.ERROR_MESSAGE);
                    return;
                }
                if (longitude <= -180F || longitude >= 180F) {
                    NotifyDisplayer.display("Invalid coordinate data provided, longitude should be in the range [-180, 180]", NotifyDescriptor.ERROR_MESSAGE);
                    return;
                }
                if (radius < 0F) {
                    NotifyDisplayer.display("Invalid coordinate data provided, radius should be greater than or equal to 0", NotifyDescriptor.ERROR_MESSAGE);
                    return;
                }
                
                final Location coordinateLocation = new Location(latitude, longitude);
                if (radius > 0) {
                    final float radiusDD = (float) Distance.Haversine.kilometersToDecimalDegrees(radius);
                    final Location coordinateDelta = new Location(coordinateLocation.x + radiusDD, coordinateLocation.y + radiusDD);
                    final List<Location> circleVertices = MarkerUtilities.generateCircle(coordinateLocation, coordinateDelta);
                    final ConstellationShapeFeature coordinateFeature = new ConstellationShapeFeature(ConstellationFeatureType.POLYGON);
                    circleVertices.forEach(vertex -> coordinateFeature.addLocation(vertex));
                    marker = renderer.addCustomMarker(coordinateFeature);
                } else {
                    final ConstellationPointFeature coordinateFeature = new ConstellationPointFeature(coordinateLocation);
                    marker = renderer.addCustomMarker(coordinateFeature);
                }
            }
            case GEO_TYPE_GEOHASH -> {
                final double[] geohashCoordinates = Geohash.decode(location, Geohash.Base.B16);
                final ConstellationShapeFeature geohashFeature = new ConstellationShapeFeature(ConstellationFeatureType.POLYGON);
                geohashFeature.addLocation(new Location(geohashCoordinates[0] - geohashCoordinates[2], geohashCoordinates[1] - geohashCoordinates[3]));
                geohashFeature.addLocation(new Location(geohashCoordinates[0] + geohashCoordinates[2], geohashCoordinates[1] - geohashCoordinates[3]));
                geohashFeature.addLocation(new Location(geohashCoordinates[0] + geohashCoordinates[2], geohashCoordinates[1] + geohashCoordinates[3]));
                geohashFeature.addLocation(new Location(geohashCoordinates[0] - geohashCoordinates[2], geohashCoordinates[1] + geohashCoordinates[3]));
                geohashFeature.addLocation(new Location(geohashCoordinates[0] - geohashCoordinates[2], geohashCoordinates[1] - geohashCoordinates[3]));
                marker = renderer.addCustomMarker(geohashFeature);
            }
            case GEO_TYPE_MGRS -> {
                final double[] mgrsCoordinates = Mgrs.decode(location);
                final Location mgrsLocation = new Location(mgrsCoordinates[0], mgrsCoordinates[1]);
                final ConstellationPointFeature mgrsFeature = new ConstellationPointFeature(mgrsLocation);
                marker = renderer.addCustomMarker(mgrsFeature);
            }
            default -> marker = null;
        }
        renderer.zoomToLocation(marker == null ? null : marker.getLocation());
    }

    private PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> geoTypeParameter = SingleChoiceParameterType.build(PARAMETER_TYPE);
        geoTypeParameter.setName("Geo Type");
        SingleChoiceParameterType.setOptions(geoTypeParameter, Arrays.asList(GEO_TYPE_COORDINATE, GEO_TYPE_GEOHASH, GEO_TYPE_MGRS));
        SingleChoiceParameterType.setChoice(geoTypeParameter, GEO_TYPE_COORDINATE);
        parameters.addParameter(geoTypeParameter);

        final PluginParameter<StringParameterValue> locationParameter = StringParameterType.build(PARAMETER_LOCATION);
        locationParameter.setName("Location");
        locationParameter.setDescription("Enter a coordinate in decimal degrees (and optionally "
                + "a radius in kilometers) with components separated by spaces or commas");
        locationParameter.setStringValue(null);
        parameters.addParameter(locationParameter);

        PluginParameterController controller = ((master, params, change) -> {
            @SuppressWarnings("unchecked") //master will need to be of type SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> typedMaster = (PluginParameter<SingleChoiceParameterValue>) master;
            switch (SingleChoiceParameterType.getChoice(typedMaster)) {
                case GEO_TYPE_COORDINATE -> params.get(PARAMETER_LOCATION)
                            .setDescription("Enter a coordinate in decimal degrees (and optionally a radius "
                                    + "in kilometers) with components separated by spaces or commas");
                case GEO_TYPE_GEOHASH -> params.get(PARAMETER_LOCATION)
                            .setDescription("Enter a base-16 geohash value");
                case GEO_TYPE_MGRS -> params.get(PARAMETER_LOCATION)
                            .setDescription("Enter an MGRS value");
                default -> {
                }
            }
        });
        parameters.addController(PARAMETER_TYPE, controller);

        return parameters;
    }

    public MapProvider getDefaultProvider() {
        return defaultProvider;
    }

    public List<? extends MapProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    public MarkerState getMarkerState() {
        return markerState;
    }

    public int getContentWidth() {
        return getWidth();
    }

    public int getContentHeight() {
        return toolBar == null ? getHeight()
                : getHeight() - toolBar.getHeight();
    }

    public void resetContent() {

        SwingUtilities.invokeLater(() -> {
            scrollPane.setViewportView(null);
            if (renderer != null) {
                renderer.dispose();
            }
            renderer = new MapViewTileRenderer(this);
            glComponent = renderer.init();
            scrollPane.setViewportView(glComponent);

            mapProviderMenu.setSelectedItem(defaultProvider);
            mapProviderMenu.setText(defaultProvider.getName());
            markerVisibilityComboBox.setSelectedItems(MARKER_TYPE_POINT, MARKER_TYPE_LINE, MARKER_TYPE_POLYGON, MARKER_TYPE_MULTI);
            markerColorSchemeComboBox.setSelectedItem(MarkerColorScheme.DEFAULT);
            markerLabelComboBox.setSelectedItem(MarkerLabel.DEFAULT);
            markerState.reset();

            SwingUtilities.updateComponentTreeUI(scrollPane);
        });
    }

    public void selectOnGraph(final GraphElementType graphElementType, final Set<Integer> elementIds) {
        PluginExecution.withPlugin(new SelectOnGraphPlugin(graphElementType, elementIds)).executeLater(getCurrentGraph());
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        resetContent();
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (needsUpdate() && renderer != null) {
            renderer.updateMarkers(currentGraph, markerState);
        }
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        if (renderer != null) {
            renderer.updateMarkers(currentGraph, markerState);
        }
    }

    @Override
    protected int getVerticalScrollPolicy() {
        return ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
    }

    @Override
    protected int getHorizontalScrollPolicy() {
        return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
    }

    @Override
    protected Component createContent() {
        return glComponent;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public boolean shouldUpdate() {
        return this.needsUpdate();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    public static class SelectOnGraphPlugin extends SimpleEditPlugin {

        private final GraphElementType graphElementType;
        private final Set<Integer> elementIds;

        public SelectOnGraphPlugin(final GraphElementType graphElementType, final Set<Integer> elementIds) {
            this.graphElementType = graphElementType;
            this.elementIds = elementIds;
        }

        @Override
        public String getName() {
            return MapViewTopComponent.UPDATE_SELECTION_PLUGIN;
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            switch (graphElementType) {
                case VERTEX -> {
                    final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
                    final int vertexCount = graph.getVertexCount();
                    for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                        final int vertexId = graph.getVertex(vertexPosition);
                        graph.setBooleanValue(vertexSelectedAttribute, vertexId, elementIds.contains(vertexId));
                    }
                }
                case TRANSACTION -> {
                    final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
                    final int transactionCount = graph.getTransactionCount();
                    for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                        final int transactionId = graph.getTransaction(transactionPosition);
                        graph.setBooleanValue(transactionSelectedAttribute, transactionId, elementIds.contains(transactionId));
                    }
                }
                default -> {
                }
            }
        }

    }

}

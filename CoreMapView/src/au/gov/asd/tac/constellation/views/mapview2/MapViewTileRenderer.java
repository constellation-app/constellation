/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview2.MapLayer;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationClusterMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationMarkerFactory;
import au.gov.asd.tac.constellation.views.mapview2.MapOverlay;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerCache;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.ui.BarScaleUI;
import de.fhpotsdam.unfolding.utils.MapUtils;
import java.applet.Applet;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ThreadUtils;
import org.openide.util.Lookup;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PSurface;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.javafx.PSurfaceFX;

/**
 * The renderer for the Map View. This class uses Processing to render map tiles
 * using OpenGL, and handles all interactions within the map.
 *
 * @author cygnus_x-1
 */
public class MapViewTileRenderer extends PApplet {

    private static final Logger LOGGER = Logger.getLogger(MapViewTileRenderer.class.getName());

    public static final Object LOCK = new Object();
    private static final int DEFAULT_ZOOM = 4;
    private static final Location DEFAULT_LOCATION = new Location(3.0, 126.0);

    private final au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent parent;
    private final MarkerCache markerCache;
    private ConstellationMarkerFactory markerFactory;
    private Component glComponent;
    private MapProvider currentProvider;
    private UnfoldingMap map;
    private EventDispatcher dispatcher;
    private Collection<? extends MapLayer> layers;
    private Collection<? extends MapOverlay> overlays;
    private BarScaleUI barScale;

    private StackPane stackPane;
    private Scene scene;
    private Stage stage;
    private PSurfaceFX surfaceFX;
    private Canvas canvas;

    private boolean boxSelectionEnabled;
    private boolean boxZoomEnabled;
    private int boxOriginX;
    private int boxOriginY;
    private int boxDeltaX;
    private int boxDeltaY;
    private boolean updating = false;

    public MapViewTileRenderer(final au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent parent) {
        this.parent = parent;
        this.markerCache = MarkerCache.getDefault();
        this.currentProvider = parent.getDefaultProvider();

        this.boxSelectionEnabled = false;
        this.boxZoomEnabled = false;
        this.boxOriginX = -1;
        this.boxOriginY = -1;
        this.boxDeltaX = -1;
        this.boxDeltaY = -1;
    }

    public int getDefaultZoom() {
        return DEFAULT_ZOOM;
    }

    public Location getDefaultLocation() {
        return DEFAULT_LOCATION;
    }

    public MarkerCache getMarkerCache() {
        return markerCache;
    }

    public Component getComponent() {
        return glComponent;
    }

    public ConstellationMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    public MarkerState getMarkerState() {
        return parent.getMarkerState();
    }

    public void switchProviders(final MapProvider provider) {
        if (map == null) {
            return;
        }

        currentProvider = provider;
        map.mapDisplay.setMapProvider(provider);
    }

    public void zoomToLocation(final Location location) {
        assert !SwingUtilities.isEventDispatchThread();

        if (map == null) {
            return;
        }

        map.zoomAndPanTo(getDefaultZoom(), location == null ? getDefaultLocation() : location);
    }

    public void zoomToMarkers(final MarkerState markerState) {
        assert !SwingUtilities.isEventDispatchThread();

        if (map == null) {
            return;
        }

        final Set<ConstellationAbstractMarker> markers = map.getMarkers().stream()
                .filter(marker -> !markerState.isShowSelectedOnly() || marker.isSelected())
                .map(ConstellationAbstractMarker.class::cast)
                .collect(Collectors.toSet());

        handleMouseZoom(markers);
    }

    public void selectCustomMarkers(final Set<ConstellationAbstractMarker> markers) {
        assert !SwingUtilities.isEventDispatchThread();

        if (map == null) {
            return;
        }

        // clear custom marker selection
        markerCache.getCustomMarkers().forEach(marker -> {
            if (!marker.isHidden()) {
                marker.setSelected(false);
            }
        });

        // update custom marker selection
        markerCache.getCustomMarkers().forEach(marker -> {
            if (!marker.isHidden() && markers.contains(marker)) {
                marker.setSelected(true);
            }
        });
    }

    public ConstellationAbstractMarker addCustomMarker(final ConstellationAbstractFeature feature) {
        assert !SwingUtilities.isEventDispatchThread();

        if (map == null) {
            return null;
        }

        ConstellationAbstractMarker marker = null;
        try {
            marker = markerFactory.createMarker(feature);
            marker.setCustom(true);
            markerCache.add(marker, GraphElement.NON_ELEMENT);
            synchronized (LOCK) {
                map.addMarker(marker);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return marker;
    }

    private void updateClusters(final MarkerState markerState) {
        assert !SwingUtilities.isEventDispatchThread();

        if (map == null) {
            return;
        }

        synchronized (LOCK) {
            map.getMarkers().removeIf(ConstellationClusterMarker.class::isInstance);
        }

        final Set<ConstellationClusterMarker> clusterMarkers = markerCache.buildClusters(map, markerFactory, markerState);

        synchronized (LOCK) {
            map.addMarkers(new ArrayList<>(clusterMarkers));
        }
    }

    public void updateMarkers(final Graph graph, final MarkerState markerState) {
        if (!parent.shouldUpdate()) {
            return;
        }
        updating = true;

        final Thread thread = new Thread("Map View: Update Markers") {
            @Override
            public void run() {
                if (map == null) {
                    return;
                }

                synchronized (LOCK) {
                    map.getMarkers().clear();
                }

                final Set<ConstellationAbstractMarker> markers = markerCache.buildMarkers(graph, markerFactory);
                markerCache.styleMarkers(graph, markerState);

                synchronized (LOCK) {
                    map.addMarkers(new ArrayList<>(markers));
                }

                updating = false;
            }
        };
        thread.start();
    }

    /**
     * Initialise the tile renderer. As of Processing 3, the {@link PApplet}
     * class no longer extends {@link Applet}, meaning in order to integrate a
     * processing sketch with Swing or JavaFX you must initialise and extract
     * the {@link PSurface} class responsible for rendering the sketch and
     * instead integrate that.
     *
     * @return
     */
    public Component init() {
        // the magical line to make sure all of the jogl files are loaded, preventing Processing loading the wrong libraries
        SharedDrawable.getGLProfile();

        // initialise the processing sketch path
        sketchPath();

        // use reflection to set insideSettings variable to true, initialise
        // settings, and then set insideSettings back to false
        try {
            Field is = PApplet.class.getDeclaredField("insideSettings");
            is.setAccessible(true);
            try {
                is.set(this, Boolean.TRUE);
                settings();
            } finally {
                is.set(this, Boolean.FALSE);
                is = null;
            }
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        // initialise the surface
        if (surface != null) {
            surface.stopThread();
        }
        initSurface();
        surface.startThread();


        // case the surface to the correct component type and return it
        glComponent = new NewtCanvasAWT((GLWindow) surfaceFX.getNative());
        return glComponent;
    }

    /**
     * Note that this method is trying to remove references to the
     * NewtWindowListener as it's preventing the MapViewTileRenderer from being
     * garbage collected. This is still a work in progress.
     */
    @Override
    public void dispose() {
        if (glComponent != null) {
            glComponent = null;
        }
        finished = true;
        if (surface != null && surface.stopThread() && g != null) {
            g.dispose();
        }
        surface = null;

        // initSurface() creates HighResTimerThread threads that don't die so kill them manually
        final Collection<Thread> threadsToKill = ThreadUtils.findThreadsByName("HighResTimerThread");
        for (final Thread thread : threadsToKill) {
            thread.interrupt();
        }
    }

    @Override
    protected PSurface initSurface() {
        PSurface surface = super.initSurface();

        surfaceFX = (PSurfaceFX) surface;
        canvas = (Canvas) surfaceFX.getNative();

        stackPane = (StackPane) canvas.getParent();
        scene = canvas.getScene();
        stage = (Stage) canvas.getScene().getWindow();

        canvas.heightProperty().unbind();
        canvas.widthProperty().unbind();


        stage.setTitle("Test processing thing");

        final MenuItem test1 = new MenuItem("Test 1");
        final MenuItem test2 = new MenuItem("Test 2");

        final Menu menu = new Menu("Exit");
        menu.getItems().add(test1);
        menu.getItems().add(test2);

        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        final VBox vBox = new VBox(menuBar, canvas);

        Scene newScene = new Scene(vBox);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                stage.setScene(newScene);
            }

        });

        return surfaceFX;
    }

    @Override
    public void settings() {
        size(parent.getContentWidth(), parent.getContentHeight(), FX2D);
    }

    @Override
    public void setup() {
        assert !SwingUtilities.isEventDispatchThread();

        markerFactory = new ConstellationMarkerFactory();
        markerFactory.setDefaultColor(MarkerUtilities.DEFAULT_COLOR);
        markerFactory.setDefaultCustomColor(MarkerUtilities.DEFAULT_CUSTOM_COLOR);
        markerFactory.setDefaultHighlightColor(MarkerUtilities.DEFAULT_HIGHLIGHT_COLOR);
        markerFactory.setDefaultSelectColor(MarkerUtilities.DEFAULT_SELECT_COLOR);
        markerFactory.setDefaultStrokeColor(MarkerUtilities.DEFAULT_STROKE_COLOR);
        markerFactory.setDefaultStrokeWeight(MarkerUtilities.DEFAULT_STROKE_WEIGHT);

        map = new UnfoldingMap(this, "Map View", currentProvider);
        map.setTweening(true);

        dispatcher = MapUtils.createDefaultEventDispatcher(this, map);
        // The map library, Unfolding Maps, defaults to a hard-coded left click pan
        dispatcher.unregister(map, PanMapEvent.TYPE_PAN, map.getId());
        dispatcher.unregister(map, ZoomMapEvent.TYPE_ZOOM, map.getId());

        layers = Lookup.getDefault().lookupAll(MapLayer.class);
        layers.forEach(layer -> layer.initialise(this, map));

        overlays = Lookup.getDefault().lookupAll(MapOverlay.class);
        overlays.forEach(overlay -> overlay.initialise(this, map, dispatcher));

        barScale = new BarScaleUI(this, map, 10F, this.getComponent().getHeight() - 10F);

        updateMarkers(parent.getCurrentGraph(), new MarkerState());
        zoomToLocation(null);
    }

    @Override
    public void draw() {
        assert !SwingUtilities.isEventDispatchThread();

        background(0);

        if (updating) {
            // TODO: draw loading animation
        } else {
            map.setZoomRange(0, currentProvider.zoomLevels());
            synchronized (LOCK) {
                map.draw();
            }

            layers.forEach(layer -> layer.setGraph(parent.getCurrentGraph()));
            layers.forEach(layer -> layer.draw());

            overlays.forEach(overlay -> overlay.setDebug(currentProvider.isDebug()));
            overlays.forEach(overlay -> overlay.draw());

            barScale.draw();

            if (boxSelectionEnabled || boxZoomEnabled) {
                final int boxColor = MarkerUtilities.DEFAULT_BOX_COLOR;
                fill(boxColor);
                rect(boxOriginX, boxOriginY, (float) boxDeltaX - boxOriginX, (float) boxDeltaY - boxOriginY);
            }

            updateClusters(parent.getMarkerState());
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        synchronized (LOCK) {
            map.getMarkers().forEach(marker -> ((ConstellationAbstractMarker) marker).setHighlighted(false));
            map.getHitMarkers(mouseX, mouseY).forEach(hitMarker -> ((ConstellationAbstractMarker) hitMarker).setHighlighted(true));
        }
        overlays.forEach(overlay -> overlay.mouseMoved(event));
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        final List<ConstellationAbstractMarker> hitMarkers;
        synchronized (LOCK) {
            hitMarkers = map.getHitMarkers(mouseX, mouseY)
                    .stream().map(ConstellationAbstractMarker.class::cast).collect(Collectors.toList());
        }

        if (event.getButton() == PConstants.LEFT) {
            // select markers
            if (event.getCount() == 2) {
                handleMouseSelection(event, new HashSet<>());
            } else if (!hitMarkers.isEmpty()) {
                handleMouseSelection(event, new HashSet<>(hitMarkers));
            } else {
                // Do nothing
            }
        }

        overlays.forEach(overlay -> overlay.mouseClicked(event));
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        if (event.getButton() == PConstants.CENTER || event.getButton() == PConstants.LEFT) {
            // zoom to box
            boxOriginX = event.getX();
            boxOriginY = event.getY();
        } else if (event.getButton() == PConstants.RIGHT && event.getCount() == 2) {
            dispatcher.register(map, ZoomMapEvent.TYPE_ZOOM, map.getId());
            // Pan + Zoom (order is important)
            final PanMapEvent panMapEvent = new PanMapEvent(this, map.getId());
            final Location location = map.getLocation(mouseX, mouseY);
            panMapEvent.setToLocation(location);
            dispatcher.fireMapEvent(panMapEvent);
            final ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL, 1);
            zoomMapEvent.setTransformationCenterLocation(location);
            dispatcher.fireMapEvent(zoomMapEvent);
            dispatcher.unregister(map, ZoomMapEvent.TYPE_ZOOM, map.getId());
        } else {
            // Do nothing
        }

        overlays.forEach(overlay -> overlay.mousePressed(event));
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        final boolean isOverlayActive = overlays.stream().anyMatch(overlay -> overlay.isActive());
        final boolean isInteractionWithOverlay = overlays.stream().anyMatch(overlay
                -> overlay.isEnabled()
                && mouseX > overlay.getX() && mouseY > overlay.getY()
                && mouseX < overlay.getX() + overlay.getWidth()
                && mouseY < overlay.getY() + overlay.getHeight());

        switch (event.getButton()) {
            case PConstants.CENTER:
                // zoom to box
                boxZoomEnabled = true;
                boxDeltaX = event.getX();
                boxDeltaY = event.getY();
                break;
            case PConstants.RIGHT:
                // select markers
                boxSelectionEnabled = false;

                if (!isInteractionWithOverlay) {
                    // triggers a pan event for a right click & drag if within map
                    dispatcher.register(map, PanMapEvent.TYPE_PAN, map.getId());
                    final Location oldLocation = map.getLocation(pmouseX, pmouseY);
                    final Location newLocation = map.getLocation(mouseX, mouseY);
                    final PanMapEvent panMapEvent = new PanMapEvent(this, map.getId(), PanMapEvent.PAN_BY);
                    panMapEvent.setFromLocation(oldLocation);
                    panMapEvent.setToLocation(newLocation);
                    dispatcher.fireMapEvent(panMapEvent);
                    dispatcher.unregister(map, PanMapEvent.TYPE_PAN, map.getId());
                }
                break;
            case PConstants.LEFT:
                if (!isInteractionWithOverlay && !isOverlayActive) {
                    boxSelectionEnabled = true;
                    boxDeltaX = event.getX();
                    boxDeltaY = event.getY();
                }
                break;
            default:
                break;
        }

        layers.forEach(layer -> layer.mouseDragged(event));
        overlays.forEach(overlay -> overlay.mouseDragged(event));
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        if (event.getButton() == PConstants.CENTER) {
            // zoom to box
            if (boxZoomEnabled) {
                // update the box
                boxDeltaX = event.getX();
                boxDeltaY = event.getY();

                final Set<ConstellationAbstractMarker> markers = new HashSet<>();
                final float minX = Math.min(boxOriginX, boxDeltaX);
                final float minY = Math.min(boxOriginY, boxDeltaY);
                final float maxX = Math.max(boxOriginX, boxDeltaX);
                final float maxY = Math.max(boxOriginY, boxDeltaY);
                try {
                    final ConstellationAbstractMarker topLeftMarker
                            = markerFactory.createMarker(new ConstellationPointFeature(map.getLocation(minX, minY)));
                    final ConstellationAbstractMarker topRightMarker
                            = markerFactory.createMarker(new ConstellationPointFeature(map.getLocation(maxX, minY)));
                    final ConstellationAbstractMarker bottomLeftMarker
                            = markerFactory.createMarker(new ConstellationPointFeature(map.getLocation(minX, maxY)));
                    final ConstellationAbstractMarker bottomRightMarker
                            = markerFactory.createMarker(new ConstellationPointFeature(map.getLocation(maxX, maxY)));
                    markers.add(topLeftMarker);
                    markers.add(topRightMarker);
                    markers.add(bottomLeftMarker);
                    markers.add(bottomRightMarker);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
                handleMouseZoom(markers);

                boxOriginX = -1;
                boxOriginY = -1;
                boxDeltaX = -1;
                boxDeltaY = -1;
                boxZoomEnabled = false;
            }
        } else if (event.getButton() == PConstants.LEFT && boxSelectionEnabled) {
            // select markers
            // update the box
            boxDeltaX = event.getX();
            boxDeltaY = event.getY();

            handleMouseSelection(event, calculateBoxSelection());

            boxOriginX = -1;
            boxOriginY = -1;
            boxDeltaX = -1;
            boxDeltaY = -1;
            boxSelectionEnabled = false;
        } else {
            // Do nothing
        }

        layers.forEach(layer -> layer.mouseReleased(event));
        overlays.forEach(overlay -> overlay.mouseReleased(event));
    }

    @Override
    public void mouseWheel(final MouseEvent event) {
        assert !SwingUtilities.isEventDispatchThread();
        dispatcher.register(map, ZoomMapEvent.TYPE_ZOOM, map.getId());
        layers.forEach(layer -> layer.mouseWheel(event));
        overlays.forEach(overlay -> overlay.mouseWheel(event));

        final boolean isInteractionWithOverlay = overlays.stream().anyMatch(overlay
                -> overlay.isEnabled()
                && mouseX > overlay.getX() && mouseY > overlay.getY()
                && mouseX < overlay.getX() + overlay.getWidth()
                && mouseY < overlay.getY() + overlay.getHeight());

        if (!isInteractionWithOverlay) {
            final ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL);

            // Use location as zoom center, so listening maps can zoom correctly
            final Location location = map.getLocation(mouseX, mouseY);
            zoomMapEvent.setTransformationCenterLocation(location);
            int delta = event.getCount();
            // Zoom in or out
            if (delta < 0) {
                zoomMapEvent.setZoomLevelDelta(1);
            } else if (delta > 0) {
                zoomMapEvent.setZoomLevelDelta(-1);
            } else {
                // Do nothing
            }
            dispatcher.fireMapEvent(zoomMapEvent);
        }

        dispatcher.unregister(map, ZoomMapEvent.TYPE_ZOOM, map.getId());
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        assert !SwingUtilities.isEventDispatchThread();

        if (key == PConstants.ESC) {
            // override exit behaviour
            key = 0;
        } else if (key == PConstants.CODED
                && (keyCode == PConstants.UP
                || keyCode == PConstants.DOWN
                || keyCode == PConstants.LEFT
                || keyCode == PConstants.RIGHT)) {
            // override move behaviour
            key = 0;
        } else {
            // switch map providers
            final int numberKey = key - 48;
            if (numberKey <= parent.getProviders().size()
                    && numberKey > 0 && numberKey < 10) {
                final MapProvider mapProvider = parent.getProviders().get(numberKey - 1);
                if (mapProvider != null) {
                    switchProviders(mapProvider);
                }
            }
        }

        overlays.forEach(overlay -> overlay.keyPressed(event));
    }

    @Override
    public File dataFile(final String where) {
        assert !SwingUtilities.isEventDispatchThread();

        final String resourcePath = "modules/ext/data/";
        if (StringUtils.isBlank(where)) {
            return new File(resourcePath);
        }

        File locatedFile = new File(where);
        if (!locatedFile.isAbsolute()) {
            final String codeNameBase = "au.gov.asd.tac.constellation.views.mapview";
            final String relativePath = resourcePath + where;
            locatedFile = ConstellationInstalledFileLocator.locate(relativePath, codeNameBase, MapViewTileRenderer.class.getProtectionDomain());
            if (locatedFile == null) {
                final URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
                locatedFile = new File(url.toString());

                // Go up two levels to the build\cluster directory so the relative path is correct.
                locatedFile = locatedFile.getParentFile().getParentFile();
                locatedFile = new File(locatedFile, relativePath);

                if (!locatedFile.exists()) {
                    throw new RuntimeException(String.format("Couldn't find file %s at %s in module %s", relativePath, locatedFile, codeNameBase));
                }
            }
        }

        return locatedFile;
    }

    /**
     * Update the zoom level of the map based on the given set of markers.
     *
     * @param event the mouse event which caused the zoom
     * @param markers the markers to zoom to
     */
    private void handleMouseZoom(final Set<ConstellationAbstractMarker> markers) {
        assert !SwingUtilities.isEventDispatchThread();

        if (markers == null) {
            return;
        }

        // the zoomAndPanToFit method is known to break for any of the following locations
        final List<Location> breakingLocations = new ArrayList<>();
        breakingLocations.add(new Location(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        breakingLocations.add(new Location(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
        breakingLocations.add(new Location(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        breakingLocations.add(new Location(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));

        final List<Location> locations = markers.stream()
                .map(marker -> marker.getLocation())
                .filter(location -> !breakingLocations.contains(location))
                .collect(Collectors.toList());

        if (!locations.isEmpty()) {
            map.zoomAndPanToFit(locations);
        }
    }

    /**
     * Update graph and marker selection based on the given set of markers.
     *
     * @param event the mouse event which caused the selection
     * @param markers the markers to select
     */
    private void handleMouseSelection(final MouseEvent event, final Set<ConstellationAbstractMarker> markers) {
        assert !SwingUtilities.isEventDispatchThread();

        // Is the measuring tool currently active
        final boolean isOverlayActive = overlays.stream().anyMatch(overlay -> overlay.isActive());

        if (event == null || markers == null || isOverlayActive) {
            return;
        }

        final Set<Integer> vertices = new HashSet<>();
        final Set<Integer> transactions = new HashSet<>();
        final Set<ConstellationAbstractMarker> custom = new HashSet<>();
        if (markers.isEmpty() && !event.isShiftDown() && !event.isControlDown()) {
            selectCustomMarkers(custom);
            parent.selectOnGraph(GraphElementType.VERTEX, vertices);
            parent.selectOnGraph(GraphElementType.TRANSACTION, transactions);
        } else {
            if (event.isShiftDown() || event.isControlDown()) {
                final Set<GraphElement> previousSelection = markerCache.getSelectedMarkers().stream()
                        .map(marker -> markerCache.get(marker))
                        .flatMap(Set::stream).collect(Collectors.toSet());
                vertices.addAll(previousSelection.stream()
                        .filter(element -> element.getType() == GraphElementType.VERTEX)
                        .map(element -> element.getId()).collect(Collectors.toSet()));
                transactions.addAll(previousSelection.stream()
                        .filter(element -> element.getType() == GraphElementType.TRANSACTION)
                        .map(element -> element.getId()).collect(Collectors.toSet()));
                custom.addAll(markerCache.getCustomMarkers().stream()
                        .filter(marker -> marker.isSelected())
                        .collect(Collectors.toSet()));
            }
            markers.forEach(marker -> {
                if (!marker.isHidden()) {
                    if ((!event.isShiftDown() && !event.isControlDown())
                            || (event.isShiftDown())
                            || (event.isControlDown() && !marker.isSelected())) {
                        final Set<GraphElement> newSelection = markerCache.get(marker);
                        vertices.addAll(newSelection.stream()
                                .filter(element -> element.getType() == GraphElementType.VERTEX)
                                .map(element -> element.getId()).collect(Collectors.toList()));
                        transactions.addAll(newSelection.stream()
                                .filter(element -> element.getType() == GraphElementType.TRANSACTION)
                                .map(element -> element.getId()).collect(Collectors.toList()));
                        if (marker.isCustom()) {
                            custom.add(marker);
                        }
                    } else if (event.isControlDown() && marker.isSelected()) {
                        final Set<GraphElement> newSelection = markerCache.get(marker);
                        vertices.removeAll(newSelection.stream()
                                .filter(element -> element.getType() == GraphElementType.VERTEX)
                                .map(element -> element.getId()).collect(Collectors.toList()));
                        transactions.removeAll(newSelection.stream()
                                .filter(element -> element.getType() == GraphElementType.TRANSACTION)
                                .map(element -> element.getId()).collect(Collectors.toList()));
                        if (marker.isCustom()) {
                            custom.remove(marker);
                        }
                    } else {
                        // Do nothing
                    }
                }
            });
            selectCustomMarkers(custom);
            parent.selectOnGraph(GraphElementType.VERTEX, vertices);
            parent.selectOnGraph(GraphElementType.TRANSACTION, transactions);
        }
    }

    /**
     * Calculate the set of markers which fall either completely or partially
     * within the selection box. This is done by first checking if any point
     * location constituting the marker is within the box, and if not then
     * performing a point in polygon test to look for intersections.
     *
     * @return the set of markers within the selection box
     */
    private Set<ConstellationAbstractMarker> calculateBoxSelection() {
        assert !SwingUtilities.isEventDispatchThread();

        // get the box
        final float minX = Math.min(boxOriginX, boxDeltaX);
        final float minY = Math.min(boxOriginY, boxDeltaY);
        final float maxX = Math.max(boxOriginX, boxDeltaX);
        final float maxY = Math.max(boxOriginY, boxDeltaY);

        // create rays from the box corner points
        final Edge boxTopLeftHorizontalRay = new Edge("Selection Top Left Horizontal", minX, minY, Float.POSITIVE_INFINITY, minY);
        final Edge boxTopLeftVerticalRay = new Edge("Selection Top Left Vertical", minX, minY, minX, Float.POSITIVE_INFINITY);
        final Edge boxTopRightHorizontalRay = new Edge("Selection Top Right Horizontal", maxX, minY, Float.POSITIVE_INFINITY, minY);
        final Edge boxTopRightVerticalRay = new Edge("Selection Top Right Vertical", maxX, minY, maxX, Float.POSITIVE_INFINITY);
        final Edge boxBottomLeftHorizontalRay = new Edge("Selection Bottom Left Horizontal", minX, maxY, Float.POSITIVE_INFINITY, maxY);
        final Edge boxBottomLeftVerticalRay = new Edge("Selection Bottom Left Vertical", minX, maxY, minX, Float.POSITIVE_INFINITY);
        final Edge boxBottomRightHorizontalRay = new Edge("Selection Bottom Right Horizontal", maxX, maxY, Float.POSITIVE_INFINITY, maxY);
        final Edge boxBottomRightVerticalRay = new Edge("Selection Bottom Right Vertical", maxX, maxY, maxX, Float.POSITIVE_INFINITY);

        // crate counters corresponding to each ray
        final Counter counterTopLeftHorizontal = new Counter("Top Left Horizontal");
        final Counter counterTopLeftVertical = new Counter("Top Left Vertical");
        final Counter counterTopRightHorizontal = new Counter("Top Right Horizontal");
        final Counter counterTopRightVertical = new Counter("Top Right Vertical");
        final Counter counterBottomLeftHorizontal = new Counter("Bottom Left Horizontal");
        final Counter counterBottomLeftVertical = new Counter("Bottom Left Vertical");
        final Counter counterBottomRightHorizontal = new Counter("Bottom Right Horizontal");
        final Counter counterBottomRightVertical = new Counter("Bottom Right Vertical");

        // store rays and counters in a map
        final Map<Edge, Counter> edgeCounters = new HashMap<>();
        edgeCounters.put(boxTopLeftHorizontalRay, counterTopLeftHorizontal);
        edgeCounters.put(boxTopLeftVerticalRay, counterTopLeftVertical);
        edgeCounters.put(boxTopRightHorizontalRay, counterTopRightHorizontal);
        edgeCounters.put(boxTopRightVerticalRay, counterTopRightVertical);
        edgeCounters.put(boxBottomLeftHorizontalRay, counterBottomLeftHorizontal);
        edgeCounters.put(boxBottomLeftVerticalRay, counterBottomLeftVertical);
        edgeCounters.put(boxBottomRightHorizontalRay, counterBottomRightHorizontal);
        edgeCounters.put(boxBottomRightVerticalRay, counterBottomRightVertical);

        // store related counters in a map
        final Map<Counter, Counter> pairedCounters = new HashMap<>();
        pairedCounters.put(counterTopLeftHorizontal, counterTopRightHorizontal);
        pairedCounters.put(counterTopLeftVertical, counterBottomLeftVertical);
        pairedCounters.put(counterBottomLeftHorizontal, counterBottomRightHorizontal);
        pairedCounters.put(counterTopRightVertical, counterBottomRightVertical);

        // get the selected markers
        return markerCache.keys().stream().filter(marker -> {
            // clear all counters
            edgeCounters.forEach((edge, counter) -> counter.reset());

            // get the marker locations
            final List<Location> locations = marker.getLocations();
            for (int i = 0; i < locations.size(); i++) {
                // check if this location is in the box and if so return true
                final Location location = locations.get(i);
                final float locationX = map.getScreenPosition(location).x;
                final float locationY = map.getScreenPosition(location).y;
                if (locationX > minX && locationX < maxX
                        && locationY > minY && locationY < maxY) {
                    return true;
                }

                // check if any ray intersects with the edge of this location
                // and the next location, and if so increment the corresponding
                // counter
                final Location nextLocation = i == locations.size() - 1
                        ? locations.get(0) : locations.get(i + 1);
                final float nextLocationX = map.getScreenPosition(nextLocation).x;
                final float nextLocationY = map.getScreenPosition(nextLocation).y;
                final Edge markerEdge = new Edge("Marker Edge", locationX, locationY, nextLocationX, nextLocationY);
                edgeCounters.entrySet().forEach(entry -> {
                    if (checkForIntersection(markerEdge, entry.getKey())) {
                        entry.getValue().increment();
                    }
                });
            }

            return edgeCounters.values().stream().anyMatch(
                    counter -> counter.get() % 2 == 1)
                    || pairedCounters.entrySet().stream().anyMatch(
                            entry -> entry.getKey().get() != entry.getValue().get());
        }).collect(Collectors.toSet());
    }

    /**
     * Check for intersection by parameterising the marker and selection edges,
     * so that x = minX + deltaX * T and y = minY + deltaY * T. This allows us
     * use to use differential equations to solve for T. If T is between zero
     * and one for both edges, then they intersect.
     *
     * @param markerEdge
     * @param selectionEdge
     * @return true if the edges intersect, false otherwise
     */
    private boolean checkForIntersection(final Edge markerEdge, final Edge selectionEdge) {
        assert !SwingUtilities.isEventDispatchThread();

        double x;
        double y;
        double t;
        double u;

        if (selectionEdge.pointOneX == selectionEdge.pointTwoX) {
            final double markerDeltaX = markerEdge.pointTwoX - markerEdge.pointOneX;
            final double markerDeltaY = markerEdge.pointTwoY - markerEdge.pointOneY;
            x = selectionEdge.pointOneX;
            t = (x - markerEdge.pointOneX) / markerDeltaX;
            y = markerEdge.pointOneY + (markerDeltaY * t);
            return t >= 0 && t <= 1
                    && y >= Math.min(selectionEdge.pointOneY, selectionEdge.pointTwoY)
                    && y <= Math.max(selectionEdge.pointOneY, selectionEdge.pointTwoY);
        } else if (selectionEdge.pointOneY == selectionEdge.pointTwoY) {
            final double markerDeltaX = markerEdge.pointTwoX - markerEdge.pointOneX;
            final double markerDeltaY = markerEdge.pointTwoY - markerEdge.pointOneY;
            y = selectionEdge.pointOneY;
            t = (y - markerEdge.pointOneY) / markerDeltaY;
            x = markerEdge.pointOneX + (markerDeltaX * t);
            return t >= 0 && t <= 1
                    && x >= Math.min(selectionEdge.pointOneX, selectionEdge.pointTwoX)
                    && x <= Math.max(selectionEdge.pointOneX, selectionEdge.pointTwoX);
        } else {
            final double markerDeltaX = markerEdge.pointTwoX - markerEdge.pointOneX;
            final double markerDeltaY = markerEdge.pointTwoY - markerEdge.pointOneY;
            final double selectionDeltaX = selectionEdge.pointTwoX - selectionEdge.pointOneX;
            final double selectionDeltaY = selectionEdge.pointTwoY - selectionEdge.pointOneY;
            final double edgeOffsetX = (markerEdge.pointOneX - selectionEdge.pointOneX) / selectionDeltaX;
            final double edgeOffsetY = (markerEdge.pointOneY - selectionEdge.pointOneY) / selectionDeltaY;
            final double differenceDeltaX = markerDeltaX / selectionDeltaX;
            final double differenceDeltaY = markerDeltaY / selectionDeltaY;
            u = (edgeOffsetX - edgeOffsetY) / (differenceDeltaY - differenceDeltaX);
            t = edgeOffsetX + (differenceDeltaX * u);
            return t >= 0 && t <= 1 && u >= 0 && u <= 1;

        }
    }

    /**
     * Class representing the named edge between two points.
     */
    private static final class Edge {

        private final String name;
        private final double pointOneX;
        private final double pointOneY;
        private final double pointTwoX;
        private final double pointTwoY;

        public Edge(final String name,
                final double pointOneX, final double pointOneY,
                final double pointTwoX, final double pointTwoY) {
            this.name = name;
            this.pointOneX = pointOneX;
            this.pointOneY = pointOneY;
            this.pointTwoX = pointTwoX;
            this.pointTwoY = pointTwoY;
        }

        public Edge(final double pointOneX, final double pointOneY,
                final double pointTwoX, final double pointTwoY) {
            this("Unnamed Edge", pointOneX, pointOneY, pointTwoX, pointTwoY);
        }

        @Override
        public String toString() {
            return String.format("%s: (%f, %f), (%f, %f)",
                    name, pointOneX, pointOneY, pointTwoX, pointTwoY);
        }
    }

    /**
     * Class representing a named counter.
     */
    private static final class Counter {

        private String name;
        private int count;

        public Counter(final String name) {
            this.name = name;
            this.count = 0;
        }

        public Counter() {
            this("Unnamed Counter");
        }

        public int get() {
            return count;
        }

        public void increment() {
            count++;
        }

        public void reset() {
            count = 0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d", name, count);
        }
    }
}

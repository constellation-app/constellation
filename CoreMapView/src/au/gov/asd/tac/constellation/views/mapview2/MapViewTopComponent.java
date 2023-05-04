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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.plugins.ExtractCoordsFromGraphPlugin;
import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ScrollPane;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component for the MapView
 *
 * @author altair1673
 */
@TopComponent.Description(
        preferredID = "MapViewTopComponent2",
        iconBase = "au/gov/asd/tac/constellation/views/mapview/resources/treasure-map.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 4000)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapView2Action",
        preferredID = "MapViewTopComponent2"
)
@NbBundle.Messages({
    "CTL_MapView2Action=Map View",
    "CTL_MapViewTopComponent2=Map View",
    "HINT_MapViewTopComponent2=Map View"
})

public final class MapViewTopComponent extends JavaFxTopComponent<MapViewPane> {

    private static final Logger LOGGER = Logger.getLogger(MapViewTopComponent.class.getName());

    public static final Object LOCK = new Object();

    // The mapview itself
    private final MapViewPane mapViewPane;


    private int markerID = 0;

    private final MapViewTopComponent self = this;

    public MapViewTopComponent() {

        setName(Bundle.CTL_MapViewTopComponent2());
        setToolTipText(Bundle.HINT_MapViewTopComponent2());

        initComponents();
        mapViewPane = new MapViewPane(this);
        mapViewPane.setUpMap();
        initContent2();

    }


    public void initContent2() {
        super.initContent();
    }

    /**
     * Sets the top components content to the findViewPane.
     *
     * @return
     */
    @Override
    protected MapViewPane createContent() {
        return mapViewPane;
    }

    /**
     * Sets the css Styling
     *
     * @return
     */
    @Override
    protected String createStyle() {
        return "";
    }


    /**
     * Handles what occurs when the component is opened. This updates the UI to
     * ensure its current, toggles the find view to set it to enabled or
     * disabled based on if a graph is open, focuses the findTextBox for UX
     * quality, ensures the view window is floating. It also sets the size and
     * location of the view to be in the top right of the users screen.
     */
    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
    }

    @Override
    protected void handleComponentClosed() {

        super.handleComponentClosed();
        mapViewPane.getMap().clearQueriedMarkers();
    }

    public int getNewMarkerID() {
        return ++markerID;
    }

    public void setMapImage(final Component mapComponent) {

        SwingUtilities.invokeLater(() -> {
            jfxContainer.add(mapComponent);
            validate();
        });


    }

    public JFXPanel getJfxContainer() {
        return jfxContainer;
    }


    /**
     * When a graph is opened handle toggling the find view disabled state
     *
     * @param graph
     */
    @Override
    protected void handleGraphOpened(final Graph graph) {
        super.handleGraphOpened(graph);

    }

    public Map<String, AbstractMarker> getAllMarkers() {
        return mapViewPane.getAllMarkers();
    }


    /**
     * When a new graph is created handle updating the UI
     *
     * @param graph
     */
    @Override
    protected void handleNewGraph(final Graph graph) {
        super.handleNewGraph(graph);

        if (graph != null) {
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        PluginExecution.withPlugin(new ExtractCoordsFromGraphPlugin(self)).executeNow(graph);
                    } catch (final PluginException | InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "Exception thrown: {0}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }).get();

            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown: {0}", ex.getMessage());
                Thread.currentThread().interrupt();

            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown: {0}", ex.getMessage());
            }

            Platform.runLater(() -> mapViewPane.getMap().redrawQueriedMarkers());
        }

    }

    /**
     * Get the findViewPane
     *
     * @return the findViewPane
     */
    public MapViewPane getMapViewPane() {
        return mapViewPane;
    }

    public MapProvider getDefaultProvider() {
        return mapViewPane.getDefaultProvider();
    }

    public boolean shouldUpdate() {
        return this.needsUpdate();
    }

    public int getContentWidth() {
        return getWidth();
    }

    /**
     * Adds marker on the map
     *
     * @param key - key which is the coordinate of the marker
     * @param e - the marker itself
     */
    public void addMarker(final String key, final AbstractMarker e) {
        if (getMapViewPane().getMap() != null) {
            getMapViewPane().getMap().addMarkerToHashMap(key, e);
        }
    }

    public int getContentHeight() {
        return getHeight();
    }

    public List<? extends MapProvider> getProviders() {
        return mapViewPane.getProviders();
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }


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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}

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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.layers.components.LayersViewPane;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import java.util.ArrayList;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * The Layers View Top Component. This class provides the Layers View window and
 * handles all interactions with the graph.
 *
 * @author aldebaran30701
 */
@TopComponent.Description(
        preferredID = "LayersViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/layers/resources/layers-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 600),
    @ActionReference(path = "Shortcuts", name = "CS-L")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LayersViewAction",
        preferredID = "LayersViewTopComponent")
@Messages({
    "CTL_LayersViewAction=Layers View",
    "CTL_LayersViewTopComponent=Layers View",
    "HINT_LayersViewTopComponent=Layers View"})
public final class LayersViewTopComponent extends JavaFxTopComponent<LayersViewPane> {

    private final LayersViewController layersViewController;
    private final LayersViewPane layersViewPane;

    public LayersViewTopComponent() {
        setName(Bundle.CTL_LayersViewTopComponent());
        setToolTipText(Bundle.HINT_LayersViewTopComponent());
        initComponents();

        layersViewController = LayersViewController.getDefault().init(LayersViewTopComponent.this);
        layersViewPane = new LayersViewPane(layersViewController);

        initContent();

        addAttributeValueChangeHandler(LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE, graph -> {
            if (!needsUpdate()) {
                return;
            }

            layersViewController.readStateFuture();
            layersViewController.updateQueries(graph);
        });
    }

    public void removeValueHandlers(final List<AttributeValueMonitor> valueMonitors) {
        // remove all monitors before re-adding updated ones
        valueMonitors.forEach(monitor -> removeAttributeValueChangeHandler(monitor));
    }

    public synchronized List<AttributeValueMonitor> setChangeListeners(final List<SchemaAttribute> changeListeners) {
        final List<AttributeValueMonitor> valueMonitors = new ArrayList<>();
        changeListeners.forEach(attribute -> valueMonitors.add(
                addAttributeValueChangeHandler(attribute, changedGraph -> layersViewController.updateQueries(changedGraph))
        ));
        return List.copyOf(valueMonitors);
    }

    @Override
    protected String createStyle() {
        return JavafxStyleManager.isDarkTheme() ? "resources/layers-view-dark.css" : "resources/layers-view-light.css";
    }

    @Override
    protected LayersViewPane createContent() {
        return layersViewPane;
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (needsUpdate() && graph != null) {
            preparePane();
        }
        setPaneStatus();
    }

    @Override
    protected void handleGraphOpened(final Graph graph) {
        if (needsUpdate() && graph != null) {
            preparePane();
        }
        setPaneStatus();
    }

    @Override
    protected void handleGraphClosed(final Graph graph) {
        if (needsUpdate() && graph != null) {
            preparePane();
        }
        setPaneStatus();
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        preparePane();
        setPaneStatus();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        layersViewController.readState();
        layersViewController.addAttributes();
        setPaneStatus();
    }

    protected void preparePane() {
        createContent().setDefaultLayers();
        layersViewController.readState();
        layersViewController.addAttributes();
    }
    
    /**
     * Sets the status of the pane dependent on if a graph is currently active.
     * The status is used to enable or disable the view when a graph exists.
     */
    protected void setPaneStatus(){
        createContent().setEnabled(GraphManager.getDefault().getActiveGraph() != null);
    }

    public LayersViewPane getLayersViewPane() {
        return layersViewPane;
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

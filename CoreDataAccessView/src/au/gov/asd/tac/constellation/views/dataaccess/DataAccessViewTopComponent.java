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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.components.ButtonToolbar;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
import au.gov.asd.tac.constellation.views.qualitycontrol.widget.DefaultQualityControlAutoButton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the Data Access View.
 */
@TopComponent.Description(
        preferredID = "DataAccessViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/dataaccess/resources/data-access-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 400),
    @ActionReference(path = "Shortcuts", name = "CS-D"),
    @ActionReference(path = "Toolbars/Views", position = 200)

})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DataAccessViewAction",
        preferredID = "DataAccessViewTopComponent"
)
@Messages({
    "CTL_DataAccessViewAction=Data Access View",
    "CTL_DataAccessViewTopComponent=Data Access View",
    "HINT_DataAccessViewTopComponent=Data Access View"
})
public final class DataAccessViewTopComponent extends JavaFxTopComponent<DataAccessPane> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    
    private final DataAccessPane dataAccessPane;

    /**
     * Create a new data access view.
     */
    public DataAccessViewTopComponent() {
        setName(Bundle.CTL_DataAccessViewTopComponent());
        setToolTipText(Bundle.HINT_DataAccessViewTopComponent());

        initComponents();

        dataAccessPane = new DataAccessPane(this);
        dataAccessPane.addUIComponents();
        
        // The data access pane that is initialized above is returned in the
        // overridden method getContent() below. That is how the initContent()
        // in the super class can reference it and add the data accees view to
        // this pane.
        initContent();

        addAttributeCountChangeHandler(graph -> {
            if (needsUpdate() && dataAccessPane != null) {
                dataAccessPane.update(graph);
            }
        });

        ProxyUtilities.setProxySelector(null);
    }

    /**
     * The pane used for the data access view.
     *
     * @return the pane used for the data access view
     */
    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }
    
    /**
     * A fixed single thread pool for execution of jobs in the data access view
     * that need to happen in an asynchronous manner.
     *
     * @return a fixed single thread pool
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public DataAccessPane createContent() {
        return dataAccessPane;
    }

    @Override
    public String createStyle() {
        return "resources/data-access-view.css";
    }

    /**
     * Handle component opening and hook up listeners and observers.
     */
    @Override
    public void handleComponentOpened() {
        super.handleComponentOpened();
        ManageQualityControlListeners(true);
        QualityControlAutoVetter.getInstance().addObserver(getDataAccessPane());
    }

    /**
     * Handle component closing and pull down listeners and observers.
     */
    @Override
    public void handleComponentClosed() {
        super.handleComponentClosed();
        ManageQualityControlListeners(false);
        QualityControlAutoVetter.getInstance().removeObserver(getDataAccessPane());
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        handleNewGraph(GraphManager.getDefault().getActiveGraph());
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (needsUpdate() && getDataAccessPane() != null) {
            getDataAccessPane().update(graph);
            Platform.runLater(() -> 
                DataAccessUtilities.loadDataAccessState(getDataAccessPane(), graph)
            );
        }
    }

    /**
     * Add or remove all quality control auto vetter listeners. These listeners
     * are tied to DefaultQualityControlAutoButton buttons found nested within
     * the button toolbar.
     *
     * @param add Should quality control auto vetter listeners be added (true)
     * or removed (false).
     */
    private void ManageQualityControlListeners(boolean add) {
        // Dig down to the button toolbar and find any quality control auto
        // buttons - these subscribe as listeners to the quality control auto
        // vetter and as such these subscriptions need to be modified.
        final ButtonToolbar buttonToolbar = dataAccessPane.getButtonToolbar();
        final HBox hboxTop = buttonToolbar.getRabRegionExectueHBoxTop();
        final HBox hboxBottom = buttonToolbar.getRabRegionExectueHBoxBottom();
        for (final Object button : hboxTop.getChildren()) {
            if (button instanceof DefaultQualityControlAutoButton) {
                if (add) {
                    ((DefaultQualityControlAutoButton) button).AddQCListener();
                } else {
                    ((DefaultQualityControlAutoButton) button).RemoveQCListener();
                }
            }
        }
        for (final Object button : hboxBottom.getChildren()) {
            if (button instanceof DefaultQualityControlAutoButton) {
                if (add) {
                    ((DefaultQualityControlAutoButton) button).AddQCListener();
                } else {
                    ((DefaultQualityControlAutoButton) button).RemoveQCListener();
                }
            }
        }
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

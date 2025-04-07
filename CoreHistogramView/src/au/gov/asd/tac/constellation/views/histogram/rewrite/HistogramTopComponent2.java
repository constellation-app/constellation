/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.utilities.ElementSet;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.HistogramState;
import java.util.LinkedHashMap;
import java.util.Map;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the histogram view.
 *
 * @author sirius, Quasar985
 */
//@ConvertAsProperties(
//        dtd = "-//au.gov.asd.tac.constellation.views.histogram//Histogram//EN",
//        autostore = false
//)
@TopComponent.Description(
        preferredID = "HistogramTopComponent2",
        iconBase = "au/gov/asd/tac/constellation/views/histogram/resources/histogram.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.histogram.rewrite.HistogramTopComponent2"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 501),
//    @ActionReference(path = "Shortcuts", name = "CS-H"),
    @ActionReference(path = "Toolbars/Views", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_HistogramAction2",
        preferredID = "HistogramTopComponent2"
)
@NbBundle.Messages({
    "CTL_HistogramAction2=Histogram2",
    "CTL_HistogramTopComponent2=Histogram2",
    "HINT_HistogramTopComponent2=The histogram view will display attribute values as a bar chart"
})

//public final class HistogramTopComponent2 extends TopComponent implements GraphManagerListener, GraphChangeListener, UndoRedo.Provider {
public final class HistogramTopComponent2 extends JavaFxTopComponent<HistogramPane> {

    private static final int MIN_WIDTH = 425;
    private static final int MIN_HEIGHT = 400;
    Graph currentGraph = null;
    //private final HistogramControls controls;
    //private final HistogramDisplay display;
    private long currentGlobalModificationCount = Long.MIN_VALUE;
    private long currentAttributeModificationCount = Long.MIN_VALUE;
    private long currentStructureModificationCount = Long.MIN_VALUE;
    private long currentSelectedModificationCount = Long.MIN_VALUE;
    private long currentBinnedModificationCount = Long.MIN_VALUE;
    private static final int CURRENT_TIME_ZONE_ATTRIBUTE = Graph.NOT_FOUND;
    private long currentTimeZoneModificationCount = Long.MIN_VALUE;
    private final Map<String, BinCreator> binCreators = new LinkedHashMap<>();
    private int histogramStateAttribute = Graph.NOT_FOUND;
    private HistogramState currentHistogramState = new HistogramState();
    private int binnedAttribute = Graph.NOT_FOUND;
    private BinCollection currentBinCollection = null;
    private int selectedAttribute = Graph.NOT_FOUND;
    private long latestGraphChangeID = 0;
    private ElementSet currentFilter;

    private final HistogramPane histogramPane;
    private final HistogramController histogramController;

    public HistogramTopComponent2() {
        super();

        setName(Bundle.CTL_HistogramTopComponent2());
        setToolTipText(Bundle.HINT_HistogramTopComponent2());
        this.setMinimumSize(new java.awt.Dimension(MIN_WIDTH, MIN_HEIGHT));

//        controls = new HistogramControls(this);
//        add(controls, BorderLayout.SOUTH);
//        display = new HistogramDisplay(null);
//        final JScrollPane displayScroll = new JScrollPane(display, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        displayScroll.getVerticalScrollBar().setUnitIncrement(HistogramDisplay.MAXIMUM_BAR_HEIGHT);
        // add(displayScroll, BorderLayout.CENTER);
        histogramController = HistogramController.getDefault().init(this);
        histogramPane = new HistogramPane(histogramController);

        initComponents();
        super.initContent();

//        final JScrollPane paneScroll = new JScrollPane(histogramPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//        add(paneScroll, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
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
    @Override
    protected void componentShowing() {
        super.componentShowing();
        histogramController.readState();
    }

    @Override
    public HistogramPane createContent() {
        System.out.println("histogram createContent");
        return histogramPane;
    }

    @Override
    protected String createStyle() {
//        return JavafxStyleManager.isDarkTheme()
//                ? "resources/data-access-view-dark.css"
//                : "resources/data-access-view-light.css";
        return null;
    }

    // VESTIGAL FROM ANALYTIC VIEW
//    @Override
//    protected void handleNewGraph(final Graph graph) {
//        if (needsUpdate() && graph != null) {
//            currentGraphId = graph.getId();
//            activeGraph = graph;
//
//            if (analyticViewPane != null) {
//                analyticViewPane.reset();
//                analyticViewPane.setIsRunnable(true);
//                analyticController.readState();
//                if (GraphManager.getDefault().getActiveGraph() == null) {
//                    analyticViewPane.reset();
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void handleGraphOpened(final Graph graph) {
//        if (graph != null) {
//            currentGraphId = graph.getId();
//        }
//        if (analyticViewPane != null) {
//            analyticViewPane.reset();
//            analyticController.readState();
//            activeGraph = GraphManager.getDefault().getActiveGraph();
//            if (activeGraph == null) {
//                analyticViewPane.reset();
//            }
//        }
//    }
//
//    @Override
//    protected void handleComponentOpened() {
//        super.handleComponentOpened();
//        activeGraph = GraphManager.getDefault().getActiveGraph();
//        if (activeGraph != null) {
//            currentGraphId = activeGraph.getId();
//        }
//        analyticController.readState();
//
//        if (activeGraph == null) {
//            analyticViewPane.reset();
//        }
//    }
//
//    @Override
//    protected void componentShowing() {
//        super.componentShowing();
//        activeGraph = GraphManager.getDefault().getActiveGraph();
//        if (activeGraph != null && !activeGraph.getId().equals(currentGraphId)) {
//            analyticViewPane.reset();
//        }
//        analyticController.readState();
//        handleNewGraph(activeGraph);
//    }
//    
//    @Override
//    protected void handleGraphChange(final GraphChangeEvent event) {
//        if (event == null) { // can be null at this point in time
//            return;
//        }
//        final GraphChangeEvent newEvent = event.getLatest();
//        if (newEvent == null) { // latest event may be null - defensive check
//            return;
//        }
//        if (newEvent.getId() > latestGraphChangeID) {
//            latestGraphChangeID = newEvent.getId();
//            if (activeGraph != null) {
//                queue.add(newEvent);
//                if (refreshThread == null || !refreshThread.isAlive()) {
//                    refreshThread = new Thread(refreshRunnable);
//                    refreshThread.setName(ANALYTIC_VIEW_GRAPH_CHANGED_THREAD_NAME);
//                    refreshThread.start();
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void handleComponentClosed() {
//        super.handleComponentClosed();
//        analyticViewPane.reset();
//    }
}

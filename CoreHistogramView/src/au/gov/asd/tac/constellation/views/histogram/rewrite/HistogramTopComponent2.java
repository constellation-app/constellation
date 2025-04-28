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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.utilities.ElementSet;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import static au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer.display;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.histogram.AttributeType;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinComparator;
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import au.gov.asd.tac.constellation.views.histogram.HistogramClearFilterPlugin;
import au.gov.asd.tac.constellation.views.histogram.HistogramConcept;
import au.gov.asd.tac.constellation.views.histogram.HistogramFilterOnSelectionPlugin;
import au.gov.asd.tac.constellation.views.histogram.HistogramState;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
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
        return histogramPane;
    }

    @Override
    protected String createStyle() {
//        return JavafxStyleManager.isDarkTheme()
//                ? "resources/data-access-view-dark.css"
//                : "resources/data-access-view-light.css";
        return null;
    }

    public void setHistogramViewOptions(final GraphElementType elementType, final AttributeType attributeType, final String attribute) {
        if (currentGraph != null) {
            if (elementType == null) {
                throw new IllegalArgumentException("Null element type");
            }
            if (currentHistogramState == null || elementType != currentHistogramState.getElementType() || attributeType != currentHistogramState.getAttributeType()
                    || (attribute == null ? currentHistogramState.getAttribute() != null : !attribute.equals(currentHistogramState.getAttribute()))) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setElementType(elementType);
                newHistogramState.setAttributeType(attributeType);
                newHistogramState.setAttribute(attribute);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    public void setGraphElementType(final GraphElementType elementType) {
        if (currentGraph != null) {
            if (elementType == null) {
                throw new IllegalArgumentException("Null element type");
            }

            // If the current state is null or the elementType selected is not the one already selected.
            if (currentHistogramState == null || elementType != currentHistogramState.getElementType()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setElementType(elementType);
                newHistogramState.setElementState();
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    public void setAttributeType(final AttributeType attributeType) {
        if (currentGraph != null && (currentHistogramState == null || attributeType != currentHistogramState.getAttributeType())) {
            HistogramState newHistogramState = new HistogramState(currentHistogramState);
            newHistogramState.setAttributeType(attributeType);
            newHistogramState.setAttribute("");
            newHistogramState.setBinFormatter(BinFormatter.DEFAULT_BIN_FORMATTER);
            PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
        }
    }

    public void setAttribute(final String attribute) {
        if (currentGraph != null && (currentHistogramState == null || (attribute == null ? currentHistogramState.getAttribute() != null : !attribute.equals(currentHistogramState.getAttribute())))) {
            HistogramState newHistogramState = new HistogramState(currentHistogramState);
            newHistogramState.setAttribute(attribute);
            newHistogramState.setBinFormatter(BinFormatter.DEFAULT_BIN_FORMATTER);
            PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
        }
    }

    public void setBinComparator(final BinComparator binComparator) {
        if (currentGraph != null) {
            if (binComparator == null) {
                throw new IllegalArgumentException("Null bin comparator");
            }
            if (currentHistogramState == null || binComparator != currentHistogramState.getBinComparator()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinComparator(binComparator);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    public void setBinFormatter(final BinFormatter binFormatter, final PluginParameters parameters) {
        if (currentGraph != null) {
            if (binFormatter == null) {
                throw new IllegalArgumentException("Null bin formatter");
            }
            if (currentHistogramState == null || binFormatter != currentHistogramState.getBinFormatter() || parameters != currentHistogramState.getBinFormatterParameters()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinFormatter(binFormatter);
                newHistogramState.setBinFormatterParameters(parameters);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    public void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        if (currentGraph != null) {
            if (binSelectionMode == null) {
                throw new IllegalArgumentException("Null bin selection mode");
            }
            if (currentHistogramState == null || binSelectionMode != currentHistogramState.getBinSelectionMode()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinSelectionMode(binSelectionMode);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    public void selectOnlyBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new HistogramSelectOnlyBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    public void filterOnSelection() {
        if (currentGraph != null) {
            Plugin plugin = new HistogramFilterOnSelectionPlugin();
            PluginParameters params = plugin.createParameters();
            params.getParameters().get(HistogramFilterOnSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(new ElementTypeParameterValue(currentHistogramState.getElementType()));
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(currentGraph);
        }
    }

    public void clearFilter() {
        if (currentGraph != null) {
            Plugin plugin = new HistogramClearFilterPlugin();
            PluginParameters params = plugin.createParameters();
            params.getParameters().get(HistogramClearFilterPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(new ElementTypeParameterValue(currentHistogramState.getElementType()));
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(currentGraph);
        }
    }

    public void selectBins(final int firstBin, final int lastBin, final boolean select) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new SelectBins(firstBin, lastBin, select)).executeLater(currentGraph);
        }
    }

    public void invertBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new InvertBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    public void completeBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new CompleteBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    public void filterSelection() {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new FilterSelection()).executeLater(currentGraph);
        }
    }

    public void saveBinsToGraph() {
        if (currentGraph != null && currentBinCollection != null) {
            PluginExecution.withPlugin(new SaveBinsToGraph()).executeLater(currentGraph);
        }
    }

    public void saveBinsToClipboard() {
        if (currentGraph != null && currentBinCollection != null) {
            PluginExecution.withPlugin(new SaveBinsToClipboard()).executeLater(currentGraph);
        }
    }

    public void expandSelection() {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new ExpandSelection()).executeLater(currentGraph);
        }
    }

    /**
     * Plugin to update histogram state.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private class HistogramStateUpdaterPlugin extends SimpleEditPlugin {

        private final HistogramState state;

        public HistogramStateUpdaterPlugin(final HistogramState state) {
            this.state = state;
        }

        @Override
        public String getName() {
            return "Histogram View: Update State";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            histogramStateAttribute = HistogramConcept.MetaAttribute.HISTOGRAM_STATE.ensure(graph);
            graph.setObjectValue(histogramStateAttribute, 0, state);
        }
    }

    /**
     * Plugin to only select bins.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class HistogramSelectOnlyBins extends SimpleEditPlugin {

        private final int firstBin;
        private final int lastBin;

        public HistogramSelectOnlyBins(final int firstBin, final int lastBin) {
            this.firstBin = firstBin;
            this.lastBin = lastBin;
        }

        @Override
        public String getName() {
            return "Histogram View: Select Only Bins";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.selectOnlyBins(graph, firstBin, lastBin);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
        }
    }

    /**
     * Plugin to select bins.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class SelectBins extends SimpleEditPlugin {

        private final int firstBin;
        private final int lastBin;
        private final boolean select;

        public SelectBins(final int firstBin, final int lastBin, final boolean select) {
            this.firstBin = firstBin;
            this.lastBin = lastBin;
            this.select = select;
        }

        @Override
        public String getName() {
            return "Histogram View: Select Bins";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.selectBins(graph, firstBin, lastBin, select);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
        }
    }

    /**
     * Plugin to invert bins.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class InvertBins extends SimpleEditPlugin {

        private final int firstBin;
        private final int lastBin;

        public InvertBins(final int firstBin, final int lastBin) {
            this.firstBin = firstBin;
            this.lastBin = lastBin;
        }

        @Override
        public String getName() {
            return "Histogram View: Invert Bins";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.invertBins(graph, firstBin, lastBin);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
        }
    }

    /**
     * Plugin to complete bins.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class CompleteBins extends SimpleEditPlugin {

        private final int firstBin;
        private final int lastBin;

        public CompleteBins(final int firstBin, final int lastBin) {
            this.firstBin = firstBin;
            this.lastBin = lastBin;
        }

        @Override
        public String getName() {
            return "Histogram View: Complete Bins";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.completeBins(graph, firstBin, lastBin);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
        }
    }

    /**
     * Plugin to filter selection.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class FilterSelection extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Histogram View: Filter Selection";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.filterSelection(graph);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
            // TODO
            //SwingUtilities.invokeLater(display::repaint);
        }
    }

    /**
     * Plugin to save bins to graph.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    private class SaveBinsToGraph extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Histogram View: Save Bins To Graph";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final int attributeId = graph.getSchema().getFactory().ensureAttribute(graph, currentHistogramState.getElementType(), HistogramConcept.HISTOGRAM_BIN_LABEL);
            currentBinCollection.saveBinsToGraph(graph, attributeId);
        }
    }

    /**
     * Plugin to save bins to clipboard.
     */
    @PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
    private class SaveBinsToClipboard extends SimpleReadPlugin {

        @Override
        public String getName() {
            return "Histogram View: Save Bins To Clipboard";
        }

        @Override
        protected void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.saveBinsToClipboard();
        }
    }

    /**
     * Plugin to expand selection.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class ExpandSelection extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Histogram View: Expand Selection";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            currentBinCollection.expandSelection(graph);
            if (selectedAttribute != Graph.NOT_FOUND) {
                currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
            }
            // TODO
            //SwingUtilities.invokeLater(display::repaint);
        }
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

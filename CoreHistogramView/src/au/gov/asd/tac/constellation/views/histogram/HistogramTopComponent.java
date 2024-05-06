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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
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
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the histogram view.
 *
 * @author sirius
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.views.histogram//Histogram//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "HistogramTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/histogram/resources/histogram.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.histogram.HistogramTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 500),
    @ActionReference(path = "Shortcuts", name = "CS-H"),
    @ActionReference(path = "Toolbars/Views", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_HistogramAction",
        preferredID = "HistogramTopComponent"
)
@Messages({
    "CTL_HistogramTopComponent=Histogram",
    "HINT_HistogramTopComponent=The histogram view will display attribute values as a bar chart"
})
public final class HistogramTopComponent extends TopComponent implements GraphManagerListener, GraphChangeListener, UndoRedo.Provider {

    private static final int MIN_WIDTH = 425;
    private static final int MIN_HEIGHT = 400;
    Graph currentGraph = null;
    private final HistogramControls controls;
    private final HistogramDisplay display;
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

    public HistogramTopComponent() {
        initComponents();
        setName(Bundle.CTL_HistogramTopComponent());
        setToolTipText(Bundle.HINT_HistogramTopComponent());
        this.setMinimumSize(new java.awt.Dimension(MIN_WIDTH, MIN_HEIGHT));

        controls = new HistogramControls(this);
        add(controls, BorderLayout.SOUTH);

        display = new HistogramDisplay(this);
        final JScrollPane displayScroll = new JScrollPane(display, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        displayScroll.getVerticalScrollBar().setUnitIncrement(HistogramDisplay.MAXIMUM_BAR_HEIGHT);
        add(displayScroll, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(100, 100));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        GraphManager.getDefault().addGraphManagerListener(this);
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
    }

    @Override
    public void componentClosed() {
        GraphManager.getDefault().removeGraphManagerListener(this);
        newActiveGraph(null);
        // Remove Listeners when Histogram View is closed.
        removeMouseListener(display);
        removeMouseMotionListener(display);
        removeMouseWheelListener(display);
        removeComponentListener(display);
        removeKeyListener(display);
    }

    void writeProperties(Properties p) {
        // Method required for @ConvertAsProperties, intentionally left blank.
    }

    void readProperties(Properties p) {
        // Method required for @ConvertAsProperties, intentionally left blank.
    }

    @Override
    public void newActiveGraph(Graph graph) {
        if (currentGraph != graph) {

            if (currentGraph != null) {
                currentGraph.removeGraphChangeListener(this);
                currentGraph = null;
            }

            if (graph != null) {
                currentGraph = graph;
                currentGraph.addGraphChangeListener(this);
            }

            reset();
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        GraphNode graphNode = GraphNode.getGraphNode(currentGraph);
        return graphNode == null ? null : graphNode.getUndoRedoManager();
    }

    @Override
    public void graphChanged(GraphChangeEvent evt) {
        evt = evt.getLatest();
        if (evt.getId() > latestGraphChangeID) {
            latestGraphChangeID = evt.getId();

            if (currentGraph != null) {

                boolean binCollectionModified = false;

                ReadableGraph rg = currentGraph.getReadableGraph();
                try {

                    long oldGlobalModificationCount = currentGlobalModificationCount;
                    currentGlobalModificationCount = rg.getGlobalModificationCounter();

                    long oldAttributeModificationCount = currentAttributeModificationCount;
                    currentAttributeModificationCount = rg.getAttributeModificationCounter();

                    long oldStructureModificationCount = currentStructureModificationCount;
                    currentStructureModificationCount = rg.getStructureModificationCounter();

                    if (currentGlobalModificationCount != oldGlobalModificationCount) {

                        if (currentAttributeModificationCount != oldAttributeModificationCount
                                || currentStructureModificationCount != oldStructureModificationCount) {
                            reset(rg);
                            return;
                        }

                        if (CURRENT_TIME_ZONE_ATTRIBUTE != Graph.NOT_FOUND && currentTimeZoneModificationCount != rg.getValueModificationCounter(CURRENT_TIME_ZONE_ATTRIBUTE)) {
                            currentTimeZoneModificationCount = rg.getValueModificationCounter(CURRENT_TIME_ZONE_ATTRIBUTE);
                            reset(rg);
                            return;
                        }

                        if (binnedAttribute != Graph.NOT_FOUND) {
                            long oldBinnedModificationCount = currentBinnedModificationCount;
                            currentBinnedModificationCount = rg.getValueModificationCounter(binnedAttribute);
                            if (currentBinnedModificationCount != oldBinnedModificationCount) {
                                reset();
                                return;
                            }
                        }

                        HistogramState oldHistogramState = currentHistogramState;
                        if (histogramStateAttribute != Graph.NOT_FOUND) {
                            currentHistogramState = (HistogramState) rg.getObjectValue(histogramStateAttribute, 0);
                            if (currentHistogramState == null) {
                                currentHistogramState = new HistogramState();
                            }
                        } else {
                            currentHistogramState = new HistogramState();
                        }

                        // Ensure that the HistogramState is compatible with the current graph.
                        currentHistogramState.validate(rg);

                        if (currentHistogramState != oldHistogramState) {

                            if (oldHistogramState == null || currentHistogramState.getElementType() != oldHistogramState.getElementType()
                                    || currentHistogramState.getAttributeType() != oldHistogramState.getAttributeType()
                                    || currentHistogramState.getBinFormatter() != oldHistogramState.getBinFormatter()
                                    || currentHistogramState.getBinFormatterParameters() != oldHistogramState.getBinFormatterParameters()) {
                                reset(rg);
                                return;
                            }

                            String currentAttribute = currentHistogramState.getAttribute();
                            if (currentAttribute == null ? oldHistogramState.getAttribute() != null : !currentAttribute.equals(oldHistogramState.getAttribute())) {
                                reset(rg);
                                return;
                            }

                            if ((currentFilter == null && currentHistogramState.getFilter(currentHistogramState.getElementType()) != null)
                                    || (currentFilter != null && currentFilter != currentHistogramState.getFilter(currentHistogramState.getElementType()))) {
                                reset(rg);
                                return;
                            }

                            if (currentHistogramState.getBinComparator() != oldHistogramState.getBinComparator() && currentBinCollection != null) {
                                currentBinCollection.sort(currentHistogramState.getBinComparator());
                                binCollectionModified = true;
                            }

                            if (currentHistogramState.getBinSelectionMode() != oldHistogramState.getBinSelectionMode() && currentBinCollection != null) {
                                display.setBinSelectionMode(currentHistogramState.getBinSelectionMode());
                                binCollectionModified = true;
                            }

                            controls.setHistogramState(currentHistogramState, binCreators);
                        }

                        if (selectedAttribute != Graph.NOT_FOUND) {
                            long oldSelectedModificationCount = currentSelectedModificationCount;
                            currentSelectedModificationCount = rg.getValueModificationCounter(selectedAttribute);
                            if (currentSelectedModificationCount != oldSelectedModificationCount && currentBinCollection != null) {
                                currentBinCollection.updateSelection(rg);
                                if (currentHistogramState.getBinComparator().usesSelection()) {
                                    currentBinCollection.sort(currentHistogramState.getBinComparator());
                                }
                                binCollectionModified = true;
                            }
                        }

                        if (binCollectionModified) {
                            display.updateBinCollection();
                        }
                    }

                } finally {
                    rg.release();
                }
            }
        }
    }

    public void modifyBinHeight(final int change) {
        if (change < 0) {
            display.decreaseBarHeight();
        } else if (change > 0) {
            display.increaseBarHeight();
        }
    }

    private void reset() {
        if (currentGraph != null) {
            ReadableGraph rg = currentGraph.getReadableGraph();
            try {
                reset(rg);
            } finally {
                if (rg != null) {
                    rg.release();
                }
            }
        } else {
            reset(null);
        }
    }

    private void reset(final GraphReadMethods graph) {

        if (graph == null) {
            currentHistogramState = null;
            controls.setHistogramState(null, null);
            display.setBinCollection(null, BinIconMode.NONE);
            currentFilter = null;
            return;
        }

        histogramStateAttribute = HistogramConcept.MetaAttribute.HISTOGRAM_STATE.get(graph);
        if (histogramStateAttribute == Graph.NOT_FOUND) {
            currentHistogramState = new HistogramState();
        } else {
            currentHistogramState = graph.getObjectValue(histogramStateAttribute, 0);

            // The histogram state attribute may have been created but not populated yet.
            if (currentHistogramState == null) {
                currentHistogramState = new HistogramState();
            }
        }

        // Ensure that the HistogramState is compatible with the current graph.
        currentHistogramState.validate(graph);

        AttributeType binType = currentHistogramState.getAttributeType();
        binCreators.clear();
        binType.addBinCreators(graph, currentHistogramState.getElementType(), binCreators);

        controls.setHistogramState(currentHistogramState, binCreators);
        display.setBinSelectionMode(currentHistogramState.getBinSelectionMode());

        currentFilter = currentHistogramState.getFilter(currentHistogramState.getElementType());

        selectedAttribute = graph.getAttribute(BinSelector.getSelectionElementType(currentHistogramState.getElementType()), "selected");
        if (selectedAttribute != Graph.NOT_FOUND) {
            currentSelectedModificationCount = graph.getValueModificationCounter(selectedAttribute);
        }

        currentBinCollection = null;
        BinIconMode binIconMode = BinIconMode.NONE;
        BinCreator binCreator = binCreators.get(currentHistogramState.getAttribute());
        binnedAttribute = Graph.NOT_FOUND;
        if (binCreator != null) {
            if (binCreator.isAttributeBased()) {
                GraphElementType elementType = binCreator.getAttributeElementType();
                if (elementType == null) {
                    elementType = currentHistogramState.getElementType();
                }
                binnedAttribute = graph.getAttribute(elementType, currentHistogramState.getAttribute());
                if (binnedAttribute != Graph.NOT_FOUND) {

                    Attribute binnedAttributeRecord = new GraphAttribute(graph, binnedAttribute);
                    if ("icon".equals(binnedAttributeRecord.getAttributeType())) {
                        binIconMode = BinIconMode.ICON;
                    } else if ("color".equals(binnedAttributeRecord.getAttributeType())) {
                        binIconMode = BinIconMode.COLOR;
                    } else {
                        // Do nothing.
                    }

                    currentBinnedModificationCount = graph.getValueModificationCounter(binnedAttribute);
                    currentBinCollection = BinCollection.createBinCollection(graph, currentHistogramState.getElementType(), currentHistogramState.getAttribute(), binCreator, currentFilter, currentHistogramState.getBinFormatter(), currentHistogramState.getBinFormatterParameters());
                    currentBinCollection.sort(currentHistogramState.getBinComparator());
                }
            } else {
                currentBinCollection = BinCollection.createBinCollection(graph, currentHistogramState.getElementType(), null, binCreator, currentFilter, currentHistogramState.getBinFormatter(), currentHistogramState.getBinFormatterParameters());
                currentBinCollection.sort(currentHistogramState.getBinComparator());
            }
        }
        display.setBinCollection(currentBinCollection, binIconMode);
    }

    public void setHistogramViewOptions(final GraphElementType elementType, final AttributeType attributeType, final String attribute) {
        if (currentGraph != null) {
            if (elementType == null) {
                throw new NullPointerException("Null element type");
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
                throw new NullPointerException("Null element type");
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

    void setBinComparator(final BinComparator binComparator) {
        if (currentGraph != null) {
            if (binComparator == null) {
                throw new NullPointerException("Null bin comparator");
            }
            if (currentHistogramState == null || binComparator != currentHistogramState.getBinComparator()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinComparator(binComparator);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    void setBinFormatter(final BinFormatter binFormatter, final PluginParameters parameters) {
        if (currentGraph != null) {
            if (binFormatter == null) {
                throw new NullPointerException("Null bin formatter");
            }
            if (currentHistogramState == null || binFormatter != currentHistogramState.getBinFormatter() || parameters != currentHistogramState.getBinFormatterParameters()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinFormatter(binFormatter);
                newHistogramState.setBinFormatterParameters(parameters);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        if (currentGraph != null) {
            if (binSelectionMode == null) {
                throw new NullPointerException("Null bin selection mode");
            }
            if (currentHistogramState == null || binSelectionMode != currentHistogramState.getBinSelectionMode()) {
                HistogramState newHistogramState = new HistogramState(currentHistogramState);
                newHistogramState.setBinSelectionMode(binSelectionMode);
                PluginExecution.withPlugin(new HistogramStateUpdaterPlugin(newHistogramState)).executeLater(currentGraph);
            }
        }
    }

    void selectOnlyBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new HistogramSelectOnlyBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    void filterOnSelection() {
        if (currentGraph != null) {
            Plugin plugin = new HistogramFilterOnSelectionPlugin();
            PluginParameters params = plugin.createParameters();
            params.getParameters().get(HistogramFilterOnSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(new ElementTypeParameterValue(currentHistogramState.getElementType()));
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(currentGraph);
        }
    }

    void clearFilter() {
        if (currentGraph != null) {
            Plugin plugin = new HistogramClearFilterPlugin();
            PluginParameters params = plugin.createParameters();
            params.getParameters().get(HistogramClearFilterPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(new ElementTypeParameterValue(currentHistogramState.getElementType()));
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(currentGraph);
        }
    }

    void selectBins(final int firstBin, final int lastBin, final boolean select) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new SelectBins(firstBin, lastBin, select)).executeLater(currentGraph);
        }
    }

    void invertBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new InvertBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    void completeBins(final int firstBin, final int lastBin) {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new CompleteBins(firstBin, lastBin)).executeLater(currentGraph);
        }
    }

    void filterSelection() {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new FilterSelection()).executeLater(currentGraph);
        }
    }

    void saveBinsToGraph() {
        if (currentGraph != null && currentBinCollection != null) {
            PluginExecution.withPlugin(new SaveBinsToGraph()).executeLater(currentGraph);
        }
    }

    void saveBinsToClipboard() {
        if (currentGraph != null && currentBinCollection != null) {
            PluginExecution.withPlugin(new SaveBinsToClipboard()).executeLater(currentGraph);
        }
    }

    void expandSelection() {
        if (currentGraph != null) {
            PluginExecution.withPlugin(new ExpandSelection()).executeLater(currentGraph);
        }
    }

    @Override
    public void graphOpened(Graph graph) {
        // Required for GraphManagerListener, intentionally left blank.
    }

    @Override
    public void graphClosed(Graph graph) {
        // Required for GraphManagerListener, intentionally left blank.
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

            SwingUtilities.invokeLater(display::repaint);
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

            SwingUtilities.invokeLater(display::repaint);
        }
    }
}

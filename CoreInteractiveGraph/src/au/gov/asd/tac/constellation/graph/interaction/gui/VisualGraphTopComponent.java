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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.file.SaveNotification;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonWriter;
import au.gov.asd.tac.constellation.graph.file.nebula.NebulaDataObject;
import au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities;
import au.gov.asd.tac.constellation.graph.interaction.framework.GraphVisualManagerFactory;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToClipboardAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CutToClipboardAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.PasteFromClipboardAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.ContractAllCompositesAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.composite.ExpandAllCompositesAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawBlazesAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawConnectionLabelsAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawConnectionsAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawEdgesAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawLinksAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawNodeLabelsAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawNodesAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.DrawTransactionsAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.Toggle3DAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.ToggleGraphVisibilityAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.ToggleDrawDirectedAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.ToggleSelectionModeAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.CloseAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.SaveAsAction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot.RecentGraphScreenshotUtilities;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.GraphNodeFactory;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.plugins.update.GraphUpdateController;
import au.gov.asd.tac.constellation.plugins.update.GraphUpdateManager;
import au.gov.asd.tac.constellation.plugins.update.UpdateComponent;
import au.gov.asd.tac.constellation.plugins.update.UpdateController;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.actions.Savable;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays a JOGL canvas in which to draw a graph.
 *
 * @author algol
 * @author antares
 */
@TopComponent.Description(
        preferredID = "VisualGraphTopComponent",
        iconBase = "au/gov/asd/tac/constellation/graph/interaction/gui/resources/constellation.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 100)
})
@NbBundle.Messages({
    "CTL_VisualGraphAction=Visual Graph",
    "CTL_VisualGraphTopComponent=Visual Graph",
    "HINT_VisualGraphTopComponent=Visual Graph"
})
public final class VisualGraphTopComponent extends CloneableTopComponent implements GraphChangeListener, UndoRedo.Provider {
    
    private static final Logger LOGGER = Logger.getLogger(VisualGraphTopComponent.class.getName());

    public static final String NEW_GRAPH_NAME_PARAMETER_ID = PluginParameter.buildId(VisualGraphTopComponent.class, "graph_name");

    private static final Map<String, BufferedImage> ICON_CACHE = new HashMap<>();
    private static final Icon HIDDEN_ICON = UserInterfaceIconProvider.HIDDEN.buildIcon(16, Color.BLACK);
    private static final Icon VISIBLE_ICON = UserInterfaceIconProvider.VISIBLE.buildIcon(16, Color.BLACK);
    private static final Icon MODE_2D_ICON = UserInterfaceIconProvider.MODE_2D.buildIcon(16);
    private static final Icon MODE_3D_ICON = UserInterfaceIconProvider.MODE_3D.buildIcon(16);
    private static final Icon DRAWING_MODE_ICON = UserInterfaceIconProvider.DRAW_MODE.buildIcon(16);
    private static final Icon SELECT_MODE_ICON = UserInterfaceIconProvider.SELECT_MODE.buildIcon(16);
    private static final Icon DIRECTED_ICON = UserInterfaceIconProvider.DIRECTED.buildIcon(16);
    private static final Icon UNDIRECTED_ICON = UserInterfaceIconProvider.UNDIRECTED.buildIcon(16);

    private static final String MODE_2D_SHORT_DESCRIPTION = "Toggle 2D";
    private static final String MODE_3D_SHORT_DESCRIPTION = "Toggle 3D";
    private static final String DRAWING_MODE_SHORT_DESCRIPTION = "Toggle Draw Mode";
    private static final String SELECTION_MODE_SHORT_DESCRIPTION = "Toggle Selection Mode";
    private static final String DIRECTED_SHORT_DESCRIPTION = "Toggle Draw Directed Transactions";
    private static final String UNDIRECTED_SHORT_DESCRIPTION = "Toggle Draw Undirected Transactions";
    private static final String SHOW_BLAZES_SHORT_DESCRIPTION = "Show Blazes";
    private static final String HIDE_BLAZES_SHORT_DESCRIPTION = "Hide Blazes";
    private static final String SHOW_NODE_LABELS_SHORT_DESCRIPTION = "Show Node Labels";
    private static final String HIDE_NODE_LABELS_SHORT_DESCRIPTION = "Hide Node Labels";
    private static final String SHOW_CONNECTION_LABELS_SHORT_DESCRIPTION = "Show Connection Labels";
    private static final String HIDE_CONNECTION_LABELS_SHORT_DESCRIPTION = "Hide Connection Labels";
    private static final String ENABLE_GRAPH_VISIBILITY_THRESHOLD_LABELS_SHORT_DESCRIPTION = "Enable Graph Visibility Threshold";
    private static final String DISABLE_GRAPH_VISIBILITY_THRESHOLD_LABELS_SHORT_DESCRIPTION = "Disable Graph Visibility Threshold";

    private final GraphVisualManagerFactory graphVisualManagerFactory;
    private final VisualManager visualManager;
    private final InstanceContent content;
    private final Graph graph;
    private MySaveAs saveAs = null;
    private MySavable savable = null;
    private final GraphNode graphNode;

    /**
     * The countBase is the value of the counter at the most recent save when
     * the graph became unmodified).
     */
    private long graphModificationCountBase;
    private long graphModificationCount;

    // Sidebar actions.
    private ContractAllCompositesAction contractCompositesAction;
    private ExpandAllCompositesAction expandCompositesAction;
    private DrawNodesAction drawNodesAction;
    private DrawConnectionsAction drawConnectionsAction;
    private DrawNodeLabelsAction drawNodeLabelsAction;
    private DrawConnectionLabelsAction drawConnectionLabelsAction;
    private DrawBlazesAction drawBlazesAction;
    private DrawLinksAction drawLinksAction;
    private DrawEdgesAction drawEdgesAction;
    private DrawTransactionsAction drawTransactionsAction;
    private Toggle3DAction display3dAction;
    private ToggleSelectionModeAction toggleSelectModeAction;
    private ToggleDrawDirectedAction toggleDrawDirectedAction;
    private ToggleGraphVisibilityAction toggleGraphVisibilityAction;

    private final UpdateController<GraphReadMethods> updateController = new UpdateController<>();
    private final GraphUpdateController graphUpdateController = new GraphUpdateController(updateController);
    private final GraphUpdateManager graphUpdateManager = new GraphUpdateManager(graphUpdateController, 2);

    private static final String SAVE = "Save";
    private static final String DISCARD = "Discard";
    private static final String CANCEL = "Cancel";

    /**
     * Initialise the TopComponent state.
     */
    private void init() {
        displayPanel.add(visualManager.getVisualComponent(), BorderLayout.CENTER);

        DropTargetAdapter dta = new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                final Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked") //files will be list of file which extends from object type
                        final List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        for (final File file : files) {
                            try (final InputStream in = StringUtils.endsWithIgnoreCase(file.getName(), FileExtensionConstants.GZIP) ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file)) {
                                final RecordStore recordStore = RecordStoreUtilities.fromTsv(in);
                                PluginExecution.withPlugin(new ImportRecordFile(recordStore)).executeLater(graph);
                            }
                        }
                    } catch (final UnsupportedFlavorException | IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                        dtde.rejectDrop();
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        };

        displayPanel.setDropTarget(new DropTarget(displayPanel, DnDConstants.ACTION_COPY, dta, true));

        content.add(getActionMap());
        savable = new MySavable();
        saveAs = new MySaveAs();
        content.add(saveAs);
        content.add(graphNode.getDataObject());
        content.add(graph);
        content.add(graphNode);
        associateLookup(new AbstractLookup(content));
        setActivatedNodes(new Node[]{
            graphNode
        });

        getActionMap().put("cut-to-clipboard", new CutToClipboardAction(graphNode));
        getActionMap().put("copy-to-clipboard", new CopyToClipboardAction(graphNode));
        getActionMap().put("paste-from-clipboard", new PasteFromClipboardAction(graphNode));

        // The actions below are per-graph.
        // NetBeans creates a single instance of an action and uses it globally, which doesn't do us any good,
        // because we want to have different toggle states on different graphs, for instance.
        // Therefore, we'll ignore NetBeans and create our own per-graph action instances.
        expandCompositesAction = new ExpandAllCompositesAction(graphNode);
        contractCompositesAction = new ContractAllCompositesAction(graphNode);
        drawNodesAction = new DrawNodesAction(graphNode);
        drawConnectionsAction = new DrawConnectionsAction(graphNode);
        drawNodeLabelsAction = new DrawNodeLabelsAction(graphNode);
        drawConnectionLabelsAction = new DrawConnectionLabelsAction(graphNode);
        drawBlazesAction = new DrawBlazesAction(graphNode);
        final ButtonGroup drawButtonGroup = new ButtonGroup();
        drawLinksAction = new DrawLinksAction(graphNode, drawButtonGroup);
        drawEdgesAction = new DrawEdgesAction(graphNode, drawButtonGroup);
        drawTransactionsAction = new DrawTransactionsAction(graphNode, drawButtonGroup);
        final ButtonGroup displayModeButtonGroup = new ButtonGroup();
        display3dAction = new Toggle3DAction(graphNode, displayModeButtonGroup);
        final ButtonGroup addModeButtonGroup = new ButtonGroup();
        toggleSelectModeAction = new ToggleSelectionModeAction(graphNode, addModeButtonGroup);
        final ButtonGroup directedModeButtonGroup = new ButtonGroup();
        toggleDrawDirectedAction = new ToggleDrawDirectedAction(graphNode, directedModeButtonGroup);
        toggleGraphVisibilityAction = new ToggleGraphVisibilityAction(graphNode);

        final JToolBar sidebar = new JToolBar(SwingConstants.VERTICAL);
        sidebar.setFloatable(false);
        sidebar.setRollover(true);
        sidebar.add(display3dAction.getToolbarPresenter());
        sidebar.addSeparator();

        sidebar.add(drawLinksAction.getToolbarPresenter());
        sidebar.add(drawEdgesAction.getToolbarPresenter());
        sidebar.add(drawTransactionsAction.getToolbarPresenter());
        sidebar.addSeparator();

        sidebar.add(drawNodesAction.getToolbarPresenter());
        sidebar.add(drawConnectionsAction.getToolbarPresenter());
        sidebar.add(drawNodeLabelsAction.getToolbarPresenter());
        sidebar.add(drawConnectionLabelsAction.getToolbarPresenter());
        sidebar.add(drawBlazesAction.getToolbarPresenter());

        sidebar.addSeparator();
        sidebar.add(toggleGraphVisibilityAction.getToolbarPresenter());

        sidebar.addSeparator();
        sidebar.add(expandCompositesAction.getToolbarPresenter());
        sidebar.add(contractCompositesAction.getToolbarPresenter());

        sidebar.addSeparator();
        sidebar.add(toggleSelectModeAction.getToolbarPresenter());
        sidebar.add(toggleDrawDirectedAction.getToolbarPresenter());

        // Add this so the side bar isn't too long.
        // Without this, the side bar has a height that extends past the icons and stops other TopComponents
        // from growing past it.
        sidebar.setMinimumSize(new Dimension(0, 0));

        // Set the modification counters to whatever they are now.
        // This causes any setup changes to be ignored.
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            graphModificationCountBase = rg.getGlobalModificationCounter();
            graphModificationCount = graphModificationCountBase;
        } finally {
            rg.release();
        }

        // Initial update so that the sidebar actions are updated to match the graph.
        visualUpdate();

        this.add(sidebar, BorderLayout.WEST);

        // Listen to graph changes so we can update our modified flag. This will determine
        // whether or not we need to enable saving of the graph.
        graph.addGraphChangeListener(this);

        final InputMap keys = getInputMap(VisualGraphTopComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        final KeyStroke key = KeyStroke.getKeyStroke("Control W");
        final CloseAction ca = new CloseAction(graphNode);
        keys.put(key, ca);

        // Set the icon.
        final Schema schema = graph.getSchema();
        final Image image = getBufferedImageForSchema(schema, false);

        VisualGraphTopComponent.this.setIcon(getNebulaIcon(image));

        final UpdateComponent<GraphReadMethods> visualUpdateComponent = new UpdateComponent<GraphReadMethods>() {

            @Override
            public boolean update(GraphReadMethods updateState) {
                visualUpdate();
                return true;
            }
        };
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.DRAW_FLAGS));
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD));
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.DISPLAY_MODE_3D));
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.DRAWING_MODE));
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS));
        visualUpdateComponent.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.GraphAttribute.CONNECTION_MODE));
        graphUpdateManager.setManaged(true);
    }

    @Override
    public UndoRedo getUndoRedo() {
        return graphNode.getUndoRedoManager();
    }

    /**
     * Construct a new TopComponent with an empty graph.
     */
    public VisualGraphTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(VisualGraphTopComponent.class, "CTL_VisualGraphTopComponent"));
        setToolTipText(NbBundle.getMessage(VisualGraphTopComponent.class, "HINT_VisualGraphTopComponent"));

        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("graph", true);
        this.graph = new DualGraph(null);

        graphVisualManagerFactory = Lookup.getDefault().lookup(GraphVisualManagerFactory.class);
        visualManager = graphVisualManagerFactory.constructVisualManager(graph);
        visualManager.startProcessing();
        graphNode = new GraphNode(graph, gdo, this, visualManager);
        content = new InstanceContent();
        init();
        MemoryManager.newObject(VisualGraphTopComponent.class);
    }

    /**
     * Construct a new TopComponent.
     *
     * @param gdo The DataObject that this graph is associated with.
     * @param graph The graph.
     */
    public VisualGraphTopComponent(final GraphDataObject gdo, final Graph graph) {
        initComponents();
        setName(gdo.getName());
        setToolTipText(gdo.getToolTipText());

        this.graph = graph;
        graphVisualManagerFactory = Lookup.getDefault().lookup(GraphVisualManagerFactory.class);
        visualManager = graphVisualManagerFactory.constructVisualManager(graph);
        visualManager.startProcessing();

        Schema schema = graph.getSchema();
        if (schema instanceof GraphNodeFactory) {
            graphNode = ((GraphNodeFactory) schema).createGraphNode(graph, gdo, this, visualManager);
        } else {
            graphNode = new GraphNode(graph, gdo, this, visualManager);
        }

        content = new InstanceContent();
        init();
        MemoryManager.newObject(VisualGraphTopComponent.class);
    }

    @Override
    public void requestActive() {
        super.requestActive();
        visualManager.getVisualComponent().requestFocusInWindow();
    }

    /**
     * This is required to display the name of the DataObject in the "Save?"
     * dialog box.
     *
     * @return The display name of the DataObject.
     */
    @Override
    public String getDisplayName() {
        // need to check that savable is registered since it will be unregistered with each 'save' call regardless of its outcome
        // therefore we need to re-register, and this method is always called as part of the save command.
        savable.resetRegistry();
        return graphNode.getDataObject().getName();
    }

    /**
     * Return the GraphNode belonging to this TopComponent.
     *
     * @return The GraphNode belonging to this TopComponent.
     */
    public GraphNode getGraphNode() {
        return graphNode;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        displayPanel.setBackground(new java.awt.Color(0, 0, 0));
        displayPanel.setLayout(new java.awt.BorderLayout());
        add(displayPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel displayPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        super.componentOpened();

        // Try to free up any unused memory
        final boolean forceGarbageCollectOnOpen = NbPreferences.forModule(ApplicationPreferenceKeys.class).getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_OPEN, DeveloperPreferenceKeys.FORCE_GC_ON_OPEN_DEFAULT);
        if (forceGarbageCollectOnOpen) {
            System.gc();
        }

        graphUpdateManager.setManaged(true);
    }

    @Override
    public void componentClosed() {
        super.componentClosed();

        setActivatedNodes(new Node[]{});

        graph.removeGraphChangeListener(this);

        visualManager.stopProcessing();
        displayPanel.remove(visualManager.getVisualComponent());
        content.remove(saveAs);
        content.remove(graphNode.getDataObject());
        content.remove(graph);
        content.remove(graphNode);
        content.remove(getActionMap());

        // Get rid of any autosaved files on a user-requested close.
        // Note that canClose() will catch unsaved graphs, so at this point the graph has either been saved or discarded by the user.
        AutosaveUtilities.deleteAutosave(graph.getId());

        graphNode.destroy();

        visualManager.destroy();

        StatusDisplayer.getDefault().setStatusText("Closed " + graphNode.getDataObject().getName());

        if (GraphManager.getDefault().getAllGraphs().isEmpty()) {
            ConstellationIcon.clearCache();
        }

        // Try to free up any unused memory
        final boolean forceGarbageCollectOnClose = NbPreferences.forModule(ApplicationPreferenceKeys.class).getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE, DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE_DEFAULT);
        if (forceGarbageCollectOnClose) {
            System.gc();
        }

        graphUpdateManager.setManaged(false);
    }

    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        long modificationCount;

        ReadableGraph rg = graph.getReadableGraph();
        try {
            modificationCount = rg.getGlobalModificationCounter();
        } finally {
            rg.release();
        }

        if (modificationCount != graphModificationCount && graphModificationCount == graphModificationCountBase) {
            graphModificationCount = modificationCount;
            savable.setModified(true);
            SwingUtilities.invokeLater(() -> {
                setHtmlDisplayName(String.format("<html><font color=\"#0000ff\"><b>%s</b></font></html>", getDisplayName()));
                requestVisible();
            });
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            MemoryManager.finalizeObject(VisualGraphTopComponent.class);
        } finally {
            super.finalize();
        }
    }

    private void visualUpdate() {

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int drawFlagsAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.get(rg);
            final int visibleAboveThresholdAttribute = VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD.get(rg);
            final int displayModeIs3DAttribute = VisualConcept.GraphAttribute.DISPLAY_MODE_3D.get(rg);
            final int drawingModeAttribute = VisualConcept.GraphAttribute.DRAWING_MODE.get(rg);
            final int drawDirectedAttribute = VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS.get(rg);
            final int connectionModeAttribute = VisualConcept.GraphAttribute.CONNECTION_MODE.get(rg);

            // Read relevant visual attributes from the graph and update the sidebar.
            final DrawFlags drawFlags;
            final ConnectionMode connectionMode;
            final boolean visibleAboveThreshold;
            final boolean isDisplay3D;
            final boolean isDrawingMode;
            final boolean isDrawingDirectedTransactions;
            drawFlags = drawFlagsAttribute != Graph.NOT_FOUND ? rg.getObjectValue(drawFlagsAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAW_FLAGS;
            visibleAboveThreshold = visibleAboveThresholdAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(visibleAboveThresholdAttribute, 0) : VisualGraphDefaults.DEFAULT_GRAPH_VISIBILITY;
            isDisplay3D = displayModeIs3DAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(displayModeIs3DAttribute, 0) : VisualGraphDefaults.DEFAULT_DISPLAY_MODE_3D;
            isDrawingMode = drawingModeAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(drawingModeAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_MODE;
            isDrawingDirectedTransactions = drawDirectedAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(drawDirectedAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_DIRECTED_TRANSACTIONS;
            connectionMode = connectionModeAttribute != Graph.NOT_FOUND ? rg.getObjectValue(connectionModeAttribute, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_MODE;

            drawNodesAction.putValue(Action.SELECTED_KEY, drawFlags.drawNodes());
            drawConnectionsAction.putValue(Action.SELECTED_KEY, drawFlags.drawConnections());
            drawNodeLabelsAction.putValue(Action.SELECTED_KEY, drawFlags.drawNodeLabels());
            drawNodeLabelsAction.putValue(Action.SHORT_DESCRIPTION, drawFlags.drawNodeLabels() ? HIDE_NODE_LABELS_SHORT_DESCRIPTION : SHOW_NODE_LABELS_SHORT_DESCRIPTION);
            drawConnectionLabelsAction.putValue(Action.SELECTED_KEY, drawFlags.drawConnectionLabels());
            drawConnectionLabelsAction.putValue(Action.SHORT_DESCRIPTION, drawFlags.drawConnectionLabels() ? HIDE_CONNECTION_LABELS_SHORT_DESCRIPTION : SHOW_CONNECTION_LABELS_SHORT_DESCRIPTION);
            drawBlazesAction.putValue(Action.SELECTED_KEY, drawFlags.drawBlazes());
            drawBlazesAction.putValue(Action.SHORT_DESCRIPTION, drawFlags.drawBlazes() ? HIDE_BLAZES_SHORT_DESCRIPTION : SHOW_BLAZES_SHORT_DESCRIPTION);
            display3dAction.putValue(Action.SELECTED_KEY, isDisplay3D);
            display3dAction.putValue(Action.SMALL_ICON, isDisplay3D ? MODE_3D_ICON : MODE_2D_ICON);
            display3dAction.putValue(Action.SHORT_DESCRIPTION, isDisplay3D ? MODE_3D_SHORT_DESCRIPTION : MODE_2D_SHORT_DESCRIPTION);
            toggleGraphVisibilityAction.putValue(Action.SELECTED_KEY, visibleAboveThreshold);
            toggleGraphVisibilityAction.putValue(Action.SMALL_ICON, visibleAboveThreshold ? VISIBLE_ICON : HIDDEN_ICON);
            toggleGraphVisibilityAction.putValue(Action.SHORT_DESCRIPTION, visibleAboveThreshold ? DISABLE_GRAPH_VISIBILITY_THRESHOLD_LABELS_SHORT_DESCRIPTION : ENABLE_GRAPH_VISIBILITY_THRESHOLD_LABELS_SHORT_DESCRIPTION);
            toggleSelectModeAction.putValue(Action.SELECTED_KEY, isDrawingMode);
            toggleSelectModeAction.putValue(Action.SMALL_ICON, isDrawingMode ? DRAWING_MODE_ICON : SELECT_MODE_ICON);
            toggleSelectModeAction.putValue(Action.SHORT_DESCRIPTION, isDrawingMode ? DRAWING_MODE_SHORT_DESCRIPTION : SELECTION_MODE_SHORT_DESCRIPTION);
            toggleDrawDirectedAction.putValue(Action.SELECTED_KEY, isDrawingDirectedTransactions);
            toggleDrawDirectedAction.putValue(Action.SMALL_ICON, isDrawingDirectedTransactions ? DIRECTED_ICON : UNDIRECTED_ICON);
            toggleDrawDirectedAction.putValue(Action.SHORT_DESCRIPTION, isDrawingDirectedTransactions ? DIRECTED_SHORT_DESCRIPTION : UNDIRECTED_SHORT_DESCRIPTION);
            toggleDrawDirectedAction.setEnabled(isDrawingMode);

            switch (connectionMode) {
                case LINK:
                    drawLinksAction.putValue(Action.SELECTED_KEY, true);
                    break;
                case EDGE:
                    drawEdgesAction.putValue(Action.SELECTED_KEY, true);
                    break;
                case TRANSACTION:
                    drawTransactionsAction.putValue(Action.SELECTED_KEY, true);
                    break;
                default:
                    throw new IllegalStateException("Unknown ConnectionMode: " + connectionMode);
            }
        } finally {
            rg.release();
        }
    }

    @Override
    public boolean canClose() {
        if (savable.isModified()) {
            final String message = String.format("Graph %s is modified. Save?", getDisplayName());
            final Object[] options = new Object[]{
                SAVE, DISCARD, CANCEL
            };
            final NotifyDescriptor d = new NotifyDescriptor(message, "Close", NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, options, "Save");
            final Object o = DialogDisplayer.getDefault().notify(d);

            if (o.equals(DISCARD)) {
                savable.setModified(false);
            } else if (o.equals(SAVE)) {
                try {
                    savable.handleSave();
                    return savable.isSaved();
                    
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public Action[] getActions() {
        // Add new actions above the default actions.
        final ArrayList<Action> actionList = new ArrayList<>();

        // An action that closes the topcomponent without saving the (possibly modified) graph.
        final Action discard = new AbstractAction(DISCARD) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                savable.setModified(false);
                close();
            }
        };
        actionList.add(discard);

        // If this graph is in a nebula, add some nebula-related actions.
        final NebulaDataObject nebula = getGraphNode().getDataObject().getNebulaDataObject();
        if (nebula != null) {
            // Discard the nebula without saving.
            final Action discardNebula = new AbstractAction("Discard nebula") {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    TopComponent.getRegistry().getOpened().stream().filter(tc -> (tc instanceof VisualGraphTopComponent)).map(tc -> (VisualGraphTopComponent) tc).forEach(vtc -> {
                        final NebulaDataObject ndo = vtc.getGraphNode().getDataObject().getNebulaDataObject();
                        if (nebula.equalsPath(ndo)) {
                            vtc.savable.setModified(false);
                            vtc.close();
                        }
                    });
                }
            };
            actionList.add(discardNebula);

            // Are there any graphs in this nebula (if it exists) that need saving?
            final List<Savable> savables = getNebulaSavables(nebula);
            if (!savables.isEmpty()) {
                // There's at least one graph in this nebula that needs saving...
                final Action saveNebula = new AbstractAction("Save nebula") {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        try {
                            for (final Savable s : savables) {
                                s.save();
                            }
                        } catch (final IOException ex) {
                            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                        }
                    }
                };
                actionList.add(saveNebula);
            } else {
                // No graphs in this nebula need saving, so offer to close the nebula.
                final Action closeNebula = new AbstractAction("Close nebula") {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        TopComponent.getRegistry().getOpened().stream().filter(tc -> (tc instanceof VisualGraphTopComponent)).map(tc -> (VisualGraphTopComponent) tc).forEach(vtc -> {
                            final NebulaDataObject ndo = vtc.getGraphNode().getDataObject().getNebulaDataObject();
                            if (nebula.equalsPath(ndo)) {
                                vtc.close();
                            }
                        });
                    }
                };
                actionList.add(closeNebula);
            }
        }

        // An action that renames the topcomponent without saving the (possibly modified) graph.
        final Action rename = new AbstractAction("Rename") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final PluginParameters parameters = new PluginParameters();
                final PluginParameter<StringParameterValue> newGraphNameParameter = StringParameterType.build(NEW_GRAPH_NAME_PARAMETER_ID);
                newGraphNameParameter.setName("New Graph Name");
                newGraphNameParameter.setStringValue(graphNode.getDisplayName());
                newGraphNameParameter.storeRecentValue();
                parameters.addParameter(newGraphNameParameter);

                final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog("Rename Graph", parameters);
                dialog.showAndWait();

                if (dialog.isAccepted()) {
                    final String newGraphName = parameters.getStringValue(NEW_GRAPH_NAME_PARAMETER_ID);

                    if (!newGraphName.isEmpty()) {
                        try {
                            // set the graph object name so the name is retained when you Save As
                            graphNode.getDataObject().rename(newGraphName);

                            // set the other graph name properties
                            graphNode.setName(newGraphName);
                            graphNode.setDisplayName(newGraphName);

                            // set the top component
                            setName(newGraphName);
                            setDisplayName(newGraphName);
                            setHtmlDisplayName(newGraphName); // this changes the text on the tab
                        } catch (final IOException ex) {
                            throw new RuntimeException(String.format("The name %s already exists.", newGraphName), ex);
                        }
                        savable.setModified(true);
                    }
                }
            }
        };
        actionList.add(rename);

        // Add the default actions.
        for (final Action action : super.getActions()) {
            actionList.add(action);
        }

        return actionList.toArray(new Action[actionList.size()]);
    }

    public boolean forceClose() {
        savable.setModified(false);
        return close();
    }

    public void saveGraph() throws IOException {
        savable.handleSave();
    }

    /**
     * A List of Savable instances in this nebula.
     *
     * @param nebula The nebula that this graph is in.
     *
     * @return A List of Savable instances in this nebula.
     */
    private static List<Savable> getNebulaSavables(final NebulaDataObject nebula) {
        final List<Savable> savableList = new ArrayList<>();
        final Collection<? extends Savable> savables = Savable.REGISTRY.lookupAll(Savable.class);
        savables.stream().filter(s -> (s instanceof MySavable)).forEach(s -> {
            final NebulaDataObject otherNDO = ((MySavable) s).tc().getGraphNode().getDataObject().getNebulaDataObject();
            if (nebula.equalsPath(otherNDO)) {
                savableList.add(s);
            }
        });

        return savableList;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.visual.about");
    }

    private Image getNebulaIcon(final Image image) {
        final Color nebulaColor = getGraphNode().getDataObject().getNebulaColor();
        if (nebulaColor == null) {
            return image;
        }

        final int w = 6;
        final int h = 16;

        final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = bi.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        g.setColor(nebulaColor);
        g.fillRect(1, 1, w - 2, h - 2);
        g.dispose();

        return ImageUtilities.mergeImages(bi, image, w, 0);
    }

    /**
     * Cache a BufferedImage per schema so that it can be retrieved to avoid an
     * icon rebuild
     *
     * @param schema The Schema representing the graph
     * @return A BufferedImage for the schema
     */
    private BufferedImage getBufferedImageForSchema(Schema schema, final boolean isModified) {
        if (schema == null) {
            schema = SchemaFactoryUtilities.getDefaultSchemaFactory().createSchema();
        }

        if (isModified) {
            if (!ICON_CACHE.containsKey(schema.getFactory().getModifiedIcon().getName())) {
                ICON_CACHE.put(schema.getFactory().getModifiedIcon().getName(),
                        schema.getFactory().getModifiedIcon().buildBufferedImage(16));
            }
            return ICON_CACHE.get(schema.getFactory().getModifiedIcon().getName());
        } else {
            if (!ICON_CACHE.containsKey(schema.getFactory().getIcon().getName())) {
                ICON_CACHE.put(schema.getFactory().getIcon().getName(),
                        schema.getFactory().getIcon().buildBufferedImage(16));
            }
            return ICON_CACHE.get(schema.getFactory().getIcon().getName());
        }
    }

    /**
     * A custom Savable.
     */
    private class MySavable extends AbstractSavable implements Icon {

        private boolean isModified;
        private boolean isSaved = false;

        /**
         * Construct a new MySavable instance.
         */
        MySavable() {
            isModified = false;
        }

        /**
         * Set this savable as modified/unmodified.
         * <p>
         * The Savable will be registered/unregistered with the SavableRegistry
         * as required.
         *
         * @param modified Modification flag.
         */
        public void setModified(final boolean modified) {
            if (modified) {
                if (!isModified) {
                    content.add(this);
                    register();
                }
            } else {
                if (isModified) {
                    content.remove(this);
                    unregister();
                }
            }

            Schema schema = graph.getSchema();
            final Image image = getBufferedImageForSchema(schema, modified);

            VisualGraphTopComponent.this.setIcon(getNebulaIcon(image));

            isModified = modified;
        }

        /**
         * Has this Savable been modified?
         *
         * @return True if modified, false if not.
         */
        public boolean isModified() {
            return isModified;
        }

        public boolean isSaved() {
            return isSaved;
        }

        /**
         * register the instance if in modified state
         */
        public void resetRegistry() {
            if (this.isModified()) {
                register();
            }
        }

        @Override
        protected String findDisplayName() {
            return getDisplayName();
        }

        /**
         * Save the graph.
         * <p>
         * The graph file is not overwritten. This would be dangerous, since a
         * large graph may take some time to write, and an interruption would
         * leave a corrupted file. Instead, the graph is written to a new file;
         * when the write is complete, the old file is deleted and the new file
         * is renamed.
         *
         * @throws IOException When I/O errors happen.
         */
        @Override
        protected void handleSave() throws IOException {
            final GraphDataObject gdo = graphNode.getDataObject();

            if (gdo.isInMemory()) {
                // We don't want to do a save if this is an in-memory DataObject.
                // Instead, we'll do the "Save As..." action and let it naturally do the right thing.
                // We have to make sure that this TopComponent is the the active one, because SaveAsAction
                // just saves the current graph. If we don't do this and we have multiple graphs, the same
                // graph will get saved each time.
                requestActive();

                SaveAsAction action = new SaveAsAction();               
                action.actionPerformed(null);
                isSaved = action.isSaved();
                return;
            }

            final String name = gdo.getName();

            // Create a new file and write to it.
            final String tmpnam = String.format("%s_tmp%08x", name, gdo.hashCode());
            final GraphDataObject freshGdo = (GraphDataObject) gdo.createFromTemplate(gdo.getFolder(), tmpnam);
            final BackgroundWriter writer = new BackgroundWriter(name, freshGdo, true);
            writer.execute();
            isSaved = true;
        }

        /**
         * Return the parent VisualTopComponent.
         *
         * @return The parent VisualTopComponent.
         */
        VisualGraphTopComponent tc() {
            return VisualGraphTopComponent.this;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() == obj.getClass()) {
                final MySavable m = (MySavable) obj;
                return tc() == m.tc();
            }

            return false;
        }

        @Override
        public int hashCode() {
            return tc().hashCode();
        }

        @Override
        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            final Schema schema = graph.getSchema();
            final Icon icon = ImageUtilities.image2Icon(getBufferedImageForSchema(schema, false));
            icon.paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            final Schema schema = graph.getSchema();
            final Icon icon = ImageUtilities.image2Icon(getBufferedImageForSchema(schema, false));
            return icon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            final Schema schema = graph.getSchema();
            final Icon icon = ImageUtilities.image2Icon(getBufferedImageForSchema(schema, false));
            return icon.getIconHeight();
        }
    }

    private class MySaveAs implements SaveAsCapable {

        @Override
        public void saveAs(final FileObject folder, String name) throws IOException {
            StatusDisplayer.getDefault().setStatusText("Save as " + folder.getPath() + "(" + name + ")");

            // The Save As dialog box has already asked if we want to overwrite an existing file,
            // so just go ahead and delete it if it exists.
            final FileObject existing = folder.getFileObject(name);
            if (existing != null) {
                existing.delete();
            }

            final String ext = FileExtensionConstants.STAR;
            if (name.endsWith(ext)) {
                name = name.substring(0, name.length() - ext.length());
            }

            final File newFile = new File(folder.getPath(), name + ext);
            final FileObject fo = FileUtil.createData(newFile);
            final GraphDataObject freshGdo = (GraphDataObject) DataObject.find(fo);
            final BackgroundWriter writer = new BackgroundWriter(name, freshGdo, false);
            writer.execute();
        }
    }

    /**
     * Write a graph to a file in a background thread so the EDT doesn't freeze.
     */
    private class BackgroundWriter extends SwingWorker<Void, Object> {

        private final String name;
        private final GraphDataObject freshGdo;
        private final boolean deleteOldGdo;
        private boolean cancelled;
        private GraphReadMethods copy;

        /**
         * Construct a new BackgroundWriter.
         *
         * @param name The name of the file to write.
         * @param freshGdo The current GraphDataObject will be replaced by this
         * GDO in the Lookup if the write succeeds.
         * @param deleteOldGdo If true, delete the file represented by the old
         * GDO.
         */
        BackgroundWriter(final String name, final GraphDataObject freshGdo, final boolean deleteOldGdo) {
            this.name = name;
            this.freshGdo = freshGdo;
            this.deleteOldGdo = deleteOldGdo;
            cancelled = false;

            GraphNode.getGraphNode(graph).makeBusy(true);
        }

        @Override
        protected Void doInBackground() throws Exception {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                copy = rg.copy();
            } finally {
                rg.release();
            }
            try {

                // Create a 'backup' copy of the file being saved before its saved. This is done to ensure that there are
                // two distinct saves/write operations that occur meaning that if the application was to terminate on one
                // of them, then 'one' of the files should be valid still.
                // i.e.:
                //  *  if the 'copy' operation fails, then the original file will still be there intact
                //  *  if the 'write' operation falls over, then we know the backup copy must have already occured so we
                //     have something to fall back on
                // This allows load logic to be of the form:
                //   - try to load star file
                //     - if star file loads successfully use it
                //     - if the star file fails to load, check to see if a 'backup' exists
                //        - if backup was found attempt to load it
                //        - if backup was not found throw load error
                final FileObject fileobj = freshGdo.getPrimaryFile();
                final File srcFile = new File(fileobj.getPath());
                final String srcfilePath = srcFile.getParent().concat(File.separator).concat(this.name).concat(".").concat(fileobj.getExt());

                if (srcFile.exists() && !srcFile.isDirectory() && FileUtils.sizeOf(srcFile) > 0) {
                    // Create a backup copy of the file before overwriting it. If the backup copy fails, then code will never
                    // get to execute the save, so the actual file should remain intact. If the save fails, the backup file will
                    // already have been written.
                    FileUtils.copyFile(new File(srcfilePath), new File(srcfilePath.concat(FileExtensionConstants.BACKUP)));
                }

                try (OutputStream out = new BufferedOutputStream(freshGdo.getPrimaryFile().getOutputStream())) {
                    // Write the graph.
                    cancelled = new GraphJsonWriter().writeGraphToZip(copy, out, new HandleIoProgress("Writing..."));
                }
                SaveNotification.saved(freshGdo.getPrimaryFile().getPath());
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                if (cancelled) {
                    freshGdo.delete();
                } else {
                    GraphDataObject gdo = graphNode.getDataObject();
                    freshGdo.setNebulaDataObject(gdo.getNebulaDataObject());
                    freshGdo.setNebulaColor(gdo.getNebulaColor());

                    // Delete the old DataObject and remove the old GDO from the lookup.
                    if (deleteOldGdo) {
                        gdo.delete();
                    }
                    content.remove(gdo);

                    // Rename the new file and add the new GDO to the lookup.
                    freshGdo.rename(name);
                    gdo = freshGdo;
                    content.add(gdo);

                    graphNode.setDataObject(gdo);

                    setToolTipText(gdo.getToolTipText());

                    // Reset the modification data.
                    setHtmlDisplayName(getDisplayName());
                    graphModificationCountBase = copy.getGlobalModificationCounter();
                    graphModificationCount = graphModificationCountBase;
                    savable.setModified(false);
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            PluginExecution.withPlugin(new WriteGraphFile(copy, freshGdo)).executeLater(null);

            if (GraphNode.getGraphNode(graph) != null) {
                GraphNode.getGraphNode(graph).makeBusy(false);
            }
        }
    }

    /**
     * Plugin to import the record file.
     */
    @PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
    private class ImportRecordFile extends SimpleEditPlugin {

        private final RecordStore recordStore;

        public ImportRecordFile(final RecordStore recordStore) {
            this.recordStore = recordStore;
        }

        @Override
        public String getName() {
            return "Visual Graph: Import Record File";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            GraphRecordStoreUtilities.addRecordStoreToGraph(graph, recordStore, false, true, null);
        }
    }

    /**
     * Plugin to write the graph file.
     */
    @PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
    private class WriteGraphFile extends SimplePlugin {

        private final GraphReadMethods copy;
        private final GraphDataObject freshGdo;

        public WriteGraphFile(final GraphReadMethods copy, final GraphDataObject freshGdo) {
            this.copy = copy;
            this.freshGdo = freshGdo;
        }

        @Override
        public String getName() {
            return "Visual Graph: Write Graph File";
        }

        @Override
        protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            final File file = new File(freshGdo.getPrimaryFile().getPath());
            ConstellationLoggerHelper.exportPropertyBuilder(
                    this,
                    GraphRecordStoreUtilities.getVertices(copy, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                    file,
                    ConstellationLoggerHelper.SUCCESS
            );
            RecentGraphScreenshotUtilities.takeScreenshot(file.getAbsolutePath());
        }
    }
}

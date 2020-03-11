/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.Segment;
import au.gov.asd.tac.constellation.views.tableview.state.TableState;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Top component which displays a table of graph data.
 *
 * @author algol
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.views.tableview//TableView//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TableViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/tableview/resources/table-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TableViewAction",
        preferredID = "TableViewTopComponent"
)
@Messages({
    "CTL_TableViewAction=Table View",
    "CTL_TableViewTopComponent=Table View",
    "HINT_TableViewTopComponent=Table View",
    "BTN_ChooseColumns=Select Columns",
    "BTN_ShowSelected=Show selected elements only",
    "BTN_ShowNodeAttributes=Show Node Attributes",
    "BTN_ShowTransactionAttributes=Show Transaction Attributes",
    "MSG_CopyAllRows=Copy All Rows",
    "MSG_ExportRows=Export Rows",
    "MSG_CopyTable=Copy Table",
    "MSG_CopySelectedRows=Copy Selected Rows",
    "MSG_CopyCell=Copy Cell",
    "MSG_CopyColumn=Copy Column",
    "MSG_CopyColumns=Copy Columns...",
    "MSG_AllCSV=All Rows to CSV...",
    "MSG_AllExcel=All Rows to Excel...",
    "MSG_AllRows=All Rows",
    "MSG_SelectedRows=Selected Rows",
    "MSG_SelectedExcel=Selected Rows to Excel...",
    "MSG_SelectedCSV=Selected Rows to CSV...",
    "MSG_Commas=Use Comma Separators",
    "MSG_Tabs=Using Tab Separators"
})
public final class TableViewTopComponent extends TopComponent implements PropertyChangeListener, ListSelectionListener, GraphChangeListener, LookupListener, UndoRedo.Provider, PreferenceChangeListener {

    static final String ALL_PREFIX = "A/";
    static final String SEL_PREFIX = "S/";

    // A sentinal that indicates we are writing our own state to the graph,
    // so no action should be taken.
    private static final Object OWN_WRITE = new Object();

    @StaticResource
    private static final String SELECTED_ONLY_ICON = "au/gov/asd/tac/constellation/views/tableview/resources/selected-only.png";
    protected static final Icon SELECTED_ONLY = ImageUtilities.loadImageIcon(SELECTED_ONLY_ICON, false);
    protected static final Icon VX_ICON = UserInterfaceIconProvider.NODES.buildIcon(16);
    protected static final Icon TX_ICON = UserInterfaceIconProvider.CONNECTIONS.buildIcon(16);

    private static final String TABLE_VIEW_STATE_UPDATER_THREAD_NAME = "Table View State Updater";

    private final Lookup.Result<GraphNode> result;
    private GraphNode graphNode;
    private final ButtonGroup group;
    private boolean isAdjusting;
    private final Action columnsAction;
    private final Action selectedOnlyAction;
    private final Action vxAction;
    private final Action txAction;
    private long attributeModificationCounter;
    private long structureModificationCounter;
    private long selectedModificationCounter;
    private GraphElementType currentElementType;
    private final CopyDataToClipboard copyClipboard;
    private final CopyDataToExcelFile copyExcel;

    private final JToggleButton selectedOnlyButton;
    private final JToggleButton vxButton;
    private final JToggleButton txButton;

    public TableViewTopComponent() {
        initComponents();
        setName(Bundle.CTL_TableViewTopComponent());
        setToolTipText(Bundle.HINT_TableViewTopComponent());

        dataTable.getTableHeader().setDefaultRenderer(new CustomTableHeaderRenderer(dataTable.getTableHeader().getDefaultRenderer()));

        final TableCellRenderer tcr = new CustomCellRenderer();
        dataTable.setDefaultRenderer(Boolean.class, tcr);
        dataTable.setDefaultRenderer(Float.class, tcr);
        dataTable.setDefaultRenderer(Integer.class, tcr);
        dataTable.setDefaultRenderer(String.class, tcr);

        // The dataTable always has a model with the current GraphElementType, even if the graph is null.
        dataTable.setModel(new GraphTableModel(null, GraphElementType.VERTEX));

        // Populate the sidebar.
        columnsAction = new ColumnsAction();
        selectedOnlyAction = new SelectedOnlyAction();
        vxAction = new VertexAction();
        txAction = new TransactionAction();
        final JButton columnsButton = new JButton(columnsAction);
        columnsButton.setToolTipText(Bundle.BTN_ChooseColumns());
        selectedOnlyButton = new JToggleButton(selectedOnlyAction);
        selectedOnlyButton.setToolTipText(Bundle.BTN_ShowSelected());
        vxButton = new JToggleButton(vxAction);
        vxButton.setToolTipText(Bundle.BTN_ShowNodeAttributes());
        txButton = new JToggleButton(txAction);
        txButton.setToolTipText(Bundle.BTN_ShowTransactionAttributes());
        sidebar.add(columnsButton);
        sidebar.add(selectedOnlyButton);
        sidebar.add(vxButton);
        sidebar.add(txButton);
        group = new ButtonGroup();
        group.add(vxButton);
        group.add(txButton);
        vxButton.setSelected(true);
        currentElementType = GraphElementType.VERTEX;

        dataTable.getSelectionModel().addListSelectionListener(this);
        dataTable.addKeyListener(new DataTableKeyListener());
        isAdjusting = false;

        copyClipboard = new CopyDataToClipboard(this.getName(), dataTable);

        copyExcel = new CopyDataToExcelFile(this.getName(), dataTable);

        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
        initTableContextMenu();
        setTableListeners();

        // Override default CNTL_C from the tabel with our own command.
        final ActionMap map = dataTable.getActionMap();
        map.put("copy", copyClipboard);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.datatable.about");
    }

    private void setTableFont() {
        // Get the standard font and adjust the row height to suit.
        // Adding one to the font height looks a little bit nicer.
        final Font font = FontUtilities.getOutputFont();
        dataTable.setFont(font);
        final FontMetrics fm = this.getFontMetrics(font);
        dataTable.setRowHeight(fm.getHeight() + 1);
    }

    /**
     * Returns indices of rows that are selected on the graph.
     * <p>
     * This is not the same as the selected data from the table.
     */
    /**
     * Initialise context menu and menu items.
     */
    private void initTableContextMenu() {
        // Right click context model initialisation.
        final JMenu copyAllMenu = new JMenu(Bundle.MSG_CopyAllRows());

        final JMenuItem copyAllCommasItem = new JMenuItem(Bundle.MSG_Commas());
        copyAllCommasItem.setActionCommand(ALL_PREFIX + Bundle.MSG_Commas());
        copyAllCommasItem.addActionListener(copyClipboard);

        final JMenuItem copyAllTabsItem = new JMenuItem(Bundle.MSG_Tabs());
        copyAllTabsItem.setActionCommand(ALL_PREFIX + Bundle.MSG_Tabs());
        copyAllTabsItem.addActionListener(copyClipboard);

        copyAllMenu.add(copyAllCommasItem);
        copyAllMenu.add(copyAllTabsItem);

        final JMenu copySelectedMenu = new JMenu(Bundle.MSG_CopySelectedRows());

        final JMenuItem copySelectedCommasItem = new JMenuItem(Bundle.MSG_Commas());
        copySelectedCommasItem.setActionCommand(SEL_PREFIX + Bundle.MSG_Commas());
        copySelectedCommasItem.addActionListener(copyClipboard);

        final JMenuItem copySelectedTabsItem = new JMenuItem(Bundle.MSG_Tabs());
        copySelectedTabsItem.setActionCommand(SEL_PREFIX + Bundle.MSG_Tabs());
        copySelectedTabsItem.addActionListener(copyClipboard);

        copySelectedMenu.add(copySelectedCommasItem);
        copySelectedMenu.add(copySelectedTabsItem);

        final JMenuItem copyCellItem = new JMenuItem(Bundle.MSG_CopyCell());
        copyCellItem.addActionListener(copyClipboard);

        final JMenuItem copyTableItem = new JMenuItem(Bundle.MSG_CopyTable());
        copyTableItem.addActionListener(copyClipboard);

        final JMenuItem copyColumn = new JMenuItem(Bundle.MSG_CopyColumn());
        copyColumn.addActionListener(copyClipboard);

        final JMenuItem copyColumnItem = new JMenuItem(Bundle.MSG_CopyColumns());
        copyColumnItem.addActionListener((final ActionEvent e) -> {
            createSelectColumnDialog();
        });

        final JMenuItem csvAllItem = new JMenuItem(Bundle.MSG_AllCSV());
        csvAllItem.addActionListener(copyClipboard);

        final JMenuItem csvSelectedItem = new JMenuItem(Bundle.MSG_SelectedCSV());
        csvSelectedItem.addActionListener(copyClipboard);

        final JMenuItem excelAllItem = new JMenuItem(Bundle.MSG_AllExcel());
        excelAllItem.addActionListener(copyExcel);

        final JMenuItem excelSelectedItem = new JMenuItem(Bundle.MSG_SelectedExcel());
        excelSelectedItem.addActionListener(copyExcel);

        rightClickMenu.add(copyAllMenu);
        rightClickMenu.add(copySelectedMenu);
        rightClickMenu.add(copyCellItem);
        rightClickMenu.add(copyTableItem);
        rightClickMenu.add(copyColumn);
        rightClickMenu.add(copyColumnItem);

        //combine into a single "export" menu
        final JMenu exportMenu = new JMenu(Bundle.MSG_ExportRows());

        final JMenu exportAllMenu = new JMenu(Bundle.MSG_AllRows());
        exportAllMenu.add(csvAllItem);
        exportAllMenu.add(excelAllItem);

        final JMenu exportSelectedMenu = new JMenu(Bundle.MSG_SelectedRows());
        exportSelectedMenu.add(csvSelectedItem);
        exportSelectedMenu.add(excelSelectedItem);

        exportMenu.add(exportAllMenu);
        exportMenu.add(exportSelectedMenu);

        rightClickMenu.add(exportMenu);

    }

    @Override
    public UndoRedo getUndoRedo() {
        return graphNode == null ? null : graphNode.getUndoRedoManager();
    }

    private void createSelectColumnDialog() {
        final ColumnSelectPanel panel = new ColumnSelectPanel(dataTable);
        final JButton cancelButton = new JButton("Cancel");
        final JButton okButton = new JButton("OK");
        final DialogDescriptor dd = new DialogDescriptor(panel, "Select Column...", true, new Object[]{
            okButton, cancelButton
        }, "OK", DialogDescriptor.DEFAULT_ALIGN, null, (final ActionEvent e) -> {
            if (e.getActionCommand().equals("OK")) {
                try {
                    panel.okButtonActionPerformed(e);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        dd.setClosingOptions(new JButton[]{
            cancelButton, okButton
        });
        Dialog d;
        d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setPreferredSize(new Dimension(200, 350));
        d.setMinimumSize(new Dimension(350, 450));
        d.setVisible(true);
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes != null && nodes.length == 1 && nodes[0] instanceof GraphNode) {
            final GraphNode gnode = ((GraphNode) nodes[0]);
            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }

    /**
     * Change to the specified view and order the rows by descending selected
     * value, so selected elements are at the top.
     *
     * @param etype The GraphElementType to show.
     * @param id The id that the action was invoked on.
     */
    public void showSelected(final GraphElementType etype, final int id) {
        // Nothing has happened to the graph, but we want to make it look like something happened so the table selection
        // will be updated. The simplest way to do this is to pretend that the selection has changed.
        selectedModificationCounter--;

        if (etype == GraphElementType.VERTEX) {
            vxButton.doClick();
        } else if (etype == GraphElementType.TRANSACTION) {
            txButton.doClick();
        }

        final int row = dataTable.convertRowIndexToView(id);
        final Rectangle cellRect = dataTable.getCellRect(row, 0, false);
        dataTable.scrollRectToVisible(cellRect);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rightClickMenu = new javax.swing.JPopupMenu();
        jScrollPane1 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        sidebar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        dataTable.setAutoCreateColumnsFromModel(false);
        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        dataTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(dataTable);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        sidebar.setFloatable(false);
        sidebar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        sidebar.setRollover(true);
        add(sidebar, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Shows the context menu when right click occurs within the dataTable
     * component.
     *
     * @param evt
     */
    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
        //BUTTON3 is Right click
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int xOffset = evt.getX();
            int yOffset = evt.getY();
            copyClipboard.setMousePosition(evt.getPoint());
            rightClickMenu.show(dataTable, xOffset, yOffset);
        }

    }//GEN-LAST:event_dataTableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable dataTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JToolBar sidebar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        result.addLookupListener(this);
        resultChanged(null);

        setTableFont();
        PreferenceUtilites.addPreferenceChangeListener(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE, this);
    }

    @Override
    public void componentClosed() {
        PreferenceUtilites.removePreferenceChangeListener(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE, this);
        result.removeLookupListener(this);
        setNode(null);
    }

    void writeProperties(final java.util.Properties p) {
    }

    void readProperties(final java.util.Properties p) {
    }

    /**
     * Listen to selection changes in the table so we can reflect them into the
     * graph.
     * <p>
     * Called when the list selection changes.
     *
     * @param e ListSelectionEvent.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        // If we're switching graph element type, a model with no rows is set.
        // This would cause the graph selections for that element type to be removed.
        // We don't want that.
        if (dataTable.getModel().getRowCount() == 0) {
            return;
        }

        if (e.getFirstIndex() < 0 || e.getValueIsAdjusting()) {
            // We have a TableModelEvent.HEADER_ROW (or similar) indicating that something like the sort order has changed.
            // This isn't related to graph data, so give up immediately.
            return;
        }

        if (isAdjusting || dataTable.getSelectionModel().getValueIsAdjusting()) {
            return;
        }

        isAdjusting = true;
        dataTable.getSelectionModel().setValueIsAdjusting(true);
        try {
            final ListSelectionModel lsm = dataTable.getSelectionModel();
            final Graph graph = graphNode.getGraph();
            final GraphTableModel gtm = (GraphTableModel) dataTable.getModel();
            final GraphElementType elementType = gtm.getElementType();

            int saId;
            ReadableGraph rg = graph.getReadableGraph();
            try {
                saId = rg.getAttribute(elementType, "selected");
            } finally {
                rg.release();
            }
            final int selectedAttrId = saId;

            // We can't run the plugin now, because we need to get off the EDT to write to the graph.
            // We can't run the plugin later, because we need to reset the isAdjusting flags.
            // Therefore, we'll run the plugin later and wait for it to finish.
            // This has the disadvantage that we'll be waiting in the EDT for the plugin to finish.
            // Not good, but what else can we do?
            final Future<?> future = PluginExecution.withPlugin(new SimpleEditPlugin("Table View: Update Selection") {
                @Override
                public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                    for (int rowIndex = e.getFirstIndex(); rowIndex <= e.getLastIndex(); rowIndex++) {
                        try {
                            final int modelIndex = dataTable.convertRowIndexToModel(rowIndex);

                            // In a happy coincidence, the row index in the table can be reused as the position in the graph.
                            final int elId = elementType == GraphElementType.VERTEX ? wg.getVertex(modelIndex) : wg.getTransaction(modelIndex);

                            wg.setBooleanValue(selectedAttrId, elId, lsm.isSelectedIndex(rowIndex));
                        } catch (IndexOutOfBoundsException ex) {
                            // ignore, given we are running in a future, the model may have changed and just there is nothing we can do
                        }
                    }
                }
            }).executeLater(graph);

            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            dataTable.getSelectionModel().setValueIsAdjusting(false);
            isAdjusting = false;
        }
    }

    /**
     * Listen to selection changes in the graph so we can reflect them into the
     * table.
     * <p>
     * The event may be null, since we call this manually from setNode() after a
     * graph change and the element actions.
     *
     * @param evt PropertyChangeEvent.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        if (graphNode == null || isAdjusting || dataTable.getSelectionModel().getValueIsAdjusting()) {
            return;
        }

        if (evt != null && evt.getDescription() == OWN_WRITE) {
            return;
        }

        final Graph graph = graphNode.getGraph();
        final ReadableGraph rg = graph.getReadableGraph();

        unsetTableListeners();
        final GraphElementType elementType = ((GraphTableModel) dataTable.getModel()).getElementType();
        isAdjusting = true;
        try {
            final long smc = rg.getStructureModificationCounter();
            final long amc = rg.getAttributeModificationCounter();

            if (smc != structureModificationCounter || amc != attributeModificationCounter) {
                // Something major has happened to the graph (a new graph, deleted nodes, etc) that makes us set a new model.
                // This means we need to preserve the table state and restore it.
                final TableState state = TableState.getMetaState(rg, elementType);
                selectedOnlyButton.setSelected(state != null ? state.getSelectedOnly() : false);
                setNewModel();
                setDefaultColumns(dataTable);
                TableState.applyMetaState(rg, state, elementType, dataTable);
                structureModificationCounter = smc;
                attributeModificationCounter = amc;
            }

            final int selectedAttr = rg.getAttribute(elementType, "selected");
            if (selectedAttr != Graph.NOT_FOUND) {
                final long selmc = rg.getValueModificationCounter(selectedAttr);

                if (selmc != selectedModificationCounter) {
                    setNewFilter();
                    setSelectionFromGraph(rg, elementType, selectedAttr);
                } else {
                    // Something minor has happened to the graph (a value change).
                    // Tell the model to tell the table.
                    ((GraphTableModel) dataTable.getModel()).graphDataChange();
                }
            }
        } finally {
            isAdjusting = false;
            rg.release();
        }

        setTableListeners();
    }

    /**
     * Update the selection model from the graph.
     * <p>
     * Note: it would probably be more efficient to bunch row ranges together to
     * make fewer calls to addSelectionInterval(), but that would mean more
     * complicated code.
     *
     * @param rg The graph read methods.
     * @param elementType The element type selection being updated.
     * @param selectedAttr The id of the "selected" attribute for the element
     * type.
     */
    private void setSelectionFromGraph(final GraphReadMethods rg, final GraphElementType elementType, final int selectedAttr) {
        // We don't want to select rows when we're only showing the selected rows. :-)
        if (!selectedOnlyButton.isSelected()) {
            final ListSelectionModel lsm = dataTable.getSelectionModel();
            lsm.removeListSelectionListener(this);
            lsm.setValueIsAdjusting(true);
            lsm.clearSelection();
            try {
                final int count = elementType == GraphElementType.VERTEX ? rg.getVertexCount() : rg.getTransactionCount();
                for (int position = 0; position < count; position++) {
                    final int id = elementType == GraphElementType.VERTEX ? rg.getVertex(position) : rg.getTransaction(position);

                    final boolean isSelected = rg.getBooleanValue(selectedAttr, id);
                    if (isSelected) {
                        // In a happy coincidence, the position in the graph can be reused as the row index in the table.
                        final int row = dataTable.convertRowIndexToView(position);
                        lsm.addSelectionInterval(row, row);
                    }
                }
            } finally {
                // Setting valueIsAdjusting kicks off a call to valueChanged(), which attempts to write to the graph.
                // This would cause a hang on the EDT, because we're still reading from it.
                // Therefore, do it later.
                SwingUtilities.invokeLater(() -> {
                    lsm.setValueIsAdjusting(false);
                    lsm.addListSelectionListener(this);
                });
            }
        }

        selectedModificationCounter = rg.getValueModificationCounter(selectedAttr);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("HEADER.HP_HEADERITEM".equals(evt.getPropertyName())) {
            // Attempt to do updates only when necessary (not when the mouse is just moving over the column headers).
            if (evt.getOldValue() != null && evt.getNewValue() != null) {
                final String oldv = evt.getOldValue().toString();
                final String newv = evt.getNewValue().toString();
                if (!((oldv.equals("HOT") && newv.equals("NORMAL")) || (oldv.equals("NORMAL") && newv.equals("HOT")))) {
                    final Graph graph = graphNode.getGraph();
                    updateStateOnGraph(graph, currentElementType, false);
                }
            }
        }
    }

    /**
     * Set the table listeners.
     */
    private void setTableListeners() {
        dataTable.getTableHeader().addPropertyChangeListener(this);
        dataTable.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Unset the table listeners.
     */
    private void unsetTableListeners() {
        dataTable.getTableHeader().removePropertyChangeListener(this);
        dataTable.getSelectionModel().removeListSelectionListener(this);
    }

    /**
     * Handle typing of specific keys.
     */
    private class DataTableKeyListener extends KeyAdapter {

        @Override
        public void keyTyped(final KeyEvent e) {
            if (graphNode != null) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    PluginExecution.withPlugin(PluginRegistry.get(InteractiveGraphPluginRegistry.DELETE_SELECTION)).interactively(true).executeLater(graphNode.getGraph());
                }
            }
        }
    }

    /**
     * Assign a new filter while keeping the sort keys.
     */
    private void setNewFilter() {
        final RowSorter<? extends TableModel> oldSorter = dataTable.getRowSorter();
        final GraphTableModel gtm = (GraphTableModel) dataTable.getModel();
        final TableRowSorter sorter = new TableRowSorter<>(gtm);
        sorter.setSortKeys(oldSorter.getSortKeys());
        if (selectedOnlyButton.isSelected()) {
            sorter.setRowFilter(new SelectionRowFilter(graphNode.getGraph(), currentElementType));
        }
        dataTable.setRowSorter(sorter);
    }

    /**
     * Assign a new table model and filter while keeping the sort keys.
     */
    private void setNewModel() {
        final RowSorter<? extends TableModel> oldSorter = dataTable.getRowSorter();
        final GraphTableModel gtm = new GraphTableModel(graphNode.getGraph(), currentElementType);
        final TableRowSorter sorter = new TableRowSorter<>(gtm);
        sorter.setSortKeys(oldSorter.getSortKeys());
        if (selectedOnlyButton.isSelected()) {
            sorter.setRowFilter(new SelectionRowFilter(graphNode.getGraph(), currentElementType));
        }
        dataTable.setModel(gtm);
        dataTable.setRowSorter(sorter);
    }

    /**
     * Sidebar selected only action.
     */
    private class SelectedOnlyAction extends AbstractAction {

        SelectedOnlyAction() {
            putValue(SMALL_ICON, SELECTED_ONLY);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            setNewModel();
            final ReadableGraph rg = graphNode.getGraph().getReadableGraph();
            try {
                final boolean isVertex = currentElementType == GraphElementType.VERTEX;
                final int selectedId = rg.getAttribute(currentElementType, (isVertex ? VisualConcept.VertexAttribute.SELECTED : VisualConcept.TransactionAttribute.SELECTED).getName());
                setSelectionFromGraph(rg, currentElementType, selectedId);
            } finally {
                rg.release();
            }
        }
    }

    /**
     * Sidebar vertex action.
     */
    private class VertexAction extends AbstractAction {

        VertexAction() {
            putValue(SMALL_ICON, VX_ICON);
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            currentElementType = GraphElementType.VERTEX;
            setNode(graphNode);
        }
    }

    /**
     * Sidebar transaction action.
     */
    private class TransactionAction extends AbstractAction {

        TransactionAction() {
            putValue(SMALL_ICON, UserInterfaceIconProvider.CONNECTIONS.buildIcon(16));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            currentElementType = GraphElementType.TRANSACTION;
            setNode(graphNode);
        }
    }

    /**
     * Sidebar column selection action.
     */
    private class ColumnsAction extends AbstractAction {

        ColumnsAction() {
            putValue(SMALL_ICON, UserInterfaceIconProvider.COLUMNS.buildIcon(16, ConstellationColor.AMETHYST.getJavaColor()));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (e.getActionCommand() == null) {
                unsetTableListeners();

                final GraphTableModel tableModel = (GraphTableModel) dataTable.getModel();
                final GraphElementType elementType = tableModel.getElementType();
                final Graph graph = graphNode.getGraph();
                final ReadableGraph rg = graph.getReadableGraph();
                try {
                    final ColumnsInTablePanel citPanel = new ColumnsInTablePanel(rg, dataTable, elementType);
                    final DialogDescriptor dd = new DialogDescriptor(citPanel, "Columns", true, this);
                    final Object result = DialogDisplayer.getDefault().notify(dd);
                    if (result == DialogDescriptor.OK_OPTION) {
                        final List<Integer> selectedAttrIds = citPanel.getSelectedAttributeIds(rg, elementType);
                        final Set<TableColumn> columnsToRemove = new HashSet<>();
                        final Set<Integer> attrIdsToRemove = new HashSet<>();
                        final TableColumnModel tcm = dataTable.getColumnModel();

                        // First we'll find out which visible columns should be removed.
                        for (int ix = 0; ix < tcm.getColumnCount(); ix++) {
                            final TableColumn tc = tcm.getColumn(ix);
                            final int modelIndex = tc.getModelIndex();
                            final Attribute attr = tableModel.getAttribute(modelIndex);
                            final boolean isSelected = selectedAttrIds.contains(attr.getId());

                            if (!isSelected) {
                                columnsToRemove.add(tc);
                            } else {
                                attrIdsToRemove.add(attr.getId());
                            }
                        }

                        columnsToRemove.stream().forEach(tc -> {
                            dataTable.removeColumn(tc);
                        });

                        selectedAttrIds.removeAll(attrIdsToRemove);

                        selectedAttrIds.stream().forEach(attrId -> {
                            if (currentElementType == GraphElementType.VERTEX) {
                                final int modelIndex = tableModel.getModelIndex(attrId, Segment.VX_SRC);
                                final TableColumn tc = new TableColumn(modelIndex);
                                dataTable.addColumn(tc);
                            } else {
                                final Attribute attr = getAttribute(rg, attrId);
                                if (attr.getElementType() == GraphElementType.TRANSACTION) {
                                    final int modelIndex = tableModel.getModelIndex(attrId, Segment.TX);
                                    final TableColumn tc = new TableColumn(modelIndex);
                                    dataTable.addColumn(tc);
                                    moveLastColumnToEndOfSegment(tableModel);
                                } else {
                                    for (final Segment segment : new Segment[]{
                                        Segment.VX_SRC, Segment.VX_DST
                                    }) {
                                        final int modelIndex = tableModel.getModelIndex(attrId, segment);
                                        final TableColumn tc = new TableColumn(modelIndex);
                                        dataTable.addColumn(tc);
                                        moveLastColumnToEndOfSegment(tableModel);
                                    }
                                }
                            }
                        });
                    }
                } finally {
                    rg.release();
                }

                setTableListeners();

                updateStateOnGraph(graph, elementType, false);
            }
        }
    }

    /**
     * Given an attribute id, return an attribute, including the table dummy
     * attributes.
     * <p>
     * The table model defines some dummy attributes that aren't in the graph:
     * allow for these.
     *
     * @param rg A readable graph.
     * @param attrId The attribute id.
     *
     * @return Either an actual graph attribute, or one of the table dummy
     * attributes.
     */
    private static Attribute getAttribute(final GraphReadMethods rg, final int attrId) {
        if (attrId >= 0) {
            return new GraphAttribute(rg, attrId);
        }

        if (attrId == GraphTableModel.VX_ID_IX) {
            return GraphTableModel.VX_ATTR;
        } else if (attrId == GraphTableModel.TX_ID_IX) {
            return GraphTableModel.TX_ATTR;
        } else if (attrId == GraphTableModel.TX_SRC_ID_IX) {
            return GraphTableModel.TX_SRC_ATTR;
        } else if (attrId == GraphTableModel.TX_DST_ID_IX) {
            return GraphTableModel.TX_DST_ATTR;
        }

        throw new IllegalArgumentException(String.format("Unknown attribute id: %d", attrId));
    }

    /**
     * Move the last column from the end of the table to just after the final TX
     * column.
     *
     * @param tableModel
     */
    private void moveLastColumnToEndOfSegment(final GraphTableModel tableModel) {
        final int cc = dataTable.getColumnCount();
        final Segment colSegment = tableModel.getSegment(dataTable.convertColumnIndexToModel(cc - 1));
        for (int ci = cc - 2; ci >= 0; ci--) {
            final int modelIndex = dataTable.convertColumnIndexToModel(ci);
            if (tableModel.getSegment(modelIndex) == colSegment) {
                dataTable.moveColumn(dataTable.getColumnCount() - 1, ci + 1);
                break;
            }
        }
    }

    /**
     * Make the graph in the specified node the source for the table model.
     * <p>
     * If another graph is attached to the model, it is detached first.
     *
     * @param node The GraphNode containing the graph to be displayed.
     */
    private void setNode(final GraphNode node) {
        unsetTableListeners();
        final GraphElementType elementType = ((GraphTableModel) dataTable.getModel()).getElementType();

        if (graphNode != null) {
//            // Record the state of the table so we can restore it when the user comes back to this graph.
            final Graph graph = graphNode.getGraph();
            graph.removeGraphChangeListener(this);
            updateStateOnGraph(graph, elementType, true);

            removeColumns(dataTable);
            dataTable.setModel(new GraphTableModel(null, elementType));
        }

        if (node != null) {
            graphNode = node;
            final Graph graph = graphNode.getGraph();
            final GraphTableModel gtm = new GraphTableModel(graph, currentElementType);
            dataTable.setModel(gtm);
            dataTable.setRowSorter(new TableRowSorter<>(gtm));

            final boolean selectedOnly;
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final TableState state = TableState.getMetaState(rg, currentElementType);
                selectedOnly = state != null ? state.getSelectedOnly() : false;
                TableState.applyMetaState(rg, state, currentElementType, dataTable);
            } finally {
                rg.release();
            }

            structureModificationCounter = -1;
            attributeModificationCounter = -1;
            selectedModificationCounter = -1;
            graph.addGraphChangeListener(this);

            selectedOnlyButton.setSelected(selectedOnly);
            columnsAction.setEnabled(true);
            selectedOnlyAction.setEnabled(true);
            vxAction.setEnabled(true);
            txAction.setEnabled(true);
        } else {
            graphNode = null;
            final GraphTableModel gtm = new GraphTableModel(null, elementType);
            dataTable.setModel(gtm);
            dataTable.setRowSorter(new TableRowSorter<>(gtm));

            columnsAction.setEnabled(false);
            selectedOnlyAction.setEnabled(false);
            vxAction.setEnabled(false);
            txAction.setEnabled(false);
        }

        setTableListeners();

        graphChanged(null);
    }

    private static void removeColumns(final JTable table) {
        final TableColumnModel tcm = table.getColumnModel();
        while (tcm.getColumnCount() > 0) {
            final TableColumn tc = tcm.getColumn(0);
            tcm.removeColumn(tc);
        }
    }

    public static boolean isImportant(final String label) {
        if (!label.isEmpty()) {
            final char c = label.charAt(0);
            return (c >= 'A' && c <= 'Z') || label.equals("selected");
        }

        return false;
    }

    private void setDefaultColumns(final JTable table) {
        final GraphTableModel model = (GraphTableModel) table.getModel();
        final TableColumnModel tcm = table.getColumnModel();

        // A blank graph with no attributes still has dummy attributes in the table model.
        // When new attributes are added, we only only want to add them to the table if the only
        // existing attributes are dummies.
        final boolean addDefaults
                = currentElementType == GraphElementType.VERTEX && tcm.getColumnCount() <= 1
                || currentElementType == GraphElementType.TRANSACTION && tcm.getColumnCount() <= 3;

        if (addDefaults) {
            for (int index = 0; index < model.getColumnCount(); index++) {
                final String name = model.getColumnName(index);
                if (isImportant(name)) {
                    final TableColumn tc = new TableColumn(index);
                    tc.setHeaderValue(name);
                    tcm.addColumn(tc);
                }
            }
        }
    }

    @Override
    public void preferenceChange(final PreferenceChangeEvent evt) {
        if (evt.getKey().equals(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE)) {
            setTableFont();
        }
    }

    /**
     * Update the Table View state metadata attribute on the graph so that it
     * can be used when one comes back to the graph
     *
     * @param graph The graph
     * @param elementType The graph element
     * @param lockTheEdt It is as nasty as it sounds, we are effectively locking
     * the EDT to ensure the table view columns are updated using the table view
     * state
     */
    private void updateStateOnGraph(final Graph graph, final GraphElementType elementType, final boolean lockTheEdt) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread thread = new Thread(() -> {
            try {
                final WritableGraph wg = graph.getWritableGraph("save table state", false);
                try {
                    TableState.putMetaState(wg, elementType, TableState.createMetaState(dataTable, selectedOnlyButton.isSelected()));
                } finally {
                    wg.commit(OWN_WRITE);
                    latch.countDown();
                }
            } catch (InterruptedException ex) {
            }
        });
        thread.setName(TABLE_VIEW_STATE_UPDATER_THREAD_NAME);
        thread.start();

        try {
            if (lockTheEdt) {
                latch.await(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ex) {
        }
    }

    private static class CustomTableHeaderRenderer implements TableCellRenderer {

        private final TableCellRenderer delegate;

        public CustomTableHeaderRenderer(final TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                final GraphTableModel model = (GraphTableModel) table.getModel();
                final int modelIndex = table.convertColumnIndexToModel(column);
                final Segment segment = model.getSegment(modelIndex);
                final JLabel label = (JLabel) c;
                label.setIcon(segment == Segment.TX ? UserInterfaceIconProvider.CONNECTIONS.buildIcon(16) : VX_ICON);
                label.setHorizontalTextPosition(SwingConstants.RIGHT);
            }

            return c;
        }
    }

    // Define some background colors to distinguish the different kinds of cells.
    static final Color TX_BG;
    static final Color VX_SRC_BG;
    static final Color VX_DST_BG;
    static final Color[] SEGMENT_COLORS;

    static {
        VX_SRC_BG = ConstellationColor.BLUEBERRY.getJavaColor();
        VX_DST_BG = ConstellationColor.MELON.getJavaColor();
        TX_BG = ConstellationColor.MANILLA.getJavaColor();
        SEGMENT_COLORS = new Color[]{TX_BG, VX_SRC_BG, VX_DST_BG};
    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                final GraphTableModel model = (GraphTableModel) table.getModel();
                final int modelIndex = table.convertColumnIndexToModel(column);
                final Segment segment = model.getSegment(modelIndex);
                c.setBackground(SEGMENT_COLORS[segment.ordinal()]);
            }

            return c;
        }
    }
}

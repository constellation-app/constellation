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
package au.gov.asd.tac.constellation.views.tableview.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.Segment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Store and retrieve the state of a table view.
 *
 * @author algol
 */
public final class TableState {

    private static final String TABLE_STATE = "table_state";

    final Point viewPosition;
    final ArrayList<String> sortOrder;
    final boolean selectedOnly;

    // Save the columns here.
    final ArrayList<ColumnState> columns;

    TableState(final JTable table, final boolean selectedOnly) {
        this(((JViewport) table.getParent()).getViewPosition(), selectedOnly);
    }

    TableState(final Point point, final boolean selectedOnly) {
        viewPosition = new Point(point.x, point.y);
        sortOrder = new ArrayList<>();
        columns = new ArrayList<>();
        this.selectedOnly = selectedOnly;
    }

    public boolean getSelectedOnly() {
        return selectedOnly;
    }

    public static TableState createMetaState(final JTable table, final boolean selectedOnly) {
        final TableState state = new TableState(table, selectedOnly);

        final GraphTableModel tm = (GraphTableModel) table.getModel();
        for (int i = 0; i < table.getColumnCount(); i++) {
            final TableColumn tc = table.getColumnModel().getColumn(i);
            final int modelIndex = tc.getModelIndex();
            final Attribute attr = tm.getAttribute(modelIndex);
            final String label = attr.getName();

            final ColumnState cs = new ColumnState(label, tm.getSegment(modelIndex), tc.getWidth());
            state.columns.add(cs);
        }

        final RowSorter<? extends TableModel> sorter = table.getRowSorter();
        for (final RowSorter.SortKey sk : sorter.getSortKeys()) {
            // TODO: should really store the column label + segment here.
            state.sortOrder.add(String.format("%d,%s", sk.getColumn(), sk.getSortOrder()));
        }

        return state;
    }

    public static void applyMetaState(final ReadableGraph rg, final TableState state, final GraphElementType elementType, final JTable table) {
        if (state == null) {
            ((JViewport) table.getParent()).setViewPosition(new Point(0, 0));
            return;
        }

        // Remove existing columns.
        final ArrayList<TableColumn> toRemove = new ArrayList<>();
        final TableColumnModel tcm = table.getColumnModel();
        for (final Enumeration<TableColumn> e = tcm.getColumns(); e.hasMoreElements();) {
            final TableColumn tc = e.nextElement();
            toRemove.add(tc);
        }

        for (final TableColumn tc : toRemove) {
            tcm.removeColumn(tc);
        }

        final GraphTableModel model = (GraphTableModel) table.getModel();
        for (int i = 0; i < state.columns.size(); i++) {
            final ColumnState cs = state.columns.get(i);
            final Attribute attr;
            if (cs.label == null) {
                // Null label means dummy attribute.
                // Obsolete and not true any more, but left here for old graphs.
                if (elementType == GraphElementType.VERTEX) {
                    attr = GraphTableModel.VX_ATTR;
                } else if (cs.segment == Segment.TX) {
                    attr = GraphTableModel.TX_ATTR;
                } else if (cs.segment == Segment.VX_SRC) {
                    attr = GraphTableModel.TX_SRC_ATTR;
                } else {
                    attr = GraphTableModel.TX_DST_ATTR;
                }
            } else if (cs.segment == Segment.VX_SRC && cs.label.equals(GraphTableModel.VX_ATTR.getName())) {
                attr = GraphTableModel.VX_ATTR;
            } else if (cs.segment == Segment.TX && cs.label.equals(GraphTableModel.TX_ATTR.getName())) {
                attr = GraphTableModel.TX_ATTR;
            } else if (cs.segment == Segment.TX && cs.label.equals(GraphTableModel.TX_SRC_ATTR.getName())) {
                attr = GraphTableModel.TX_SRC_ATTR;
            } else if (cs.segment == Segment.TX && cs.label.equals(GraphTableModel.TX_DST_ATTR.getName())) {
                attr = GraphTableModel.TX_DST_ATTR;
            } else {
                final GraphElementType colElementType = cs.segment == Segment.TX ? GraphElementType.TRANSACTION : GraphElementType.VERTEX;
                final int attrId = rg.getAttribute(colElementType, cs.label);
                if (attrId != Graph.NOT_FOUND) {
                    attr = new GraphAttribute(rg, attrId);
                } else {
                    // If we get here, the ColumnState value was bogus, probably from a hacked save file.
                    attr = null;
                }
            }

            if (attr != null) {
                final int modelIndex = model.getModelIndex(attr.getId(), cs.segment);
                final TableColumn tc = cs.width > 0 ? new TableColumn(modelIndex, cs.width) : new TableColumn(modelIndex);
                tc.setHeaderValue(attr.getName());
                tcm.addColumn(tc);
            }
        }

        ((JViewport) table.getParent()).setViewPosition(state.viewPosition);

        final ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
        final RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (!state.sortOrder.isEmpty()) {
            for (int i = 0; i < state.sortOrder.size(); i++) {
                final String[] so = state.sortOrder.get(i).split(",");
                final int sortColumn = Integer.parseInt(so[0]);
                final SortOrder sortOrder = SortOrder.valueOf(so[1]);
                final RowSorter.SortKey sortKey = new RowSorter.SortKey(sortColumn, sortOrder);
                sortKeys.add(sortKey);
            }
        }
        sorter.setSortKeys(sortKeys);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(String.format("[%s\n", TableState.class.getSimpleName()));
        b.append(String.format("  Columns: %d\n", columns.size()));
        for (final String so : sortOrder) {
            b.append(String.format("  Sort: %s\n", so));
        }
        b.append(String.format("Selected only: %s\n", selectedOnly));
        b.append("]");

        return b.toString();
    }

    /**
     * Save a state instance as a META attribute.
     * <p>
     * Each graph has a vertex table and a transaction table, so we have to
     * allow a state for each of them.
     *
     * @param wg the operation will be performed using this write lock.
     * @param elementType the element type.
     * @param state the state object.
     */
    public static void putMetaState(final WritableGraph wg, final GraphElementType elementType, final TableState state) {
        final String label = getAttrLabel(elementType);
        int tableStateAttr = wg.getAttribute(GraphElementType.META, label);
        if (tableStateAttr == Graph.NOT_FOUND) {
            tableStateAttr = wg.addAttribute(GraphElementType.META, getAttrLabel(elementType), label, label, null, null);
        }

        wg.setObjectValue(tableStateAttr, 0, state);
    }

    /**
     * Retrieve a state instance from its META attribute.
     *
     * @param rg the operation will be performed using this read lock.
     * @param elementType the element type.
     *
     * @return The loaded state, or a new state if there isn't one in the graph.
     */
    public static TableState getMetaState(final ReadableGraph rg, final GraphElementType elementType) {
        final int tableStateAttr = rg.getAttribute(GraphElementType.META, getAttrLabel(elementType));
        if (tableStateAttr != Graph.NOT_FOUND) {
            final Object o = rg.getObjectValue(tableStateAttr, 0);
            if (o != null && o instanceof TableState) {
                return (TableState) o;
            }
        }

        return null;
    }

    static String getAttrLabel(final GraphElementType elementType) {
        return String.format("%s_%s", TABLE_STATE, elementType.getShortLabel());
    }

    static class ColumnState {

        public final String label;
        public final Segment segment;
        public final int width;

        ColumnState(final String label, final Segment segment, final int width) {
            this.label = label;
            this.segment = segment;
            this.width = width;
        }

        @Override
        public String toString() {
            return String.format("[%s %s segment=%s width=%d]", ColumnState.class.getSimpleName(), label, segment, width);
        }
    }
}

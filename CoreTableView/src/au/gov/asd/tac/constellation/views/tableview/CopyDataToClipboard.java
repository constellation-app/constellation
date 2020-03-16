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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.csv.SmartCSVWriter;
import au.gov.asd.tac.constellation.views.tableview.GraphTableModel.AttributeSegment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.apache.commons.text.StringEscapeUtils;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;

/**
 * Copy data from the table view to the clipboard.
 *
 * @author altair
 */
public class CopyDataToClipboard implements ActionListener, Action {

    private final JTable table;
    private final String pluginName;

    private Point mousePosition;

    /**
     * constructor
     *
     * @param pluginName
     * @param table
     */
    CopyDataToClipboard(final String pluginName, final JTable table) {
        this.pluginName = pluginName;
        this.table = table;
    }

    void processAllRowsToCSV(final boolean includeHeader) throws IOException {
        final File out = getCsvFile();
        if (out != null) {
            final ArrayList<Integer> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                columns.add(i);
            }

            try (final SmartCSVWriter csv = new SmartCSVWriter(new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8.name()))) {
                toCSV(csv, allRows(), columns, includeHeader);
            }
        }
    }

    void processSelectedRowsToCSV(final boolean includeHeader) throws IOException {
        final File out = getCsvFile();
        if (out != null) {
            final ArrayList<Integer> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                columns.add(i);
            }

            try (final SmartCSVWriter csv = new SmartCSVWriter(new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8.name()))) {
                toCSV(csv, table.getSelectedRows(), columns, includeHeader);
            }
        }
    }

    private File getCsvFile() {
        File f = new FileChooserBuilder(CopyDataToClipboard.class.getName()).setTitle("CSV file").addFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV files";
            }
        }).showSaveDialog();
        if (f != null) {
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getAbsolutePath() + ".csv");
            }
        }

        return f;
    }

    private void toClipboard(final int[] rows, final List<Integer> columns, final boolean includeHeader, final char separator) throws IOException {
        final StringWriter out = new StringWriter();
        try (final SmartCSVWriter csv = new SmartCSVWriter(out, separator, false)) {
            toCSV(csv, rows, columns, includeHeader);
        }
        final Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(new StringSelection(out.toString()), null);
    }

    /**
     * Copy all rows and columns to the clipboard.
     *
     * @param includeHeader True to include column headers in the copy.
     */
    void processAllRows(final boolean includeHeader, final char sep) throws IOException {
        final ArrayList<Integer> columns = new ArrayList<>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            columns.add(i);
        }

        toClipboard(allRows(), columns, includeHeader, sep);
    }

    /**
     * Copy all rows and specified columns to the clipboard.
     *
     * @param attrsegs A List of AttributeSegment instances corresponding to the
     * columns to be copied.
     * @param includeHeader True to include column headers in the copy.
     */
    void processAllRows(final List<AttributeSegment> attrsegs, boolean includeHeader) throws IOException {
        final ArrayList<Integer> columns = getColumnIndices(attrsegs);
        toClipboard(allRows(), columns, includeHeader, ',');
    }

    /**
     * Copy the table to the clipboard in a format that Word is able to
     * understand.
     *
     *
     */
    void processCopyTable() throws IOException {
        final ArrayList<Integer> columns = new ArrayList<>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            columns.add(i);
        }

        //toClipboard(allRows(), columns, includeHeader, ',');
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=1 width=100%>");

        final TableModel dataModel = table.getModel();

        // Add column names.
        final GraphTableModel gtmodel = (GraphTableModel) dataModel;
        final GraphElementType elementType = gtmodel.getElementType();
        sb.append("<tr>");
        int columnModelIdx;
        for (final int column : columns) {
            columnModelIdx = table.convertColumnIndexToModel(column);
            String colName = dataModel.getColumnName(columnModelIdx);

            // If this is a transaction table, prefix the column name with the element type so we know which column is which.
            if (elementType == GraphElementType.TRANSACTION) {
                switch (gtmodel.getAttributeSegment(columnModelIdx).segment) {
                    case TX:
                        colName = GraphRecordStoreUtilities.TRANSACTION + colName;
                        break;
                    case VX_SRC:
                        colName = GraphRecordStoreUtilities.SOURCE + colName;
                        break;
                    case VX_DST:
                        colName = GraphRecordStoreUtilities.DESTINATION + colName;
                        break;
                }
            }

            sb.append("<th>");
            sb.append(colName);
            sb.append("</th>");
        }
        sb.append("</tr>");

        // Add data.
        for (int i = 0; i < dataModel.getRowCount(); i++) {
            sb.append("<tr>");
            for (final int column : columns) {
                columnModelIdx = table.convertColumnIndexToModel(column);
                final Object o = dataModel.getValueAt(i, columnModelIdx);
                final String s = o == null ? "" : o.toString();
                sb.append("<td>");
                sb.append(StringEscapeUtils.escapeHtml4(s));
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        final Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipBoard.setContents(new HtmlSelection(sb.toString()), null);
    }

    private static class HtmlSelection implements Transferable {

        private static final ArrayList<DataFlavor> HTML_FLAVORS = new ArrayList<>();

        static {
            try {
                HTML_FLAVORS.add(new DataFlavor("text/html;class=java.lang.String"));
                HTML_FLAVORS.add(new DataFlavor("text/html;class=java.io.Reader"));
                HTML_FLAVORS.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private final String html;

        public HtmlSelection(String html) {
            this.html = html;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {

            return (DataFlavor[]) HTML_FLAVORS.toArray(new DataFlavor[HTML_FLAVORS.size()]);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return HTML_FLAVORS.contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (String.class.equals(flavor.getRepresentationClass())) {
                return html;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * Copy selected rows and all columns to the clipboard.
     *
     * @param includeHeader True to include column headers in the copy.
     */
    void processSelectedRows(final boolean includeHeader, final char sep) throws IOException {
        final ArrayList<Integer> columns = new ArrayList<>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            columns.add(i);
        }
        toClipboard(table.getSelectedRows(), columns, includeHeader, sep);
    }

    /**
     * Copy selected rows and specified columns to the clipboard.
     *
     * @param attrsegs A List of AttributeSegment instances corresponding to the
     * columns to be copied.
     * @param includeHeader True to include column headers in the copy.
     */
    void processSelectedRows(final ArrayList<AttributeSegment> attrsegs, boolean includeHeader) throws IOException {
        final ArrayList<Integer> columns = getColumnIndices(attrsegs);
        toClipboard(table.getSelectedRows(), columns, includeHeader, ',');
    }

    /**
     * Copy the table cell under the mouse.
     *
     * @param pointerLocation The mouse pointer location.
     * @param includeHeader True to include column headers in the copy.
     */
    void processSelectedCell(final Point pointerLocation, boolean includeHeader) throws IOException {
        final int currentColumn = table.columnAtPoint(pointerLocation);
        final int currentRow = table.rowAtPoint(pointerLocation);

        final List<Integer> columnIdx = new ArrayList<>();
        columnIdx.add(currentColumn);
        final int[] rowIdx = new int[]{currentRow};
        toClipboard(rowIdx, columnIdx, includeHeader, ',');
    }

    /**
     * Copies the column the the mouse is on.
     *
     * @param pointerLocation The mouse pointer location.
     * @param includeHeader True to include the column header.
     */
    void processColumnCopy(final Point pointerLocation, final boolean includeHeader) throws IOException {
        final Integer currentColumn = table.columnAtPoint(pointerLocation);
        final List<Integer> columnIdx = new ArrayList<>();
        columnIdx.add(currentColumn);
        toClipboard(allRows(), columnIdx, includeHeader, ',');
    }

    /**
     * Copy data to CSV.
     *
     * @param csv CSVWriter.
     * @param rows Array of rows.
     * @param columns List of columns.
     * @param includeHeader True to include column headers in the copy.
     */
    private void toCSV(final SmartCSVWriter csv, final int[] rows, final List<Integer> columns, final boolean includeHeader) throws IOException {
        int auditCounter = 0;
        final TableModel dataModel = table.getModel();
        int columnModelIdx;
        final ArrayList<String> data = new ArrayList<>();

        // Add column names.
        if (includeHeader) {
            final GraphTableModel gtmodel = (GraphTableModel) dataModel;
            final GraphElementType elementType = gtmodel.getElementType();

            for (final int column : columns) {
                columnModelIdx = table.convertColumnIndexToModel(column);
                String colName = dataModel.getColumnName(columnModelIdx);

                // If this is a transaction table, prefix the column name with the element type so we know which column is which.
                if (elementType == GraphElementType.TRANSACTION) {
                    switch (gtmodel.getAttributeSegment(columnModelIdx).segment) {
                        case TX:
                            colName = GraphRecordStoreUtilities.TRANSACTION + colName;
                            break;
                        case VX_SRC:
                            colName = GraphRecordStoreUtilities.SOURCE + colName;
                            break;
                        case VX_DST:
                            colName = GraphRecordStoreUtilities.DESTINATION + colName;
                            break;
                    }
                }

                data.add(colName);
            }

            csv.writeNext(data.toArray(new String[data.size()]));
        }

        // Add data.
        for (final int row : rows) {
            data.clear();
            for (final Integer j : columns) {
                final int rowModelIndex = table.convertRowIndexToModel(row);
                columnModelIdx = table.convertColumnIndexToModel(j);
                final Object o = dataModel.getValueAt(rowModelIndex, columnModelIdx);
                final String s = o == null ? null : o.toString();
                data.add(s);
                auditCounter++;
            }

            csv.writeNext(data.toArray(new String[data.size()]));
        }

        final int count = auditCounter;

        PluginExecution.withPlugin(new SimplePlugin("Copy To Clipboard") {
            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                ConstellationLoggerHelper.copyPropertyBuilder(this, count, ConstellationLoggerHelper.SUCCESS);
            }
        }).executeLater(null);
    }

    /**
     * Gets the indices of columns based on the AttributeSegment for that
     * column.
     *
     * @param attrsegs A List of AttributeSegment instances corresponding to
     * columns.
     *
     * @return The indices that correspond to the columns with names provided.
     */
    private ArrayList<Integer> getColumnIndices(final List<AttributeSegment> attrsegs) {

        // Instead of nested loops to match attrsegs and columns,
        // we'll make a map of attrseg to column index,
        // then pick out the attrsegs we want.
        final HashMap<String, Integer> attrseg2index = new HashMap<>();
        final GraphTableModel gtModel = (GraphTableModel) table.getModel();
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            final TableColumn tc = table.getColumnModel().getColumn(i);
            final int modelIndex = tc.getModelIndex();
            final AttributeSegment attrseg = gtModel.getAttributeSegment(modelIndex);
            attrseg2index.put(attrseg.toString(), i);
        }

        final ArrayList<Integer> result = new ArrayList<>();
        attrsegs.stream().forEach((attrseg) -> {
            result.add(attrseg2index.get(attrseg.toString()));
        });

        return result;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            if (e.getActionCommand().startsWith(TableViewTopComponent.ALL_PREFIX)) {
                final char sep = e.getActionCommand().endsWith(Bundle.MSG_Commas()) ? ',' : '\t';
                this.processAllRows(true, sep);
            } else if (e.getActionCommand().startsWith(TableViewTopComponent.SEL_PREFIX)) {
                final char sep = e.getActionCommand().endsWith(Bundle.MSG_Commas()) ? ',' : '\t';
                this.processSelectedRows(true, sep);
            } else if (e.getActionCommand().equals(Bundle.MSG_CopyCell())) {
                this.processSelectedCell(mousePosition, false);
            } else if (e.getActionCommand().equals(Bundle.MSG_CopyColumn())) {
                this.processColumnCopy(mousePosition, false);
            } else if (e.getActionCommand().equals(Bundle.MSG_CopyTable())) {
                this.processCopyTable();
            } else if (e.getActionCommand().equals(Bundle.MSG_AllCSV())) {
                processAllRowsToCSV(true);
            } else if (e.getActionCommand().equals(Bundle.MSG_SelectedCSV())) {
                processSelectedRowsToCSV(true);
            } else { // called from CNTL-C
                this.processSelectedRows(false, ',');
            }
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * An array of length table.getRowCount() containing 0, 1, 2,...
     * representing all rows in the table.
     *
     * @return An array of length table.getRowCount() containing 0, 1, 2,...
     * representing all rows in the table.
     */
    private int[] allRows() {
        final int[] rowIndices = new int[table.getRowCount()];
        for (int i = 0; i < rowIndices.length; i++) {
            rowIndices[i] = i;
        }

        return rowIndices;
    }

    @Override
    public Object getValue(final String key) {
        return this;
    }

    @Override
    public void putValue(final String key, final Object value) {
    }

    @Override
    public void setEnabled(final boolean b) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
    }

    /**
     * @param MousePosition the MousePosition to set
     */
    public void setMousePosition(final Point MousePosition) {
        this.mousePosition = MousePosition;
    }
}

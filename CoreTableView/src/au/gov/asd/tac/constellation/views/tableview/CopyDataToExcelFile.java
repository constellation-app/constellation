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
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;

/**
 * Copy data from the table view to the clipboard.
 *
 * @author altair
 */
public class CopyDataToExcelFile implements ActionListener, Action {

    private final JTable table;
    private final String pluginName;

    private Point mousePosition;

    /**
     * constructor
     *
     * @param pluginName
     * @param table
     */
    CopyDataToExcelFile(final String pluginName, final JTable table) {
        this.pluginName = pluginName;
        this.table = table;
    }

    void processAllRowsToExcel(final boolean includeHeader) throws IOException {
        final File out = getExcelFile();
        if (out != null) {
            final ArrayList<Integer> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                columns.add(i);
            }

            try (final FileOutputStream fos = new FileOutputStream(out)) {
                toExcel(fos, allRows(), columns, includeHeader);
            }

        }
    }

    void processSelectedRowsToExcel(final boolean includeHeader) throws IOException {
        final File out = getExcelFile();
        if (out != null) {
            final ArrayList<Integer> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                columns.add(i);
            }

            toExcel(new FileOutputStream(out), table.getSelectedRows(), columns, includeHeader);

        }
    }

    private File getExcelFile() {
        File f = new FileChooserBuilder(CopyDataToExcelFile.class.getName()).setTitle("Excel file").addFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().toLowerCase().endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel files";
            }
        }).showSaveDialog();
        if (f != null) {
            if (!f.getName().toLowerCase().endsWith(".xlsx")) {
                f = new File(f.getAbsolutePath() + ".xlsx");
            }
        }

        return f;
    }

    /**
     * Copy data to Excel.
     *
     * @param fos The output steam that the file will be written to.
     * @param rows Array of rows.
     * @param columns List of columns.
     * @param includeHeader True to include column headers in the copy.
     */
    private void toExcel(final FileOutputStream fos, final int[] rows, final List<Integer> columns, final boolean includeHeader) throws IOException {
        int auditCounter = 0;
        final TableModel dataModel = table.getModel();

        // create the file in mem and set up one sheet.
        try (final SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            final Sheet sh = wb.createSheet(String.format("%s Table View Export", BrandingUtilities.APPLICATION_NAME));

            int columnModelIdx;
            int rowIndex = 0;

            // Add column names.
            if (includeHeader) {
                // Write header row.
                Row headerRow = sh.createRow(rowIndex);
                rowIndex++;

                final GraphTableModel gtmodel = (GraphTableModel) dataModel;
                final GraphElementType elementType = gtmodel.getElementType();
                int colIndex = 0;
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
                    Cell cell = headerRow.createCell(colIndex);
                    colIndex++;
                    cell.setCellValue(colName);
                }

            }

            // Add data.
            for (final int row : rows) {
                Row r = sh.createRow(rowIndex);
                rowIndex++;

                for (final Integer j : columns) {
                    final int rowModelIndex = table.convertRowIndexToModel(row);
                    columnModelIdx = table.convertColumnIndexToModel(j);
                    final Object o = dataModel.getValueAt(rowModelIndex, columnModelIdx);
                    final String s = o == null ? null : o.toString();
                    Cell cell = r.createCell(j);
                    cell.setCellValue(s);
                    auditCounter++;
                }
            }
            wb.write(fos);
            wb.dispose();
        }
        final int count = auditCounter;

        PluginExecution.withPlugin(new SimplePlugin("Export to Excel") {
            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                ConstellationLoggerHelper.copyPropertyBuilder(this, count, ConstellationLoggerHelper.SUCCESS);
            }
        }).executeLater(null);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            if (e.getActionCommand().equals(Bundle.MSG_AllExcel())) {
                processAllRowsToExcel(true);
            } else if (e.getActionCommand().equals(Bundle.MSG_SelectedExcel())) {
                processSelectedRowsToExcel(true);
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

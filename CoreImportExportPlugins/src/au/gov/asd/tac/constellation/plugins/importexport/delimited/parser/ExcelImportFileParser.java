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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.lookup.ServiceProvider;

/**
 * A CSVImportFileParser implements an ImportFileParser that can parse CSV
 * files.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = ImportFileParser.class)
public class ExcelImportFileParser extends ImportFileParser {

    private static final Logger LOGGER = Logger.getLogger(ExcelImportFileParser.class.getName());

    public ExcelImportFileParser() {
        super("Excel", 2);
    }

    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        final ArrayList<String[]> results = new ArrayList<>();
        if (input.getFile().getName().endsWith("xlsx")) {

            try (XSSFWorkbook wb = new XSSFWorkbook(input.getInputStream())) {
                XSSFSheet sheet = wb.getSheetAt(0);
                XSSFRow row;
                XSSFCell cell;
                int rows; // No of rows
                rows = sheet.getPhysicalNumberOfRows();

                int cols = 0; // No of columns
                int tmp = 0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                for (int i = 0; i < 10 || i < rows; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp > cols) {
                            cols = tmp;
                        }
                    }
                }

                for (int r = 0; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {
                        String[] line = new String[cols];
                        for (int c = 0; c < cols; c++) {
                            cell = row.getCell(c);
                            if (cell != null) {
                                line[c] = line[c] = getCellStringValue(cell);
                            }
                        }
                        results.add(line);
                    }
                }
            }

        } else if (input.getFile().getName().endsWith("xls")) {

            try (HSSFWorkbook wb = new HSSFWorkbook(input.getInputStream())) {
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row;
                HSSFCell cell;
                int rows; // No of rows
                rows = sheet.getPhysicalNumberOfRows();

                int cols = 0; // No of columns
                int tmp = 0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                for (int i = 0; i < 10 || i < rows; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp > cols) {
                            cols = tmp;
                        }
                    }
                }

                for (int r = 0; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {
                        String[] line = new String[cols];
                        for (int c = 0; c < cols; c++) {
                            cell = row.getCell(c);
                            if (cell != null) {
                                line[c] = line[c] = getCellStringValue(cell);
                            }
                        }
                        results.add(line);
                    }
                }
            }
        } else {
            // Do nothing
        }
        return results;
    }

    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        // Leave the header on, as the importer expects this as the first entry.
        final ArrayList<String[]> results = new ArrayList<>();
        int count = 0;
        if (input.getFile().getName().endsWith("xlsx")) {

            try (XSSFWorkbook wb = new XSSFWorkbook(input.getInputStream())) {
                XSSFSheet sheet = wb.getSheetAt(0);
                XSSFRow row;
                XSSFCell cell;
                int rows; // No of rows
                rows = sheet.getPhysicalNumberOfRows();

                int cols = 0; // No of columns
                int tmp = 0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                for (int i = 0; i < 10 || i < rows; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp > cols) {
                            cols = tmp;
                        }
                    }
                }

                for (int r = 0; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {
                        count++;
                        if (count >= limit) {
                            break;
                        }
                        String[] line = new String[cols];
                        for (int c = 0; c < cols; c++) {
                            cell = row.getCell(c);
                            if (cell != null) {
                                line[c] = getCellStringValue(cell);
                            }
                        }
                        results.add(line);
                    }
                }
            }

        } else if (input.getFile().getName().endsWith("xls")) {

            try (HSSFWorkbook wb = new HSSFWorkbook(input.getInputStream())) {
                HSSFSheet sheet = wb.getSheetAt(0);
                HSSFRow row;
                HSSFCell cell;
                int rows; // No of rows
                rows = sheet.getPhysicalNumberOfRows();

                int cols = 0; // No of columns
                int tmp = 0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                for (int i = 0; i < 10 || i < rows; i++) {
                    row = sheet.getRow(i);
                    if (row != null) {
                        tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp > cols) {
                            cols = tmp;
                        }
                    }
                }

                for (int r = 0; r < rows; r++) {
                    row = sheet.getRow(r);
                    if (row != null) {
                        count++;
                        if (count >= limit) {
                            break;
                        }
                        String[] line = new String[cols];
                        for (int c = 0; c < cols; c++) {
                            cell = row.getCell(c);
                            if (cell != null) {
                                line[c] = getCellStringValue(cell);
                            }
                        }
                        results.add(line);
                    }
                }
            }
        } else {
            // Do nothing
        }
        return results;

    }

    private String getCellStringValue(final Cell cell) {
        String result;
        final CellType type = cell.getCellType();

        try {
            switch (type) {
                case STRING:
                    result = cell.getStringCellValue();
                    break;
                case NUMERIC:
                case FORMULA:
                    final Double temp = cell.getNumericCellValue();
                    result = temp % 1 == 0 ? Long.toString(temp.longValue()) : Double.toString(temp);
                    break;
                case BLANK:
                    result = "";
                    break;
                case BOOLEAN:
                    result = Boolean.toString(cell.getBooleanCellValue());
                    break;
                default:
                    result = "";
                    break;
            }
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage() + " with value " + cell.getStringCellValue(), ex);
            result = "";
        }
        return result;
    }

    /**
     * Returns the file filter to use when browsing for files of this type.
     *
     * @return the file filter to use when browsing for files of this type.
     */
    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(final File file) {
                final String name = file.getName();
                return (file.isFile() && (StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.XLS)
                        || StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.XLSX)))
                        || file.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Excel Files ("
                        + FileExtensionConstants.XLS + ", "
                        + FileExtensionConstants.XLSX + ")";
            }
        };
    }
}

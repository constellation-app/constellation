/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.hashmod;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A text hashmod based on a supplied CSV file. Will modify attributes specified
 * in the headers to be values based on the first Key column.
 *
 * @author CrucisGamma
 */
public class Hashmod {

    private static final Logger LOGGER = Logger.getLogger(Hashmod.class.getName());

    public static final String ATTRIBUTE_NAME = "hashmod";
    private HashmodCSVImportFileParser parser;
    private String csvFileStr;
    private List<String[]> data;
    
    private static final Pattern HEADER_MATCH_REGEX = Pattern.compile(".*\\.\\.\\..*");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("^.*;;;([^\"]+)");

    public Hashmod() {
        parser = null;
        csvFileStr = StringUtils.EMPTY;
    }

    /**
     * Create a new Hashmod.
     *
     * @param csvFile Name of the CSV file the user has chosen
     */
    public Hashmod(final String csvFileStr) {
        setCSVFileStr(csvFileStr);
    }

    /**
     * 
     * Set the CSVFileStr and update data attribute.
     * @param csvFileStr 
     */
    public void setCSVFileStr(final String csvFileStr) {
        this.csvFileStr = StringUtils.defaultString(csvFileStr);
        parser = new HashmodCSVImportFileParser();
        try {
            data = parser.parse(new HashmodInputSource(new File(this.csvFileStr)));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public String[] getCSVFileHeaders() {
        if (CollectionUtils.isNotEmpty(data)) {
            return data.get(0);
        }
        return new String[0];
    }

    /**
     * Gets the @link{ String[] } object at position row. 
     * Returns an empty @link{ String[] } when nothing exists at that index.
     * 
     * @param row the row index to get
     * @return the String[] for that row index.
     */
    public String[] getCSVRow(final int row) {
        if (CollectionUtils.isNotEmpty(data) && data.size() > row) {
            return data.get(row);
        }
        return new String[0];
    }

    public String getCSVKey() {
        final String[] headers = getCSVFileHeaders();
        if (ArrayUtils.isNotEmpty(headers)) {
            return headers[0];
        }
        return StringUtils.EMPTY;
    }

    public Map<String, Integer> getCSVKeys() {
        final Map<String, Integer> keys = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                final String[] row = getCSVRow(i);
                if (row[0] != null) {
                    keys.put(row[0].toUpperCase(), i);
                }
            }
        }
        return keys;
    }

    public int getNumberCSVDataColumns() {
        final String[] headers = getCSVFileHeaders();
        for (int i = 0; i < headers.length; i++) {
            if (HEADER_MATCH_REGEX.matcher(headers[i]).matches()) {
                return i;
            }
        }
        return headers.length;
    }

    public int getNumberCSVTransactionColumns() {
        final String[] headers = getCSVFileHeaders();
        int numTransactions = 0;
        for (int i = getNumberCSVDataColumns(); i < headers.length; i++) {
            if (!HEADER_MATCH_REGEX.matcher(headers[i]).matches()) {
                return numTransactions;
            }
            numTransactions++;
        }
        return numTransactions;
    }

    private String getColumnOfTransaction(final int transactionCol, final String regex) {
        final Pattern transactionPattern = Pattern.compile(regex);

        final String[] headers = getCSVFileHeaders();
        int numTransactions = 0;
        for (int i = getNumberCSVDataColumns(); i < headers.length; i++) {
            if (HEADER_MATCH_REGEX.matcher(headers[i]).matches() && numTransactions == transactionCol) {
                final Matcher matchPattern = transactionPattern.matcher(headers[i]);
                if (matchPattern.find()) {
                    return matchPattern.group(1);
                }
            }
            numTransactions++;
        }
        return StringUtils.EMPTY;
    }

    public String getFirstColumnOfTransaction(final int transactionCol) {
        return getColumnOfTransaction(transactionCol, "([^\"]+)\\.\\.\\.");
    }

    public String getSecondColumnOfTransaction(final int transactionCol) {
        return getColumnOfTransaction(transactionCol, ".*?\\.\\.\\.([^\"]+?)(;;;.*|$)");
    }

    public String getTransactionAttribute(final String attributeFromCSV) {
        final Matcher matchPattern = TRANSACTION_PATTERN.matcher(attributeFromCSV);
        if (matchPattern.find()) {
            return matchPattern.group(1);
        }
        return StringUtils.EMPTY;
    }

    public String getCSVHeader(final int col) {
        final String[] headers = getCSVFileHeaders();
        if (headers != null && headers.length > col) {
            return headers[col];
        }
        return null;
//        return StringUtils.EMPTY;
    }

    public List<String[]> getCSVFileData() {
        if (CollectionUtils.isNotEmpty(data)) {
            return data;
        }
        return Collections.emptyList();
    }

    public String getValueFromKey(final String key, final int value) {
        if (key != null && CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                final String[] row = getCSVRow(i);
                if (key.equalsIgnoreCase(row[0]) && row.length > value) {
                    return row[value];
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get a value based from a key and index.
     * Will look through the rest of the data for that key if one does not exist.
     * 
     * @param key
     * @param value
     * @param index
     * @return the String of the data, or "" when non-existent.
     */
    public String getValueFromKeyAndIndex(final String key, final int value, final int index) {
        if (key != null && CollectionUtils.isNotEmpty(data)) {
            final String[] row = getCSVRow(index);
            if (row[0].equalsIgnoreCase(key) && row.length > value) {
                return row[value];
            }
        }
        return getValueFromKey(key, value);
    }

    /**
     * Checks the data of the file if it contains the @link{String} key.
     * @param key The @link{String} to check for
     * 
     * @return True if found
     */
    public boolean hasKey(final String key) {
        if (key != null && CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                if (key.equalsIgnoreCase(getCSVRow(i)[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the csv file name or empty string if one does not exist.
     * 
     * @return "" empty string or the file name
     */
    public String getCSVFileName() {
        return StringUtils.defaultString(this.csvFileStr);
    }
}

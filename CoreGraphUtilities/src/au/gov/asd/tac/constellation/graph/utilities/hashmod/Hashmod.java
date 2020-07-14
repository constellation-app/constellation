/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;

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

    public Hashmod() {
        parser = null;
        csvFileStr = "";
    }

    /**
     * Create a new Hashmod.
     *
     * @param csvFile Name of the CSV file the user has chosen
     */
    public Hashmod(final String csvFileStr) {
        if (csvFileStr == null) {
            this.csvFileStr = "";
        } else {
            this.csvFileStr = csvFileStr;
        }

        parser = new HashmodCSVImportFileParser();
        try {
            data = parser.parse(new HashmodInputSource(new File(csvFileStr)), null);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public void setCSVFileStr(final String csvFileStr) {
        this.csvFileStr = csvFileStr;
        parser = new HashmodCSVImportFileParser();
        try {
            data = parser.parse(new HashmodInputSource(new File(csvFileStr)), null);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public String[] getCSVFileHeaders() {
        if (CollectionUtils.isNotEmpty(data)) {
            return data.get(0);
        }
        return null;
    }

    public String[] getCSVRow(final int row) {
        if (data != null && data.size() > row) {
            return data.get(row);
        }
        return null;
    }

    public String getCSVKey() {
        final String[] headers = getCSVFileHeaders();
        if (headers != null && headers.length > 0) {
            if (headers.length > 0) {
                return headers[0];
            }
        }
        return null;
    }

    public HashMap<String, Integer> getCSVKeys() {
        final HashMap<String, Integer> keys = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                final String[] row = getCSVRow(i);
                if (row[0] != null) {
                    keys.put(row[0].toUpperCase(), 0);
                }
            }
        }
        return keys;
    }

    public int getNumberCSVColumns() {
        final String[] headers = getCSVFileHeaders();
        if (headers != null) {
            return headers.length;
        }
        return 0;
    }

    public String getCSVHeader(final int col) {
        final String[] headers = getCSVFileHeaders();
        if (headers != null && headers.length > col) {
            return headers[col];
        }
        return null;
    }

    public List<String[]> getCSVFileData() {
        if (CollectionUtils.isNotEmpty(data)) {
            return data;
        }
        return null;
    }

    public String getValueFromKey(final String key, final int value) {
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                final String[] row = getCSVRow(i);
                if (row[0].equalsIgnoreCase(key)) {

                    if (row.length > value) {
                        return row[value];
                    }
                }
            }
        }
        return null;
    }

    public Boolean doesKeyExist(final String key) {
        if (key == null) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 1; i < data.size(); i++) {
                final String[] row = getCSVRow(i);
                if (row[0].equalsIgnoreCase(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getCSVFileName() {
        return this.csvFileStr == null ? "" : this.csvFileStr;
    }
}

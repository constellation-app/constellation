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
import java.util.List;
import static javatests.TestSupport.fail;

/**
 * A text hashmod to be displayed at the top and bottom of the main window.
 *
 * @author CrucisGamma
 */
public class Hashmod {

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
            fail("IO Exception : " + ex.getLocalizedMessage());
        }
    }

    public void setCSVFileStr(final String csvFileStr) {
        this.csvFileStr = csvFileStr;
        parser = new HashmodCSVImportFileParser();
        try {
            data = parser.parse(new HashmodInputSource(new File(csvFileStr)), null);
        } catch (IOException ex) {
            fail("IO Exception : " + ex.getLocalizedMessage());
        }
    }

    public String[] getCSVFileHeaders() {
        if (data != null && data.size() > 0) {
            return data.get(0);
        }
        return null;
    }

    public String[] getCSVRow(int row) {
        if (data != null && data.size() > row) {
            return data.get(row);
        }
        return null;
    }

    public String getCSVKey() {
        String[] headers = getCSVFileHeaders();
        if (headers != null && headers.length > 0) {
            if (headers.length > 0) {
                return headers[0];
            }
        }
        return null;
    }

    public String getCSVHeader(int col) {
        String[] headers = getCSVFileHeaders();
        if (headers != null && headers.length >= col) {
            return headers[col];
        }
        return null;
    }

    public List<String[]> getCSVFileData() {
        if (data != null && data.size() > 0) {
            return data;
        }
        return null;
    }

    public String getValueFromKey(String key, int value) {
        if (data != null && data.size() > 0) {
            int i;
            for (i = 1; i < data.size(); i++) {
                String[] row = getCSVRow(i);
                if (row[0].equalsIgnoreCase(key)) {

                    if (row.length > value) {
                        return row[value];
                    }
                }
            }
        }
        return null;
    }

    public Boolean doesKeyExist(String key) {
        if (key == null) {
            return false;
        }
        if (data != null && data.size() > 0) {
            int i;
            for (i = 1; i < data.size(); i++) {
                String[] row = getCSVRow(i);
                if (row[0].equalsIgnoreCase(key)) {
                    return true;
                }
            }
        }
        return false;
    }
}

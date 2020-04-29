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
package au.gov.asd.tac.constellation.testing.jdbc;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Container for holding JDBC import/export data.
 *
 * @author algol
 */
class JdbcData {

    // JDBC connection parameters.
    String username;
    transient char[] password;
    String url;
    String jar;
    String driverName;

    // Vertex + transaction table names.
    String vxTable;
    String txTable;

    // Vertex + transaction mappings: column 0 = table column labels, column 1 = graph attribute labels.
    String[][] vxMappings;
    String[][] txMappings;

    // JDBC tables, passing from the connections controller to the tables controller.
    transient ArrayList<String> tables;

    // The vertex and transaction table columns, passing from the tables controller to the mapping controller.
    transient ArrayList<String> vxColumns;
    transient ArrayList<String> txColumns;

    JdbcData() {
        username = "";
        password = new char[0];
        url = "";
        jar = "";
        driverName = "";
        vxMappings = null;
        txMappings = null;

        vxColumns = null;
        txColumns = null;
    }

    void copyTo(final JdbcData data) {
        data.username = username;
        data.password = Arrays.copyOf(password, password.length);
        data.url = url;
        data.jar = jar;
        data.driverName = driverName;
        data.vxTable = vxTable;
        data.txTable = txTable;
        data.vxMappings = vxMappings != null ? copy(vxMappings) : null;
        data.txMappings = txMappings != null ? copy(txMappings) : null;
    }

    static String[][] copy(final String[][] src) {
        final int len = src[0].length;
        final String[][] dst = new String[2][len];
        for (int i = 0; i < 2; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, len);
        }

        return dst;
    }
}

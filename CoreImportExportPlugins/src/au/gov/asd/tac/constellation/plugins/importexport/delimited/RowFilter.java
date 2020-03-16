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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * The definition of a row filter.
 * <p>
 * A RowFilter contains a script and an array of column names. The script is run
 * against each row (as a String[]) to determine whether the row passes the
 * filter.
 * <p>
 * Rows that fail the row filter are typically excluded from being imported into
 * a graph.
 *
 * @author sirius
 */
public class RowFilter {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final ScriptEngineManager manager;
    private final ScriptEngine engine;
    private final Bindings bindings;

    private CompiledScript compiledScript;
    private String script;

    private String[] columns = new String[0];
    private String[] encodedColumns = new String[0];

    private static final Logger LOGGER = Logger.getLogger(RowFilter.class.getName());

    public RowFilter() {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");
        bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        script = null;
    }

    /**
     * The script that this filter implements.
     *
     * @return The script that this filter implements.
     */
    public String getScript() {
        return script;
    }

    /**
     * The script that this filter implements.
     *
     * @param script the script that will be applied to each row to test if it
     * should be included in the import.
     *
     * @return true if the script was successfully compiled.
     */
    public boolean setScript(final String script) {
        try {
            this.script = encodeScript(script);
            compiledScript = ((Compilable) engine).compile(this.script);

            LOGGER.log(Level.INFO, "SCRIPT = {0}", this.script);
            return true;

        } catch (ScriptException ex) {
            this.script = null;
            return false;
        }
    }

    private static String encodeScript(String script) {

        StringBuilder out = new StringBuilder();
        StringBuilder encodedPart = null;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);

            if (c == '\'') {
                if (encodedPart == null) {
                    encodedPart = new StringBuilder();
                } else {
                    out.append(encodeColumn(encodedPart.toString()));
                    encodedPart = null;
                }
            } else if (encodedPart != null) {
                encodedPart.append(c);
            } else {
                out.append(c);
            }
        }

        if (encodedPart != null) {
            out.append('\'').append(encodedPart);
        }

        return out.toString();
    }

    /**
     * The columns included in the filter.
     *
     * @return The columns included in the filter.
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * The names of the columns included in the filter.
     * <p>
     * Columns that match the pattern "^[a-zA-Z][a-zA-Z0-9_]*$" keep their
     * names, otherwise the name of the ith column is value<i>i</i>.
     *
     * @param columns the columns included in the filter.
     */
    public void setColumns(final String[] columns) {
        this.columns = new String[columns.length];
        this.encodedColumns = new String[columns.length];
        for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
            if (columns[columnIndex] != null && columns[columnIndex].matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                this.columns[columnIndex] = columns[columnIndex];
            }
            this.encodedColumns[columnIndex] = encodeColumn(columns[columnIndex]);
        }

        LOGGER.log(Level.INFO, "COLUMNS = {0} {1}", new Object[]{Arrays.toString(this.columns), Arrays.toString(this.encodedColumns)});
    }

    private static String encodeColumn(String column) {
        if (column == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder("$");
        for (byte b : column.getBytes(UTF8)) {
            int i = b;
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                builder.append('0');
            }
            builder.append(Integer.toHexString(i));
        }
        return builder.toString();
    }

    /**
     * Do the row values pass the filter?
     * <p>
     * The row number is bound to "Row", and the values are bound to their
     * respective column names. The script is then evaluated and the return
     * value of the script is returned.
     *
     * @param row The row number.
     * @param values The row values.
     *
     * @return True if the row passes the filter, false otherwise.
     */
    public boolean passesFilter(int row, String[] values) {
        try {
            bindings.clear();
            bindings.put("Row", row);

            final int fieldCount = Math.min(columns.length, values.length + 1);

            for (int i = 1; i < fieldCount; i++) {
                if (columns[i] != null) {
                    bindings.put(columns[i], values[i - 1]);
                }

                final String columnBinding = "$column" + i;
                if (!bindings.containsKey(columnBinding)) {
                    bindings.put(columnBinding, values[i - 1]);
                }
            }

            final Object result = compiledScript.eval();
            return result instanceof Boolean && ((Boolean) result);

        } catch (ScriptException ex) {
            return false;
        }
    }
}

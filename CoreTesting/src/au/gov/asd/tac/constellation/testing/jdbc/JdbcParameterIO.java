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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

/**
 *
 * @author algol
 */
class JdbcParameterIO {

    private static final String SAVE_DIR = "ImportExportJDBC";

    private static final String USERNAME = "username";
    private static final String CONNECTION_URL = "connection_url";
    private static final String JAR_FILE = "jar_file";
    private static final String DRIVER = "driver";
    private static final String VX_TABLE = "vx_table";
    private static final String TX_TABLE = "tx_table";
    private static final String MAPPINGS = "mappings";
    private static final String COLUMN = "column";
    private static final String ATTRIBUTE = "attribute";
    private static final String VERTICES = "vx";
    private static final String TRANSACTIONS = "tx";

    static void saveParameters(final JdbcData data, final String name) {
        final File saveDir = new File(String.format("%s/.CONSTELLATION/%s", System.getProperty("user.home"), SAVE_DIR));
        if (!saveDir.isDirectory()) {
            saveDir.mkdir();
        }

        final File saveFile = new File(saveDir, encode(name + ".json"));
        boolean go = true;
        if (saveFile.exists()) {
            final String msg = String.format("'%s' already exists. Do you want to overwrite it?", name);
            final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Save file exists", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
            go = DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
        }

        if (go) {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode rootNode = mapper.createObjectNode();

            rootNode.put(USERNAME, data.username);
            rootNode.put(CONNECTION_URL, data.url);
            rootNode.put(JAR_FILE, data.jar);
            rootNode.put(DRIVER, data.driverName);
            rootNode.put(VX_TABLE, data.vxTable);
            rootNode.put(TX_TABLE, data.txTable);

            final ObjectNode mappingObject = rootNode.putObject(MAPPINGS);

            final ArrayNode vxMappings = mappingObject.putArray(VERTICES);
            final int vxlen = data.vxMappings[0].length;
            for (int i = 0; i < vxlen; i++) {
                final ArrayNode mapping = vxMappings.addArray();
                mapping.add(data.vxMappings[0][i]);
                mapping.add(data.vxMappings[1][i]);
            }

            final ArrayNode txMappings = mappingObject.putArray(TRANSACTIONS);
            final int txlen = data.txMappings[0].length;
            for (int i = 0; i < txlen; i++) {
                final ArrayNode mapping = txMappings.addArray();
                mapping.add(data.txMappings[0][i]);
                mapping.add(data.txMappings[1][i]);
            }

            try {
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
                mapper.writeValue(saveFile, rootNode);
                StatusDisplayer.getDefault().setStatusText(String.format("Query saved to %s.", saveFile.getPath()));
            } catch (IOException ex) {
                final NotifyDescriptor nderr = new NotifyDescriptor.Message(String.format("Can't save %s:%n%s", saveFile.getPath(), ex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nderr);
            }
        }
    }

    static void loadParameters(final JdbcData data) {
        final File saveDir = new File(String.format("%s/.CONSTELLATION/%s", System.getProperty("user.home"), SAVE_DIR));
        final String[] names;
        if (saveDir.isDirectory()) {
            names = saveDir.list((File dir, String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        }

        // Chop off ".json".
        for (int i = 0; i < names.length; i++) {
            names[i] = decode(names[i].substring(0, names[i].length() - 5));
        }

        final String[] paramName = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            paramName[0] = QueryListDialog.getQueryName(null, names);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
        }

//        final JdbcParameterIoLabelsPanel panel = new JdbcParameterIoLabelsPanel(names);
//        final DialogDescriptor dd = new DialogDescriptor(panel, "Saved JDBC parameters");
//        final Object result = DialogDisplayer.getDefault().notify(dd);
//        if (result == DialogDescriptor.OK_OPTION)
        if (true) {
            final String queryName = paramName[0]; // panel.getLabel();
            if (queryName != null) {
                try {
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode root = mapper.readTree(new File(saveDir, encode(queryName) + ".json"));
                    data.username = root.get(USERNAME).textValue();
                    data.url = root.get(CONNECTION_URL).textValue();
                    data.jar = root.get(JAR_FILE).textValue();
                    data.vxTable = root.get(VX_TABLE).textValue();
                    data.txTable = root.get(TX_TABLE).textValue();

                    final JsonNode mappingsNode = root.get(MAPPINGS);
                    if (mappingsNode != null) {
                        if (mappingsNode.has(VERTICES)) {
                            final ArrayNode array = (ArrayNode) mappingsNode.get(VERTICES);
                            final String[][] mappings = new String[2][array.size()];
                            int i = 0;
                            for (final JsonNode element : array) {
                                final ArrayNode map = (ArrayNode) element;
                                final String colLabel = map.get(0).textValue();
                                final String attrLabel = map.get(1).textValue();
                                mappings[0][i] = colLabel;
                                mappings[1][i] = attrLabel;
                                i++;
                            }

                            data.vxMappings = mappings;
                        }

                        if (mappingsNode.has(TRANSACTIONS)) {
                            final ArrayNode array = (ArrayNode) mappingsNode.get(TRANSACTIONS);
                            final String[][] mappings = new String[2][array.size()];
                            int i = 0;
                            for (final JsonNode element : array) {
                                final ArrayNode map = (ArrayNode) element;
                                final String colLabel = map.get(0).textValue();
                                final String attrLabel = map.get(1).textValue();
                                mappings[0][i] = colLabel;
                                mappings[1][i] = attrLabel;
                                i++;
                            }

                            data.txMappings = mappings;
                        }
                    }
                } catch (final IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Encode a String so it can be used as a filename.
     *
     * @param s The String to be encoded.
     *
     * @return The encoded String.
     */
    static String encode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (final char c : s.toCharArray()) {
            if (isValidFileCharacter(c)) {
                b.append(c);
            } else {
                b.append(String.format("_%04x", (int) c));
            }
        }

        return b.toString();
    }

    /**
     * Decode a String that has been encoded by {@link encode(String)}.
     *
     * @param s The String to be decoded.
     *
     * @return The decoded String.
     */
    static String decode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '_') {
                b.append(c);
            } else {
                final String hex = s.substring(i + 1, Math.min(i + 5, s.length()));
                if (hex.length() == 4) {
                    try {
                        final int value = Integer.parseInt(hex, 16);
                        b.append((char) value);
                        i += 4;
                    } catch (final NumberFormatException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return b.toString();
    }

    static boolean isValidFileCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == '-' || c == '.';
    }
}

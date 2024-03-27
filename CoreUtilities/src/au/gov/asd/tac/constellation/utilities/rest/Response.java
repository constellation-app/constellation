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
package au.gov.asd.tac.constellation.utilities.rest;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST response.
 *
 * @author algol
 * @author arcturus
 */
public abstract class Response {

    /**
     * HTTP response code.
     */
    public final int code;

    /**
     * HTTP response message.
     */
    public final String message;

    /**
     * Header fields.
     */
    public final Map<String, List<String>> headers;

    /**
     * Body.
     */
    public final byte[] bytes;

    /**
     * Body as JSON.
     */
    public final JsonNode json;

    private static final String DASH_STRING = "----\n";

    private static final Logger LOGGER = Logger.getLogger(Response.class.getName());

    protected Response(final int code, final String message, final Map<String, List<String>> headers, final byte[] bytes) throws IOException {
        this(code, message, headers, bytes, true);
    }

    protected Response(final int code, final String message, final Map<String, List<String>> headers, final byte[] bytes, final boolean isJson) throws IOException {
        this.code = code;
        this.message = message;
        this.headers = headers;
        this.bytes = bytes;

        if (isJson) {
            json = isSuccess() ? getJson(bytes) : null;
        } else {
            json = null;
        }
    }

    /**
     * Does the HTTP response status code indicate success?
     *
     * @return True if the HTTP response status code begins with 2 or 4, false
     * otherwise.
     */
    public boolean isSuccess() {
        return isCodeSuccess(this.code);
    }

    /**
     * Does the HTTP response status code indicate success?
     *
     * @param code The HTTP response status code.
     *
     * @return True if the HTTP response status code begins with 2 or 4, false
     * otherwise.
     */
    public static boolean isCodeSuccess(final int code) {
        final int range = code / 100;
        return range == 2 || range == 4;
    }

    /**
     * Does the HTTP response status code indicate success and was json content
     * returned?
     *
     * @return True if successful, false otherwise.
     */
    public boolean isSuccessWithJson() {
        return isSuccess() && json != null;
    }

    private JsonNode getJson(final byte[] buf) throws IOException {
        if (buf == null || buf.length <= 0) {
            return null;
        }

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode root = getRootNode(mapper, buf);

        if (getSaveResponseFilename() != null) {
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);

            final File tmp = File.createTempFile(getSaveResponseFilename(), FileExtensionConstants.JSON);
            mapper.writeValue(tmp, root);
            LOGGER.log(Level.INFO, "Response saved to {0}", tmp);
        }

        return root;
    }

    /**
     * Get the filename used to save the JSON response.
     * <p>
     * The file is saved to the systems temporary folder.
     *
     * @return Return the filename prefix to save the JSON response to a file,
     * null otherwise.
     */
    public String getSaveResponseFilename() {
        return null;
    }

    /**
     * Return the {@link JsonNode}
     * <p>
     * This method provides an opportunity to modify the InputStream before
     * making the {@link JsonNode}
     *
     * @param mapper
     * @param bytes
     * @return
     * @throws IOException
     */
    public JsonNode getRootNode(final ObjectMapper mapper, final byte[] bytes) throws IOException {
        final InputStream in = new ByteArrayInputStream(bytes);
        return mapper.readTree(in);
    }

    public String getLogMessage() {
        if (json != null && json.get("logMessage") != null) {
            return json.get("logMessage").textValue();
        }

        return String.format("Invalid response %d: %s%n%s%n", code, message, Arrays.toString(bytes));
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(String.format("[%s%n", this.getClass().getSimpleName()));
        b.append(DASH_STRING);
        b.append(String.format("code    : %d%n", code));
        b.append(String.format("message : %s%n", message));
        b.append(DASH_STRING);
        
        if (headers != null) {
            headers.entrySet().stream().forEach(header -> {
                b.append(String.format("header  : %s%n", header.getKey()));
                header.getValue().stream().forEach(v -> b.append(String.format("        : %s%n", v)));
            });

            b.append(DASH_STRING);
        }

        boolean jsonShown = false;
        if (json != null) {
            try {
                b.append(jsonToString(json));
                b.append(SeparatorConstants.NEWLINE);
                jsonShown = true;
            } catch (final IOException ex) {
            }
        }

        if (!jsonShown && bytes != null) {
            try {
                b.append(new String(bytes, StandardCharsets.UTF_8.name()));
            } catch (final UnsupportedEncodingException ex) {
                b.append(String.format("(bytes: length %d)", bytes.length));
            }
        }

        b.append(DASH_STRING);
        b.append("]\n");

        return b.toString();
    }

    public static String jsonToString(final JsonNode node) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, node);

        return new String(out.toByteArray(), StandardCharsets.UTF_8.name());
    }
}

/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.html;

import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * Emulate an HTML form post.
 *
 * @author algol
 */
public class MultiPart {
    
    private static final Logger LOGGER = Logger.getLogger(MultiPart.class.getName());

    private static final String DASH_DASH = "--";
    private static final String EOL = "\r\n";
    private static final String C_D = "Content-Disposition: form-data";

    private static final String NOT_AFTER_END_CALL = "Not allowed after calling end().";
    private static final String THREE_STRING_FORMAT = "%s%s%s";

    private final ByteArrayOutputStream buf;
    private final String boundary;
    private boolean isEnded;

    public MultiPart() {
        buf = new ByteArrayOutputStream();
        boundary = UUID.randomUUID().toString().replace(SeparatorConstants.HYPHEN, "");
        isEnded = false;
    }

    /**
     * Add a text field to the form.
     *
     * @param key The field name.
     * @param value The field value.
     */
    public void addText(final String key, final String value) {
        if (isEnded) {
            throw new MultiPartException(NOT_AFTER_END_CALL);
        }

        try {
            final String k = htmlEncode(key);
            final String v = htmlEncode(value);
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format(THREE_STRING_FORMAT, DASH_DASH, boundary, EOL));
            sb.append(String.format("%s; name=\"%s\"%s%s%s%s", C_D, k, EOL, EOL, v, EOL));
            final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8.name());
            buf.write(bytes);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Add a file to the form.
     *
     * @param name The file's name.
     * @param content The file content.
     * @param mime The MIME type of the file; if unknown, use
     * "application/octet-stream".
     */
    public void addBytes(final String name, final byte[] content, final String mime) {
        if (isEnded) {
            throw new MultiPartException(NOT_AFTER_END_CALL);
        }

        try {
            final String n = htmlEncode(name);
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format(THREE_STRING_FORMAT, DASH_DASH, boundary, EOL));
            sb.append(String.format("%s; name=\"file\"; filename=\"%s\"%s", C_D, n, EOL));
            sb.append(String.format("Content-Type: %s%s%s", mime, EOL, EOL));
            final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8.name());
            buf.write(bytes);
            buf.write(content);
            buf.write(EOL.getBytes(StandardCharsets.UTF_8.name()));
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Indicate that there are no more fields in the form.
     */
    public void end() {
        if (isEnded) {
            throw new MultiPartException(NOT_AFTER_END_CALL);
        }

        try {
            try (buf) {
                buf.write(String.format(THREE_STRING_FORMAT, DASH_DASH, boundary, DASH_DASH).getBytes(StandardCharsets.UTF_8.name()));
            }
            isEnded = true;
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public String getBoundary() {
        return boundary;
    }

    public int size() {
        return buf.size();
    }

    public byte[] getBuffer() {
        if (!isEnded) {
            throw new MultiPartException("Must call end() first.");
        }

        return buf.toByteArray();
    }

    /**
     * Post this multipart message using the given connection.
     *
     * @param conn An HTTPS connection.
     *
     * @return A (message, location) pair; the message is the http response
     * message, location is null if the response code was not in the 200 range.
     *
     * @throws IOException if the thread is interrupted during the connection.
     */
    public Pair<String, String> post(final HttpURLConnection conn) throws IOException {
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", BrandingUtilities.APPLICATION_NAME);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + getBoundary());
        conn.setRequestProperty("Content-Length", Integer.toString(size()));

        try (final OutputStream os = conn.getOutputStream()) {
            os.write(getBuffer());
        }

        final int code = conn.getResponseCode();

        final int responseClass = code / 100;
        final String location = responseClass == 2 ? conn.getHeaderField("Location") : null;
        
        return new Pair<>(conn.getResponseMessage(), location);
    }

    public static byte[] getBody(final HttpURLConnection conn, final int code) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] buf = new byte[256 * 1024];
        try (final InputStream in = code / 100 == 2 ? conn.getInputStream() : conn.getErrorStream()) {
            while (true) {
                final int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                os.write(buf, 0, len);
            }
        }
        return os.toByteArray();
    }

    private static String htmlEncode(final String s) {
        final StringBuilder sb = new StringBuilder();
        if (s != null) {
            final int len = s.length();
            for (int i = 0; i < len; i++) {
                final char c = s.charAt(i);
                if (c >= ' ' && c <= '~') {
                    sb.append(c);
                } else {
                    sb.append(String.format("&#%d;", (int) c));
                }
            }
        }

        return sb.toString();
    }

    public static class MultiPartException extends RuntimeException {

        public MultiPartException(final String message) {
            super(message);
        }
    }
}

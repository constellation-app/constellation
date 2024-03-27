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
package au.gov.asd.tac.constellation.utilities.https;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * A utilities class to retrieve an input stream which may be GZIP or deflate
 * encoded. Refer to {@link HttpsConnection} for more information.
 *
 * @author arcturus
 */
public class HttpsUtilities {
    
    private HttpsUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a {@code InputStream} from a {@code HttpsURLConnection} using the
     * appropriate input stream depending on whether the content encoding is
     * GZIP or not
     *
     * @param connection A HttpsURLConnection connection
     * @return An InputStream which could be a {@link GZIPInputStream} if the
     * content encoding is GZIP, {@link InflaterInputStream} of the encoding is
     * deflate, InputStream otherwise
     *
     * @throws IOException if an error occurs during the connection.
     */
    public static InputStream getInputStream(final HttpURLConnection connection) throws IOException {
        final String encoding = connection.getContentEncoding();

        if (encoding != null && "gzip".equalsIgnoreCase(encoding)) {
            return new GZIPInputStream(connection.getInputStream());
        } else if (encoding != null && "deflate".equalsIgnoreCase(encoding)) {
            return new InflaterInputStream(connection.getInputStream(), new Inflater(Boolean.TRUE));
        } else {
            return connection.getInputStream();
        }
    }

    /**
     * Get a {@code InputStream} from a {@code HttpsURLConnection} using the
     * appropriate input stream depending on whether the content encoding is
     * GZIP or not
     *
     * @param connection A HttpsURLConnection connection
     * @return An InputStream which could be a {@link GZIPInputStream} if the
     * content encoding is GZIP, {@link InflaterInputStream} of the encoding is
     * deflate,InputStream otherwise which could also be null.
     *
     * @throws IOException if an error occurs during the connection.
     */
    public static InputStream getErrorStream(final HttpURLConnection connection) throws IOException {
        final String encoding = connection.getContentEncoding();

        if (encoding != null && "gzip".equalsIgnoreCase(encoding)) {
            return new GZIPInputStream(connection.getErrorStream());
        } else if (encoding != null && "deflate".equalsIgnoreCase(encoding)) {
            return new InflaterInputStream(connection.getErrorStream(), new Inflater(Boolean.TRUE));
        } else {
            return connection.getErrorStream();
        }
    }

    /**
     * A convenient method to read the error stream and throw an
     * {@link IOException} with a generic error message
     *
     * @param connection A HttpsURLConnection connection
     * @param system A name to represent the system which will be shown as part
     * of the error message
     *
     * @throws IOException
     */
    public static void readErrorStreamAndThrowException(final HttpURLConnection connection, final String system) throws IOException {
        final InputStream responseStream = HttpsUtilities.getErrorStream(connection);
        if (responseStream != null) {
            final StringBuilder message = new StringBuilder();
            final Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8.name());
            final char[] buffer = new char[8 * 1024];
            int c;
            while ((c = reader.read(buffer)) != -1) {
                message.append(buffer, 0, c);
            }

            throw new IOException(
                    String.format("""
                                  An error occurred with the %s service: %d %s
                                  
                                  If problems persist, contact support via Help -> Support
                                  
                                  Technical Error: %s""",
                            system,
                            connection.getResponseCode(),
                            connection.getResponseMessage(),
                            message.toString()
                    )
            );
        }
    }
}

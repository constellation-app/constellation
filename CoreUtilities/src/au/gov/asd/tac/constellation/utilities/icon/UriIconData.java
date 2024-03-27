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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;

/**
 * An IconData implementation allowing an icon to be built using a {@link URI}.
 * Note that this URI must be absolute as per {@link URI#isAbsolute}.
 *
 * @author cygnus_x-1
 */
public class UriIconData extends IconData {

    private static final Logger LOGGER = Logger.getLogger(UriIconData.class.getName());
    private final URI uri;

    public UriIconData(final String uriString) {
        this.uri = URI.create(uriString);
        assert uri.isAbsolute();
    }

    public UriIconData(final URI uri) {
        this.uri = uri;
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected InputStream createRasterInputStream() throws IOException {
        InputStream stream;
        try {
            if (StringUtils.equalsIgnoreCase(uri.getScheme(), "HTTPS")) {
                final HttpsURLConnection connection = HttpsConnection.withUrl(uri.toURL().toString()).get();
                stream = HttpsUtilities.getInputStream(connection);
            } else {
                stream = uri.toURL().openStream();
            }
        } catch (final FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "UriIconData: file not found at {0}", uri);
            stream = null;
        }

        return stream;
    }

    @Override
    protected InputStream createVectorInputStream() throws IOException {
        throw new UnsupportedOperationException("URI data can not be converted to vector input stream.");
    }
}

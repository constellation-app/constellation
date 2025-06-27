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
package au.gov.asd.tac.constellation.utilities.file;

import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javax.net.ssl.HttpsURLConnection;
import org.netbeans.api.annotations.common.StaticResource;

/**
 *
 * @author arcturus
 */
public class DownloadImageUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(DownloadImageUtilities.class.getName());

    /**
     * Icon to state that the download failed
     */
    @StaticResource
    private static final String DOWNLOAD_FAILED_ICON = "au/gov/asd/tac/constellation/utilities/file/resources/download_failed.png";

    private DownloadImageUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Download a png file and return an {@code Image} instance of the image. If
     * the download failed for whatever reason, an image with
     * {@code DOWNLOAD_FAILED_ICON} will be returned.
     *
     * @param q
     * @return an {@code Image}
     */
    public static Image getImage(final String q) {
        Image img = null;

        HttpsURLConnection connection = null;
        try {
            connection = HttpsConnection.withUrl(q).acceptPng().withReadTimeout(10 * 1000).get();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                img = new Image(HttpsUtilities.getInputStream(connection));
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            img = new Image(DOWNLOAD_FAILED_ICON);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return img;
    }

}

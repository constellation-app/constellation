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
package au.gov.asd.tac.constellation.utilities.nifi.rest;

import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.nifi.FlowFileV3Utilities;
import au.gov.asd.tac.constellation.utilities.nifi.NifiConfig;
import au.gov.asd.tac.constellation.utilities.rest.RestClient;
import static au.gov.asd.tac.constellation.utilities.rest.RestClient.generateUrl;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.openide.util.Lookup;

/**
 * A Niagara Files Client.
 *
 * @author cygnus_x-1
 */
public class NifiClient extends RestClient {

    private static final Logger LOGGER = Logger.getLogger(NifiClient.class.getName());

    private static final NifiConfig DEFAULT_CONFIG = Lookup.getDefault().lookup(NifiConfig.class);

    // This is used to prevent the same file being submitted multiple times.
    // TODO: make this more robust and efficient.
    private static final HashMap<String, String> SUBMIT_CACHE = new HashMap<>();

    private NifiFileSubmitResponse postToNodes(final List<Tuple<String, String>> headers, final byte[] bytes, final Boolean stopAfterFirstAccept) throws IOException {
        final List<String> nodes = DEFAULT_CONFIG.getNifiNodes();
        boolean anyNodeResponded = false;
        String acceptingNode = null;

        NifiFileSubmitResponse response = null;
        for (final String node : nodes) {
            LOGGER.log(Level.INFO, "Posting to {0}", node);
            try {
                postWithBytes(node, headers, bytes);
                anyNodeResponded = true;
                if (this.responseCode == 200) {
                    response = new NifiFileSubmitResponse(this.responseCode, this.responseMessage, this.headerFields, this.bytes);
                    acceptingNode = node;
                    LOGGER.log(Level.INFO, "Success: response code {0} from node {1}", new Object[]{responseCode, node});
                    if (Boolean.TRUE.equals(stopAfterFirstAccept)) {
                        LOGGER.log(Level.INFO, "Stopping after node {0} accepted request", node);
                        break;
                    } else {
                        LOGGER.log(Level.INFO, "Continuing to POST to other nodes...");
                    }
                } else {
                    LOGGER.log(Level.INFO, "Failure: response code {0} from node {1}, trying next node...", new Object[]{responseCode, node});
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex, () -> "Failure: could not post to node " + node);
            }
        }
        LOGGER.log(Level.INFO, "Finished posting to nodes");

        if (response != null && response.isSuccess()) {
            LOGGER.log(Level.INFO, "Success: The upstream server {0} accepted the file, file submitted for ingest", acceptingNode);
        } else {
            LOGGER.log(Level.INFO, "Failure: {0}", (anyNodeResponded ? "nodes responded but failed" : " All nodes did not respond"));
        }

        return response;
    }

    public NifiFileSubmitResponse postToNifi(final String filePath, final Map<String, String> flowfileAttributes) throws IOException {
        flowfileAttributes.put("adds.source.system", BrandingUtilities.APPLICATION_NAME);
        LOGGER.log(Level.INFO, "Posting to NiFi: {0}, {1}", new Object[]{filePath, flowfileAttributes});

        // package the flowfile
        final File file = new File(filePath);
        if (DEFAULT_CONFIG.duplicateFilterEnabled()) {
            final String hashString;
            hashString = Files.asByteSource(file).hash(Hashing.sha256()).toString();
            if (SUBMIT_CACHE.containsKey(hashString)) {
                LOGGER.log(Level.SEVERE, "A file with matching contents has already been submitted (original: {0}).", SUBMIT_CACHE.get(hashString));
                return null;
            }
            SUBMIT_CACHE.put(hashString, filePath);
        }

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            final InputStream is = new FileInputStream(file);
            FlowFileV3Utilities.packageFlowFile(is, os, flowfileAttributes, file.length());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Error writing flow file", ex);
            return null;
        }

        // To avoid using expensive identity mime type at nifi, we specify ffv3
        // in the headers and nifi will apply this as an attribute which can be
        // routed on instead.
        final List<Tuple<String, String>> headers = new ArrayList<>();
        headers.add(new Tuple("flexloader.type", "flowfile-v3"));
        
        
        
        return postToNodes(headers, os.toByteArray(), true);
    }

    public NifiFileSubmitResponse submitAndDelete(final String fileHandle, final Map<String, String> attributes) throws IOException {
        // submit the file to nifi
        final NifiFileSubmitResponse response = postToNifi(getFullFilename(fileHandle), attributes);

        // delete the file
        if (response != null && response.isSuccessWithJson()) {
            try {
                // TODO: remove copy
                copyFile(fileHandle, fileHandle + "." + ".submitted." + Instant.now().toEpochMilli());
                deleteFile(fileHandle);
            } catch (final IOException ex) {
                // The file delete failed but the submit was ok, so ignore this exception.
                LOGGER.log(Level.SEVERE, "Error ", ex);
            }
        }

        return response;
    }

    private String getFullFilename(final String fileName) {
        return DEFAULT_CONFIG.getNifiUri() + fileName;
    }
    
    private void copyFile(final String fileName, final String newFileName) throws IOException {
        final String fileOnServer = DEFAULT_CONFIG.getNifiUri() + fileName;
        final String newFileOnServer = DEFAULT_CONFIG.getNifiUri() + newFileName;
        java.nio.file.Files.copy(new File(fileOnServer).toPath(), new File(newFileOnServer).toPath());
    }

    private void deleteFile(final String fileName) throws IOException {
        final String fileOnServer = DEFAULT_CONFIG.getNifiUri() + fileName;
        java.nio.file.Files.deleteIfExists(new File(fileOnServer).toPath());
    }

    @Override
    public HttpsURLConnection makeGetConnection(final String url, final List<Tuple<String, String>> params) throws IOException {
        final URL nifiUrl = generateUrl(url, params);
        return HttpsConnection
                .withUrl(nifiUrl.toString())
                .doOutput()
                .acceptJson()
                .addRequestProperty(HttpsConnection.CONTENT_TYPE, HttpsConnection.APPLICATION_FORM)
                .withReadTimeout(60 * 1000)
                .get();
    }

    @Override
    public HttpsURLConnection makePostConnection(String url, List<Tuple<String, String>> params) throws IOException {
        final URL nifiUrl = generateUrl(url, params);
        return HttpsConnection
                .withUrl(nifiUrl.toString())
                .doOutput()
                .acceptJson()
                .addRequestProperty(HttpsConnection.CONTENT_TYPE, HttpsConnection.APPLICATION_FORM)
                .addRequestProperty("flexloader.type", "flowfile-v3")
                .withReadTimeout(60 * 1000)
                .post();
    }
}

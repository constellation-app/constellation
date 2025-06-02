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
package au.gov.asd.tac.constellation.webserver.transport;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.webserver.WebServer;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceRegistry;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;

/**
 * Implement HTTP on a filesystem.
 * <p>
 * When started in a thread, listen for files in a specified directory, call the
 * appropriate methods, and write one or more files in response.
 *
 * @author algol
 */
public class FileListener implements Runnable {

    private static final long MIN_POLL_SLEEP = 1000;
    private static final long MAX_POLL_SLEEP = 5000;
    private static final Logger LOGGER = Logger.getLogger(FileListener.class.getName());

    private static final String REQUEST_JSON = "request.json";      // The JSON document containing the request.
    private static final String RESPONSE_JSON = "response.json";    // The JSON document containing the response.
    private static final String CONTENT_IN = "content.in";          // The file containing input data (may be JSON / binary / anything).
    private static final String CONTENT_OUT = "content.out";        // The file containing ioutput data (may be JSON / binary / anything).

    private static final String ENDPOINT = "endpoint";

    private final Path restPath;
    private volatile boolean running;

    public FileListener() throws IOException {

        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        restPath = Paths.get(ApplicationPreferenceKeys.getRESTDir(prefs));
        if (Files.isDirectory(restPath)) {
            // Delete any existing files in the REST directory.
            // We don't want to execute any left over commands from a previous run.
            final String[] files = restPath.toFile().list();
            for (final String f : files) {
                final Path p = restPath.resolve(f);
                LOGGER.log(Level.WARNING, "{0}", String.format("Deleting old REST file %s", p));
                Files.delete(p);
            }

        } else {
            Files.createDirectories(restPath);
        }
        running = false;
    }

    public void stop() {
        running = false;
    }

    /**
     * Run the file listener thread.
     * <p>
     * Poll the listener directory looking for the REQUEST_JSON file. It assumed
     * that the client has already written the CONTENT_IN file if required.
     * <p>
     * When REQUEST_JSON is found, read and extract the verb + endpoint + path +
     * args. Call the multi-level switch statement that figures out what to do.
     * Some of these will write CONTENT_OUT. Ensure that CONTENT_IN is deleted
     * when we've finished with it. Ensure that REQUEST_JSON is deleted when
     * we're finished with it. Last of all, write the RESPONSE_JSON file: this
     * is what the client will be polling on. An empty JSON document implies
     * success. A JSON document with the "error" key is failure, with the
     * explanation in the value. The client will delete RESPONSE_JSON and
     * CONTENT_OUT.
     */
    @Override
    public void run() {
        // Download the Python REST client if enabled.
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final boolean pythonRestClientDownload = prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);
        if (pythonRestClientDownload) {
            WebServer.downloadPythonClient();
        }

        StatusDisplayer.getDefault().setStatusText(String.format("Starting file listener in directory %s", restPath));

        running = true;

        // We start the poll sleep time at MIN_POLL_SLEEP.
        // If the listener doesn't find any files, the sleep time will slowly get longer up to MAX_POLL_SLEEP,
        // so the filesystem doesn't get pounded as much.
        // When a request is found, the poll sleep time is reset to MIN_POLL_SLEEP.
        long pollSleep = MIN_POLL_SLEEP;
        try {
            while (running) {
                final String[] files = restPath.toFile().list();
                for (final String f : files) {
                    if (f.equals(REQUEST_JSON)) {
                        // If any other files are required, write this file last, so the other files are already present.
                        LOGGER.log(Level.INFO, "{0}", String.format("Found REST file %s", f));
                        final Path p = restPath.resolve(f);
                        JsonNode json = null;
                        try (final InputStream in = new FileInputStream(p.toFile())) {
                            final ObjectMapper mapper = new ObjectMapper();
                            json = mapper.readTree(in);
                        } catch (final IOException ex) {
                            response(ex.getMessage());
                        }

                        try {
                            Files.delete(p);
                        } catch (final IOException ex) {
                            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                        }

                        if (json != null) {
                            // Extract the equivalent of a REST URL from the request JSON.
                            // The HTTP request GET http://localhost/v1/plugin/run?name=selectall
                            // becomes the JSON document
                            // {
                            //   "verb": "get",
                            //   "endpoint": "/v1/plugin",
                            //   "path": "run",
                            //   "args": { "name": "selectall" }
                            // }
                            //
                            // If content (JSON or otherwise) is required, it gets delivered in a separate CONTENT_DATA file.
                            if (json.hasNonNull("verb") && json.hasNonNull(ENDPOINT) && json.hasNonNull("path")) {
                                final String verb = json.get("verb").textValue();
                                final String endpoint = json.get(ENDPOINT).textValue();
                                final String path = json.get("path").textValue();

                                final JsonNode args = json.get("args");
                                try {
                                    // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
                                    final String msg = String.format("File REST API: %s %s %s", verb, endpoint, path);
                                    StatusDisplayer.getDefault().setStatusText(msg);

                                    parseAndExecute(verb, endpoint, path, args);
                                    response();
                                } catch (final RestServiceException ex) {
                                    response(ex.getMessage());
                                } catch (final Exception ex) {
                                    response(ex.getMessage());
                                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                                }
                            } else {
                                response("Request must contain verb + endpoint + path");
                            }
                        }

                        // Reset the poll sleep after a successful request,
                        // since we're obviously being used.
                        pollSleep = MIN_POLL_SLEEP;
                    }
                }

                // Slowly sneak the poll sleep time up to a maximum.
                pollSleep = Math.min(pollSleep + 1, MAX_POLL_SLEEP);
                Thread.sleep(pollSleep);
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            stop();
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        StatusDisplayer.getDefault().setStatusText(String.format("Stopped file listener in directory %s", restPath));
    }

    /**
     * Execute a REST endpoint.
     *
     * @param node A JSON node representing the input parameters.
     *
     * @throws Exception because of AutoCloseable
     */
    private void parseAndExecute(final String verb, final String endpoint, final String path, final JsonNode args) throws Exception {
        if ("/v2/service".equals(endpoint)) {
            final HttpMethod httpMethod = HttpMethod.getValue(verb);
            // Get an instance of the service (if it exists).
            //
            final RestService rs = RestServiceRegistry.get(path, httpMethod);

            // Convert the arguments to PluginParameters.
            //
            final PluginParameters parameters = rs.createParameters();
            RestServiceUtilities.parametersFromJson((ObjectNode) args, parameters);

            try (final InStream ins = new InStream(restPath, CONTENT_IN, true); final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                rs.callService(parameters, ins.in, out);
            } catch (final IOException | RuntimeException ex) {
                throw new RestServiceException(ex);
            }
        } else {
            unrec(ENDPOINT, endpoint);
        }
    }

    /**
     * An InputStream that deletes its input file on close.
     * <p>
     * We don't want the input file left lying around after we're finished with
     * it.
     * <p>
     * Services may or may not require an input file, we don't know. Therefore
     * the "optional" parameter allows the input file to not exist. If a service
     * tries to use an InputStream that doesn't exist, things go bang.
     */
    private static class InStream implements AutoCloseable {

        final File fqp;
        final InputStream in;

        public InStream(final Path p, final String name) throws FileNotFoundException {
            this(p, name, false);
        }

        public InStream(final Path p, final String name, final boolean optional) throws FileNotFoundException {
            fqp = p.resolve(name).toFile();
            if (fqp.canRead()) {
                in = new FileInputStream(fqp);
            } else if (optional) {
                in = null;
            } else {
                throw new FileNotFoundException(fqp.getAbsolutePath());
            }
        }

        @Override
        public void close() {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, "Error occurred while attempting to close input stream");
                }
                try {
                    Files.delete(Path.of(fqp.getPath()));
                } catch (final IOException ex) {
                    //TODO: Handle case where file not successfully deleted
                }
            }
        }
    }

    private static OutputStream outStream(final Path p, final String name) {
        final Path fqp = p.resolve(name);
        try {
            return new FileOutputStream(fqp.toFile());
        } catch (final FileNotFoundException ex) {
            throw new RestServiceException(ex);
        }
    }

    private void unrec(final String type, final String name) {
        final String msg = String.format("Unrecognised %s '%s'", type, name);
        throw new RestServiceException(msg);
    }

    private void response() {
        response(null);
    }

    /**
     * Create a JSON response.
     * <p>
     * If a non-null message is supplied, it will be added to the document with
     * the "error" key. The message will be returned to the caller.
     *
     * @param message A string describing the error.
     */
    private void response(final String message) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        if (message != null) {
            root.put("error", message);
            LOGGER.warning(message);
        }

        final Path p = restPath.resolve(RESPONSE_JSON);
        try (final OutputStream out = new FileOutputStream(p.toFile())) {
            mapper.writeValue(out, root);
        } catch (final IOException ex1) {
            LOGGER.log(Level.SEVERE, ex1.getLocalizedMessage(), ex1);
        }
    }
}

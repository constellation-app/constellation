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
package au.gov.asd.tac.constellation.webserver.transport;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.webserver.WebServer;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import au.gov.asd.tac.constellation.webserver.impl.GraphImpl;
import au.gov.asd.tac.constellation.webserver.impl.IconImpl;
import au.gov.asd.tac.constellation.webserver.impl.PluginImpl;
import au.gov.asd.tac.constellation.webserver.impl.RecordStoreImpl;
import au.gov.asd.tac.constellation.webserver.impl.TypeImpl;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
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
                LOGGER.warning(String.format("Deleting old REST file %s", p));
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
                        LOGGER.info(String.format("Found REST file %s", f));
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
                            Exceptions.printStackTrace(ex);
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
                            if (json.hasNonNull("verb") && json.hasNonNull("endpoint") && json.hasNonNull("path")) {
                                final String verb = json.get("verb").textValue();
                                final String endpoint = json.get("endpoint").textValue();
                                final String path = json.get("path").textValue();

                                final JsonNode args = json.get("args");
                                try {
                                    // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
                                    final String msg = String.format("File REST API: %s %s %s", verb, endpoint, path);
                                    StatusDisplayer.getDefault().setStatusText(msg);

                                    parseAndExecute(verb, endpoint, path, args);
                                    response();
                                } catch (final EndpointException ex) {
                                    response(ex.getMessage());
                                } catch (final Exception ex) {
                                    response(ex.getMessage());
                                    Exceptions.printStackTrace(ex);
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
            stop();
            Exceptions.printStackTrace(ex);
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
        final String graphId = getString(args, "graph_id");
        switch (endpoint) {
            case "/v1/graph":
                switch (verb) {
                    case "get":
                        switch (path) {
                            case "getattrs":
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    GraphImpl.get_attributes(graphId, out);
                                }
                                break;
                            case "get":
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    GraphImpl.get_get(graphId, out);
                                }
                                break;
                            case "image":
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    GraphImpl.get_image(out);
                                }
                                break;
                            case "schema":
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    GraphImpl.get_schema(out);
                                }
                                break;
                            case "schema_all":
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    GraphImpl.get_schema_all(out);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    case "post":
                        switch (path) {
                            case "set":
                                try (final InStream in = new InStream(restPath, CONTENT_IN)) {
                                    GraphImpl.post_set(graphId, in.in);
                                }
                                break;
                            case "new":
                                final String schemaParam = getString(args, "schema");
                                GraphImpl.post_new(schemaParam);
                                break;
                            case "open":
                                final String filenameParam = getString(args, "filename");
                                if (filenameParam == null) {
                                    throw new EndpointException("Required filename not found");
                                }

                                GraphImpl.post_open(filenameParam);
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    case "put":
                        switch (path) {
                            case "current":
                                final String gid = getString(args, "id");
                                if (gid != null) {
                                    GraphImpl.put_current(gid);
                                } else {
                                    throw new EndpointException("Must specify id");
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    default:
                        unrec("verb", verb);
                        break;
                }
                break;
            case "/v1/icon":
                switch (verb) {
                    case "get":
                        switch (path) {
                            case "list":
                                final Boolean editable = getBoolean(args, "editable");
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    IconImpl.get_list(editable, out);
                                }
                                break;
                            case "get":
                                final String name = getString(args, "name");
                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    IconImpl.get_get(name, out);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    default:
                        unrec("verb", verb);
                        break;
                }
                break;
            case "/v1/plugin":
                switch (verb) {
                    case "get":
                        switch (path) {
                            case "list":
                                final Boolean alias = getBooleanNonNull(args, "alias");

                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    PluginImpl.get_list(alias, out);
                                }
                                break;
                            default:
                                unrec("path", path);
                                break;
                        }
                        break;
                    case "post":
                        switch (path) {
                            case "run":
                                final String pluginName = getString(args, "name");
                                if (pluginName == null) {
                                    throw new EndpointException("No plugin specified!");
                                }

                                try (final InStream in = new InStream(restPath, CONTENT_IN)) {
                                    PluginImpl.post_run(graphId, pluginName, in.in);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    default:
                        unrec("verb", verb);
                        break;
                }
                break;
            case "/v1/recordstore":
                switch (verb) {
                    case "get":
                        switch (path) {
                            case "get":
                                final boolean selected = getBooleanNonNull(args, "selected");
                                final boolean vx = getBooleanNonNull(args, "vx");
                                final boolean tx = getBooleanNonNull(args, "tx");

                                // Allow the user to specify a specific set of attributes,
                                // cutting down data transfer and processing a lot,
                                // particularly on the Python side.
                                final String attrsParam = getString(args, "attrs");
                                final String[] attrsArray = attrsParam != null ? attrsParam.split(",") : new String[0];
                                final Set<String> attrs = new LinkedHashSet<>(); // Maintain the order specified by the user.
                                for (final String k : attrsArray) {
                                    attrs.add(k);
                                }

                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    RecordStoreImpl.get_get(graphId, vx, tx, selected, attrs, out);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    case "post":
                        switch (path) {
                            case "add":
                                final Boolean completeWithSchemaParam = getBoolean(args, "complete_with_schema");
                                final boolean completeWithSchema = completeWithSchemaParam == null ? true : completeWithSchemaParam;

                                final String arrangeParam = getString(args, "arrange");
                                final String arrange = arrangeParam == null ? null : arrangeParam;

                                final Boolean resetViewParam = getBoolean(args, "reset_view");
                                final boolean resetView = resetViewParam == null ? true : resetViewParam;

                                try (final InStream in = new InStream(restPath, CONTENT_IN)) {
                                    RecordStoreImpl.post_add(graphId, completeWithSchema, arrange, resetView, in.in);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    default:
                        unrec("verb", verb);
                        break;
                }
                break;
            case "/v1/type":
                switch (verb) {
                    case "get":
                        switch (path) {
                            case "describe":
                                final String type = getString(args, "type");
                                if (type == null) {
                                    throw new EndpointException("No type specified.");
                                }

                                try (final OutputStream out = outStream(restPath, CONTENT_OUT)) {
                                    TypeImpl.get_describe(type, out);
                                }
                                break;
                            default:
                                unrec("path", path);
                        }
                        break;
                    default:
                        unrec("verb", verb);
                        break;
                }
                break;
            default:
                unrec("endpoint", endpoint);
                break;
        }
    }

    private static String getString(final JsonNode j, final String key) {
        return j != null && j.hasNonNull(key) ? j.get(key).textValue() : null;
    }

    private static Boolean getBoolean(final JsonNode j, final String key) {
        return j != null && j.hasNonNull(key) ? j.get(key).booleanValue() : null;
    }

    private static boolean getBooleanNonNull(final JsonNode j, final String key) {
        return j != null && j.hasNonNull(key) ? j.get(key).booleanValue() : false;
    }

    /**
     * An InputStream that deletes its input file on close.
     * <p>
     * We don't want the input file left lying around after we're finished with
     * it.
     */
    private static class InStream implements AutoCloseable {

        final File fqp;
        final InputStream in;

        InStream(final Path p, final String name) throws FileNotFoundException {
            fqp = p.resolve(name).toFile();
            in = new FileInputStream(fqp);
        }

        InputStream in() {
            return in;
        }

        @Override
        public void close() {
            try {
                in.close();
            } catch (IOException ex) {
            }
            fqp.delete();
        }
    }

    private static OutputStream outStream(final Path p, final String name) {
        final Path fqp = p.resolve(name);
        try {
            final OutputStream out = new FileOutputStream(fqp.toFile());
            return out;
        } catch (final FileNotFoundException ex) {
            throw new EndpointException(ex);
        }
    }

    private void unrec(final String type, final String name) {
        final String msg = String.format("Unrecognised %s '%s'", type, name);
        throw new EndpointException(msg);
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
            Exceptions.printStackTrace(ex1);
        }
    }
}

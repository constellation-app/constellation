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
package au.gov.asd.tac.constellation.webserver.api;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceRegistry;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * A servlet that displays the REST API using swagger-ui.
 * <p>
 * Swagger is obtained from https://github.com/swagger-api/swagger-ui. The
 * swagger packages contain the contents of swagger-ui/dist unzipped as-is with
 * the addition of the constellation.json swagger config file. The index.html
 * file has been modified to link to constellation.json
 * <p>
 * The JSON returned by the servlet is modified on the fly to contain REST
 * service information, using the information from each service and the
 * PluginParameters instance from each service's createParameters().
 * <p>
 * Some heuristics are used.
 * <ul>
 * <li>If the getName() of a parameter contains the string "(body)", that
 * parameter specified to Swagger as a body parameter instead of a query
 * parameter.</li>
 * <li>If the service returns JSON, and the name of the service starts with
 * "list" (case-insensitive), the schema of the returned JSON is specified as a
 * list of object; otherwise, the default schema is object.
 * </ul>
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "SwaggerDoc",
        description = "Swagger documentation for the REST API",
        urlPatterns = {"/swagger/*", "/swagger-ui/*"})
public class SwaggerServlet extends ConstellationHttpServlet {

    /**
     * This *must* match the URL pattern for RestServiceServlet.
     */
    private static final String SERVICE_PATH = "/v2/service/%s";

    private static final String DESCRIPTION = "description";
    private static final String SCHEMA = "schema";
    private static final String REQUIRED = "required";
    private static final String OBJECT = "object";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String requestPath = request.getPathInfo();
        final String fileName = request.getServletPath().substring(1) + requestPath;

        try {
            final InputStream in = SwaggerServlet.class.getResourceAsStream(fileName);

            if ("swagger/constellation.json".equals(fileName)) {
                // The file constellation.json contains our swagger info.
                // Dynamically add data and services.
                final ObjectMapper mapper = new ObjectMapper();
                final ObjectNode root = (ObjectNode) mapper.readTree(in);

                // Get the hostname:port right.
                final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
                final int port = prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT);
                final ArrayNode servers = root.putArray("servers");
                final ObjectNode server = servers.addObject();
                server.put("url", String.format("http://localhost:%d", port));

                // Add the REST services.
                final ObjectNode paths = (ObjectNode) root.get("paths");
                RestServiceRegistry.getServices().forEach(serviceKey -> {
                    final ObjectNode path = paths.putObject(String.format(SERVICE_PATH, serviceKey.name));
                    final ObjectNode httpMethod = path.putObject(serviceKey.httpMethod.name().toLowerCase(Locale.ENGLISH));

                    final RestService rs = RestServiceRegistry.get(serviceKey);

                    httpMethod.put("summary", rs.getDescription());

                    if (rs.getTags().length > 0) {
                        final ArrayNode tags = httpMethod.putArray("tags");
                        for (final String tag : rs.getTags()) {
                            tags.add(tag);
                        }
                    }

                    // Most parameters are passed in the URL query.
                    // Some parameters are passed in the body of the request.
                    // Since PluginParameter doesn't have an option to specify
                    // this, we'll improvise and look for "(body)" in the
                    // parameter name. These will be dummy parameters,
                    // unused except for their swagger description.
                    final ArrayNode params = httpMethod.putArray("parameters");
                    rs.createParameters().getParameters().entrySet().forEach(entry -> {
                        final PluginParameter<?> pp = entry.getValue();

                        if (pp.getName().toLowerCase(Locale.ENGLISH).contains("(body)")) {
                            final ObjectNode requestBody = httpMethod.putObject("requestBody");
                            requestBody.put(DESCRIPTION, pp.getName().replace("(body)", " - ") + pp.getDescription());
                            requestBody.put(REQUIRED, pp.isRequired());
                            final ObjectNode content = requestBody.putObject("content");
                            final ObjectNode mime = content.putObject(RestServiceUtilities.APPLICATION_JSON);
                            final ObjectNode schema = mime.putObject(SCHEMA);
                            schema.put("$ref", pp.getRequestBodyExampleJson());
                        } else {
                            final ObjectNode param = params.addObject();
                            param.put("name", pp.getId());
                            param.put("in", "query");
                            param.put(REQUIRED, pp.isRequired());
                            param.put(DESCRIPTION, pp.getDescription());
                            final ObjectNode schema = param.putObject(SCHEMA);
                            schema.put("type", pp.getType().getId());
                        }
                    });

                    // Add the required CONSTELLATION secret header parameter.
                    final ObjectNode secretParam = params.addObject();
                    secretParam.put("name", "X-CONSTELLATION-SECRET");
                    secretParam.put("in", "header");
                    secretParam.put(REQUIRED, true);
                    secretParam.put(DESCRIPTION, "CONSTELLATION secret");
                    final ObjectNode secretSchema = secretParam.putObject(SCHEMA);
                    secretSchema.put("type", "string");

                    final ObjectNode responses = httpMethod.putObject("responses");
                    final ObjectNode success = responses.putObject("200");
                    success.put(DESCRIPTION, rs.getDescription());

                    if (rs.getMimeType().equals(RestServiceUtilities.APPLICATION_JSON)) {
                        final ObjectNode content = success.putObject("content");
                        final ObjectNode mime = content.putObject(rs.getMimeType());
                        final ObjectNode schema = mime.putObject(SCHEMA);
                        // Make a wild guess about the response.
                        if (serviceKey.name.toLowerCase(Locale.ENGLISH).startsWith("list")) {
                            schema.put("type", "array");
                            final ObjectNode items = schema.putObject("items");
                            items.put("type", OBJECT);
                        } else {
                            schema.put("type", OBJECT);
                        }
                    } else {
                        // Do nothing
                    }
                });

                final OutputStream out = response.getOutputStream();
                mapper.writeValue(out, root);
            } else {
                if (fileName.endsWith(FileExtensionConstants.JAVASCRIPT)) {
                    response.setContentType("text/javascript");
                }

                // This is every other file, just transfer the bytes.
                try (final OutputStream out = response.getOutputStream()) {
                    final byte[] buf = new byte[8192];
                    while (true) {
                        final int len = in.read(buf);
                        if (len == -1) {
                            break;
                        }
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (final IOException ex) {
            throw new ServletException(ex);
        }
    }
}

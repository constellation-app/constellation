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
package au.gov.asd.tac.constellation.webserver.api;

import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service for displaying the REST API using swagger-ui.
 * <p>
 * The swagger packages contain the contents of swagger-ui/dist unzipped as-is
 * with the addition of the constellation.json swagger config file.
 * The index.html file has been modified to link to constellation.json
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "SwaggerDoc",
        description = "Swagger documentation for the REST API",
        urlPatterns = {"/swagger-ui/*"})
public class SwaggerServlet extends ConstellationHttpServlet {

    /**
     * This *must* match the URL pattern for RestServiceServlet.
     */
    private static final String SERVICE_PATH = "/v1/service/%s";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String requestPath = request.getPathInfo();
        final String fnam = "swagger" + requestPath;

        try {
            final InputStream in = SwaggerServlet.class.getResourceAsStream(fnam);

            if(fnam.equals("swagger/constellation.json")) {
                // The file constellation.json contains our swagger info.
                // Dynamically add services.
                //

                final Set<String> serviceTags = new HashSet<>();

                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode root = mapper.readTree(in);
                final ObjectNode paths = (ObjectNode)root.get("paths");
                RestServiceRegistry.getServices().forEach(serviceKey -> {
                    final ObjectNode path = paths.putObject(String.format(SERVICE_PATH, serviceKey.name));
                    final ObjectNode httpMethod = path.putObject(serviceKey.httpMethod.name().toLowerCase(Locale.US));

                    final RestService rs = RestServiceRegistry.get(serviceKey);

                    final ArrayNode tags = httpMethod.putArray("tags");
                    for(final String tag : rs.getTags()) {
                        tags.add(tag);
                        serviceTags.add(tag);
                    }

                    httpMethod.put("description", rs.getDescription());

                    final ArrayNode params = httpMethod.putArray("parameters");
                    rs.createParameters().getParameters().entrySet().forEach(entry -> {
                        final PluginParameter<?> pp = entry.getValue();
                        final ObjectNode param = params.addObject();
                        param.put("in", "query");
                        param.put("required", false); // TODO Hard-code this until PluginParameters grows a required field.
                        param.put("name", pp.getName());
                        param.put("type", pp.getType().getId());
                        param.put("description", pp.getDescription());
                        if(pp.getObjectValue()!=null) {
                            param.put("value", pp.getObjectValue().toString());
                        }
                    });

                    // Add the required CONSTELLATION secret header parameter.
                    //
                    {
                        final ObjectNode param = params.addObject();
                        param.put("name", "X-CONSTELLATION-SECRET");
                        param.put("in", "header");
                        param.put("type", "string");
                        param.put("description", "CONSTELLATION secret (from ~/.CONSTELLATION/rest.json)");
                        param.put("required", true);
                    }
                });

                // Update the tags: discover which tags are already there, and add the rest.
                //
                final ArrayNode tagArray = (ArrayNode)root.get("tags");
                tagArray.forEach(existing -> {
                    final String name = existing.get("name").textValue();
                    serviceTags.remove(name);
                });

                serviceTags.forEach(tag -> {
                    final ObjectNode newTag = tagArray.addObject();
                    newTag.put("name", tag);
                    newTag.put("description", String.format("Related to %s operations", tag));
                });

                final OutputStream out = response.getOutputStream();
                mapper.writeValue(out, root);
            } else {
                // This is every other file.
                // Just transfer the bytes.
                //
                try(OutputStream out = response.getOutputStream()) {
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

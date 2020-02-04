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

import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.impl.RecordStoreImpl;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service that allows a client use RecordStores for interaction with
 * graphs in Constellation.
 * <p>
 * For testing, see the Python script in the corresponding unit test package.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "RecordStoreAPI",
        description = "REST API for RecordStore",
        urlPatterns = {"/v1/recordstore/*"})
public class RecordStoreServlet extends ConstellationApiServlet {

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        switch (request.getPathInfo()) {
            case "/get":
                // Get (parts of) the currently active graph as a RecordStore.
                final String graphId = request.getParameter("graph_id");

                final boolean selected = Boolean.parseBoolean(request.getParameter("selected"));
                final boolean vx = Boolean.parseBoolean(request.getParameter("vx"));
                final boolean tx = Boolean.parseBoolean(request.getParameter("tx"));

                // Allow the user to specify a specific set of attributes,
                // cutting down data transfer and processing a lot,
                // particularly on the Python side.
                final String attrsParam = request.getParameter("attrs");
                final String[] attrsArray = attrsParam != null ? attrsParam.split(",") : new String[0];
                final Set<String> attrs = new LinkedHashSet<>(); // Maintain the order specified by the user.
                for (final String k : attrsArray) {
                    attrs.add(k);
                }

                RecordStoreImpl.get_get(graphId, vx, tx, selected, attrs, response.getOutputStream());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().close();

                break;
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }
    }

    @Override
    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/add":
                // Add data to a new store, and add the store to the graph.
                // If any transaction does not specify a source, add our own.
                final String graphId = request.getParameter("graph_id");

                final String completeWithSchemaParam = request.getParameter("complete_with_schema");
                final boolean completeWithSchema = completeWithSchemaParam == null ? true : Boolean.parseBoolean(completeWithSchemaParam);

                final String arrangeParam = request.getParameter("arrange");
                final String arrange = arrangeParam == null ? null : arrangeParam;

                final String resetViewParam = request.getParameter("reset_view");
                final boolean resetView = resetViewParam == null ? true : Boolean.parseBoolean(resetViewParam);

                RecordStoreImpl.post_add(graphId, completeWithSchema, arrange, resetView, request.getInputStream());

                break;
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }
    }
}

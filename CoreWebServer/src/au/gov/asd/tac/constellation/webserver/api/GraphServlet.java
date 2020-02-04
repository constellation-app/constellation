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
import au.gov.asd.tac.constellation.webserver.impl.GraphImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service that allows a client to interact with graphs in Constellation.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "GraphAPI",
        description = "REST API for graphs",
        urlPatterns = {"/v1/graph/*"})
public class GraphServlet extends ConstellationApiServlet {

    private static final String IMAGE_TYPE = "png";

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final String graphId = request.getParameter("graph_id");

        switch (request.getPathInfo()) {
            case "/getattrs": {
                // Return the graph, vertex, and transaction attributes as a map.
                GraphImpl.get_attributes(graphId, response.getOutputStream());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
            case "/get": {
                // Return the graph attribute values in DataFrame format.
                GraphImpl.get_get(graphId, response.getOutputStream());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
            case "/image": {
                // Return a screenshot of the graph.

                final boolean hasContent = GraphImpl.get_image(response.getOutputStream());
                if (hasContent) {
                    response.setContentType(IMAGE_TYPE);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
                break;
            }
            case "/schema": {
                // Return:
                //      the id of the active graph, or null if there is no active graph;
                //      if there is an active graph, the graph's name and schema (which may be null).

                GraphImpl.get_schema(response.getOutputStream());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
            case "/schema_all": {
                // Return the id, name, and schema of all open graphs.
                GraphImpl.get_schema_all(response.getOutputStream());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }

        response.getOutputStream().close();
    }

    @Override
    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final String graphId = request.getParameter("graph_id");

        switch (request.getPathInfo()) {
            case "/set":
                // We want to read a JSON document that looks like:
                //
                // {"columns":["A","B"],"data":[[1,"a"]]}
                //
                // which is what is output by pandas.to_json(..., orient="split').
                // (We ignore the index array.)
                GraphImpl.post_set(graphId, request.getInputStream());

                break;
            case "/new":
                // Open a new graph.
                // Use the specified schema for the new graph, or the default if no schema was specified.
                final String schemaParam = request.getParameter("schema");
                GraphImpl.post_new(schemaParam);

                break;
            case "/open":
                // Open an existing graph.
                final String filenameParam = request.getParameter("filename");
                if (filenameParam == null) {
                    throw new ServletException("Required filename not found");
                }

                GraphImpl.post_open(filenameParam);

                break;
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }
    }

    @Override
    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        final String graphId = request.getParameter("graph_id");

        switch (request.getPathInfo()) {
            case "/current":
                // Make the specified graph the current graph.
                if (graphId != null) {
                    GraphImpl.put_current(graphId);
                } else {
                    throw new ServletException("Must specify graph_id");
                }
                break;
        }
    }
}

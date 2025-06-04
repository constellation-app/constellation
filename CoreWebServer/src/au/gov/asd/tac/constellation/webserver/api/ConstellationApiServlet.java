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

import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.StatusDisplayer;

/**
 * A web service which makes up part of the Constellation REST API.
 * <p>
 * The use of a secret is enforced for this type of web service.
 * <p>
 * Any exceptions thrown while executing a servlet call are caught and converted
 * to an HttpServletResponse.sendError() response, as well as being logged at
 * Level.INFO (to avoid an error dialog box being displayed). Clients can see
 * the error by viewing the resulting HTML in the body of the response.
 * <p>
 * Note that servlet API 3.x is required for HttpServletResponse.getStatus()
 * (which is called by HttpServletResponse.sendError()). See CoreDependencies
 * ivy.xml for more info.
 *
 * @author cygnus_x-1
 */
public class ConstellationApiServlet extends ConstellationHttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ConstellationApiServlet.class.getName());

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            displayStatus(request.getMethod(), request.getServletPath(), request.getPathInfo());

            try {
                get(request, response);
            } catch (final RestServiceException ex) {
                response.reset();
                response.sendError(ex.getHttpCode(), ex.getMessage());
                LOGGER.log(Level.INFO, "in doGet", ex);
            } catch (final IOException | ServletException ex) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                LOGGER.log(Level.INFO, "in doGet", ex);
            }
        }
    }

    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            displayStatus(request.getMethod(), request.getServletPath(), request.getPathInfo());

            try {
                post(request, response);
            } catch (final RestServiceException ex) {
                response.reset();
                response.sendError(ex.getHttpCode(), ex.getMessage());
            } catch (final IOException | ServletException ex) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                LOGGER.log(Level.INFO, "in doPost", ex);
            }
        }
    }

    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }

    @Override
    protected final void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            displayStatus(request.getMethod(), request.getServletPath(), request.getPathInfo());

            try {
                put(request, response);
            } catch (final RestServiceException ex) {
                response.reset();
                response.sendError(ex.getHttpCode(), ex.getMessage());
            } catch (final IOException | ServletException ex) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                LOGGER.log(Level.INFO, "in doPut", ex);
            }
        }
    }

    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }

    /**
     * Display the incoming REST request to provide some confidence to the user
     * and debugging for the developer :-).
     *
     * @param method request.getMethod()
     * @param path request.getServletPath()
     * @param pathInfo request.getPathInfo()
     */
    private static void displayStatus(final String method, final String path, final String pathInfo) {
        final String msg = String.format("HTTP REST API: %s %s %s", method, path, pathInfo);
        StatusDisplayer.getDefault().setStatusText(msg);
    }
}

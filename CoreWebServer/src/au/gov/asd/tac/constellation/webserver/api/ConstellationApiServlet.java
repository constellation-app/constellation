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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.awt.StatusDisplayer;

/**
 * A web service which makes up part of the Constellation REST API. The use of a
 * secret is enforced for this type of web service.
 *
 * @author cygnus_x-1
 */
public class ConstellationApiServlet extends ConstellationHttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ConstellationApiServlet.class.getName());

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
            //
            final String msg = String.format("HTTP REST API: %s %s %s", request.getMethod(), request.getServletPath(), request.getPathInfo());
            StatusDisplayer.getDefault().setStatusText(msg);

            try {
                get(request, response);
            }
            catch(final Exception ex) {
                response.reset();

                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());

                    // Write the error to the response body as well.
                    // Swagger shows this but not the status phrase.
                    //
                    response.getWriter().print(ex.getMessage());
                }
                catch(final Throwable ee) {
                    // The call to response.SendError sort of works: it sets the status and status phrase, but then errors
                    // with "java.lang.NoSuchMethodError: javax.servlet.http.HttpServletResponse.getStatus()I".
                    // This indicates mismatched jars: see for example:
                    // https://stackoverflow.com/questions/44730096/javax-servlet-http-httpservletresponse-getstatus-not-found .
                    //
                    // When the jars are sorted out, the inner try and this catch can be removed (and the LOGGER).
                    //
                    LOGGER.log(Level.SEVERE, String.format("While calling response.sendError(): %s", ee.getMessage()));
                    LOGGER.log(Level.SEVERE, "This indicates mismatched servlet API jars, ");
                }

                response.flushBuffer();
            }
        }
    }

    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }

    @Override
    protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
            //
            final String msg = String.format("HTTP REST API: %s %s %s", request.getMethod(), request.getServletPath(), request.getPathInfo());
            StatusDisplayer.getDefault().setStatusText(msg);

            try {
                post(request, response);
            }
            catch(final Exception ex) {
                response.reset();

                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());

                    // Write the error to the response body as well.
                    // Swagger shows this but not the status phrase.
                    //
                    response.getWriter().print(ex.getMessage());
                }
                catch(final Throwable ee) {
                    // The call to response.SendError sort of works: it sets the status and status phrase, but then errors
                    // with "java.lang.NoSuchMethodError: javax.servlet.http.HttpServletResponse.getStatus()I".
                    // This indicates mismatched jars: see for example:
                    // https://stackoverflow.com/questions/44730096/javax-servlet-http-httpservletresponse-getstatus-not-found .
                    //
                    // When the jars are sorted out, the inner try and this catch can be removed (and the LOGGER).
                    //
//                    LOGGER.log(Level.SEVERE, "While calling response.sendError()", ee);
                    LOGGER.log(Level.SEVERE, String.format("While calling response.sendError(): %s", ee.getMessage()));
                    LOGGER.log(Level.SEVERE, "This indicates mismatched servlet API jars, ");
                }

                response.flushBuffer();
            }
        }
    }

    protected void post(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }

    @Override
    protected final void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
            //
            final String msg = String.format("HTTP REST API: %s %s %s", request.getMethod(), request.getServletPath(), request.getPathInfo());
            StatusDisplayer.getDefault().setStatusText(msg);

            try {
                put(request, response);
            }
            catch(final Exception ex) {
                response.reset();

                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());

                    // Write the error to the response body as well.
                    // Swagger shows this but not the status phrase.
                    //
                    response.getWriter().print(ex.getMessage());
                }
                catch(final Throwable ee) {
                    // The call to response.SendError sort of works: it sets the status and status phrase, but then errors
                    // with "java.lang.NoSuchMethodError: javax.servlet.http.HttpServletResponse.getStatus()I".
                    // This indicates mismatched jars: see for example:
                    // https://stackoverflow.com/questions/44730096/javax-servlet-http-httpservletresponse-getstatus-not-found .
                    //
                    // When the jars are sorted out, the inner try and this catch can be removed (and the LOGGER).
                    //
//                    LOGGER.log(Level.SEVERE, "While calling response.sendError()", ee);
                    LOGGER.log(Level.SEVERE, String.format("While calling response.sendError(): %s", ee.getMessage()));
                    LOGGER.log(Level.SEVERE, "This indicates mismatched servlet API jars, ");
                }

                response.flushBuffer();
            }
        }
    }

    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }
}

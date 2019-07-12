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

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        if (ConstellationHttpServlet.checkSecret(request, response)) {
            // Display the incoming REST request to provide some confidence to the user and debugging for the developer :-).
            //
            final String msg = String.format("HTTP REST API: %s %s %s", request.getMethod(), request.getServletPath(), request.getPathInfo());
            StatusDisplayer.getDefault().setStatusText(msg);

            get(request, response);
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

            post(request, response);
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

            put(request, response);
        }
    }

    protected void put(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // DO NOTHING
    }
}

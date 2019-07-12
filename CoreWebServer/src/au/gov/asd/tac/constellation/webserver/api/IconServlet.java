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
import au.gov.asd.tac.constellation.webserver.impl.IconImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service for interacting with icons in Constellation.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "IconAPI",
        description = "REST API for icons",
        urlPatterns = {"/v1/icon/*"})
public class IconServlet extends ConstellationApiServlet {

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/list":
                // A list of available icons.
                final String editableParam = request.getParameter("editable");
                final Boolean editable = editableParam == null ? null : Boolean.parseBoolean(editableParam);

                IconImpl.get_list(editable, response.getOutputStream());
                response.getOutputStream().close();

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            case "/get":
                // A particular icon.
                final String name = request.getParameter("name");
                if (name == null) {
                    throw new ServletException("No icon name specified.");
                }

                IconImpl.get_get(name, response.getOutputStream());

                response.setContentType("image/png");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }
    }
}

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
import au.gov.asd.tac.constellation.webserver.impl.TypeImpl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * A web service for interacting with types in Constellation.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "TypeAPI",
        description = "REST API for types",
        urlPatterns = {"/v1/type/*"})
public class TypeServlet extends ConstellationApiServlet {

    @Override
    protected void get(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        switch (request.getPathInfo()) {
            case "/describe":
                final String type = request.getParameter("type");
                if (type == null) {
                    throw new ServletException("No type specified.");
                }

                TypeImpl.get_describe(type, response.getOutputStream());
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                throw new ServletException(String.format("Unknown API path %s", request.getPathInfo()));
        }

        response.getOutputStream().close();
    }
}

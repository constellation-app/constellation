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
import java.io.InputStream;
import java.io.OutputStream;
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
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "SwaggerDoc",
        description = "Swagger documentation for the REST API",
        urlPatterns = {"/swagger-ui/*"})
public class SwaggerServlet extends ConstellationHttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String path = request.getPathInfo();
        final String fnam = "swagger" + path;

        try {
            final InputStream in = SwaggerServlet.class.getResourceAsStream(fnam);
            try (OutputStream out = response.getOutputStream()) {
                final byte[] buf = new byte[8192];
                while (true) {
                    final int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
            }
        } catch (final IOException ex) {
            throw new ServletException(ex);
        }
    }
}

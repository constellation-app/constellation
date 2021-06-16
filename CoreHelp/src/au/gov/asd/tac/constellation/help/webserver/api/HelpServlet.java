/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.help.webserver.api;

import au.gov.asd.tac.constellation.help.ConstellationHelpDisplayer;
import au.gov.asd.tac.constellation.webserver.WebServer.ConstellationHttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A servlet that serves help files.
 * <p>
 * The files live in a zip file. Each request opens the zip file and reads the
 * requested resource.
 *
 * @author algol
 */
@ServiceProvider(service = ConstellationHttpServlet.class)
@WebServlet(
        name = "HelpServer",
        description = "HTML help server",
        urlPatterns = {"/help/*"})
public class HelpServlet extends ConstellationHttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HelpServlet.class.getName());

    private static final Map<String, String> MIME_TYPES = Map.of(
            ".css", "text/css",
            ".gif", "image/gif",
            ".html", "text/html",
            ".jpg", "image/jpeg",
            ".js", "text/javascript",
            ".png", "image/png",
            ".txt", "text/plain",
            ".md", "text/html" // this allows an markdown file be converted to html on the fly
    );

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String requestPath = request.getPathInfo();
        LOGGER.log(Level.INFO, "GET {0}", requestPath);

        final int extIx = requestPath.lastIndexOf('.');
        final String ext = extIx > -1 ? requestPath.substring(extIx) : "";
        final String mimeType = MIME_TYPES.containsKey(ext) ? MIME_TYPES.get(ext) : "application/octet-stream";
        response.setContentType(mimeType);

        try {
            ConstellationHelpDisplayer.copy(requestPath, response.getOutputStream());
        } catch (final IOException ex) {
            throw new ServletException(ex);
        }
    }
}

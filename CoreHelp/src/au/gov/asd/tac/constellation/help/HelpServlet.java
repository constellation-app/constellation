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
package au.gov.asd.tac.constellation.help;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * A servlet that serves help files.
 * <p>
 * The files live in a zip file. Each request opens the zip file and reads the
 * requested resource.
 *
 * @author algol
 */
@ServiceProvider(service = HttpServlet.class)
@WebServlet(
        name = "HelpServer",
        description = "HTML help server",
        urlPatterns = {"/"})
public class HelpServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HelpServlet.class.getName());

    private static final Map<String, String> MIME_TYPES = Map.of(
            ".css", "text/css",
            ".gif", "image/gif",
            ".html", "text/html",
            ".jpg", "image/jpeg",
            ".js", "text/javascript",
            ".png", "image/png",
            ".txt", "text/plain",
            ".md", "text/html" // this allows a markdown file be converted to html on the fly
    );
    private static boolean wasRedirect = false;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String requestPath = request.getRequestURI();
        final String referer = request.getHeader("referer");

        LOGGER.log(Level.INFO, "GET {0}", requestPath);
        try {
            if (referer != null && !(referer.contains("toc.md") || requestPath.contains(".css") || requestPath.contains(".js") || requestPath.contains(".ico"))) {
                final String repeatedText = "src/au/gov/asd/tac";
                final int firstIndex = requestPath.indexOf(repeatedText);
                if (firstIndex != -1) {
                    final int secondIndex = requestPath.indexOf(repeatedText, firstIndex + repeatedText.length());
                    if (secondIndex != -1) {
                        final File file = new File(Generator.baseDirectory);
                        final URL fileUrl = file.toURI().toURL();
                        String requestfrontHalfRemoved = requestPath.replace(fileUrl.toString(), ""); // remove first bit
                        String refererfrontHalfRemoved = referer.replace(fileUrl.toString(), ""); // remove first bit
                        refererfrontHalfRemoved = refererfrontHalfRemoved.substring(0, refererfrontHalfRemoved.lastIndexOf("/")); // remove filename.md
                        refererfrontHalfRemoved = refererfrontHalfRemoved.substring(0, refererfrontHalfRemoved.lastIndexOf("/")); // remove up one level
                        refererfrontHalfRemoved = refererfrontHalfRemoved.replace("http://localhost:" + ConstellationHelpDisplayer.currentPort, "");

                        requestfrontHalfRemoved = requestfrontHalfRemoved.replaceFirst(refererfrontHalfRemoved, "");

                        String redirectURL = Generator.baseDirectory + requestfrontHalfRemoved;
                        final File file2 = new File(redirectURL);
                        final URL fileUrl2 = file2.toURI().toURL();
                        response.sendRedirect("/" + fileUrl2.toString());
                        wasRedirect = true;
                        return;
                    }
                }
            } else if (wasRedirect) {
                wasRedirect = false;
            }

        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.log(Level.SEVERE, "Redirect Failed!", ex);
        }
        final int extIx = requestPath.lastIndexOf('.');
        final String ext = extIx > -1 ? requestPath.substring(extIx) : "";
        final String mimeType = MIME_TYPES.containsKey(ext) ? MIME_TYPES.get(ext) : "application/octet-stream";
        response.setContentType(mimeType);

        try {
            ConstellationHelpDisplayer.copy(requestPath.substring(6), response.getOutputStream());
        } catch (final IOException ex) {
            throw new ServletException(ex);
        }
    }
}

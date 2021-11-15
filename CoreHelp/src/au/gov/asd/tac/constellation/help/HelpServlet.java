/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private static boolean redirect = false;

    protected boolean isRedirect() {
        return redirect;
    }

    protected static void setWasRedirect(final boolean wasRedirect) {
        HelpServlet.redirect = wasRedirect;
    }

    /**
     * Attempt to send the request to be displayed by the
     * ConstellationHelpDisplayer or redirect the request if the path given is
     * incorrect
     *
     * @param request
     * @param response
     * @throws ServletException
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        try {
            final String requestPath = request.getRequestURI();
            final String referer = request.getHeader("referer");

            LOGGER.log(Level.INFO, "GET {0}", requestPath);
            final URL fileUrl = redirectPath(requestPath, referer);

            if (fileUrl != null) {
                try {
                    response.sendRedirect("/" + fileUrl.toString());
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, ex, () -> "Failed to send redirect while navigating to {0}" + fileUrl.toString());
                }
            }

            final int extIx = requestPath.lastIndexOf('.');
            final String ext = extIx > -1 ? requestPath.substring(extIx) : "";
            final String mimeType = MIME_TYPES.containsKey(ext) ? MIME_TYPES.get(ext) : "application/octet-stream";
            response.setContentType(mimeType);
            final String path = URLDecoder.decode(requestPath, StandardCharsets.UTF_8.name());

            try {
                ConstellationHelpDisplayer.copy(stripLeadingPath(path), response.getOutputStream());
            } catch (final IOException ex) {
                throw new ServletException(ex);
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * If the requestPath is duplicated or not correct then update it after
     * removing the additional parts from the path
     *
     * @param requestPath
     * @param referer
     * @return new file path
     */
    protected static URL redirectPath(final String requestPath, final String referer) {
        try {
            if (referer != null && !(referer.contains("toc.md") || requestPath.contains(".css") || requestPath.contains(".js")
                    || requestPath.contains(".ico"))) {
                final String repeatedText = "src/au/gov/asd";
                final int firstIndex = requestPath.indexOf(repeatedText);
                if (firstIndex != -1) {
                    final int secondIndex = requestPath.indexOf(repeatedText, firstIndex + repeatedText.length());
                    if (secondIndex != -1) {
                        // If the request path is duplicated then change it to be the last half
                        final File file = new File(Generator.getBaseDirectory());
                        final URL fileUrl = file.toURI().toURL();
                        String requestfrontHalfRemoved = requestPath.replace(fileUrl.toString(), ""); // remove first bit
                        String refererfrontHalfRemoved = referer.replace(fileUrl.toString(), ""); // remove first bit
                        refererfrontHalfRemoved = refererfrontHalfRemoved.substring(0, refererfrontHalfRemoved.lastIndexOf("/")); // remove filename.md
                        refererfrontHalfRemoved = refererfrontHalfRemoved.substring(0, refererfrontHalfRemoved.lastIndexOf("/")); // remove up one level
                        refererfrontHalfRemoved = refererfrontHalfRemoved.replace("http://localhost:" + HelpWebServer.getPort(), "");
                        requestfrontHalfRemoved = requestfrontHalfRemoved.replaceFirst(refererfrontHalfRemoved, "");

                        final String redirectURL = Generator.getBaseDirectory() + requestfrontHalfRemoved;
                        final File file2 = new File(redirectURL);
                        final URL fileUrl2 = file2.toURI().toURL();
                        HelpServlet.redirect = true;
                        return fileUrl2;
                    }
                }
            } else if (HelpServlet.redirect) {
                HelpServlet.redirect = false;
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, String.format("Redirect Failed! Could not navigate to: %s", requestPath), ex);
        }
        return null;
    }

    /**
     * Strip off the /file:/C: /file: file: from the fullPath String.
     *
     * @param fullPath the fully qualified path coming in
     * @return the stripped path without /file:/home or drive letter within it.
     */
    protected static String stripLeadingPath(final String fullPath) {
        String modifiedPath = fullPath;
        final String replace1 = "\\/file:\\/[a-zA-Z]:";
        final String replace2 = "\\/file:";
        final String replace3 = "file:";
        modifiedPath = modifiedPath.replaceAll(replace1, "");
        modifiedPath = modifiedPath.replace(replace2, "");
        modifiedPath = modifiedPath.replace(replace3, "");

        return modifiedPath;
    }
}

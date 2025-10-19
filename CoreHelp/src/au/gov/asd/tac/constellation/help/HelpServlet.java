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
package au.gov.asd.tac.constellation.help;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.help.utilities.HelpMapper;
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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.SystemUtils;
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
    
    private static final Pattern FILE_AND_DRIVE = Pattern.compile("\\/file:\\/[a-zA-Z]:");

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

    protected static boolean isRedirect() {
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
            final String requestPath = request.getRequestURI().replace("%20", " ");
            final String referer = request.getHeader("referer");

            LOGGER.log(Level.INFO, "GET {0}", requestPath);
            final URL fileUrl = redirectPath(requestPath, referer);

            if (fileUrl != null) {
                tryResponseRedirect(fileUrl, response);
            }

            final int extIx = requestPath.lastIndexOf('.');
            final String ext = extIx > -1 ? requestPath.substring(extIx) : "";
            final String mimeType = MIME_TYPES.containsKey(ext) ? MIME_TYPES.get(ext) : "application/octet-stream";
            response.setContentType(mimeType);
            final String path = URLDecoder.decode(requestPath, StandardCharsets.UTF_8.name());

            tryCopy(path, response);

        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, "Character encoding not supported", ex);
        }
    }

    /**
     * Try to send a redirect to the response with the given fileUrl
     *
     * @param fileUrl
     * @param response
     */
    private void tryResponseRedirect(final URL fileUrl, final HttpServletResponse response) {
        try {
            // Note: base dir may have spaces, so replace
            response.sendRedirect("/" + fileUrl.toString().replace("%20", " "));
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "Failed to send redirect while navigating to {0}" + fileUrl.toString());
        }
    }

    /**
     * Try to display the correct page
     *
     * @param path
     * @param response
     * @throws ServletException
     */
    private void tryCopy(final String path, final HttpServletResponse response) throws ServletException {
        try {
            ConstellationHelpDisplayer.copy(stripLeadingPath(path), response.getOutputStream());
        } catch (final IOException ex) {
            throw new ServletException(ex);
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
            if (referer != null && !referer.contains("toc.md") && !requestPath.contains(".css") && !requestPath.contains(".js")
                    && !requestPath.contains(".ico")) {
                if (requestPath.contains(".png")) {
                    // get image referred by page itself
                    final String extText = "/ext/";
                    final int firstIndex = requestPath.indexOf(extText);
                    if (firstIndex != -1) {
                        final int secondIndex = requestPath.indexOf(extText, firstIndex + extText.length());
                        if (secondIndex != -1) {
                            // cut-off the duplicate section in the request path
                            final String duplicateSubstring = requestPath.substring(firstIndex, secondIndex);
                            final String fileString = new StringBuilder("/file:").append(SystemUtils.IS_OS_WINDOWS ? "/" : "").toString();
                            final String newPath = requestPath.replaceFirst(duplicateSubstring, "").replace(fileString, "");
                            final File imageFile = new File(newPath);
                            final URL imageUrl = imageFile.toURI().toURL();
                            HelpServlet.redirect = true;
                            return imageUrl;
                        }
                    }
                } else if (requestPath.contains(".md")) {
                    // find correct help page
                    final String extText = "/ext/";
                    final int index = requestPath.lastIndexOf(extText);
                    if (index != -1) {
                        final String pathSubstring = requestPath.substring(index + extText.length()).replace("/", File.separator);
                        final Collection<String> helpAddresses = HelpMapper.getMappings().values();

                        for (final String helpAddress : helpAddresses) {
                            // if helpAddress contains the substring, this should be the correct path
                            // due to the way help page paths are constructed, the assumption is the collection's values are also unique
                            if (helpAddress.contains(pathSubstring)) {
                                String filePath = Generator.getBaseDirectory() + File.separator + helpAddress;
                                // if helpAddress contains any backwards directory changes then these should be normalised first
                                // before comparing with already-normalised requestPath
                                if (helpAddress.contains("..")) {
                                    filePath = Paths.get(filePath).normalize().toString();                                  
                                }
                                
                                final File pageFile = new File(filePath);
                                final URL fileUrl = pageFile.toURI().toURL();
                                // if these match then no redirect is required as the resulting file path is the same
                                // check substring(1) as request path has leading /
                                // Note: base dir may have spaces, so replace and check
                                if (fileUrl.toString().replace("%20", " ").equals(requestPath.substring(1))) {
                                    return null;
                                }
                                
                                HelpServlet.redirect = true;
                                return fileUrl;
                            }
                        }
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
        return FILE_AND_DRIVE.matcher(fullPath).replaceAll("")
                .replace("/file:", "")
                .replace("file:", "");
    }
}

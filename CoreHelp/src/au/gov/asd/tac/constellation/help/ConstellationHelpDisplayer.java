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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.webserver.WebServer;
import com.github.rjeschke.txtmark.Processor;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = HelpCtx.Displayer.class, position = 9999)
public class ConstellationHelpDisplayer implements HelpCtx.Displayer {

    private static final Logger LOGGER = Logger.getLogger(ConstellationHelpDisplayer.class.getName());

    // This system property controls where the help is displayed from.
    //
    // * If it is undefined, help is read from getResourceAsStream().
    // * If it is defined as a (zip) file, help is read from that zip file.
    // * If it is defined as a web URL, help is read from that URL.
    //
    // Define it in this module's project.properties file or another
    // appropriate place as (for example):
    //
    // run.args.extra="-J-Dconstellation.help=D:/tmp/help.zip"
    // run.args.extra="-J-Dconstellation.help=http://localhost:8000"
    // run.args.extra="-J-Dconstellation.help=https://example.com"
    //
    // Set the value to the following github url to have the pages served from readthedocs.io
    //
    // run.args.extra="-J-Dconstellation.help=https://github.com/constellation-app/constellation/raw/master/docs"
    //
    private static final String CONSTELLATION_HELP = "constellation.help";

    // The help zip must contain a text file mapping helpIds to page paths.
    //
    private static final String HELP_MAP = "help_map.txt";

    // The name of the internal help.zip resource.
    //
    private static final String HELP_ZIP = "help.zip";

    // Maximum size of buffer for reading help files.
    //
    private static final int BUFSIZ = 10 * 1024 * 1024;

    private static String helpSource;

    // The mapping of helpIds to page paths.
    //
    private static Map<String, String> helpMap;

    // A special case to use the readthedocs.io website if the HELP_MAP file is
    // in the official GitHub repository
    //
    private static final String OFFICIAL_GITHUB_REPOSITORY = "https://github.com/constellation-app/constellation";
    private static final String READ_THE_DOCS = "https://constellation.readthedocs.io/en/latest/%s";

    /**
     * Read an entry from the zip file HELP_ZIP stored as an internal resource.
     * <p>
     * Because we only get access via getResourceAsStream(), we have to iterate
     * through the entries to get the right one.
     *
     * @param filepath The zip entry to get.
     * @param out The OutputStream to write the entry's contents to.
     *
     * @throws IOException
     */
    private static void copyFromZipResource(final String filepath, final OutputStream out) throws IOException {
        final InputStream in = ConstellationHelpDisplayer.class.getResourceAsStream(HELP_ZIP);
        if (in != null) {
            final ZipInputStream zin = new ZipInputStream(in);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                final String name = entry.getName();
                if (name.equals(filepath)) {
                    final byte[] buf = zin.readNBytes(BUFSIZ);
                    zin.closeEntry();
                    zin.close();

                    out.write(buf);

                    return;
                }
            }

            LOGGER.log(Level.WARNING, "Could not find entry {0} in resource {1}", new Object[]{HELP_MAP, HELP_ZIP});
        } else {
            throw new IOException(String.format("Help resource %s not found " + filepath, HELP_ZIP));
        }
    }

    /**
     * Read an entry from a zip file.
     * <p>
     * The zip file is stored on the filesystem, so maybe this is faster than
     * copyFromZipResource()?
     *
     * @param filepath
     * @param out
     * @throws IOException
     */
    private static void copyFromZipFile(final String zipFile, final String filepath, final OutputStream out) throws IOException {
        // TODO: need to rework this so that the local version includes a custom header and footer
        if (filepath.endsWith(".md")) {
            try ( ZipFile zip = new ZipFile(zipFile)) {
                final Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (filepath.equals(entry.getName())) {
                        final InputStream input = zip.getInputStream(entry);
                        final String html = Processor.process(input);
                        out.write(html.getBytes());
                    }
                }
            }
        } else {
            // TODO: perhaps move this to another method
            final Path p = Paths.get(filepath);
            try {
                try (final FileSystem fs = FileSystems.newFileSystem(p, null)) {
                    final Path path = fs.getPath(filepath);
                    Files.copy(path, out);
                }
            } catch (final FileSystemNotFoundException ex) {
                throw new IOException(String.format("Help resource %s not found " + filepath + zipFile, HELP_ZIP));
            }
        }
    }

    /**
     * Read a file from a web server.
     * <p>
     * We need this to get help_map.txt. All other files will be retrieved by
     * the user's browser.
     *
     * @param url The URL of the file to get.
     *
     * @return The lines of the file as a List&lt;String&gt;.
     *
     * @throws IOException
     */
    private static List<String> getHttpFile(final String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = url.startsWith("https")
                    ? HttpsConnection.withInsecureUrl(url).withReadTimeout(10 * 1000).get() // not checking for a user certiticate
                    : HttpsConnection.withInsecureUrl(url).withReadTimeout(10 * 1000).insecureGet();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(HttpsUtilities.getInputStream(connection), StandardCharsets.UTF_8)
                )) {
                    return reader.lines().collect(Collectors.toList());
                }
            } else {
                throw new IOException(String.format("HTTP %s response code: %d", url, connection.getResponseCode()));
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static List<String> getZipFile(final String zipFile, final String filepath) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copyFromZipFile(zipFile, filepath, buf);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf.toByteArray()), StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.toList());
    }

    private static List<String> getResourceZipFile(final String zipResource) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copyFromZipResource(zipResource, buf);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf.toByteArray()), StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.toList());
    }

    /**
     *
     * @return @throws IOException
     */
    private static List<String> initHelp() throws IOException {
        helpSource = System.getProperty(CONSTELLATION_HELP);
        LOGGER.log(Level.INFO, "Help source: {0}", HELP_ZIP);

        if (helpSource == null) {
            LOGGER.log(Level.INFO, "help_map file at zip resource {0}, {1}", new Object[]{HELP_ZIP, HELP_MAP});
            return getResourceZipFile(HELP_MAP);
        } else if (helpSource.startsWith("http")) {
            final String url = String.format("%s/%s", helpSource, HELP_MAP);
            LOGGER.log(Level.INFO, "help_map file at {0}", url);
            return getHttpFile(url);
        } else {
            LOGGER.log(Level.INFO, "help_map file at zip {0}, {1}", new Object[]{helpSource, HELP_MAP});
            return getZipFile(helpSource, HELP_MAP);
        }
    }

    /**
     * Return the help map mapping helpIds to documentation page paths.
     * <p>
     * This can be in any one of three places:
     * <ul>
     * <li>in a zip file in modules/resources/ext</li>
     * <li>in a named external zip file</li>
     * <li>at a web location</li>
     * </ul>
     *
     * @return The helpId to documentation path mapping.
     */
    private static synchronized Map<String, String> getHelpMap() {
        if (helpMap == null) {
            try {
                final List<String> lines = initHelp();
                helpMap = new HashMap<>();
                lines.forEach(line -> {
                    final int ix = line.indexOf(',');
                    final String helpId = line.substring(0, ix).strip();
                    if (!helpId.isEmpty() && !helpId.startsWith("#")) {
                        final String helpPath = line.substring(ix + 1);
                        helpMap.put(helpId, helpPath);
                    }
                });
            } catch (final IOException ex) {
                LOGGER.log(Level.INFO, "Fetching help map:", ex);

                // If we couldn't read the file the first time,
                // it won't magically work the next time, so stop trying.
                //
                helpMap = Map.of();
            }
        }

        return helpMap;
    }

    public static void copy(final String filepath, final OutputStream out) throws IOException {
        final String p = filepath.startsWith("/") ? filepath.substring(1) : filepath;
        if (helpSource == null) {
            copyFromZipResource(p, out);
        } else {
            copyFromZipFile(helpSource, p, out);
        }
    }

    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String helpId = helpCtx.getHelpID();
        LOGGER.log(Level.INFO, "display {0} from {1}", new Object[]{helpId, helpSource});

        // Given the helpId, get the corresponding help page path.
        // If it doesn't exist (maybe because someone forgot to put the helpId
        // in their .rst file), go to the root page.
        //
        List<String> helpPages = getHelpPages();
        //  final Map<String, String> helpMapCopy = getHelpMap();
        if (!helpPages.isEmpty()) {
            final int index = helpPages.indexOf(helpId);
            final String part = helpPages.contains(helpId) ? helpPages.get(index) : "analytic-view.md";

            // Send the user's browser to the correct page, depending on the help source.
            //
            String url;
            if (helpSource == null || !helpSource.startsWith("http")) {
                // The help source is an internal zipped resource or an actual zip file,
                // therefore we need to invoke the internal web server's help servlet,
                // so insert /help into the path. The servlet will call copy() to get
                // the file from the resource/file.
                //
                final int port = WebServer.start();
                url = String.format("http://localhost:8000/help/html/%s", part);
            } else if (helpSource.startsWith(OFFICIAL_GITHUB_REPOSITORY)) {
                // If the helpSource points to github contellation-app/constellation then
                // we are going to use read the docs to serve the help pages
                url = String.format(READ_THE_DOCS, part);
            } else {
                // The help source is an external web server, so we just
                // assemble the URL and disavow all knowledge.
                //
                url = String.format("%s/html/%s", helpSource, part);
            }

            LOGGER.log(Level.INFO, "help url {0}", url);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // Run in a different thread, not the JavaFX thread
                new Thread(() -> {
                    Thread.currentThread().setName("Browse Help");
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (URISyntaxException | IOException ex) {
                        LOGGER.log(Level.SEVERE, "Tried to browse a url.", ex);
                        final String msg = "Unable to browse to that location.";
                        NotificationDisplayer.getDefault().notify("Help displayer",
                                UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.RED.getJavaColor()),
                                msg,
                                null
                        );
                    }
                }).start();

                return true;
            }
        } else {
            final String msg = "Help not available; see logs for the reason.";
            NotificationDisplayer.getDefault().notify("Help displayer",
                    UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.RED.getJavaColor()),
                    msg,
                    null
            );
        }

        return false;
    }

    /**
     * Use a lookup to get all of the help pages
     *
     * @return help pages
     */
    public List<String> getHelpPages() {
        final HelpPageProvider helpFiles = Lookup.getDefault().lookup(HelpPageProvider.class);

        List<String> pages = helpFiles.getHelpPages();
        return pages;
    }
}

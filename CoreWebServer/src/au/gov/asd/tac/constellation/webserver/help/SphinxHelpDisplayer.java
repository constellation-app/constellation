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
package au.gov.asd.tac.constellation.webserver.help;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.https.HttpsConnection;
import au.gov.asd.tac.constellation.utilities.https.HttpsUtilities;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.webserver.WebServer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service=HelpCtx.Displayer.class, position=9999)
public class SphinxHelpDisplayer implements HelpCtx.Displayer {
    private static final Logger LOGGER = Logger.getLogger(SphinxHelpDisplayer.class.getName());

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
    private static Map<String, String> helpMap_;

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
        final InputStream in = SphinxHelpDisplayer.class.getResourceAsStream(HELP_ZIP);
        if(in!=null) {
            final ZipInputStream zin = new ZipInputStream(in);
            ZipEntry entry;
            while((entry = zin.getNextEntry())!=null) {
                final String name = entry.getName();
                if(name.equals(filepath)) {
                    final byte[] buf = zin.readNBytes(BUFSIZ);
                    zin.closeEntry();
                    zin.close();

                    out.write(buf);

                    return;
                }
            }

            LOGGER.warning(String.format("Could not find entry '%s' in resource %s", HELP_MAP, HELP_ZIP));
        } else {
            throw new IOException(String.format("Help resource %s not found", HELP_ZIP));
        }
    }

    /**
     * Read an entry from a zip file.
     * <p>
     * The zip file is stored on the filesystem, so maybe this is faster
     * than copyFromZipResource()?
     *
     * @param filepath
     * @param out
     * @throws IOException
     */
    private static void copyFromZipFile(final String zipFile, final String filepath, final OutputStream out) throws IOException {
        final Path p = Paths.get(zipFile);
        try {
            final FileSystem fs = FileSystems.newFileSystem(p, null);

            final Path path = fs.getPath(filepath);
            Files.copy(path, out);
        } catch(final FileSystemNotFoundException ex) {
            final String msg = String.format("Zip file %s not found", zipFile);
            throw new IOException(ex);
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
                ? HttpsConnection.withUrl(url).withReadTimeout(10 * 1000).get()
                : HttpsConnection.withInsecureUrl(url).withReadTimeout(10 * 1000).insecureGet();
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(HttpsUtilities.getInputStream(connection), StandardCharsets.UTF_8)
                );
                final List<String> lines = reader.lines().collect(Collectors.toList());

                return lines;
            } else {
                throw new IOException(String.format("HTTP %s response code: %d", url, connection.getResponseCode()));
            }
        } finally {
            if(connection!=null) {
                connection.disconnect();
            }
        }
    }

    private static List<String> getZipFile(final String zipFile, final String filepath) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copyFromZipFile(zipFile, filepath, buf);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf.toByteArray()), StandardCharsets.UTF_8));
        final List<String> lines = reader.lines().collect(Collectors.toList());

        return lines;
    }

    private static List<String> getResourceZipFile(final String zipResource) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        copyFromZipResource(zipResource, buf);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf.toByteArray()), StandardCharsets.UTF_8));
        final List<String> lines = reader.lines().collect(Collectors.toList());

        return lines;
    }

    private static List<String> initHelp() throws IOException {
        helpSource = System.getProperty(CONSTELLATION_HELP);
        LOGGER.info(String.format("Help source: %s", helpSource));

        if(helpSource==null) {
            LOGGER.info(String.format("help_map file at zip resource %s, %s", HELP_ZIP, HELP_MAP));
            return getResourceZipFile(HELP_MAP);
        } else if(helpSource.startsWith("http")) {
            final String url = String.format("%s/%s", helpSource, HELP_MAP);
            LOGGER.info(String.format("help_map file at %s", url));
            return getHttpFile(url);
        } else {
            LOGGER.info(String.format("help_map file at zip %s, %s", helpSource, HELP_MAP));
            return getZipFile(helpSource, HELP_MAP);
        }
    }

    /**
     * Return the help map mapping helpIds to documentation page paths.
     * <p>
     * This can be in any one of three places:
     * * in a zip file in modules/resources/ext
     * * in a named external zip file
     * * at a web location
     *
     * @return The helpId to documentation path mapping.
     */
    private static synchronized Map<String, String> getHelpMap() {
        if(helpMap_==null) {
            try {
                final List<String> lines = initHelp();
                helpMap_ = new HashMap<>();
                lines.forEach(line -> {
                    final int ix = line.indexOf(',');
                    final String helpId = line.substring(0, ix).strip();
                    if(!helpId.isEmpty() && !helpId.startsWith("#")) {
                        final String helpPath = line.substring(ix+1);
                        helpMap_.put(helpId, helpPath);
                    }
                });
            } catch(final IOException ex) {
                LOGGER.log(Level.INFO, "Fetching help map:", ex);

                // If we couldn't read the file the first time,
                // it won't magically work the next time, so stop trying.
                //
                helpMap_ = Map.of();
            }
        }

        return helpMap_;
    }

    public static void copy(final String filepath, final OutputStream out) throws IOException {
        final String p = filepath.startsWith("/") ? filepath.substring(1) : filepath;
        if(helpSource==null) {
            copyFromZipResource(p, out);
        } else {
            copyFromZipFile(helpSource, p, out);
        }
    }

    @Override
    public boolean display(final HelpCtx helpCtx) {
        final String helpId = helpCtx.getHelpID();
        LOGGER.info(String.format("display '%s' from %s", helpId, helpSource));

        // Given the helpId, get the corresponding help page path.
        // If it doesn't exist (maybe because someone forgot to put the helpId
        // in their .rst file), go to the root page.
        //
        final Map<String, String> helpMap = getHelpMap();
        if(!helpMap.isEmpty()) {
            final String part = helpMap.containsKey(helpId) ? helpMap.get(helpId) : "index.html";

            // Send the user's browser to the correct page, depending on the help source.
            //
            String url;
            if(helpSource==null || !helpSource.startsWith("http")) {
                // The help source is an internal zipped resource or an actual zip file,
                // therefore we need to invoke the internal web server's help servlet,
                // so insert /help into the path. The servlet will call copy() to get
                // the file from the resource/file.
                //
                final int port = WebServer.start();
                url = String.format("http://localhost:%d/help/html/%s", port, part);
            } else {
                // The help source is an external web server, so we just
                // assemble the URL and disavow all knowledge.
                //
                url = String.format("%s/html/%s", helpSource, part);
            }

            LOGGER.info(String.format("help url %s", url));
            try {
                // Technically we should do the desktop checking first, but if it
                // isn't, we need to show the user an error anyway.
                //
    //            if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));

                return true;
    //            }
            } catch(final URISyntaxException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            final String msg = "Help not available; see logs for reason.";
            NotificationDisplayer.getDefault().notify("Help displayer",
                    UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.RED.getJavaColor()),
                    msg,
                    null
            );
        }

        return false;
    }
}

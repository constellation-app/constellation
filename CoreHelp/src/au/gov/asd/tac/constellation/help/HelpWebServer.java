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

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Delphinus8821
 */
public class HelpWebServer {

    private static final Logger LOGGER = Logger.getLogger(HelpWebServer.class.getName());

    private static boolean running = false;
    private static int port = 0;
    private static final String WEB_SERVER_THREAD_NAME = "Web Server";
    private static final String REST_FILE = "rest.json";

    protected static final String CONSTELLATION_CLIENT = "constellation_client.py";
    private static final String IPYTHON = ".ipython";
    private static final String RESOURCES = "resources/";

    public static boolean isRunning() {
        return running;
    }

    public static synchronized int start() {
        if (!running) {
            try {
                final InetAddress loopback = InetAddress.getLoopbackAddress();
                port = 1517;

                // Put the session secret and port number in a JSON file in the .CONSTELLATION directory.
                // Make sure the file is owner read/write.
                final String userDir = System.getProperty("user.dir");
                final File restFile = new File(userDir, REST_FILE);
                if (restFile.exists()) {
                    final boolean restFileIsDeleted = restFile.delete();
                    if (!restFileIsDeleted) {
                        //TODO: Handle case where file not successfully deleted
                    }
                }
                // On Posix, we can use stricter file permissions.
                // On Windows, we just create the new file.
                final String os = System.getProperty("os.name");
                if (!os.startsWith("Windows")) {
                    final Path restPath = restFile.toPath();
                    final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                    Files.createFile(restPath, PosixFilePermissions.asFileAttribute(perms));
                }
                // Download the Python REST client if enabled.
               // final boolean pythonRestClientDownload = prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);
                //   if (pythonRestClientDownload) {
                //       downloadPythonClient();
                //   }
                // Build the server.
                //
                final Server server = new Server(new InetSocketAddress(loopback, port));
                final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);

                // Gather the servlets and add them to the server.
                //
                Lookup.getDefault().lookupAll(HttpServlet.class).forEach(servlet -> {
                    if (servlet.getClass().isAnnotationPresent(WebServlet.class)) {
//                        for (String urlPattern : servlet.getClass().getAnnotation(WebServlet.class).value()) {
//                            Logger.getGlobal().info(String.format("value %s %s", servlet, urlPattern));
//                            context.addServlet(new ServletHolder(servlet), urlPattern);
//                        }
                        for (String urlPattern : servlet.getClass().getAnnotation(WebServlet.class).urlPatterns()) {
                            Logger.getGlobal().info(String.format("urlpattern %s %s", servlet, urlPattern));
                            context.addServlet(new ServletHolder(servlet), urlPattern);
                        }
                    }
                });

                // Make our own handler so we can log requests with the CONSTELLATION logs.
                //
                final RequestLog requestLog = (request, response) -> {
                    final String log = String.format("Request at %s from %s %s, status %d", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), request.getRemoteAddr(), request.getRequestURI(), response.getStatus());
                    LOGGER.info(log);
                };
                server.setRequestLog(requestLog);

                LOGGER.info(String.format("Starting Jetty version %s on%s:%d...", Server.getVersion(), loopback, port));
                server.start();

                // Wait for the server to stop (if it ever does).
                //
                final Thread webserver = new Thread(() -> {
                    try {
                        server.join();
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    } finally {
                        // Play nice and clean up (if Netbeans lets us).
                        //  final boolean restFileIsDeleted = restFile.delete();
                        // if (!restFileIsDeleted) {
                        //TODO: Handle case where file not successfully deleted
                        // }
                    }
                });
                webserver.setName(WEB_SERVER_THREAD_NAME);
                webserver.start();

                running = true;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        return port;
    }

    static File getScriptDir(final boolean mkdir) {
        final File homeDir = new File(System.getProperty("user.home"));
        final File ipython = new File(homeDir, IPYTHON);

        if (!ipython.exists() && mkdir) {
            ipython.mkdir();
        }

        return ipython;
    }

    /**
     * Download the Python REST API client to the user's ~/.ipython directory.
     * <p>
     * The download is done only if the script doesn't exist, or the existing script needs updating.
     * <p>
     * This is in the path for Jupyter notebooks, but not for standard Python.
     */
    public static void downloadPythonClient() {
        final File ipython = getScriptDir(true);
        final File download = new File(ipython, CONSTELLATION_CLIENT);

        final boolean doDownload = !download.exists() || !equalScripts(download);

        if (doDownload) {
            boolean complete = false;
            try (final InputStream in = HelpWebServer.class.getResourceAsStream(RESOURCES + CONSTELLATION_CLIENT); final FileOutputStream out = new FileOutputStream(download)) {
                final byte[] buf = new byte[64 * 1024];
                while (true) {
                    final int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }

                    out.write(buf, 0, len);
                }

                complete = true;
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (complete) {
                final String msg = String.format("'%s' downloaded to %s", CONSTELLATION_CLIENT, ipython);
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        }
    }

    /**
     * Get the SHA-256 digest of an InputStream.
     *
     * @param in An InputStream.
     *
     * @return A SHA256 digest.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getDigest(final InputStream in) throws IOException, NoSuchAlgorithmException {
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final byte[] buf = new byte[64 * 1024];
        while (true) {
            final int len = in.read(buf);
            if (len == -1) {
                break;
            }

            sha256.update(buf, 0, len);
        }

        return sha256.digest();
    }

    /**
     * Compare the on-disk script file with our resource.
     *
     * @param scriptFile The script file.
     *
     * @return True if both exist and are (pseudo-)equal, False otherwise.
     */
    static boolean equalScripts(final File scriptFile) {
        try (final FileInputStream in1 = new FileInputStream(scriptFile)) {
            try (final InputStream in2 = HelpWebServer.class.getResourceAsStream(RESOURCES + CONSTELLATION_CLIENT)) {
                final byte[] dig1 = getDigest(in1);
                final byte[] dig2 = getDigest(in2);

                return MessageDigest.isEqual(dig1, dig2);
            }
        } catch (final FileNotFoundException | NoSuchAlgorithmException ex) {
            return false;
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Equal scripts", ex);
            return false;
        }
    }
}

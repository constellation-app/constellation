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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * A Web Server.
 *
 * @author capella
 */
public class WebServer {

    /**
     * Marker interface for lookups.
     */
    public static class ConstellationHttpServlet extends HttpServlet {

        /**
         * The HTTP header that contains the REST secret.
         * <p>
         * Note: RFC6648 has deprecated the use of "X-" because changing it when
         * it becomes a standard is problematic. Since this header is highly
         * unlikely to ever to be registered, using "X-" is fine.
         */
        public static final String SECRET_HEADER = "X-CONSTELLATION-SECRET";

        private static final String SECRET = UUID.randomUUID().toString();

        /**
         * Check that a request contains the correct secret.
         * <p>
         * If the request does not contain the correct secret, send an
         * SC_AUTHORIZED error to the client; the caller is expected to honor
         * the return value and return immediately without doing any work if the
         * return value is false.
         *
         * @param request The client request.
         * @param response The response.
         *
         * @return True if the correct secret was found, false otherwise.
         *
         * @throws javax.servlet.ServletException
         * @throws java.io.IOException
         */
        public static boolean checkSecret(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            final String header = request.getHeader(SECRET_HEADER);
            final boolean ok = header != null && SECRET.equals(header);
            if (!ok) {
                final String msg = String.format("REST API secret %s not provided.", SECRET_HEADER);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);

                final String msg2 = String.format("<html>REST API secret %s not provided.<br>Please download the external scripting Python client again.</html>",
                        SECRET_HEADER);
                NotificationDisplayer.getDefault().notify("REST API server",
                        UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                        msg2,
                        null
                );
            }

            return ok;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    private static final String WEB_SERVER_THREAD_NAME = "Web Server";
    private static final String REST_FILE = "rest.json";
    private static boolean running = false;
    private static int port = 0;

    static final String CONSTELLATION_CLIENT = "constellation_client.py";
    static final String RESOURCES = "resources/";
    private static final String IPYTHON = ".ipython";

    public static synchronized int start() {
        if (!running) {
            try {
                final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

                final InetAddress loopback = InetAddress.getLoopbackAddress();
                port = prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT);
                final Server server = new Server(new InetSocketAddress(loopback, port));
                final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);

                for (ConstellationHttpServlet servlet : Lookup.getDefault().lookupAll(ConstellationHttpServlet.class)) {
                    if (servlet.getClass().isAnnotationPresent(WebServlet.class)) {
                        for (String urlPattern : servlet.getClass().getAnnotation(WebServlet.class).value()) {
                            context.addServlet(new ServletHolder(servlet), urlPattern);
                        }
                        for (String urlPattern : servlet.getClass().getAnnotation(WebServlet.class).urlPatterns()) {
                            context.addServlet(new ServletHolder(servlet), urlPattern);
                        }
                    }
                }
                server.start();

                // Put the session secret and port number in a JSON file in the .CONSTELLATION directory.
                // Make sure the file is owner read/write.
                final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
                final File restFile = new File(userDir, REST_FILE);
                if (restFile.exists()) {
                    restFile.delete();
                }

                // On Posix, we can use stricter file permissions.
                // On Windows, we just create the new file.
                final String os = System.getProperty("os.name");
                if (!os.startsWith("Windows")) {
                    final Path restPath = restFile.toPath();
                    final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                    Files.createFile(restPath, PosixFilePermissions.asFileAttribute(perms));
                }

                // Now write the file contents.
                try (final PrintWriter pw = new PrintWriter(restFile)) {
                    // Couldn't be bothered starting up a JSON writer for two simple values.
                    pw.printf("{\"%s\":\"%s\", \"port\":%d}\n", ConstellationHttpServlet.SECRET_HEADER, ConstellationHttpServlet.SECRET, port);
                }

                // Download the Python REST client if enabled.
                final boolean pythonRestClientDownload = prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);
                if (pythonRestClientDownload) {
                    downloadPythonClient();
                }

                final Thread webserver = new Thread(() -> {
                    try {
                        server.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        // Play nice and clean up (if Netbeans lets us).
                        restFile.delete();
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
     * The download is done only if the script doesn't exist, or the existing
     * script needs updating.
     * <p>
     * This is in the path for Jupyter notebooks, but not for standard Python.
     */
    public static void downloadPythonClient() {
        final File ipython = getScriptDir(true);
        final File download = new File(ipython, CONSTELLATION_CLIENT);

        final boolean doDownload = !download.exists() || !equalScripts(download);

        if (doDownload) {
            boolean complete = false;
            try (
                    final InputStream in = WebServer.class.getResourceAsStream(RESOURCES + CONSTELLATION_CLIENT);
                    final FileOutputStream out = new FileOutputStream(download)) {
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
     * Get the MD5 digest of an InputStream.
     * <p>
     * MD5 isn't particularly secure, but we don't really care. This is a
     * pseudo-equality check; if the user wants to break it, that's their
     * problem.
     *
     * @param in An InputStream.
     *
     * @return A digest.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getDigest(final InputStream in) throws IOException, NoSuchAlgorithmException {
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final byte[] buf = new byte[64 * 1024];
        while (true) {
            final int len = in.read(buf);
            if (len == -1) {
                break;
            }

            md5.update(buf, 0, len);
        }

        return md5.digest();
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
            try (final InputStream in2 = WebServer.class.getResourceAsStream(RESOURCES + CONSTELLATION_CLIENT)) {
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

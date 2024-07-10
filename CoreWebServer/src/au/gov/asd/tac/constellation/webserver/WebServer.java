/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import static jnr.constants.PlatformConstants.OS;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
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
         * Note: RFC6648 has deprecated the use of "X-" because changing it when it becomes a standard is problematic.
         * Since this header is highly unlikely to ever to be registered, using "X-" is fine.
         */
        public static final String SECRET_HEADER = "X-CONSTELLATION-SECRET";

        private static final String SECRET = UUID.randomUUID().toString();

        /**
         * Check that a request contains the correct secret.
         * <p>
         * If the request does not contain the correct secret, send an SC_AUTHORIZED error to the client; the caller is
         * expected to honor the return value and return immediately without doing any work if the return value is
         * false.
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
                final String msg = String.format("REST API secret %s is invalid.", SECRET_HEADER);
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

    protected static final String CONSTELLATION_CLIENT = "constellation_client.py";
    private static final String IPYTHON = ".ipython";
    private static final String SEP = File.separator;
    private static final String SCRIPT_SOURCE = Generator.getBaseDirectory() + "ext" + SEP + "package" + SEP + "src" + SEP + "constellation_client" + SEP;

    private static final String PACKAGE_SOURCE = Generator.getBaseDirectory() + "ext" + SEP + "package" + SEP + "package_dist";
    private static final String[] PACKAGE_INSTALL = {"pip", "install", "--upgrade", "constellation_client", "--no-index", "--find-links", "file:" + PACKAGE_SOURCE};
    private static final String[] WINDOWS_COMMAND = {"cmd.exe", "/C"};
    private static final String[] UNIX_COMMAND = {"/bin/sh", "-c"}; // Untested, on linux

    private static final int INSTALL_SUCCESS = 0; // Untested on linux

    public static boolean isRunning() {
        return running;
    }

    public static synchronized int start() {
        if (!running) {
            try {
                final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

                final InetAddress loopback = InetAddress.getLoopbackAddress();
                port = prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT);

                // Put the session secret and port number in a JSON file in the .CONSTELLATION directory.
                // Make sure the file is owner read/write.
                final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
                final File restFile = new File(userDir, REST_FILE);
                cleanupRest(restFile, userDir);

                // Also put rest file in the ipython directory
                final File restFileIPython = new File(getScriptDir(true), REST_FILE);
                cleanupRest(restFileIPython, getScriptDir(true).toString());

                // On Posix, we can use stricter file permissions.
                // On Windows, we just create the new file.
                final String os = System.getProperty("os.name");
                if (!os.startsWith("Windows")) {
                    final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                    Files.createFile(restFile.toPath(), PosixFilePermissions.asFileAttribute(perms));
                    Files.createFile(restFileIPython.toPath(), PosixFilePermissions.asFileAttribute(perms));
                }

                // Now write the file contents.
                try (final PrintWriter pw = new PrintWriter(restFile)) {
                    // Couldn't be bothered starting up a JSON writer for two simple values.
                    pw.printf("{\"%s\":\"%s\", \"port\":%d}%n", ConstellationHttpServlet.SECRET_HEADER, ConstellationHttpServlet.SECRET, port);
                }
                try (final PrintWriter pw = new PrintWriter(restFileIPython)) {
                    // Couldn't be bothered starting up a JSON writer for two simple values.
                    pw.printf("{\"%s\":\"%s\", \"port\":%d}%n", ConstellationHttpServlet.SECRET_HEADER, ConstellationHttpServlet.SECRET, port);
                }

                // Download the Python REST client if enabled.
                final boolean pythonRestClientDownload = prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT);
                if (pythonRestClientDownload) {
                    installPythonPackage();
                }

                // Build the server.
                //
                final Server server = new Server(new InetSocketAddress(loopback, port));
                final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);

                // Gather the servlets and add them to the server.
                //
                Lookup.getDefault().lookupAll(ConstellationHttpServlet.class).forEach(servlet -> {
                    if (servlet.getClass().isAnnotationPresent(WebServlet.class)) {
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

                LOGGER.log(Level.INFO, "{0}", String.format("Starting Jetty version %s on%s:%d...", Server.getVersion(), loopback, port));
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
                        try {
                            Files.delete(Path.of(restFile.getPath()));
                        } catch (final IOException ex) {
                            //TODO: Handle case where file not successfully deleted
                        }
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

    private static void cleanupRest(final File restFile, final String directory) {
        if (Files.exists(Path.of(directory).resolve(REST_FILE))) {
            try {
                Files.delete(Path.of(restFile.getPath()));
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Error deleting existing rest file in user directory");
            }
        }
    }

    public static File getScriptDir(final boolean mkdir) {
        final File homeDir = new File(System.getProperty("user.home"));
        final File ipython = new File(homeDir, IPYTHON);

        if (!ipython.exists() && mkdir) {
            ipython.mkdir();
        }

        return ipython;
    }

    public static String getNotebookDir() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        // Return path to directory
        return prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT);
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
        downloadPythonClientToDir(ipython);
    }

    /**
     * Download the Python REST API client to the user's Jupyter Notebook directory.
     * <p>
     * The download is done only if the package installation fails
     */
    public static void downloadPythonClientNotebookDir() {
        downloadPythonClientToDir(new File(getNotebookDir()));
    }

    /**
     * Download the Python REST API client to a given directory.
     * <p>
     * The download is done only if the script doesn't exist, or the existing script needs updating.
     * <p>
     * This is in the path for Jupyter notebooks, but not for standard Python.
     *
     * @param directory The directory for constellation_client.py to be downloaded to.
     */
    public static void downloadPythonClientToDir(final File directory) {
        final File download = new File(directory, CONSTELLATION_CLIENT);

        final boolean doDownload = !download.exists() || !equalScripts(download);

        if (doDownload) {

            try {
                Files.copy(Paths.get(SCRIPT_SOURCE + CONSTELLATION_CLIENT), download.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                LOGGER.log(Level.WARNING, "Error retrieving constellation_client.py:", e);
                return;
            }

            // Succssfully copied files
            final String msg = String.format("'%s' downloaded to %s", CONSTELLATION_CLIENT, directory);
            StatusDisplayer.getDefault().setStatusText(msg);

        }
    }

    // Run pip install on package
    public static void installPythonPackage() throws IOException {
        // Create the process buillder with required arguments
        final ProcessBuilder pb;
        if (isWindows()) {
            pb = new ProcessBuilder(ArrayUtils.addAll(WINDOWS_COMMAND, PACKAGE_INSTALL)).redirectErrorStream(true);
        } else {
            pb = new ProcessBuilder(ArrayUtils.addAll(UNIX_COMMAND, PACKAGE_INSTALL)).redirectErrorStream(true);
        }

        // Srart installed process
        LOGGER.log(Level.INFO, "Python package installation begun...");
        Process p = null;
        try {
            p = pb.start();
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, "IO EXCEPTION CAUGHT reading python package installation:", ex);
            Thread.currentThread().interrupt();

        }

        if (p == null) {
            return;
        }

        try (final BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));) {

            // If inputStream available, log output
            String line;

            while ((line = inputBuffer.readLine()) != null) {
                LOGGER.log(Level.INFO, "{0}", line);
            }

            final int result = p.waitFor();
            LOGGER.log(Level.INFO, "Python package installation finished...");
            LOGGER.log(Level.INFO, "Python install process result: {0}", result);

            // If not successful
            if (result != INSTALL_SUCCESS) {
                LOGGER.log(Level.INFO, "Python package installation unsuccessful, copying script to notebook directory...");
                downloadPythonClientNotebookDir();
            }

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.WARNING, "INTERRUPTED EXCEPTION CAUGHT in the python package installation:", ex);
            Thread.currentThread().interrupt();

        }

        p.destroy();
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
        try (final FileInputStream in1 = new FileInputStream(scriptFile); final InputStream in2 = new FileInputStream(SCRIPT_SOURCE + CONSTELLATION_CLIENT)) {
            final byte[] dig1 = getDigest(in1);
            final byte[] dig2 = getDigest(in2);

            return MessageDigest.isEqual(dig1, dig2);

        } catch (final FileNotFoundException | NoSuchAlgorithmException ex) {
            return false;
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Equal scripts", ex);
            return false;
        }
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }
}

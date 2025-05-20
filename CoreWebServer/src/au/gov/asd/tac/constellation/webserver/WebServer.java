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
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
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

    private static final String PACKAGE_NAME = "constellation_client";
    protected static final String CONSTELLATION_CLIENT = PACKAGE_NAME + ".py";

    private static final String IPYTHON = ".ipython";
    private static final String SEP = File.separator;
    private static final String SCRIPT_SOURCE = Generator.getBaseDirectory() + "ext" + SEP + "package" + SEP + "src" + SEP + PACKAGE_NAME + SEP;

    private static final String PACKAGE_SOURCE = Generator.getBaseDirectory() + "ext" + SEP + "package" + SEP + "package_dist";
    private static final String[] PACKAGE_INSTALL = {"pip", "install", "--upgrade", PACKAGE_NAME, "--no-index", "--find-links", "file:" + PACKAGE_SOURCE};
    private static final String[] WINDOWS_COMMAND = {"cmd.exe", "/C"};

    private static final String[] CHECK_INSTALL = {"pip", "freeze"};
    private static final int INSTALL_SUCCESS = 0;

    private static final String SELECT_FOLDER_TITLE = "Select folder";
    private static final String ALERT_HEADER_TEXT = "Copying constellation_client.py to %s has failed!\n\nReason: %s\n\nYou will need to manually place a copy of constellation_client.py in %s";
    private static final String ALERT_CONTEXT_TEXT = "Do you want to save a copy of constellation_client.py?";

    public static boolean isRunning() {
        return running;
    }

    public static synchronized int start() {
        if (running) {
            return port;
        }

        try {
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

            final InetAddress loopback = InetAddress.getLoopbackAddress();
            port = prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT);

            // Put the session secret and port number in a JSON file in the .CONSTELLATION directory.
            // Get rest directory, if path to directory is empty (default), use the ipython directory
            final String restPref = prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT);
            final String restDir = "".equals(restPref) ? getScriptDir(true).toString() : restPref;
            final File restFile = new File(restDir, REST_FILE);
            cleanupRest(restFile, restDir);

            // On Posix, we can use stricter file permissions.
            // On Windows, we just create the new file.
            if (!isWindows()) {
                // Make sure the file is owner read/write.
                final Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                Files.createFile(restFile.toPath(), PosixFilePermissions.asFileAttribute(perms));
            }

            // Now write the file contents.
            try (final PrintWriter pw = new PrintWriter(restFile)) {
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

    private static boolean isHeadless() {
        return Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty("java.awt.headless"));
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

        // If file already exist and is latest version of script, no need to copy file
        if (download.exists() && equalScripts(download)) {
            LOGGER.log(Level.INFO, "constellation_client.py already present!");
            return;
        }

        try {
            Files.copy(Paths.get(SCRIPT_SOURCE + CONSTELLATION_CLIENT), download.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            // Formatted string is used both to log directory and because the exception would be shown
            // to user if e was logged as throwable, rather than converted to string
            LOGGER.log(Level.WARNING, String.format("Error copying constellation_client.py into %s: %s", directory, e));
            alertUserOfAccessException(e);
            return;
        }

        // Succssfully copied files
        final String msg = String.format("'%s' downloaded to %s", CONSTELLATION_CLIENT, directory);
        StatusDisplayer.getDefault().setStatusText(msg);
    }

    private static void alertUserOfPackageFailure() {
        if (isHeadless()) {
            return;
        }

        // Show and wait has to be called from a runlater, but the rest of code wont actually wait. Hence, the latch
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            final Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Installation of constellation client python package has failed!");
            alert.setContentText("A copy of the constellation client script will be created in " + getNotebookDir());
            alert.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

            alert.showAndWait();
            latch.countDown();
        });

        // Wait
        try {
            latch.await();
        } catch (final InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void alertUserOfAccessException(final Exception exception) {
        if (isHeadless()) {
            return;
        }

        // Show and wait has to be called from a runlater, but the rest of code wont actually wait. Hence, the latch
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean requireScriptCopy = new AtomicBoolean(false);

        Platform.runLater(() -> {
            final Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(String.format(ALERT_HEADER_TEXT, getNotebookDir(), exception, getNotebookDir()));
            alert.setContentText(ALERT_CONTEXT_TEXT);

            alert.getDialogPane().getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

            final ButtonType buttonYes = new ButtonType("Yes", ButtonData.YES);
            final ButtonType buttonCancel = new ButtonType("No", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonYes, buttonCancel);

            final Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonYes) {
                requireScriptCopy.set(true);
            }
            latch.countDown();
        });

        // Wait
        try {
            latch.await();
        } catch (final InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted!", e);
            Thread.currentThread().interrupt();
        }

        if (requireScriptCopy.get()) {
            showSaveClientScriptPopup(exception);
        }
    }

    private static void showSaveClientScriptPopup(final Exception exception) {
        if (isHeadless()) {
            return;
        }

        FileChooser.openSaveDialog(getFolderChooser()).thenAccept(folder -> {
            // If a folder was chosen
            if (!folder.isEmpty()) {
                downloadPythonClientToDir(folder.get());
            } else {
                // Otherwise show previous popup
                alertUserOfAccessException(exception);
            }
        });
    }

    /**
     * Creates a new folder chooser.
     *
     * @return the created folder chooser.
     */
    private static FileChooserBuilder getFolderChooser() {
        return new FileChooserBuilder(SELECT_FOLDER_TITLE)
                .setTitle(SELECT_FOLDER_TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setDirectoriesOnly(true);
    }

    /**
     * Install the Python REST API client package using pip install
     *
     * @throws IOException
     */
    public static void installPythonPackage() throws IOException {
        // Create the process buillder with required arguments
        final ProcessBuilder pb;
        int processResult = -1; // Fail by default

        if (isWindows()) {
            pb = new ProcessBuilder(ArrayUtils.addAll(WINDOWS_COMMAND, PACKAGE_INSTALL)).redirectErrorStream(true);
        } else {
            pb = new ProcessBuilder(PACKAGE_INSTALL).redirectErrorStream(true);
        }

        // Start install process
        final Process p = startPythonProcess(pb, "installation");
        if (p == null) {
            return;
        }

        try (final BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));) {

            // If inputStream available, log output
            String line;

            while ((line = inputBuffer.readLine()) != null) {
                LOGGER.log(Level.INFO, "{0}", line);
            }

            processResult = p.waitFor();
            LOGGER.log(Level.INFO, "Python package installation finished...");
            LOGGER.log(Level.INFO, "Python install process result: {0}", processResult);

            // If not successful
            if (processResult != INSTALL_SUCCESS) {
                LOGGER.log(Level.INFO, "Python package installation unsuccessful, copying script to notebook directory...");
                alertUserOfPackageFailure();
                downloadPythonClientNotebookDir();
            }

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.WARNING, "INTERRUPTED EXCEPTION CAUGHT in the python package installation:", ex);
            Thread.currentThread().interrupt();
        }

        p.destroy();

        // Verify install worked, unsuccessful process would have already been caught
        if (processResult == INSTALL_SUCCESS) {
            verifyPythonPackage();
        }
    }

    /**
     * Verify that the Python REST API client package was installed. Otherwise copy the script file to the notebook
     * directory
     *
     */
    public static void verifyPythonPackage() throws IOException {
        // Create the process buillder with required arguments
        final ProcessBuilder pb = new ProcessBuilder(CHECK_INSTALL).redirectErrorStream(true);

        // Verify process
        final Process p = startPythonProcess(pb, "verification");
        if (p == null) {
            return;
        }

        try (final BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(p.getInputStream()));) {

            // If inputStream available, log output
            String allLines = "";
            String currentLine;

            while ((currentLine = inputBuffer.readLine()) != null) {
                allLines = allLines.concat(currentLine);
            }

            final int result = p.waitFor();

            LOGGER.log(Level.INFO, "Verification process result: {0}", result);

            // If not successful
            if (result != INSTALL_SUCCESS) {
                LOGGER.log(Level.INFO, "Python package verification unsuccessful, copying script to notebook directory...");
                alertUserOfPackageFailure();
                downloadPythonClientNotebookDir();
            } else if (allLines.contains(PACKAGE_NAME)) {  // Result must be success, so if output of listed packages include constellation_client
                LOGGER.log(Level.INFO, "Python package was installed!");
            } else {
                LOGGER.log(Level.INFO, "Could not find python package, copying script to notebook directory...");
                alertUserOfPackageFailure();
                Platform.runLater(() -> downloadPythonClientNotebookDir());
            }

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.WARNING, "INTERRUPTED EXCEPTION CAUGHT in the python package verification:", ex);
            Thread.currentThread().interrupt();
        }

        LOGGER.log(Level.INFO, "Verification of package finished...");
        p.destroy();
    }

    public static Process startPythonProcess(final ProcessBuilder pb, final String warning) {
        LOGGER.log(Level.INFO, "Python package {0} begun...", warning);
        final Process p;
        try {
            p = pb.start();
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, "IO EXCEPTION CAUGHT in process for python package {0}: {1}", new Object[]{warning, ex});
            LOGGER.log(Level.INFO, "Copying python script to notebook directory instead...");
            alertUserOfPackageFailure();
            downloadPythonClientNotebookDir();
            return null;
        }
        return p;
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
        return System.getProperty("os.name").startsWith("Windows");
    }
}

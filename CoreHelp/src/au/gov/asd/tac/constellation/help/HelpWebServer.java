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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
}

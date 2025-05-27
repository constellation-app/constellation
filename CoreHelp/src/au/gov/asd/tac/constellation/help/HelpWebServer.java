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
package au.gov.asd.tac.constellation.help;

import au.gov.asd.tac.constellation.help.preferences.HelpPreferenceKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Delphinus8821
 */
public class HelpWebServer {

    private static final Logger LOGGER = Logger.getLogger(HelpWebServer.class.getName());

    private static boolean running = false;
    private static final String WEB_SERVER_THREAD_NAME = "Help Web Server";

    private HelpWebServer() {
        // Intentionally left blank 
    }
    
    public static synchronized int start() {
        int port = 0;
        if (!running) {
            try {
                final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
                final InetAddress loopback = InetAddress.getLoopbackAddress();
                port = prefs.getInt(HelpPreferenceKeys.OFFLINE_HELP_PORT, HelpPreferenceKeys.OFFLINE_HELP_PORT_DEFAULT);

                // Build the server.
                final Server server = new Server(new InetSocketAddress(loopback, port));
                final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                server.setHandler(context);

                // Gather the servlets and add them to the server.
                Lookup.getDefault().lookupAll(HttpServlet.class).forEach(servlet -> {
                    if (servlet.getClass().isAnnotationPresent(WebServlet.class)) {
                        for (final String urlPattern : servlet.getClass().getAnnotation(WebServlet.class).urlPatterns()) {
                            Logger.getGlobal().info(String.format("urlpattern %s %s", servlet, urlPattern));
                            context.addServlet(new ServletHolder(servlet), urlPattern);
                        }
                    }
                });

                // Make our own handler so we can log requests with the CONSTELLATION logs.
                final RequestLog requestLog = (request, response) ->
                    LOGGER.log(Level.INFO, "Request at {0} from {1} {2}, status {3}", new Object[]{LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), request.getRemoteAddr(), request.getRequestURI(), response.getStatus()});
                server.setRequestLog(requestLog);
                
                LOGGER.log(Level.INFO, "Starting Jetty version {0} on {1}:{2}...", new Object[]{Server.getVersion(), loopback, port});
                server.start();

                // Wait for the server to stop (if it ever does).
                final Thread webserver = new Thread(() -> {
                    try {
                        server.join();
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
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

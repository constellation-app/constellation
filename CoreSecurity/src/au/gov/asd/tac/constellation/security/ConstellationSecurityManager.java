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
package au.gov.asd.tac.constellation.security;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.net.ssl.SSLContext;
import org.openide.util.Lookup;

/**
 * The ConstellationSecurityManager manages starting and stopping security in
 * the application.
 * <p>
 * Any code that uses the security manager must first call one of
 * {@link #startSecurity()}, {@link #startSecurityLater(Runnable)}, or
 * {@link #startSecurityLaterFX(Runnable)}. This allows the security manager do
 * do its thing, which may include prompting the user for passwords, and can
 * therefore take some time. Failing to do this means that any code requiring
 * security (including implicit use of SSL contexts, for example) runs the risk
 * of failing.
 *
 * @author sirius
 */
public class ConstellationSecurityManager {

    private static final Logger LOGGER = Logger.getLogger(ConstellationSecurityManager.class.getName());
    private static final Level LEVEL = Level.INFO;

    public static final String PREFERRED_PROVIDER_PROPERTY = "preferred.security.provider";
    public static final String RUNNABLE_THREAD_NAME = "Runnable Thread";
    public static final String RUNNABLE_JAVAFX_THREAD_NAME = "Runnable JavaFX Thread";

    private static ConstellationSecurityProvider[] securityProviders = null;
    private static ConstellationSecurityContext[][] securityContexts = null;

    private static ConstellationSecurityContext currentContext = null;

    private static final boolean RUN_IMMEDIATELY = false;
    private static final List<Runnable> RUN_AFTER_STARTING = new ArrayList<>();

    /**
     * Starts the security manager synchronously.
     * <p>
     * If the security manager has already been started, this call has no
     * effect.
     */
    public static synchronized void startSecurity() {
        if (securityProviders == null) {
            LOGGER.log(LEVEL, "Starting security");

            final List<ConstellationSecurityProvider> providers = new ArrayList<>(Lookup.getDefault().lookupAll(ConstellationSecurityProvider.class));
            LOGGER.log(LEVEL, "Found {0} security provider{1}", new Object[]{providers.size(), providers.size() == 1 ? "" : "s"});
            providers.stream().forEach(provider -> LOGGER.log(LEVEL, "  {0}", provider));

            // Check for a preferred provider, if the preferred provider is present move it to the top of the list
            final String preferredProvider = System.getProperty(PREFERRED_PROVIDER_PROPERTY);
            if (preferredProvider != null) {
                LOGGER.log(LEVEL, "Searching for preferred security provider: {0}", preferredProvider);

                for (int i = 0; i < providers.size(); i++) {
                    final ConstellationSecurityProvider provider = providers.get(i);
                    if (preferredProvider.equals(provider.getName())) {
                        LOGGER.log(LEVEL, "Found preferred security provider {0} in position {1}", new Object[]{preferredProvider, i});

                        providers.remove(i);
                        providers.add(0, provider);
                        break;
                    }
                }
            } else {
                LOGGER.log(LEVEL, "No preferred security provider specified");
            }

            securityProviders = providers.toArray(ConstellationSecurityProvider[]::new);
            securityContexts = new ConstellationSecurityContext[securityProviders.length][];

            for (int i = 0; i < securityProviders.length; i++) {
                LOGGER.log(LEVEL, "Getting contexts from {0}", securityProviders[i].getName());
                
                final List<ConstellationSecurityContext> contexts = securityProviders[i].getContexts();
                if (contexts != null) {
                    securityContexts[i] = contexts.toArray(ConstellationSecurityContext[]::new);
                    for (final ConstellationSecurityContext context : securityContexts[i]) {
                        try {
                            final SSLContext sslContext = context.getSSLContext();
                            if (sslContext != null) {
                                SSLContext.setDefault(sslContext);
                                currentContext = context;

                                LOGGER.log(LEVEL, "Setting current context {0} from provider {1}",
                                        new Object[]{currentContext.getName(), securityProviders[i].getName()});

                                return;
                            }
                        } catch (final SecurityException ex) {
                            // TODO: Handle exceptions from getSSLContext()
                            LOGGER.severe(String.format("Exception while setting default SSLContext: %s", ex.getMessage()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Run the specified Runnable on a new thread after starting the
     * Constellation security manager.
     * <p>
     * The security manager is started on a background thread using
     * {@link #startSecurity()}. When security has been started, the Runnable is
     * run using {@link Runnable#run()}.
     *
     * @param runAfter A Runnable to run after security is started.
     */
    public static void startSecurityLater(final Runnable runAfter) {
        new Thread() {
            @Override
            public void run() {
                setName(RUNNABLE_THREAD_NAME);
                startSecurity();
                if (runAfter != null) {
                    runAfter.run();
                }
            }
        }.start();
    }

    /**
     * Run the specified Runnable on the JavaFX application thread after
     * starting the Constellation security manager.
     * <p>
     * The security manager is started on a background thread using
     * {@link #startSecurity()}. When security has been started, the Runnable is
     * run using {@link Platform#runLater(Runnable) }.
     *
     * @param runAfter A Runnable to run on the JavaFX application thread after
     * security is started.
     */
    public static void startSecurityLaterFX(final Runnable runAfter) {
        new Thread() {
            @Override
            public void run() {
                setName(RUNNABLE_JAVAFX_THREAD_NAME);
                startSecurity();
                Platform.runLater(runAfter);
            }
        }.start();
    }

    /**
     * Stops the security manager synchronously.
     */
    public synchronized void stopSecurity() {
        if (securityProviders != null) {
            securityProviders = null;
            securityContexts = null;
            currentContext = null;
        }
    }

    /**
     * Get the current ConstellationSecurityContext.
     *
     * @return the current ConstellationSecurityContext.
     */
    public static ConstellationSecurityContext getCurrentSecurityContext() {
        return currentContext;
    }

    /**
     * Allows you to queue up a Runnable to be run after security is
     * initialised.
     *
     * @param runnable the Runnable.
     */
    public static void runAfterStarting(final Runnable runnable) {
        synchronized (RUN_AFTER_STARTING) {
            if (RUN_IMMEDIATELY) {
                new Thread(runnable).start();
            } else {
                RUN_AFTER_STARTING.add(runnable);
            }
        }
    }
}

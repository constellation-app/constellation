/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.https;

import au.gov.asd.tac.constellation.security.ConstellationSecurityContext;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLContext;

/**
 * Factory for {@link java.net.http.HttpClient} builders that use the same
 * client certificates (mTLS) and proxy settings as the rest of Constellation.
 * <p>
 * The JDK HttpClient captures {@link SSLContext#getDefault()} and
 * {@link ProxySelector#getDefault()} when it is built. Builders created here
 * instead take the SSL context from the current
 * {@link ConstellationSecurityContext} (starting security synchronously if it
 * hasn't been started yet), and use a proxy selector that delegates to whatever
 * the default selector is at the time of each request, so changes made by
 * {@code ProxyUtilities} take effect without rebuilding the client.
 *
 * @author constellation
 */
public final class ConstellationHttpClients {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private ConstellationHttpClients() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create a new HttpClient builder configured with the current Constellation
     * security context and proxy settings.
     *
     * @param requireSecurityContext if true, fail if there is no Constellation
     * security context (the same fail-closed rule as {@link HttpsConnection}).
     * @return a configured builder.
     * @throws IllegalStateException if a security context is required but none
     * is available.
     */
    public static HttpClient.Builder newBuilder(final boolean requireSecurityContext) {
        // synchronous and idempotent, so we never race the asynchronous startup
        ConstellationSecurityManager.startSecurity();
        final ConstellationSecurityContext context = ConstellationSecurityManager.getCurrentSecurityContext();
        final SSLContext sslContext = context != null ? context.getSSLContext() : null;
        if (sslContext == null && requireSecurityContext) {
            throw new IllegalStateException(HttpsConnection.NO_CERTIFICATE_ERROR);
        }

        final HttpClient.Builder builder = HttpClient.newBuilder()
                .proxy(new LiveDefaultProxySelector())
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        if (sslContext != null) {
            builder.sslContext(sslContext);
        }
        return builder;
    }

    /**
     * Describe the security context that {@link #newBuilder(boolean)} would
     * use, for display in user interfaces.
     *
     * @return a human readable description of the current security context.
     */
    public static String describeSecurityContext() {
        ConstellationSecurityManager.startSecurity();
        final ConstellationSecurityContext context = ConstellationSecurityManager.getCurrentSecurityContext();
        if (context == null || context.getSSLContext() == null) {
            return "No client certificate configured (default JVM trust store)";
        }
        final String dn = context.getDistinguishedName();
        return dn == null ? context.getName() : String.format("%s (%s)", context.getName(), dn);
    }

    /**
     * A ProxySelector that follows {@code ProxySelector.setDefault(...)}
     * replacing the default selector at runtime.
     */
    static final class LiveDefaultProxySelector extends ProxySelector {

        @Override
        public List<Proxy> select(final URI uri) {
            final ProxySelector current = ProxySelector.getDefault();
            return current == null || current == this ? List.of(Proxy.NO_PROXY) : current.select(uri);
        }

        @Override
        public void connectFailed(final URI uri, final SocketAddress sa, final IOException ex) {
            final ProxySelector current = ProxySelector.getDefault();
            if (current != null && current != this) {
                current.connectFailed(uri, sa, ex);
            }
        }
    }
}

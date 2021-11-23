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
package au.gov.asd.tac.constellation.security.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 * A ProxySelector that matches local hosts, specific hosts, and a default proxy
 * for HTTP and HTTPS protocols.
 *
 * @author algol
 */
public class ConstellationHttpProxySelector extends ProxySelector {

    private static final Logger LOGGER = Logger.getLogger(ConstellationHttpProxySelector.class.getName());
    private static final List<Proxy> NO_PROXY = Collections.singletonList(Proxy.NO_PROXY);

    private final Pair<String, Integer> defaultProxy;
    private final List<Pair<String, Pair<String, Integer>>> additionalProxies;
    private final List<String> bypassProxyHosts;

    public ConstellationHttpProxySelector() {
        final Preferences prefs = NbPreferences.forModule(ProxyPreferenceKeys.class);
        final boolean useDefaults = prefs.getBoolean(ProxyPreferenceKeys.USE_DEFAULTS, true);
        if (useDefaults) {
            final ConstellationHttpProxy proxy = ConstellationHttpProxy.getDefault();
            if (proxy != null) {
                defaultProxy = proxy.getDefaultProxy();
                additionalProxies = proxy.getAdditionalProxies();
                bypassProxyHosts = proxy.getBypassProxyHosts();
            } else {
                defaultProxy = new Pair<>("", 0);
                additionalProxies = Collections.emptyList();
                bypassProxyHosts = Collections.emptyList();
            }
        } else {
            defaultProxy = ProxyUtilities.parseProxy(prefs.get(ProxyPreferenceKeys.DEFAULT, ProxyPreferenceKeys.DEFAULT_DEFAULT), true);
            additionalProxies = ProxyUtilities.parseProxies(prefs.get(ProxyPreferenceKeys.ADDITIONAL, ProxyPreferenceKeys.ADDITIONAL_DEFAULT), true);
            bypassProxyHosts = Arrays.asList(prefs.get(ProxyPreferenceKeys.BYPASS, ProxyPreferenceKeys.BYPASS_DEFAULT).split(ProxyUtilities.PROXY_SEPARATOR));
        }

        // Iterate to remove whitespace and make lowercase
        for (int i = 0; i < bypassProxyHosts.size(); i++) {
            bypassProxyHosts.set(i, bypassProxyHosts.get(i).trim().toLowerCase());
        }

        LOGGER.log(Level.INFO, "Setting default proxy: {0}", defaultProxy);
        LOGGER.log(Level.INFO, "Setting additional proxies: {0}", additionalProxies);
        LOGGER.log(Level.INFO, "Setting bypass proxy hosts: {0}", bypassProxyHosts);
    }

    @Override
    public List<Proxy> select(final URI uri) {
        final String scheme = uri.getScheme();
        final boolean isHttp = scheme.compareToIgnoreCase("http") == 0 || scheme.compareToIgnoreCase("https") == 0;
        LOGGER.log(Level.FINE, "isHttp is {0}, schema is {1} for uri {2}", new Object[]{isHttp, scheme, uri});
        if (isHttp) {
            final String host = uri.getHost().toLowerCase();

            // First step: do we have a specific proxy for this host?
            for (final Pair<String, Pair<String, Integer>> entry : additionalProxies) {
                final String additionalProxyHost = entry.getKey();
                if (isValidHost(host, additionalProxyHost)) {
                    LOGGER.log(Level.FINE, "host {0} will use additional proxy {1}", new Object[]{host, additionalProxyHost});
                    return makeProxy(entry.getValue());
                }
            }

            // Second step: is this a local host?
            final boolean isBypassProxyHost = isLocalHost(host, bypassProxyHosts);
            if (isBypassProxyHost) {
                LOGGER.log(Level.FINE, "host {0} will bypass the proxy", host);
                return NO_PROXY;
            }

            // Third and last step: do we have a default proxy?
            if (defaultProxy != ConstellationHttpProxy.NO_PROXY) {
                LOGGER.log(Level.FINE, "host {0} will use the default proxy {1}", new Object[]{host, defaultProxy});
                return makeProxy(defaultProxy);
            }
        }

        // All the checks failed, so no proxy for you.
        LOGGER.log(Level.FINE, "No proxy for uri {0}", uri);
        return NO_PROXY;
    }

    /**
     * Construct a {@link Proxy} object from the given {@link Pair} containing
     * an address and port for the proxy.
     *
     * @param proxy a {@link Pair} object containing an address and port for the
     * proxy.
     * @return a {@link Proxy} object representing the proxy.
     */
    private List<Proxy> makeProxy(final Pair<String, Integer> proxy) {
        final Proxy proxyObject = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(proxy.getKey(), proxy.getValue()));
        LOGGER.log(Level.FINER, "Proxy {0}", proxyObject);
        return Collections.singletonList(proxyObject);
    }

    /**
     * Is the specified host local?
     * <p>
     * The host is compared case-insensitively to each name in the localHosts
     * list. If the local name starts with ".", it is a suffix; the host is
     * local if it ends with the suffix. If the local name does not start with
     * ".", it is a host name; the host is local if it equals the local name.
     * <p>
     * The localHosts list implicitly has "localhost" and "127.0.0.1" at the
     * beginning.
     * <p>
     * If nothing matches, the host is not local.
     *
     * @param host A host name.
     *
     * @return True if the host matches a suffix or name and is therefore local,
     * false otherwise.
     */
    private static boolean isLocalHost(final String host, final List<String> localHosts) {
        final String hostLowerCase = host.toLowerCase();
        if (StringUtils.equalsAnyIgnoreCase(hostLowerCase, (CharSequence[]) new String[]{"localhost", "127.0.0.1"})) {
            return true;
        }

        return localHosts.stream().anyMatch(localHost -> isValidHost(hostLowerCase, localHost));
    }

    /**
     * Does the specified host match the specified compareHost?
     * <p>
     * The host is compared to the compareHost. If the compare name starts with
     * ".", it is a suffix; the host is valid if it ends with that suffix. If
     * the compare name does not start with ".", it is a host name; the host is
     * valid if it equals that host name.
     * <p>
     * If nothing matches, the host is not valid.
     *
     * @param host A host name.
     *
     * @return True if the host matches a suffix or name and is therefore local,
     * false otherwise.
     */
    private static boolean isValidHost(final String host, final String compareHost) {
        if (compareHost.startsWith(".")) {
            if (host.endsWith(compareHost)) {
                return true;
            }
        } else if (compareHost.equals(host)) {
            return true;
        } else {
            // Do nothing
        }

        return false;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress socket, final IOException ex) {
        LOGGER.log(Level.SEVERE, "Connect failed for uri {0}, socket {1} "
                + "with message {2})", new Object[]{uri, socket, ex.getMessage()});
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(bypassProxyHosts.toString());
        sb.append(ProxyUtilities.SEMICOLON);
        sb.append(additionalProxies.toString());
        sb.append(ProxyUtilities.SEMICOLON);
        sb.append(defaultProxy.toString());

        return sb.toString();
    }
}

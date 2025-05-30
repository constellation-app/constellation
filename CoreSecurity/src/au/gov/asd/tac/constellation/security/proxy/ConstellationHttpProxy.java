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
package au.gov.asd.tac.constellation.security.proxy;

import java.util.List;
import javafx.util.Pair;
import org.openide.util.Lookup;

/**
 * Provides default values for use by the
 * {@link ConstellationHttpProxySelector}.
 * <p>
 * Names with a leading '.' are considered to be suffixes, and match the end of
 * a specified host name. So ".server.com" matches "my.server.com" and
 * "a.b.server.com", whereas "server.com" only matches "server.com".
 *
 * @author algol
 * @author cygnus_x-1
 */
public interface ConstellationHttpProxy {

    /**
     * No proxy.
     */
    public static final Pair<String, Integer> NO_PROXY = new Pair<>("", 0);

    /**
     * The default proxy to use.
     * <p>
     * This Pair object represents the address and port number of the default
     * proxy server. Use {@link #NO_PROXY} if there is no default proxy.
     *
     * @return The default proxy.
     */
    public Pair<String, Integer> getDefaultProxy();

    /**
     * A string representation of the default proxy.
     *
     * @return A string representation of the default proxy.
     */
    public default String getDefaultProxyString() {
        return String.format("%s:%d", getDefaultProxy().getKey(), getDefaultProxy().getValue());
    }

    /**
     * A list of additional proxies for use in special cases in place of the
     * default proxy.
     * <p>
     * This list should contain Pair objects matching a host name to a specific
     * proxy. For example:
     * <pre>
     * example.com = thisproxy:8888 # host example.com is access via thisproxy:8888
     * .remote = thatproxy:8080 # any host ending in .remote are accessed via thatproxy:8080
     * </pre>
     *
     * @return A list of additional proxies.
     */
    public List<Pair<String, Pair<String, Integer>>> getAdditionalProxies();

    /**
     * A string representation of additional proxies.
     *
     * @return A string representation of additional proxies.
     */
    public default String getAdditionalProxiesString() {
        final StringBuilder additionalProxies = new StringBuilder();
        getAdditionalProxies().forEach(additionalProxy -> additionalProxies.append(additionalProxy.getKey())
                .append("=")
                .append(additionalProxy.getValue().getKey())
                .append(":")
                .append(additionalProxy.getValue().getValue())
                .append(ProxyUtilities.PROXY_SEPARATOR));
        return additionalProxies.toString();
    }

    /**
     * A list of host names to be connected to directly, ignoring the default
     * proxy.
     * <p>
     * The host names "localhost" and "127.0.0.1" are implicitly included, so
     * they need not be included.
     *
     * @return A list of host names to be connected to directly.
     */
    public List<String> getBypassProxyHosts();

    /**
     * A string representation of the no proxy hosts.
     *
     * @return A string representation of the no proxy hosts.
     */
    public default String getBypassProxyHostsString() {
        final StringBuilder noProxyHosts = new StringBuilder();
        getBypassProxyHosts().forEach(noProxyHost -> 
            noProxyHosts.append(noProxyHost).append(ProxyUtilities.PROXY_SEPARATOR));
        return noProxyHosts.toString();
    }

    /**
     * Get the default ConstellationHttpProxy. This is the registered
     * ConstellationHttpProxy class with the lowest position value.
     *
     * @return The default ConstellationHttpProxy.
     */
    public static ConstellationHttpProxy getDefault() {
        return Lookup.getDefault().lookup(ConstellationHttpProxy.class);
    }
}

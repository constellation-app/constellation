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

import java.io.IOException;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 * Utilities for setting the HTTP proxy used by CONSTELLATION.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class ProxyUtilities implements PreferenceChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ProxyUtilities.class.getName());
    private static final ProxySelector DEFAULT_PROXY_SELECTOR = new ConstellationHttpProxySelector();
    public static final String PROXY_SEPARATOR = "\n";
    public static final String SEMICOLON = ";";

    public ProxyUtilities() {
        NbPreferences.forModule(ProxyPreferenceKeys.class)
                .addPreferenceChangeListener(ProxyUtilities.this);
    }

    @Override
    public void preferenceChange(final PreferenceChangeEvent event) {
        if (event.getKey().equals(ProxyPreferenceKeys.USE_DEFAULTS)
                || event.getKey().equals(ProxyPreferenceKeys.DEFAULT)
                || event.getKey().equals(ProxyPreferenceKeys.ADDITIONAL)
                || event.getKey().equals(ProxyPreferenceKeys.BYPASS)) {
            setProxySelector(new ConstellationHttpProxySelector());
        }
    }

    /**
     * Set the NetBeans proxy settings to "No Proxy", and set the system-wide
     * ProxySelector.
     * <p>
     * Java 8 seems to have a problem with the default ProxySelector: it throws
     * internal Nashorn exceptions. A workaround is to set it to null. Since
     * CONSTELLATION plugins manually set their own proxies when required, this
     * shouldn't be a problem.
     * <p>
     * Individual modules are perfectly capable of doing this, but this method
     * centralises the call so it gets done in one place, and can log the
     * process. Also, since it's a system-wide setting, in the future we can
     * coordinate the different callers.
     * <p>
     * @param proxySelector Set this ProxySelector system-wide: use null to
     * unset the proxy selector.
     */
    public static synchronized void setProxySelector(final ProxySelector proxySelector) {
        // Set the NetBeans proxy settings to "No Proxy".
        final Preferences proxySettings = NbPreferences.root().node("org/netbeans/core");
        proxySettings.putInt("proxyType", 0);
        removeUnwantedOptions();

        // We don't use a null ProxySelector as there is a bug in NbProxySelector
        // where it checks for null second instead of first.
        final ProxySelector defaultProxySelector = proxySelector == null
                ? DEFAULT_PROXY_SELECTOR : proxySelector;
        LOGGER.log(Level.FINE, "Using ProxySelector: {0}", defaultProxySelector);
        ProxySelector.setDefault(defaultProxySelector);
    }

    /**
     * Remove unwanted options from the Netbeans Options panel.
     * <p>
     * Since we're handling proxies ourselves, the NetBeans general proxy
     * settings panel is redundant. The Miscellaneous Files panel is just
     * confusing.
     */
    private static void removeUnwantedOptions() {
        final String unwanted1 = "OptionsDialog/General.instance";
        final String unwanted2 = "OptionsDialog/Advanced/org-netbeans-core-ui-options-filetypes-FileAssociationsOptionsPanelController.instance";
        final FileObject root = FileUtil.getConfigRoot();
        for (final String unwanted : new String[]{unwanted1, unwanted2}) {
            final FileObject general = root.getFileObject(unwanted);
            if (general != null) {
                try {
                    general.delete();
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, "Error deleting option {0}", unwanted);
                }
            }
        }
    }

    /**
     * Parse a string representing a list of proxies of the form
     * "name=host:port\nname=host:port\n...".
     * <p>
     * The "name" components may be prefixed with a '.'. Blank lines and comment
     * lines (starting with "#") are allowed.
     *
     * @param text The string of proxy representations to be parsed.
     * @param ignoreProblems if true, continue to parse when a problem is found,
     * otherwise return null.
     *
     * @return A list of proxies.
     */
    public static List<Pair<String, Pair<String, Integer>>> parseProxies(final String text, final boolean ignoreProblems) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Pair<String, Pair<String, Integer>>> proxies = new ArrayList<>();

        final String[] lines = text.split(PROXY_SEPARATOR);
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                final int ix = line.indexOf('=');
                if (ix == -1) {
                    if (ignoreProblems) {
                        continue;
                    } else {
                        return null;
                    }
                }

                final String host = line.substring(0, ix).trim();
                if (host.isEmpty()) {
                    if (ignoreProblems) {
                        continue;
                    } else {
                        return null;
                    }
                }

                final String proxyText = line.substring(ix + 1).trim();
                final Pair<String, Integer> proxy = parseProxy(proxyText, false);
                if (proxy == null) {
                    if (ignoreProblems) {
                        continue;
                    } else {
                        return null;
                    }
                }

                proxies.add(new Pair<>(host, proxy));
            }
        }

        return proxies;
    }

    /**
     * Parse a string representing a proxy of the form "host:port".
     *
     * @param text The proxy representation to be parsed.
     * @param canBeEmpty If true, an empty string will represent "no proxy".
     *
     * @return A Pair containing the host and port.
     */
    public static Pair<String, Integer> parseProxy(final String text, final boolean canBeEmpty) {
        if (canBeEmpty && text.isEmpty()) {
            return new Pair<>("", 0);
        }

        final int ix = text.indexOf(':');
        if (ix == -1) {
            return null;
        }

        final String host = text.substring(0, ix).trim();
        if (host.isEmpty()) {
            return null;
        }

        try {
            final int port = Integer.parseInt(text.substring(ix + 1).trim());
            if (port > 0 && port <= 65535) {
                return new Pair<>(host, port);
            }
        } catch (final NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Port couldn't be parsed");
        }

        return null;
    }
}

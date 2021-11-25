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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * A controller for {@link ProxyOptionsPanel}.
 *
 * @author algol
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#ProxyOptions_DisplayName",
        keywords = "#ProxyOptions_Keywords",
        keywordsCategory = "constellation/Preferences",
        position = 900)
@org.openide.util.NbBundle.Messages({
    "ProxyOptions_DisplayName=Proxy",
    "ProxyOptions_Keywords=proxy"
})
public class ProxyOptionsPanelController extends OptionsPanelController implements HelpCtx.Provider {

    private ProxyOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(ProxyPreferenceKeys.class);
        final ProxyOptionsPanel proxyOptionsPanel = getPanel();

        proxyOptionsPanel.setDefaultProxy(prefs.get(ProxyPreferenceKeys.DEFAULT, ProxyPreferenceKeys.DEFAULT_DEFAULT));
        proxyOptionsPanel.setAdditionalProxies(prefs.get(ProxyPreferenceKeys.ADDITIONAL, ProxyPreferenceKeys.ADDITIONAL_DEFAULT));
        proxyOptionsPanel.setBypassProxyHosts(prefs.get(ProxyPreferenceKeys.BYPASS, ProxyPreferenceKeys.BYPASS_DEFAULT));

        // do this last to ensure panel updates correctly
        proxyOptionsPanel.setUseDefaultSettings(prefs.getBoolean(ProxyPreferenceKeys.USE_DEFAULTS, ProxyPreferenceKeys.USE_DEFAULTS_DEFAULT));
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(ProxyPreferenceKeys.class);
                final ProxyOptionsPanel proxyOptionsPanel = getPanel();

                prefs.putBoolean(ProxyPreferenceKeys.USE_DEFAULTS, proxyOptionsPanel.isUseDefaultSettingsSelected());
                prefs.put(ProxyPreferenceKeys.DEFAULT, proxyOptionsPanel.getDefaultProxy());
                prefs.put(ProxyPreferenceKeys.ADDITIONAL, proxyOptionsPanel.getAdditionalProxies());
                prefs.put(ProxyPreferenceKeys.BYPASS, proxyOptionsPanel.getBypassProxyHosts());

                // TODO: remove this, make ProxyUtilities listen to preferences instead
                ProxyUtilities.setProxySelector(new ConstellationHttpProxySelector());
            }
        }
    }

    @Override
    public void cancel() {
        // DO NOTHING
    }

    @Override
    public boolean isValid() {
        final ProxyOptionsPanel proxyOptionsPanel = getPanel();
        return ProxyUtilities.parseProxy(proxyOptionsPanel.getDefaultProxy(), true) != null
                && ProxyUtilities.parseProxies(proxyOptionsPanel.getAdditionalProxies(), false) != null
                && proxyOptionsPanel.getBypassProxyHosts() != null;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(ProxyPreferenceKeys.class);
        final ProxyOptionsPanel proxyOptionsPanel = getPanel();
        return !(proxyOptionsPanel.isUseDefaultSettingsSelected() == prefs.getBoolean(ProxyPreferenceKeys.USE_DEFAULTS, ProxyPreferenceKeys.USE_DEFAULTS_DEFAULT)
                && proxyOptionsPanel.getDefaultProxy().equals(prefs.get(ProxyPreferenceKeys.DEFAULT, ProxyPreferenceKeys.DEFAULT_DEFAULT))
                && proxyOptionsPanel.getAdditionalProxies().equals(prefs.get(ProxyPreferenceKeys.ADDITIONAL, ProxyPreferenceKeys.ADDITIONAL_DEFAULT))
                && proxyOptionsPanel.getBypassProxyHosts().equals(prefs.get(ProxyPreferenceKeys.BYPASS, ProxyPreferenceKeys.BYPASS_DEFAULT)));
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    private ProxyOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ProxyOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.security.proxies");
    }
}

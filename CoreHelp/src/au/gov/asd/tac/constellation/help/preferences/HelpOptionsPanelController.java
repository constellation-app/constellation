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
package au.gov.asd.tac.constellation.help.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * A controller for {@link HelpOptionsPanel}.
 *
 * @author Delphinus8821
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#HelpOptions_DisplayName",
        keywords = "#HelpOptions_Keywords",
        keywordsCategory = "constellation/Preferences",
        position = 1000)
@org.openide.util.NbBundle.Messages({
    "HelpOptions_DisplayName=Help",
    "HelpOptions_Keywords=Help"
})
public class HelpOptionsPanelController extends OptionsPanelController implements HelpCtx.Provider {

    private HelpOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
        final HelpOptionsPanel helpOptionsPanel = getPanel();

        helpOptionsPanel.setOnlineHelpOption(prefs.getBoolean(HelpPreferenceKeys.HELP_KEY, HelpPreferenceKeys.ONLINE_HELP));
    }

    protected PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            getPropertyChangeSupport().firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                getPropertyChangeSupport().firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
                final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
                final HelpOptionsPanel helpOptionsPanel = getPanel();
                prefs.putBoolean(HelpPreferenceKeys.HELP_KEY, helpOptionsPanel.isOnlineHelpSelected());
            }
        }
    }

    @Override
    public void cancel() {
        // Required for OptionsPanelController, intentionally left blank
    }

    @Override
    public boolean isValid() {
        // the checkbox will always have a valid response
        return true;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(HelpPreferenceKeys.class);
        final HelpOptionsPanel helpOptionsPanel = getPanel();
        return (helpOptionsPanel.isOnlineHelpSelected() != prefs.getBoolean(HelpPreferenceKeys.HELP_KEY, HelpPreferenceKeys.ONLINE_HELP));
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    protected HelpOptionsPanel getPanel() {
        if (panel == null) {
            panel = new HelpOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

}

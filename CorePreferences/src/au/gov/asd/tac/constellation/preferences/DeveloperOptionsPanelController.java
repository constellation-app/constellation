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
package au.gov.asd.tac.constellation.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for CONSTELLATION preferences panel.
 *
 * @author algol
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#DeveloperOptions_DisplayName",
        keywords = "#DeveloperOptions_Keywords",
        keywordsCategory = "constellation/Preferences",
        position = 400)
@org.openide.util.NbBundle.Messages({
    "DeveloperOptions_DisplayName=Developer",
    "DeveloperOptions_Keywords=developer"
})
public final class DeveloperOptionsPanelController extends OptionsPanelController {

    private DeveloperOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        final DeveloperOptionsPanel developerOptionsPanel = getPanel();

        developerOptionsPanel.setGcOnOpen(prefs.getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_OPEN, DeveloperPreferenceKeys.FORCE_GC_ON_OPEN_DEFAULT));
        developerOptionsPanel.setGcOnClose(prefs.getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE, DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE_DEFAULT));
        developerOptionsPanel.setDebugGl(prefs.getBoolean(DeveloperPreferenceKeys.DEBUG_GL, DeveloperPreferenceKeys.DEBUG_GL_DEFAULT));
        developerOptionsPanel.setPrintGl(prefs.getBoolean(DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES, DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES_DEFAULT));
        developerOptionsPanel.setDisplayFps(prefs.getBoolean(DeveloperPreferenceKeys.DISPLAY_FRAME_RATE, DeveloperPreferenceKeys.DISPLAY_FRAME_RATE_DEFAULT));
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
                final DeveloperOptionsPanel developerOptionsPanel = getPanel();

                prefs.putBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_OPEN, developerOptionsPanel.isGcOnOpenSelected());
                prefs.putBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE, developerOptionsPanel.isGcOnCloseSelected());
                prefs.putBoolean(DeveloperPreferenceKeys.DEBUG_GL, developerOptionsPanel.isDebugGlSelected());
                prefs.putBoolean(DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES, developerOptionsPanel.isPrintGlSelected());
                prefs.putBoolean(DeveloperPreferenceKeys.DISPLAY_FRAME_RATE, developerOptionsPanel.isDisplayFpsSelected());
            }
        }
    }

    @Override
    public void cancel() {
        // Required for OptionsPanelController, intentionally left blank
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        final DeveloperOptionsPanel developerOptionsPanel = getPanel();
        return !(developerOptionsPanel.isGcOnOpenSelected() == prefs.getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_OPEN, DeveloperPreferenceKeys.FORCE_GC_ON_OPEN_DEFAULT)
                && developerOptionsPanel.isGcOnCloseSelected() == prefs.getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE, DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE_DEFAULT)
                && developerOptionsPanel.isDebugGlSelected() == prefs.getBoolean(DeveloperPreferenceKeys.DEBUG_GL, DeveloperPreferenceKeys.DEBUG_GL_DEFAULT)
                && developerOptionsPanel.isPrintGlSelected() == prefs.getBoolean(DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES, DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES_DEFAULT)
                && developerOptionsPanel.isDisplayFpsSelected() == prefs.getBoolean(DeveloperPreferenceKeys.DISPLAY_FRAME_RATE, DeveloperPreferenceKeys.DISPLAY_FRAME_RATE_DEFAULT));
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private DeveloperOptionsPanel getPanel() {
        if (panel == null) {
            panel = new DeveloperOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.preferences.developer");
    }
}

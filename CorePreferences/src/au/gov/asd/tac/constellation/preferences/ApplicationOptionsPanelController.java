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
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for the application preferences panel
 *
 * @author algol
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#AdvancedOption_DisplayName",
        keywords = "#AdvancedOption_Keywords",
        keywordsCategory = "constellation/ApplicationPreferences",
        position = 0)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName=Application",
    "AdvancedOption_Keywords=directory autosave startup display webserver jupyter notebook python"
})
public final class ApplicationOptionsPanelController extends OptionsPanelController {

    private ApplicationOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // This is a map from the color names to their RGB color bits used by glColorMask.
    //
    private static final Map<String, boolean[]> COLOR_BITS = Map.of(
            "Blue",    new boolean[]{false, false, true},
            "Cyan",    new boolean[]{false, true,  true},
            "Green",   new boolean[]{false, true,  false},
            "Magenta", new boolean[]{true,  false, true},
            "Red",     new boolean[]{true,  false, false},
            "Yellow",  new boolean[]{true,  true,  false}
    );

    public static boolean[] getColorMask(final String color) {
        if (COLOR_BITS.containsKey(color)) {
            return COLOR_BITS.get(color);
        }

        throw new IllegalArgumentException(String.format("Color %s is not valid", color));
    }

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();

        applicationOptionsPanel.setUserDirectory(ApplicationPreferenceKeys.getUserDir(prefs));
        applicationOptionsPanel.setAutosaveEnabled(prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT));
        applicationOptionsPanel.setAutosaveFrequency(prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT));
        applicationOptionsPanel.setWelcomeOnStartup(prefs.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, ApplicationPreferenceKeys.WELCOME_ON_STARTUP_DEFAULT));
        applicationOptionsPanel.setWhatsNewOnStartup(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT));
        applicationOptionsPanel.setWebserverPort(prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT));
        applicationOptionsPanel.setNotebookDirectory(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT));
        applicationOptionsPanel.setRestDirectory(prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT));
        applicationOptionsPanel.setDownloadPythonClient(prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT));
        applicationOptionsPanel.setCurrentFont(prefs.get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT));
        applicationOptionsPanel.setFontSize(prefs.get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT));
        applicationOptionsPanel.setEnableSpellChecking(prefs.getBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING_DEFAULT));
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
                final ApplicationOptionsPanel applicationOptionsPanel = getPanel();

                prefs.put(ApplicationPreferenceKeys.USER_DIR, applicationOptionsPanel.getUserDirectory());
                prefs.putBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, applicationOptionsPanel.isAustosaveEnabled());
                prefs.putInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, applicationOptionsPanel.getAustosaveFrequency());
                prefs.putBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, applicationOptionsPanel.isWelcomeOnStartupSelected());
                prefs.putBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, applicationOptionsPanel.isWhatsNewOnStartupSelected());
                prefs.putInt(ApplicationPreferenceKeys.WEBSERVER_PORT, applicationOptionsPanel.getWebserverPort());
                prefs.put(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, applicationOptionsPanel.getNotebookDirectory());
                prefs.put(ApplicationPreferenceKeys.REST_DIR, applicationOptionsPanel.getRestDirectory());
                prefs.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, applicationOptionsPanel.isDownloadPythonClientSelected());
                prefs.put(ApplicationPreferenceKeys.FONT_FAMILY, applicationOptionsPanel.getCurrentFont());
                prefs.put(ApplicationPreferenceKeys.FONT_SIZE, applicationOptionsPanel.getFontSize());
                prefs.putBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, applicationOptionsPanel.isEnableSpellCheckingSelected());
            }
        }
    }

    @Override
    public void cancel() {
        // Required for OptionsPanelController, intentionally left blank
    }

    @Override
    public boolean isValid() {
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();

        return applicationOptionsPanel.getUserDirectory() != null
                && applicationOptionsPanel.getAustosaveFrequency() > 0
                && applicationOptionsPanel.getWebserverPort() > 0
                && applicationOptionsPanel.getNotebookDirectory() != null
                && applicationOptionsPanel.getRestDirectory() != null
                && applicationOptionsPanel.getCurrentFont() != null
                && applicationOptionsPanel.getFontSize() != null;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();

        return !(applicationOptionsPanel.getUserDirectory().equals(prefs.get(ApplicationPreferenceKeys.USER_DIR, ApplicationPreferenceKeys.USER_DIR_DEFAULT))
                && applicationOptionsPanel.isAustosaveEnabled() == prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT)
                && applicationOptionsPanel.getAustosaveFrequency() == prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT)
                && applicationOptionsPanel.isWelcomeOnStartupSelected() == prefs.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, ApplicationPreferenceKeys.WELCOME_ON_STARTUP_DEFAULT)
                && applicationOptionsPanel.isWhatsNewOnStartupSelected() == prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT)
                && applicationOptionsPanel.getWebserverPort() == prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT)
                && applicationOptionsPanel.getNotebookDirectory().equals(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT))
                && applicationOptionsPanel.getRestDirectory().equals(prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT))
                && applicationOptionsPanel.isDownloadPythonClientSelected() == prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT)
                && applicationOptionsPanel.getCurrentFont().equals(prefs.get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT))
                && applicationOptionsPanel.getFontSize().equals(prefs.get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT))
                && applicationOptionsPanel.isEnableSpellCheckingSelected() == prefs.getBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING_DEFAULT));
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private ApplicationOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ApplicationOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.preferences.application");
    }
}

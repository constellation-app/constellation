/*
 * Copyright 2010-2019 Australian Signals Directorate
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

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();

        applicationOptionsPanel.setUserDirectory(ApplicationPreferenceKeys.getUserDir(prefs));
        applicationOptionsPanel.setAutosaveEnabled(prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT));
        applicationOptionsPanel.setAutosaveFrequency(prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT));
        applicationOptionsPanel.setTutorialOnStartup(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT));
        applicationOptionsPanel.setFreezeGraph(prefs.getBoolean(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT));
        applicationOptionsPanel.setWebserverPort(prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT));
        applicationOptionsPanel.setNotebookDirectory(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT));
        applicationOptionsPanel.setRestDirectory(prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT));
        applicationOptionsPanel.setDownloadPythonClient(prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT));
        applicationOptionsPanel.setRememberSaveLocation(prefs.getBoolean(ApplicationPreferenceKeys.REMEMBER_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_SAVE_LOCATION_DEFAULT));
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
                prefs.putBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, applicationOptionsPanel.getAustosaveEnabled());
                prefs.putInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, applicationOptionsPanel.getAustosaveFrequency());
                prefs.putBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, applicationOptionsPanel.getTutorialOnStartup());
                prefs.putBoolean(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, applicationOptionsPanel.getFreezeGraph());
                prefs.putInt(ApplicationPreferenceKeys.WEBSERVER_PORT, applicationOptionsPanel.getWebserverPort());
                prefs.put(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, applicationOptionsPanel.getNotebookDirectory());
                prefs.put(ApplicationPreferenceKeys.REST_DIR, applicationOptionsPanel.getRestDirectory());
                prefs.putBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, applicationOptionsPanel.getDownloadPythonClient());
                prefs.putBoolean(ApplicationPreferenceKeys.REMEMBER_SAVE_LOCATION, applicationOptionsPanel.getRememberSaveLocation());
            }
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isValid() {
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();
        final boolean valid = applicationOptionsPanel.getUserDirectory() != null
                && applicationOptionsPanel.getAustosaveFrequency() > 0
                && applicationOptionsPanel.getWebserverPort() > 0
                && applicationOptionsPanel.getNotebookDirectory() != null
                && applicationOptionsPanel.getRestDirectory() != null;

        return valid;
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final ApplicationOptionsPanel applicationOptionsPanel = getPanel();
        final boolean changed
                = !(applicationOptionsPanel.getUserDirectory().equals(prefs.get(ApplicationPreferenceKeys.USER_DIR, ApplicationPreferenceKeys.USER_DIR_DEFAULT))
                && applicationOptionsPanel.getAustosaveEnabled() == prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT)
                && applicationOptionsPanel.getAustosaveFrequency() == prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT)
                && applicationOptionsPanel.getTutorialOnStartup() == prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT)
                && applicationOptionsPanel.getFreezeGraph() == prefs.getBoolean(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW_DEFAULT)
                && applicationOptionsPanel.getWebserverPort() == prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT)
                && applicationOptionsPanel.getNotebookDirectory().equals(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT))
                && applicationOptionsPanel.getRestDirectory().equals(prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT))
                && applicationOptionsPanel.getDownloadPythonClient() == prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT)
                && applicationOptionsPanel.getRememberSaveLocation() == prefs.getBoolean(ApplicationPreferenceKeys.REMEMBER_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_SAVE_LOCATION_DEFAULT));

        return changed;
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

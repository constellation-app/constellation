/*
 * Copyright 2010-2024 Australian Signals Directorate
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
 * UI Controller for the View options panel.
 *
 * @author sol695510
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#ViewOptions_DisplayName",
        keywords = "#ViewOptions_Keywords",
        keywordsCategory = "constellation/ViewPreferences",
        position = 1000)
@org.openide.util.NbBundle.Messages({
    "ViewOptions_DisplayName=View",
    "ViewOptions_Keywords=View"
})
public class ViewOptionsPanelController extends OptionsPanelController {

    private ViewOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Preferences prefs = NbPreferences.forModule(ViewPreferenceKeys.class);
    private final Map<String, Boolean> defaultOptions = ViewPreferenceKeys.DEFAULT_VIEW_OPTIONS;

    @Override
    public void update() {
        final ViewOptionsPanel viewOptionsPanel = getPanel();
        viewOptionsPanel.fireTableDataChanged();
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
                final ViewOptionsPanel viewOptionsPanel = getPanel();

                for (final String view : defaultOptions.keySet()) {
                    prefs.putBoolean(view, viewOptionsPanel.getOptionsFromUI().get(view));
                }
            }
        }
    }

    @Override
    public void cancel() {
        // Required for OptionsPanelController, intentionally left blank.
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        final ViewOptionsPanel viewOptionsPanel = getPanel();
        return !viewOptionsPanel.getOptionsFromUI().equals(viewOptionsPanel.getOptionsFromPrefs());
    }

    @Override
    public JComponent getComponent(final Lookup lookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public ViewOptionsPanel getPanel() {
        if (panel == null) {
            panel = new ViewOptionsPanel();
        }

        return panel;
    }
}

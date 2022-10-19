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

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for the graph preferences panel
 *
 * @author aldebaran30701
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#GraphOption_DisplayName",
        keywords = "#GraphOption_Keywords",
        keywordsCategory = "constellation/GraphPreferences",
        position = 600)
@org.openide.util.NbBundle.Messages({
    "GraphOption_DisplayName=Graph",
    "GraphOption_Keywords=blaze size blaze opacity blaze color"
})

public final class GraphOptionsPanelController extends OptionsPanelController {

    private GraphOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
        final GraphOptionsPanel graphOptionsPanel = getPanel();

        // grabbing blaze size from preferences file, reverting to default if none found
        graphOptionsPanel.setBlazeSize(prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT));
        graphOptionsPanel.setBlazeOpacity(prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT));

        final String presetColorsString = NbPreferences.forModule(GraphPreferenceKeys.class)
                .get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);

        // set the colors here - below generates a list of colored icons.
        final List<Color> colors = new ArrayList<>();
        for (final String currentColor : presetColorsString.split(";")) {
            if (StringUtils.isNotBlank(currentColor) && !"null".equals(currentColor)) {
                final int r = Integer.valueOf(currentColor.substring(1, 3), 16);
                final int g = Integer.valueOf(currentColor.substring(3, 5), 16);
                final int b = Integer.valueOf(currentColor.substring(5, 7), 16);
                colors.add(new Color(r, g, b));
            } else {
                colors.add(null);
            }
        }
        graphOptionsPanel.setPresetColors(colors);
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
                final GraphOptionsPanel graphOptionsPanel = getPanel();

                prefs.putInt(GraphPreferenceKeys.BLAZE_SIZE, graphOptionsPanel.getBlazeSize());
                prefs.putInt(GraphPreferenceKeys.BLAZE_OPACITY, graphOptionsPanel.getBlazeOpacity());
            }
        }
    }

    @Override
    public void cancel() {
        // Method intentionally left blank
    }

    // Add code to check valid values. may be needed for expansion of this UI manu.
    @Override
    public boolean isValid() {
        return true;
    }

    // Check if the preference values are changed upon adding to the UI menu
    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
        final GraphOptionsPanel graphOptionsPanel = getPanel();
        return !(graphOptionsPanel.getBlazeSize() == prefs.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)
                && graphOptionsPanel.getBlazeOpacity() == prefs.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT));
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private GraphOptionsPanel getPanel() {
        if (panel == null) {
            panel = new GraphOptionsPanel(this);
        }
        return panel;
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.preferences.graph");
    }
}

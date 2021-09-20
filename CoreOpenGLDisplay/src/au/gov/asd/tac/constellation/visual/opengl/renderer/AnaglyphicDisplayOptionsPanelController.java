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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for the anaglyphic display eye colors.
 *
 * @author algol
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#AdvancedOption_DisplayName_AnaglyphicDisplay",
        keywords = "#AdvancedOption_Keywords_AnaglyphicDisplay",
        keywordsCategory = "constellation/AnaglyphicDisplay",
        position=1100
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_AnaglyphicDisplay=Anaglyphic Display",
    "AdvancedOption_Keywords_AnaglyphicDisplay=anaglyphic"
})
public final class AnaglyphicDisplayOptionsPanelController extends OptionsPanelController {

    private AnaglyphicDisplayPanel thePanel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // This is a map from the color names to their RGB color bits used by glColorMask.
    //
    private static final Map<String, boolean[]> COLOR_BITS = Map.of(
            "Blue",    new boolean[]{false, false, true,  true},
            "Cyan",    new boolean[]{false, true,  true,  true},
            "Green",   new boolean[]{false, true,  false, true},
            "Magenta", new boolean[]{true,  false, true,  true},
            "Red",     new boolean[]{true,  false, false, true},
            "Yellow",  new boolean[]{true,  true,  false, true}
    );

    public static boolean[] getColorMask(final String color) {
        if (COLOR_BITS.containsKey(color)) {
            return COLOR_BITS.get(color);
        }

        throw new IllegalArgumentException(String.format("Color %s is not valid", color));
    }

    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(AnaglyphicDisplayPreferenceKeys.class);
        SwingUtilities.invokeLater(() -> {
            final AnaglyphicDisplayPanel panel = getPanel();
            panel.setLeftColor(prefs.get(AnaglyphicDisplayPreferenceKeys.LEFT_COLOR, AnaglyphicDisplayPreferenceKeys.LEFT_COLOR_DEFAULT));
            panel.setRightColor(prefs.get(AnaglyphicDisplayPreferenceKeys.RIGHT_COLOR, AnaglyphicDisplayPreferenceKeys.RIGHT_COLOR_DEFAULT));
        });
    }

    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);

                final Preferences prefs = NbPreferences.forModule(AnaglyphicDisplayPreferenceKeys.class);
                final AnaglyphicDisplayPanel panel = getPanel();
                prefs.put(AnaglyphicDisplayPreferenceKeys.LEFT_COLOR, panel.getLeftColor());
                prefs.put(AnaglyphicDisplayPreferenceKeys.RIGHT_COLOR, panel.getRightColor());
            }
        }
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        final AnaglyphicDisplayPanel panel = getPanel();
        final String leftEye = panel.getLeftColor();
        final String rightEye = panel.getRightColor();

        final boolean valid = !leftEye.equals(rightEye);
        return !leftEye.equals(rightEye);
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(AnaglyphicDisplayPreferenceKeys.class);
        final AnaglyphicDisplayPanel panel = getPanel();
        final String leftColor = panel.getLeftColor();
        final String rightColor = panel.getRightColor();
        final boolean leftChanged = !leftColor.equals(prefs.get(AnaglyphicDisplayPreferenceKeys.LEFT_COLOR, AnaglyphicDisplayPreferenceKeys.LEFT_COLOR_DEFAULT));
        final boolean rightChanged = !rightColor.equals(prefs.get(AnaglyphicDisplayPreferenceKeys.RIGHT_COLOR, AnaglyphicDisplayPreferenceKeys.LEFT_COLOR_DEFAULT));
        
        return leftChanged || rightChanged;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private AnaglyphicDisplayPanel getPanel() {
        if (thePanel == null) {
            thePanel = new AnaglyphicDisplayPanel(this);
        }
        return thePanel;
    }
}

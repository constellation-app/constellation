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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import au.gov.asd.tac.constellation.preferences.ApplicationOptionsPanelController;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.visual.opengl.renderer.AnaglyphicDisplayAction")
@ActionRegistration(displayName = "#CTL_AnaglyphicDisplayAction", surviveFocusChange = true, lazy = false)
@ActionReference(path = "Menu/Experimental/Display", position = 1200)
@Messages("CTL_AnaglyphicDisplayAction=Anaglyphic")
public final class AnaglyphicDisplayAction extends AbstractAction implements Presenter.Menu {

    public static final class EyeColorMask {
        boolean red;
        boolean green;
        boolean blue;

        void set(final boolean[] mask) {
            red = mask[0];
            green = mask[1];
            blue = mask[2];
        }
    }

    private final JCheckBoxMenuItem menuItem;

    // Not a particularly nice way of making a global state available,
    // but it has to be fast because it's used at every call to display().
    private static final AtomicBoolean displayAnaglyph = new AtomicBoolean(false);

    // Also quite ugly, but these are also used at every call to display() when anaglyphic display is active.
    //
    private static final EyeColorMask LEFT_EYE = new EyeColorMask();
    private static final EyeColorMask RIGHT_EYE = new EyeColorMask();

    public AnaglyphicDisplayAction() {
        menuItem = new JCheckBoxMenuItem(this);
        menuItem.setSelected(displayAnaglyph.get());
    }

    public static boolean isAnaglyphicDisplay() {
        return displayAnaglyph.get();
    }

    public static EyeColorMask getLeftColorMask() {
        return LEFT_EYE;
    }

    public static EyeColorMask getRightColorMask() {
        return RIGHT_EYE;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        displayAnaglyph.set(!displayAnaglyph.get());
        menuItem.setSelected(displayAnaglyph.get());

        if (displayAnaglyph.get()) {
            // Get the current color options and convert them to bit masks.
            //
            final Preferences prefs = NbPreferences.forModule(GraphPreferenceKeys.class);
            final String leftColor = prefs.get(GraphPreferenceKeys.LEFT_COLOR, GraphPreferenceKeys.LEFT_COLOR_DEFAULT);
            final String rightColor = prefs.get(GraphPreferenceKeys.RIGHT_COLOR, GraphPreferenceKeys.RIGHT_COLOR_DEFAULT);
            LEFT_EYE.set(ApplicationOptionsPanelController.getColorMask(leftColor));
            RIGHT_EYE.set(ApplicationOptionsPanelController.getColorMask(rightColor));
        }
    }

    @Override
    public JMenuItem getMenuPresenter() {
        putValue(Action.NAME, Bundle.CTL_AnaglyphicDisplayAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_AnaglyphicDisplayAction());

        return menuItem;
    }
}

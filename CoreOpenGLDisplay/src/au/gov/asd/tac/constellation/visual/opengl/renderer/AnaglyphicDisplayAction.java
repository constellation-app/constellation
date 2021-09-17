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

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.visual.opengl.renderer.AnaglyphicDisplayAction")
@ActionRegistration(displayName = "#CTL_AnaglyphicDisplayAction", surviveFocusChange = true, lazy = false)
@ActionReference(path = "Menu/Experimental/Display", position = 1200)
@Messages("CTL_AnaglyphicDisplayAction=Anaglyphic")
public final class AnaglyphicDisplayAction extends AbstractAction implements Presenter.Menu {

    private final JCheckBoxMenuItem menuItem;

    // Not a particularly nice way of making a global state available,
    // but it has to be fast because it's used at every call to display().
    private static final AtomicBoolean displayAnaglyph = new AtomicBoolean(false);

    public AnaglyphicDisplayAction() {
        menuItem = new JCheckBoxMenuItem(this);
        menuItem.setSelected(displayAnaglyph.get());
    }

    public static boolean isAnaglyphicDisplay() {
        return displayAnaglyph.get();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        displayAnaglyph.set(!displayAnaglyph.get());
        menuItem.setSelected(displayAnaglyph.get());
    }

    @Override
    public JMenuItem getMenuPresenter() {
        putValue(Action.NAME, Bundle.CTL_AnaglyphicDisplayAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_AnaglyphicDisplayAction());

        return menuItem;
    }
}

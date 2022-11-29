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
package au.gov.asd.tac.constellation.views.find.gui;

import au.gov.asd.tac.constellation.graph.utilities.widgets.IconChooser;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * A PropertyEditor for icons.
 *
 * @author algol
 */
@Messages({"MSG_SelectIcon=Select icon"})
public final class IconPropertyEditor extends PropertyEditorSupport implements ActionListener {

    private static final Color MULTIPLE_COLOR = new Color(0, 0, 127);
    private IconChooser chooser;

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(final Graphics g, final Rectangle r) {
        final String text = (String) getValue();
        if (text != null) {
            int px = 0;

            ((Graphics2D) g).setRenderingHints(IconUtilities.getHints());
            final FontMetrics fm = g.getFontMetrics();
            g.drawString(text, r.x + px, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
        } else {
            final int px = 0;
            ((Graphics2D) g).setRenderingHints(IconUtilities.getHints());
            final FontMetrics fm = g.getFontMetrics();
            g.setColor(MULTIPLE_COLOR);
            g.drawString("«multiple selection»", r.x + px, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        chooser = new IconChooser(IconManager.getIcons(), (String) getValue());
        final DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(IconPropertyEditor.class, "MSG_SelectIcon"), true, this);

        return DialogDisplayer.getDefault().createDialog(dd);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("OK".equals(e.getActionCommand())) {
            final String name = chooser.getSelectedIconName();
            if (name != null) {
                setValue(chooser.getSelectedIconName());
            }
        }
    }
}

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

import au.gov.asd.tac.constellation.graph.utilities.widgets.GraphColorChooser;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import javax.swing.JColorChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * color editor
 *
 * @author algol
 */
@Messages({"MSG_SelectColor=Select color"})
public final class ColorPropertyEditor extends PropertyEditorSupport implements ActionListener {

    private static final Color MULTIPLE_COLOR = new Color(0, 0, 127);
    private final JColorChooser chooser;

    public ColorPropertyEditor() {
        chooser = new GraphColorChooser();
    }

    /**
     * We don't want the user to edit this manually.
     *
     * @return Null to indicate no manual editing.
     */
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
        final ConstellationColor cv = (ConstellationColor) getValue();
        if (cv != null) {
            final Color gcolor = g.getColor();
            final Color color = new Color(cv.getRed(), cv.getGreen(), cv.getBlue(), cv.getAlpha());
            int px = 0;
            g.drawRect(r.x, r.y + r.height / 2 - 5, 10, 10);
            g.setColor(color);
            g.fillRect(r.x + 1, r.y + r.height / 2 - 4, 9, 9);
            px = 18;

            final String text = cv.getName() != null ? cv.getName() : String.format("r=%s g=%s b=%s a=%s", formatFloat(cv.getRed()), formatFloat(cv.getGreen()), formatFloat(cv.getBlue()), formatFloat(cv.getAlpha()));
            ((Graphics2D) g).setRenderingHints(IconUtilities.getHints());
            final FontMetrics fm = g.getFontMetrics();
            g.setColor(gcolor);
            g.drawString(text, r.x + px, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
        } else {
            final int px = 0;
            ((Graphics2D) g).setRenderingHints(IconUtilities.getHints());
            final FontMetrics fm = g.getFontMetrics();
            g.setColor(MULTIPLE_COLOR);
            g.drawString("«multiple selection»", r.x + px, r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent());
        }
    }

    private String formatFloat(final float f) {
        String s = String.format("%5.3f", f);
        while (s.endsWith("00")) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        final ConstellationColor cv = (ConstellationColor) getValue();
        final Color currentColor = cv != null ? new Color(cv.getRed(), cv.getGreen(), cv.getBlue(), cv.getAlpha()) : Color.BLUE;
        chooser.setColor(currentColor);
        final DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(ColorPropertyEditor.class, "MSG_SelectColor"), true, this);

        return DialogDisplayer.getDefault().createDialog(dd);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("OK".equals(e.getActionCommand())) {
            final Color color = chooser.getColor();
            final ConstellationColor cv = ConstellationColor.fromJavaColor(color);
            setValue(cv);
        }
    }
}

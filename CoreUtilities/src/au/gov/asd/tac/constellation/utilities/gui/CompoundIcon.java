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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * THe CompoundIcon will paint two or more Icons as a single Icon. The Icons are
 * painted in the order in which they are added.
 *
 * @author cygnus_x-1
 */
public class CompoundIcon implements Icon {

    public enum Axis {

        X_AXIS,
        Y_AXIS,
        Z_AXIS;
    }

    private static final float CENTER = 0.5f;
    private Icon[] icons;
    private Axis axis;
    private int gap;
    private float alignmentX = CENTER;
    private float alignmentY = CENTER;

    public CompoundIcon(final Icon... icons) {
        this(Axis.X_AXIS, icons);
    }

    public CompoundIcon(final Axis axis, final Icon... icons) {
        this(axis, 0, icons);
    }

    public CompoundIcon(final Axis axis, final int gap, final Icon... icons) {
        this(axis, gap, CENTER, CENTER, icons);
    }

    public CompoundIcon(final Axis axis, final int gap, final float alignmentX, final float alignmentY, final Icon... icons) {
        this.axis = axis;
        this.gap = gap;
        this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
        this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;

        for (int index = 0; index < icons.length; index++) {
            if (icons[index] == null) {
                throw new IllegalArgumentException(String.format("Icon (%d) cannot be null", index));
            }
        }
        this.icons = icons;
    }

    public final Axis getAxis() {
        return axis;
    }

    public final int getGap() {
        return gap;
    }

    public final float getAlignmentX() {
        return alignmentX;
    }

    public final float getAlignmentY() {
        return alignmentY;
    }

    public final int getIconCount() {
        return icons.length;
    }

    public final Icon getIcon(final int index) {
        return icons[index];
    }

    @Override
    public final int getIconWidth() {
        int width = 0;
        if (axis == Axis.X_AXIS) {
            width += (icons.length - 1) * gap;
            for (final Icon icon : icons) {
                width += icon.getIconWidth();
            }
        } else {
            for (final Icon icon : icons) {
                width = Math.max(width, icon.getIconWidth());
            }
        }

        return width;
    }

    @Override
    public final int getIconHeight() {
        int height = 0;
        if (axis == Axis.Y_AXIS) {
            height += (icons.length - 1) * gap;
            for (final Icon icon : icons) {
                height += icon.getIconHeight();
            }
        } else {
            for (final Icon icon : icons) {
                height = Math.max(height, icon.getIconHeight());
            }
        }

        return height;
    }

    @Override
    public final void paintIcon(final Component c, final Graphics g, int x, int y) {
        if (axis == Axis.X_AXIS) {
            final int height = getIconHeight();
            for (final Icon icon : icons) {
                final int iconY = getOffset(height, icon.getIconHeight(), alignmentY);
                icon.paintIcon(c, g, x, y + iconY);
                x += icon.getIconWidth() + gap;
            }
        } else if (axis == Axis.Y_AXIS) {
            final int width = getIconWidth();
            for (final Icon icon : icons) {
                final int iconX = getOffset(width, icon.getIconWidth(), alignmentX);
                icon.paintIcon(c, g, x + iconX, y);
                y += icon.getIconHeight() + gap;
            }
        } else {
            final int width = getIconWidth();
            final int height = getIconHeight();
            for (final Icon icon : icons) {
                final int iconX = getOffset(width, icon.getIconWidth(), alignmentX);
                final int iconY = getOffset(height, icon.getIconHeight(), alignmentY);
                icon.paintIcon(c, g, x + iconX, y + iconY);
            }
        }
    }

    private int getOffset(final int maxValue, final int iconValue, final float alignment) {
        return Math.round((maxValue - iconValue) * alignment);
    }
}

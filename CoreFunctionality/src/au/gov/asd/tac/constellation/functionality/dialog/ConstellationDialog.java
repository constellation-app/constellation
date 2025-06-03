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
package au.gov.asd.tac.constellation.functionality.dialog;

import javafx.embed.swing.JFXPanel;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * A JavaFX dialog that will stay on top. Use this instead of extending a
 * {@code Stage}
 * <p>
 * To make JavaFx dialogs stay on top, they are wrapped inside a Swing dialog.
 * With the update to ControlsFx this work around may no longer be required.
 *
 * @author arcturus
 */
public abstract class ConstellationDialog {

    protected final JFXPanel fxPanel;
    protected JDialog dialog;
    private static final java.awt.Color TRANSPARENT = new java.awt.Color(0, 0, 0, 0);

    protected double mouseOrigX = 0;
    protected double mouseOrigY = 0;

    protected ConstellationDialog() {
        fxPanel = new JFXPanel();
        final BoxLayout layout = new BoxLayout(fxPanel, BoxLayout.Y_AXIS);
        fxPanel.setLayout(layout);
        fxPanel.setOpaque(false);
        fxPanel.setBackground(TRANSPARENT);
    }

    /**
     * Shows this dialog with no title.
     */
    public void showDialog() {
        showDialog(null);
    }

    /**
     * Shows this dialog.
     *
     * @param title The title of the dialog.
     */
    public void showDialog(final String title) {
        SwingUtilities.invokeLater(() -> {
            final DialogDescriptor dd = new DialogDescriptor(fxPanel, title);
            dd.setOptions(new Object[0]);
            dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
            dialog.setEnabled(true);
            dialog.setVisible(true);
        });
    }

    /**
     * Hides this dialog.
     */
    public void hideDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
    }
}

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
package au.gov.asd.tac.constellation.views.namedselection;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Class that manages the registered shortcuts for creating and retrieving named
 * selections.
 * <p>
 * Any non-modified registered shortcuts must be paired by creating a
 * Control+[x] and Control+Shift+[x] combination where [x] represents the
 * hotkey.
 *
 * @author betelgeuse
 */
@ActionID(category = "Options", id = "au.gov.asd.tac.constellation.functionality.select.named.NamedSelectionShortcuts")
@ActionRegistration(displayName = "#CTL_NamedSelectionShortcuts", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-1"),
    @ActionReference(path = "Shortcuts", name = "D-1"),
    @ActionReference(path = "Shortcuts", name = "DS-2"),
    @ActionReference(path = "Shortcuts", name = "D-2"),
    @ActionReference(path = "Shortcuts", name = "DS-3"),
    @ActionReference(path = "Shortcuts", name = "D-3"),
    @ActionReference(path = "Shortcuts", name = "DS-4"),
    @ActionReference(path = "Shortcuts", name = "D-4"),
    @ActionReference(path = "Shortcuts", name = "DS-5"),
    @ActionReference(path = "Shortcuts", name = "D-5"),
    @ActionReference(path = "Shortcuts", name = "DS-6"),
    @ActionReference(path = "Shortcuts", name = "D-6"),
    @ActionReference(path = "Shortcuts", name = "DS-7"),
    @ActionReference(path = "Shortcuts", name = "D-7"),
    @ActionReference(path = "Shortcuts", name = "DS-8"),
    @ActionReference(path = "Shortcuts", name = "D-8"),
    @ActionReference(path = "Shortcuts", name = "DS-9"),
    @ActionReference(path = "Shortcuts", name = "D-9")
})
@Messages("CTL_NamedSelectionShortcuts=Named Selection: Shortcuts")
public class NamedSelectionShortcuts extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent e) {
        /*
         * Determine whether we have to write a new named selection (ie Control key was held),
         * or to read a previously saved named selection (ie Control was not held).
         */
        String hotkey = e.getActionCommand();

        // Create named selection combination:
        if (hotkey.startsWith("CS-")) {
            // Create a new NamedSelection for Hotkey:
            hotkey = hotkey.replace("CS-", "");

            NamedSelectionManager.getDefault().createNamedSelectionFromHotkey(hotkey);
        } else { // Recall named selection combination:
            // Remove the control character
            hotkey = hotkey.replace("C-", "");

            NamedSelectionManager.getDefault().recallSelectionFromHotkey(hotkey);
        }
    }
}

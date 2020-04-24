/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.graph.interaction.plugins.io.Autosaver;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Debugging autosave.
 *
 * @author algol
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.AutosaveTestAction")
@ActionRegistration(displayName = "#CTL_AutosaveTestAction")
@ActionReference(path = "Menu/Experimental/Developer", position = 0)
@Messages("CTL_AutosaveTestAction=Test Autosave")
public final class AutosaveTestAction extends AbstractAction {

    @Override
    public void actionPerformed(final ActionEvent e) {
        // Call the same autosave method that the scheduled task uses.
        Autosaver.runAutosave();
    }
}

/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.log;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.modules.Places;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Help",
        id = "au.gov.asd.tac.constellation.utilities.log.LogAction"
)
@ActionRegistration(
        displayName = "#CTL_LogAction"
)
@ActionReference(path = "Menu/Help", position = 940)
@Messages("CTL_LogAction=Show Logs")
public final class LogAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final File userDir = Places.getUserDirectory();
        if (userDir == null) {
            return;
        }
        final File f = new File(userDir, "/var/log/messages.log");
        final LogViewerSupport p = new LogViewerSupport(f, "Logs");
        try {
            p.showLogViewer();
        } catch (IOException ex) {
            Logger.getLogger(LogAction.class.getName()).log(Level.SEVERE, "Unable to show the log file", ex);
        }
    }
}

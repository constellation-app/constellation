/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.webserver.StartRestServerAction")
@ActionRegistration(displayName = "#CTL_StartRestServerAction",
        iconBase = "au/gov/asd/tac/constellation/webserver/resources/start.png")
@ActionReference(path = "Menu/Tools", position = 1500, separatorBefore = 1499)
@Messages("CTL_StartRestServerAction=Start REST Server")
public final class StartRestServerAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        // title should be assigned befroe WebServer.start
        final String title = WebServer.isRunning() ? "Web server already started" : "Web server started";
        final int port = WebServer.start();
        final String msg = String.format("External scripting listening on port %d", port);
        NotificationDisplayer.getDefault().notify(title,
                UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                msg,
                null
        );
    }
}

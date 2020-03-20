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
package au.gov.asd.tac.constellation.webserver.transport;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Start and stop the file listener.
 *
 * @author algol
 * @author rsabhi modified
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.webserver.transport.StartStopFileListenerAction")
@ActionRegistration(
        displayName = "#CTL_StartFileListenerAction",
        iconBase = "au/gov/asd/tac/constellation/webserver/transport/resources/filelistener_off.png",
        surviveFocusChange = true,
        lazy = true
)
@ActionReference(path = "Menu/Tools", position = 1550)
@Messages({
    "CTL_StartFileListenerAction=Start File Listener",
    "CTL_StopFileListenerAction=Stop File Listener"
})
public final class StartStopFileListenerAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(StartStopFileListenerAction.class.getName());

    private static final String RESOURCE_ON = "resources/filelistener_on.png";
    private static final String RESOURCE_OFF = "resources/filelistener_off.png";
    private static final ImageIcon ICON_ON = new ImageIcon(StartStopFileListenerAction.class.getResource(RESOURCE_ON));
    private static final ImageIcon ICON_OFF = new ImageIcon(StartStopFileListenerAction.class.getResource(RESOURCE_OFF));

    private static final long JOIN_WAIT = 1000;

    private boolean listener_on;
    private FileListener fileListener;
    private Thread listenerRunner;

    public StartStopFileListenerAction() {
        listener_on = false;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        if(!listener_on) {
            try {
                fileListener = new FileListener();
                listenerRunner = new Thread(fileListener);
                listenerRunner.start();

                putValue(Action.NAME, NbBundle.getMessage(StartStopFileListenerAction.class, "CTL_StopFileListenerAction"));
                putValue(Action.SMALL_ICON, ICON_ON);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, "When starting file listener", ex);
                Exceptions.printStackTrace(ex);
            }

        } else {
            fileListener.stop();
            try {
                listenerRunner.join(JOIN_WAIT);

                putValue(Action.NAME, NbBundle.getMessage(StartStopFileListenerAction.class, "CTL_StartFileListenerAction"));
                putValue(Action.SMALL_ICON, ICON_OFF);
            } catch(final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "When stopping file listener", ex);
                Exceptions.printStackTrace(ex);
            }
        }

        listener_on = !listener_on;
    }
}

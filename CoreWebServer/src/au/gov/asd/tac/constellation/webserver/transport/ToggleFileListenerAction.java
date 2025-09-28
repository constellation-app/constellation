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
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Start and stop the file listener.
 *
 * @author algol
 * @author rsabhi modified
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.webserver.transport.ToggleFileListenerAction")
@ActionRegistration(displayName = "#CTL_StartFileListenerAction",
        iconBase = "au/gov/asd/tac/constellation/webserver/transport/resources/stopFileListener.png",
        surviveFocusChange = true, lazy = true)
@ActionReference(path = "Menu/Tools", position = 1550)
@Messages({
    "CTL_StartFileListenerAction=Start File Listener",
    "CTL_StopFileListenerAction=Stop File Listener"
})
public final class ToggleFileListenerAction extends AbstractAction {
    
    private static final Logger LOGGER = Logger.getLogger(ToggleFileListenerAction.class.getName());

    private static final String ICON_ON_RESOURCE = "resources/startFileListener.png";
    private static final String ICON_OFF_RESOURCE = "resources/stopFileListener.png";
    private static final ImageIcon ICON_ON = new ImageIcon(ToggleFileListenerAction.class.getResource(ICON_ON_RESOURCE));
    private static final ImageIcon ICON_OFF = new ImageIcon(ToggleFileListenerAction.class.getResource(ICON_OFF_RESOURCE));
    private static final long JOIN_WAIT = 1000;

    private boolean listenerOn;
    private FileListener fileListener;
    private Thread listenerRunner;

    public ToggleFileListenerAction() {
        listenerOn = false;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (!listenerOn) {
            try {
                fileListener = new FileListener();
                listenerRunner = new Thread(fileListener);
                listenerRunner.start();
                putValue(Action.NAME, NbBundle.getMessage(ToggleFileListenerAction.class, "CTL_StopFileListenerAction"));
                putValue(Action.SMALL_ICON, ICON_ON);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

        } else {
            try {
                fileListener.stop();
                listenerRunner.join(JOIN_WAIT);
                putValue(Action.NAME, NbBundle.getMessage(ToggleFileListenerAction.class, "CTL_StartFileListenerAction"));
                putValue(Action.SMALL_ICON, ICON_OFF);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                Thread.currentThread().interrupt();
            }
        }

        listenerOn = !listenerOn;
    }
}

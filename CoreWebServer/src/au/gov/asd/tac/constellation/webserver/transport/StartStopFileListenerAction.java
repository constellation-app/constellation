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
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.BooleanStateAction;

/**
 * Start and stop the file listener.
 *
 * @author algol
 * @author rsabhi modified
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.filetransport.StartStopFileListenerAction")
@ActionRegistration(displayName = "#CTL_StartStopFileListenerAction", iconBase = "au/gov/asd/tac/constellation/webserver/resources/filelistener_off.png", surviveFocusChange = true, lazy = true)
@ActionReference(path = "Menu/Tools", position = 1550)
@Messages("CTL_StartStopFileListenerAction=Start/Stop File Listener")
public final class StartStopFileListenerAction extends BooleanStateAction {

    private static final String ICON_ON = "au/gov/asd/tac/constellation/filetransport/filelistener_on.png";
    private static final String ICON_OFF = "au/gov/asd/tac/constellation/filetransport/filelistener_off.png";
    private static final long JOIN_WAIT = 1000;

    private boolean listener_on;
    private FileListener fileListener;
    private Thread listenerRunner;

    @Override
    public void actionPerformed(final ActionEvent ev) {
        super.actionPerformed(ev);
        if (!listener_on) {
            try {
                fileListener = new FileListener();
                listenerRunner = new Thread(fileListener);
                listenerRunner.start();

                // This will trigger a call to iconResource() which will set the icon, so no point doing it twice; hence setting it to null.
                putValue(Action.SMALL_ICON, null);

                setBooleanState(!listener_on);
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        } else {
            fileListener.stop();
            try {
                listenerRunner.join(JOIN_WAIT);

                // This will trigger a call to iconResource() which will set the icon, so no point doing it twice; hence setting it to null.
                putValue(Action.SMALL_ICON, null);

                setBooleanState(!listener_on);
            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        listener_on = !listener_on;

    }

    @Override
    protected void initialize() {
        listener_on = false;
        super.initialize();
        setBooleanState(listener_on);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StartStopFileListenerAction.class, "CTL_StartStopFileListenerAction");
    }

    @Override
    protected String iconResource() {
        return listener_on ? ICON_ON : ICON_OFF;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}

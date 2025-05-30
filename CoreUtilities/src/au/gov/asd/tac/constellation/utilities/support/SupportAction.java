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
package au.gov.asd.tac.constellation.utilities.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Action class to allows the user to provide feedback/info to the constellation
 * team. The action is added to the help menu in the application, and the
 * information is sent via email.
 *
 * @author algol
 */
@ActionID(
        category = "Help",
        id = "au.gov.asd.tac.constellation.utilities.support.SupportAction")
@ActionRegistration(
        displayName = "#CTL_SupportAction",
        iconBase = "au/gov/asd/tac/constellation/utilities/support/submitTicket.png"
)
@ActionReference(path = "Menu/Help", position = 900)
@Messages("CTL_SupportAction=Submit a Ticket")
public final class SupportAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final SupportHandler helpHandler = Lookup.getDefault().lookup(SupportHandler.class);
        helpHandler.supportAction();
    }
}

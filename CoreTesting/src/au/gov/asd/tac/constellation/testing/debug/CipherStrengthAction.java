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
package au.gov.asd.tac.constellation.testing.debug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.testing.debug.CipherStrengthAction")
@ActionRegistration(displayName = "#CTL_CipherStrengthAction",
        iconBase = "au/gov/asd/tac/constellation/testing/debug/cipherStrength.png")
@ActionReference(path = "Menu/Help", position = 1450)
@Messages("CTL_CipherStrengthAction=Cipher Strength")
public final class CipherStrengthAction implements ActionListener {
    
    private static final Logger LOGGER = Logger.getLogger(CipherStrengthAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            final int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            final String msg = String.format("Maximum key length: %s", maxKeyLen == Integer.MAX_VALUE ? "unlimited" : Integer.toString(maxKeyLen));

            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}

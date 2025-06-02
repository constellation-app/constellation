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
package au.gov.asd.tac.constellation.testing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Deliberately throw an Exception.
 * <p>
 * This exists to exercise the error handling capability.
 *
 * @author algol
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.CreateExceptionAction")
@ActionRegistration(displayName = "#CTL_CreateExceptionAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Developer", position = 0)
})
@Messages("CTL_CreateExceptionAction=Throw a RuntimeException")
public final class RuntimeExceptionAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        throw new RuntimeException(String.format("Runtime exception successfully thrown at %s", new Date()));
    }
}

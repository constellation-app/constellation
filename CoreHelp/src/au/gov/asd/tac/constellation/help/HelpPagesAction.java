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
package au.gov.asd.tac.constellation.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * Display the default table of contents for help pages
 *
 * @author aldebaran30701
 */
@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.help.HelpPagesAction")
@ActionRegistration(displayName = "#CTL_HelpPagesAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 2)
})
@NbBundle.Messages("CTL_HelpPages=Help Pages")
public class HelpPagesAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        new HelpCtx("").display();
    }

}

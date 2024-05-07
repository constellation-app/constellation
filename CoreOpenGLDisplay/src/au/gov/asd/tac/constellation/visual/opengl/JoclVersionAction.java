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
package au.gov.asd.tac.constellation.visual.opengl;

import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.jar.Attributes;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.visual.opengl.JoclVersionAction")
@ActionRegistration(displayName = "#CTL_JoclVersionAction",
        iconBase = "au/gov/asd/tac/constellation/visual/opengl/versionsJOCL.png")
@ActionReference(path = "Menu/Help", position = 1401)
@Messages("CTL_JoclVersionAction=JOCL Version")
public final class JoclVersionAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final com.jogamp.opencl.JoclVersion jv = com.jogamp.opencl.JoclVersion.getInstance();
        final Set<?> names = jv.getAttributeNames();
        final ArrayList<String> lines = new ArrayList<>();
        for (final Object name : names) {
            lines.add(String.format("%s: %s%n", name, jv.getAttribute((Attributes.Name) name)));
        }

        Collections.sort(lines);

        final StringBuilder sb = new StringBuilder();
        sb.append("JOCL Attributes\n");
        for (final String line : lines) {
            sb.append(line);
        }

        final InfoTextPanel itp = new InfoTextPanel(sb.toString());
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(itp);
        msg.setTitle(Bundle.CTL_JoclVersionAction());
        DialogDisplayer.getDefault().notify(msg);
    }
}

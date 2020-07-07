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
package au.gov.asd.tac.constellation.visual.opengl;

import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.JoglVersion;
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

@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.visual.opengl.JoglVersionAction")
@ActionRegistration(displayName = "#CTL_JoglVersionAction",
        iconBase = "au/gov/asd/tac/constellation/visual/opengl/versionsJOGL.png")
@ActionReference(path = "Menu/Help", position = 1400, separatorBefore = 1350)
@Messages("CTL_JoglVersionAction=JOGL Version")
public final class JoglVersionAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final GLProfile glProfile = SharedDrawable.getGLProfile();

        // Create GLContext and trigger GLContext object creation and native realization.
        final GLAutoDrawable drawable = GLDrawableFactory.getFactory(glProfile).createDummyAutoDrawable(null, true, new GLCapabilities(glProfile), null);
        drawable.display();
        drawable.getContext().makeCurrent();

        final GL gl = drawable.getGL().getGL3();

        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("OpenGL version: %s\n", gl.glGetString(GL.GL_VERSION)));
        sb.append(String.format("Vendor: %s\n", gl.glGetString(GL.GL_VENDOR)));
        sb.append(String.format("Renderer: %s\n", gl.glGetString(GL.GL_RENDERER)));
        if (gl instanceof GL2ES2) {
            sb.append(String.format("Shading language version: %s\n", gl.glGetString(GL2ES2.GL_SHADING_LANGUAGE_VERSION)));
        }

        final JoglVersion jv = JoglVersion.getInstance();
        final Set<?> names = jv.getAttributeNames();
        final ArrayList<String> lines = new ArrayList<>();
        for (final Object name : names) {
            lines.add(String.format("%s: %s\n", name, jv.getAttribute((Attributes.Name) name)));
        }

        Collections.sort(lines);

        sb.append("\nJOGL Attributes\n");
        for (final String line : lines) {
            sb.append(line);
        }

        sb.append("\nGL Strings\n");
        JoglVersion.getGLStrings(gl, sb, true);
        sb.append(SeparatorConstants.NEWLINE);

        final InfoTextPanel itp = new InfoTextPanel(sb.toString());
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(itp);
        msg.setTitle(Bundle.CTL_JoglVersionAction());
        DialogDisplayer.getDefault().notify(msg);
    }
}

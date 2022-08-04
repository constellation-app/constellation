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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Utility class for debugging and diagnostics relating to OpenGL information.
 * This includes things like openGL capabilities, extensions, shader language
 * versions, etc.
 *
 * @author algol
 */
public class GLInfo {

    private static final Logger LOGGER = Logger.getLogger(GLInfo.class.getName());

    public static final String MINIMUM_OPEN_GL_VERSION = "3.3";
    private final String basicInfo;
    private final String extensions;

    /**
     * Generate a user dialogue box to alert the user that the hardware/graphics
     * drivers they are using are incompatible with CONSTELLATION
     *
     * @param drawable - a GLAutoDrawable object currently being displayed on
     * the screen. This may be null in the event of the method being called from
     * a GLException exception handler.
     */
    public static void respondToIncompatibleHardwareOrGL(final GLAutoDrawable drawable) {
        final String basicInfo = drawable == null ? "Not available" : (new GLInfo(drawable.getGL())).getBasicInfo();
        final String errorMessage
                = BrandingUtilities.APPLICATION_NAME + " requires a minimum of "
                + "OpenGL version " + MINIMUM_OPEN_GL_VERSION + "\n\n"
                + "This PC has an incompatible graphics card.\n"
                + "Please contact CONSTELLATION support, or use a different PC.\n\n"
                + "This PC's details:\n\n" + basicInfo;

        new Thread(() -> {
            final InfoTextPanel itp = new InfoTextPanel(errorMessage);
            final NotifyDescriptor d = new NotifyDescriptor.Message(itp, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }).start();
    }

    public static void printGLCapabilities(final GL3 gl) {
        final int[] v = new int[10];
        gl.glGetIntegerv(GL.GL_MAX_RENDERBUFFER_SIZE, v, 0);
        gl.glGetIntegerv(GL2ES2.GL_MAX_VERTEX_ATTRIBS, v, 1);
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, v, 2);
        gl.glGetIntegerv(GL2GL3.GL_MAX_RECTANGLE_TEXTURE_SIZE, v, 3);
        gl.glGetIntegerv(GL2ES3.GL_MAX_TEXTURE_BUFFER_SIZE, v, 4);
        gl.glGetIntegerv(GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, v, 5);
        gl.glGetIntegerv(GL2ES2.GL_MAX_3D_TEXTURE_SIZE, v, 6);
        gl.glGetIntegerv(GL2ES3.GL_MAX_ARRAY_TEXTURE_LAYERS, v, 7);
        gl.glGetIntegerv(GL2ES2.GL_MAX_DRAW_BUFFERS, v, 8);
        gl.glGetIntegerv(GL2ES2.GL_MAX_COLOR_ATTACHMENTS, v, 9);
        final StringBuilder b = new StringBuilder();
        b.append(String.format("GL: MAX_RENDERBUFFER_SIZE %d%n", v[0]));
        b.append(String.format("GL: MAX_VERTEX_ATTRIBS %d%n", v[1]));
        b.append(String.format("GL: MAX_TEXTURE_SIZE %d%n", v[2]));
        b.append(String.format("GL: MAX_RECTANGLE_TEXTURE_SIZE %d%n", v[3]));
        b.append(String.format("GL: MAX_TEXTURE_BUFFER_SIZE %d%n", v[4]));
        b.append(String.format("GL: MAX_COMBINED_TEXTURE_IMAGE_UNITS %d%n", v[5]));
        b.append(String.format("GL: MAX_3D_TEXTURE_SIZE %d%n", v[6]));
        b.append(String.format("GL: MAX_ARRAY_TEXTURE_LAYERS %d%n", v[7]));
        b.append(String.format("GL: MAX_DRAW_BUFFERS %d%n", v[8]));
        b.append(String.format("GL: MAX_COLOR_ATTACHMENTS %d%n", v[9]));
        final String log = b.toString();
        LOGGER.log(Level.INFO, log);
    }

    public GLInfo(final GL gl) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("OpenGL version: %s\n", gl.glGetString(GL.GL_VERSION)));
        sb.append(String.format("Vendor: %s\n", gl.glGetString(GL.GL_VENDOR)));
        sb.append(String.format("Renderer: %s\n", gl.glGetString(GL.GL_RENDERER)));
        if (gl instanceof GL2ES2) {
            sb.append(String.format("Shading language version: %s\n", gl.glGetString(GL2ES2.GL_SHADING_LANGUAGE_VERSION)));
        }

        basicInfo = sb.toString();
        extensions = gl.glGetString(GL.GL_EXTENSIONS);
    }

    /**
     * Is a particular extension supported by the implementation?
     *
     * @param ext The extension to query.
     *
     * @return True if the extension is supported, false if not.
     */
    public boolean isExtensionSupported(final String ext) {
        return extensions != null && extensions.contains(ext);
    }

    /**
     * Get basic information about the implementation.
     *
     * @return A String containing basic information.
     */
    public String getBasicInfo() {
        return basicInfo;
    }

    /**
     * Get the extensions supported by the implementation.
     *
     * @return A String containing the extensions.
     */
    public String getExtensions() {
        return extensions;
    }
}

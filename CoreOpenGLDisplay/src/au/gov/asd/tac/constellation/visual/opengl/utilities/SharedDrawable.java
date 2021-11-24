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
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManager;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManagerBI;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManagerOpenGLController;
import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Utilities;

/**
 * Set up a shared GLAutoDrawable to share textures across multiple
 * graphs/displays.
 * <p>
 * This makes use of the GLSharedContextSetter interface implemented by GLCanvas
 * et al. See the Javadoc for GLSharedContextSetter.
 * <p>
 * Note that the required texture names must be created before any other
 * drawables that share with this one are created, otherwise there is a grave
 * danger of name clashes, and that will just confuse everyone.
 *
 * TODO: {@link GlyphManagerFX} is broken, fix it or remove it.
 *
 * @author algol
 */
public final class SharedDrawable {

    private static GLAutoDrawable sharedDrawable = null;
    private static GL3 gl;
    private static int iconTextureName;

    private static boolean isInitialised = false;

    private static int simpleIconShader;
    private static int vertexIconShader;
    private static int lineShader;
    private static int lineLineShader;
    private static int loopShader;
    private static int nodeLabelShader;
    private static int connectionLabelShader;
    private static int blazeShader;

    private static int labelBackgroundGlyphPosition;

    private static GlyphManagerOpenGLController glyphTextureController;
    private static GlyphManager glyphManager;

    private static final String COULD_NOT_CONTEXT_CURRENT = "Could not make texture context current.";
    private static final String FRAG_COLOR = "fragColor";

    private static final Logger LOGGER = Logger.getLogger(SharedDrawable.class.getName());

    /**
     * No instances for anybody.
     */
    private SharedDrawable() {
    }

    private static void init() {
        if (isInitialised) {
            throw new RenderException("Can't initialise SharedDrawable more than once.");
        }

        // Use own display device.
        final boolean createNewDevice = true;
        sharedDrawable = GLDrawableFactory.getFactory(getGLProfile()).createDummyAutoDrawable(null, createNewDevice, getGLCapabilities(), null);

        // Trigger GLContext object creation and native realization.
        sharedDrawable.display();
        sharedDrawable.getContext().makeCurrent();
        try {
            sharedDrawable.setGL(new DebugGL3(sharedDrawable.getGL().getGL3()));

            // Create a shared texture object for the icon texture array.
            gl = sharedDrawable.getGL().getGL3();
            final int[] textureName = new int[1];
            gl.glGenTextures(1, textureName, 0);
            iconTextureName = textureName[0];

            // Create shared glyph coordinates and glyph image textures using a GlyphManager
            final boolean useMultiFonts = LabelFontsPreferenceKeys.useMultiFontLabels();
            if (useMultiFonts) {
                glyphManager = new GlyphManagerBI(LabelFontsPreferenceKeys.getFontInfo());
            } else {
                glyphManager = null;
            }

            glyphTextureController = new GlyphManagerOpenGLController(glyphManager);

            labelBackgroundGlyphPosition = glyphManager != null ? glyphManager.createBackgroundGlyph(0.5F) : 0;
            glyphTextureController.init(gl);
        } finally {
            sharedDrawable.getContext().release();
            isInitialised = true;
        }
    }

    public static void exportGlyphTextures(final File baseFile) {

        // Ensure that JavaFX is running
        try {
            Platform.startup(() -> {
            });
        } catch (final IllegalStateException ex) {
            /**
             * there isn't a way to tell whether the JavaFX platform is running
             * so we'll absorb this exception and move on.
             */
        }

        if (glyphManager != null) {
            Platform.runLater(() -> {
                String baseFileName = baseFile.getAbsolutePath();
                baseFileName = FilenameUtils.removeExtension(baseFileName);
                for (int page = 0; page < glyphManager.getGlyphPageCount(); page++) {
                    final File outputFile = new File(baseFileName + SeparatorConstants.UNDERSCORE + page + FileExtensionConstants.PNG);
                    try (final OutputStream out = new FileOutputStream(outputFile)) {
                        glyphManager.writeGlyphBuffer(page, out);
                    } catch (IOException ex) {
                        LOGGER.severe(ex.getLocalizedMessage());
                    }
                }
            });
        } else {
            LOGGER.log(Level.INFO, "No glyph textures to export");
        }
    }

    public static GLProfile getGLProfile() {
        final long startTime = System.currentTimeMillis();
        final GLProfile glProfile = GLProfile.get(GLProfile.GL3);
        final long endTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Took {0} seconds to retrieve a GL3 profile", (endTime - startTime) / 1000);

        return glProfile;
    }

    /**
     * Return the capabilities required by the application.
     * <p>
     * This is returned here to ensure that all GL contexts have the same
     * capabilities.
     *
     * @return the capabilities required by the application.
     */
    public static GLCapabilities getGLCapabilities() {
        return new GLCapabilities(getGLProfile());
    }

    public static GLAutoDrawable getSharedAutoDrawable() {
        if (sharedDrawable == null) {
            init();
        }

        return sharedDrawable;
    }

    public static int getLabelBackgroundGlyphPosition() {
        return labelBackgroundGlyphPosition;
    }

    public static int getIconTextureName() {
        return iconTextureName;
    }

    public static GlyphManager getGlyphManager() {
        return glyphManager;
    }

    public static GlyphManagerOpenGLController getGlyphTextureController() {
        return glyphTextureController;
    }

    /**
     * Update the glyph textures on the shared GL context. The update will occur
     * via the GlyphManagerOpenGLController object, which checks its
     * GlyphManager to see if anything has changed, and if so, copies the
     * appropriate data to the open GL textures.
     *
     * @param glCurrent The GL context to switch back to after updating the
     * shared context.
     */
    public static void updateGlyphTextureController(final GL3 glCurrent) {
        if (Utilities.isMac()) {
            glyphTextureController.update(glCurrent);
        } else {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }
                glyphTextureController.update(gl);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }
    }

    /**
     * The simple icon shader draws non-interactive icons.
     *
     * @param glCurrent the current OpenGL context.
     * @param colorTarget
     * @param colorShaderName
     * @param iconShaderName
     * @param iconTarget
     * @return the id of the icon shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getSimpleIconShader(final GL3 glCurrent, final int colorTarget, final String colorShaderName, final int iconTarget, final String iconShaderName) throws IOException {
        if (simpleIconShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/SimpleIcon.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/SimpleIcon.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/SimpleIcon.fs");
                simpleIconShader = GLTools.loadShaderSourceWithAttributes(gl, "SimpleIcon", vp, gp, fp,
                        colorTarget, colorShaderName,
                        iconTarget, iconShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return simpleIconShader;
    }

    /**
     * The vertex icon shader draws icons for vertices on the graph.
     *
     * @param glCurrent the current OpenGL context.
     * @param colorTarget
     * @param colorShaderName
     * @param iconShaderName
     * @param iconTarget
     * @return the id of the icon shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getVertexIconShader(final GL3 glCurrent, final int colorTarget, final String colorShaderName, final int iconTarget, final String iconShaderName) throws IOException {
        if (vertexIconShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/VertexIcon.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/VertexIcon.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/VertexIcon.fs");
                vertexIconShader = GLTools.loadShaderSourceWithAttributes(gl, "VertexIcon", vp, gp, fp,
                        colorTarget, colorShaderName,
                        iconTarget, iconShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return vertexIconShader;
    }

    /**
     * Lines close to the camera are drawn as triangles to provide perspective.
     *
     * @param glCurrent the current OpenGL context.
     * @param colotTarget
     * @param colorShaderName
     * @param connectionInfoTarget
     * @param connectionInfoShaderName
     * @return the id of the line shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getLineShader(final GL3 glCurrent, final int colotTarget, final String colorShaderName, final int connectionInfoTarget, final String connectionInfoShaderName) throws IOException {
        if (lineShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Line.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Line.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Line.fs");
                lineShader = GLTools.loadShaderSourceWithAttributes(gl, "Line", vp, gp, fp,
                        colotTarget, colorShaderName,
                        connectionInfoTarget, connectionInfoShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return lineShader;
    }

    /**
     * Lines further away don't look good as triangles (too many artifacts), so
     * distant lines are drawn as lines (which is more efficient anyway).
     *
     * @param glCurrent the current OpenGL context.
     * @param colotTarget
     * @param colorShaderName
     * @param connectionInfoTarget
     * @param connectionInfoShaderName
     * @return the id of the line shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getLineLineShader(final GL3 glCurrent, final int colotTarget, final String colorShaderName, final int connectionInfoTarget, final String connectionInfoShaderName) throws IOException {
        if (lineLineShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Line.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/LineLine.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/LineLine.fs");
                lineLineShader = GLTools.loadShaderSourceWithAttributes(gl, "LineLine", vp, gp, fp,
                        colotTarget, colorShaderName,
                        connectionInfoTarget, connectionInfoShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return lineLineShader;
    }

    /**
     * Lines close to the camera are drawn as triangles to provide perspective.
     *
     * @param glCurrent the current OpenGL context.
     * @param colorTarget
     * @param colorShaderName
     * @param loopInfoTarget
     * @param loopInfoShaderName
     * @return the id of the loop shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getLoopShader(final GL3 glCurrent, final int colorTarget, final String colorShaderName, final int loopInfoTarget, final String loopInfoShaderName) throws IOException {
        if (loopShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Loop.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Loop.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Loop.fs");
                loopShader = GLTools.loadShaderSourceWithAttributes(gl, "Loop", vp, gp, fp,
                        colorTarget, colorShaderName,
                        loopInfoTarget, loopInfoShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return loopShader;
    }

    /**
     * Each character is drawn individually.
     *
     * @param glCurrent the current OpenGL context.
     * @param labelFloatsTarget The ID of the float buffer in the label batch
     * @param labelFloatsShaderName the name of the float buffer in the shader.
     * @param labelIntsTarget The ID of the int buffer in the label batch
     * @param labelIntsShaderName the name of the int buffer in the shader.
     * @return the id of the shader.
     * @throws IOException if an error occurs while reading the shader source.
     */
    public static int getNodeLabelShader(final GL3 glCurrent, final int labelFloatsTarget, final String labelFloatsShaderName, final int labelIntsTarget, final String labelIntsShaderName) throws IOException {
        if (nodeLabelShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/NodeLabel.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Label.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Label.fs");
                nodeLabelShader = GLTools.loadShaderSourceWithAttributes(gl, "Label", vp, gp, fp,
                        labelFloatsTarget, labelFloatsShaderName,
                        labelIntsTarget, labelIntsShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return nodeLabelShader;
    }

    /**
     * Each character is drawn individually.
     *
     * @param glCurrent the current OpenGL context.
     * @param labelFloatsTarget The ID of the float buffer in the label batch
     * @param labelFloatsShaderName the name of the float buffer in the shader
     * source.
     * @param labelIntsTarget The ID of the int buffer in the label batch
     * @param labelIntsShaderName the name of the int buffer in the shader
     * source.
     * @return the name of the shader.
     * @throws IOException if an error occurs while reader the shader source.
     */
    public static int getConnectionLabelShader(final GL3 glCurrent, final int labelFloatsTarget, final String labelFloatsShaderName, final int labelIntsTarget, final String labelIntsShaderName) throws IOException {
        if (connectionLabelShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/ConnectionLabel.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Label.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Label.fs");
                connectionLabelShader = GLTools.loadShaderSourceWithAttributes(gl, "Label", vp, gp, fp,
                        labelFloatsTarget, labelFloatsShaderName,
                        labelIntsTarget, labelIntsShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return connectionLabelShader;
    }

    /**
     * The blaze shader draws visual attachments to the nodes.
     *
     * @param glCurrent the current OpenGL context.
     * @param colorTarget
     * @param colorShaderName the name of the color buffer in the shader source.
     * @param blazeInfoTarget
     * @param blazeInfoShaderName the name of the int buffer in the shader
     * source.
     * @return the name of the shader.
     * @throws IOException if an error occurs while reader the shader source.
     */
    public static int getBlazeShader(final GL3 glCurrent, final int colorTarget, final String colorShaderName, final int blazeInfoTarget, final String blazeInfoShaderName) throws IOException {
        if (blazeShader == 0) {
            glCurrent.getContext().release();
            try {
                final int result = gl.getContext().makeCurrent();
                if (result == GLContext.CONTEXT_NOT_CURRENT) {
                    glCurrent.getContext().makeCurrent();
                    throw new RenderException(COULD_NOT_CONTEXT_CURRENT);
                }

                final String vp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Blaze.vs");
                final String gp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Blaze.gs");
                final String fp = GLTools.loadFile(GLVisualProcessor.class, "shaders/Blaze.fs");
                blazeShader = GLTools.loadShaderSourceWithAttributes(gl, "Blaze", vp, gp, fp,
                        colorTarget, colorShaderName,
                        blazeInfoTarget, blazeInfoShaderName,
                        ShaderManager.FRAG_BASE, FRAG_COLOR);
            } finally {
                gl.getContext().release();
                glCurrent.getContext().makeCurrent();
            }
        }

        return blazeShader;
    }
}

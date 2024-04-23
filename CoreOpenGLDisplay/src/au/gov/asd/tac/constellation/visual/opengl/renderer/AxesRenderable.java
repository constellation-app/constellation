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

import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.Batch;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.ShaderManager;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulate the JOGL code required to implement a set of axes that mirror the
 * rotation of the main scene.
 * <p>
 * There's no particular reason to pull the code out of the GraphRenderer class,
 * but since the axes are independent of whatever else is being displayed, we'll
 * put the relevant stuff here to avoid cluttering the GraphRenderer code.
 * <p>
 * The drawing of the axes in a fixed position works by extracting the rotation
 * matrix from the MVP matrix (thus removing any translating and scaling done by
 * the scene) and performing a specific translate and scale to the correct place
 * and size. Note that the size of the axes are fixed: we rely on the camera
 * never moving.
 *
 * @author algol
 */
public class AxesRenderable implements GLRenderable {
    
    private static final Logger LOGGER = Logger.getLogger(AxesRenderable.class.getName());

    private static final float LEN = 0.5F;
    private static final float HEAD = 0.05F;
    private static final int AXES_OFFSET = 50;
    private static final Vector4f XCOLOR = new Vector4f(1, 0.5F, 0.5F, 0.75F);
    private static final Vector4f YCOLOR = new Vector4f(0.5F, 1, 0.5F, 0.75F);
    private static final Vector4f ZCOLOR = new Vector4f(0, 0.5F, 1, 0.75F);
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    private static final Matrix44f IDENTITY_44F = Matrix44f.identity();

    private static final int NUMBER_OF_VERTICES = 3 * 2 + 4 + 4 + 4 + 4 + 6 + 6;
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int VERTEX_BUFFER_WIDTH = 3;

    private final GLVisualProcessor parent;
    private final Batch axesBatch;
    private Vector3f topRightCorner;
    private float pScale;

    private int axesShader;
    private int axesShaderLocMVP;
    private final int colorTarget;
    private final int vertexTarget;

    public AxesRenderable(final GLVisualProcessor parent) {
        this.parent = parent;
        axesBatch = new Batch(GL.GL_LINES);
        colorTarget = axesBatch.newFloatBuffer(COLOR_BUFFER_WIDTH, true);
        vertexTarget = axesBatch.newFloatBuffer(VERTEX_BUFFER_WIDTH, true);
    }

    @Override
    public int getPriority() {
        return RenderablePriority.ANNOTATIONS_PRIORITY.getValue();
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        String axesVp = null;
        String axesGp = null;
        String axesFp = null;

        try {
            axesVp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.vs");
            axesGp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThruLine.gs");
            axesFp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.fs");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        topRightCorner = new Vector3f();

        axesShader = GLTools.loadShaderSourceWithAttributes(gl, "PassThru axes", axesVp, axesGp, axesFp,
                vertexTarget, "vertex",
                colorTarget, "color",
                ShaderManager.FRAG_BASE, "fragColor");
        axesShaderLocMVP = gl.glGetUniformLocation(axesShader, "mvpMatrix");

        axesBatch.initialise(NUMBER_OF_VERTICES);
        // x axis
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, ZERO_3F);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN, 0, 0);
        // arrow
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN - HEAD, HEAD, 0);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN, 0, 0);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN, 0, 0);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN - HEAD, -HEAD, 0);
        // X
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, HEAD);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, -HEAD);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, -HEAD);
        axesBatch.stage(colorTarget, XCOLOR);
        axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, HEAD);

        // y axis
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, ZERO_3F);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN, 0);
        // arrow
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN - HEAD, HEAD);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN, 0);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN, 0);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN - HEAD, -HEAD);
        // Y
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, -HEAD, LEN + HEAD, -HEAD);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, HEAD, LEN + HEAD, -HEAD);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        axesBatch.stage(colorTarget, YCOLOR);
        axesBatch.stage(vertexTarget, 0, LEN + HEAD, HEAD);

        // z axis
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, ZERO_3F);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, 0, 0, LEN);
        // arrow
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, -HEAD, 0, LEN - HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, 0, 0, LEN);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, 0, 0, LEN);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, HEAD, 0, LEN - HEAD);
        // Z
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, -HEAD, HEAD, LEN + HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        axesBatch.stage(colorTarget, ZCOLOR);
        axesBatch.stage(vertexTarget, HEAD, -HEAD, LEN + HEAD);

        axesBatch.finalise(gl);
    }

    private float calcProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.a[1] - proj1.a[1];

        return 25.0F / yScale;
    }

    @Override
    public void reshape(final int x, final int y, final int width, final int height) {
        // Whenever the drawable shape changes, recalculate the place where the axes are drawn.
        final int[] viewport = new int[]{x, y, width, height};
        final int dx = width / 2 - AXES_OFFSET;
        final int dy = -height / 2 + AXES_OFFSET;

        pScale = calcProjectionScale(viewport);

        // Use project+unproject to determine how to translate to the top right corner of the viewport.
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, topRightCorner);
    }

    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f projectionMatrix) {
        final GL3 gl = drawable.getGL().getGL3();

        // Extract the rotation matrix from the mvp matrix.
        final Matrix44f rotationMatrix = new Matrix44f();
        parent.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);

        // Scale down to size.
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pScale, pScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, rotationMatrix);

        // Translate to the top right corner.
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(topRightCorner.getX(), topRightCorner.getY(), topRightCorner.getZ());
        final Matrix44f axesMatrix = new Matrix44f();
        axesMatrix.multiply(translationMatrix, srMatrix);

        // Disable depth so the axes are drawn over everything else.
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Draw.
        gl.glLineWidth(1);
        gl.glUseProgram(axesShader);
        gl.glUniformMatrix4fv(axesShaderLocMVP, 1, false, axesMatrix.a, 0);
        axesBatch.draw(gl);

        // Reenable depth.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(true);
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        axesBatch.dispose(gl);
    }
}

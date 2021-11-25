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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Maintain a hit test buffer.
 *
 * As well as drawing objects to the default framebuffer, also draw the objects
 * to an alternate framebuffer, but draw each object with a unique color
 * determined in the shader by the node id.
 * <p>
 * Whenever the mouse is moved, read the current pixel from the alternate
 * framebuffer and convert the unique color back to the node id.
 * <p>
 * It is assumed that node ids are &gt;=0. Since a black background would return
 * 0, we add 1 to the node id in the shader, and subtract 1 here. Change this
 * for a non-black background.
 * <p>
 * The alternate framebuffer is currently GL_R32F format. This gives 22 bits of
 * mantissa, or 4,194,304 ids. Using the sign bit gives another 22 bits. We use
 * positive numbers for node ids, negative numbers for line ids.
 *
 * @author algol
 */
public final class HitTester implements GLRenderable {

    // Framebuffer for hit-testing.
    private final int[] hitTestFboName;
    private final int[] hitTestDepthBufferName;
    private final int[] hitTestRboName;
    // These have to have some non-zero value for initialisation, but we will be set to match the canvas dimensions upon reshape. Seems dodgy...might be worth fixing.
    private int width = 10;
    private int height = 10;
    private boolean needsResize = false;
    // The buffer to read from. This is hardcoded as their seems to be no real reason to change it,
    // but perhaps it should be looked up from the corersponding GraphRenderable. At the moment it simply
    // matches the buffer name used inside the if(doHitTesting) {} block of GraphRenderable's display method.
    private final int hitTestBufferName = GL.GL_COLOR_ATTACHMENT0;
    private final GLVisualProcessor parent;

    private HitTestRequest hitTestRequest;
    private final BlockingDeque<HitTestRequest> requestQueue = new LinkedBlockingDeque<>();
    private final Queue<Queue<HitState>> notificationQueues = new LinkedList<>();

    /**
     *
     * @param parent
     */
    public HitTester(final GLVisualProcessor parent) {
        this.parent = parent;
        hitTestFboName = new int[1];
        hitTestDepthBufferName = new int[1];
        hitTestRboName = new int[1];
    }

    @Override
    public int getPriority() {
        return RenderablePriority.HIGH_PRIORITY.getValue();
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        // Hit testing.
        // Create an FBO name and bind a new FBO.
        gl.glGenFramebuffers(1, hitTestFboName, 0);
        gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, hitTestFboName[0]);

        // Create a depth buffer object and attach it.
        gl.glGenRenderbuffers(1, hitTestDepthBufferName, 0);
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, hitTestDepthBufferName[0]);
        gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL2ES3.GL_DEPTH_COMPONENT32F, width, height);

        // Create an RBO and bind it.
        gl.glGenRenderbuffers(1, hitTestRboName, 0);
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, hitTestRboName[0]);

        // Allocate memory to back the RBO.
        // Using R32F gives us plenty of unique values (2**22 in the mantissa without
        // worrying about floating point stuff).
        gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_R32F, width, height);

        // Attach the render buffers.
        gl.glFramebufferRenderbuffer(GL.GL_DRAW_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, hitTestDepthBufferName[0]);
        gl.glFramebufferRenderbuffer(GL.GL_DRAW_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_RENDERBUFFER, hitTestRboName[0]);

        GLTools.checkFramebufferStatus(gl, "ht-check");
        parent.setHitTestFboName(hitTestFboName[0]);
    }

    /**
     * Resize the depth and RBO buffers.
     *
     * @param x Viewport x.
     * @param y Viewport y.
     * @param width Viewport width.
     * @param height Viewport height.
     */
    @Override
    public void reshape(final int x, final int y, final int width, final int height) {
        this.width = width;
        this.height = height;
        needsResize = true;
    }

    public void queueRequest(final HitTestRequest request) {
        requestQueue.add(request);
    }

    @Override
    public void update(final GLAutoDrawable drawable) {
        if (CollectionUtils.isNotEmpty(requestQueue)) {
            requestQueue.forEach(request -> notificationQueues.add(request.getNotificationQueue()));
            hitTestRequest = requestQueue.getLast();
            requestQueue.clear();
        }
    }

    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f modelViewProjectionMatrix) {
        final GL3 gl = drawable.getGL().getGL3();
        if (needsResize) {
            gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, hitTestDepthBufferName[0]);
            gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT32, width, height);

            gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, hitTestRboName[0]);
            gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_R32F, width, height);

            gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);
            needsResize = false;
        }
        if (!notificationQueues.isEmpty()) {
            final int x = hitTestRequest.getX();
            final int y = hitTestRequest.getY();

            //  Windows-DPI-Scaling
            //
            // If JOGL is ever fixed or another solution is found, either change
            // needsManualDPIScaling to return false (so there is effectively no
            // DPI scaling here) or to remove dpiScaleY below.
            float dpiScaleY = 1.0F;
            if (GLTools.needsManualDPIScaling()) {
                dpiScaleY = parent.getDPIScaleY();
            }
            final int surfaceHeight = (int) (drawable.getSurfaceHeight() * dpiScaleY);

            // Allocate 3 floats for RGB values.
            FloatBuffer fbuf = Buffers.newDirectFloatBuffer(3);

            gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, hitTestFboName[0]);
            gl.glReadBuffer(hitTestBufferName);
            gl.glReadPixels(x, surfaceHeight - y, 1, 1, GL.GL_RGB, GL.GL_FLOAT, fbuf);

            // There are enough colors in the buffer that we only need worry about
            // r component for now. That gives us 2**22 distinct values.
            final int r = (int) (fbuf.get(0));

            final int id;
            final HitType currentHitType;
            if (r == 0) {
                currentHitType = HitType.NO_ELEMENT;
                id = -1;
            } else {
                currentHitType = r > 0 ? HitType.VERTEX : HitType.TRANSACTION;
                id = r > 0 ? r - 1 : -r - 1;
            }

            final HitState hitState = hitTestRequest.getHitState();
            hitState.setCurrentHitId(id);
            hitState.setCurrentHitType(currentHitType);
            if (hitTestRequest.getFollowUpOperation() != null) {
                hitTestRequest.getFollowUpOperation().accept(hitState);
            }
            synchronized (this.notificationQueues) {
                while (!notificationQueues.isEmpty()) {
                    final Queue<HitState> queue = notificationQueues.remove();
                    if (queue != null) {
                        queue.add(hitState);
                    }
                }
            }
        }
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        gl.glDeleteRenderbuffers(1, hitTestRboName, 0);
        gl.glDeleteRenderbuffers(1, hitTestDepthBufferName, 0);
        gl.glDeleteFramebuffers(1, hitTestFboName, 0);
    }
}

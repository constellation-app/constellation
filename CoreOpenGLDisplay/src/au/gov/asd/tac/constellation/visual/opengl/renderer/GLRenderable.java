/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * An interface for a unit that performs drawing operations on a GLContext.
 * <p>
 * A renderable's interface closely mimics that of a {@link GLEventListener}.
 * This is so that these methods can be called by the corresponding methods of
 * such an event listener, such as a {@link GLRenderer}. The main addition is
 * the {@link #update update()} method, which is usually called as part of the
 * <code>display()</code> of the GL life-cycle, but prior to {@link #display}.
 * This method allows updating of data on the card to happen independently of
 * and prior to the passing of data on the card through the shaders.
 *
 * @author twilight_sparkle
 */
public interface GLRenderable extends Comparable<GLRenderable> {

    /**
     * A convenient functional interface for specifying operations which usually
     * involve transfering data to the specified openGL context as part of a
     * call to {@link #update}.
     */
    @FunctionalInterface
    public static interface GLRenderableUpdateTask {

        public void run(final GL3 gl);
    }

    public enum RenderablePriority {
        ANNOTATIONS_PRIORITY(20),
        DEFAULT_PRIORITY(10),
        HIGH_PRIORITY(0);

        private final int value;

        private RenderablePriority(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public default int compareTo(final GLRenderable o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

    /**
     * Get the priority of this renderable.
     * <p>
     * Lower priority renderables have their relevant methods executed earlier
     * by a coordinating {@link GLRenderer} in each phase of the GL life-cycle.
     * Renderables with the same priority are executed earlier if they were
     * added to the renderer earlier.
     *
     * @return
     */
    public default int getPriority() {
        return RenderablePriority.DEFAULT_PRIORITY.getValue();
    }

    /**
     * Initialise this renderable using the specified drawable.
     *
     * @param drawable The drawable to initialise with respect to.
     */
    public void init(final GLAutoDrawable drawable);

    /**
     * Reshape this renderable.
     *
     * @param x The x of the reshaped canvas.
     * @param y The y of the reshaped canvas.
     * @param width The width of the reshaped canvas.
     * @param height The height of the reshaped canvas.
     */
    public default void reshape(final int x, final int y, final int width, final int height) {
    }

    /**
     * Update this renderable using the specified drawable.
     *
     * @param drawable The drawable to update with respect to.
     */
    public default void update(final GLAutoDrawable drawable) {
    }

    /**
     * Display this renderable using the specified drawable.
     *
     * @param drawable The drawable to display with respect to.
     * @param pMatrix The projection matrix of the renderer being displayed on.
     */
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix);

    /**
     * Dispose this renderable using the specified drawable.
     *
     * @param drawable The drawable to dispose with respect to.
     */
    public void dispose(final GLAutoDrawable drawable);
}

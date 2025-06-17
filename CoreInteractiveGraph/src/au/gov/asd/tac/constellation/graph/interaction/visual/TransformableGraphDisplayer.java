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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.visual.opengl.renderer.GraphDisplayer;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import com.jogamp.opengl.GL3;
import java.io.IOException;

/**
 * A {@link GraphDisplayer} that allows the graph to be displayed in greyscale
 * or full color.
 * <p>
 * This effect is currently used to show that the graph is busy when a user
 * tries to interact with it. More effects may be added here later as needed.
 *
 * @author twilight_sparkle
 */
public class TransformableGraphDisplayer extends GraphDisplayer {

    private int greyscaleShaderLocation;
    private boolean greyscale;

    /**
     * Set whether or not the graph should be displayed in greyScale
     *
     * @param greyScale whether or not the graph should be displayed in
     * greyScale.
     */
    public void setGreyscale(final boolean greyScale) {
        this.greyscale = greyScale;
    }

    @Override
    protected String getFragmentShader() throws IOException {
        return GLTools.loadFile(TransformableGraphDisplayer.class, "shaders/Graph.fs");
    }

    @Override
    protected void createShaderLocations(final GL3 gl) {
        greyscaleShaderLocation = gl.glGetUniformLocation(graphTextureShader, "greyscale");
    }

    @Override
    protected void bindShaderLocations(final GL3 gl) {
        gl.glUniform1i(greyscaleShaderLocation, greyscale ? 1 : 0);
    }
}

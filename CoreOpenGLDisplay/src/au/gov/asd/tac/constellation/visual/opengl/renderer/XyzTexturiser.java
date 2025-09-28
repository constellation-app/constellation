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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.SceneBatcher;
import au.gov.asd.tac.constellation.visual.opengl.utilities.FloatTextureBuffer;
import com.jogamp.common.nio.Buffers;
import java.nio.FloatBuffer;

/**
 *
 * @author twilight_sparkle
 */
public class XyzTexturiser {

    private FloatTextureBuffer xyzTexture;

    private static final int XYZ_BUFFER_WIDTH = 8;

    public int getTextureName() {
        return xyzTexture == null ? -1 : xyzTexture.getTextureName();
    }

    public boolean isReady() {
        return xyzTexture != null;
    }

    public GLRenderableUpdateTask dispose() {
        return gl -> {
            if (xyzTexture != null) {
                xyzTexture.dispose(gl);
                xyzTexture = null;
            }
        };
    }

    public GLRenderableUpdateTask createTexture(final VisualAccess access) {
        final FloatBuffer xyzBuffer = Buffers.newDirectFloatBuffer(XYZ_BUFFER_WIDTH * access.getVertexCount());
        for (int i = 0; i < access.getVertexCount(); i++) {
            bufferXyzInfo(i, xyzBuffer, access);
        }
        xyzBuffer.flip();
        return gl -> xyzTexture = new FloatTextureBuffer(gl, xyzBuffer);
    }

    public GLRenderableUpdateTask updateXyzs(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferXyzInfo, xyzTexture::connectBuffer, xyzTexture::disconnectBuffer, XYZ_BUFFER_WIDTH);
    }

    private int bufferXyzInfo(final int pos, final FloatBuffer xyzBuffer, final VisualAccess access) {
        xyzBuffer.put(access.getX(pos));
        xyzBuffer.put(access.getY(pos));
        xyzBuffer.put(access.getZ(pos));
        xyzBuffer.put(access.getRadius(pos));
        xyzBuffer.put(access.getX2(pos));
        xyzBuffer.put(access.getY2(pos));
        xyzBuffer.put(access.getZ2(pos));
        xyzBuffer.put(access.getRadius(pos));
        return pos;
    }

}

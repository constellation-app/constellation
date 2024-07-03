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

import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.SceneBatcher;
import au.gov.asd.tac.constellation.visual.opengl.utilities.ByteTextureBuffer;
import com.jogamp.common.nio.Buffers;
import java.nio.ByteBuffer;

/**
 *
 * @author twilight_sparkle
 */
public class VertexFlagsTexturiser {

    public static final int SELECTED_BIT = 1;
    public static final int DIMMED_BIT = 2;
    public static final int IS_COMPOSITE_PARENT_BIT = 4;
    public static final int RESERVED_BIT_3 = 8;
    public static final int RESERVED_BIT_4 = 16;
    public static final int RESERVED_BIT_5 = 32;
    public static final int RESERVED_BIT_6 = 64;
    public static final int RESERVED_BIT_7 = -128;

    private ByteTextureBuffer vertexFlagsTexture;
    private static final int FLAGS_BUFFER_WIDTH = 1;

    public int getTextureName() {
        return vertexFlagsTexture == null ? -1 : vertexFlagsTexture.getTextureName();
    }

    public boolean isReady() {
        return vertexFlagsTexture != null;
    }

    public GLRenderableUpdateTask dispose() {
        return gl -> {
            if (vertexFlagsTexture != null) {
                vertexFlagsTexture.dispose(gl);
                vertexFlagsTexture = null;
            }
        };
    }

    public GLRenderableUpdateTask createTexture(final VisualAccess access) {
        final ByteBuffer flagsBuffer = Buffers.newDirectByteBuffer(access.getVertexCount());
        for (int i = 0; i < access.getVertexCount(); i++) {
            bufferFlagsInfo(i, flagsBuffer, access);
        }
        flagsBuffer.flip();
        return gl -> vertexFlagsTexture = new ByteTextureBuffer(gl, flagsBuffer);
    }

    public GLRenderableUpdateTask updateFlags(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateByteBufferTask(change, access, this::bufferFlagsInfo, vertexFlagsTexture::connectBuffer, vertexFlagsTexture::disconnectBuffer, FLAGS_BUFFER_WIDTH);
    }

    private int bufferFlagsInfo(final int pos, final ByteBuffer flagsBuffer, final VisualAccess access) {
        final boolean isSelected = access.isVertexSelected(pos);
        final boolean isDimmed = access.isVertexDimmed(pos);
        flagsBuffer.put((byte) ((isDimmed ? DIMMED_BIT : 0) | (isSelected ? SELECTED_BIT : 0)));
        return pos;
    }

}

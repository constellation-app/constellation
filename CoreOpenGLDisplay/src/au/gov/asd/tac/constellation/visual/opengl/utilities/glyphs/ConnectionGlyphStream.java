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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import au.gov.asd.tac.constellation.utilities.graphics.FloatArray;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.ConnectionLabelBatcher;
import static au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.ConnectionLabelBatcher.MAX_STAGGERS;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;

/**
 *
 * @author Nova
 */
public class ConnectionGlyphStream implements GlyphManager.GlyphStream {

    private final FloatArray currentFloats;
    private final IntArray currentInts;
    private float currentWidth;
    private final Object addLock = new Object();

    public ConnectionGlyphStream() {
        this.currentFloats = new FloatArray();
        this.currentInts = new IntArray();
    }

    @Override
    public void addGlyph(int glyphPosition, float x, float y, final GlyphStreamContext streamContext) {
        if (streamContext instanceof ConnectionGlyphStreamContext context) {
            synchronized (addLock) {
                currentFloats.add(currentWidth, x, y, context.visibility);
                currentInts.add(context.currentLowNodeId, context.currentHighNodeId, (context.currentOffset << 16) + (context.totalScale << 2) + context.labelNumber, (glyphPosition << 8) + context.currentStagger * 256 / (Math.min(context.currentLinkLabelCount, ConnectionLabelBatcher.MAX_STAGGERS) + 1));
            }
        }
    }

    @Override
    public void newLine(float width, final GlyphStreamContext streamContext) {
        if (streamContext instanceof ConnectionGlyphStreamContext context) {
            synchronized (addLock) {
                currentWidth = -width / 2.0F - 0.2F;
                currentFloats.add(currentWidth, currentWidth, 0.0F, context.visibility);
                currentInts.add(context.currentLowNodeId, context.currentHighNodeId, (context.currentOffset << 16) + (context.totalScale << 2) + context.labelNumber, (SharedDrawable.getLabelBackgroundGlyphPosition() << 8) + context.currentStagger * 256 / (Math.min(context.currentLinkLabelCount, MAX_STAGGERS) + 1));
            }
        }
    }

    public FloatArray getCurrentFloats() {
        return currentFloats;
    }

    public IntArray getCurrentInts() {
        return currentInts;
    }

    public void trimToSize() {
        synchronized (addLock) {
            currentFloats.trimToSize();
            currentInts.trimToSize();
        }
    }
}

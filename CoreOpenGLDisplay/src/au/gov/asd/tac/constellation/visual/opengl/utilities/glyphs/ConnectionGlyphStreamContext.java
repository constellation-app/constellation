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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

/**
 *
 * @author Nova
 */
public class ConnectionGlyphStreamContext extends GlyphStreamContext {

    public int currentLowNodeId;
    public int currentHighNodeId;
    public int currentLinkLabelCount;
    public int currentStagger;
    public int currentOffset;
    public int nextLeftOffset;
    public int nextRightOffset;

    public ConnectionGlyphStreamContext() {
        super();
    }

    public ConnectionGlyphStreamContext(int totalScale, float visibility, int labelNumber) {
        super(totalScale, visibility, labelNumber);
    }

    public ConnectionGlyphStreamContext(ConnectionGlyphStreamContext context) {
        currentLowNodeId = context.currentLowNodeId;
        currentHighNodeId = context.currentHighNodeId;
        currentLinkLabelCount = context.currentLinkLabelCount;
        currentStagger = context.currentStagger;
        currentOffset = context.currentOffset;
        nextLeftOffset = context.nextLeftOffset;
        nextRightOffset = context.nextRightOffset;
    }
}

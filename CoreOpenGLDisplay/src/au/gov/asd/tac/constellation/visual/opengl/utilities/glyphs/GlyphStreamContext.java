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
 * Contains basic information required in every GlyphStream implementation. Will
 * usually be extended rather than used directly.
 *
 * @author Nova
 */
public class GlyphStreamContext {

    public int totalScale;
    public float visibility;
    public int labelNumber;

    public GlyphStreamContext() {
    }

    public GlyphStreamContext(int totalScale, float visibility, int labelNumber) {
        this.totalScale = totalScale;
        this.visibility = visibility;
        this.labelNumber = labelNumber;
    }
}

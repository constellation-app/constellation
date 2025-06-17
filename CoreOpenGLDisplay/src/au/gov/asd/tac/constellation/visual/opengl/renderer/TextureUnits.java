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

/**
 * Assign unique texture units.
 * <p>
 * Different parts of the scene use different textures; for instance,
 * SceneBatchStore uses one texture for storing x,y,z coordinates and another
 * for storing icons. By assigning the texture unit identifiers here, we can
 * track which texture units are used for drawing across classes.
 * <p>
 * JOGL uses a texture unit for text: don't clash with it.
 *
 * @author algol
 */
public class TextureUnits {
    // Texture unit index values for rendering textures.

    /**
     * Used by SceneBatchStore for storing x,y,z coordinates.
     */
    public static final int VERTICES = 0;

    /**
     * Used by SceneBatchStore for storing icons to draw nodes.
     */
    public static final int ICONS = 1;

    /**
     * Used by SceneBatchStore for storing vertex flags.
     */
    public static final int VERTEX_FLAGS = 2;

    /**
     * Used by PlaneBatchStore for storing overlay images.
     */
    public static final int PLANES = 3;

    /**
     * Used by SceneBatchStore for the text font texture.
     */
    public static final int GLYPHS = 4;

    /**
     * Used by SceneBatchStore for storing information about the characters in
     * labels
     */
    public static final int GLYPH_INFO = 5;
}

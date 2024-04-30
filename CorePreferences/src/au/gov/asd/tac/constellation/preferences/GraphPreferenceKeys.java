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
package au.gov.asd.tac.constellation.preferences;

/**
 * Keys used to access graph preferences.
 *
 * @author aldebaran30701
 */
public final class GraphPreferenceKeys {

    /**
     * Blaze settings
     */
    public static final String BLAZE_SIZE = "blazeSize";
    public static final int BLAZE_SIZE_DEFAULT = 30;
    public static final String BLAZE_OPACITY = "blazeOpacity";
    public static final int BLAZE_OPACITY_DEFAULT = 100;
    public static final String BLAZE_PRESET_COLORS = "blazePresetColors";
    public static final String BLAZE_PRESET_COLORS_DEFAULT = "#FF0000;#0000FF;#FFFF00;";
    public static final String BLAZE_RECENT_COLORS = "blazeRecentColors";
    public static final String BLAZE_RECENT_COLORS_DEFAULT = "";

    /**
     * Anaglyphic Display
     */
    public static final String LEFT_COLOR = "anaglyph.left";
    public static final String RIGHT_COLOR = "anaglyph.right";

    // Why are the default colors green and magenta?
    // Because the DreamWorks movie Monsters vs Aliens came with a 3D short called
    // Bob's Big Break which uses those colors.
    //
    public static final String LEFT_COLOR_DEFAULT = "Green";
    public static final String RIGHT_COLOR_DEFAULT = "Magenta";

    private GraphPreferenceKeys() {
    }

}

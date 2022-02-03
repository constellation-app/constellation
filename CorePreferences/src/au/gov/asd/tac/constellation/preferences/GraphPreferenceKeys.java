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
    public static String BLAZE_PRESET_COLORS_DEFAULT = "#FF0000;#0000FF;#FFFF00;";
    public static final String BLAZE_RECENT_COLORS = "blazeRecentColors";
    public static final String BLAZE_RECENT_COLORS_DEFAULT = "";

    private GraphPreferenceKeys() {
    }
}

/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import org.openide.util.NbPreferences;


/**
 * Keys used to access graph preferences.
 *
 * @author aldebaran30701
 */
public final class GraphPreferenceKeys {

    
    /**
     * Blaze settings
     */
    public static int BLAZE_SIZE_DEFAULT = 50;
    public static final int BLAZE_OPACITY_DEFAULT = 50;
    public static final String BLAZE_SIZE = "blazeSize";
    public static final String BLAZE_OPACITY = "blazeOpacity";

    public static float getDefaultBlazeSize(){
        return (float)BLAZE_SIZE_DEFAULT;
    }
    public static void setDefaultBlazeSize(int newValue){
        BLAZE_SIZE_DEFAULT = newValue;
    }
    
    /**
     * Output window preferences including font family, size and style.
     */
    public static final String OUTPUT2_PREFERENCE = "org/netbeans/core/output2";
    public static final String OUTPUT2_FONT_SIZE = "output.settings.font.size";
    public static final String OUTPUT2_FONT_SIZE_DEFAULT = "12";
    public static final String OUTPUT2_FONT_FAMILY = "output.settings.font.family";
    public static final String OUTPUT2_FONT_FAMILY_DEFAULT = "Dialog";

    /**
     * Charts.
     */
    public static final String CHART_DISPLAY = "chartDisplay";
    public static final String CHART_DISPLAY_CONSTELLATION = "constellation";
    public static final String CHART_DISPLAY_BROWSER = "browser";
    public static final String CHART_DISPLAY_DEFAULT = CHART_DISPLAY_CONSTELLATION;
    /**
     * Scripting.
     */
    public static final String DEFAULT_TEMPLATE = "defaultTemplate";
    public static final String DEFAULT_TEMPLATE_DEFAULT = null;

    private GraphPreferenceKeys() {
    }
}
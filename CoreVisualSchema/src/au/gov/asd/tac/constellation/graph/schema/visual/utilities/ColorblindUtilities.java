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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author centauri0320001
 */
public class ColorblindUtilities {

    private ColorblindUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Adjust RGB values using the to-be removed RGB value as a proportion of the calculation, 
     * acting as contrast booster for brightness adjustments. 
     * Evaluate the selected colorblind mode and adjust contrast if RGB value is high enough; 
     * prevents new color from being too dark, then remove imperceivable colors. 
     * Primary colors for the modes are then adjusted at different strengths to improve contrast. 
     * i.e. remove 50% red in Deuteranopia, remove 18% blue for Protanopia.
     * 
     * @param vertexColor the color to adjust
     * @return The adjusted color for colorblind filter
     */
    public static final ConstellationColor calculateColorBrightness(final ConstellationColor vertexColor) {
        final String colorMode = getApplicationPreferences().get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

        float adjustedRed = vertexColor.getRed();
        float adjustedGreen = vertexColor.getGreen();
        float adjustedBlue = vertexColor.getBlue();
        final float minPrimaryRGBVal = 0.15F;
        final float minimumRGBVal = 0.25F;
        final float minimumAdjustedVal = 0.35F;
        final float minimumCombinedRGB = 0.70F;
        final float brightenRGB = 0.1F;

        switch (colorMode) {
            case "None" -> {
                // Do nothing
            }
            case "Deuteranopia" -> {
                //If the constellation color is primarily composed of a single rgb shade (e.g. Blue) do not adjust the value
                if (vertexColor.getRed() + vertexColor.getBlue() <= minimumCombinedRGB || vertexColor.getBlue() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getRed() >= minimumRGBVal) {
                    adjustedRed = vertexColor.getRed() * vertexColor.getGreen();
                    adjustedRed = adjustedRed <= minimumAdjustedVal ? adjustedRed + brightenRGB : adjustedRed;
                    adjustedBlue = vertexColor.getBlue() / 1.2F;
                }
            }
            case "Protanopia" -> {
                if (vertexColor.getGreen() + vertexColor.getBlue() < minimumCombinedRGB || vertexColor.getRed() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getGreen() >= minimumRGBVal) {
                    adjustedGreen = vertexColor.getGreen() * vertexColor.getRed();
                    adjustedGreen = adjustedGreen <= minimumAdjustedVal ? adjustedGreen + brightenRGB : adjustedGreen;
                    adjustedRed = vertexColor.getRed() / 1.8F;
                }
            }
            case "Tritanopia" -> {
                if (vertexColor.getBlue() + vertexColor.getRed() <= minimumCombinedRGB || vertexColor.getGreen() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getBlue() >= minimumRGBVal) {
                    adjustedBlue = vertexColor.getBlue() * vertexColor.getRed();
                    adjustedBlue = adjustedBlue <= minimumAdjustedVal ? adjustedBlue + brightenRGB : adjustedBlue;
                    adjustedGreen = vertexColor.getGreen() / 1.05F;
                }
            }
            default -> {
                // Do nothing
            }
        }

        return ConstellationColor.getColorValue(adjustedRed, adjustedGreen, adjustedBlue, vertexColor.getAlpha());
    }
    
    protected static Preferences getApplicationPreferences() {
        return NbPreferences.forModule(ApplicationPreferenceKeys.class);
    }
}

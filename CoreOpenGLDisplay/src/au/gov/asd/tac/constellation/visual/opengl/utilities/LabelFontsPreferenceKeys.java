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
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.ConstellationLabelFonts;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.FontInfo;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManagerBI;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preference keys for label font settings.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class LabelFontsPreferenceKeys {

    public static final String USE_DEFAULTS = "render.font.use_defaults";
    public static final String USE_MULTI_FONTS = "render.font.use_multi";
    public static final String FONT_LIST = "render.font.list";

    public static final boolean USE_DEFAULTS_DEFAULT = true;
    public static final boolean USE_MULTI_FONTS_DEFAULT = ConstellationLabelFonts.getDefault().isUseMultiFontsChosen();
    public static final String FONT_LIST_DEFAULT = ConstellationLabelFonts.getDefault().getFontListString();

    public static boolean useMultiFontLabels() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        return prefs.getBoolean(USE_MULTI_FONTS, USE_MULTI_FONTS_DEFAULT);
    }

    public static String getFontText() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        return prefs.get(FONT_LIST, FONT_LIST_DEFAULT);
    }

    public static FontInfo[] getFontInfo() {
        final String text = getFontText();
        final FontInfo.ParsedFontInfo pfi = FontInfo.parseFontInfo(text.split(SeparatorConstants.NEWLINE), GlyphManagerBI.DEFAULT_FONT_SIZE);

        return pfi.fontsInfo;
    }

    private LabelFontsPreferenceKeys() {
    }
}

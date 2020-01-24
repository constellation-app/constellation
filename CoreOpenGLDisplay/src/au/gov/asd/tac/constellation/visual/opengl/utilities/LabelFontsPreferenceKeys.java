package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.FontInfo;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManagerBI;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author algol
 */
public class LabelFontsPreferenceKeys {

    public static final String FONT_LIST = "render.font.list";
    public static final String USE_MULTI_FONTS = "render.font.use_multi";

    public static boolean useMultiFontLabels() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        final boolean use = prefs.getBoolean(USE_MULTI_FONTS, true);

        return use;
    }

    public static String getFontText() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        final String text = prefs.get(LabelFontsPreferenceKeys.FONT_LIST, "");

        return text;
    }

    public static FontInfo[] getFontNames() {
        final String text = getFontText();
        final FontInfo.ParsedFontInfo pfi = FontInfo.parseFontInfo(text.split("\n"), GlyphManagerBI.DEFAULT_FONT_SIZE);

        return pfi.fontsInfo;
    }

    private LabelFontsPreferenceKeys() {
    }
}

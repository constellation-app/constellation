package au.gov.asd.tac.constellation.visual.opengl.utilities;

import java.util.Arrays;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author algol
 */
public class LabelFontsPreferenceKeys {
    public static final String FONT_LIST = "render.font.list";

    public static String[] getFontNames() {
        final Preferences prefs = NbPreferences.forModule(LabelFontsPreferenceKeys.class);
        final String text = prefs.get(LabelFontsPreferenceKeys.FONT_LIST, "");
        return Arrays.stream(text.split("\n")).filter(line -> {
            line = line.trim();
            return line.length()>0 && !line.startsWith("#");
        }).toArray(String[]::new);
    }

    private LabelFontsPreferenceKeys() {}
}

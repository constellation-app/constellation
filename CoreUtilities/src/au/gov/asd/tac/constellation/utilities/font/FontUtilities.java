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
package au.gov.asd.tac.constellation.utilities.font;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.openide.util.NbPreferences;

/**
 * Font Utilities
 *
 * @author cygnus_x-1
 */
public class FontUtilities {

    private static final Logger LOGGER = Logger.getLogger(FontUtilities.class.getName());

    /**
     * Used by LAF (Look and Feel)
     */
    private static final String SWING_FONT = "Label.font";

    // Somewhere to stash the fonts.
    // Unless the user plays with font sizes, there will typically only be one font instance stored
    // and shared amongst everything.
    private static final Map<String, Font> FONTS = new HashMap<>();
    private static final Map<String, Font> OUTPUT_FONTS = new HashMap<>();

    private FontUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Set the default font size and family preferences if they are not already defined.
     * <p>
     * Top Components listen for changes in ApplicationFontPreferenceKeys.FONT_PREFERENCES and if its not defined then
     * the listener will not be registered. This initialise method is called when the application starts to make sure a
     * preference is defined.
     */
    public static synchronized void initialiseApplicationFontPreferenceOnFirstUse() {
        final Preferences p = NbPreferences.root();
        try {
            if (!p.nodeExists(ApplicationPreferenceKeys.FONT_PREFERENCES)) {
                p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
                p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
            }
        } catch (final BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Return a cached font to be used for the application.
     * <p>
     * The font is based on the user's font preference.
     *
     * @return A Font.
     */
    public static synchronized Font getApplicationFont() {
        final int fontSize = getApplicationFontSize();
        final String fontFamily = getApplicationFontFamily();
        final String fontKey = String.format("%s//%d", fontFamily, fontSize);
        if (!FONTS.containsKey(fontKey)) {
            FONTS.put(fontKey, new Font(fontFamily, Font.PLAIN, fontSize));
        }
        return FONTS.get(fontKey);
    }

    /**
     * Return the user's default font size.
     * <p>
     * This retrieves the font size specified in Setup &rarr; Options &rarr; Constellation &rarr; Application Font
     * &rarr; Font Size. The default if not specified is 12.
     *
     * @return The user's default font size.
     */
    public static int getApplicationFontSize() {
        int fontSize;
        try {
            final Preferences p = NbPreferences.root();
            if (p.nodeExists(ApplicationPreferenceKeys.FONT_PREFERENCES)) {
                final String fontSizePreference = p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_SIZE,
                        ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
                fontSize = Integer.parseInt(fontSizePreference);
            } else {
                fontSize = UIManager.getFont(SWING_FONT).getSize();
            }
        } catch (final BackingStoreException | NumberFormatException ex) {
            fontSize = Integer.parseInt(ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
            LOGGER.severe(ex.getLocalizedMessage());
        }
        LOGGER.log(Level.FINE, "Font size is {0}", fontSize);
        return fontSize;
    }

    /**
     * Return the user's default font family.
     * <p>
     * This retrieves the font family specified in Setup &rarr; Options &rarr; Constellation &rarr; Application Font
     * &rarr; Default Font. The default if not specified is Arial.
     *
     * @return The user's default font family.
     */
    public static String getApplicationFontFamily() {
        String fontFamily;
        try {
            final Preferences p = NbPreferences.root();
            if (p.nodeExists(ApplicationPreferenceKeys.FONT_PREFERENCES)) {
                fontFamily = p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
            } else {
                fontFamily = ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT;
            }
        } catch (final BackingStoreException ex) {
            fontFamily = ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT;
            LOGGER.severe(ex.getLocalizedMessage());
        }

        LOGGER.log(Level.FINE, "Font family is {0}", fontFamily);
        return fontFamily;
    }

    /**
     * Set the default font size and family as preferences if they are not already defined.
     * <p>
     * Top Components listen for changes in ApplicationPreferenceKeys.OUTPUT2_PREFERENCE and if its not defined then the
     * listener will not be registered. This initialise method is called when the application starts to make sure a
     * preference is defined.
     */
    public static synchronized void initialiseOutputFontPreferenceOnFirstUse() {
        final Preferences p = NbPreferences.root();
        try {
            if (!p.nodeExists(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)) {
                p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).put(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);
            }
        } catch (final BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Return a cached font to be used for the output window.
     * <p>
     * The font is based on the user's font preference.
     *
     * @return A Font.
     */
    public static synchronized Font getOutputFont() {
        final int fontSize = getOutputFontSize();
        final String fontFamily = getOutputFontFamily();
        final String fontKey = String.format("%s//%d", fontFamily, fontSize);
        if (!OUTPUT_FONTS.containsKey(fontKey)) {
            OUTPUT_FONTS.put(fontKey, new Font(fontFamily, Font.PLAIN, fontSize));
        }

        return OUTPUT_FONTS.get(fontKey);
    }

    /**
     * Return the user's default font size.
     * <p>
     * This retrieves the font size specified in Setup &rarr; Options &rarr; Miscellaneous &rarr; Output &rarr; Font
     * Size. The default if not specified is 11.
     *
     * @return The user's default font size.
     */
    public static int getOutputFontSize() {
        int fontSize;
        try {
            final Preferences p = NbPreferences.root();
            if (p.nodeExists(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)) {
                final String fontSizePreference = p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).get(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);
                fontSize = Integer.parseInt(fontSizePreference);
            } else {
                fontSize = UIManager.getFont(SWING_FONT).getSize();
            }
        } catch (final BackingStoreException | NumberFormatException ex) {
            fontSize = Integer.parseInt(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);
            LOGGER.severe(ex.getLocalizedMessage());
        }

        LOGGER.log(Level.FINE, "Font size is {0}", fontSize);
        return fontSize;
    }

    /**
     * Return the user's default font family.
     * <p>
     * This retrieves the font family specified in Setup &rarr; Options &rarr; Miscellaneous &rarr; Output &rarr; Font
     * Size. The default if not specified is Dialog.
     *
     * @return The user's default font family.
     */
    public static String getOutputFontFamily() {
        String fontFamily;
        try {
            final Preferences p = NbPreferences.root();
            if (p.nodeExists(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)) {
                fontFamily = p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).get(ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY, ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY_DEFAULT);
            } else {
                fontFamily = ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY_DEFAULT;
            }
        } catch (final BackingStoreException ex) {
            fontFamily = ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY_DEFAULT;
            LOGGER.severe(ex.getLocalizedMessage());
        }

        LOGGER.log(Level.FINE, "Font family is {0}", fontFamily);
        return fontFamily;
    }
}

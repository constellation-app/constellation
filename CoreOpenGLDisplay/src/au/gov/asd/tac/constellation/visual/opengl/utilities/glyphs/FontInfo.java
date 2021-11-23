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

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Describes a font and all its properties.
 *
 * @author algol
 */
public class FontInfo {

    private static final Logger LOGGER = Logger.getLogger(FontInfo.class.getName());

    final String fontName;
    final int fontStyle;
    final Set<Character.UnicodeScript> mustHave;
    final Set<Character.UnicodeScript> mustNotHave;
    final Font font;

    public FontInfo(final String fontName, final int fontStyle, final Set<Character.UnicodeScript> mustHave, final Set<Character.UnicodeScript> mustNotHave, final Font font) {
        this.fontName = fontName;
        this.fontStyle = fontStyle;
        this.mustHave = mustHave != null ? mustHave : Collections.emptySet();
        this.mustNotHave = mustNotHave != null ? mustNotHave : Collections.emptySet();
        this.font = font;
    }

    public FontInfo(final String fontName, final int fontStyle, final int fontSize, final Set<Character.UnicodeScript> mustHave, final Set<Character.UnicodeScript> mustNotHave) {
        this(fontName, fontStyle, mustHave, mustNotHave, getFont(fontName, fontStyle, fontSize));
    }

    private static Font getFont(final String fontName, final int fontStyle, final int fontSize) {
        Font font = null;
        if (StringUtils.endsWithIgnoreCase(fontName, FileExtensionConstants.OTF_EXTENSION) || StringUtils.endsWithIgnoreCase(fontName, FileExtensionConstants.TTF_EXTENSION)) {
            File otfFile = getOtfFont(fontName);
            if (otfFile != null) {
                LOGGER.log(Level.INFO, "Reading font from {0}", otfFile);
                try {
                    final Font otf = Font.createFont(Font.TRUETYPE_FONT, otfFile);
                    font = otf.deriveFont(fontStyle, fontSize);
                } catch (final FontFormatException | IOException ex) {
                    final String msg = String.format("Can't load font %s from %s", fontName, otfFile);
                    LOGGER.log(Level.SEVERE, msg, ex);
                    throw new IllegalArgumentException(msg);
                }
            } else {
                final String msg = String.format("Font file %s not found", fontName);
                LOGGER.info(msg);
                throw new IllegalArgumentException(msg);
            }
        } else {
            font = new Font(fontName, fontStyle, fontSize);
        }

        if (font.getFamily(Locale.ENGLISH).equals(Font.DIALOG)) {
            // From the Javadoc:
            // If the name parameter represents something other than a logical
            // font, i.e. is interpreted as a physical font face or family, and
            // this cannot be mapped by the implementation to a physical font or
            // a compatible alternative, then the font system will map the Font
            // instance to "Dialog", such that for example, the family as
            // reported by getFamily will be "Dialog".
            throw new IllegalArgumentException(String.format("Font '%s' is not available", fontName));
        }

        return font;
    }

    /**
     * Determine if a codepoint can (or should) be displayed by this font.
     * <p>
     *
     * @param codepoint A Unicode codepoint.
     *
     * @return True if the font will display the codepoint, false otherwise.
     */
    public boolean canDisplay(final int codepoint) {
        if (font.canDisplay(codepoint)) {
            final Character.UnicodeScript script = Character.UnicodeScript.of(codepoint);
            if (!mustHave.isEmpty() && mustHave.contains(script)) {
                return true;
            } else if (!mustNotHave.isEmpty() && !mustNotHave.contains(script)) {
                return true;
            } else if (mustHave.isEmpty() && mustNotHave.isEmpty()) {
                return true;
            } else {
                // Do nothing
            }
        }

        return false;
    }

    /**
     * Is this font a suitable font of last resort?
     * <p>
     * The font must not exclude any Unicode scripts.
     *
     * @param defaultName The name of the default font used when all other fonts
     * aren't suitable.
     *
     * @return True if this is a suitable default font, otherwise false.
     */
    public boolean isDefault(final String defaultName) {
        return fontName.trim().equalsIgnoreCase(defaultName)
                && mustHave.isEmpty()
                && mustNotHave.isEmpty();
    }

    /**
     * Find the File specified by the given OTF font name.
     *
     * @param otfName A font file (probably ending with ".otf" or ".ttf".
     *
     * @return A File specifying the font file, or null if it doesn't exist.
     */
    private static File getOtfFont(final String otfName) {
        File otfFile = new File(otfName);
        if (otfFile.isAbsolute()) {
            return otfFile.canRead() ? otfFile : null;
        } else {
            // If it is relative, look in operating system specific places for the font file.
            final String osName = System.getProperty("os.name");
            if (StringUtils.containsIgnoreCase(osName, "win")) {
                // Look in the user's local profile, then the system font directory.
                final String lap = System.getenv("LOCALAPPDATA");
                if (lap != null) {
                    otfFile = new File(String.format("%s/Microsoft/Windows/Fonts/%s", lap, otfName));
                    if (otfFile.canRead()) {
                        return otfFile;
                    } else {
                        final String windir = System.getenv("windir");
                        otfFile = new File(String.format("%s/Fonts/%s", windir, otfName));
                        if (otfFile.canRead()) {
                            return otfFile;
                        }
                    }
                }
            } else {
                // Figure out something for Linux, etc.
                return null;
            }
        }

        return null;
    }

    public static class ParsedFontInfo {

        public final FontInfo[] fontsInfo;
        public final List<String> messages;

        private ParsedFontInfo(final List<FontInfo> fontInfoList, final List<String> messages) {
            fontsInfo = fontInfoList.stream().toArray(FontInfo[]::new);
            this.messages = messages;
        }

        public String getMessages() {
            return messages.stream().collect(Collectors.joining(SeparatorConstants.NEWLINE));
        }
    }

    /**
     * Parse lines of a string to find fontName[,bold|plain|block]... for each
     * line.
     *
     * @param lines
     * @param fontSize
     *
     * @return
     */
    public static ParsedFontInfo parseFontInfo(final String[] lines, final int fontSize) {
        final List<FontInfo> fiList = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        int lineno = 0;
        for (String line : lines) {
            lineno++;
            int fontStyle = Font.PLAIN;
            final Set<Character.UnicodeScript> mustHave = new HashSet<>();
            final Set<Character.UnicodeScript> mustNotHave = new HashSet<>();
            boolean ok = true;
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                final String[] parts = line.trim().split("\\p{Zs}*,\\p{Zs}*");
                final String fontName = parts[0];
                if (fontName.isEmpty()) {
                    ok = false;
                    messages.add(String.format("Line %d: Blank font name", lineno));
                } else {
                    for (int i = 1; i < parts.length; i++) {
                        String part = parts[i].toUpperCase();
                        if (part.isEmpty()) {
                            ok = false;
                            messages.add(String.format("Line %d: Blank font description", lineno));
                        } else {
                            switch (part) {
                                case "PLAIN":
                                    fontStyle = Font.PLAIN;
                                    break;
                                case "BOLD":
                                    fontStyle = Font.BOLD;
                                    break;
                                case "ITALIC":
                                    fontStyle = Font.ITALIC;
                                    break;
                                case "BOLD_ITALIC":
                                    fontStyle = Font.BOLD | Font.ITALIC;
                                    break;
                                default:
                                    final boolean mustNot = part.startsWith("!");
                                    if (mustNot) {
                                        part = part.substring(1);
                                    }
                                    try {
                                        final Character.UnicodeScript script = Character.UnicodeScript.forName(part);
                                        final Set<Character.UnicodeScript> s = mustNot ? mustNotHave : mustHave;
                                        s.add(script);
                                    } catch (final IllegalArgumentException ex) {
                                        ok = false;
                                        messages.add(String.format("Line %d: Font style or unicode script '%s' does not exist", lineno, part));
                                    }
                                    break;
                            }
                        }
                    }
                }

                if (ok) {
                    try {
                        final Font font = getFont(fontName, fontStyle, fontSize);
                        final FontInfo fi = new FontInfo(fontName, fontStyle, mustHave, mustNotHave, font);
                        fiList.add(fi);
                    } catch (final IllegalArgumentException ex) {
                        messages.add(ex.getMessage());
                    }
                }
            }
        }

        return new ParsedFontInfo(fiList, messages);
    }

    @Override
    public String toString() {
        return String.format("[FontInfo[%s] %s style:%d must:%s mustNot:%s]", font, fontName, fontStyle, mustHave, mustNotHave);
    }
}

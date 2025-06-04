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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.List;
import org.openide.util.Lookup;

/**
 * Provides fonts for use as labels in a graph.
 *
 * @author cygnus_x-1
 */
public interface ConstellationLabelFonts {

    /**
     * Specify whether to use a list of fonts, or just one.
     *
     * @return True is you want to use a list of fonts, false otherwise.
     */
    public default boolean isUseMultiFontsChosen() {
        return true;
    }

    /**
     * A list of fonts in order of preference.
     *
     * @return A list of font names.
     */
    public List<String> getFontList();

    /**
     * A string representation of the font list.
     *
     * @return A string representation of the fonts.
     */
    public default String getFontListString() {
        final StringBuilder fontList = new StringBuilder();
        getFontList().forEach(font -> fontList.append(font).append(SeparatorConstants.NEWLINE));
        return fontList.toString();
    }

    /**
     * Get the default ConstellationLabelFonts. This is the registered
     * ConstellationLabelFonts class with the lowest position value.
     *
     * @return The default ConstellationLabelFonts.
     */
    public static ConstellationLabelFonts getDefault() {
        return Lookup.getDefault().lookup(ConstellationLabelFonts.class);
    }
}

/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.javafx;

/**
 * Manages the CSS style sheet common to all CONSTELLATION JavaFX components.
 *
 * @author twinkle2_little
 */
public class JavafxStyleManager {

    public static final String CSS_BASE_STYLE_PREFIX = "-fx-base:";
    public static final String CSS_FONT_WEIGHT_BOLD = "-fx-font-weight: bold";
    public static final String CSS_BACKGROUND_COLOR_TRANSPARENT = "-fx-background-color: transparent;";

    public static final String UNEDITABLE_COMBOBOX = "uneditableCombo";
    public static final String HIDDEN = "hidden";
    public static final String LIGHT_NAME_TEXT = "lightNameText";
    public static final String LIGHT_MESSAGE_TEXT = "lightMessageText";

    public static String getMainStyleSheet() {
        return JavafxStyleManager.class.getResource("main.css").toExternalForm();
    }
}

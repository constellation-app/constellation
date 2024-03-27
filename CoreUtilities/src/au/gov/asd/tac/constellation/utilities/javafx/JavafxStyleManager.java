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
package au.gov.asd.tac.constellation.utilities.javafx;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 * Manages the CSS style sheet common to all Constellation JavaFX components.
 *
 * @author twinkle2_little
 * @author arcturus
 */
public class JavafxStyleManager {

    private static final Logger LOGGER = Logger.getLogger(JavafxStyleManager.class.getName());

    private static String dynamicStyleSheet = null;
    private static String currentFontFamily = null;
    private static int currentFontSize = 0;

    public static Collection<String> getMainStyleSheet() {
        return Arrays.asList(
                JavafxStyleManager.class.getResource("main.css").toExternalForm(), 
                JavafxStyleManager.class.getResource(isDarkTheme() ? "dark.css" : "light.css").toExternalForm()
        );
    }
    
    public static boolean isDarkTheme(){
        return UIManager.getLookAndFeel().getName().toUpperCase().contains("DARK");
    }

    /**
     * Dynamically create a style sheet that will include the new font values so
     * that it can be added to the list of style sheets to be applied.
     * <p>
     * Inspiration taken from https://stackoverflow.com/a/44409349
     * </p>
     *
     * @return
     */
    public static String getDynamicStyleSheet() {
        final double titleSize = FontUtilities.getApplicationFontSize() * 1.5;
        final double smallInfoTextSize = FontUtilities.getApplicationFontSize() * 0.8;
        final double infoTextSize = FontUtilities.getApplicationFontSize() * 0.8;

        if (dynamicStyleSheet == null || hasFontChanged()) {
            currentFontSize = FontUtilities.getApplicationFontSize();
            currentFontFamily = FontUtilities.getApplicationFontFamily();
            try {
                // create a new temp file that will be removed as the application exits
                final File tempStyleClass = File.createTempFile("dynamic", FileExtensionConstants.CASCADING_STYLE_SHEET);
                tempStyleClass.deleteOnExit();

                // dynamically write the style sheet
                try (final PrintWriter printWriter = new PrintWriter(tempStyleClass)) {
                    printWriter.println(String.format("#title { -fx-font-size: %fpx; -fx-font-family:\"%s\"; }", titleSize, currentFontFamily));
                    printWriter.println(String.format("#smallInfoText { -fx-font-size: %fpx; -fx-font-family:\"%s\"; }", smallInfoTextSize, currentFontFamily));
                    printWriter.println(String.format("#infoText { -fx-font-size: %fpx; -fx-font-family:\"%s\"; }", infoTextSize, currentFontFamily));
                    printWriter.println(String.format(".button { -fx-font-size: %dpx; -fx-font-family:\"%s\"; }", currentFontSize, currentFontFamily));
                    printWriter.println(String.format(".label { -fx-font-size: %dpx; -fx-font-family:\"%s\"; }", currentFontSize, currentFontFamily));
                    printWriter.println(String.format(".root { -fx-font-size: %dpx; -fx-font-family:\"%s\"; }", currentFontSize, currentFontFamily));
                }

                dynamicStyleSheet = tempStyleClass.toURI().toString();
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, ex.toString());
            }
        }

        return dynamicStyleSheet;
    }

    private static boolean hasFontChanged() {
        return FontUtilities.getApplicationFontSize() != currentFontSize
                || !FontUtilities.getApplicationFontFamily().equals(currentFontFamily);
    }
}

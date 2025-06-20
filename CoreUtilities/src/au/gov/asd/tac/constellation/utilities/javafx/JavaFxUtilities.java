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
package au.gov.asd.tac.constellation.utilities.javafx;

import javafx.scene.paint.Color;

/**
 *
 * @author Quasar985
 */
public class JavaFxUtilities {

    public static Color awtColorToFXColor(final java.awt.Color awtColor) {
        final int r = awtColor.getRed();
        final int g = awtColor.getGreen();
        final int b = awtColor.getBlue();
        final int a = awtColor.getAlpha();
        final double opacity = a / 255.0;

        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
}

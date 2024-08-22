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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Point;
import java.awt.Window;

/**
 *
 * @author Andromeda-224
 */
public class ScreenWindowsHelper {
    
     /**
     * Get a new point in the centre of the Main Window.
     * @return new centre Point or null if not able to.
     */
    public static Point getNewCentrePoint() {
        Point newPoint = null;
        for (final Window window : Window.getWindows()) {
            final Window mainWin = window.getName().contains("MainWindow") ? window : null;
            if (mainWin != null) {
                newPoint = new Point((int) (mainWin.getX() + (mainWin.getSize().getWidth() / 2)),
                        (int) ((mainWin.getY() + mainWin.getSize().getHeight() / 2)));
                break;
            }
        }
        return newPoint;
    }
}

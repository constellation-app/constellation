/*
 * Copyright 2010-2022 Australian Signals Directorate
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

package au.gov.asd.tac.constellation.functionality.startup;

import java.awt.Color;
import java.util.logging.Logger;

/**
 * This Pseudo class is used for testing purposes as a Reflection target.
 *
 * @author OrionsGuardian
 */
public class PseudoTabDisplayerUI {
    private static final Logger LOGGER = Logger.getLogger(PseudoTabDisplayerUI.class.getName());
    // The colorsReady variable is used in Windows FlatLAf and Metal Look and Feel classes.
    private static boolean colorsReady = false;
    
    // The actBgColor and inactBgColor variable are only used in Metal Look and Feel classes.
    private static Color actBgColor = Color.LIGHT_GRAY;
    private static Color inactBgColor = Color.GRAY;

    PseudoTabDisplayerUI() {
        actBgColor = Color.WHITE;
        inactBgColor = Color.BLACK;
    }

    private static void initColors() {
        colorsReady = true;
        LOGGER.info("Called PseudoTabDisplayerUI.initColors()");
    }
    
    private static Color getColor(boolean active){
        return active ? actBgColor : inactBgColor;
    }
    
    private static boolean getColorsReady(){
        return colorsReady;
    }
    
}

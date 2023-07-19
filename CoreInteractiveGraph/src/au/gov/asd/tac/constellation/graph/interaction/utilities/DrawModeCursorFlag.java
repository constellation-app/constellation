/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.utilities;

/**
 * This class sets a Boolean to be called for determining whether Draw Mode has been enabled.
 *
 * @author centauri0320001
 */
public class DrawModeCursorFlag {

    private static boolean drawModeEnabled = true;

    private DrawModeCursorFlag() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean getDrawModeEnabled() {
        return drawModeEnabled;
    }

    public static void setDrawModeEnabled(final boolean drawModeEnabled) {
        DrawModeCursorFlag.drawModeEnabled = drawModeEnabled;
    }
}

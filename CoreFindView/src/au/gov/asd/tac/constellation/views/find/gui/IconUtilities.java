/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.gui;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

/**
 * Taken from org.netbeans.beaninfo.editors.ColorEditor.
 *
 * @author algol
 */
public class IconUtilities {

    private static final boolean ANTIALIAS = Boolean.getBoolean("nb.cellrenderer.antialiasing") // NOI18N
            || Boolean.getBoolean("swing.aatext") // NOI18N
            //        || (GTK && gtkShouldAntialias()) // NOI18N
            //        || AQUA
            ;
    private static Map<Object, Object> hintsMap;

    @SuppressWarnings("unchecked")
    public static Map<?, ?> getHints() {
        if (hintsMap == null) {
            hintsMap = (Map) (Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<>();
                if (ANTIALIAS) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
        }
//        Boolean.getBoolean(null);
        return hintsMap;
    }
}

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
package au.gov.asd.tac.constellation.preferences;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;

/**
 * Set the default look and feel to be the FlatLaf Dark theme. This needs to run
 * when modules are loaded so that the LAF can be set to the Swing parts of the
 * application before they load.
 *
 * @author arcturus
 */
public class LookAndFeelModuleInstall extends ModuleInstall {

    private static final String FLAT_DARK_THEME_CLASS = "com.formdev.flatlaf.FlatDarkLaf";
    private static final String FLAT_DARK_THEME_NAME = "FlatLaf Dark";
    private static final String LAF = "laf";
    private static final String NO_THEME_SET = "No Theme Set";

    @Override
    public void validate() throws IllegalStateException {
        if (NO_THEME_SET.equals(NbPreferences.root().node(LAF).get(LAF, NO_THEME_SET))) {
            NbPreferences.root().node(LAF).put(LAF, FLAT_DARK_THEME_CLASS);
            UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo(FLAT_DARK_THEME_NAME, FlatDarkLaf.class.getName()));
        }
    }
}
